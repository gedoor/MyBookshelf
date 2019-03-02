//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.presenter;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.DownloadBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.presenter.contract.BookListContract;
import com.kunfei.bookshelf.service.DownloadService;
import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BookListPresenter extends BasePresenterImpl<BookListContract.View> implements BookListContract.Presenter {
    private int threadsNum = 6;
    private int refreshIndex;
    private List<BookShelfBean> bookShelfBeans;
    private int group;
    private boolean hasUpdate = false;
    private List<String> errBooks = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void queryBookShelf(final Boolean needRefresh, final int group) {
        this.group = group;
        if (needRefresh) {
            hasUpdate = false;
            errBooks.clear();
        }
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfList;
            if (group == 0) {
                bookShelfList = BookshelfHelp.getAllBook();
            } else {
                bookShelfList = BookshelfHelp.getBooksByGroup(group - 1);
            }
            e.onNext(bookShelfList == null ? new ArrayList<>() : bookShelfList);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        if (null != value) {
                            bookShelfBeans = value;
                            mView.refreshBookShelf(bookShelfBeans);
                            if (needRefresh && NetworkUtil.isNetWorkAvailable()) {
                                startRefreshBook();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                    }
                });
    }

    private void downloadAll(int downloadNum, boolean onlyNew) {
        if (bookShelfBeans == null || mView.getContext() == null) {
            return;
        }
        AsyncTask.execute(() -> {
            for (BookShelfBean bookShelfBean : new ArrayList<>(bookShelfBeans)) {
                if (!bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG) && (!onlyNew || bookShelfBean.getHasUpdate())) {
                    int chapterNum = bookShelfBean.getChapterListSize();
                    bookShelfBean.getBookInfoBean().setChapterList(BookshelfHelp.getChapterList(bookShelfBean.getNoteUrl()));
                    for (int start = bookShelfBean.getDurChapter(); start < chapterNum; start++) {
                        if (!BookshelfHelp.isChapterCached(bookShelfBean.getBookInfoBean(), bookShelfBean.getChapter(start))
                                && start < chapterNum - 1) {
                            DownloadBookBean downloadBook = new DownloadBookBean();
                            downloadBook.setName(bookShelfBean.getBookInfoBean().getName());
                            downloadBook.setNoteUrl(bookShelfBean.getNoteUrl());
                            downloadBook.setCoverUrl(bookShelfBean.getBookInfoBean().getCoverUrl());
                            downloadBook.setStart(start);
                            downloadBook.setEnd(downloadNum > 0 ? Math.min(chapterNum - 1, start + downloadNum - 1) : chapterNum - 1);
                            downloadBook.setFinalDate(System.currentTimeMillis());
                            DownloadService.addDownload(mView.getContext(), downloadBook);
                            bookShelfBean.getBookInfoBean().setChapterList(null);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void startRefreshBook() {
        if (mView.getContext() != null) {
            threadsNum = mView.getPreferences().getInt(mView.getContext().getString(R.string.pk_threads_num), 6);
            if (bookShelfBeans != null && bookShelfBeans.size() > 0) {
                refreshIndex = -1;
                for (int i = 1; i <= threadsNum; i++) {
                    refreshBookshelf();
                }
            }
        }
    }

    private synchronized void refreshBookshelf() {
        refreshIndex++;
        if (refreshIndex < bookShelfBeans.size()) {
            BookShelfBean bookShelfBean = bookShelfBeans.get(refreshIndex);
            if (!bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG) && bookShelfBean.getAllowUpdate() && bookShelfBean.getGroup() != 3) {
                int chapterNum = bookShelfBean.getChapterListSize();
                bookShelfBean.setLoading(true);
                mView.refreshBook(bookShelfBean.getNoteUrl());
                WebBookModel.getInstance().getChapterList(bookShelfBean)
                        .flatMap(this::saveBookToShelfO)
                        .compose(RxUtils::toSimpleSingle)
                        .subscribe(new Observer<BookShelfBean>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(BookShelfBean value) {
                                if (value.getErrorMsg() != null) {
                                    mView.toast(value.getErrorMsg());
                                    value.setErrorMsg(null);
                                }
                                bookShelfBean.setLoading(false);
                                if (chapterNum < bookShelfBean.getChapterListSize())
                                    hasUpdate = true;
                                mView.refreshBook(bookShelfBean.getNoteUrl());
                                refreshBookshelf();
                            }

                            @Override
                            public void onError(Throwable e) {
                                errBooks.add(bookShelfBean.getBookInfoBean().getName());
                                Log.w("MonkBook", String.format("%s: %s", bookShelfBean.getBookInfoBean().getName(), e.getMessage()));
                                bookShelfBean.setLoading(false);
                                mView.refreshBook(bookShelfBean.getNoteUrl());
                                refreshBookshelf();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else {

                refreshBookshelf();
            }
        } else if (refreshIndex >= bookShelfBeans.size() + threadsNum - 1) {
            if (errBooks.size() > 0) {
                mView.toast(TextUtils.join("、", errBooks) + " 更新失败！");
                errBooks.clear();
            }
            if (hasUpdate && mView.getPreferences().getBoolean(mView.getContext().getString(R.string.pk_auto_download), false)) {
                downloadAll(10, true);
                hasUpdate = false;
            }
            queryBookShelf(false, group);
        }
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            bookShelfBean.getBookInfoBean().setChapterList(null);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
        compositeDisposable.dispose();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK), @Tag(RxBusTag.HAD_REMOVE_BOOK), @Tag(RxBusTag.UPDATE_BOOK_PROGRESS)})
    public void hadAddOrRemoveBook(BookShelfBean bookShelfBean) {
        queryBookShelf(false, group);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UPDATE_GROUP)})
    public void updateGroup(Integer group) {
        this.group = group;
        mView.updateGroup(group);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.REFRESH_BOOK_LIST)})
    public void reFlashBookList(Boolean needRefresh) {
        queryBookShelf(needRefresh, group);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.DOWNLOAD_ALL)})
    public void downloadAll(Integer downloadNum) {
        downloadAll(downloadNum, false);
    }
}
