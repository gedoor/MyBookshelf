package com.monke.monkeybook.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;


public class FilletImageView extends android.support.v7.widget.AppCompatImageView {
    float width,height;

    public FilletImageView(Context context) {
        super(context);
    }

    public FilletImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilletImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width >= 10 && height > 10) {
            @SuppressLint("DrawAllocation")
            Path path = new Path();
            //四个圆角
            path.moveTo(10, 0);
            path.lineTo(width - 10, 0);
            path.quadTo(width, 0, width, 10);
            path.lineTo(width, height - 10);
            path.quadTo(width, height, width - 10, height);
            path.lineTo(10, height);
            path.quadTo(0, height, 0, height - 10);
            path.lineTo(0, 10);
            path.quadTo(0, 0, 10, 0);

            canvas.clipPath(path);
        }
        super.onDraw(canvas);
    }


}
