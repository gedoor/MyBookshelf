package com.monke.monkeybook.model;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.net.URL;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class CheckSourceModel  extends BaseModelImpl {

    private BaseActivity activity;
    private List<BookSourceBean> bookSourceBeanList;
    private int threadsNum;
    private int checkIndex;

    private OnCheckSourceListener checkSourceListener;

    public CheckSourceModel(BaseActivity activity, OnCheckSourceListener checkSourceListener) {
        this.activity = activity;
        this.checkSourceListener = checkSourceListener;
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(activity);
        threadsNum = preference.getInt(activity.getString(R.string.pk_threads_num), 6);

        bookSourceBeanList = BookSourceManage.getAllBookSource();
    }

    public void startCheck() {
        if (bookSourceBeanList != null && bookSourceBeanList.size() > 0) {
            if (checkSourceListener != null) {
                checkSourceListener.startCheck();
            }
            checkIndex = -1;
            for (int i = 1; i <= threadsNum; i++) {
                checkSource();
            }
        }
    }

    private synchronized void checkSource() {
        checkIndex++;
        if (checkIndex < bookSourceBeanList.size()) {
            final BookSourceBean sourceBean = bookSourceBeanList.get(checkIndex);
            if (!TextUtils.isEmpty(sourceBean.getCheckUrl())) {
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setTag(sourceBean.getBookSourceUrl());
                bookShelfBean.setNoteUrl(sourceBean.getCheckUrl());
                bookShelfBean.setFinalDate(System.currentTimeMillis());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                WebBookModelImpl.getInstance().getBookInfo(bookShelfBean)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(activity.bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Observer<BookShelfBean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(BookShelfBean bookShelfBean) {
                                checkSource();
                            }

                            @Override
                            public void onError(Throwable e) {
                                sourceBean.setBookSourceGroup("失效");
                                DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao()
                                        .insertOrReplace(sourceBean);
                                checkSource();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else {
                try {
                    new URL(sourceBean.getBookSourceUrl());
                    getRetrofitString(sourceBean.getBookSourceUrl())
                            .create(IHttpGetApi.class)
                            .getWebContent(sourceBean.getBookSourceUrl(), AnalyzeHeaders.getMap(null))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Response<String>>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Response<String> stringResponse) {
                                    checkSource();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    sourceBean.setBookSourceGroup("失效");
                                    DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao()
                                            .insertOrReplace(sourceBean);
                                    checkSource();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }  catch (Exception e) {
                    sourceBean.setBookSourceGroup("失效");
                    DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao()
                            .insertOrReplace(sourceBean);
                    checkSource();
                }

            }
        } else {
            if (checkIndex >= bookSourceBeanList.size() + threadsNum - 1) {
                if (checkSourceListener != null) {
                    checkSourceListener.checkFinish();
                }
            }
        }
    }

    public interface OnCheckSourceListener {
        void startCheck();

        void checkFinish();
    }


}
