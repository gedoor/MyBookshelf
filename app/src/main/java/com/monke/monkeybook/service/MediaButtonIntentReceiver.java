package com.monke.monkeybook.service;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;

import com.monke.monkeybook.BuildConfig;
import com.monke.monkeybook.service.ReadAloudService;

import static com.monke.monkeybook.service.ReadAloudService.mediaButtonAction;

/**
 * Created by GKF on 2018/1/6.
 * 监听耳机键
 */

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        // 获得KeyEvent对象
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {

            // 获得按键码
            int keycode = event.getKeyCode();

            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    //播放下一首
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    //播放上一首
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    //中间按钮,暂停or播放
                    Intent buttonIntent = new Intent(context, ReadAloudService.class);
                    buttonIntent.setAction(mediaButtonAction);
                    context.startService(buttonIntent);
                    break;
                default:
                    break;
            }
        }
    }

}
