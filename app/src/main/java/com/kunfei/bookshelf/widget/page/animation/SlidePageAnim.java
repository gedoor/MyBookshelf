package com.kunfei.bookshelf.widget.page.animation;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * 滑动翻页
 */

public class SlidePageAnim extends HorizonPageAnim {
    private Rect mSrcRect, mDestRect, mNextSrcRect, mNextDestRect;

    public SlidePageAnim(int w, int h, View view, OnPageChangeListener listener) {
        super(w, h, view, listener);
        mSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
    }

    @Override
    public void drawMove(Canvas canvas) {
        int dis;
        switch (mDirection) {
            case NEXT:
                //左半边的剩余区域
                dis = (int) (mScreenWidth - mStartX + mTouchX);
                if (dis > mScreenWidth) {
                    dis = mScreenWidth;
                }
                //计算bitmap截取的区域
                mSrcRect.left = mScreenWidth - dis;
                //计算bitmap在canvas显示的区域
                mDestRect.right = dis;
                //计算下一页截取的区域
                mNextSrcRect.right = mScreenWidth - dis;
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis;

                canvas.drawBitmap(mNextBitmap, mNextSrcRect, mNextDestRect, null);
                canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null);
                break;
            default:
                dis = (int) (mTouchX - mStartX);
                if (dis < 0) {
                    dis = 0;
                    mStartX = mTouchX;
                }
                mSrcRect.left = mScreenWidth - dis;
                mDestRect.right = dis;

                //计算下一页截取的区域
                mNextSrcRect.right = mScreenWidth - dis;
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis;

                canvas.drawBitmap(mCurBitmap, mNextSrcRect, mNextDestRect, null);
                canvas.drawBitmap(mPreBitmap, mSrcRect, mDestRect, null);
                break;
        }
    }

    @Override
    public void startAnim() {
        super.startAnim();
        int dx;
        switch (mDirection) {
            case NEXT:
                if (isCancel) {
                    int dis = (int) ((mScreenWidth - mStartX) + mTouchX);
                    if (dis > mScreenWidth) {
                        dis = mScreenWidth;
                    }
                    dx = mScreenWidth - dis;
                } else {
                    dx = (int) -(mTouchX + (mScreenWidth - mStartX));
                }
                break;
            default:
                if (isCancel) {
                    dx = (int) -Math.abs(mTouchX - mStartX);
                } else {
                    dx = (int) (mScreenWidth - (mTouchX - mStartX));
                }
                break;
        }
        //滑动速度保持一致
        int duration = (animationSpeed * Math.abs(dx)) / mScreenWidth;
        mScroller.startScroll((int) mTouchX, 0, dx, 0, duration);
    }
}
