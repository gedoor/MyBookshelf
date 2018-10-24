package com.monke.monkeybook.widget.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.monke.monkeybook.help.ReadBookControl;

/**
 * Created by newbiechen on 17-7-24.
 * 翻页动画抽象类
 */

public abstract class PageAnimation {
    //正在使用的View
    protected View mView;
    protected ReadBookControl readBookControl = ReadBookControl.getInstance();
    //滑动装置
    protected Scroller mScroller;
    //监听器
    protected OnPageChangeListener mListener;
    //移动方向
    protected Direction mDirection = Direction.NONE;

    //屏幕的尺寸
    protected int mScreenWidth;
    protected int mScreenHeight;
    //屏幕的间距
    protected int mMarginWidth;
    protected int mMarginTop;
    protected int mMarginBottom;
    //视图的尺寸
    protected int mViewWidth;
    protected int mViewHeight;
    //起始点
    protected float mStartX;
    protected float mStartY;
    //触碰点
    protected float mTouchX;
    protected float mTouchY;
    //上一个触碰点
    protected float mLastX;
    protected float mLastY;

    protected boolean isRunning = false;
    protected boolean changePage = false;

    public PageAnimation(int w, int h, View view, OnPageChangeListener listener) {
        this(w, h, 0, 0, 0, view, listener);
    }

    public PageAnimation(int w, int h, int marginWidth, int marginTop, int marginBottom, View view, OnPageChangeListener listener) {
        mScreenWidth = w;
        mScreenHeight = h;

        mMarginWidth = marginWidth;
        mMarginTop = marginTop;
        mMarginBottom = marginBottom;

        mViewWidth = mScreenWidth - mMarginWidth * 2;
        mViewHeight = mScreenHeight - mMarginTop - mMarginBottom;

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

    public void setChangePage(boolean changePage) {
        this.changePage = changePage;
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
    public abstract boolean onTouchEvent(MotionEvent event);

    /**
     * 绘制图形
     */
    public abstract void draw(Canvas canvas);

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    public abstract void scrollAnim();

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

    public enum Direction {
        NONE(true), NEXT(true), PRE(true), UP(false), DOWN(false);

        public final boolean isHorizontal;

        Direction(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
        }
    }

    public interface OnPageChangeListener {
        void changePage(Direction direction);

        boolean hasPrev();

        boolean hasNext();

        void pageCancel();

        void autoNextPage();

        void autoPrevPage();
    }

}
