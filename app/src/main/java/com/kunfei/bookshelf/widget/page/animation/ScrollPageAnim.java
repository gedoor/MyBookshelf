package com.kunfei.bookshelf.widget.page.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;


public class ScrollPageAnim extends PageAnimation {
    private static final String TAG = "ScrollAnimation";
    // 滑动追踪的时间
    private static final int VELOCITY_DURATION = 1000;
    private VelocityTracker mVelocity;
    // 整个Bitmap的背景显示
    private Bitmap mBgBitmap;

    public ScrollPageAnim(int w, int h, int marginWidth, int marginTop, int marginBottom, View view, OnPageChangeListener listener) {
        super(w, h, marginWidth, marginTop, marginBottom, view, listener);
        mListener.resetScroll();
        mBgBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        final int slop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
        int x = (int) event.getX();
        int y = (int) event.getY();

        // 初始化速度追踪器
        if (mVelocity == null) {
            mVelocity = VelocityTracker.obtain();
        }

        mVelocity.addMovement(event);
        // 设置触碰点
        setTouchPoint(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                isRunning = false;
                // 设置起始点
                setStartPoint(x, y);
                // 停止动画
                abortAnim();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMove) {
                    isMove = Math.abs(mStartX - x) > slop || Math.abs(mStartY - y) > slop;
                }
                mVelocity.computeCurrentVelocity(VELOCITY_DURATION);
                isRunning = true;
                // 进行刷新
                mView.postInvalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isRunning = false;
                if (!isMove) {

                    if (!readBookControl.getCanClickTurn() || readBookControl.disableScrollClickTurn()) {
                        return;
                    }

                    //是否翻阅下一页。true表示翻到下一页，false表示上一页。
                    boolean isNext = x > mScreenWidth / 2 || readBookControl.getClickAllNext();
                    if (isNext) {
                        startAnim(Direction.NEXT);
                    } else {
                        startAnim(Direction.PREV);
                    }
                } else {
                    // 开启动画
                    startAnim();
                }
                // 删除检测器
                if (mVelocity != null) {
                    mVelocity.recycle();
                    mVelocity = null;
                }
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        //进行布局
        float offset = mLastY - mTouchY;

        //绘制背景
        mListener.drawBackground(canvas);
        //绘制内容
        canvas.save();
        //移动位置
        canvas.translate(0, mMarginTop);
        //裁剪显示区域
        canvas.clipRect(0, 0, mViewWidth, mViewHeight);
        mListener.drawContent(canvas, offset);
        canvas.restore();
    }

    @Override
    public synchronized void startAnim() {
        super.startAnim();
        //惯性滚动
        mScroller.fling(0, (int) mTouchY, 0, (int) mVelocity.getYVelocity(),
                0, 0, -10 * mViewHeight, 10 * mViewHeight);
    }

    /**
     * 翻页动画
     */
    public void startAnim(Direction direction) {
        setStartPoint(0, 0);
        setTouchPoint(0, 0);
        switch (direction) {
            case NEXT:
                super.startAnim();
                mScroller.startScroll(0, 0, 0, -mViewHeight + 300, animationSpeed);
                break;
            case PREV:
                super.startAnim();
                mScroller.startScroll(0, 0, 0, mViewHeight - 300, animationSpeed);
                break;
        }
    }

    @Override
    public void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isRunning = false;
        }
    }

    @Override
    public boolean changePage() {
        return false;
    }

    @Override
    public Bitmap getBgBitmap(int pageOnCur) {
        return mBgBitmap;
    }

}
