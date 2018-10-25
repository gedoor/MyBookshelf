package com.monke.monkeybook.widget.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.widget.animation.CoverPageAnim;
import com.monke.monkeybook.widget.animation.HorizonPageAnim;
import com.monke.monkeybook.widget.animation.NonePageAnim;
import com.monke.monkeybook.widget.animation.PageAnimation;
import com.monke.monkeybook.widget.animation.ScrollPageAnim;
import com.monke.monkeybook.widget.animation.SimulationPageAnim;
import com.monke.monkeybook.widget.animation.SlidePageAnim;

import java.util.Objects;


/**
 * 绘制页面显示内容的类
 */
public class PageView extends View {

    private final static String TAG = PageView.class.getSimpleName();

    private ReadBookActivity activity;

    private int mViewWidth = 0; // 当前View的宽
    private int mViewHeight = 0; // 当前View的高
    private int statusBarHeight = 0; //状态栏高度

    private int mStartX = 0;
    private int mStartY = 0;
    private boolean isMove = false;
    private boolean actionFromEdge = false;
    private int mPageIndex;
    private int mChapterIndex;
    // 初始化参数
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    // 是否允许点击
    private boolean canTouch = true;
    // 唤醒菜单的区域
    private RectF mCenterRect = null;
    private boolean isPrepare;
    // 动画类
    private PageAnimation mPageAnim;
    //点击监听
    private TouchListener mTouchListener;
    //内容加载器
    private PageLoader mPageLoader;
    // 动画监听类
    private PageAnimation.OnPageChangeListener mPageAnimListener = new PageAnimation.OnPageChangeListener() {
        @Override
        public void changePage(PageAnimation.Direction direction) {
//            mPageLoader.pagingEnd(direction);
        }

        @Override
        public boolean hasPrev() {
            return PageView.this.hasPrevPage();
        }

        @Override
        public boolean hasNext() {
            return PageView.this.hasNextPage();
        }

        @Override
        public void pageCancel() {
            PageView.this.pageCancel();
        }

        @Override
        public void autoNextPage() {
            autoNextPage();
        }

        @Override
        public void autoPrevPage() {
            autoPrevPage();
        }
    };

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        isPrepare = true;

        if (mPageLoader != null) {
            mPageLoader.prepareDisplay(w, h);
        }
    }

    //设置翻页的模式
    void setPageMode(PageMode pageMode, int marginTop, int marginBottom) {
        //视图未初始化的时候，禁止调用
        if (mViewWidth == 0 || mViewHeight == 0 || mPageLoader == null) return;
        if (!readBookControl.getHideStatusBar()) {
            marginTop = marginTop + statusBarHeight;
        }
        switch (pageMode) {
            case SIMULATION:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case COVER:
                mPageAnim = new CoverPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SLIDE:
                mPageAnim = new SlidePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case NONE:
                mPageAnim = new NonePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SCROLL:
                mPageAnim = new ScrollPageAnim(mViewWidth, mViewHeight, 0,
                        marginTop, marginBottom, this, mPageAnimListener);
                break;
            default:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
        }
    }

    public ReadBookActivity getActivity() {
        return activity;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public Bitmap getContentBitmap(int pageOnCur) {
        if (mPageAnim == null) return null;
        return mPageAnim.getContentBitmap(pageOnCur);
    }

    public Bitmap getBgBitmap(int pageOnCur) {
        if (mPageAnim == null) return null;
        return mPageAnim.getBgBitmap(pageOnCur);
    }

    public boolean autoPrevPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            if (hasPrevPage()) {
                resetScroll();
                drawCurPage();
                return true;
            }
            return false;
        } else {
            startPageAnim(PageAnimation.Direction.PRE);
            return true;
        }
    }

    public boolean autoNextPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            if (hasNextPage()) {
                resetScroll();
                drawCurPage();
                return true;
            }
            return false;
        } else {
            startPageAnim(PageAnimation.Direction.NEXT);
            return true;
        }
    }

    private void startPageAnim(PageAnimation.Direction direction) {
        if (mTouchListener == null) return;
        //是否正在执行动画
        abortAnimation();
        if (direction == PageAnimation.Direction.NEXT) {
            int x = mViewWidth;
            int y = mViewHeight;
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            //设置方向
            Boolean hasNext = hasNextPage();

            mPageAnim.setDirection(direction);
            if (!hasNext) {
                return;
            }
        } else {
            int x = 0;
            int y = mViewHeight;
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            mPageAnim.setDirection(direction);
            //设置方向方向
            Boolean hashPrev = hasPrevPage();
            if (!hashPrev) {
                return;
            }
        }
        mPageAnim.startAnim();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制动画
        if (mPageAnim != null) {
            mPageAnim.draw(canvas);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (mPageAnim == null) {
            return true;
        }

        if (!canTouch && event.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        if (actionFromEdge) {
            if (event.getAction() == MotionEvent.ACTION_UP)
                actionFromEdge = false;
            return true;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getEdgeFlags() != 0 || event.getRawY() < ScreenUtils.dpToPx(20) || event.getRawY() > Resources.getSystem().getDisplayMetrics().heightPixels - ScreenUtils.dpToPx(20)) {
                    actionFromEdge = true;
                    return true;
                }
                mStartX = x;
                mStartY = y;
                isMove = false;
                canTouch = mTouchListener.onTouch();
                mPageAnim.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                // 判断是否大于最小滑动值。
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!isMove) {
                    isMove = Math.abs(mStartX - event.getX()) > slop || Math.abs(mStartY - event.getY()) > slop;
                }

                // 如果滑动了，则进行翻页。
                if (isMove) {
                    mPageAnim.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new RectF(mViewWidth / 3, mViewHeight / 3,
                                mViewWidth * 2 / 3, mViewHeight * 2 / 3);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (mTouchListener != null) {
                            mTouchListener.center();
                        }
                        return true;
                    }

                    if (!readBookControl.getCanClickTurn()) {
                        return true;
                    }
                }
                mPageAnim.onTouchEvent(event);
                break;
        }
        return true;
    }

    /**
     * 判断是否存在上一页
     */
    private boolean hasPrevPage() {
        if (mPageLoader.prev()) {
            return true;
        } else {
            activity.showSnackBar(this,"没有上一页");
            return false;
        }
    }

    /**
     * 判断是否下一页存在
     */
    private boolean hasNextPage() {
        if (mPageLoader.next()) {
            return true;
        } else {
            activity.showSnackBar(this, "没有下一页");
            return false;
        }
    }

    private void pageCancel() {
        mPageLoader.pageCancel();
    }

    @Override
    public void computeScroll() {
        //进行滑动
        if (mPageAnim != null) {
            mPageAnim.scrollAnim();
            if (mPageAnim.isChangePage() && !mPageAnim.getScroller().computeScrollOffset()) {
                mPageAnim.changePageEnd();
                mPageLoader.pagingEnd(mPageAnim.getDirection());
                mPageAnim.setDirection(PageAnimation.Direction.NONE);
            }
        }
        super.computeScroll();

    }

    public void upPagePos(int chapterPos, int pagePos) {
        mChapterIndex = chapterPos;
        mPageIndex = pagePos;
    }

    //如果滑动状态没有停止就取消状态，重新设置Anim的触碰点
    public void abortAnimation() {
        mPageAnim.abortAnim();
    }

    public boolean isRunning() {
        return mPageAnim != null && mPageAnim.isRunning();
    }

    public boolean isPrepare() {
        return isPrepare;
    }

    public void setTouchListener(TouchListener mTouchListener) {
        this.mTouchListener = mTouchListener;
    }

    /**
     * 重置滚动位移
     */
    public void resetScroll() {
        if (mPageAnim instanceof ScrollPageAnim) {
            ((ScrollPageAnim) mPageAnim).resetBitmap();
        }
    }

    /**
     * 绘制上一页
     */
    public void drawPrevPage() {
        if (!isPrepare) return;
        mPageLoader.drawPage(getBgBitmap(-1), getContentBitmap(-1), -1);
    }

    /**
     * 绘制当前页。
     */
    public void drawCurPage() {
        if (!isPrepare) return;

        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(0), getContentBitmap(0), 0);
            mPageAnim.setChangePage(true);
            invalidate();
        }
    }

    /**
     * 绘制下一页
     */
    public void drawNextPage() {
        if (!isPrepare) return;

//        if (mPageAnim instanceof HorizonPageAnim) {
//            ((HorizonPageAnim) mPageAnim).changePage();
//        }
        mPageLoader.drawPage(getBgBitmap(1), getContentBitmap(1), 1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPageAnim != null) {
            mPageAnim.abortAnim();
            mPageAnim.clear();
        }

        mPageLoader = null;
        mPageAnim = null;
    }

    /**
     * 获取 PageLoader
     */
    public PageLoader getPageLoader(ReadBookActivity activity, BookShelfBean collBook) {
        this.activity = activity;
        this.statusBarHeight = ImmersionBar.getStatusBarHeight(activity);
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader;
        }
        // 根据书籍类型，获取具体的加载器
        if (Objects.equals(collBook.getTag(), BookShelfBean.LOCAL_TAG)) {
            mPageLoader = new LocalPageLoader(this, collBook);
        } else {
            mPageLoader = new NetPageLoader(this, collBook);
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.prepareDisplay(mViewWidth, mViewHeight);
        }

        return mPageLoader;
    }

    public interface TouchListener {
        boolean onTouch();

        void center();
    }
}
