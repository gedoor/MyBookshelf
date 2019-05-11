package com.kunfei.bookshelf.widget.recycler.sectioned;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by wsw on 2017/12/12.
 */

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int space;
    private int color = -1;
    private Drawable mDivider;
    private Paint mPaint;
    private int type;

    public int getColor() {
        return color;
    }

    public void setColor(@ColorRes int color) {
        this.color = color;
    }

    public GridSpacingItemDecoration(int space) {
        this.space = space;
    }

    public GridSpacingItemDecoration(int space, int color) {
        this.space = space;
        this.color = color;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(space * 2);
    }

    public GridSpacingItemDecoration(int space, int color, int type) {
        this.space = space;
        this.color = color;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(space * 2);
        this.type = type;
    }

    public GridSpacingItemDecoration(int space, Drawable mDivider) {
        this.space = space;
        this.mDivider = mDivider;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager && !(parent.getLayoutManager() instanceof GridLayoutManager)) {
                if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    outRect.set(space, 0, space, 0);
                } else {
                    outRect.set(0, space, 0, space);
                }
            } else {
                outRect.set(space, space, space, space);
            }
        }

    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager && !(parent.getLayoutManager() instanceof GridLayoutManager)) {
                if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    drawHorizontal(c, parent);
                } else {
                    drawVertical(c, parent);
                }
            } else {
                if (type == 0) {
                    drawGrideview(c, parent);
                } else {
                    drawGrideview1(c, parent);
                }
            }
        }
    }

    //绘制纵向 item 分割线

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + space;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    //绘制横向 item 分割线
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + layoutParams.bottomMargin;
            int bottom = top + space;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }

        }
    }

    //绘制grideview item 分割线 不是填充满的
    private void drawGrideview(Canvas canvas, RecyclerView parent) {
        GridLayoutManager linearLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int childSize = parent.getChildCount();
        assert linearLayoutManager != null;
        int other = parent.getChildCount() / linearLayoutManager.getSpanCount() - 1;
        if (other < 1) {
            other = 1;
        }
        other = other * linearLayoutManager.getSpanCount();
        if (parent.getChildCount() < linearLayoutManager.getSpanCount()) {
            other = parent.getChildCount();
        }
        int top, bottom, left, right, spancount;
        spancount = linearLayoutManager.getSpanCount() - 1;
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            if (i < other) {
                top = child.getBottom() + layoutParams.bottomMargin;
                bottom = top + space;
                left = (layoutParams.leftMargin + space) * (i + 1);
                right = child.getMeasuredWidth() * (i + 1) + left + space * i;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
            if (i != spancount) {
                top = (layoutParams.topMargin + space) * (i / linearLayoutManager.getSpanCount() + 1);
                bottom = (child.getMeasuredHeight() + space) * (i / linearLayoutManager.getSpanCount() + 1) + space;
                left = child.getRight() + layoutParams.rightMargin;
                right = left + space;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            } else {
                spancount += 4;
            }
        }
    }

    /***/
    private void drawGrideview1(Canvas canvas, RecyclerView parent) {
        GridLayoutManager linearLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int childSize = parent.getChildCount();
        int top, bottom, left, right, spancount;
        spancount = linearLayoutManager.getSpanCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            //画横线
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            top = child.getBottom() + layoutParams.bottomMargin;
            bottom = top + space;
            left = layoutParams.leftMargin + child.getPaddingLeft() + space;
            right = child.getMeasuredWidth() * (i + 1) + left + space * i;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
            //画竖线
            top = (layoutParams.topMargin + space) * (i / linearLayoutManager.getSpanCount() + 1);
            bottom = (child.getMeasuredHeight() + space) * (i / linearLayoutManager.getSpanCount() + 1) + space;
            left = child.getRight() + layoutParams.rightMargin;
            right = left + space;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }

            //画上缺失的线框
            if (i < spancount) {
                top = child.getTop() + layoutParams.topMargin;
                bottom = top + space;
                left = (layoutParams.leftMargin + space) * (i + 1);
                right = child.getMeasuredWidth() * (i + 1) + left + space * i;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
            if (i % spancount == 0) {
                top = (layoutParams.topMargin + space) * (i / linearLayoutManager.getSpanCount() + 1);
                bottom = (child.getMeasuredHeight() + space) * (i / linearLayoutManager.getSpanCount() + 1) + space;
                left = child.getLeft() + layoutParams.leftMargin;
                right = left + space;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
        }
    }
}
