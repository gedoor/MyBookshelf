//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.DownloadChapterBean;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.DownloadChapterBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.view.activity.DownloadActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DownloadService extends Service {
    public static final String doneAction = "doneAction";
    public static final String pauseAction = "pauseAction";
    public static final String startAction = "startAction";
    public static final String addDownloadAction = "addDownload";
    private final static int ADD = 1;
    private final static int REMOVE = 2;
    private final static int CHECK = 3;
    public static Boolean isStartDownload = false;
    private final int notificationId = 19931118;
    private SharedPreferences preferences;
    private Boolean isDownloading = false;
    private Boolean isFinish = false;
    private int totalChapters = 0;
    private int threadsNum;
    private List<DownloadChapterBean> downloadingChapter = new ArrayList<>();
    private ExecutorService executorService;
    private Scheduler scheduler;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download)
                .setOngoing(false)
                .setContentTitle(getString(R.string.download_offline_t))
                .setContentText(getString(R.string.download_offline_s));
        //发送通知
        startForeground(notificationId, builder.build());
        RxBus.get().register(this);
        preferences = MApplication.getInstance().getConfigPreferences();
        threadsNum = preferences.getInt(this.getString(R.string.pk_threads_num), 6);
        executorService = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executorService);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        stopForeground(true);
        RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
        RxBus.get().unregister(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                stopSelf();
            } else {
                switch (action) {
                    case addDownloadAction:
                        String noteUrl = intent.getStringExtra("noteUrl");
                        int start = intent.getIntExtra("start", 0);
                        int end = intent.getIntExtra("end", 0);
                        addNewTask(noteUrl, start, end);
                        break;
                    case doneAction:
                        cancelDownload();
                        break;
                    case pauseAction:
                        pauseDownload();
                        break;
                    case startAction:
                        startDownload();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addNewTask(final String noteUrl, final int start, final int end) {
        isStartDownload = true;
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookShelfBean bookShelf = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                    .where(BookShelfBeanDao.Properties.NoteUrl.eq(noteUrl)).build().unique();
            if (!(bookShelf == null)) {
                List<DownloadChapterBean> chapterBeans = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    if (!BookshelfHelp.isChapterCached(bookShelf.getBookInfoBean(), bookShelf.getChapterList(i))) {
                        DownloadChapterBean item = new DownloadChapterBean();
                        item.setNoteUrl(bookShelf.getNoteUrl());
                        item.setDurChapterIndex(bookShelf.getChapterList(i).getDurChapterIndex());
                        item.setDurChapterName(bookShelf.getChapterList(i).getDurChapterName());
                        item.setDurChapterUrl(bookShelf.getChapterList(i).getDurChapterUrl());
                        item.setTag(bookShelf.getTag());
                        item.setBookName(bookShelf.getBookInfoBean().getName());
                        item.setCoverUrl(bookShelf.getBookInfoBean().getCoverUrl());
                        chapterBeans.add(item);
                    }
                }
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().insertOrReplaceInTx(chapterBeans);
            }
            e.onNext(true);
            e.onComplete();
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (!isDownloading) {
                            for (int i = 1; i <= preferences.getInt(getString(R.string.pk_threads_num), threadsNum); i++) {
                                toDownload();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private synchronized void toDownload() {
        isDownloading = true;
        if (isStartDownload) {
            getDownloadChapterO()
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<DownloadChapterBean>() {
                        @Override
                        public void onNext(DownloadChapterBean value) {
                            downloading(value);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            isDownloading = false;
                            finishDownload();
                        }
                    });
        } else {
            isPause();
        }
    }

    private Observable<DownloadChapterBean> getDownloadChapterO() {
        return Observable.create(e -> {
            List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                    .where(BookShelfBeanDao.Properties.Tag.notEq(BookShelfBean.LOCAL_TAG))
                    .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
            if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                for (BookShelfBean bookItem : bookShelfBeanList) {
                    List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder()
                            .where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl()))
                            .orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).list();
                    if (downloadChapterList != null && downloadChapterList.size() > 0) {
                        for (int i = 0; i < downloadChapterList.size(); i++) {
                            if (!editDownloadList(CHECK, downloadChapterList.get(i))) {
                                editDownloadList(ADD, downloadChapterList.get(i));
                                e.onNext(downloadChapterList.get(i));
                                e.onComplete();
                                return;
                            }
                        }
                    }
                }
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                isDownloading = false;
                finishDownload();
            } else {
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                isDownloading = false;
                finishDownload();
            }
            e.onComplete();
        });
    }

    @SuppressLint("DefaultLocale")
    private synchronized void downloading(final DownloadChapterBean data) {
        if (isStartDownload) {
            isProgress(data);
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                e.onNext(!BookshelfHelp.isChapterCached(
                        BookshelfHelp.getCachePathName(data), data.getDurChapterIndex(), data.getDurChapterName()));
                e.onComplete();
            }).flatMap(result -> {
                if (result) {
                    return WebBookModelImpl.getInstance().getBookContent(scheduler, data.getBookName(), data.getDurChapterUrl(), data.getDurChapterIndex(), data.getTag());
                } else {
                    return Observable.create(e -> {
                        e.onNext(new BookContentBean());
                        e.onComplete();
                    });
                }
            }).flatMap(bookContentBean -> Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().delete(data);
                totalChapters += 1;
                e.onNext(editDownloadList(REMOVE, data));
                e.onComplete();
            }))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            handler.postDelayed(() -> {
                                if (!d.isDisposed()) {
                                    d.dispose();
                                }
                            }, 30 * 1000);
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                            if (isStartDownload) {
                                handler.postDelayed(() -> {
                                    if (isStartDownload) {
                                        toDownload();
                                    } else {
                                        isPause();
                                    }
                                }, 800);
                            } else {
                                isPause();
                            }
                        }
                    });
        }
    }

    private synchronized boolean editDownloadList(int editType, DownloadChapterBean value) {
        if (editType == ADD) {
            downloadingChapter.add(value);
            return true;
        } else if (editType == REMOVE) {
            downloadingChapter.remove(value);
            return true;
        } else {
            boolean inDownloadList = false;
            for (DownloadChapterBean chapterBean : downloadingChapter) {
                if (chapterBean.getDurChapterUrl().equals(value.getDurChapterUrl())) {
                    inDownloadList = true;
                }
            }
            return inDownloadList;
        }
    }

    public void startDownload() {
        isStartDownload = true;
        for (int i = 1; i <= preferences.getInt(getString(R.string.pk_threads_num), 6); i++) {
            toDownload();
        }
    }

    public void pauseDownload() {
        isStartDownload = false;
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
                        stopSelf();
                    }
                });
    }

    private void isPause() {
        isDownloading = false;
        Observable.create((ObservableOnSubscribe<DownloadChapterBean>) e -> {
            List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                    .where(BookShelfBeanDao.Properties.Tag.notEq(BookShelfBean.LOCAL_TAG))
                    .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
            if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                for (BookShelfBean bookItem : bookShelfBeanList) {
                    List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder()
                            .where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl()))
                            .orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).limit(1).list();
                    if (downloadChapterList != null && downloadChapterList.size() > 0) {
                        e.onNext(downloadChapterList.get(0));
                        e.onComplete();
                        return;
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
                        if (value.getNoteUrl() != null && value.getNoteUrl().length() > 0) {
                            isProgress(value);
                            RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD_LISTENER, new Object());
                        } else {
                            RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void isProgress(DownloadChapterBean downloadChapterBean) {
        RxBus.get().post(RxBusTag.PROGRESS_DOWNLOAD_LISTENER, downloadChapterBean);

        Intent mainIntent = new Intent(this, DownloadActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download)
                //点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle("正在下载：" + downloadChapterBean.getBookName())
                .setContentText(downloadChapterBean.getDurChapterName() == null ? "  " : downloadChapterBean.getDurChapterName())
                .setContentIntent(mainPendingIntent);
        if (isStartDownload) {
            builder.addAction(R.drawable.ic_pause1, getString(R.string.pause), getThisServicePendingIntent(pauseAction));
        } else {
            builder.addAction(R.drawable.ic_play1, getString(R.string.resume), getThisServicePendingIntent(startAction));
        }
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.cancel), getThisServicePendingIntent(doneAction));
        //发送通知
        startForeground(notificationId, builder.build());
    }

    private synchronized void finishDownload() {
        if (downloadingChapter.size() == 0 && !isFinish) {
            isFinish = true;
            stopSelf();
            if (totalChapters > 0)
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), "共下载" + totalChapters + "章", Toast.LENGTH_SHORT).show());
        }
        cancelDownload();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.PAUSE_DOWNLOAD)})
    public void pauseTask(Object o) {
        pauseDownload();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.START_DOWNLOAD)})
    public void startTask(Object o) {
        startDownload();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CANCEL_DOWNLOAD)})
    public void cancelTask(Object o) {
        cancelDownload();
    }

}
