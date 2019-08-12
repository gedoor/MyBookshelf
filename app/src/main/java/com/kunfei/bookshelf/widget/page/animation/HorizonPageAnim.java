package com.kunfei.bookshelf.widget.page.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 横向动画的模板
 */

public abstract class HorizonPageAnim extends PageAnimation {
    private static final String TAG = "HorizonPageAnim";
    List<Bitmap> bitmapList = new ArrayList<>();

    HorizonPageAnim(int w, int h, View view, OnPageChangeListener listener) {
        super(w, h, view, listener);
        //创建图片
        for (int i = 0; i < 3; i++) {
            bitmapList.add(Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888));
        }
    }

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    @Override
    public boolean changePage() {
        if (isCancel) return false;
        switch (mDirection) {
            case NEXT:
                Collections.swap(bitmapList, 0, 1);
                Collections.swap(bitmapList, 1, 2);
                break;
            case PREV:
                Collections.swap(bitmapList, 1, 2);
                Collections.swap(bitmapList, 0, 1);
                break;
            default:
                return false;
        }
        return true;
    }

    public abstract void drawMove(Canvas canvas);

    @Override
    public void onTouchEvent(MotionEvent event) {
        abortAnim();
        final int slop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
        //获取点击位置
        int x = (int) event.getX();
        int y = (int) event.getY();
        //设置触摸点
        setTouchPoint(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
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
                            setDirection(Direction.PREV);
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
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isRunning = false;
                if (!isMove) {

                    if (!readBookControl.getCanClickTurn()) {
                        return;
                    }

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
                        setDirection(Direction.PREV);
                    }
                } else {
                    isCancel = Math.abs(mLastX - mStartX) < slop * 3 || isCancel;
                }

                // 开启翻页效果
                if (!noNext) {
                    startAnim();
                }
                mView.invalidate();
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (isRunning && !noNext) {
            drawMove(canvas);
        } else {
            canvas.drawBitmap(getBgBitmap(0), 0, 0, null);
            isCancel = true;
        }
    }

    @Override
    public void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            if (changePage()) {
                mListener.changePage(mDirection);
                setDirection(PageAnimation.Direction.NONE);
            }
            movingFinish();
            setTouchPoint(mScroller.getFinalX(), mScroller.getFinalY());
            mView.invalidate();
        }
    }

    @Override
    public Bitmap getBgBitmap(int pageOnCur) {
        if (pageOnCur < 0) {
            return bitmapList.get(0);
        } else if (pageOnCur > 0) {
            return bitmapList.get(2);
        }
        return bitmapList.get(1);
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public void setNoNext(boolean noNext) {
        this.noNext = noNext;
    }
}
