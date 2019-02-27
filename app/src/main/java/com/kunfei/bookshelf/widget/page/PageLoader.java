package com.kunfei.bookshelf.widget.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.Toast;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.constant.AppConstant;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ChapterContentHelp;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.service.ReadAloudService;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.widget.page.animation.PageAnimation;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 页面加载器
 */

public abstract class PageLoader {
    private static final String TAG = "PageLoader";

    // 默认的显示参数配置
    private static final int CONTENT_MARGIN_HEIGHT = 1;
    private static final int DEFAULT_MARGIN_HEIGHT = 20;
    public static final int DEFAULT_MARGIN_WIDTH = 15;
    private static final int DEFAULT_TIP_SIZE = 12;
    private static final int EXTRA_TITLE_SIZE = 1;
    private static final float MAX_SCROLL_OFFSET = 100;
    private static final int TIP_ALPHA = 180;
    // 监听器
    OnPageChangeListener mPageChangeListener;
    private Context mContext;
    BookShelfBean bookShelfBean;
    // 页面显示类
    PageView mPageView;
    // 上一章的页面列表缓存
    private TxtChapter mPreChapter;
    // 当前章节的页面列表
    private TxtChapter mCurChapter;
    // 下一章的页面列表缓存
    private TxtChapter mNextChapter;

    // 绘制电池的画笔
    private Paint mBatteryPaint;
    // 绘制提示的画笔(章节名称和时间)
    private TextPaint mTipPaint;
    private float pageOffset = 0;
    // 绘制标题的画笔
    TextPaint mTitlePaint;
    // 绘制小说内容的画笔
    TextPaint mTextPaint;
    // 绘制结束的画笔
    private TextPaint mTextEndPaint;
    // 阅读器的配置选项
    ReadBookControl readBookControl = ReadBookControl.getInstance();

    /*****************params**************************/
    // 判断章节列表是否加载完成
    boolean isChapterListPrepare;
    private boolean isClose;
    // 页面的翻页效果模式
    private PageAnimation.Mode mPageMode;
    //书籍绘制区域的宽高
    int mVisibleWidth;
    int mVisibleHeight;
    //应用的宽高
    int mDisplayWidth;
    private int mDisplayHeight;
    //间距
    private int mMarginTop;
    private int mMarginBottom;
    private int mMarginLeft;
    private int mMarginRight;
    int contentMarginHeight;
    private int defaultMarginWidth;
    private int defaultMarginHeight;

    //标题的大小
    private int mTitleSize;
    //字体的大小
    private int mTextSize;
    private int mTextEndSize;
    //行间距
    int mTextInterval;
    //标题的行间距
    int mTitleInterval;
    //段落距离(基于行间距的额外距离)
    int mTextPara;
    int mTitlePara;
    private int textInterval;
    private int textPara;
    private int titleInterval;
    private int titlePara;
    private float tipBottomTop;
    private float tipBottomBot;
    private float tipDistance;
    private float tipMarginLeft;
    private float displayRightEnd;
    private float tipVisibleWidth;

    private boolean hideStatusBar;
    private boolean showTimeBattery;

    //电池的百分比
    private int mBatteryLevel;

    // 当前章
    int mCurChapterPos;
    private int mCurPagePos;
    private int readTextLength; //已读字符数
    private boolean resetReadAloud; //是否重新朗读
    private int readAloudParagraph; //正在朗读章节

    Bitmap cover;
    private int linePos = 0;
    private boolean isLastPage = false;

    CompositeDisposable compositeDisposable;
    //翻页时间
    private long skipPageTime = 0;

    /*****************************init params*******************************/
    PageLoader(PageView pageView, BookShelfBean bookShelfBean) {
        mPageView = pageView;
        this.bookShelfBean = bookShelfBean;
        mContext = pageView.getContext();
        mCurChapterPos = bookShelfBean.getDurChapter();
        mCurPagePos = bookShelfBean.getDurChapterPage();
        compositeDisposable = new CompositeDisposable();
        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        setupTipMargins();
        // 初始化PageView
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);
    }

    private void initData() {
        // 获取配置参数
        hideStatusBar = readBookControl.getHideStatusBar();
        showTimeBattery = hideStatusBar && readBookControl.getShowTimeBattery();
        mPageMode = PageAnimation.Mode.getPageMode(readBookControl.getPageMode());
        // 初始化参数
        mMarginTop = hideStatusBar ?
                ScreenUtils.dpToPx(readBookControl.getPaddingTop() + DEFAULT_MARGIN_HEIGHT)
                : ScreenUtils.dpToPx(readBookControl.getPaddingTop());
        mMarginBottom = ScreenUtils.dpToPx(readBookControl.getPaddingBottom() + DEFAULT_MARGIN_HEIGHT);
        mMarginLeft = ScreenUtils.dpToPx(readBookControl.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(readBookControl.getPaddingRight());
        contentMarginHeight = ScreenUtils.dpToPx(CONTENT_MARGIN_HEIGHT);
        defaultMarginWidth = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH);
        defaultMarginHeight = ScreenUtils.dpToPx(DEFAULT_MARGIN_HEIGHT);

        // 配置文字有关的参数
        setUpTextParams();
    }

    /**
     * 作用：设置与文字相关的参数
     */
    private void setUpTextParams() {
        // 文字大小
        mTextSize = ScreenUtils.spToPx(readBookControl.getTextSize());
        mTitleSize = mTextSize + ScreenUtils.spToPx(EXTRA_TITLE_SIZE);
        mTextEndSize = mTextSize - ScreenUtils.spToPx(EXTRA_TITLE_SIZE);
        // 行间距(大小为字体的一半)
        mTextInterval = (int) (mTextSize * readBookControl.getLineMultiplier() / 2);
        mTitleInterval = (int) (mTitleSize * readBookControl.getLineMultiplier() / 2);
        // 段落间距(大小为字体的高度)
        mTextPara = (int) (mTextSize * readBookControl.getLineMultiplier() * readBookControl.getParagraphSize() / 2);
        mTitlePara = (int) (mTitleSize * readBookControl.getLineMultiplier() * readBookControl.getParagraphSize() / 2);
    }

    private void initPaint() {
        Typeface typeface;
        try {
            if (!TextUtils.isEmpty(readBookControl.getFontPath())) {
                typeface = Typeface.createFromFile(readBookControl.getFontPath());
            } else {
                typeface = Typeface.SANS_SERIF;
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "字体文件未找,到恢复默认字体", Toast.LENGTH_SHORT).show();
            readBookControl.setReadBookFont(null);
            typeface = Typeface.SANS_SERIF;
        }
        // 绘制提示的画笔
        mTipPaint = new TextPaint();
        mTipPaint.setColor(readBookControl.getTextColor());
        mTipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        mTipPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE)); // Tip默认的字体大小
        mTipPaint.setTypeface(Typeface.create(typeface, Typeface.NORMAL));
        mTipPaint.setAntiAlias(true);
        mTipPaint.setSubpixelText(true);

        // 绘制标题的画笔
        mTitlePaint = new TextPaint();
        mTitlePaint.setColor(readBookControl.getTextColor());
        mTitlePaint.setTextSize(mTitleSize);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setAntiAlias(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(readBookControl.getTextColor());
        mTextPaint.setTextSize(mTextSize);
        int bold = readBookControl.getTextBold() ? Typeface.BOLD : Typeface.NORMAL;
        mTextPaint.setTypeface(Typeface.create(typeface, bold));
        mTextPaint.setAntiAlias(true);

        // 绘制结束的画笔
        mTextEndPaint = new TextPaint();
        mTextEndPaint.setColor(readBookControl.getTextColor());
        mTextEndPaint.setTextSize(mTextEndSize);
        mTextEndPaint.setTypeface(Typeface.create(typeface, Typeface.NORMAL));
        mTextEndPaint.setAntiAlias(true);
        mTextEndPaint.setSubpixelText(true);
        mTextEndPaint.setTextAlign(Paint.Align.CENTER);

        // 绘制电池的画笔
        mBatteryPaint = new Paint();
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setDither(true);

        setupTextInterval();
        setupTipMargins();
        // 初始化页面样式
        setPageStyle();
    }

    /**
     * 设置文字相关参数
     */
    public void setTextSize() {
        // 设置文字相关参数
        setUpTextParams();

        // 设置画笔的字体大小
        mTextPaint.setTextSize(mTextSize);
        // 设置标题的字体大小
        mTitlePaint.setTextSize(mTitleSize);
        mTextEndPaint.setTextSize(mTextEndSize);

        // setupTextInterval
        setupTextInterval();
        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    private void setupTextInterval() {
        textInterval = mTextInterval + (int) mTextPaint.getTextSize();
        textPara = mTextPara + (int) mTextPaint.getTextSize();
        titleInterval = mTitleInterval + (int) mTitlePaint.getTextSize();
        titlePara = mTitlePara + (int) mTextPaint.getTextSize();
    }

    private void setupTipMargins() {
        Paint.FontMetrics fontMetrics = mTipPaint.getFontMetrics();
        float tipMarginHeight = (defaultMarginHeight + fontMetrics.top - fontMetrics.bottom) / 2;
        tipBottomTop = tipMarginHeight - fontMetrics.top;
        tipBottomBot = mDisplayHeight - fontMetrics.bottom - tipMarginHeight;
        tipDistance = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH);
        tipMarginLeft = readBookControl.getTipMarginChange() ? mMarginLeft : defaultMarginWidth;
        float tipMarginRight = readBookControl.getTipMarginChange() ? mMarginRight : defaultMarginWidth;
        displayRightEnd = mDisplayWidth - tipMarginRight;
        tipVisibleWidth = mDisplayWidth - tipMarginLeft - tipMarginRight;
    }

    /**
     * 设置页面样式
     */
    public void setPageStyle() {

        mTipPaint.setColor(readBookControl.getTextColor());
        mTitlePaint.setColor(readBookControl.getTextColor());
        mTextPaint.setColor(readBookControl.getTextColor());
        mBatteryPaint.setColor(readBookControl.getTextColor());
        mTextEndPaint.setColor(readBookControl.getTextColor());
        mTipPaint.setAlpha(TIP_ALPHA);
        mBatteryPaint.setAlpha(TIP_ALPHA);
        mTextEndPaint.setAlpha(TIP_ALPHA);

        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    /**
     * 设置翻页动画
     */
    public void setPageMode(PageAnimation.Mode pageMode) {
        mPageMode = pageMode;
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);
        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    /**
     * 设置内容与屏幕的间距 单位为 px
     */
    public void upMargin() {
        mMarginTop = readBookControl.getHideStatusBar()
                ? ScreenUtils.dpToPx(readBookControl.getPaddingTop() + DEFAULT_MARGIN_HEIGHT)
                : ScreenUtils.dpToPx(readBookControl.getPaddingTop());
        mMarginBottom = ScreenUtils.dpToPx(readBookControl.getPaddingBottom() + DEFAULT_MARGIN_HEIGHT);
        mMarginLeft = ScreenUtils.dpToPx(readBookControl.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(readBookControl.getPaddingRight());
        prepareDisplay(mDisplayWidth, mDisplayHeight);
    }

    /**
     * 设置页面切换监听
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;
        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            mPageChangeListener.onCategoryFinish(bookShelfBean.getChapterList());
        }
    }

    /**
     * 刷新界面
     */
    public void refreshUi() {
        initData();
        initPaint();
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);
        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    /**
     * 刷新当前章节
     */
    @SuppressLint("DefaultLocale")
    public void refreshDurChapter() {
        BookshelfHelp.delChapter(BookshelfHelp.getCachePathName(bookShelfBean.getBookInfoBean()), mCurChapterPos, bookShelfBean.getChapter(mCurChapterPos).getDurChapterName());
        skipToChapter(mCurChapterPos, 0);
    }

    /**
     * 换源结束
     */
    public void changeSourceFinish(BookShelfBean book) {
        if (book == null) {
            openChapter(mCurPagePos);
        } else {
            bookShelfBean = book;
            mPageChangeListener.onCategoryFinish(book.getChapterList());
            skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
        }
    }

    /**
     * 跳转到上一章
     */
    public void skipPreChapter() {
        if (mCurChapterPos <= 0) {
            return;
        }

        // 载入上一章。
        mCurChapterPos = mCurChapterPos - 1;
        mCurPagePos = 0;
        mNextChapter = mCurChapter;
        mCurChapter = mPreChapter;
        mPreChapter = null;
        parsePrevChapter();

        chapterChangeCallback();
        openChapter(mCurPagePos);
        pagingEnd(PageAnimation.Direction.NONE);
    }

    /**
     * 跳转到下一章
     */
    public boolean skipNextChapter() {
        if (mCurChapterPos + 1 >= bookShelfBean.getChapterListSize()) {
            return false;
        }

        //载入下一章
        mCurChapterPos = mCurChapterPos + 1;
        mCurPagePos = 0;
        mPreChapter = mCurChapter;
        mCurChapter = mNextChapter;
        mNextChapter = null;
        parseNextChapter();

        chapterChangeCallback();
        openChapter(mCurPagePos);
        pagingEnd(PageAnimation.Direction.NONE);
        return true;
    }

    /**
     * 跳转到指定章节页
     */
    public void skipToChapter(int chapterPos, int pagePos) {
        // 设置参数
        mCurChapterPos = chapterPos;
        mCurPagePos = pagePos;

        mPreChapter = null;
        mCurChapter = null;
        mNextChapter = null;

        openChapter(pagePos);
    }

    /**
     * 跳转到指定的页
     */
    public void skipToPage(int pos) {
        if (!isChapterListPrepare) {
            return;
        }
        openChapter(pos);
    }

    /**
     * 翻到上一页
     */
    public void skipToPrePage() {
        if ((System.currentTimeMillis() - skipPageTime) > 300) {
            mPageView.autoPrevPage();
            skipPageTime = System.currentTimeMillis();
        }
    }

    /**
     * 翻到下一页
     */
    public void skipToNextPage() {
        if ((System.currentTimeMillis() - skipPageTime) > 300) {
            mPageView.autoNextPage();
            skipPageTime = System.currentTimeMillis();
        }
    }

    /**
     * 翻到下一页,无动画
     */
    public boolean noAnimationToNextPage() {
        if (getCurPagePos() < mCurChapter.getPageSize() - 1) {
            skipToPage(getCurPagePos() + 1);
            return true;
        }
        return skipNextChapter();
    }

    /**
     * 更新时间
     */
    public void updateTime() {
        if (readBookControl.getHideStatusBar() && readBookControl.getShowTimeBattery()) {
            if (mPageMode == PageAnimation.Mode.SCROLL) {
                mPageView.drawBackground(0);
            } else {
                upPage();
            }
            mPageView.invalidate();
        }
    }

    /**
     * 更新电量
     */
    public boolean updateBattery(int level) {
        if (mBatteryLevel == level) {
            return true;
        }
        mBatteryLevel = level;
        if (readBookControl.getHideStatusBar() && readBookControl.getShowTimeBattery()) {
            if (mPageMode == PageAnimation.Mode.SCROLL) {
                mPageView.drawBackground(0);
            } else if (mCurChapter != null) {
                upPage();
            }
            mPageView.invalidate();
            return true;
        }
        return true;
    }

    /**
     * 获取当前页的状态
     */
    public TxtChapter.Status getPageStatus() {
        return mCurChapter != null ? mCurChapter.getStatus() : TxtChapter.Status.LOADING;
    }

    /**
     * 获取当前页的页码
     */
    private int getCurPagePos() {
        return mCurPagePos;
    }

    /**
     * 更新状态
     */
    public void setStatus(TxtChapter.Status status) {
        mCurChapter.setStatus(status);
        reSetPage();
        mPageView.invalidate();
    }

    /**
     * 加载错误
     */
    void chapterError(String msg) {
        if (mCurChapter == null) {
            mCurChapter = new TxtChapter(mCurChapterPos);
        }
        mCurChapter.setStatus(TxtChapter.Status.ERROR);
        mCurChapter.setMsg(msg);
        if (mPageMode != PageAnimation.Mode.SCROLL) {
            upPage();
        } else {
            mPageView.drawPage(0);
        }
        mPageView.invalidate();
    }

    /**
     * @return 本页未读内容
     */
    public String getContent() {
        if (mCurChapter == null) return null;
        if (mCurChapter.getPageSize() == 0) return null;
        TxtPage txtPage = mCurChapter.getPage(mCurPagePos);
        StringBuilder s = new StringBuilder();
        int size = txtPage.lines.size();
        int start = mPageMode == PageAnimation.Mode.SCROLL ? Math.min(Math.max(0, linePos), size - 1) : 0;
        for (int i = start; i < size; i++) {
            s.append(txtPage.lines.get(i));
        }
        return s.toString();
    }

    /**
     * @return 本章未读内容
     */
    public String getUnReadContent() {
        if (mCurChapter == null) return null;
        if (mCurChapter.getTxtPageList() == null) return null;
        StringBuilder s = new StringBuilder();
        String content = getContent();
        if (content != null) {
            s.append(content);
        }
        if (mCurChapter.getPageSize() > mCurPagePos + 1) {
            for (int i = mCurPagePos + 1; i < mCurChapter.getPageSize(); i++) {
                s.append(mCurChapter.getPage(i).getContent());
            }
        }
        readTextLength = mCurPagePos > 0 ? mCurChapter.getPageLength(mCurPagePos - 1) : 0;
        if (mPageMode == PageAnimation.Mode.SCROLL) {
            for (int i = 0; i < Math.min(Math.max(0, linePos), mCurChapter.getPage(mCurPagePos).lines.size() - 1); i++) {
                readTextLength += mCurChapter.getPage(mCurPagePos).lines.get(i).length();
            }
        }
        return s.toString();
    }

    public void readAloudStart(int start) {
        start = readTextLength + start;
        int x = mCurChapter.getParagraphIndex(start);
        if (readAloudParagraph != x) {
            readAloudParagraph = x;
            mPageView.drawPage(0);
            mPageView.invalidate();
            mPageView.drawPage(-1);
            mPageView.drawPage(1);
            mPageView.invalidate();
        }
    }

    public void readAloudLength(int readAloudLength) {
        if (mCurChapter == null) return;
        if (mCurChapter.getStatus() != TxtChapter.Status.FINISH) return;
        if (mCurChapter.getPageLength(mCurPagePos) < 0) return;
        if (mPageView.isRunning()) return;
        readAloudLength = readTextLength + readAloudLength;
        if (readAloudLength >= mCurChapter.getPageLength(mCurPagePos)) {
            resetReadAloud = false;
            noAnimationToNextPage();
            mPageView.invalidate();
        }
    }

    /**
     * 刷新章节列表
     */
    public abstract void refreshChapterList();

    /**
     * 获取章节的文本流
     */
    protected abstract String getChapterContent(ChapterListBean chapter) throws Exception;

    /**
     * 章节数据是否存在
     */
    protected abstract boolean noChapterData(ChapterListBean chapter);

    /**
     * 打开当前章节指定页
     */
    void openChapter(int pagePos) {
        mCurPagePos = pagePos;
        if (!mPageView.isPrepare()) {
            return;
        }

        if (mCurChapter == null) {
            mCurChapter = new TxtChapter(mCurChapterPos);
            reSetPage();
        } else if (mCurChapter.getStatus() == TxtChapter.Status.FINISH) {
            reSetPage();
            mPageView.invalidate();
            pagingEnd(PageAnimation.Direction.NONE);
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mCurChapter.setStatus(TxtChapter.Status.LOADING);
            reSetPage();
            mPageView.invalidate();
            return;
        }

        // 如果获取到的章节目录为空
        if (bookShelfBean.getChapterList().isEmpty()) {
            mCurChapter.setStatus(TxtChapter.Status.CATEGORY_EMPTY);
            reSetPage();
            mPageView.invalidate();
            return;
        }

        parseCurChapter();
        resetPageOffset();
    }

    private void reSetPage() {
        if (mPageMode == PageAnimation.Mode.SCROLL) {
            resetPageOffset();
            mPageView.invalidate();
        } else {
            upPage();
        }
    }

    private void upPage() {
        if (mPageMode != PageAnimation.Mode.SCROLL) {
            mPageView.drawPage(0);
            mPageView.invalidate();
            if (mCurPagePos > 0 || mCurChapter.getPosition() > 0) {
                mPageView.drawPage(-1);
                mPageView.invalidate();
            }
            if (mCurPagePos < mCurChapter.getPageSize() - 1 || mCurChapter.getPosition() < bookShelfBean.getChapterList().size() - 1) {
                mPageView.drawPage(1);
                mPageView.invalidate();
            }
        }
    }

    /**
     * 翻页完成
     */
    void pagingEnd(PageAnimation.Direction direction) {
        if (!isChapterListPrepare) {
            return;
        }
        switch (direction) {
            case NEXT:
                if (mCurPagePos < mCurChapter.getPageSize() - 1) {
                    mCurPagePos = mCurPagePos + 1;
                } else if (mCurChapterPos < bookShelfBean.getChapterListSize() - 1) {
                    mCurChapterPos = mCurChapterPos + 1;
                    mCurPagePos = 0;
                    mPreChapter = mCurChapter;
                    mCurChapter = mNextChapter;
                    mNextChapter = null;
                    parseNextChapter();
                    chapterChangeCallback();
                }
                if (mPageMode != PageAnimation.Mode.SCROLL) {
                    mPageView.drawPage(1);
                }
                break;
            case PRE:
                if (mCurPagePos > 0) {
                    mCurPagePos = mCurPagePos - 1;
                } else if (mCurChapterPos > 0) {
                    mCurChapterPos = mCurChapterPos - 1;
                    mCurPagePos = mPreChapter.getPageSize() - 1;
                    mNextChapter = mCurChapter;
                    mCurChapter = mPreChapter;
                    mPreChapter = null;
                    parsePrevChapter();
                    chapterChangeCallback();
                }
                if (mPageMode != PageAnimation.Mode.SCROLL) {
                    mPageView.drawPage(-1);
                }
                break;
        }
        mPageView.setContentDescription(getContent());
        bookShelfBean.setDurChapter(mCurChapterPos);
        bookShelfBean.setDurChapterPage(mCurPagePos);
        mPageChangeListener.onPageChange(mCurChapterPos, getCurPagePos(), resetReadAloud);
        resetReadAloud = true;
    }

    /**
     * 绘制页面
     * pageOnCur: 位于当前页的位置, 小于0上一页, 0 当前页, 大于0下一页
     */
    synchronized void drawPage(Bitmap bgBitmap, Bitmap bitmap, int pageOnCur) {
        TxtChapter txtChapter;
        TxtPage txtPage;
        if (mCurChapter == null) {
            mCurChapter = new TxtChapter(mCurChapterPos);
        }
        if (pageOnCur == 0) { //当前页
            txtChapter = mCurChapter;
            txtPage = mCurChapter.getPage(mCurPagePos);
        } else if (pageOnCur < 0) { //上一页
            if (mCurPagePos > 0) {
                txtChapter = mCurChapter;
                txtPage = mCurChapter.getPage(mCurPagePos - 1);
            } else {
                if (mPreChapter == null) return;
                txtChapter = mPreChapter;
                txtPage = mPreChapter.getPage(mPreChapter.getPageSize() - 1);
            }
        } else { //下一页
            if (mCurPagePos + 1 < mCurChapter.getPageSize()) {
                txtChapter = mCurChapter;
                txtPage = mCurChapter.getPage(mCurPagePos + 1);
            } else {
                if (mNextChapter == null) return;
                txtChapter = mNextChapter;
                txtPage = mNextChapter.getPage(0);
            }
        }
        if (txtChapter != null) {
            if (bgBitmap != null)
                drawBackground(bgBitmap, txtChapter, txtPage);
            if (bitmap != null)
                drawContent(bitmap, txtChapter, txtPage);
        }
    }

    /**
     * 滚动模式绘制背景
     */
    void drawBackground(Canvas canvas) {
        if (mCurChapter == null) {
            mCurChapter = new TxtChapter(mCurChapterPos);
        }
        drawBackground(canvas, mCurChapter, mCurChapter.getPage(mCurPagePos));
    }

    /**
     * 横翻模式绘制背景
     */
    private synchronized void drawBackground(Bitmap bitmap, TxtChapter txtChapter, TxtPage txtPage) {
        if (bitmap == null) return;
        Canvas canvas = new Canvas(bitmap);
        if (mPageMode == PageAnimation.Mode.SCROLL) {
            bitmap.eraseColor(Color.TRANSPARENT);
        } else if (!readBookControl.bgIsColor() && !readBookControl.bgBitmapIsNull()) {
            Rect mDestRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(readBookControl.getBgBitmap(), null, mDestRect, null);
        } else {
            canvas.drawColor(readBookControl.getBgColor());
        }
        drawBackground(canvas, txtChapter, txtPage);
    }

    /**
     * 绘制背景
     */
    @SuppressLint("DefaultLocale")
    private synchronized void drawBackground(Canvas canvas, TxtChapter txtChapter, TxtPage txtPage) {
        if (canvas == null) return;

        if (!bookShelfBean.getChapterList().isEmpty()) {
            String title = isChapterListPrepare ? bookShelfBean.getChapter(txtChapter.getPosition()).getDurChapterName() : "";
            title = ChapterContentHelp.getInstance().replaceContent(bookShelfBean.getBookInfoBean().getName(), bookShelfBean.getTag(), title);
            String page = (txtChapter.getStatus() != TxtChapter.Status.FINISH || txtPage == null) ? ""
                    : String.format("%d/%d", txtPage.position + 1, txtChapter.getPageSize());
            String progress = (txtChapter.getStatus() != TxtChapter.Status.FINISH) ? ""
                    : BookshelfHelp.getReadProgress(mCurChapterPos, bookShelfBean.getChapterListSize(), mCurPagePos, mCurChapter.getPageSize());

            float tipBottom;
            float tipLeft;
            //初始化标题的参数
            //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部的位置
            if (!hideStatusBar) { //显示状态栏
                if (txtChapter.getStatus() != TxtChapter.Status.FINISH) {
                    if (isChapterListPrepare) {
                        //绘制标题
                        title = TextUtils.ellipsize(title, mTipPaint, tipVisibleWidth, TextUtils.TruncateAt.END).toString();
                        canvas.drawText(title, tipMarginLeft, tipBottomBot, mTipPaint);
                    }
                } else {
                    //绘制总进度
                    tipLeft = displayRightEnd - mTipPaint.measureText(progress);
                    canvas.drawText(progress, tipLeft, tipBottomBot, mTipPaint);
                    //绘制页码
                    tipLeft = tipLeft - tipDistance - mTipPaint.measureText(page);
                    canvas.drawText(page, tipLeft, tipBottomBot, mTipPaint);
                    //绘制标题
                    title = TextUtils.ellipsize(title, mTipPaint, tipLeft - tipDistance, TextUtils.TruncateAt.END).toString();
                    canvas.drawText(title, tipMarginLeft, tipBottomBot, mTipPaint);
                }
                if (readBookControl.getShowLine()) {
                    //绘制分隔线
                    tipBottom = mDisplayHeight - defaultMarginHeight;
                    canvas.drawRect(tipMarginLeft, tipBottom, displayRightEnd, tipBottom + 2, mTipPaint);
                }
            } else { //隐藏状态栏
                if (getPageStatus() != TxtChapter.Status.FINISH) {
                    if (isChapterListPrepare) {
                        //绘制标题
                        title = TextUtils.ellipsize(title, mTipPaint, tipVisibleWidth, TextUtils.TruncateAt.END).toString();
                        canvas.drawText(title, tipMarginLeft, tipBottomTop, mTipPaint);
                    }
                } else {
                    //绘制标题
                    float titleTipLength = showTimeBattery ? tipVisibleWidth - mTipPaint.measureText(progress) - tipDistance : tipVisibleWidth;
                    title = TextUtils.ellipsize(title, mTipPaint, titleTipLength, TextUtils.TruncateAt.END).toString();
                    canvas.drawText(title, tipMarginLeft, tipBottomTop, mTipPaint);
                    // 绘制页码
                    canvas.drawText(page, tipMarginLeft, tipBottomBot, mTipPaint);
                    //绘制总进度
                    float progressTipLeft = displayRightEnd - mTipPaint.measureText(progress);
                    float progressTipBottom = showTimeBattery ? tipBottomTop : tipBottomBot;
                    canvas.drawText(progress, progressTipLeft, progressTipBottom, mTipPaint);
                }
                if (readBookControl.getShowLine()) {
                    //绘制分隔线
                    tipBottom = defaultMarginHeight - 2;
                    canvas.drawRect(tipMarginLeft, tipBottom, displayRightEnd, tipBottom + 2, mTipPaint);
                }
            }
        }

        int visibleRight = (int) displayRightEnd;
        if (hideStatusBar && showTimeBattery) {
            //绘制当前时间
            String time = StringUtils.dateConvert(System.currentTimeMillis(), AppConstant.FORMAT_TIME);
            float timeTipLeft = (mDisplayWidth - mTipPaint.measureText(time)) / 2;
            canvas.drawText(time, timeTipLeft, tipBottomBot, mTipPaint);

            //绘制电池
            int outFrameWidth = (int) mTipPaint.measureText("xxx");
            int outFrameHeight = (int) mTipPaint.getTextSize() - ScreenUtils.dpToPx(4);
            int visibleBottom = mDisplayHeight - (defaultMarginHeight - outFrameHeight) / 2;

            int polarHeight = ScreenUtils.dpToPx(4);
            int polarWidth = ScreenUtils.dpToPx(2);
            int border = 1;
            int innerMargin = 1;

            //电极的制作
            int polarLeft = visibleRight - polarWidth;
            int polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2;
            Rect polar = new Rect(polarLeft, polarTop, visibleRight, polarTop + polarHeight);

            mBatteryPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(polar, mBatteryPaint);

            //外框的制作
            int outFrameLeft = polarLeft - outFrameWidth;
            int outFrameTop = visibleBottom - outFrameHeight;
            Rect outFrame = new Rect(outFrameLeft, outFrameTop, polarLeft, visibleBottom);

            mBatteryPaint.setStyle(Paint.Style.STROKE);
            mBatteryPaint.setStrokeWidth(border);
            canvas.drawRect(outFrame, mBatteryPaint);

            //内框的制作
            float innerWidth = (outFrame.width() - innerMargin * 2 - border) * (mBatteryLevel / 100.0f);
            RectF innerFrame = new RectF(outFrameLeft + border + innerMargin, outFrameTop + border + innerMargin,
                    outFrameLeft + border + innerMargin + innerWidth, visibleBottom - border - innerMargin);

            mBatteryPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(innerFrame, mBatteryPaint);

            String batLevel = mBatteryLevel + "%";
            float batTipLeft = outFrameLeft - mTipPaint.measureText(batLevel) - ScreenUtils.dpToPx(4);
            canvas.drawText(batLevel, batTipLeft, tipBottomBot, mTipPaint);
        }
    }

    public void drawCover(Canvas canvas, float top) {
    }

    private int getCoverHeight() {
        return cover == null ? 0 : cover.getHeight() + 20;
    }

    void resetPageOffset() {
        pageOffset = 0;
        linePos = 0;
        isLastPage = false;
    }

    private void switchToPageOffset(int offset) {
        switch (offset) {
            case 1:
                if (mCurPagePos < mCurChapter.getPageSize() - 1) {
                    mCurPagePos = mCurPagePos + 1;
                } else if (mCurChapterPos < bookShelfBean.getChapterListSize() - 1) {
                    mCurChapterPos = mCurChapterPos + 1;
                    mPreChapter = mCurChapter;
                    mCurChapter = mNextChapter;
                    mNextChapter = null;
                    mCurPagePos = 0;
                    if (mCurChapter == null) {
                        mCurChapter = new TxtChapter(mCurChapterPos);
                        parseCurChapter();
                    } else {
                        parseNextChapter();
                    }
                }
                break;
            case -1:
                if (mCurPagePos > 0) {
                    mCurPagePos = mCurPagePos - 1;
                } else if (mCurChapterPos > 0) {
                    mCurChapterPos = mCurChapterPos - 1;
                    mNextChapter = mCurChapter;
                    mCurChapter = mPreChapter;
                    mPreChapter = null;
                    if (mCurChapter == null) {
                        mCurChapter = new TxtChapter(mCurChapterPos);
                        mCurPagePos = 0;
                        parseCurChapter();
                    } else {
                        mCurPagePos = mCurChapter.getPageSize() - 1;
                        parsePrevChapter();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 绘制内容-滚动
     */
    @SuppressWarnings("ConstantConditions")
    void drawContent(Canvas canvas, float offset) {
        if (offset > MAX_SCROLL_OFFSET) {
            offset = MAX_SCROLL_OFFSET;
        } else if (offset < 0 - MAX_SCROLL_OFFSET) {
            offset = -MAX_SCROLL_OFFSET;
        }

        boolean pageChanged = false;

        final float totalHeight = mVisibleHeight + titleInterval;
        if (mCurChapter == null) {
            mCurChapter = new TxtChapter(mCurChapterPos);
        }
        if (mCurChapter == null) return;

        if (!isLastPage || offset < 0) {
            pageOffset += offset;
            isLastPage = false;
        }
        // 首页
        if (pageOffset < 0 && mCurChapterPos == 0 && mCurPagePos == 0) {
            pageOffset = 0;
        }

        float cHeight = getFixedPageHeight(mCurChapter, mCurPagePos);
        cHeight = cHeight > 0 ? cHeight : mVisibleHeight;
        if (offset > 0 && pageOffset > cHeight) {
            while (pageOffset > cHeight) {
                switchToPageOffset(1);
                pageOffset -= cHeight;
                cHeight = getFixedPageHeight(mCurChapter, mCurPagePos);
                cHeight = cHeight > 0 ? cHeight : mVisibleHeight;
                pageChanged = true;
            }
        } else if (offset < 0 && pageOffset < 0) {
            while (pageOffset < 0) {
                switchToPageOffset(-1);
                cHeight = getFixedPageHeight(mCurChapter, mCurPagePos);
                cHeight = cHeight > 0 ? cHeight : mVisibleHeight;
                pageOffset += cHeight;
                pageChanged = true;
            }
        }

        if (pageChanged) {
            chapterChangeCallback();
            pagingEnd(PageAnimation.Direction.NONE);
        }

        float top = contentMarginHeight - mTextPaint.ascent() - pageOffset;

        int chapterPos = mCurChapterPos;
        int pagePos = mCurPagePos;
        boolean isLight;

        if (mCurChapter.getStatus() != TxtChapter.Status.FINISH) {
            String tip = getStatusText(mCurChapter);
            drawErrorMsg(canvas, tip, pageOffset);
            top += mVisibleHeight;
            chapterPos += 1;
            pagePos = 0;
        }
        String str;
        linePos = 0;
        boolean linePosSet = false;
        boolean bookEnd = false;
        float startHeight = -2 * titleInterval;
        if (pageOffset < mTextPaint.getTextSize()) {
            linePos = 0;
            linePosSet = true;
        }
        while (top < totalHeight) {
            TxtChapter chapter = chapterPos == mCurChapterPos ? mCurChapter : mNextChapter;
            if (chapter == null || chapterPos - mCurChapterPos > 1) break;
            if (chapter.getStatus() != TxtChapter.Status.FINISH) {
                String tip = getStatusText(chapter);
                drawErrorMsg(canvas, tip, 0 - top);
                top += mVisibleHeight;
                chapterPos += 1;
                pagePos = 0;
                continue;
            }
            if (chapter.getPageSize() == 0) break;
            TxtPage page = chapter.getPage(pagePos);
            if (page.lines == null) break;
            if (top > totalHeight) break;
            float topi = top;
            int strLength = 0;
            isLight = ReadAloudService.running && readAloudParagraph == 0;
            mTitlePaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());
            for (int i = 0; i < page.titleLines; i++) {
                if (top > totalHeight) {
                    break;
                } else if (top > startHeight) {
                    str = page.lines.get(i);
                    strLength = strLength + str.length();
                    //进行绘制
                    canvas.drawText(str, mDisplayWidth / 2f, top, mTitlePaint);
                }
                top += (i == page.titleLines - 1) ? titlePara : titleInterval;
                if (!linePosSet && chapterPos == mCurChapterPos && top > titlePara) {
                    linePos = i;
                    linePosSet = true;
                }
            }
            if (top > totalHeight) break;
            // 首页画封面
            if (pagePos == 0 && chapterPos == 0) {
                drawCover(canvas, top);
                top += getCoverHeight();
            }
            if (top > totalHeight) break;
            for (int i = page.titleLines, size = page.lines.size(); i < size; i++) {
                str = page.lines.get(i);
                strLength = strLength + str.length();
                int paragraphLength = page.position == 0 ? strLength : chapter.getPageLength(page.position - 1) + strLength;
                isLight = ReadAloudService.running && readAloudParagraph == chapter.getParagraphIndex(paragraphLength);
                mTextPaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());
                if (top > totalHeight) {
                    break;
                } else if (top > startHeight) {
                    Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                    if (needScale(str)) {
                        drawScaledText(canvas, str, width, mTextPaint, top);
                    } else {
                        canvas.drawText(str, mMarginLeft, top, mTextPaint);
                    }
                }
                top += str.endsWith("\n") ? textPara : textInterval;
                if (!linePosSet && chapterPos == mCurChapterPos && top >= textPara) {
                    linePos = i;
                    linePosSet = true;
                }
            }
            if (top > totalHeight) break;
            if (pagePos == chapter.getPageSize() - 1) {
                String sign = "\u23af \u23af";
                if (chapterPos == bookShelfBean.getChapterListSize() - 1) {
                    bookEnd = pagePos == mCurPagePos;
                    str = sign + " 所有章节已读完 " + sign;
                } else {
                    str = sign + " 本章完 " + sign;
                }
                top += textPara;
                canvas.drawText(str, mDisplayWidth / 2f, top, mTextEndPaint);
                top += textPara * 2;
            }
            if (top > totalHeight) break;
            if (chapter.getPageSize() == 1) {
                float pHeight = getFixedPageHeight(chapter, pagePos);
                if (top - topi < pHeight) {
                    top = topi + pHeight;
                }
                if (top > totalHeight) break;
            }
            if (pagePos >= chapter.getPageSize() - 1) {
                chapterPos += 1;
                pagePos = 0;
                top += 60;
            } else {
                pagePos += 1;
            }
            if (bookEnd && top < mVisibleHeight) {
                isLastPage = true;
                break;
            }
        }
    }

    private float getFixedPageHeight(TxtChapter chapter, int pagePos) {
        float height = getPageHeight(chapter, pagePos);
        if (height == 0) {
            return height;
        }
        int lastPageIndex = chapter.getPageSize() - 1;
        if (pagePos == lastPageIndex) {
            height += 60 + textPara * 3;
        }
        if (lastPageIndex <= 0 && height < mVisibleHeight / 2.0f) {
            height = mVisibleHeight / 2.0f;
        }
        return height;
    }

    private float getPageHeight(TxtChapter chapter, int pagePos) {
        float height = 0;
        if (chapter == null || chapter.getStatus() != TxtChapter.Status.FINISH) {
            return height;
        }
        if (pagePos >= 0 && pagePos < chapter.getPageSize()) {
            height = getPageHeight(chapter.getPage(pagePos));
        }
        if (chapter.getPosition() == 0 && pagePos == 0) {
            height += getCoverHeight();
        }
        return height;
    }

    private float getPageHeight(TxtPage page) {
        if (page.lines == null || page.lines.size() == 0)
            return 0;
        float height = 0;
        if (page.titleLines > 0)
            height += titleInterval * (page.titleLines - 1) + titlePara;
        for (int i = page.titleLines; i < page.lines.size(); i++) {
            height += page.lines.get(i).endsWith("\n") ? textPara : textInterval;
        }
        return height;
    }

    private void drawErrorMsg(Canvas canvas, String msg, float offset) {
        Layout tempLayout = new StaticLayout(msg, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
        List<String> linesData = new ArrayList<>();
        for (int i = 0; i < tempLayout.getLineCount(); i++) {
            linesData.add(msg.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
        }
        float pivotY = (mDisplayHeight - textInterval * linesData.size()) / 3f - offset;
        for (String str : linesData) {
            float textWidth = mTextPaint.measureText(str);
            float pivotX = (mDisplayWidth - textWidth) / 2;
            canvas.drawText(str, pivotX, pivotY, mTextPaint);
            pivotY += textInterval;
        }
    }

    private String getStatusText(TxtChapter chapter) {
        String tip = "";
        switch (chapter.getStatus()) {
            case LOADING:
                tip = mContext.getString(R.string.loading);
                break;
            case ERROR:
                tip = String.format("加载失败\n%s", mCurChapter.getMsg());
                break;
            case EMPTY:
                tip = "文章内容为空";
                break;
            case CATEGORY_EMPTY:
                tip = "目录列表为空";
                break;
            case CHANGE_SOURCE:
                tip = "正在换源请等待...";
        }
        return tip;
    }

    /**
     * 绘制内容
     */
    private synchronized void drawContent(Bitmap bitmap, TxtChapter txtChapter, TxtPage txtPage) {
        if (bitmap == null) return;
        Canvas canvas = new Canvas(bitmap);
        if (mPageMode == PageAnimation.Mode.SCROLL) {
            bitmap.eraseColor(Color.TRANSPARENT);
        }

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();

        if (txtChapter.getStatus() != TxtChapter.Status.FINISH) {
            //绘制字体
            String tip = getStatusText(txtChapter);
            drawErrorMsg(canvas, tip, 0);
        } else {
            float top = contentMarginHeight - fontMetrics.ascent;
            if (mPageMode != PageAnimation.Mode.SCROLL) {
                top += readBookControl.getHideStatusBar() ? mMarginTop : mPageView.getStatusBarHeight() + mMarginTop;
            }

            //对标题进行绘制
            String str;
            int strLength = 0;
            boolean isLight;
            for (int i = 0; i < txtPage.titleLines; ++i) {
                str = txtPage.lines.get(i);
                strLength = strLength + str.length();
                isLight = ReadAloudService.running && readAloudParagraph == 0;
                mTitlePaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());

                //进行绘制
                canvas.drawText(str, mDisplayWidth / 2f, top, mTitlePaint);

                //设置尾部间距
                if (i == txtPage.titleLines - 1) {
                    top += titlePara;
                } else {
                    //行间距
                    top += titleInterval;
                }
            }

            if (txtPage.lines == null) {
                return;
            }
            //对内容进行绘制
            for (int i = txtPage.titleLines; i < txtPage.lines.size(); ++i) {
                str = txtPage.lines.get(i);
                strLength = strLength + str.length();
                int paragraphLength = txtPage.position == 0 ? strLength : txtChapter.getPageLength(txtPage.position - 1) + strLength;
                isLight = ReadAloudService.running && readAloudParagraph == txtChapter.getParagraphIndex(paragraphLength);
                mTextPaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());
                Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                if (needScale(str)) {
                    drawScaledText(canvas, str, width, mTextPaint, top);
                } else {
                    canvas.drawText(str, mMarginLeft, top, mTextPaint);
                }

                //设置尾部间距
                if (str.endsWith("\n")) {
                    top += textPara;
                } else {
                    top += textInterval;
                }
            }
        }
    }

    /**
     * 屏幕大小变化处理
     */
    void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginLeft - mMarginRight;
        mVisibleHeight = readBookControl.getHideStatusBar()
                ? mDisplayHeight - mMarginTop - mMarginBottom
                : mDisplayHeight - mMarginTop - mMarginBottom - mPageView.getStatusBarHeight();

        // atb
        setupTipMargins();

        // 重置 PageMode
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);
        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    /**
     * 判断是否存在上一页
     */
    boolean hasPrev() {
        // 以下情况禁止翻页
        if (canNotTurnPage()) {
            return false;
        }
        if (getPageStatus() == TxtChapter.Status.FINISH) {
            // 先查看是否存在上一页
            if (mCurPagePos > 0) {
                return true;
            }
        }
        return mCurChapterPos > 0;
    }

    /**
     * 判断是否下一页存在
     */
    boolean hasNext(int pageOnCur) {
        // 以下情况禁止翻页
        if (canNotTurnPage()) {
            return false;
        }
        if (getPageStatus() == TxtChapter.Status.FINISH) {
            // 先查看是否存在下一页
            if (mCurPagePos + pageOnCur < mCurChapter.getPageSize() - 1) {
                return true;
            }
        }
        return mCurChapterPos + 1 < bookShelfBean.getChapterListSize();
    }

    /**
     * 解析数据
     */
    void parseCurChapter() {
        if (mCurChapter.getStatus() != TxtChapter.Status.FINISH) {
            Single.create((SingleOnSubscribe<TxtChapter>) e -> {
                ChapterProvider chapterProvider = new ChapterProvider(this);
                TxtChapter txtChapter = chapterProvider.dealLoadPageList(bookShelfBean.getChapter(mCurChapterPos), mPageView.isPrepare());
                e.onSuccess(txtChapter);
            })
                    .compose(RxUtils::toSimpleSingle)
                    .subscribe(new SingleObserver<TxtChapter>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(TxtChapter txtChapter) {
                            upTextChapter(txtChapter);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (mPreChapter == null || mPreChapter.getStatus() != TxtChapter.Status.FINISH) {
                                mPreChapter = new TxtChapter(mCurChapterPos);
                                mPreChapter.setStatus(TxtChapter.Status.ERROR);
                                mPreChapter.setMsg(e.getMessage());
                            }
                        }
                    });
        }
        parsePrevChapter();
        parseNextChapter();
    }

    /**
     * 解析上一章数据
     */
    void parsePrevChapter() {
        final int prevChapterPos = mCurChapterPos - 1;
        if (prevChapterPos < 0) {
            mPreChapter = null;
            return;
        }
        if (mPreChapter == null) mPreChapter = new TxtChapter(prevChapterPos);
        if (mPreChapter.getStatus() == TxtChapter.Status.FINISH) {
            return;
        }
        Single.create((SingleOnSubscribe<TxtChapter>) e -> {
            ChapterProvider chapterProvider = new ChapterProvider(this);
            TxtChapter txtChapter = chapterProvider.dealLoadPageList(bookShelfBean.getChapter(prevChapterPos), mPageView.isPrepare());
            e.onSuccess(txtChapter);
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<TxtChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(TxtChapter txtChapter) {
                        upTextChapter(txtChapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPreChapter == null || mPreChapter.getStatus() != TxtChapter.Status.FINISH) {
                            mPreChapter = new TxtChapter(prevChapterPos);
                            mPreChapter.setStatus(TxtChapter.Status.ERROR);
                            mPreChapter.setMsg(e.getMessage());
                        }
                    }
                });
    }

    /**
     * 解析下一章数据
     */
    void parseNextChapter() {
        final int nextChapterPos = mCurChapterPos + 1;
        if (nextChapterPos >= bookShelfBean.getChapterList().size()) {
            mNextChapter = null;
            return;
        }
        if (mNextChapter == null) mNextChapter = new TxtChapter(nextChapterPos);
        if (mNextChapter.getStatus() == TxtChapter.Status.FINISH) {
            return;
        }
        Single.create((SingleOnSubscribe<TxtChapter>) e -> {
            ChapterProvider chapterProvider = new ChapterProvider(this);
            TxtChapter txtChapter = chapterProvider.dealLoadPageList(bookShelfBean.getChapter(nextChapterPos), mPageView.isPrepare());
            e.onSuccess(txtChapter);
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<TxtChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(TxtChapter txtChapter) {
                        upTextChapter(txtChapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mNextChapter == null || mNextChapter.getStatus() != TxtChapter.Status.FINISH) {
                            mPreChapter = new TxtChapter(nextChapterPos);
                            mPreChapter.setStatus(TxtChapter.Status.ERROR);
                            mPreChapter.setMsg(e.getMessage());
                        }
                    }
                });
    }

    private void upTextChapter(TxtChapter txtChapter) {
        if (txtChapter.getPosition() == mCurChapterPos - 1) {
            mPreChapter = txtChapter;
            if (mPageMode == PageAnimation.Mode.SCROLL) {
                mPageView.drawContent(-1);
            } else {
                mPageView.drawPage(-1);
            }
        } else if (txtChapter.getPosition() == mCurChapterPos) {
            mCurChapter = txtChapter;
            reSetPage();
            chapterChangeCallback();
            pagingEnd(PageAnimation.Direction.NONE);
        } else if (txtChapter.getPosition() == mCurChapterPos + 1) {
            mNextChapter = txtChapter;
            if (mPageMode == PageAnimation.Mode.SCROLL) {
                mPageView.drawContent(1);
            } else {
                mPageView.drawPage(1);
            }
        }
        mPageView.invalidate();
    }

    private void drawScaledText(Canvas canvas, String line, float lineWidth, TextPaint paint, float top) {
        float x = mMarginLeft;

        if (isFirstLineOfParagraph(line)) {
            String blanks = StringUtils.halfToFull("  ");
            canvas.drawText(blanks, x, top, paint);
            float bw = StaticLayout.getDesiredWidth(blanks, paint);
            x += bw;
            line = line.substring(2);
        }
        int gapCount = line.length() - 1;
        int i = 0;
        float d = ((mDisplayWidth - (mMarginLeft + mMarginRight)) - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, paint);
            canvas.drawText(c, x, top, paint);
            x += cw + d;
        }

    }

    //判断是不是d'hou
    private boolean isFirstLineOfParagraph(String line) {
        return line.length() > 3 && line.charAt(0) == (char) 12288 && line.charAt(1) == (char) 12288;
    }

    private boolean needScale(String line) {//判断不是空行
        return line != null && line.length() != 0 && line.charAt(line.length() - 1) != '\n';
    }

    private void chapterChangeCallback() {
        if (mPageChangeListener != null) {
            readAloudParagraph = -1;
            mPageChangeListener.onChapterChange(mCurChapterPos);
            mPageChangeListener.onPageCountChange(mCurChapter != null ? mCurChapter.getPageSize() : 0);
        }
    }

    public abstract void updateChapter();

    /**
     * 根据当前状态，决定是否能够翻页
     */
    private boolean canNotTurnPage() {
        return !isChapterListPrepare
                || getPageStatus() == TxtChapter.Status.CHANGE_SOURCE;
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        compositeDisposable.dispose();
        compositeDisposable = null;

        isChapterListPrepare = false;
        isClose = true;

        mPreChapter = null;
        mCurChapter = null;
        mNextChapter = null;
    }

    public boolean isClose() {
        return isClose;
    }

    /*****************************************interface*****************************************/

    public interface OnPageChangeListener {
        /**
         * 作用：章节切换的时候进行回调
         *
         * @param pos:切换章节的序号
         */
        void onChapterChange(int pos);

        /**
         * 作用：章节目录加载完成时候回调
         *
         * @param chapters：返回章节目录
         */
        void onCategoryFinish(List<ChapterListBean> chapters);

        /**
         * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
         *
         * @param count:页面的数量
         */
        void onPageCountChange(int count);

        /**
         * 作用：当页面改变的时候回调
         */
        void onPageChange(int chapterIndex, int pageIndex, boolean resetReadAloud);
    }
}
