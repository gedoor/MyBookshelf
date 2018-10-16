//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.DataBackup;
import com.monke.monkeybook.help.DataRestore;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.contract.MainContract;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.utils.NetworkUtil;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<MainContract.View> implements MainContract.Presenter {

    @Override
    public void backupData() {
        DataBackup.getInstance().run();
    }

    @Override
    public void restoreData() {
        mView.onRestore(mView.getContext().getString(R.string.on_restore));
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (DataRestore.getInstance().run()) {
                e.onNext(true);
            } else {
                e.onNext(false);
            }
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            mView.dismissHUD();
                            //更新书架并刷新
                            RxBus.get().post(RxBusTag.REFRESH_BOOK_LIST, true);
                        } else {
                            mView.dismissHUD();
                            Toast.makeText(mView.getContext(), R.string.restore_fail, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.dismissHUD();
                        Toast.makeText(mView.getContext(), R.string.restore_fail, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void addBookUrl(String bookUrl) {
        if (TextUtils.isEmpty(bookUrl.trim())) return;
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            URL url = new URL(bookUrl);
            BookInfoBean temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookUrl)).limit(1).build().unique();
            if (temp != null) {
                e.onNext(null);
            } else {
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setTag(String.format("%s://%s", url.getProtocol(), url.getHost()));
                bookShelfBean.setNoteUrl(url.toString());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                bookShelfBean.setFinalDate(System.currentTimeMillis());
                e.onNext(bookShelfBean);
            }
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelfBean != null) {
                            getBook(bookShelfBean);
                        } else {
                            Toast.makeText(mView.getContext(), "已在书架中", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "网址格式不对", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void clearBookshelf() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookshelfHelp.clearBookshelf();
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getBook(BookShelfBean bookShelfBean) {
        WebBookModelImpl.getInstance()
                .getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .flatMap(this::saveBookToShelfO)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        if (value.getBookInfoBean().getChapterUrl() == null) {
                            Toast.makeText(mView.getContext(), "添加书籍失败", Toast.LENGTH_SHORT).show();
                        } else {
                            //成功   //发送RxBus
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                            Toast.makeText(mView.getContext(), "添加书籍成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "添加书籍失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    @Subscribe(thread = EventThread.MAIN_THREAD,tags = {@Tag(RxBusTag.IMMERSION_CHANGE)})
    public void initImmersionBar(Boolean immersion) {
        mView.initImmersionBar();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,tags = {@Tag(RxBusTag.UPDATE_PX)})
    public void updatePx(Boolean px) {
        mView.recreate();
    }
}
