package com.kunfei.bookshelf.widget.recycler.refresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

public class RefreshScrollView extends ScrollView {
    private RefreshProgressBar rpb;
    private float durTouchY = -1000000;
    private BaseRefreshListener baseRefreshListener;
    private Boolean isRefreshing = false;

    public RefreshScrollView(Context context) {
        this(context, null);
    }

    public RefreshScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RefreshScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setRpb(@NonNull RefreshProgressBar rpb) {
        this.rpb = rpb;
        init();
    }

    private void init() {
        this.setOnTouchListener((View v, MotionEvent event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    durTouchY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (durTouchY == -1000000)
                        durTouchY = event.getY();
                    float dY = event.getY() - durTouchY;  //>0下拉
                    durTouchY = event.getY();
                    if (baseRefreshListener != null && !isRefreshing && rpb.getSecondDurProgress() == rpb.getSecondFinalProgress() && getScrollY() <= 0) {
                        if (rpb.getVisibility() != View.VISIBLE) {
                            rpb.setVisibility(View.VISIBLE);
                        }
                        rpb.setSecondDurProgress((int) (rpb.getSecondDurProgress() + dY));
                        if (rpb.getSecondDurProgress() <= 0) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (baseRefreshListener != null && rpb.getSecondMaxProgress() > 0 && rpb.getSecondDurProgress() > 0) {
                        if (rpb.getSecondDurProgress() >= rpb.getSecondMaxProgress() && !isRefreshing) {
                            startRefresh();
                        } else {
                            rpb.setSecondDurProgressWithAnim(0);
                        }
                    }
                    durTouchY = -1000000;
                    break;
            }
            return false;
        });
    }

    public void setBaseRefreshListener(BaseRefreshListener baseRefreshListener) {
        this.baseRefreshListener = baseRefreshListener;
    }

    public void startRefresh() {
        if (baseRefreshListener != null) {
            isRefreshing = true;
            rpb.setIsAutoLoading(true);
            baseRefreshListener.startRefresh();
        }
    }

    public void finishRefresh() {
        isRefreshing = false;
        rpb.setDurProgress(0);
        rpb.setIsAutoLoading(false);
    }
}
