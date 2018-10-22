package com.monke.monkeybook.service;

import android.app.Notification;
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
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.UpdateInfoBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.UpdateManager;
import com.monke.monkeybook.view.activity.UpdateActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateService extends Service {
    public static boolean isRunning = false;
    private static final String startDownload = "startDownload";
    private static final String stopDownload = "stopDownload";
    private String apkFilePath;
    private UpdateInfoBean updateInfo;
    private boolean interceptFlag = false;
    private Disposable disposableDown;
    private int count = 0;

    public static void startThis(Context context, UpdateInfoBean updateInfoBean) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(startDownload);
        intent.putExtra("updateInfo", updateInfoBean);
        context.startService(intent);
    }

    public static void stopThis(Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(stopDownload);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        //创建 Notification.Builder 对象
        updateNotification(0);
        RxBus.get().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (disposableDown != null) {
            disposableDown.dispose();
        }
        stopForeground(true);
        RxBus.get().post(RxBusTag.UPDATE_APK_STATE, -1);
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
                    case startDownload:
                        updateInfo = intent.getParcelableExtra("updateInfo");
                        downloadApk(updateInfo.getUrl());
                        break;
                    case stopDownload:
                        stopDownload();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopDownload() {
        interceptFlag = true;
        if (count == 0) {
            stopSelf();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 更新通知
     */
    private void updateNotification(int state) {
        RxBus.get().post(RxBusTag.UPDATE_APK_STATE, state);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdReadAloud)
                .setSmallIcon(R.drawable.ic_download)
                .setOngoing(true)
                .setContentTitle(getString(R.string.download_update))
                .setContentText(String.format(getString(R.string.progress_show), state, 100))
                .setContentIntent(getActivityPendingIntent(""));
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.cancel), getThisServicePendingIntent(stopDownload));
        builder.setProgress(100, state, false);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        int notificationId = 3425;
        startForeground(notificationId, notification);
    }

    private PendingIntent getActivityPendingIntent(String actionStr) {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.setAction(actionStr);
        intent.putExtra("updateInfo", updateInfo);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void downloadApk(String apkUrl) {
        if (disposableDown != null) {
            return;
        }
        Observable.create((ObservableOnSubscribe<Integer>) e -> {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                apkFilePath = UpdateManager.getSavePath(apkUrl.substring(apkUrl.lastIndexOf("/")));
                File apkFile = new File(apkFilePath);
                FileOutputStream fos = new FileOutputStream(apkFile);

                byte buf[] = new byte[1024];
                int numread;
                do {
                    numread = is.read(buf);
                    count += numread;
                    int progress = (int) (((float) count / length) * 100);
                    //更新进度
                    e.onNext(progress);
                    if (numread <= 0) {
                        //下载完成通知安装
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);//点击取消就停止下载.
                fos.close();
                is.close();
                if (numread > 0) {
                    apkFile.delete();
                }
                e.onNext(-1);
            } catch (Exception exception) {
                e.onError(exception);
            } finally {
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableDown = d;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (integer < 0) {
                            RxBus.get().post(RxBusTag.UPDATE_APK_STATE, -1);
                            UpdateActivity.startThis(UpdateService.this, updateInfo);
                        } else {
                            updateNotification(integer);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), "下载更新出错\n" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onComplete() {
                        RxBus.get().post(RxBusTag.UPDATE_APK_STATE, -1);
                        UpdateService.this.stopSelf();
                    }
                });

    }

}
