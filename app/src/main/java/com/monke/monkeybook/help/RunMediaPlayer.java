package com.monke.monkeybook.help;

import android.content.Context;
import android.media.MediaPlayer;

import com.monke.monkeybook.R;

/**
 * Created by GKF on 2018/1/9.
 * 播放音频
 */

public class RunMediaPlayer {

    public static void playSilentSound(Context mContext) {
        // Stupid Android 8 "Oreo" hack to make media buttons work
        final MediaPlayer mMediaPlayer;
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.silent_sound);
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> mMediaPlayer.release());
        mMediaPlayer.start();
    }
}
