//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.contract.BookListContract;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.utils.NetworkUtil;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookListPresenterImpl extends BasePresenterImpl<BookListContract.View> implements BookListContract.Presenter {
    private int threadsNum = 6;
    private int refreshIndex;
    private List<BookShelfBean> bookShelfBeans;
    private int group;
    private boolean hasUpdate = false;
    private List<String> errBooks = new ArrayList<>();

    @Override
    public void queryBookShelf(final Boolean needRefresh, final int group) {
        this.group = group;
        if (needRefresh) {
            hasUpdate = false;
            errBooks.clear();
        }
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfList = BookshelfHelp.getBooksByGroup(group);
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
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            for (BookShelfBean bookShelfBean : new ArrayList<>(bookShelfBeans)) {
                if (!Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG) && (!onlyNew || bookShelfBean.getHasUpdate())) {
                    int chapterNum = bookShelfBean.getChapterListSize();
                    for (int start = bookShelfBean.getDurChapter(); start < chapterNum; start++) {
                        if (!BookshelfHelp.isChapterCached(bookShelfBean.getBookInfoBean(), bookShelfBean.getChapterList(start))) {
                            Intent intent = new Intent(mView.getContext(), DownloadService.class);
                            intent.setAction("addDownload");
                            intent.putExtra("noteUrl", bookShelfBean.getNoteUrl());
                            intent.putExtra("start", start);
                            int end = downloadNum > 0 ? Math.min(chapterNum - 1, start + downloadNum - 1) : chapterNum - 1;
                            intent.putExtra("end", end);
                            mView.getContext().startService(intent);
                            break;
                        }
                    }
                }
            }
            e.onNext(true);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
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
            if (!bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG) && bookShelfBean.getAllowUpdate()) {
                int chapterNum = bookShelfBean.getChapterListSize();
                bookShelfBean.setLoading(true);
                mView.refreshBook(bookShelfBean.getNoteUrl());
                WebBookModelImpl.getInstance().getChapterList(bookShelfBean)
                        .flatMap(this::saveBookToShelfO)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new SimpleObserver<BookShelfBean>() {
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

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UPDATE_PX)})
    public void updatePx(Boolean px) {
        mView.recreate();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.DOWNLOAD_ALL)})
    public void downloadAll(Integer downloadNum) {
        downloadAll(downloadNum, false);
    }
}
