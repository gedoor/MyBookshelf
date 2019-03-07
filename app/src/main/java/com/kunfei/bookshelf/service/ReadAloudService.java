package com.kunfei.bookshelf.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.MediaManager;
import com.kunfei.bookshelf.view.activity.ReadBookActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */
public class ReadAloudService extends Service {
    private static final String TAG = ReadAloudService.class.getSimpleName();
    public static final String ActionMediaButton = "mediaButton";
    public static final String ActionNewReadAloud = "newReadAloud";
    public static final String ActionDoneService = "doneService";
    public static final String ActionPauseService = "pauseService";
    public static final String ActionResumeService = "resumeService";
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
    private List<String> contentList = new ArrayList<>();
    private int nowSpeak;
    private int timeMinute = 0;
    private boolean timerEnable = false;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSessionCompat;
    private AudioFocusChangeListener audioFocusChangeListener;
    private AudioFocusRequest mFocusRequest;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences preference;
    private int speechRate;
    private String title;
    private String text;
    private boolean fadeTts;
    private Handler handler = new Handler();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable dsRunnable;
    private MediaManager mediaManager;
    private int readAloudNumber;

    /**
     * 朗读
     */
    public static void play(Context context, Boolean aloudButton, String content, String title, String text) {
        Intent readAloudIntent = new Intent(context, ReadAloudService.class);
        readAloudIntent.setAction(ActionNewReadAloud);
        readAloudIntent.putExtra("aloudButton", aloudButton);
        readAloudIntent.putExtra("content", content);
        readAloudIntent.putExtra("title", title);
        readAloudIntent.putExtra("text", text);
        context.startService(readAloudIntent);
    }

    /**
     * @param context 停止
     */
    public static void stop(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionDoneService);
            context.startService(intent);
        }
    }

    /**
     * @param context 暂停
     */
    public static void pause(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionPauseService);
            context.startService(intent);
        }
    }

    /**
     * @param context 继续
     */
    public static void resume(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionResumeService);
            context.startService(intent);
        }
    }

    public static void setTimer(Context context, int minute) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionSetTimer);
            intent.putExtra("minute", minute);
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        preference = MApplication.getConfigPreferences();
        textToSpeech = new TextToSpeech(this, new TTSListener());
        audioFocusChangeListener = new AudioFocusChangeListener();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaManager = MediaManager.getInstance();
        mediaManager.setStream(TextToSpeech.Engine.DEFAULT_STREAM);
        fadeTts = preference.getBoolean("fadeTTS", false);
        dsRunnable = this::doDs;
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
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ActionDoneService:
                        stopSelf();
                        break;
                    case ActionPauseService:
                        pauseReadAloud(true);
                        break;
                    case ActionResumeService:
                        resumeReadAloud();
                        break;
                    case ActionSetTimer:
                        updateTimer(intent.getIntExtra("minute", 10));
                        break;
                    case ActionNewReadAloud:
                        newReadAloud(intent.getStringExtra("content"),
                                intent.getBooleanExtra("aloudButton", false),
                                intent.getStringExtra("title"),
                                intent.getStringExtra("text"));
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
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

    private void newReadAloud(String content, Boolean aloudButton, String title, String text) {
        if (content == null) {
            stopSelf();
            return;
        }
        this.text = text;
        this.title = title;
        nowSpeak = 0;
        readAloudNumber = 0;
        contentList.clear();
        String[] splitSpeech = content.split("\n");
        for (String aSplitSpeech : splitSpeech) {
            if (!isEmpty(aSplitSpeech)) {
                contentList.add(aSplitSpeech);
            }
        }
        if (aloudButton || speak) {
            speak = false;
            pause = false;
            playTTS();
        }
    }

    public void playTTS() {
        if (fadeTts) {
            AsyncTask.execute(() -> mediaManager.fadeInVolume());
            handler.postDelayed(this::playTTSN, 200);
        } else {
            playTTSN();
        }
    }

    public void playTTSN() {
        if (contentList.size() < 1) {
            RxBus.get().post(RxBusTag.ALOUD_STATE, Status.NEXT);
            return;
        }
        if (ttsInitSuccess && !speak && requestFocus()) {
            speak = !speak;
            RxBus.get().post(RxBusTag.ALOUD_STATE, Status.PLAY);
            updateNotification();
            initSpeechRate();
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "content");
            for (int i = nowSpeak; i < contentList.size(); i++) {
                if (i == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_FLUSH, null, "content");
                    } else {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_FLUSH, map);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_ADD, null, "content");
                    } else {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_ADD, map);
                    }
                }
            }
        }
    }

    public void toTTSSetting() {
        //跳转到文字转语音设置界面
        try {
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    private void initSpeechRate() {
        if (speechRate != preference.getInt("speechRate", 10) && !preference.getBoolean("speechRateFollowSys", true)) {
            speechRate = preference.getInt("speechRate", 10);
            float speechRateF = (float) speechRate / 10;
            textToSpeech.setSpeechRate(speechRateF);
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
        if (fadeTts) {
            AsyncTask.execute(() -> mediaManager.fadeOutVolume());
            handler.postDelayed(() -> textToSpeech.stop(), 300);
        } else {
            textToSpeech.stop();
        }
        RxBus.get().post(RxBusTag.ALOUD_STATE, Status.PAUSE);
    }

    /**
     * 恢复朗读
     */
    private void resumeReadAloud() {
        updateTimer(0);
        pause = false;
        playTTS();
    }

    private void updateTimer(int minute) {
        timeMinute = timeMinute + minute;
        int maxTimeMinute = 60;
        if (timeMinute > maxTimeMinute) {
            timerEnable = false;
            handler.removeCallbacks(dsRunnable);
            timeMinute = 0;
            updateNotification();
        } else if (timeMinute <= 0) {
            if (timerEnable) {
                handler.removeCallbacks(dsRunnable);
                stopSelf();
            }
        } else {
            timerEnable = true;
            updateNotification();
            handler.removeCallbacks(dsRunnable);
            handler.postDelayed(dsRunnable, 60000);
        }
    }

    private void doDs() {
        if (!pause) {
            setTimer(this, -1);
        }
    }

    private PendingIntent getReadBookActivityPendingIntent() {
        Intent intent = new Intent(this, ReadBookActivity.class);
        intent.setAction(ReadAloudService.ActionReadActivity);
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
        if (text == null)
            text = getString(R.string.read_aloud_s);
        String nTitle;
        if (pause) {
            nTitle = getString(R.string.read_aloud_pause);
        } else if (timeMinute > 0 && timeMinute <= 60) {
            nTitle = getString(R.string.read_aloud_timer, timeMinute);
        } else {
            nTitle = getString(R.string.read_aloud_t);
        }
        nTitle += ": " + title;
        RxBus.get().post(RxBusTag.ALOUD_TIMER, nTitle);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_read_book))
                .setOngoing(true)
                .setContentTitle(nTitle)
                .setContentText(text)
                .setContentIntent(getReadBookActivityPendingIntent());
        if (pause) {
            builder.addAction(R.drawable.ic_play_24dp, getString(R.string.resume), getThisServicePendingIntent(ActionResumeService));
        } else {
            builder.addAction(R.drawable.ic_pause_24dp, getString(R.string.pause), getThisServicePendingIntent(ActionPauseService));
        }
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), getThisServicePendingIntent(ActionDoneService));
        builder.addAction(R.drawable.ic_time_add_24dp, getString(R.string.set_timer), getThisServicePendingIntent(ActionSetTimer));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
        stopForeground(true);
        handler.removeCallbacks(dsRunnable);
        RxBus.get().post(RxBusTag.ALOUD_STATE, Status.STOP);
        unRegisterMediaButton();
        unregisterReceiver(broadcastReceiver);
        clearTTS();
    }

    private void clearTTS() {
        if (textToSpeech != null) {
            if (fadeTts) {
                AsyncTask.execute(() -> mediaManager.fadeOutVolume());
            }
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
        MediaManager.playSilentSound(this);
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

    private final class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.CHINA);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    mainHandler.post(() -> Toast.makeText(ReadAloudService.this, getString(R.string.tts_fix), Toast.LENGTH_SHORT).show());
                    //先停止朗读服务方便用户设置好后的重试
                    ReadAloudService.stop(ReadAloudService.this);
                    //跳转到文字转语音设置界面
                    toTTSSetting();
                } else {
                    textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
                    ttsInitSuccess = true;
                    playTTS();
                }
            } else {
                mainHandler.post(() -> Toast.makeText(ReadAloudService.this, getString(R.string.tts_init_failed), Toast.LENGTH_SHORT).show());
                ReadAloudService.this.stopSelf();
            }
        }
    }

    /**
     * 朗读监听
     */
    private class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {
            updateMediaSessionPlaybackState();
            RxBus.get().post(RxBusTag.READ_ALOUD_START, readAloudNumber + 1);
            RxBus.get().post(RxBusTag.READ_ALOUD_NUMBER, readAloudNumber + 1);
        }

        @Override
        public void onDone(String s) {
            readAloudNumber = readAloudNumber + contentList.get(nowSpeak).length() + 1;
            nowSpeak = nowSpeak + 1;
            if (nowSpeak >= contentList.size()) {
                RxBus.get().post(RxBusTag.ALOUD_STATE, Status.NEXT);
            }
        }

        @Override
        public void onError(String s) {
            pauseReadAloud(true);
            RxBus.get().post(RxBusTag.ALOUD_STATE, Status.PAUSE);
        }

        @Override
        public void onRangeStart(String utteranceId, int start, int end, int frame) {
            super.onRangeStart(utteranceId, start, end, frame);
            RxBus.get().post(RxBusTag.READ_ALOUD_NUMBER, readAloudNumber + start);
        }
    }

    class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
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

    public enum Status {
        PLAY, STOP, PAUSE, NEXT
    }

}
