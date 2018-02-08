package com.monke.monkeybook.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.DensityUtil;

public class RecyclerViewBar extends LinearLayout {
    public static long SLIDE_ANIM_TIME = 800;

    private ImageView ivSlider;
    private int sliderHeight = DensityUtil.dp2px(getContext(), 35f);

    private RecyclerView recyclerView;

    private Animator slideIn;
    private Animator slideOut;

    public RecyclerViewBar(Context context) {
        this(context, null);
    }

    public RecyclerViewBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclerViewBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setOrientation(VERTICAL);
        @SuppressLint("Recycle") TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RecyclerViewBar);
        sliderHeight = a.getDimensionPixelSize(R.styleable.RecyclerViewBar_slider_height, sliderHeight);
        int paddingLeft = a.getDimensionPixelSize(R.styleable.RecyclerViewBar_slider_paddingLeft, 0);
        int paddingRight = a.getDimensionPixelSize(R.styleable.RecyclerViewBar_slider_paddingRight, 0);
        ivSlider = new ImageView(getContext());
        ivSlider.setPadding(paddingLeft, 0, paddingRight, 0);
        ivSlider.setAlpha(0f);
        ivSlider.setClickable(true);
        addView(ivSlider);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sliderHeight);
        ivSlider.setLayoutParams(layoutParams);
        ivSlider.setImageResource(R.drawable.icon_slider);
        ivSlider.setScaleType(ImageView.ScaleType.FIT_XY);

        initIvSlider();

        RecyclerViewBar.this.getViewTreeObserver().addOnGlobalLayoutListener(layoutInitListener);
    }

    private float finalY = -10000;

    @SuppressLint("ClickableViewAccessibility")
    private void initIvSlider() {
        ivSlider.setOnTouchListener((View v, MotionEvent event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    finalY = event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (finalY >= 0) {
                        float tempY = event.getY();
                        float durY = tempY - finalY;
                        updateSlider(durY);

                        showSlide();
                    } else {
                        finalY = event.getY();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (finalY >= 0) {
                        finalY = -10000;
                        timeCountDown.cancel();
                        timeCountDown.start();
                        return true;
                    }
                    break;
                default:
                    if (finalY >= 0) {
                        finalY = -10000;
                        return true;
                    }
                    break;
            }
            return false;
        });
    }

    private void updateSlider(float durY) {
        LayoutParams l = (LayoutParams) ivSlider.getLayoutParams();
        float finalMarginTop = l.topMargin + durY;
        if (finalMarginTop < 0) {
            finalMarginTop = 0;
        } else if (finalMarginTop > getHeight() - sliderHeight) {
            finalMarginTop = getHeight() - sliderHeight;
        }
        if (recyclerView != null) {
            int position = Math.round(finalMarginTop / (getHeight() - sliderHeight) * (recyclerView.getAdapter().getItemCount() - 1));
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
        }

        l.topMargin = Math.round(finalMarginTop);
        ivSlider.setLayoutParams(l);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        if (this.recyclerView != null) {
            this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState != 0) {
                        showSlide();
                    } else {
                        timeCountDown.cancel();
                        timeCountDown.start();
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    scrollToPositionWithOffset(((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
                }
            });
        }
    }

    public void scrollToPositionWithOffset(int position) {
        if (recyclerView != null && position < recyclerView.getAdapter().getItemCount()) {
            float temp = position * 1.0f / recyclerView.getAdapter().getItemCount();
            LayoutParams l = (LayoutParams) ivSlider.getLayoutParams();
            l.topMargin = Math.round(((getHeight() - sliderHeight) * temp));
            ivSlider.setLayoutParams(l);
        }
    }

    private void showSlide() {
        if (ivSlider.getAlpha() < 1) {
            if (slideOut != null && slideOut.isRunning()) {
                slideOut.cancel();
            }
            if (slideIn == null) {
                slideIn = ObjectAnimator.ofFloat(ivSlider, "alpha", ivSlider.getAlpha(), 1f);
                slideIn.setDuration((long) (SLIDE_ANIM_TIME * (1f - ivSlider.getAlpha())));
            }
            if (!slideIn.isRunning()) {
                slideIn.start();
            }
        }
    }

    private void hideSlide() {
        if (ivSlider.getAlpha() > 0) {
            if (slideIn != null && slideIn.isRunning()) {
                slideIn.cancel();
            }
            if (slideOut == null) {
                slideOut = ObjectAnimator.ofFloat(ivSlider, "alpha", ivSlider.getAlpha(), 0f);
                slideOut.setDuration((long) (SLIDE_ANIM_TIME * ivSlider.getAlpha()));
            }
            if (!slideOut.isRunning()) {
                slideOut.start();
            }
        }
    }

    private TimeCountDown timeCountDown = new TimeCountDown();

    class TimeCountDown extends CountDownTimer {

        TimeCountDown() {
            this(1000, 1000);
        }

        TimeCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            hideSlide();
        }
    }

    private int height = 0;
    private ViewTreeObserver.OnGlobalLayoutListener layoutInitListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if(getHeight()>0){
                if (height == 0) {
                    height = getHeight();
                } else {
                    int diff = height -  getHeight();
                    if (diff != 0) {
                        LayoutParams l = (LayoutParams) ivSlider.getLayoutParams();
                        l.topMargin = (int) ((l.topMargin*1.0f/(height-sliderHeight))*(getHeight()-sliderHeight));
                        ivSlider.setLayoutParams(l);
                        height = getHeight();
                    }
                }
            }
        }
    };
}