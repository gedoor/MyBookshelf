package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.view.activity.BookSourceActivity;

import java.net.URL;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.monke.monkeybook.help.RxBusTag.CHECK_SOURCE_STATE;

public class CheckSourceService extends Service {
    private static final int notificationId = 3333;
    public static final String ActionStartService = "startService";
    public static final String ActionDoneService = "doneService";
    private static final String ActionOpenActivity = "openActivity";

    private List<BookSourceBean> bookSourceBeanList;
    private int threadsNum;
    private int checkIndex;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        threadsNum = preference.getInt(this.getString(R.string.pk_threads_num), 6);

        bookSourceBeanList = BookSourceManage.getAllBookSource();
        updateNotification(0);
        startCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ActionDoneService:
                        doneService();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 启动服务
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, CheckSourceService.class);
        intent.setAction(ActionStartService);
        context.startService(intent);
    }

    private void doneService() {
        stopSelf();
    }

    /**
     * 更新通知
     */
    private void updateNotification(int state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdReadAloud)
                .setSmallIcon(R.drawable.ic_network_check)
                .setOngoing(true)
                .setContentTitle("校验书源")
                .setContentText(String.format(getString(R.string.progress_show), state, bookSourceBeanList.size()))
                .setContentIntent(getActivityPendingIntent(ActionOpenActivity));
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.cancel), getThisServicePendingIntent(ActionDoneService));
        builder.setProgress(bookSourceBeanList.size(), state, false);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    private PendingIntent getActivityPendingIntent(String actionStr) {
        Intent intent = new Intent(this, BookSourceActivity.class);
        intent.setAction(actionStr);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startCheck() {
        if (bookSourceBeanList != null && bookSourceBeanList.size() > 0) {
            RxBus.get().post(CHECK_SOURCE_STATE, 0);
            checkIndex = -1;
            for (int i = 1; i <= threadsNum; i++) {
                checkSource();
            }
        }
    }

    private synchronized void checkSource() {
        checkIndex++;
        if (checkIndex > threadsNum) {
            RxBus.get().post(CHECK_SOURCE_STATE, checkIndex - threadsNum);
            updateNotification(checkIndex - threadsNum);
        }

        if (checkIndex < bookSourceBeanList.size()) {
            final BookSourceBean sourceBean = bookSourceBeanList.get(checkIndex);
            if (!TextUtils.isEmpty(sourceBean.getCheckUrl())) {
                try {
                    new URL(sourceBean.getCheckUrl());
                    BookShelfBean bookShelfBean = new BookShelfBean();
                    bookShelfBean.setTag(sourceBean.getBookSourceUrl());
                    bookShelfBean.setNoteUrl(sourceBean.getCheckUrl());
                    bookShelfBean.setFinalDate(System.currentTimeMillis());
                    bookShelfBean.setDurChapter(0);
                    bookShelfBean.setDurChapterPage(0);
                    WebBookModelImpl.getInstance().getBookInfo(bookShelfBean)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
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
                } catch (Exception exception) {
                    sourceBean.setBookSourceGroup("失效");
                    DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao()
                            .insertOrReplace(sourceBean);
                    checkSource();
                }
            } else {
                try {
                    new URL(sourceBean.getBookSourceUrl());
                    BaseModelImpl.getRetrofitString(sourceBean.getBookSourceUrl())
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
                    checkSource();
                }
            }
        } else {
            if (checkIndex >= bookSourceBeanList.size() + threadsNum - 1) {
                RxBus.get().post(CHECK_SOURCE_STATE, -1);
                stopSelf();
            }
        }
    }


}
