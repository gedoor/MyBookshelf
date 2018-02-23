package com.monke.monkeybook.presenter;

import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.FileHelper;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.impl.IBookSourcePresenter;
import com.monke.monkeybook.view.impl.IBookSourceView;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public class BookSourcePresenterImpl extends BasePresenterImpl<IBookSourceView> implements IBookSourcePresenter {
    private BookSourceBean delBookSource;

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void saveDate(List<BookSourceBean> bookSourceBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            for (int i = 1; i <= bookSourceBeans.size(); i++) {
                bookSourceBeans.get(i - 1).setSerialNumber(i);
            }
            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(bookSourceBeans);
            BookSourceManage.refreshBookSource();
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void delDate(BookSourceBean bookSourceBean) {
        this.delBookSource = bookSourceBean;
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().delete(bookSourceBean);
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        Snackbar.make(mView.getView(), delBookSource.getBookSourceName() + "已删除", Snackbar.LENGTH_LONG)
                                .setAction("恢复", view -> {
                                    restoreBookSource(delBookSource);
                                })
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                        mView.refreshBookSource();
                    }
                });
    }

    private void restoreBookSource(BookSourceBean bookSourceBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookSourceManage.addBookSource(bookSourceBean);
            BookSourceManage.refreshBookSource();
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.refreshBookSource();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void importBookSource(Uri uri) {
        String json;
        if (uri.toString().startsWith("content://")) {
            json = FileHelper.readString(uri);
        } else {
            DocumentFile file = DocumentFile.fromFile(new File(uri.getPath()));
            json = FileHelper.readString(file);
        }
        if (!isEmpty(json)) {
            try {
                List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
                }.getType());
                BookSourceManage.addBookSource(bookSourceBeans);
                mView.refreshBookSource();
            } catch (Exception e) {
                Toast.makeText(mView.getContext(), "格式不对", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
