package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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
import com.monke.mprogressbar.OnProgressListener;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */

public class ReadAloudService extends Service {
    private static final int notificationId = 3222;
    private static final int doneService = 22;
    private static final int pauseService = 23;
    private static final int resumeService = 24;
    private TextToSpeech textToSpeech;
    private NotificationManager notifyManager;
    private Boolean ttsInitSuccess = false;
    private Boolean speak = false;
    private String content;
    private OnProgressListener progressListener;
    private int nowSpeak;
    private int allSpeak;

    private PendingIntent donePendingIntent;
    private PendingIntent pausePendingIntent;
    private PendingIntent resumePendingIntent;

    @Override
    public void onCreate() {
        textToSpeech = new TextToSpeech(this, new TTSListener());
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        initIntent();
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(true)
                .setContentTitle(getString(R.string.read_aloud_t))
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(donePendingIntent)
                .addAction(R.drawable.ic_stop_black_24dp, "停止", donePendingIntent)
                .addAction(R.drawable.ic_pause_black_24dp, "暂停", pausePendingIntent);
        //发送通知
        Notification notification = builder.build();
        startForeground(notificationId, notification);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int formIntent = intent.getIntExtra("from", 0);
        switch (formIntent) {
            case doneService:
                doneService();
                break;
            case pauseService:
                pauseReadAloud();
                break;
            case resumeService:
                resumeReadAloud();
                break;
            default:
                content = intent.getStringExtra("content");
                speak = false;
                nowSpeak = 0;
                playTTS();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void playTTS() {
        if (ttsInitSuccess && !speak) {
            speak = !speak;
            String[] splitSpeech = content.split("。");
            allSpeak = splitSpeech.length;
            for (int i = nowSpeak; i < allSpeak; i++) {
                if (i == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_FLUSH, null, "content");
                    } else {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_FLUSH, null);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_ADD, null, "content");
                    } else {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        }
    }

    private void doneService() {
        stopSelf();
        progressListener.moveStopProgress(1);
    }

    private void pauseReadAloud() {
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(false)
                .setContentTitle(getString(R.string.read_aloud_t))
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(donePendingIntent)
                .addAction(R.drawable.ic_stop_black_24dp, "停止", donePendingIntent)
                .addAction(R.drawable.ic_play_arrow_black_24dp, "继续", resumePendingIntent);
        //发送通知
        Notification notification = builder.build();
        notifyManager.notify(notificationId, notification);
        speak = false;
        textToSpeech.stop();
    }

    private void resumeReadAloud() {
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(false)
                .setContentTitle(getString(R.string.read_aloud_t))
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(donePendingIntent)
                .addAction(R.drawable.ic_stop_black_24dp, "停止", donePendingIntent)
                .addAction(R.drawable.ic_pause_black_24dp, "暂停", pausePendingIntent);
        //发送通知
        Notification notification = builder.build();
        notifyManager.notify(notificationId, notification);
        playTTS();
    }

    private void initIntent() {
        Intent doneIntent = new Intent(this, this.getClass());
        doneIntent.putExtra("from", doneService);
        donePendingIntent = PendingIntent.getService(this, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent pauseIntent = new Intent(this, this.getClass());
        pauseIntent.putExtra("from", pauseService);
        pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent resumeIntent = new Intent(this, this.getClass());
        resumeIntent.putExtra("from", resumeService);
        resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        stopForeground(true);
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
            nowSpeak = nowSpeak + 1;
            if (nowSpeak == allSpeak) {
                progressListener.setDurProgress(1);
            }
        }

        @Override
        public void onError(String s) {

        }
    }
}
