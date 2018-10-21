package com.monke.monkeybook.model;

import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2017/12/15.
 * 所有书源
 */

public class BookSourceManage extends BaseModelImpl {
    private static List<BookSourceBean> selectedBookSource;
    private static List<BookSourceBean> allBookSource;
    public static List<String> groupList = new ArrayList<>();

    public static BookSourceManage getInstance() {
        return new BookSourceManage();
    }

    public static List<BookSourceBean> getSelectedBookSource() {
        if (selectedBookSource == null) {
            selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .where(BookSourceBeanDao.Properties.Enable.eq(true))
                    .orderRaw("-WEIGHT ASC")
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
        }
        return selectedBookSource;
    }

    public static List<BookSourceBean> getAllBookSource() {
        if (allBookSource == null) {
            allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
            upGroupList();
        }
        return allBookSource;
    }

    public static void refreshBookSource() {
        allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
        selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .orderRaw("-WEIGHT ASC")
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
        upGroupList();
    }

    public static void addBookSource(List<BookSourceBean> bookSourceBeans) {
        refreshBookSource();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            addBookSource(bookSourceBean);
        }
        refreshBookSource();
    }

    public static void addBookSource(BookSourceBean bookSourceBean) {
        if (bookSourceBean.getBookSourceUrl().endsWith("/")) {
            bookSourceBean.setBookSourceUrl(bookSourceBean.getBookSourceUrl().substring(0, bookSourceBean.getBookSourceUrl().lastIndexOf("/")));
        }
        BookSourceBean temp = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl())).unique();
        if (temp != null) {
            bookSourceBean.setSerialNumber(temp.getSerialNumber());
            bookSourceBean.setEnable(temp.getEnable());
        }
        if (bookSourceBean.getSerialNumber() == 0) {
            bookSourceBean.setSerialNumber(allBookSource.size() + 1);
        }
        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
    }

    private synchronized static void upGroupList() {
        groupList.clear();
        for (BookSourceBean bookSourceBean : allBookSource) {
            if (!TextUtils.isEmpty(bookSourceBean.getBookSourceGroup()) && !groupList.contains(bookSourceBean.getBookSourceGroup())) {
                for (String item: bookSourceBean.getBookSourceGroup().split("\\s*[,;，；]\\s*")) {
                    if (TextUtils.isEmpty(item) || groupList.contains(item)) continue;
                    groupList.add(item);
                }
            }
        }
        Collections.sort(groupList);
        RxBus.get().post(RxBusTag.UPDATE_BOOK_SOURCE, new Object());
    }

    public static Observable<Boolean> importSourceFromWww(URL url) {
        return getRetrofitString(String.format("%s://%s", url.getProtocol(), url.getHost()), "utf-8")
                .create(IHttpGetApi.class)
                .getWebContent(url.getPath(), AnalyzeHeaders.getMap(null))
                .flatMap(rsp -> importBookSourceO(rsp.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Boolean> importBookSourceO(String json) {
        return Observable.create(e -> {
            try {
                List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
                }.getType());
                for (BookSourceBean bookSourceBean : bookSourceBeans) {
                    if (bookSourceBean.containsGroup("删除")) {
                        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                .buildDelete().executeDeleteWithoutDetachingEntities();
                    } else {
                        try {
                            new URL(bookSourceBean.getBookSourceUrl());
                            bookSourceBean.setSerialNumber(0);
                            addBookSource(bookSourceBean);
                        } catch (Exception exception) {
                            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                                    .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                    .buildDelete().executeDeleteWithoutDetachingEntities();
                        }
                    }
                }
                refreshBookSource();
                e.onNext(true);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(false);
            }
            e.onComplete();
        });
    }

    public static void initDefaultBookSource(MBaseActivity activity) {
        if (getAllBookSource() == null || getAllBookSource().size() == 0) {
            new AlertDialog.Builder(activity)
                    .setTitle("加载默认书源")
                    .setMessage("当前书源为空,是否加载默认书源?")
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        try {
                            URL url = new URL(activity.getString(R.string.default_source_url));
                            BookSourceManage.importSourceFromWww(url)
                                    .subscribe(new SimpleObserver<Boolean>() {
                                        @Override
                                        public void onNext(Boolean aBoolean) {
                                            activity.toast("默认书源加载成功.");
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            activity.toast("默认书源加载失败.");
                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .show();
        }
    }


}
