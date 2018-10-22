package com.monke.monkeybook.widget.page;

import android.annotation.SuppressLint;

import com.monke.monkeybook.base.observer.SimpleObserver;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.monke.monkeybook.utils.NetworkUtil.isNetWorkAvailable;

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {
    private static final String TAG = "NetPageLoader";

    public NetPageLoader(PageView pageView, BookShelfBean collBook) {
        super(pageView, collBook);
    }

    @Override
    public void refreshChapterList() {
        if (mCollBook.getChapterList().size() > 0) {
            isChapterListPrepare = true;

            // 目录加载完成，执行回调操作。
            if (mPageChangeListener != null) {
                mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
            }

            // 如果章节未打开
            if (!isChapterOpen()) {
                // 打开章节
                skipToChapter(mCollBook.getDurChapter(), mCollBook.getDurChapterPage());
            }
        } else {
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
    boolean parsePrevChapter() {
        boolean isRight = super.parsePrevChapter();
        if (mPageChangeListener != null && mCurChapterPos >= 1 && shouldRequestChapter(mCurChapterPos - 1)) {
            mPageChangeListener.requestChapters(mCurChapterPos - 1);
        }
        return isRight;
    }

    // 装载当前章内容。
    @Override
    boolean parseCurChapter() {
        boolean isRight = super.parseCurChapter();
        if (mPageChangeListener != null) {
            for (int i = mCurChapterPos; i < mCurChapterPos + 5; i++) {
                if (i < mCollBook.getChapterListSize() && shouldRequestChapter(i)) {
                    mPageChangeListener.requestChapters(i);
                }
            }
        }
        return isRight;
    }

    // 装载下一章节的内容
    @Override
    boolean parseNextChapter() {
        boolean isRight = super.parseNextChapter();
        if (mPageChangeListener != null) {
            for (int i = mCurChapterPos + 1; i < mCurChapterPos + 6; i++) {
                if (i < mCollBook.getChapterListSize() && shouldRequestChapter(i)) {
                    mPageChangeListener.requestChapters(i);
                }
            }
        }
        return isRight;
    }

    @Override
    TxtChapter dealLoadPageList(int chapterPos) {
        if (!isNetWorkAvailable() && !hasChapterData(mCollBook.getChapterList(chapterPos)) && getPageStatus() == STATUS_LOADING) {
            chapterError("网络连接不可用");
        }
        return super.dealLoadPageList(chapterPos);
    }
}
