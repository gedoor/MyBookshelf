package com.kunfei.bookshelf.widget.recycler.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.kunfei.bookshelf.R;

public class RefreshProgressBar extends View {
    int a = 1;
    private int maxProgress = 100;
    private int durProgress = 0;
    private int secondMaxProgress = 100;
    private int secondDurProgress = 0;
    private int bgColor = 0x00000000;
    private int secondColor = 0xFFC1C1C1;
    private int fontColor = 0xFF363636;
    private int speed = 1;
    private int secondFinalProgress = 0;
    private Paint paint;
    private Handler handler;
    private Boolean isAutoLoading = false;

    public RefreshProgressBar(Context context) {
        this(context, null);
    }

    public RefreshProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);
    }

    public Boolean getIsAutoLoading() {
        return isAutoLoading;
    }

    public void setIsAutoLoading(Boolean loading) {
        if (loading && getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        isAutoLoading = loading;
        if (!isAutoLoading) {
            secondDurProgress = 0;
            secondFinalProgress = 0;
        }
        maxProgress = 0;

        invalidate();
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        handler = new Handler(Looper.getMainLooper());
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshProgressBar);
        speed = a.getDimensionPixelSize(R.styleable.RefreshProgressBar_speed, speed);
        maxProgress = a.getInt(R.styleable.RefreshProgressBar_max_progress, maxProgress);
        durProgress = a.getInt(R.styleable.RefreshProgressBar_dur_progress, durProgress);
        secondDurProgress = a.getDimensionPixelSize(R.styleable.RefreshProgressBar_second_dur_progress, secondDurProgress);
        secondFinalProgress = secondDurProgress;
        secondMaxProgress = a.getDimensionPixelSize(R.styleable.RefreshProgressBar_second_max_progress, secondMaxProgress);
        bgColor = a.getColor(R.styleable.RefreshProgressBar_bg_color, bgColor);
        secondColor = a.getColor(R.styleable.RefreshProgressBar_second_color, secondColor);
        fontColor = a.getColor(R.styleable.RefreshProgressBar_font_color, fontColor);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(bgColor);
        Rect bgRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
        canvas.drawRect(bgRect, paint);

        if (secondDurProgress > 0 && secondMaxProgress > 0) {
            int secondDur = secondDurProgress;
            if (secondDur < 0) {
                secondDur = 0;
            }
            if (secondDur > secondMaxProgress) {
                secondDur = secondMaxProgress;
            }
            paint.setColor(secondColor);
            int tempW = (int) (getMeasuredWidth() * 1.0f * (secondDur * 1.0f / secondMaxProgress));
            Rect secondRect = new Rect(getMeasuredWidth() / 2 - tempW / 2, 0, getMeasuredWidth() / 2 + tempW / 2, getMeasuredHeight());
            canvas.drawRect(secondRect, paint);
        }

        if (durProgress > 0 && maxProgress > 0) {
            paint.setColor(fontColor);
            RectF fontRectF = new RectF(0, 0, getMeasuredWidth() * 1.0f * (durProgress * 1.0f / maxProgress), getMeasuredHeight());
            canvas.drawRect(fontRectF, paint);
        }

        if (isAutoLoading) {
            if (secondDurProgress >= secondMaxProgress) {
                a = -1;
            } else if (secondDurProgress <= 0) {
                a = 1;
            }
            secondDurProgress += (a * speed);
            if (secondDurProgress < 0)
                secondDurProgress = 0;
            else if (secondDurProgress > secondMaxProgress)
                secondDurProgress = secondMaxProgress;
            secondFinalProgress = secondDurProgress;
            invalidate();
        } else {
            if (secondDurProgress != secondFinalProgress) {
                if (secondDurProgress > secondFinalProgress) {
                    secondDurProgress -= speed;
                    if (secondDurProgress < secondFinalProgress) {
                        secondDurProgress = secondFinalProgress;
                    }
                } else {
                    secondDurProgress += speed;
                    if (secondDurProgress > secondFinalProgress) {
                        secondDurProgress = secondFinalProgress;
                    }
                }
                this.invalidate();
            }
            if (secondDurProgress == 0 && durProgress == 0 && secondFinalProgress == 0 && getVisibility() == View.VISIBLE) {
                setVisibility(View.INVISIBLE);
            }
        }
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getDurProgress() {
        return durProgress;
    }

    public void setDurProgress(int durProgress) {
        if (durProgress < 0) {
            durProgress = 0;
        }
        if (durProgress > maxProgress) {
            durProgress = maxProgress;
        }
        this.durProgress = durProgress;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }
    }

    public int getSecondMaxProgress() {
        return secondMaxProgress;
    }

    public void setSecondMaxProgress(int secondMaxProgress) {
        this.secondMaxProgress = secondMaxProgress;
    }

    public int getSecondDurProgress() {
        return secondDurProgress;
    }

    public void setSecondDurProgress(int secondDur) {
        this.secondDurProgress = secondDur;
        this.secondFinalProgress = secondDurProgress;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }
    }

    public void setSecondDurProgressWithAnim(int secondDur) {
        if (secondDur < 0) {
            secondDur = 0;
        }
        if (secondDur > secondMaxProgress) {
            secondDur = secondMaxProgress;
        }
        this.secondFinalProgress = secondDur;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }
    }

    public void clean() {
        durProgress = 0;
        secondDurProgress = 0;
        secondFinalProgress = 0;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getSecondColor() {
        return secondColor;
    }

    public void setSecondColor(int secondColor) {
        this.secondColor = secondColor;
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSecondFinalProgress() {
        return secondFinalProgress;
    }
}
