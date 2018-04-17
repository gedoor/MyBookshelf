package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.monke.monkeybook.help.RunMediaPlayer;
import com.monke.monkeybook.view.activity.ReadBookActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.monke.monkeybook.MApplication.DEBUG;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */

public class ReadAloudService extends Service {
    public static final int PLAY = 1;
    public static final int STOP = 0;
    public static final int PAUSE = 2;
    public static final String ActionMediaButton = "mediaButton";
    public static final String ActionNewReadAloud = "newReadAloud";
    public static final String ActionDoneService = "doneService";
    public static final String ActionPauseService = "pauseService";
    public static final String ActionResumeService = "resumeService";
    private static final String TAG = ReadAloudService.class.getSimpleName();
    private static final String ActionReadActivity = "readActivity";
    private static final String ActionSetTimer = "updateTimer";
    private static final int notificationId = 3222;
    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;
    public static Boolean running = false;
    private TextToSpeech textToSpeech;
    private Boolean ttsInitSuccess = false;
    private Boolean speak = true;
    private Boolean pause = false;
    private String content;
    private int nowSpeak;
    private int allSpeak;
    private int timeMinute = 0;
    private final int maxTimeMinute = 60;
    private boolean timerEnable = false;
    private Timer mTimer;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSessionCompat;
    private AudioFocusChangeListener audioFocusChangeListener;
    private AudioFocusRequest mFocusRequest;
    private AloudServiceListener aloudServiceListener;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences preference;
    private int speechRate;

    @Override
    public void onCreate() {
        super.onCreate();
        preference = this.getSharedPreferences("CONFIG", 0);
        textToSpeech = new TextToSpeech(this, new TTSListener());
        audioFocusChangeListener = new AudioFocusChangeListener();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initFocusRequest();
        }
        initMediaSession();
        initBroadcastReceiver();
        mediaSessionCompat.setActive(true);
        updateMediaSessionPlaybackState();
        updateNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        assert action != null;
        switch (action) {
            case ActionDoneService:
                doneService();
                break;
            case ActionPauseService:
                pauseReadAloud(true);
                break;
            case ActionResumeService:
                resumeReadAloud();
                break;
            case ActionMediaButton:
                aloudControl();
                break;
            case ActionSetTimer:
                updateTimer(intent.getIntExtra("minute", 10));
                break;
            case ActionNewReadAloud:
                newReadAloud(intent.getStringExtra("content"), intent.getBooleanExtra("aloudButton", false));
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void newReadAloud(String content, Boolean aloudButton) {
        if (content == null) {
            stopSelf();
            return;
        }
        nowSpeak = 0;
        this.content = content.replaceAll("……", "");
        running = true;
        if (aloudButton || speak) {
            speak = false;
            pause = false;
            playTTS();
        }
    }

    public void playTTS() {
        if (content == null) {
            stopSelf();
            return;
        }
        if (ttsInitSuccess && !speak && requestFocus()) {
            speak = !speak;
            aloudServiceListener.setStatus(PLAY);
            updateNotification();
            initSpeechRate();
            String[] splitSpeech = content.split("\r\n");
            allSpeak = splitSpeech.length;
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "content");
            for (int i = nowSpeak; i < allSpeak; i++) {
                if (i == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_FLUSH, null, "content");
                    } else {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_FLUSH, map);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_ADD, null, "content");
                    } else {
                        textToSpeech.speak(splitSpeech[i], TextToSpeech.QUEUE_ADD, map);
                    }
                }
            }
        }
    }

    private void initSpeechRate() {
        if (speechRate != preference.getInt("speechRate", 10) && !preference.getBoolean("speechRateFollowSys", true)) {
            speechRate = preference.getInt("speechRate", 10);
            float speechRateF = (float) speechRate / 10;
            textToSpeech.setSpeechRate(speechRateF);
        }
    }

    public static void stop(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionDoneService);
            context.startService(intent);
        }
    }

    public static void pause(Context context) {
        Intent intent = new Intent(context, ReadAloudService.class);
        intent.setAction(ActionPauseService);
        context.startService(intent);
    }

    public static void resume(Context context) {
        Intent intent = new Intent(context, ReadAloudService.class);
        intent.setAction(ActionResumeService);
        context.startService(intent);
    }

    public static void setTimer(Context context) {
        Intent intent = new Intent(context, ReadAloudService.class);
        intent.setAction(ActionSetTimer);
        context.startService(intent);
    }

    /**
     * 关闭服务
     */
    private void doneService() {
        cancelTimer();
        stopSelf();
        if (aloudServiceListener != null) {
            aloudServiceListener.setStatus(STOP);
            aloudServiceListener.stopService();
        }
    }

    /**
     * @param pause true 暂停, false 失去焦点
     */
    private void pauseReadAloud(Boolean pause) {
        this.pause = pause;
        speak = false;
        updateNotification();
        updateMediaSessionPlaybackState();
        textToSpeech.stop();
        if (pause && aloudServiceListener != null) {
            aloudServiceListener.setStatus(PAUSE);
        }
    }

    /**
     * 恢复朗读
     */
    private void resumeReadAloud() {
        updateTimer(0);
        pause = false;
        playTTS();
    }

    private void aloudControl() {
        if (pause) {
            resumeReadAloud();
        } else {
            pauseReadAloud(true);
        }
    }

    private void updateTimer(int minute) {
        timeMinute = timeMinute + minute;
        if (timeMinute > maxTimeMinute) {
            timerEnable = false;
            cancelTimer();
            timeMinute = 0;
            updateNotification();
        } else if (timeMinute <= 0) {
            if (timerEnable) {
                cancelTimer();
                doneService();
            }
        } else {
            timerEnable = true;
            updateNotification();
            setTimer();
        }
    }

    private void setTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!pause) {
                        Intent setTimerIntent = new Intent(getApplicationContext(), ReadAloudService.class);
                        setTimerIntent.setAction(ActionSetTimer);
                        setTimerIntent.putExtra("minute", -1);
                        startService(setTimerIntent);
                    }
                }
            }, 60000, 60000);
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private PendingIntent getReadBookActivityPendingIntent(String actionStr) {
        Intent intent = new Intent(this, ReadBookActivity.class);
        intent.setAction(actionStr);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 更新通知
     */
    private void updateNotification() {
        String title;
        if (pause) {
            title = getString(R.string.read_aloud_pause);
        } else if (timeMinute > 0 && timeMinute <= 60) {
            title = String.format(getString(R.string.read_aloud_timer), timeMinute);
        } else {
            title = getString(R.string.read_aloud_t);
        }
        if (aloudServiceListener != null) {
            aloudServiceListener.upTimer(title);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(getString(R.string.read_aloud_s))
                .setContentIntent(getReadBookActivityPendingIntent(ActionReadActivity));
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), getThisServicePendingIntent(ActionDoneService));
        if (pause) {
            builder.addAction(R.drawable.ic_play_arrow_black_24dp, getString(R.string.resume), getThisServicePendingIntent(ActionResumeService));
        } else {
            builder.addAction(R.drawable.ic_pause_black_24dp, getString(R.string.pause), getThisServicePendingIntent(ActionPauseService));
        }
        builder.addAction(R.drawable.ic_timer_black_24dp, getString(R.string.set_timer), getThisServicePendingIntent(ActionSetTimer));
        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat.getSessionToken()).setShowActionsInCompactView(0, 1, 2));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    public void setAloudServiceListener(AloudServiceListener aloudServiceListener) {
        this.aloudServiceListener = aloudServiceListener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        clearTTS();
        unRegisterMediaButton();
        unregisterReceiver(broadcastReceiver);
    }

    private void clearTTS() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    private void unRegisterMediaButton() {
        if (mediaSessionCompat != null) {
            mediaSessionCompat.setCallback(null);
            mediaSessionCompat.setActive(false);
            mediaSessionCompat.release();
        }
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    /**
     * @return 音频焦点
     */
    private boolean requestFocus() {
        RunMediaPlayer.playSilentSound(this);
        int request;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request = audioManager.requestAudioFocus(mFocusRequest);
        } else {
            request = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return (request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initFocusRequest() {
        AudioAttributes mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build();
    }

    /**
     * 初始化MediaSession
     */
    private void initMediaSession() {
        ComponentName mComponent = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mComponent);
        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(this, 0,
                mediaButtonIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mediaSessionCompat = new MediaSessionCompat(this, TAG, mComponent, mediaButtonReceiverPendingIntent);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return MediaButtonIntentReceiver.handleIntent(ReadAloudService.this, mediaButtonEvent);
            }
        });
        mediaSessionCompat.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                    pauseReadAloud(true);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void updateMediaSessionPlaybackState() {
        mediaSessionCompat.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(speak ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                                nowSpeak, 1)
                        .build());
    }

    public interface AloudServiceListener {
        void stopService();

        void readAloudNext();

        void showMassage(String msg);

        void setStatus(int status);

        void upTimer(String text);
    }

    public class MyBinder extends Binder {
        public ReadAloudService getService() {
            return ReadAloudService.this;
        }
    }

    private final class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.CHINA);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    aloudServiceListener.showMassage("LANG_MISSING_DATA  or LANG_NOT_SUPPORTED!");
                } else {
                    textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
                    ttsInitSuccess = true;
                    playTTS();
                }
            } else {
                if (aloudServiceListener != null) {
                    aloudServiceListener.showMassage("TTS初始化失败");
                    doneService();
                }
            }
        }
    }

    private class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {
            updateMediaSessionPlaybackState();
        }

        @Override
        public void onDone(String s) {
            nowSpeak = nowSpeak + 1;
            if (nowSpeak == allSpeak) {
                aloudServiceListener.readAloudNext();
            }
        }

        @Override
        public void onError(String s) {
            pauseReadAloud(true);
            aloudServiceListener.setStatus(PAUSE);
        }
    }

    class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (DEBUG) Log.v(TAG, "focusChange: " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // 重新获得焦点,  可做恢复播放，恢复后台音量的操作
                    if (!pause) {
                        resumeReadAloud();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 永久丢失焦点除非重新主动获取，这种情况是被其他播放器抢去了焦点，  为避免与其他播放器混音，可将音乐暂停
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 暂时丢失焦点，这种情况是被其他应用申请了短暂的焦点，可压低后台音量
                    if (!pause) {
                        pauseReadAloud(false);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 短暂丢失焦点，这种情况是被其他应用申请了短暂的焦点希望其他声音能压低音量（或者关闭声音）凸显这个声音（比如短信提示音），
                    break;
            }
        }
    }

}
