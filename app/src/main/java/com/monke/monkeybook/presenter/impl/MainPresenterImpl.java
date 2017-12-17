//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter.impl;

import android.support.annotation.NonNull;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.IView;
import com.monke.basemvplib.impl.BasePresenterImpl;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.common.RxBusTag;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.listener.OnGetChapterListListener;
import com.monke.monkeybook.model.impl.WebBookModelImpl;
import com.monke.monkeybook.presenter.IMainPresenter;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.view.IMainView;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<IMainView> implements IMainPresenter {

    private List<BookShelfBean> getAllBookShelf() {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            List<BookInfoBean> temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).limit(1).build().list();
            if (temp != null && temp.size() > 0) {
                BookInfoBean bookInfoBean = temp.get(0);
                bookInfoBean.setChapterlist(DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder()
                        .where(ChapterListBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).orderAsc(ChapterListBeanDao.Properties.DurChapterIndex).build().list());
                bookShelfList.get(i).setBookInfoBean(bookInfoBean);
            } else {
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().delete(bookShelfList.get(i));
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    public void queryBookShelf(final Boolean needRefresh) {
        if (needRefresh) {
            mView.activityRefreshView();
        }
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfList = getAllBookShelf();
            e.onNext(bookShelfList == null ? new ArrayList<BookShelfBean>() : bookShelfList);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        if (null != value) {
                            mView.refreshBookShelf(value);
                            if (needRefresh) {
                                startRefreshBook(value);
                            } else {
                                mView.refreshFinish();
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

    private void startRefreshBook(List<BookShelfBean> value){
        if (value != null && value.size() > 0){
            mView.setRecyclerMaxProgress(value.size());
            refreshBookShelf(value,0);
        }else{
            mView.refreshFinish();
        }
    }

    private void refreshBookShelf(final List<BookShelfBean> value, final int index) {
        if (index<=value.size()-1) {
            int chapterSize = value.get(index).getBookInfoBean().getChapterlist().size();
            WebBookModelImpl.getInstance().getChapterList(value.get(index), new OnGetChapterListListener() {
                @Override
                public void success(BookShelfBean bookShelfBean) {
                    boolean hasUpdate = chapterSize < value.get(index).getBookInfoBean().getChapterlist().size();
                    saveBookToShelf(value, index, hasUpdate);
                }

                @Override
                public void error() {
                    mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_NONET));
                }
            });
        } else {
            queryBookShelf(false);
        }
    }

    private void saveBookToShelf(final List<BookShelfBean> datas, final int index, final boolean hasUpdate) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(datas.get(index).getBookInfoBean().getChapterlist());
            if (hasUpdate) {
                datas.get(index).setHasUpdate(true);
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(datas.get(index));
            }
            e.onNext(datas.get(index));
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        mView.refreshRecyclerViewItemAdd();
                        refreshBookShelf(datas, index + 1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_NONET));
                    }
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

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK),
                    @Tag(RxBusTag.HAD_REMOVE_BOOK),
                    @Tag(RxBusTag.UPDATE_BOOK_PROGRESS)
            }
    )
    public void hadddOrRemoveBook(BookShelfBean bookShelfBean) {
        queryBookShelf(false);
    }
}
