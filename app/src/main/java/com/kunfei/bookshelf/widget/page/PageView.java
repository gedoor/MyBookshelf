package com.kunfei.bookshelf.widget.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.bar.ImmersionBar;
import com.kunfei.bookshelf.view.activity.ReadBookActivity;
import com.kunfei.bookshelf.widget.page.animation.CoverPageAnim;
import com.kunfei.bookshelf.widget.page.animation.HorizonPageAnim;
import com.kunfei.bookshelf.widget.page.animation.NonePageAnim;
import com.kunfei.bookshelf.widget.page.animation.PageAnimation;
import com.kunfei.bookshelf.widget.page.animation.ScrollPageAnim;
import com.kunfei.bookshelf.widget.page.animation.SimulationPageAnim;
import com.kunfei.bookshelf.widget.page.animation.SlidePageAnim;

import java.util.Objects;

import static com.kunfei.bookshelf.utils.ScreenUtils.getDisplayMetrics;


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
        public void resetScroll() {
            mPageLoader.resetPageOffset();
        }

        @Override
        public boolean hasPrev() {
            return PageView.this.hasPrevPage();
        }

        @Override
        public boolean hasNext(int pageOnCur) {
            return PageView.this.hasNextPage(pageOnCur);
        }

        @Override
        public void drawContent(Canvas canvas, float offset) {
            PageView.this.drawContent(canvas, offset);
        }

        @Override
        public void drawBackground(Canvas canvas) {
            PageView.this.drawBackground(canvas);
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
    void setPageMode(PageAnimation.Mode pageMode, int marginTop, int marginBottom) {
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

    public void autoPrevPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            ((ScrollPageAnim) mPageAnim).startAnim(PageAnimation.Direction.PRE);
        } else {
            startHorizonPageAnim(PageAnimation.Direction.PRE);
        }
    }

    public void autoNextPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            ((ScrollPageAnim) mPageAnim).startAnim(PageAnimation.Direction.NEXT);
        } else {
            startHorizonPageAnim(PageAnimation.Direction.NEXT);
        }
    }

    private void startHorizonPageAnim(PageAnimation.Direction direction) {
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
            boolean hasNext = hasNextPage(0);

            mPageAnim.setDirection(direction);
            if (!hasNext) {
                ((HorizonPageAnim) mPageAnim).setNoNext(true);
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
            boolean hashPrev = hasPrevPage();
            if (!hashPrev) {
                ((HorizonPageAnim) mPageAnim).setNoNext(true);
                return;
            }
        }
        ((HorizonPageAnim) mPageAnim).setNoNext(false);
        ((HorizonPageAnim) mPageAnim).setCancel(false);
        mPageAnim.startAnim();
    }

    public void drawPage(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            Bitmap content = (mPageAnim instanceof ScrollPageAnim) ? getBgBitmap(pageOnCur) : getContentBitmap(pageOnCur);
            mPageLoader.drawPage(getBgBitmap(pageOnCur), content, pageOnCur);
            if (mPageAnim instanceof SimulationPageAnim) {
                ((SimulationPageAnim) mPageAnim).onPageDrawn(pageOnCur);
            }
        }
    }

    public void drawBackground(Canvas canvas) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawBackground(canvas);
        }
    }

    public void drawBackground(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(pageOnCur), null, pageOnCur);
        }
    }

    public void drawContent(Canvas canvas, float offset) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawContent(canvas, offset);
        }
    }

    public void drawContent(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(null, getContentBitmap(pageOnCur), pageOnCur);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPageAnim instanceof ScrollPageAnim)
            super.onDraw(canvas);
        //绘制动画
        if (mPageAnim != null) {
            mPageAnim.draw(canvas);
        }
    }

    @Override
    public void computeScroll() {
        //进行滑动
        if (mPageAnim != null) {
            mPageAnim.scrollAnim();
            if (mPageAnim.isChangePage() && !mPageAnim.getScroller().computeScrollOffset()) {
                mPageAnim.changePageEnd();
                if (mPageAnim.getDirection() != PageAnimation.Direction.NONE) {
                    mPageLoader.pagingEnd(mPageAnim.getDirection());
                    mPageAnim.setDirection(PageAnimation.Direction.NONE);
                }
            }
        }
        super.computeScroll();
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
                if (event.getEdgeFlags() != 0 || event.getRawY() < ScreenUtils.dpToPx(5) || event.getRawY() > getDisplayMetrics().heightPixels - ScreenUtils.dpToPx(5)) {
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
                        mCenterRect = new RectF(mViewWidth / 3f, mViewHeight / 3f,
                                mViewWidth * 2f / 3, mViewHeight * 2f / 3);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (mTouchListener != null) {
                            mTouchListener.center();
                        }
                        return true;
                    }

                    if (!readBookControl.getCanClickTurn() || isRunning()) {
                        return true;
                    }

                    if (mPageAnim instanceof ScrollPageAnim && MApplication.getConfigPreferences().getBoolean("disableScrollClickTurn", false)) {
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
        if (mPageLoader.hasPrev()) {
            return true;
        } else {
            showSnackBar("没有上一页");
            return false;
        }
    }

    /**
     * 判断是否下一页存在
     */
    private boolean hasNextPage(int pageOnCur) {
        if (mPageLoader.hasNext(pageOnCur)) {
            return true;
        } else {
            showSnackBar("没有下一页");
            return false;
        }
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
    public PageLoader getPageLoader(ReadBookActivity activity, BookShelfBean bookShelfBean) {
        this.activity = activity;
        this.statusBarHeight = ImmersionBar.getStatusBarHeight(activity);
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader;
        }
        // 根据书籍类型，获取具体的加载器
        if (!Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG)) {
            mPageLoader = new PageLoaderNet(this, bookShelfBean);
        } else {
            String fileSuffix = FileHelp.getFileSuffix(bookShelfBean.getNoteUrl());
            if (fileSuffix.equalsIgnoreCase(FileHelp.SUFFIX_EPUB)) {
                mPageLoader = new PageLoaderEpub(this, bookShelfBean);
            } else {
                mPageLoader = new PageLoaderText(this, bookShelfBean);
            }
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.prepareDisplay(mViewWidth, mViewHeight);
        }

        return mPageLoader;
    }

    public void showSnackBar(String msg) {
        activity.showSnackBar(this, msg);
    }

    public interface TouchListener {
        boolean onTouch();

        void center();
    }
}
