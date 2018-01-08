package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.mprogressbar.OnProgressListener;

import static com.monke.monkeybook.MApplication.DEBUG;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */

public class ReadAloudService extends Service {
    private static final String TAG = ReadAloudService.class.getSimpleName();
    public static final String mediaButtonAction = "mediaButton";
    public static final String newReadAloudAction = "newReadAloud";
    private static final String doneServiceAction = "doneService";
    private static final String pauseServiceAction = "pauseService";
    private static final String resumeServiceAction = "resumeService";
    private static final String readActivityAction = "readActivity";
    private static final int notificationId = 3222;
    private TextToSpeech textToSpeech;
    private Boolean ttsInitSuccess = false;
    private Boolean speak = false;
    private String content;
    private OnProgressListener progressListener;
    private int nowSpeak;
    private int allSpeak;

    private PendingIntent readPendingIntent;
    private PendingIntent donePendingIntent;
    private PendingIntent pausePendingIntent;
    private PendingIntent resumePendingIntent;

    private AudioManager audioManager;
    private MediaSessionCompat sessionCompat;
    private AudioFocusChangeListener audioFocusChangeListener;
    private AudioFocusRequest mFocusRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        initIntent();
        pauseNotification();

        textToSpeech = new TextToSpeech(this, new TTSListener());

        audioFocusChangeListener = new AudioFocusChangeListener();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initFocusRequest();
        }
        setMediaButtonEvent();
        sessionCompat.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        assert action != null;
        switch (action) {
            case doneServiceAction:
                doneService();
                break;
            case pauseServiceAction:
                pauseReadAloud();
                break;
            case resumeServiceAction:
                resumeReadAloud();
                break;
            case mediaButtonAction:
                aloudControl();
                break;
            case newReadAloudAction:
                newReadAloud(intent.getStringExtra("content"));
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void newReadAloud(String content) {
        this.content = content;
        speak = false;
        nowSpeak = 0;
        playTTS();
    }

    public void playTTS() {
        if (ttsInitSuccess && !speak && requestFocus()) {
            speak = !speak;
            updateMediaSessionPlaybackState();
            String[] splitSpeech = content.split("\r\n");
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
        resumeNotification();
        speak = false;
        updateMediaSessionPlaybackState();
        textToSpeech.stop();
    }

    private void resumeReadAloud() {
        pauseNotification();
        playTTS();
    }

    private void aloudControl() {
        if (speak) {
            pauseReadAloud();
        } else {
            resumeReadAloud();
        }
    }

    private void initIntent() {
        Intent readIntent = new Intent(this, ReadBookActivity.class);
        readIntent.setAction(readActivityAction);
        readPendingIntent = PendingIntent.getActivity(this, 0, readIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent doneIntent = new Intent(this, this.getClass());
        doneIntent.setAction(doneServiceAction);
        donePendingIntent = PendingIntent.getService(this, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent pauseIntent = new Intent(this, this.getClass());
        pauseIntent.setAction(pauseServiceAction);
        pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent resumeIntent = new Intent(this, this.getClass());
        resumeIntent.setAction(resumeServiceAction);
        resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void pauseNotification() {
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(true)
                .setContentTitle(getString(R.string.read_aloud_t))
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(readPendingIntent)
                .addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), donePendingIntent)
                .addAction(R.drawable.ic_pause_black_24dp, getString(R.string.pause), pausePendingIntent);
        //发送通知
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    private void resumeNotification() {
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(false)
                .setContentTitle(getString(R.string.read_aloud_t))
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(readPendingIntent)
                .addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), donePendingIntent)
                .addAction(R.drawable.ic_play_arrow_black_24dp, getString(R.string.resume), resumePendingIntent);
        //发送通知
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
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
        stopForeground(true);
        textToSpeech.stop();
        textToSpeech.shutdown();
        textToSpeech = null;
        unRegisterMediaButton();
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

    private AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    private boolean requestFocus() {
        int request;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request = getAudioManager().requestAudioFocus(mFocusRequest);
        } else {
            request = getAudioManager().requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return ( request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initFocusRequest() {
        AudioAttributes mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build();
    }

    class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (DEBUG) Log.v(TAG, "focusChange: " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // 重新获得焦点,  可做恢复播放，恢复后台音量的操作
                    resumeReadAloud();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 永久丢失焦点除非重新主动获取，这种情况是被其他播放器抢去了焦点，  为避免与其他播放器混音，可将音乐暂停
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 暂时丢失焦点，这种情况是被其他应用申请了短暂的焦点，可压低后台音量
                    pauseReadAloud();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 短暂丢失焦点，这种情况是被其他应用申请了短暂的焦点希望其他声音能压低音量（或者关闭声音）凸显这个声音（比如短信提示音），
                    break;
            }
        }
    }

    private void setMediaButtonEvent() {
        if (sessionCompat != null) return;
        ComponentName mComponent = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mComponent);
        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

        sessionCompat = new MediaSessionCompat(this, TAG, mComponent, mediaButtonReceiverPendingIntent);
        sessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return MediaButtonIntentReceiver.handleIntent(ReadAloudService.this, mediaButtonEvent);
            }
        });
        sessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        sessionCompat.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
    }

    private void unRegisterMediaButton() {
        if (sessionCompat != null) {
            sessionCompat.setCallback(null);
            sessionCompat.setActive(false);
            sessionCompat.release();
        }
        getAudioManager().abandonAudioFocus(audioFocusChangeListener);
    }

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    private void updateMediaSessionPlaybackState() {
        sessionCompat.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(speak ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                                nowSpeak, 1)
                        .build());
    }
}
