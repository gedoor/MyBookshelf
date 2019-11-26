package com.kunfei.bookshelf.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.AppActivityManager;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.view.activity.ReadBookActivity;

/**
 * Created by GKF on 2018/1/6.
 * 监听耳机键
 */

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    public static final String TAG = MediaButtonIntentReceiver.class.getSimpleName();

    public static boolean handleIntent(final Context context, final Intent intent) {
        final String intentAction = intent.getAction();
        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return false;
            }

            final int keycode = event.getKeyCode();
            final int action = event.getAction();

            String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    command = ReadAloudService.ActionMediaPlay;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = ReadAloudService.ActionMediaPrev;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = ReadAloudService.ActionMediaNext;
                    break;
                default:
                    break;
            }
            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    readAloud(context, command);
                    return true;
                }
            }
        }
        return false;
    }

    private static void readAloud(final Context context, String command) {
        if (!AppActivityManager.getInstance().isExist(ReadBookActivity.class)) {
            Intent intent = new Intent(context, ReadBookActivity.class);
            intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
            intent.putExtra("readAloud", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            RxBus.get().post(RxBusTag.MEDIA_BUTTON, command);
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (handleIntent(context, intent) && isOrderedBroadcast()) {
            abortBroadcast();
        }
    }
}
