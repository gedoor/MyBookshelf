package com.monke.monkeybook.widget.animation;

import android.graphics.Canvas;
import android.view.View;

/**
 * 无动画翻页
 */

public class NonePageAnim extends HorizonPageAnim {

    public NonePageAnim(int w, int h, View view, OnPageChangeListener listener) {
        super(w, h, view, listener);
    }

    @Override
    public void drawStatic(Canvas canvas) {
        if (isCancel) {
            canvas.drawBitmap(mCurBitmap, 0, 0, null);
        } else {
            canvas.drawBitmap(mNextBitmap, 0, 0, null);
        }
    }

    @Override
    public void drawMove(Canvas canvas) {
        canvas.drawBitmap(mCurBitmap, 0, 0, null);
    }

    @Override
    public void startAnim() {
        super.startAnim();
        isRunning = false;
        changePage = true;
    }
}
