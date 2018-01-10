package com.monke.monkeybook.utils;

import android.os.Handler;
import android.view.KeyEvent;

/**
 * Created by GKF on 2018/1/10.
 */

public class KeyUtil {
    private boolean isVolumeDown = false;
    private boolean isVolumeUp = false;
    private boolean isMenu = false;
    private int currentKeyCode = 0;

    private static Boolean isDoubleClick = false;
    private static Boolean isLongClick = false;

    private CheckForLongPress mPendingCheckForLongPress = null;
    private CheckForDoublePress mPendingCheckForDoublePress = null;
    private Handler mHandler = new Handler();
    private OnKeyDownListener onKeyDownListener;

    public static KeyUtil getInstance() {
        return new KeyUtil();
    }

    public void dispatchKeyEvent(KeyEvent event) {
        int keycode = event.getKeyCode();

        // 有不同按键按下，取消长按、短按的判断
        if (currentKeyCode != keycode) {
            removeLongPressCallback();
            removeDoublePressCallback();
            isDoubleClick = false;
            onKeyDownListener.twoKeyDown();
        }

        // 处理长按、单击、双击按键
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            checkForLongClick(event);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            checkForDoubleClick(event);
        }

        if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                isVolumeDown = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                isVolumeDown = false;
            }
        } else if (keycode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                isVolumeUp = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                isVolumeUp = false;
            }
        } else if (keycode == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                isMenu = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                isMenu = true;
            }
        }

        // 判断组合按键
        if (isVolumeDown
                && isVolumeUp
                && isMenu
                && (keycode == KeyEvent.KEYCODE_VOLUME_UP
                || keycode == KeyEvent.KEYCODE_VOLUME_DOWN || keycode == KeyEvent.KEYCODE_MENU)
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            //组合按键事件处理；
            isVolumeDown = false;
            isVolumeUp = false;
            isMenu = false;
        }
    }

    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            mHandler.removeCallbacks(mPendingCheckForLongPress);
        }
    }

    private void checkForLongClick(KeyEvent event) {
        int count = event.getRepeatCount();
        int keycode = event.getKeyCode();
        if (count == 0) {
            currentKeyCode = keycode;
        } else {
            return;
        }
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.setKeycode(event.getKeyCode());
        mHandler.postDelayed(mPendingCheckForLongPress, 1000);
    }

    class CheckForLongPress implements Runnable {

        int currentKeycode = 0;

        public void run() {
            isLongClick = true;
            onKeyDownListener.longPress(currentKeycode);
        }

        public void setKeycode(int keycode) {
            currentKeycode = keycode;
        }
    }

    //返回事件
    public interface  OnKeyDownListener {
        void longPress(int keycode);
        void singleClick(int keycode);
        void doublePress(int keycode);
        void twoKeyDown();
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    private void checkForDoubleClick(KeyEvent event) {
        // 有长按时间发生，则不处理单击、双击事件
        removeLongPressCallback();
        if (isLongClick) {
            isLongClick = false;
            return;
        }

        if (!isDoubleClick) {
            isDoubleClick = true;
            if (mPendingCheckForDoublePress == null) {
                mPendingCheckForDoublePress = new CheckForDoublePress();
            }
            mPendingCheckForDoublePress.setKeycode(event.getKeyCode());
            mHandler.postDelayed(mPendingCheckForDoublePress, 500);
        } else {
            // 500ms内两次单击，触发双击
            isDoubleClick = false;
            onKeyDownListener.doublePress(event.getKeyCode());
        }
    }

    class CheckForDoublePress implements Runnable {

        int currentKeycode = 0;

        public void run() {
            if (isDoubleClick) {
                onKeyDownListener.singleClick(currentKeycode);
            }
            isDoubleClick = false;
        }

        public void setKeycode(int keycode) {
            currentKeycode = keycode;
        }
    }

    private void removeDoublePressCallback() {
        if (mPendingCheckForDoublePress != null) {
            mHandler.removeCallbacks(mPendingCheckForDoublePress);
        }
    }
}
