package com.kunfei.bookshelf.widget.page.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.ReadBookControl;

import androidx.annotation.NonNull;

/**
 * 翻页动画抽象类
 */

public abstract class PageAnimation {
    //动画速度
    static final int animationSpeed = 300;
    //正在使用的View
    protected View mView;
    protected ReadBookControl readBookControl = ReadBookControl.getInstance();
    //滑动装置
    Scroller mScroller;
    //监听器
    protected OnPageChangeListener mListener;
    //移动方向
    Direction mDirection = Direction.NONE;

    //屏幕的尺寸
    int mScreenWidth;
    int mScreenHeight;
    int mMarginTop;
    //视图的尺寸
    int mViewWidth;
    int mViewHeight;
    //起始点
    float mStartX;
    float mStartY;
    //触碰点
    float mTouchX;
    float mTouchY;
    //上一个触碰点
    float mLastX;
    float mLastY;

    boolean isRunning = false;
    boolean changePage = false;

    PageAnimation(int w, int h, View view, OnPageChangeListener listener) {
        this(w, h, 0, 0, 0, view, listener);
    }

    PageAnimation(int w, int h, int marginWidth, int marginTop, int marginBottom, View view, OnPageChangeListener listener) {
        mScreenWidth = w;
        mScreenHeight = h;

        //屏幕的间距
        mMarginTop = marginTop;

        mViewWidth = mScreenWidth - marginWidth * 2;
        mViewHeight = mScreenHeight - mMarginTop - marginBottom;

        mView = view;
        mListener = listener;

        mScroller = new Scroller(mView.getContext(), new LinearInterpolator());
    }

    public Scroller getScroller() {
        return mScroller;
    }

    public void setStartPoint(float x, float y) {
        mStartX = x;
        mStartY = y;

        mLastX = mStartX;
        mLastY = mStartY;
    }

    public void setTouchPoint(float x, float y) {
        mLastX = mTouchX;
        mLastY = mTouchY;

        mTouchX = x;
        mTouchY = y;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isChangePage() {
        return changePage;
    }

    /**
     * 开启翻页动画
     */
    public void startAnim() {
        isRunning = true;
        mView.postInvalidate();
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    public void clear() {
        mView = null;
    }

    /**
     * 点击事件的处理
     */
    public abstract void onTouchEvent(MotionEvent event);

    /**
     * 绘制图形
     */
    public abstract void draw(Canvas canvas);

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    public void scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            setTouchPoint(x, y);

            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                isRunning = false;
            }
            mView.postInvalidate();
        }
    }

    /**
     * 取消动画
     */
    public abstract void abortAnim();

    public abstract void changePageEnd();

    /**
     * 获取背景板
     * pageOnCur: 位于当前页的位置, 小于0上一页, 0 当前页, 大于0下一页
     */
    public abstract Bitmap getBgBitmap(int pageOnCur);

    /**
     * 获取内容显示版面
     * pageOnCur: 位于当前页的位置, 小于0上一页, 0 当前页, 大于0下一页
     */
    public abstract Bitmap getContentBitmap(int pageOnCur);

    /**
     * 翻页模式
     */
    public enum Mode {
        COVER(MApplication.getAppResources().getString(R.string.page_mode_COVER)),
        SIMULATION(MApplication.getAppResources().getString(R.string.page_mode_SIMULATION)),
        SLIDE(MApplication.getAppResources().getString(R.string.page_mode_SLIDE)),
        SCROLL(MApplication.getAppResources().getString(R.string.page_mode_SCROLL)),
        NONE(MApplication.getAppResources().getString(R.string.page_mode_NONE));

        private String name;

        Mode(String name) {
            this.name = name;
        }

        public static Mode getPageMode(int pageMode) {
            switch (pageMode) {
                case 0:
                    return COVER;
                case 1:
                    return SIMULATION;
                case 2:
                    return SLIDE;
                case 3:
                    return SCROLL;
                case 4:
                    return NONE;
                default:
                    return COVER;
            }
        }

        public static String[] getAllPageMode() {
            return new String[]{COVER.name, SIMULATION.name, SLIDE.name, SCROLL.name, NONE.name};
        }

        @NonNull
        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * 翻页方向
     */
    public enum Direction {
        NONE(true), NEXT(true), PRE(true), UP(false), DOWN(false);

        public final boolean isHorizontal;

        Direction(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
        }
    }

    public interface OnPageChangeListener {

        void resetScroll();

        boolean hasPrev();

        boolean hasNext(int pageOnCur);

        void drawContent(Canvas canvas, float offset);

        void drawBackground(Canvas canvas);
    }

}
