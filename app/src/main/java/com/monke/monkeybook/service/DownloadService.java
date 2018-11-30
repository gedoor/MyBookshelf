//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.Toast;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.DownloadChapterBean;
import com.monke.monkeybook.model.impl.IDownloadTask;
import com.monke.monkeybook.model.task.DownloadTaskImpl;
import com.monke.monkeybook.view.activity.DownloadActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class DownloadService extends Service {
    public static final String cancelAction = "cancelAction";
    public static final String addDownloadAction = "addDownload";
    public static final String removeDownloadAction = "removeDownloadAction";
    public static final String progressDownloadAction = "progressDownloadAction";
    public static final String obtainDownloadListAction = "obtainDownloadListAction";
    public static final String finishDownloadAction = "finishDownloadAction";
    private int notificationId = 19901122;
    private int downloadTaskId = 0;
    private NotificationManagerCompat managerCompat;
    private long currentTime;

    public static boolean isRunning = false;

    private ExecutorService executor;
    private Scheduler scheduler;
    private int threadsNum;
    private Handler handler = new Handler(Looper.getMainLooper());

    private SparseArray<IDownloadTask> downloadTasks = new SparseArray<>();

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setOngoing(false)
                .setContentTitle(getString(R.string.download_offline_t))
                .setContentText(getString(R.string.download_offline_s));
        //发送通知
        managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(notificationId, builder.build());

        SharedPreferences preferences = getSharedPreferences("CONFIG", 0);
        threadsNum = preferences.getInt(this.getString(R.string.pk_threads_num), 4);
        executor = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executor);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        executor.shutdown();
        managerCompat.cancelAll();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                finishSelf();
            } else {
                switch (action) {
                    case addDownloadAction:
                        DownloadBookBean downloadBook = intent.getParcelableExtra("downloadBook");
                        if (downloadBook != null) {
                            addDownload(downloadBook);
                        }
                        break;
                    case removeDownloadAction:
                    case cancelAction:
                        cancelDownload();
                        break;
                    case obtainDownloadListAction:
                        refreshDownloadList();
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

    private synchronized void addDownload(DownloadBookBean downloadBook) {
        if (checkDownloadTaskExist(downloadBook)) {
            return;
        }
        new DownloadTaskImpl(downloadTaskId, downloadBook) {
            @Override
            public void onDownloadPrepared(DownloadBookBean downloadBook) {
                if (canStartNextTask()) {
                    startDownload(scheduler, threadsNum);
                }
                downloadTasks.put(getId(), this);
                sendUpDownloadBook(addDownloadAction, downloadBook);
            }

            @Override
            public void onDownloadProgress(DownloadChapterBean chapterBean) {
                isProgress(chapterBean);
            }

            @Override
            public void onDownloadChange(DownloadBookBean downloadBook) {
                sendUpDownloadBook(progressDownloadAction, downloadBook);
            }

            @Override
            public void onDownloadError(DownloadBookBean downloadBook) {
                if (downloadTasks.indexOfValue(this) >= 0) {
                    managerCompat.cancel(getId());
                    downloadTasks.remove(getId());
                }

                toast(String.format(Locale.getDefault(), "%s：下载失败", downloadBook.getName()));

                startNextTaskAfterRemove(downloadBook);
            }

            @Override
            public void onDownloadComplete(DownloadBookBean downloadBook) {
                if (downloadTasks.indexOfValue(this) >= 0) {
                    managerCompat.cancel(getId());
                    downloadTasks.remove(getId());
                }
                startNextTaskAfterRemove(downloadBook);
            }
        };
        downloadTaskId += 1;
    }

    private void cancelDownload() {
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            downloadTask.stopDownload();
        }
        finishSelf();
    }

    private void removeDownload(String noteUrl) {
        if (noteUrl == null) {
            return;
        }

        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            DownloadBookBean downloadBook = downloadTask.getDownloadBook();
            if (downloadBook != null && TextUtils.equals(noteUrl, downloadBook.getNoteUrl())) {
                downloadTask.stopDownload();
                break;
            }
        }
    }

    private void refreshDownloadList() {
        ArrayList<DownloadBookBean> downloadBookBeans = new ArrayList<>();
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            DownloadBookBean downloadBook = downloadTask.getDownloadBook();
            if (downloadBook != null) {
                downloadBookBeans.add(downloadBook);
            }
        }
        if (!downloadBookBeans.isEmpty()) {
            sendUpDownloadBooks(downloadBookBeans);
        }
    }

    private void startNextTaskAfterRemove(DownloadBookBean downloadBook) {
        sendUpDownloadBook(removeDownloadAction, downloadBook);
        handler.postDelayed(() -> {
            if (downloadTasks.size() == 0) {
                finishSelf();
            } else {
                startNextTask();
            }
        }, 1000);
    }

    private void startNextTask() {
        if (!canStartNextTask()) {
            return;
        }
        for (int i = 0; i < downloadTasks.size(); i++) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (!downloadTask.isDownloading()) {
                downloadTask.startDownload(scheduler, threadsNum);
                break;
            }
        }
    }


    private boolean canStartNextTask() {
        int downloading = 0;
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (downloadTask.isDownloading()) {
                downloading += 1;
            }
        }
        return downloading < threadsNum;
    }


    private synchronized boolean checkDownloadTaskExist(DownloadBookBean downloadBook) {
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (Objects.equals(downloadTask.getDownloadBook(), downloadBook)) {
                return true;
            }
        }
        return false;
    }


    private void sendUpDownloadBook(String action, DownloadBookBean downloadBook) {
        Intent intent = new Intent(action);
        intent.putExtra("downloadBook", downloadBook);
        sendBroadcast(intent);
    }

    private void sendUpDownloadBooks(ArrayList<DownloadBookBean> downloadBooks) {
        Intent intent = new Intent(obtainDownloadListAction);
        intent.putParcelableArrayListExtra("downloadBooks", downloadBooks);
        sendBroadcast(intent);
    }

    private void toast(String msg) {
        Toast.makeText(DownloadService.this, msg, Toast.LENGTH_LONG).show();
    }

    private PendingIntent getChancelPendingIntent() {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(DownloadService.removeDownloadAction);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void isProgress(DownloadChapterBean downloadChapterBean) {
        if (!isRunning) {
            return;
        }

        if (System.currentTimeMillis() - currentTime < 1000) {//更新太快无法取消
            return;
        }

        currentTime = System.currentTimeMillis();

        Intent mainIntent = new Intent(this, DownloadActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download)
                //通知栏大图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                //点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle("正在下载：" + downloadChapterBean.getBookName())
                .setContentText(downloadChapterBean.getDurChapterName() == null ? "  " : downloadChapterBean.getDurChapterName())
                .setContentIntent(mainPendingIntent);
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.cancel), getChancelPendingIntent());
        //发送通知
        managerCompat.notify(notificationId, builder.build());
    }

    private void finishSelf() {
        sendBroadcast(new Intent(finishDownloadAction));
        stopSelf();
    }

    public static void addDownload(Context context, DownloadBookBean downloadBook) {
        if (context == null || downloadBook == null) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(addDownloadAction);
        intent.putExtra("downloadBook", downloadBook);
        context.startService(intent);
    }

    public static void removeDownload(Context context, String noteUrl) {
        if (noteUrl == null || !isRunning) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(removeDownloadAction);
        intent.putExtra("noteUrl", noteUrl);
        context.startService(intent);
    }

    public static void cancelDownload(Context context) {
        if (!isRunning) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(cancelAction);
        context.startService(intent);
    }

    public static void obtainDownloadList(Context context) {
        if (!isRunning) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(obtainDownloadListAction);
        context.startService(intent);
    }

}
