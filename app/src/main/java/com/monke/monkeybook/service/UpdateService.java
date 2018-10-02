package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.UpdateInfoBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.UpdateManager;
import com.monke.monkeybook.view.activity.BookSourceActivity;
import com.monke.monkeybook.view.activity.UpdateActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateService extends Service {

    /* 下载包安装路径 */
    private final String savePath = Environment.getDownloadCacheDirectory().getAbsolutePath();
    private static final String startDownload = "startDownload";

    private UpdateInfoBean updateInfo;
    private boolean interceptFlag = false;

    public static void startThis(Context context, UpdateInfoBean updateInfoBean) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(startDownload);
        intent.putExtra("updateInfo", updateInfoBean);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //创建 Notification.Builder 对象
        updateNotification(0);
        RxBus.get().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                    case startDownload:
                        updateInfo = intent.getParcelableExtra("updateInfo");
                        downloadApk(updateInfo.getUrl());
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

    /**
     * 更新通知
     */
    private void updateNotification(int state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdReadAloud)
                .setSmallIcon(R.drawable.ic_download)
                .setOngoing(true)
                .setContentTitle(getString(R.string.download_update))
                .setContentText(String.format(getString(R.string.progress_show), state, 100))
                .setContentIntent(getActivityPendingIntent(""));
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.cancel), getThisServicePendingIntent(""));
        builder.setProgress(100, state, false);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        int notificationId = 3425;
        startForeground(notificationId, notification);
    }

    private PendingIntent getActivityPendingIntent(String actionStr) {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.setAction(actionStr);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void downloadApk(String apkUrl) {
        try {
            URL url = new URL(apkUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int length = conn.getContentLength();
            InputStream is = conn.getInputStream();

            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdir();
            }
            String apkFilePath = savePath + apkUrl.substring(apkUrl.lastIndexOf("\\"));
            File apkFile = new File(apkFilePath);
            FileOutputStream fos = new FileOutputStream(apkFile);

            int count = 0;
            byte buf[] = new byte[1024];

            do {
                int numread = is.read(buf);
                count += numread;
                int progress = (int) (((float) count / length) * 100);
                updateNotification(progress);
                //更新进度

                if (numread <= 0) {
                    //下载完成通知安装
                    fos.close();
                    is.close();
                    UpdateActivity.startThis(this, updateInfo);
                    UpdateManager.getInstance(this).installApk(apkFile);
                    break;
                }
                fos.write(buf, 0, numread);
            } while (!interceptFlag);//点击取消就停止下载.

            fos.close();
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
