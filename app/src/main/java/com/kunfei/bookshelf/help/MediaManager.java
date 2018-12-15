package com.kunfei.bookshelf.help;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;

/**
 * Created by GKF on 2018/1/9.
 * 播放音频
 */

public class MediaManager {
    private static int VOLUME;
    private AudioManager audioManager;
    private int stream;
    private final int FADE_DURATION = 1000;
    private final int FADE_INTERVAL = 100;
    private boolean isFading = false;
    private boolean cancelFading = false;

    public static MediaManager instance;

    private MediaManager() {
        audioManager = (AudioManager) MApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
    }

    public void setStream(int stream) {
        this.stream = stream;
        getSysVolume();
    }

    public static synchronized MediaManager getInstance() {
        if (instance == null)
            instance = new MediaManager();
        return instance;
    }

    public void fadeInVolume() {
        if (!isFading) {
            getSysVolume();
        } else {
            cancelFading = true;
        }
        while (isFading) try {
            Thread.sleep(10);
        } catch (Exception ignored) {
        }
        cancelFading = false;
        startAudioFade(1, VOLUME);
        setSysVolume(VOLUME);
    }

    public void fadeOutVolume() {
        if (!isFading) {
            getSysVolume();
        } else {
            cancelFading = true;
        }
        while (isFading) try {
            Thread.sleep(FADE_INTERVAL);
        } catch (Exception ignored) {
        }
        cancelFading = false;
        startAudioFade(VOLUME, 1);
        setSysVolume(VOLUME);
    }

    private void getSysVolume() {
        VOLUME = audioManager.getStreamVolume(stream);
    }

    private void setSysVolume(float vol) {
        audioManager.setStreamVolume(stream, (int) vol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private void startAudioFade(float from, float to) {
        isFading = true;
        cancelFading = false;
        int numberOfSteps = FADE_DURATION / FADE_INTERVAL;
        float deltaVolume = (to - from) / numberOfSteps;
        for (float vol = from; (vol - to) * (vol - from) <= 0 && !cancelFading; vol += deltaVolume) {
            setSysVolume(vol);
            try {
                Thread.sleep(FADE_INTERVAL);
            } catch (Exception ignored) {
            }
        }
        isFading = false;
        cancelFading = false;
    }

    public static void playSilentSound(Context mContext) {
        try {
            // Stupid Android 8 "Oreo" hack to make media buttons work
            final MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.silent_sound);
            mMediaPlayer.setOnCompletionListener(mediaPlayer -> mMediaPlayer.release());
            mMediaPlayer.start();
        } catch (Exception ignored) {
        }
    }
}
