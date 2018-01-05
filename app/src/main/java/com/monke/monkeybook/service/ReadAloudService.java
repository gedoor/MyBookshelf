package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.mprogressbar.OnProgressListener;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */

public class ReadAloudService extends Service {
    private static final int doneService = 22;
    private TextToSpeech textToSpeech;
    private Boolean ttsInitSuccess = false;
    private Boolean speak = false;
    private String content;
    private OnProgressListener progressListener;

    @Override
    public void onCreate() {
        textToSpeech = new TextToSpeech(this, new TTSListener());
        //创建 Notification.Builder 对象
        Intent doneIntent = new Intent(this, this.getClass());
        doneIntent.putExtra("from", doneService);
        PendingIntent donePendingIntent = PendingIntent.getService(this, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(false)
                .setContentTitle(getString(R.string.read_aloud_t))
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(donePendingIntent);
        //发送通知
        Notification notification = builder.build();
        startForeground(3222, notification);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getIntExtra("from", 0)== doneService) {
            stopSelf();
            progressListener.moveStopProgress(1);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, "content");
            }
        }
    }

    public void setOnProgressListener(OnProgressListener onProgressListener){
        progressListener = onProgressListener;
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
                textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
                ttsInitSuccess = true;
                playTTS();
            }
        }
    }

    private class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {

        }

        @Override
        public void onDone(String s) {
            progressListener.setDurProgress(1);
        }

        @Override
        public void onError(String s) {

        }
    }
}
