package com.monke.monkeybook.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;


import android.graphics.Paint;
import android.util.Log;

import java.math.BigDecimal;

/**
 * 解决文字排版混乱参差不齐的问题
 */

public class ContentTextView extends AppCompatTextView {

    private int mLineY;
    private int mViewWidth;
    private TextPaint paint;

    public ContentTextView(Context context) {
        super(context);
    }

    public ContentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (paint==null){
            paint = getPaint();
        }
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mViewWidth = getMeasuredWidth();
        String text = getText().toString();
        mLineY = 0;
        mLineY += getTextSize();
        Layout layout = getLayout();

        if (layout == null) {
            return;
        }

        int textHeight = getLineHeight();

        //计算扩大数据
        double lineCount = ((getMeasuredHeight()) * 1.0f /  textHeight);
        double oneLineAdd = ((getMeasuredHeight()) * 1.0f % textHeight)/((int)lineCount);

        textHeight+=oneLineAdd;

        //解决了最后一行文字间距过大的问题
        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
            String line = text.substring(lineStart, lineEnd);

            if (i < layout.getLineCount() - 1) {
                if (needScale(line)) {
                    drawScaledText(canvas, line, width,i);
                } else {
                    canvas.drawText(line, 0, mLineY, paint);
                }
            } else {
                canvas.drawText(line, 0, mLineY, paint);
            }

            mLineY += textHeight;
        }

    }

    private void drawScaledText(Canvas canvas, String line, float lineWidth,int xuhao) {
        float x = 0;

        if (isFirstLineOfParagraph(line)) {
            String blanks = " ";
            canvas.drawText(blanks, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.substring(3);
        }


        int gapCount = line.length() - 1;

        int i = 0;

        //字长度大于2&&第一个字符和第二个字符都是空格
        if (line.length() > 2 && line.charAt(0) == 12288 && line.charAt(1) == 12288) {
            String substring = line.substring(0, 2);
            float cw = StaticLayout.getDesiredWidth(substring, getPaint());
            canvas.drawText(substring, x, mLineY, getPaint());
            x += cw;
            i += 2;
        }

        float d = (mViewWidth - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, getPaint());
            canvas.drawText(c, x, mLineY, getPaint());
            x += cw + d;
        }

    }

    private boolean isFirstLineOfParagraph(String line) {//字长度大于3&&第一个字符和第二个字符都是空格
        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
    }

    private boolean needScale(String line) {//判断不是空行
        return line != null && line.length() != 0 && line.charAt(line.length() - 1) != '\n';

    }

}