package com.monke.monkeybook.widget.page;

import android.annotation.SuppressLint;
import android.os.Handler;

import com.monke.basemvplib.BaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.monke.monkeybook.utils.NetworkUtil.isNetWorkAvailable;

/**
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {
    private static final String TAG = "NetPageLoader";
    private List<String> downloadingChapterList = new ArrayList<>();
    private ExecutorService executorService;
    private Scheduler scheduler;
    private Handler handler = new Handler();

    NetPageLoader(PageView pageView, BookShelfBean collBook) {
        super(pageView, collBook);
        executorService = Executors.newFixedThreadPool(10);
        scheduler = Schedulers.from(executorService);
    }

    @Override
    public void refreshChapterList() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (mCollBook.getChapterList().size() == 0) {
                mCollBook.getBookInfoBean().setChapterList(BookshelfHelp.getChapterList(mCollBook.getNoteUrl()));
                mCollBook.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(mCollBook.getBookInfoBean().getName()));
            }
            e.onNext(mCollBook.getChapterList().size() > 0);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mPageView.getActivity().bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            isChapterListPrepare = true;

                            // 目录加载完成，执行回调操作。
                            if (mPageChangeListener != null) {
                                mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
                            }

                            // 打开章节
                            skipToChapter(mCollBook.getDurChapter(), mCollBook.getDurChapterPage());
                        } else {
                            loadChapterList();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        chapterError(e.getMessage());
                    }
                });
    }

    private void loadChapterList() {
        WebBookModelImpl.getInstance().getChapterList(mCollBook)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mPageView.getActivity().bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        isChapterListPrepare = true;

                        // 存储章节到数据库
                        mCollBook.setFinalRefreshData(System.currentTimeMillis());

                        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(mCollBook.getChapterList());
                        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplaceInTx(mCollBook);

                        // 提示目录加载完成
                        if (mPageChangeListener != null) {
                            mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
                        }

                        // 加载并显示当前章节
                        skipToChapter(mCollBook.getDurChapter(), mCollBook.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        chapterError(e.getMessage());
                    }
                });
    }

    @SuppressLint("DefaultLocale")
    public synchronized void loadContent(final int chapterIndex) {
        if (null != mCollBook && mCollBook.getChapterListSize() > 0) {
            Observable.create((ObservableOnSubscribe<Integer>) e -> {
                if (!BookshelfHelp.isChapterCached(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),
                        chapterIndex, mCollBook.getChapterList(chapterIndex).getDurChapterName())
                        && !DownloadingList(listHandle.CHECK, mCollBook.getChapterList(chapterIndex).getDurChapterUrl())) {
                    DownloadingList(listHandle.ADD, mCollBook.getChapterList(chapterIndex).getDurChapterUrl());
                    e.onNext(chapterIndex);
                }
                e.onComplete();
            })
                    .flatMap(index -> WebBookModelImpl.getInstance().getBookContent(scheduler, mCollBook.getBookInfoBean().getName(), mCollBook.getChapterList(index).getDurChapterUrl(), index, mCollBook.getTag()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mPageView.getActivity()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new Observer<BookContentBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            handler.postDelayed(() -> {
                                DownloadingList(listHandle.REMOVE, mCollBook.getChapterList(chapterIndex).getDurChapterUrl());
                                d.dispose();
                            }, 30 * 1000);
                        }

                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onNext(BookContentBean bookContentBean) {
                            DownloadingList(listHandle.REMOVE, bookContentBean.getDurChapterUrl());
                            finishContent(bookContentBean.getDurChapterIndex());
                        }

                        @Override
                        public void onError(Throwable e) {
                            DownloadingList(listHandle.REMOVE, mCollBook.getChapterList(chapterIndex).getDurChapterUrl());
                            if (chapterIndex == mCurChapterPos) {
                                chapterError(e.getMessage());
                            }
                        }

                        @Override
                        public void onComplete() {}
                    });
        }
    }

    /**
     * 编辑下载列表
     */
    private synchronized boolean DownloadingList(listHandle editType, String value) {
        if (editType == listHandle.ADD) {
            downloadingChapterList.add(value);
            return true;
        } else if (editType == listHandle.REMOVE) {
            downloadingChapterList.remove(value);
            return true;
        } else {
            return downloadingChapterList.indexOf(value) != -1;
        }
    }

    /**
     * 章节下载完成
     */
    private void finishContent(int chapterIndex) {
        if (chapterIndex == mCurChapterPos || mCurChapter.getStatus() != Enum.PageStatus.FINISH) {
            openChapter(mCurPagePos);
        }
        if (chapterIndex == mCurChapterPos - 1) {
            parsePrevChapter();
            mPageView.drawPage(-1);
        }
        if (chapterIndex == mCurChapterPos + 1) {
            parseNextChapter();
            mPageView.drawPage(1);
        }
    }

    @Override
    protected BufferedReader getChapterReader(ChapterListBean chapter) throws Exception {
        @SuppressLint("DefaultLocale")
        File file = BookshelfHelp.getBookFile(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),
                chapter.getDurChapterIndex(), chapter.getDurChapterName());
        if (!file.exists()) return null;

        Reader reader = new FileReader(file);
        return new BufferedReader(reader);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected boolean hasChapterData(ChapterListBean chapter) {
        return BookshelfHelp.isChapterCached(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),
                chapter.getDurChapterIndex(), chapter.getDurChapterName());
    }

    private boolean shouldRequestChapter(Integer chapterIndex) {
        return isNetWorkAvailable() && !hasChapterData(mCollBook.getChapterList(chapterIndex));
    }

    // 装载上一章节的内容
    @Override
    void parsePrevChapter() {
        if (mPageChangeListener != null && mCurChapterPos >= 1 && shouldRequestChapter(mCurChapterPos - 1)) {
            loadContent(mCurChapterPos - 1);
        }
        super.parsePrevChapter();
    }

    // 装载当前章内容。
    @Override
    void parseCurChapter() {
        if (mPageChangeListener != null) {
            for (int i = mCurChapterPos - 1; i < mCurChapterPos + 5; i++) {
                if (i < mCollBook.getChapterListSize() && shouldRequestChapter(i)) {
                    loadContent(i);
                }
            }
        }
        super.parseCurChapter();
    }

    // 装载下一章节的内容
    @Override
    void parseNextChapter() {
        if (mPageChangeListener != null) {
            for (int i = mCurChapterPos + 1; i < mCurChapterPos + 6; i++) {
                if (i < mCollBook.getChapterListSize() && shouldRequestChapter(i)) {
                    loadContent(i);
                }
            }
        }
        super.parseNextChapter();
    }

    @Override
    public void closeBook() {
        super.closeBook();
        executorService.shutdown();
    }

    @Override
    TxtChapter dealLoadPageList(int chapterPos) {
        TxtChapter txtChapter = super.dealLoadPageList(chapterPos);
        if (!isNetWorkAvailable() && !hasChapterData(mCollBook.getChapterList(chapterPos)) && txtChapter.getStatus() == Enum.PageStatus.LOADING) {
            txtChapter.setStatus(Enum.PageStatus.ERROR);
            txtChapter.setMsg("网络连接不可用");
        }
        return txtChapter;
    }

    public enum listHandle {
        ADD, REMOVE, CHECK
    }
}
