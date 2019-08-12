package com.kunfei.bookshelf.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.web.ShareServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static com.kunfei.bookshelf.constant.AppConstant.ActionDoneService;
import static com.kunfei.bookshelf.constant.AppConstant.ActionStartService;

public class ShareService extends Service {
    private static boolean isRunning = false;
    private ShareServer shareServer;

    private List<BookSourceBean> bookSourceBeans;

    public static void startThis(Activity activity, List<BookSourceBean> bookSourceBeans) {
        String key = String.valueOf(System.currentTimeMillis());
        BitIntentDataManager.getInstance().putData(key, bookSourceBeans);
        Intent intent = new Intent(activity, ShareService.class);
        intent.putExtra("data_key", key);
        intent.setAction(ActionStartService);
        activity.startService(intent);
    }

    public static void upServer(Activity activity) {
        if (isRunning) {
            Intent intent = new Intent(activity, ShareService.class);
            intent.setAction(ActionStartService);
            activity.startService(intent);
        }
    }

    public static void stopThis(Context context) {
        if (isRunning) {
            Intent intent = new Intent(context, ShareService.class);
            intent.setAction(ActionDoneService);
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateNotification("正在启动分享");
        new Handler(Looper.getMainLooper())
                .post(() -> Toast.makeText(this, "正在启动分享\n具体信息查看通知栏", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ActionStartService:
                        upServer(intent);
                        break;
                    case ActionDoneService:
                        stopSelf();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressWarnings("unchecked")
    private void upServer(Intent intent) {
        String key = intent.getStringExtra("data_key");
        if (!TextUtils.isEmpty(key)) {
            bookSourceBeans = (List<BookSourceBean>) BitIntentDataManager.getInstance().getData(key);
        }
        if (shareServer != null && shareServer.isAlive()) {
            shareServer.stop();
        }
        shareServer = new ShareServer(65501, () -> bookSourceBeans);
        InetAddress inetAddress = NetworkUtils.getLocalIPAddress();
        if (inetAddress != null) {
            try {
                shareServer.start();
                isRunning = true;
                updateNotification(String.format("分享地址:%s", inetAddress.getHostAddress()));
            } catch (IOException e) {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (shareServer != null && shareServer.isAlive()) {
            shareServer.stop();
        }
    }

    private PendingIntent getThisServicePendingIntent() {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(ActionDoneService);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 更新通知
     */
    private void updateNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdWeb)
                .setSmallIcon(R.drawable.ic_share)
                .setOngoing(true)
                .setContentTitle(getString(R.string.wifi_share))
                .setContentText(content);
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.cancel), getThisServicePendingIntent());
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        int notificationId = 1122;
        startForeground(notificationId, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
