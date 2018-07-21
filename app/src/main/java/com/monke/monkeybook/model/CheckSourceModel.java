package com.monke.monkeybook.model;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.monke.basemvplib.BaseActivity;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CheckSourceModel {

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
                            }

                            @Override
                            public void onError(Throwable e) {
                                sourceBean.setBookSourceGroup("失效");
                                DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao()
                                        .insertOrReplace(sourceBean);
                            }

                            @Override
                            public void onComplete() {
                                checkSource();
                            }
                        });
            } else {
                   checkSource();
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
