package com.monke.monkeybook.model;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.monke.basemvplib.BaseActivity;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CheckBookSource {

    private BaseActivity activity;
    private List<BookSourceBean> bookSourceBeanList;
    private int threadsNum;
    private int checkIndex;
    private int searchSuccessNum;

    private OnCheckSourceListener checkSourceListener;

    public CheckBookSource(BaseActivity activity, OnCheckSourceListener checkSourceListener) {
        this.activity = activity;
        this.checkSourceListener = checkSourceListener;
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(activity);
        threadsNum = preference.getInt(activity.getString(R.string.pk_threads_num), 6);

        bookSourceBeanList = BookSourceManage.getAllBookSource();
    }

    public void startCheck(final String content, final long searchTime, List<BookShelfBean> bookShelfS, Boolean fromError) {
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
            BookShelfBean bookShelfBean = new BookShelfBean();


            WebBookModelImpl.getInstance().getChapterList(bookShelfBean)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(activity.bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<BookShelfBean>() {
                        @Override
                        public void onNext(BookShelfBean value) {

                            checkSource();
                        }

                        @Override
                        public void onError(Throwable e) {

                            checkSource();
                        }
                    });

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
