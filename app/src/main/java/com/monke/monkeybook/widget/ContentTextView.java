package com.monke.monkeybook.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

/**
 * Created by ZQH on 2017/4/10.
 * 内容显示
 */

public class ContentTextView extends android.support.v7.widget.AppCompatTextView {

    public ContentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getTextColors().getDefaultColor());
        Layout layout = new StaticLayout(getText(), paint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, getLineSpacingMultiplier(), getLineSpacingExtra(), false);
        layout.draw(canvas);
    }
}