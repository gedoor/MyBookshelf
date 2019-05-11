package com.kunfei.bookshelf.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;


public class CoverImageView extends androidx.appcompat.widget.AppCompatImageView {
    float width,height;

    public CoverImageView(Context context) {
        super(context);
    }

    public CoverImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = measuredWidth * 7 / 5;
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
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

    public void setHeight(int height) {
        int width = height * 5 / 7;
        setMinimumWidth(width);
    }
}
