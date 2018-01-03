package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;

/**
 * Created by GKF on 2018/1/2.
 */

public class ReadAloudService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
