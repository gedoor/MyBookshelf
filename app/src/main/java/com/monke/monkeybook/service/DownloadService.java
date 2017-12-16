//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.DownloadChapterBean;
import com.monke.monkeybook.bean.DownloadChapterListBean;
import com.monke.monkeybook.common.RxBusTag;
import com.monke.monkeybook.dao.BookContentBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.DownloadChapterBeanDao;
import com.monke.monkeybook.model.impl.WebBookModelImpl;
import com.monke.monkeybook.view.activity.MainActivity;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DownloadService extends Service {
    private NotificationManager notifyManager;
    private int notifiId = 19931118;
    private Boolean isStartDownload = false;
    private Boolean isInit = false;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
        isInit = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isInit) {
            isInit = true;
            notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            RxBus.get().register(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addNewTask(final List<DownloadChapterBean> newData) {
        isStartDownload = true;
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().insertOrReplaceInTx(newData);
            e.onNext(true);
            e.onComplete();
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (!isDownloading) {
                            toDownload();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private Boolean isDownloading = false;
    public static final int reTryTimes = 1;

    private void toDownload() {
        isDownloading = true;
        if (isStartDownload) {
            Observable.create((ObservableOnSubscribe<DownloadChapterBean>) e -> {
                List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
                if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                    for (BookShelfBean bookItem : bookShelfBeanList) {
                        if (!bookItem.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                            List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder().where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl())).orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).limit(1).list();
                            if (downloadChapterList != null && downloadChapterList.size() > 0) {
                                e.onNext(downloadChapterList.get(0));
                                e.onComplete();
                                return;
                            }
                        }
                    }
                    DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                    e.onNext(new DownloadChapterBean());
                } else {
                    DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                    e.onNext(new DownloadChapterBean());
                }
                e.onComplete();
            })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<DownloadChapterBean>() {
                        @Override
                        public void onNext(DownloadChapterBean value) {
                            if (value.getNoteUrl() != null && value.getNoteUrl().length() > 0) {
                                downloading(value, 0);
                            } else {
                                Observable.create(e -> {
                                    DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                                    e.onNext(new Object());
                                    e.onComplete();
                                })
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .observeOn(Schedulers.io())
                                        .subscribe(new SimpleObserver<Object>() {
                                            @Override
                                            public void onNext(Object value) {
                                                isDownloading = false;
                                                finishDownload();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                e.printStackTrace();
                                                isDownloading = false;
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            isDownloading = false;
                        }
                    });
        } else {
            isPause();
        }
    }

    private void downloading(final DownloadChapterBean data, final int durTime) {
        if (durTime < reTryTimes && isStartDownload) {
            isProgress(data);
            Observable.create((ObservableOnSubscribe<BookContentBean>) e -> {
                List<BookContentBean> result = DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().queryBuilder().where(BookContentBeanDao.Properties.DurChapterUrl.eq(data.getDurChapterUrl())).list();
                if (result != null && result.size() > 0) {
                    e.onNext(result.get(0));
                } else {
                    e.onNext(new BookContentBean());
                }
                e.onComplete();
            }).flatMap(bookContentBean -> {
                if (bookContentBean.getDurChapterUrl() == null || bookContentBean.getDurChapterUrl().length() <= 0) {
                    return WebBookModelImpl.getInstance().getBookContent(data.getDurChapterUrl(), data.getDurChapterIndex(), data.getTag()).map(bookContentBean1 -> {
                        DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().delete(data);
                        if (bookContentBean1.getRight()) {
                            DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().insertOrReplace(bookContentBean1);
                            DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().update(new ChapterListBean(data.getNoteUrl(), data.getDurChapterIndex(), data.getDurChapterUrl(), data.getDurChapterName(), data.getTag(), true));
                        }
                        return bookContentBean1;
                    });
                } else {
                    return Observable.create(e -> {
                        DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().delete(data);
                        e.onNext(bookContentBean);
                        e.onComplete();
                    });
                }
            })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new SimpleObserver<BookContentBean>() {
                        @Override
                        public void onNext(BookContentBean value) {
                            if(isStartDownload){
                                new Handler().postDelayed(() -> {
                                    if(isStartDownload){
                                        toDownload();
                                    }else{
                                        isPause();
                                    }
                                },800);
                            }else{
                                isPause();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            int time = durTime + 1;
                            downloading(data, time);
                        }
                    });
        } else {
            if (isStartDownload) {
                Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                    DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().delete(data);
                    e.onNext(true);
                    e.onComplete();
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(new SimpleObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean value) {
                                if(isStartDownload){
                                    new Handler().postDelayed(() -> {
                                        if(isStartDownload){
                                            toDownload();
                                        }else{
                                            isPause();
                                        }
                                    },800);
                                }else{
                                    isPause();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                if(!isStartDownload)
                                    isPause();
                            }
                        });
            } else
                isPause();
        }
    }

    public void startDownload() {
        isStartDownload = true;
        toDownload();
    }

    public void pauseDownload() {
        isStartDownload = false;
        notifyManager.cancelAll();
    }

    public void cancelDownload() {
        Observable.create(e -> {
            DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
            e.onNext(new Object());
            e.onComplete();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object value) {
                        pauseDownload();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void isPause() {
        isDownloading = false;
        Observable.create((ObservableOnSubscribe<DownloadChapterBean>) e -> {
            List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
            if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                for (BookShelfBean bookItem : bookShelfBeanList) {
                    if (!bookItem.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                        List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder().where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl())).orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).limit(1).list();
                        if (downloadChapterList != null && downloadChapterList.size() > 0) {
                            e.onNext(downloadChapterList.get(0));
                            e.onComplete();
                            return;
                        }
                    }
                }
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                e.onNext(new DownloadChapterBean());
            } else {
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                e.onNext(new DownloadChapterBean());
            }
            e.onComplete();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new SimpleObserver<DownloadChapterBean>() {
                    @Override
                    public void onNext(DownloadChapterBean value) {
                        if (value.getNoteUrl() != null && value.getNoteUrl().length() > 0){
                            RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD_LISTENER, new Object());
                        }else{
                            RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void isProgress(DownloadChapterBean downloadChapterBean) {
        RxBus.get().post(RxBusTag.PROGRESS_DOWNLOAD_LISTENER, downloadChapterBean);

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                //点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle("正在下载："+downloadChapterBean.getBookName())
                .setContentText(downloadChapterBean.getDurChapterName()==null?"  ":downloadChapterBean.getDurChapterName())
                .setContentIntent(mainPendingIntent);
        //发送通知
        notifyManager.notify(notifiId, builder.build());
    }

    private void finishDownload() {
        RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
        notifyManager.cancelAll();
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), "全部离线章节下载完成", Toast.LENGTH_SHORT).show());
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.PAUSE_DOWNLOAD)
            }
    )
    public void pauseTask(Object o) {
        pauseDownload();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.START_DOWNLOAD)
            }
    )
    public void startTask(Object o) {
        startDownload();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.CANCEL_DOWNLOAD)
            }
    )
    public void cancelTask(Object o) {
        cancelDownload();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.ADD_DOWNLOAD_TASK)
            }
    )
    public void addTask(DownloadChapterListBean newData) {
        addNewTask(newData.getData());
    }
}