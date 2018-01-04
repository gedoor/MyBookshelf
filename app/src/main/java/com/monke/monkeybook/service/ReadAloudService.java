package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.view.activity.MainActivity;
import com.monke.monkeybook.view.activity.ReadBookActivity;

import java.util.Locale;

import static com.monke.monkeybook.view.activity.ReadBookActivity.OPEN_FROM_ALOUD;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */

public class ReadAloudService extends Service {
    private static final int DONESERVICE = 22;
    private int notifiId = 154;
    private TextToSpeech textToSpeech;
    ReadBookActivity readBookActivity;
    private Boolean ttsInitSuccess = false;
    private Boolean speak = false;
    private String content;

    @Override
    public void onCreate() {
        textToSpeech = new TextToSpeech(this, new TTSListener());
        //创建 Notification.Builder 对象
        Intent doneIntent = new Intent(this, this.getClass());
        doneIntent.putExtra("from", DONESERVICE);
        PendingIntent donePendingIntent = PendingIntent.getService(this, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(false)
                .setContentTitle(getString(R.string.read_aloud))
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(donePendingIntent);
        //发送通知
        Notification notification = builder.build();
        startForeground(notifiId, notification);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getIntExtra("from", 0)==DONESERVICE) {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        content = intent.getStringExtra("content");
        speak = false;
        playTTS();
        return super.onStartCommand(intent, flags, startId);
    }

    public void playTTS() {
        if (ttsInitSuccess && !speak) {
            speak = !speak;
            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public ReadAloudService getService() {
            return ReadAloudService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        textToSpeech.stop();
        textToSpeech.shutdown();
        textToSpeech = null;
    }

    private final class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                ttsInitSuccess = true;
                playTTS();
            }
        }
    }
}
