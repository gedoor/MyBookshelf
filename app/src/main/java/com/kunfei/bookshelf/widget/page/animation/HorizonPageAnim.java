package com.kunfei.bookshelf.widget.page.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 横向动画的模板
 */

public abstract class HorizonPageAnim extends PageAnimation {
    private static final String TAG = "HorizonPageAnim";
    Bitmap mPreBitmap;
    Bitmap mCurBitmap;
    Bitmap mNextBitmap;
    //是否取消翻页
    boolean isCancel = false;

    //可以使用 mLast代替
    private int mMoveX = 0;
    private int mMoveY = 0;
    //是否移动了
    private boolean isMove = false;
    //是否翻阅下一页。true表示翻到下一页，false表示上一页。
    private boolean isNext = false;

    //是否没下一页或者上一页
    private boolean noNext = false;

    HorizonPageAnim(int w, int h, View view, OnPageChangeListener listener) {
        super(w, h, view, listener);
        //创建图片
        mPreBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
        mCurBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
        mNextBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
    }

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    @Override
    public void changePageEnd() {
        switch (mDirection) {
            case NEXT:
                mPreBitmap.recycle();
                mPreBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true);
                mCurBitmap.recycle();
                mCurBitmap = mNextBitmap.copy(Bitmap.Config.RGB_565, true);
                break;
            case PRE:
                mNextBitmap.recycle();
                mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true);
                mCurBitmap.recycle();
                mCurBitmap = mPreBitmap.copy(Bitmap.Config.RGB_565, true);
                break;
        }
    }

    public abstract void drawMove(Canvas canvas);

    @Override
    public void onTouchEvent(MotionEvent event) {
        changePage = false;
        final int slop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
        //获取点击位置
        int x = (int) event.getX();
        int y = (int) event.getY();
        //设置触摸点
        setTouchPoint(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //移动的点击位置
                mMoveX = 0;
                mMoveY = 0;
                //是否移动
                isMove = false;
                //是否存在下一章
                noNext = false;
                //是下一章还是前一章
                isNext = false;
                //是否正在执行动画
                isRunning = false;
                //取消
                isCancel = false;
                //设置起始位置的触摸点
                setStartPoint(x, y);
                //如果存在动画则取消动画
                abortAnim();
                break;
            case MotionEvent.ACTION_MOVE:
                //判断是否移动了
                if (!isMove) {
                    isMove = Math.abs(mStartX - x) > slop || Math.abs(mStartY - y) > slop;
                }

                if (isMove) {
                    //判断是否是准备移动的状态(将要移动但是还没有移动)
                    if (mMoveX == 0 && mMoveY == 0) {
                        //判断翻得是上一页还是下一页
                        if (x - mStartX > 0) {
                            //上一页的参数配置
                            isNext = false;
                            boolean hasPrev = mListener.hasPrev();
                            setDirection(Direction.PRE);
                            //如果上一页不存在
                            if (!hasPrev) {
                                noNext = true;
                                return;
                            }
                        } else {
                            //进行下一页的配置
                            isNext = true;
                            //判断是否下一页存在
                            boolean hasNext = mListener.hasNext(0);
                            //如果存在设置动画方向
                            setDirection(Direction.NEXT);

                            //如果不存在表示没有下一页了
                            if (!hasNext) {
                                noNext = true;
                                return;
                            }
                        }
                    } else {
                        //判断是否取消翻页
                        isCancel = isNext ? x - mMoveX > 0 : x - mMoveX < 0;
                    }

                    mMoveX = x;
                    mMoveY = y;
                    isRunning = true;
                    mView.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                isRunning = false;
                if (!isMove) {
                    isNext = x > mScreenWidth / 2 || readBookControl.getClickAllNext();

                    if (isNext) {
                        //判断是否下一页存在
                        boolean hasNext = mListener.hasNext(0);
                        //设置动画方向
                        if (!hasNext) {
                            return;
                        }
                        setDirection(Direction.NEXT);
                    } else {
                        boolean hasPrev = mListener.hasPrev();
                        if (!hasPrev) {
                            return;
                        }
                        setDirection(Direction.PRE);
                    }
                } else {
                    isCancel = Math.abs(mLastX - mStartX) < slop * 3 || isCancel;
                }

                // 开启翻页效果
                if (!noNext) {
                    startAnim();
                    mView.invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                isCancel = true;
                isRunning = false;
                setDirection(Direction.NONE);
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (isRunning && !noNext) {
            drawMove(canvas);
        } else {
            if (!isCancel && !noNext) {
                switch (mDirection) {
                    case NEXT:
                        canvas.drawBitmap(mNextBitmap, 0, 0, null);
                        break;
                    case PRE:
                        canvas.drawBitmap(mPreBitmap, 0, 0, null);
                        break;
                    default:
                        canvas.drawBitmap(mCurBitmap, 0, 0, null);
                        break;
                }
                changePage = true;
            } else {
                canvas.drawBitmap(mCurBitmap, 0, 0, null);
            }
            isCancel = true;
        }
    }

    @Override
    public void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isRunning = false;
            setTouchPoint(mScroller.getFinalX(), mScroller.getFinalY());
            mView.postInvalidate();
        }
    }

    @Override
    public Bitmap getBgBitmap(int pageOnCur) {
        if (pageOnCur < 0) {
            return mPreBitmap;
        } else if (pageOnCur > 0) {
            return mNextBitmap;
        }
        return mCurBitmap;
    }

    @Override
    public Bitmap getContentBitmap(int pageOnCur) {
        if (pageOnCur < 0) {
            return mPreBitmap;
        } else if (pageOnCur > 0) {
            return mNextBitmap;
        }
        return mCurBitmap;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public void setNoNext(boolean noNext) {
        this.noNext = noNext;
    }
}
