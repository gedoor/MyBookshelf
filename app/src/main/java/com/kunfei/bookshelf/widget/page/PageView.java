package com.kunfei.bookshelf.widget.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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

    private ReadBookActivity activity;

    private int mViewWidth = 0; // 当前View的宽
    private int mViewHeight = 0; // 当前View的高
    private int statusBarHeight = 0; //状态栏高度

    private boolean actionFromEdge = false;
    // 初始化参数
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
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

        @Override
        public void changePage(PageAnimation.Direction direction) {
            mPageLoader.pagingEnd(direction);
        }

        @Override
        public void clickCenter() {
            if (mTouchListener != null) {
                mTouchListener.center();
            }
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
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mViewWidth = width;
        mViewHeight = height;

        isPrepare = true;

        if (mPageLoader != null) {
            mPageLoader.prepareDisplay(width, height);
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

    public Bitmap getBgBitmap(int pageOnCur) {
        if (mPageAnim == null) return null;
        return mPageAnim.getBgBitmap(pageOnCur);
    }

    public void autoPrevPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            ((ScrollPageAnim) mPageAnim).startAnim(PageAnimation.Direction.PREV);
        } else {
            startHorizonPageAnim(PageAnimation.Direction.PREV);
        }
    }

    public void autoNextPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            ((ScrollPageAnim) mPageAnim).startAnim(PageAnimation.Direction.NEXT);
        } else {
            startHorizonPageAnim(PageAnimation.Direction.NEXT);
        }
    }

    private synchronized void startHorizonPageAnim(PageAnimation.Direction direction) {
        if (mTouchListener == null) return;
        //结束动画
        mPageAnim.abortAnim();
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
        } else if (direction == PageAnimation.Direction.PREV) {
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
        } else {
            return;
        }
        ((HorizonPageAnim) mPageAnim).setNoNext(false);
        ((HorizonPageAnim) mPageAnim).setCancel(false);
        mPageAnim.startAnim();
    }

    public void drawPage(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(pageOnCur), pageOnCur);
        }
        invalidate();
    }

    /**
     * 绘制滚动背景
     */
    public void drawBackground(Canvas canvas) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawBackground(canvas);
        }
    }

    /**
     * 绘制滚动内容
     */
    public void drawContent(Canvas canvas, float offset) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawContent(canvas, offset);
        }
    }

    /**
     * 绘制横翻背景
     */
    public void drawBackground(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(pageOnCur), pageOnCur);
        }
        invalidate();
    }

    /**
     * 绘制横翻内容
     * @param pageOnCur 相对当前页的位置
     */
    public void drawContent(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(pageOnCur), pageOnCur);
        }
        invalidate();
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
        }
        super.computeScroll();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mPageAnim == null) return true;
        if (actionFromEdge) {
            if (event.getAction() == MotionEvent.ACTION_UP)
                actionFromEdge = false;
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getEdgeFlags() != 0 || event.getRawY() < ScreenUtils.dpToPx(5) || event.getRawY() > getDisplayMetrics().heightPixels - ScreenUtils.dpToPx(5)) {
                actionFromEdge = true;
                return true;
            }
            mTouchListener.onTouch();
            mPageAnim.onTouchEvent(event);
        } else {
            mPageAnim.onTouchEvent(event);
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
    public PageLoader getPageLoader(ReadBookActivity activity, BookShelfBean bookShelfBean, PageLoader.Callback callback) {
        this.activity = activity;
        this.statusBarHeight = ImmersionBar.getStatusBarHeight(activity);
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader;
        }
        // 根据书籍类型，获取具体的加载器
        if (!Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG)) {
            mPageLoader = new PageLoaderNet(this, bookShelfBean, callback);
        } else {
            String fileSuffix = FileHelp.getFileSuffix(bookShelfBean.getNoteUrl());
            if (fileSuffix.equalsIgnoreCase(FileHelp.SUFFIX_EPUB)) {
                mPageLoader = new PageLoaderEpub(this, bookShelfBean, callback);
            } else {
                mPageLoader = new PageLoaderText(this, bookShelfBean, callback);
            }
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.prepareDisplay(mViewWidth, mViewHeight);
        }

        return mPageLoader;
    }

    public void autoChangeSource() {
        mPageLoader.setStatus(TxtChapter.Status.CHANGE_SOURCE);
        activity.autoChangeSource();
    }

    public void showSnackBar(String msg) {
        activity.showSnackBar(this, msg);
    }

    public interface TouchListener {
        void onTouch();

        void center();
    }
}
