package com.kunfei.bookshelf.widget.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.Toast;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
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
import java.util.Collections;
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
    private static final int DEFAULT_MARGIN_HEIGHT = 20;
    public static final int DEFAULT_MARGIN_WIDTH = 15;
    private static final int DEFAULT_TIP_SIZE = 12;
    private static final float MAX_SCROLL_OFFSET = 100;
    private static final int TIP_ALPHA = 180;
    // 监听器
    Callback callback;
    private Context mContext;
    BookShelfBean book;
    // 页面显示类
    PageView mPageView;
    private List<ChapterContainer> chapterContainers = new ArrayList<>();

    // 绘制电池的画笔
    private TextPaint mBatteryPaint;
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
    //缩进
    String indent;
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
    private int tipMarginTop;
    private int tipMarginBottom;
    private int oneSpPx;
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
    PageLoader(PageView pageView, BookShelfBean book, Callback callback) {
        mPageView = pageView;
        this.book = book;
        this.callback = callback;
        for (int i = 0; i < 3; i++) {
            chapterContainers.add(new ChapterContainer());
        }
        mContext = pageView.getContext();
        mCurChapterPos = book.getDurChapter();
        mCurPagePos = book.getDurChapterPage();
        compositeDisposable = new CompositeDisposable();
        oneSpPx = ScreenUtils.spToPx(1);
        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
    }

    private void initData() {
        // 获取配置参数
        hideStatusBar = readBookControl.getHideStatusBar();
        showTimeBattery = hideStatusBar && readBookControl.getShowTimeBattery();
        mPageMode = PageAnimation.Mode.getPageMode(readBookControl.getPageMode());
        // 初始化参数
        indent = StringUtils.repeat(StringUtils.halfToFull(" "), readBookControl.getIndent());
        // 配置文字有关的参数
        setUpTextParams();
    }

    /**
     * 屏幕大小变化处理
     */
    void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 设置边距
        mMarginTop = hideStatusBar ?
                ScreenUtils.dpToPx(readBookControl.getTipPaddingTop() + readBookControl.getPaddingTop() + DEFAULT_MARGIN_HEIGHT)
                : ScreenUtils.dpToPx(readBookControl.getPaddingTop());
        mMarginBottom = ScreenUtils.dpToPx(readBookControl.getTipPaddingBottom() + readBookControl.getPaddingBottom() + DEFAULT_MARGIN_HEIGHT);
        mMarginLeft = ScreenUtils.dpToPx(readBookControl.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(readBookControl.getPaddingRight());
        contentMarginHeight = oneSpPx;
        tipMarginTop = ScreenUtils.dpToPx(readBookControl.getTipPaddingTop() + DEFAULT_MARGIN_HEIGHT);
        tipMarginBottom = ScreenUtils.dpToPx(readBookControl.getTipPaddingBottom() + DEFAULT_MARGIN_HEIGHT);

        Paint.FontMetrics fontMetrics = mTipPaint.getFontMetrics();
        float tipMarginTopHeight = (tipMarginTop + fontMetrics.top - fontMetrics.bottom) / 2;
        float tipMarginBottomHeight = (tipMarginBottom + fontMetrics.top - fontMetrics.bottom) / 2;
        tipBottomTop = tipMarginTopHeight - fontMetrics.top;
        tipBottomBot = mDisplayHeight - fontMetrics.bottom - tipMarginBottomHeight;
        tipDistance = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH);
        tipMarginLeft = ScreenUtils.dpToPx(readBookControl.getTipPaddingLeft());
        float tipMarginRight = ScreenUtils.dpToPx(readBookControl.getTipPaddingRight());
        displayRightEnd = mDisplayWidth - tipMarginRight;
        tipVisibleWidth = mDisplayWidth - tipMarginLeft - tipMarginRight;

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginLeft - mMarginRight;
        mVisibleHeight = readBookControl.getHideStatusBar()
                ? mDisplayHeight - mMarginTop - mMarginBottom
                : mDisplayHeight - mMarginTop - mMarginBottom - mPageView.getStatusBarHeight();

        // 设置翻页模式
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);
        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    /**
     * 设置与文字相关的参数
     */
    private void setUpTextParams() {
        // 文字大小
        mTextSize = ScreenUtils.spToPx(readBookControl.getTextSize());
        mTitleSize = mTextSize + oneSpPx;
        mTextEndSize = mTextSize - oneSpPx;
        // 行间距(大小为字体的一半)
        mTextInterval = (int) (mTextSize * readBookControl.getLineMultiplier() / 2);
        mTitleInterval = (int) (mTitleSize * readBookControl.getLineMultiplier() / 2);
        // 段落间距(大小为字体的高度)
        mTextPara = (int) (mTextSize * readBookControl.getLineMultiplier() * readBookControl.getParagraphSize() / 2);
        mTitlePara = (int) (mTitleSize * readBookControl.getLineMultiplier() * readBookControl.getParagraphSize() / 2);
    }

    /**
     * 初始化画笔
     */
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTitlePaint.setLetterSpacing(readBookControl.getTextLetterSpacing());
        }
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setAntiAlias(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(readBookControl.getTextColor());
        mTextPaint.setTextSize(mTextSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextPaint.setLetterSpacing(readBookControl.getTextLetterSpacing());
        }
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
        mBatteryPaint = new TextPaint();
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setDither(true);
        mBatteryPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE - 3));
        mBatteryPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "number.ttf"));

        setupTextInterval();
        // 初始化页面样式
        initPageStyle();
    }

    /**
     * 设置文字相关参数
     */
    public void setTextSize() {
        // 设置文字相关参数
        setUpTextParams();
        initPaint();
        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    private void setupTextInterval() {
        textInterval = mTextInterval + (int) mTextPaint.getTextSize();
        textPara = mTextPara + (int) mTextPaint.getTextSize();
        titleInterval = mTitleInterval + (int) mTitlePaint.getTextSize();
        titlePara = mTitlePara + (int) mTextPaint.getTextSize();
    }

    /**
     * 设置页面样式
     */
    private void initPageStyle() {
        mTipPaint.setColor(readBookControl.getTextColor());
        mTitlePaint.setColor(readBookControl.getTextColor());
        mTextPaint.setColor(readBookControl.getTextColor());
        mBatteryPaint.setColor(readBookControl.getTextColor());
        mTextEndPaint.setColor(readBookControl.getTextColor());
        mTipPaint.setAlpha(TIP_ALPHA);
        mBatteryPaint.setAlpha(TIP_ALPHA);
        mTextEndPaint.setAlpha(TIP_ALPHA);
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
        prepareDisplay(mDisplayWidth, mDisplayHeight);
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
     * 跳转到上一章
     */
    public void skipPreChapter() {
        if (mCurChapterPos <= 0) {
            return;
        }

        // 载入上一章。
        mCurChapterPos = mCurChapterPos - 1;
        mCurPagePos = 0;
        Collections.swap(chapterContainers, 2, 1);
        Collections.swap(chapterContainers, 1, 0);
        prevChapter().txtChapter = null;
        parsePrevChapter();

        chapterChangeCallback();
        openChapter(mCurPagePos);
        pagingEnd(PageAnimation.Direction.NONE);
    }

    /**
     * 跳转到下一章
     */
    public boolean skipNextChapter() {
        if (mCurChapterPos + 1 >= book.getChapterListSize()) {
            return false;
        }

        //载入下一章
        mCurChapterPos = mCurChapterPos + 1;
        mCurPagePos = 0;
        Collections.swap(chapterContainers, 0, 1);
        Collections.swap(chapterContainers, 1, 2);
        nextChapter().txtChapter = null;
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

        prevChapter().txtChapter = null;
        curChapter().txtChapter = null;
        nextChapter().txtChapter = null;

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
    private void noAnimationToNextPage() {
        if (getCurPagePos() < curChapter().txtChapter.getPageSize() - 1) {
            skipToPage(getCurPagePos() + 1);
            return;
        }
        skipNextChapter();
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
            return false;
        }
        mBatteryLevel = level;
        if (readBookControl.getHideStatusBar() && readBookControl.getShowTimeBattery()) {
            if (mPageMode == PageAnimation.Mode.SCROLL) {
                mPageView.drawBackground(0);
            } else if (curChapter().txtChapter != null) {
                upPage();
            }
            mPageView.invalidate();
            return true;
        }
        return false;
    }

    /**
     * 获取当前页的状态
     */
    public TxtChapter.Status getPageStatus() {
        return curChapter().txtChapter != null ? curChapter().txtChapter.getStatus() : TxtChapter.Status.LOADING;
    }

    /**
     * 获取当前页的页码
     */
    int getCurPagePos() {
        return mCurPagePos;
    }

    /**
     * 更新状态
     */
    public void setStatus(TxtChapter.Status status) {
        curChapter().txtChapter.setStatus(status);
        reSetPage();
        mPageView.invalidate();
    }

    /**
     * 加载错误
     */
    void durDhapterError(String msg) {
        if (curChapter().txtChapter == null) {
            curChapter().txtChapter = new TxtChapter(mCurChapterPos);
        }
        if (curChapter().txtChapter.getStatus() == TxtChapter.Status.FINISH) return;
        curChapter().txtChapter.setStatus(TxtChapter.Status.ERROR);
        curChapter().txtChapter.setMsg(msg);
        if (mPageMode != PageAnimation.Mode.SCROLL) {
            upPage();
        } else {
            mPageView.drawPage(0);
        }
        mPageView.invalidate();
    }

    /**
     * @return 当前章节所有内容
     */
    public String getAllContent() {
        return getContentStartPage(0);
    }

    /**
     * @return 本页未读内容
     */
    public String getContent() {
        if (curChapter().txtChapter == null) return null;
        if (curChapter().txtChapter.getPageSize() == 0) return null;
        TxtPage txtPage = curChapter().txtChapter.getPage(mCurPagePos);
        StringBuilder s = new StringBuilder();
        int size = txtPage.size();
        int start = mPageMode == PageAnimation.Mode.SCROLL ? Math.min(Math.max(0, linePos), size - 1) : 0;
        for (int i = start; i < size; i++) {
            s.append(txtPage.getLine(i));
        }
        return s.toString();
    }

    /**
     * @return 本章未读内容
     */
    public String getUnReadContent() {
        if (curChapter().txtChapter == null) return null;
        if (book.isAudio()) return curChapter().txtChapter.getMsg();
        if (curChapter().txtChapter.getTxtPageList().isEmpty()) return null;
        StringBuilder s = new StringBuilder();
        String content = getContent();
        if (content != null) {
            s.append(content);
        }
        content = getContentStartPage(mCurPagePos + 1);
        if (content != null) {
            s.append(content);
        }
        readTextLength = mCurPagePos > 0 ? curChapter().txtChapter.getPageLength(mCurPagePos - 1) : 0;
        if (mPageMode == PageAnimation.Mode.SCROLL) {
            for (int i = 0; i < Math.min(Math.max(0, linePos), curChapter().txtChapter.getPage(mCurPagePos).size() - 1); i++) {
                readTextLength += curChapter().txtChapter.getPage(mCurPagePos).getLine(i).length();
            }
        }
        return s.toString();
    }

    /**
     * @param page 开始页数
     * @return 从page页开始的的当前章节所有内容
     */
    private String getContentStartPage(int page) {
        if (curChapter().txtChapter == null) return null;
        if (curChapter().txtChapter.getTxtPageList().isEmpty()) return null;
        StringBuilder s = new StringBuilder();
        if (curChapter().txtChapter.getPageSize() > page) {
            for (int i = page; i < curChapter().txtChapter.getPageSize(); i++) {
                s.append(curChapter().txtChapter.getPage(i).getContent());
            }
        }
        return s.toString();
    }

    /**
     * @param start 开始朗读字数
     */
    public void readAloudStart(int start) {
        start = readTextLength + start;
        int x = curChapter().txtChapter.getParagraphIndex(start);
        if (readAloudParagraph != x) {
            readAloudParagraph = x;
            mPageView.drawPage(0);
            mPageView.invalidate();
            mPageView.drawPage(-1);
            mPageView.drawPage(1);
            mPageView.invalidate();
        }
    }

    /**
     * @param readAloudLength 已朗读字数
     */
    public void readAloudLength(int readAloudLength) {
        if (curChapter().txtChapter == null) return;
        if (curChapter().txtChapter.getStatus() != TxtChapter.Status.FINISH) return;
        if (curChapter().txtChapter.getPageLength(mCurPagePos) < 0) return;
        if (mPageView.isRunning()) return;
        readAloudLength = readTextLength + readAloudLength;
        if (readAloudLength >= curChapter().txtChapter.getPageLength(mCurPagePos)) {
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
     * 获取章节的文本
     */
    protected abstract String getChapterContent(BookChapterBean chapter) throws Exception;

    /**
     * 章节数据是否存在
     */
    protected abstract boolean noChapterData(BookChapterBean chapter);

    /**
     * 打开当前章节指定页
     */
    void openChapter(int pagePos) {
        mCurPagePos = pagePos;
        if (!mPageView.isPrepare()) {
            return;
        }

        if (curChapter().txtChapter == null) {
            curChapter().txtChapter = new TxtChapter(mCurChapterPos);
            reSetPage();
        } else if (curChapter().txtChapter.getStatus() == TxtChapter.Status.FINISH) {
            reSetPage();
            mPageView.invalidate();
            pagingEnd(PageAnimation.Direction.NONE);
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            curChapter().txtChapter.setStatus(TxtChapter.Status.LOADING);
            reSetPage();
            mPageView.invalidate();
            return;
        }

        // 如果获取到的章节目录为空
        if (callback.getChapterList().isEmpty()) {
            curChapter().txtChapter.setStatus(TxtChapter.Status.CATEGORY_EMPTY);
            reSetPage();
            mPageView.invalidate();
            return;
        }

        parseCurChapter();
        resetPageOffset();
    }

    /**
     * 重置页面
     */
    private void reSetPage() {
        if (mPageMode == PageAnimation.Mode.SCROLL) {
            resetPageOffset();
            mPageView.invalidate();
        } else {
            upPage();
        }
    }

    /**
     * 更新页面
     */
    private void upPage() {
        if (mPageMode != PageAnimation.Mode.SCROLL) {
            mPageView.drawPage(0);
            if (mCurPagePos > 0 || curChapter().txtChapter.getPosition() > 0) {
                mPageView.drawPage(-1);
            }
            if (mCurPagePos < curChapter().txtChapter.getPageSize() - 1 || curChapter().txtChapter.getPosition() < callback.getChapterList().size() - 1) {
                mPageView.drawPage(1);
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
                if (mCurPagePos < curChapter().txtChapter.getPageSize() - 1) {
                    mCurPagePos = mCurPagePos + 1;
                } else if (mCurChapterPos < book.getChapterListSize() - 1) {
                    mCurChapterPos = mCurChapterPos + 1;
                    mCurPagePos = 0;
                    Collections.swap(chapterContainers, 0, 1);
                    Collections.swap(chapterContainers, 1, 2);
                    nextChapter().txtChapter = null;
                    parseNextChapter();
                    chapterChangeCallback();
                }
                if (mPageMode != PageAnimation.Mode.SCROLL) {
                    mPageView.drawPage(1);
                }
                break;
            case PREV:
                if (mCurPagePos > 0) {
                    mCurPagePos = mCurPagePos - 1;
                } else if (mCurChapterPos > 0) {
                    mCurChapterPos = mCurChapterPos - 1;
                    mCurPagePos = prevChapter().txtChapter.getPageSize() - 1;
                    Collections.swap(chapterContainers, 2, 1);
                    Collections.swap(chapterContainers, 1, 0);
                    prevChapter().txtChapter = null;
                    parsePrevChapter();
                    chapterChangeCallback();
                }
                if (mPageMode != PageAnimation.Mode.SCROLL) {
                    mPageView.drawPage(-1);
                }
                break;
        }
        mPageView.setContentDescription(getContent());
        book.setDurChapter(mCurChapterPos);
        book.setDurChapterPage(mCurPagePos);
        callback.onPageChange(mCurChapterPos, getCurPagePos(), resetReadAloud);
        resetReadAloud = true;
    }

    /**
     * 绘制页面
     * pageOnCur: 位于当前页的位置, 小于0上一页, 0 当前页, 大于0下一页
     */
    synchronized void drawPage(Bitmap bitmap, int pageOnCur) {
        TxtChapter txtChapter;
        TxtPage txtPage = null;
        if (curChapter().txtChapter == null) {
            curChapter().txtChapter = new TxtChapter(mCurChapterPos);
        }
        if (pageOnCur == 0) { //当前页
            txtChapter = curChapter().txtChapter;
            txtPage = txtChapter.getPage(mCurPagePos);
        } else if (pageOnCur < 0) { //上一页
            if (mCurPagePos > 0) {
                txtChapter = curChapter().txtChapter;
                txtPage = txtChapter.getPage(mCurPagePos - 1);
            } else {
                if (prevChapter().txtChapter == null) {
                    txtChapter = new TxtChapter(mCurChapterPos + 1);
                    txtChapter.setStatus(TxtChapter.Status.ERROR);
                    txtChapter.setMsg("未加载完成");
                } else {
                    txtChapter = prevChapter().txtChapter;
                    txtPage = txtChapter.getPage(txtChapter.getPageSize() - 1);
                }
            }
        } else { //下一页
            if (mCurPagePos + 1 < curChapter().txtChapter.getPageSize()) {
                txtChapter = curChapter().txtChapter;
                txtPage = txtChapter.getPage(mCurPagePos + 1);
            } else {
                if (mCurChapterPos + 1 >= callback.getChapterList().size()) {
                    txtChapter = new TxtChapter(mCurChapterPos + 1);
                    txtChapter.setStatus(TxtChapter.Status.ERROR);
                    txtChapter.setMsg("没有下一页");
                } else if (nextChapter().txtChapter == null) {
                    txtChapter = new TxtChapter(mCurChapterPos + 1);
                    txtChapter.setStatus(TxtChapter.Status.ERROR);
                    txtChapter.setMsg("未加载完成");
                } else {
                    txtChapter = nextChapter().txtChapter;
                    txtPage = txtChapter.getPage(0);
                }
            }
        }
        if (bitmap != null)
            drawBackground(bitmap, txtChapter, txtPage);
        drawContent(bitmap, txtChapter, txtPage);
    }

    /**
     * 滚动模式绘制背景
     */
    void drawBackground(Canvas canvas) {
        if (curChapter().txtChapter == null) {
            curChapter().txtChapter = new TxtChapter(mCurChapterPos);
        }
        drawBackground(canvas, curChapter().txtChapter, curChapter().txtChapter.getPage(mCurPagePos));
    }

    /**
     * 横翻模式绘制背景
     */
    private synchronized void drawBackground(Bitmap bitmap, TxtChapter txtChapter, TxtPage txtPage) {
        if (bitmap == null) return;
        Canvas canvas = new Canvas(bitmap);
        if (!readBookControl.bgIsColor() && !readBookControl.bgBitmapIsNull()) {
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
    private synchronized void drawBackground(final Canvas canvas, TxtChapter txtChapter, TxtPage txtPage) {
        if (canvas == null) return;
        if (!callback.getChapterList().isEmpty()) {
            String title = callback.getChapterList().size() > txtChapter.getPosition() ? callback.getChapterList().get(txtChapter.getPosition()).getDurChapterName() : "";
            title = ChapterContentHelp.getInstance().replaceContent(book.getBookInfoBean().getName(), book.getTag(), title, book.getReplaceEnable());
            String page = (txtChapter.getStatus() != TxtChapter.Status.FINISH || txtPage == null) ? ""
                    : String.format("%d/%d", txtPage.getPosition() + 1, txtChapter.getPageSize());
            String progress = (txtChapter.getStatus() != TxtChapter.Status.FINISH) ? ""
                    : BookshelfHelp.getReadProgress(mCurChapterPos, book.getChapterListSize(), mCurPagePos, curChapter().txtChapter.getPageSize());

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
                    tipBottom = mDisplayHeight - tipMarginBottom;
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
                    tipBottom = tipMarginTop - 2;
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
            int polarHeight = ScreenUtils.dpToPx(4);
            int polarWidth = ScreenUtils.dpToPx(2);
            int border = 2;
            int outFrameWidth = (int) mBatteryPaint.measureText("0000") + polarWidth;
            int outFrameHeight = (int) mBatteryPaint.getTextSize() + oneSpPx;
            int visibleBottom = mDisplayHeight - (tipMarginBottom - outFrameHeight) / 2;

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

            //绘制电量
            mBatteryPaint.setStyle(Paint.Style.FILL);
            Paint.FontMetrics fontMetrics = mBatteryPaint.getFontMetrics();
            String batteryLevel = String.valueOf(mBatteryLevel);
            float batTextLeft = outFrameLeft + (outFrameWidth - mBatteryPaint.measureText(batteryLevel)) / 2;
            float batTextBaseLine = visibleBottom - outFrameHeight / 2f - fontMetrics.top / 2 - fontMetrics.bottom / 2;
            canvas.drawText(batteryLevel, batTextLeft, batTextBaseLine, mBatteryPaint);
        }
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

        Paint.FontMetrics fontMetricsForTitle = mTitlePaint.getFontMetrics();
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
            int ppp = 0;//pzl,文字位置
            //对标题进行绘制
            String str;
            int strLength = 0;
            boolean isLight;
            for (int i = 0; i < txtPage.getTitleLines(); ++i) {
                str = txtPage.getLine(i);
                strLength = strLength + str.length();
                isLight = ReadAloudService.running && readAloudParagraph == 0;
                mTitlePaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());

                //进行绘制
                canvas.drawText(str, mDisplayWidth / 2f, top, mTitlePaint);

                //pzl
                float leftposition = mDisplayWidth / 2;
                float rightposition = 0;
                float bottomposition = top + mTitlePaint.getFontMetrics().descent;
                float TextHeight = Math.abs(fontMetricsForTitle.ascent) + Math.abs(fontMetricsForTitle.descent);

                if (txtPage.getTxtLists() != null) {
                    for (TxtChar c : txtPage.getTxtLists().get(i).getCharsData()) {
                        rightposition = leftposition + c.getCharWidth();
                        Point tlp = new Point();
                        c.setTopLeftPosition(tlp);
                        tlp.x = (int) leftposition;
                        tlp.y = (int) (bottomposition - TextHeight);

                        Point blp = new Point();
                        c.setBottomLeftPosition(blp);
                        blp.x = (int) leftposition;
                        blp.y = (int) bottomposition;

                        Point trp = new Point();
                        c.setTopRightPosition(trp);
                        trp.x = (int) rightposition;
                        trp.y = (int) (bottomposition - TextHeight);

                        Point brp = new Point();
                        c.setBottomRightPosition(brp);
                        brp.x = (int) rightposition;
                        brp.y = (int) bottomposition;
                        ppp++;
                        c.setIndex(ppp);

                        leftposition = rightposition;
                    }
                }

                //设置尾部间距
                if (i == txtPage.getTitleLines() - 1) {
                    top += titlePara;
                } else {
                    //行间距
                    top += titleInterval;
                }
            }

            if (txtPage.getLines().isEmpty()) {
                return;
            }
            //对内容进行绘制
            for (int i = txtPage.getTitleLines(); i < txtPage.size(); ++i) {
                str = txtPage.getLine(i);
                strLength = strLength + str.length();
                int paragraphLength = txtPage.getPosition() == 0 ? strLength : txtChapter.getPageLength(txtPage.getPosition() - 1) + strLength;
                isLight = ReadAloudService.running && readAloudParagraph == txtChapter.getParagraphIndex(paragraphLength);
                mTextPaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());
                Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                if (needScale(str)) {
                    drawScaledText(canvas, str, width, mTextPaint, top, i, txtPage.getTxtLists());
                } else {
                    canvas.drawText(str, mMarginLeft, top, mTextPaint);
                }

                //记录文字位置 --开始 pzl
                float leftposition = mMarginLeft;
                if (isFirstLineOfParagraph(str)) {
                    String blanks = StringUtils.halfToFull("  ");
                    //canvas.drawText(blanks, x, top, mTextPaint);
                    float bw = StaticLayout.getDesiredWidth(blanks, mTextPaint);
                    leftposition += bw;
                }
                float rightposition = 0;
                float bottomposition = top + mTextPaint.getFontMetrics().descent;
                float textHeight = Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent);

                if (txtPage.getTxtLists() != null) {
                    for (TxtChar c : txtPage.getTxtLists().get(i).getCharsData()) {
                        rightposition = leftposition + c.getCharWidth();
                        Point tlp = new Point();
                        c.setTopLeftPosition(tlp);
                        tlp.x = (int) leftposition;
                        tlp.y = (int) (bottomposition - textHeight);

                        Point blp = new Point();
                        c.setBottomLeftPosition(blp);
                        blp.x = (int) leftposition;
                        blp.y = (int) bottomposition;

                        Point trp = new Point();
                        c.setTopRightPosition(trp);
                        trp.x = (int) rightposition;
                        trp.y = (int) (bottomposition - textHeight);

                        Point brp = new Point();
                        c.setBottomRightPosition(brp);
                        brp.x = (int) rightposition;
                        brp.y = (int) bottomposition;

                        leftposition = rightposition;

                        ppp++;
                        c.setIndex(ppp);
                    }
                }
                //记录文字位置 --结束 pzl

                //设置尾部间距
                if (str.endsWith("\n")) {
                    top += textPara;
                } else {
                    top += textInterval;
                }
            }
        }
    }

    public void drawCover(Canvas canvas, float top) {
    }

    private int getCoverHeight() {
        return cover == null ? 0 : cover.getHeight() + 20;
    }

    /**
     * 绘制内容-滚动
     */
    void drawContent(final Canvas canvas, float offset) {

        if (offset > MAX_SCROLL_OFFSET) {
            offset = MAX_SCROLL_OFFSET;
        } else if (offset < 0 - MAX_SCROLL_OFFSET) {
            offset = -MAX_SCROLL_OFFSET;
        }

        boolean pageChanged = false;
        Paint.FontMetrics fontMetricsForTitle = mTitlePaint.getFontMetrics();
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();

        final float totalHeight = mVisibleHeight + titleInterval;
        if (curChapter().txtChapter == null) {
            curChapter().txtChapter = new TxtChapter(mCurChapterPos);
        }

        if (!isLastPage || offset < 0) {
            pageOffset += offset;
            isLastPage = false;
        }
        // 首页
        if (pageOffset < 0 && mCurChapterPos == 0 && mCurPagePos == 0) {
            pageOffset = 0;
        }

        float cHeight = getFixedPageHeight(curChapter().txtChapter, mCurPagePos);
        cHeight = cHeight > 0 ? cHeight : mVisibleHeight;
        if (offset > 0 && pageOffset > cHeight) {
            while (pageOffset > cHeight) {
                switchToPageOffset(1);
                pageOffset -= cHeight;
                cHeight = getFixedPageHeight(curChapter().txtChapter, mCurPagePos);
                cHeight = cHeight > 0 ? cHeight : mVisibleHeight;
                pageChanged = true;
            }
        } else if (offset < 0 && pageOffset < 0) {
            while (pageOffset < 0) {
                switchToPageOffset(-1);
                cHeight = getFixedPageHeight(curChapter().txtChapter, mCurPagePos);
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
        int ppp = 0;//pzl,文字位置

        if (curChapter().txtChapter.getStatus() != TxtChapter.Status.FINISH) {
            String tip = getStatusText(curChapter().txtChapter);
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
            TxtChapter chapter = chapterPos == mCurChapterPos ? curChapter().txtChapter : nextChapter().txtChapter;
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
            if (page.getLines().isEmpty()) break;
            if (top > totalHeight) break;
            float topi = top;
            int strLength = 0;
            isLight = ReadAloudService.running && readAloudParagraph == 0;
            mTitlePaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());
            for (int i = 0; i < page.getTitleLines(); i++) {
                if (top > totalHeight) {
                    break;
                } else if (top > startHeight) {
                    str = page.getLine(i);
                    strLength = strLength + str.length();
                    //进行绘制
                    canvas.drawText(str, mDisplayWidth / 2f, top, mTitlePaint);
                    //pzl
                    float leftposition = mDisplayWidth / 2f;
                    float rightPosition = 0;
                    float bottomPosition = top + mTitlePaint.getFontMetrics().descent;
                    float TextHeight = Math.abs(fontMetricsForTitle.ascent) + Math.abs(fontMetricsForTitle.descent);

                    if (page.getTxtLists() != null) {
                        for (TxtChar c : page.getTxtLists().get(i).getCharsData()) {
                            rightPosition = leftposition + c.getCharWidth();
                            Point tlp = new Point();
                            c.setTopLeftPosition(tlp);
                            tlp.x = (int) leftposition;
                            tlp.y = (int) (bottomPosition - TextHeight);

                            Point blp = new Point();
                            c.setBottomLeftPosition(blp);
                            blp.x = (int) leftposition;
                            blp.y = (int) bottomPosition;

                            Point trp = new Point();
                            c.setTopRightPosition(trp);
                            trp.x = (int) rightPosition;
                            trp.y = (int) (bottomPosition - TextHeight);

                            Point brp = new Point();
                            c.setBottomRightPosition(brp);
                            brp.x = (int) rightPosition;
                            brp.y = (int) bottomPosition;
                            ppp++;
                            c.setIndex(ppp);

                            leftposition = rightPosition;
                        }
                    }
                    //pzl
                }
                top += (i == page.getTitleLines() - 1) ? titlePara : titleInterval;
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
            for (int i = page.getTitleLines(); i < page.size(); i++) {
                str = page.getLine(i);
                strLength = strLength + str.length();
                int paragraphLength = page.getPosition() == 0 ? strLength : chapter.getPageLength(page.getPosition() - 1) + strLength;
                isLight = ReadAloudService.running && readAloudParagraph == chapter.getParagraphIndex(paragraphLength);
                mTextPaint.setColor(isLight ? ThemeStore.accentColor(mContext) : readBookControl.getTextColor());
                if (top > totalHeight) {
                    break;
                } else if (top > startHeight) {
                    Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                    if (needScale(str)) {
                        drawScaledText(canvas, str, width, mTextPaint, top, i, page.getTxtLists());
                    } else {
                        canvas.drawText(str, mMarginLeft, top, mTextPaint);
                    }
                    //记录文字位置 --开始 pzl

                    float leftposition = mMarginLeft;

                    if (isFirstLineOfParagraph(str)) {
                        String blanks = StringUtils.halfToFull("  ");
                        //canvas.drawText(blanks, x, top, mTextPaint);
                        float bw = StaticLayout.getDesiredWidth(blanks, mTextPaint);
                        leftposition += bw;
                    }

                    float rightposition = 0;
                    float bottomposition = top + mTextPaint.getFontMetrics().descent;
                    float textHeight = Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent);

                    if (page.getTxtLists() != null) {
                        for (TxtChar c : page.getTxtLists().get(i).getCharsData()) {

                            rightposition = leftposition + c.getCharWidth();
                            Point tlp = new Point();
                            c.setTopLeftPosition(tlp);
                            tlp.x = (int) leftposition;
                            tlp.y = (int) (bottomposition - textHeight);

                            Point blp = new Point();
                            c.setBottomLeftPosition(blp);
                            blp.x = (int) leftposition;
                            blp.y = (int) bottomposition;

                            Point trp = new Point();
                            c.setTopRightPosition(trp);
                            trp.x = (int) rightposition;
                            trp.y = (int) (bottomposition - textHeight);

                            Point brp = new Point();
                            c.setBottomRightPosition(brp);
                            brp.x = (int) rightposition;
                            brp.y = (int) bottomposition;

                            leftposition = rightposition;

                            ppp++;
                            c.setIndex(ppp);
                        }
                    }
                    //记录文字位置 --结束 pzl
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
                if (chapterPos == book.getChapterListSize() - 1) {
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

    void resetPageOffset() {
        pageOffset = 0;
        linePos = 0;
        isLastPage = false;
    }

    private void switchToPageOffset(int offset) {
        switch (offset) {
            case 1:
                if (mCurPagePos < curChapter().txtChapter.getPageSize() - 1) {
                    mCurPagePos = mCurPagePos + 1;
                } else if (mCurChapterPos < book.getChapterListSize() - 1) {
                    mCurChapterPos = mCurChapterPos + 1;
                    Collections.swap(chapterContainers, 0, 1);
                    Collections.swap(chapterContainers, 1, 2);
                    nextChapter().txtChapter = null;
                    mCurPagePos = 0;
                    if (curChapter().txtChapter == null) {
                        curChapter().txtChapter = new TxtChapter(mCurChapterPos);
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
                    Collections.swap(chapterContainers, 2, 1);
                    Collections.swap(chapterContainers, 1, 0);
                    prevChapter().txtChapter = null;
                    if (curChapter().txtChapter == null) {
                        curChapter().txtChapter = new TxtChapter(mCurChapterPos);
                        mCurPagePos = 0;
                        parseCurChapter();
                    } else {
                        mCurPagePos = curChapter().txtChapter.getPageSize() - 1;
                        parsePrevChapter();
                    }
                }
                break;
            default:
                break;
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
        if (page.getLines().isEmpty())
            return 0;
        float height = 0;
        if (page.getTitleLines() > 0)
            height += titleInterval * (page.getTitleLines() - 1) + titlePara;
        for (int i = page.getTitleLines(); i < page.size(); i++) {
            height += page.getLine(i).endsWith("\n") ? textPara : textInterval;
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

    /**
     * 获取状态文本
     */
    private String getStatusText(TxtChapter chapter) {
        String tip = "";
        switch (chapter.getStatus()) {
            case LOADING:
                tip = mContext.getString(R.string.loading);
                break;
            case ERROR:
                tip = mContext.getString(R.string.load_error_msg, curChapter().txtChapter.getMsg());
                break;
            case EMPTY:
                tip = mContext.getString(R.string.content_empty);
                break;
            case CATEGORY_EMPTY:
                tip = mContext.getString(R.string.chapter_list_empty);
                break;
            case CHANGE_SOURCE:
                tip = mContext.getString(R.string.on_change_source);
        }
        return tip;
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
            if (mCurPagePos + pageOnCur < curChapter().txtChapter.getPageSize() - 1) {
                return true;
            }
        }
        return mCurChapterPos + 1 < book.getChapterListSize();
    }

    /**
     * 解析当前页数据
     */
    void parseCurChapter() {
        if (curChapter().txtChapter.getStatus() != TxtChapter.Status.FINISH) {
            Single.create((SingleOnSubscribe<TxtChapter>) e -> {
                ChapterProvider chapterProvider = new ChapterProvider(this);
                TxtChapter txtChapter = chapterProvider.dealLoadPageList(callback.getChapterList().get(mCurChapterPos), mPageView.isPrepare());
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
                            if (curChapter().txtChapter == null || curChapter().txtChapter.getStatus() != TxtChapter.Status.FINISH) {
                                curChapter().txtChapter = new TxtChapter(mCurChapterPos);
                                curChapter().txtChapter.setStatus(TxtChapter.Status.ERROR);
                                curChapter().txtChapter.setMsg(e.getMessage());
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
            prevChapter().txtChapter = null;
            return;
        }
        if (prevChapter().txtChapter == null)
            prevChapter().txtChapter = new TxtChapter(prevChapterPos);
        if (prevChapter().txtChapter.getStatus() == TxtChapter.Status.FINISH) {
            return;
        }
        Single.create((SingleOnSubscribe<TxtChapter>) e -> {
            ChapterProvider chapterProvider = new ChapterProvider(this);
            TxtChapter txtChapter = chapterProvider.dealLoadPageList(callback.getChapterList().get(prevChapterPos), mPageView.isPrepare());
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
                        if (prevChapter().txtChapter == null || prevChapter().txtChapter.getStatus() != TxtChapter.Status.FINISH) {
                            prevChapter().txtChapter = new TxtChapter(prevChapterPos);
                            prevChapter().txtChapter.setStatus(TxtChapter.Status.ERROR);
                            prevChapter().txtChapter.setMsg(e.getMessage());
                        }
                    }
                });
    }

    /**
     * 解析下一章数据
     */
    void parseNextChapter() {
        final int nextChapterPos = mCurChapterPos + 1;
        if (nextChapterPos >= callback.getChapterList().size()) {
            nextChapter().txtChapter = null;
            return;
        }
        if (nextChapter().txtChapter == null)
            nextChapter().txtChapter = new TxtChapter(nextChapterPos);
        if (nextChapter().txtChapter.getStatus() == TxtChapter.Status.FINISH) {
            return;
        }
        Single.create((SingleOnSubscribe<TxtChapter>) e -> {
            ChapterProvider chapterProvider = new ChapterProvider(this);
            TxtChapter txtChapter = chapterProvider.dealLoadPageList(callback.getChapterList().get(nextChapterPos), mPageView.isPrepare());
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
                        if (nextChapter().txtChapter == null || nextChapter().txtChapter.getStatus() != TxtChapter.Status.FINISH) {
                            nextChapter().txtChapter = new TxtChapter(nextChapterPos);
                            nextChapter().txtChapter.setStatus(TxtChapter.Status.ERROR);
                            nextChapter().txtChapter.setMsg(e.getMessage());
                        }
                    }
                });
    }

    private void upTextChapter(TxtChapter txtChapter) {
        if (txtChapter.getPosition() == mCurChapterPos - 1) {
            prevChapter().txtChapter = txtChapter;
            if (mPageMode == PageAnimation.Mode.SCROLL) {
                mPageView.drawContent(-1);
            } else {
                mPageView.drawPage(-1);
            }
        } else if (txtChapter.getPosition() == mCurChapterPos) {
            curChapter().txtChapter = txtChapter;
            reSetPage();
            chapterChangeCallback();
            pagingEnd(PageAnimation.Direction.NONE);
        } else if (txtChapter.getPosition() == mCurChapterPos + 1) {
            nextChapter().txtChapter = txtChapter;
            if (mPageMode == PageAnimation.Mode.SCROLL) {
                mPageView.drawContent(1);
            } else {
                mPageView.drawPage(1);
            }
        }
    }

    private void drawScaledText(Canvas canvas, String line, float lineWidth, TextPaint paint, float top, int y, List<TxtLine> txtLists) {
        float x = mMarginLeft;

        if (isFirstLineOfParagraph(line)) {
            canvas.drawText(indent, x, top, paint);
            float bw = StaticLayout.getDesiredWidth(indent, paint);
            x += bw;
            line = line.substring(readBookControl.getIndent());
        }
        int gapCount = line.length() - 1;
        int i = 0;

        TxtLine txtList = new TxtLine();//每一行pzl
        txtList.setCharsData(new ArrayList<>());//pzl

        float d = ((mDisplayWidth - (mMarginLeft + mMarginRight)) - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, paint);
            canvas.drawText(c, x, top, paint);
            //pzl
            TxtChar txtChar = new TxtChar();
            txtChar.setChardata(line.charAt(i));
            if (i == 0) txtChar.setCharWidth(cw + d / 2);
            if (i == gapCount) txtChar.setCharWidth(cw + d / 2);
            txtChar.setCharWidth(cw + d);
            ;//字宽
            //txtChar.Index = y;//每页每个字的位置
            txtList.getCharsData().add(txtChar);
            //pzl
            x += cw + d;
        }
        if (txtLists != null) {
            txtLists.set(y, txtList);//pzl
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
        if (callback != null) {
            readAloudParagraph = -1;
            callback.onChapterChange(mCurChapterPos);
            callback.onPageCountChange(curChapter().txtChapter != null ? curChapter().txtChapter.getPageSize() : 0);
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

        prevChapter().txtChapter = null;
        curChapter().txtChapter = null;
        nextChapter().txtChapter = null;
    }

    public boolean isClose() {
        return isClose;
    }

    private ChapterContainer prevChapter() {
        return chapterContainers.get(0);
    }

    ChapterContainer curChapter() {
        return chapterContainers.get(1);
    }

    private ChapterContainer nextChapter() {
        return chapterContainers.get(2);
    }

    /*****************************************interface*****************************************/

    class ChapterContainer {
        TxtChapter txtChapter;
    }

    /**
     * --------------------
     * TODO 检测获取按压坐标所在位置的字符，没有的话返回null
     * --------------------
     * author: huangwei
     * 2017年7月4日上午10:23:19
     */
    TxtChar detectPressTxtChar(float down_X2, float down_Y2) {
        TxtPage txtPage = curChapter().txtChapter.getPage(mCurPagePos);
        if (txtPage == null) return null;
        List<TxtLine> txtLines = txtPage.getTxtLists();
        if (txtLines == null) return null;
        for (TxtLine l : txtLines) {
            List<TxtChar> txtChars = l.getCharsData();
            if (txtChars != null) {
                for (TxtChar c : txtChars) {
                    Point leftPoint = c.getBottomLeftPosition();
                    Point rightPoint = c.getBottomRightPosition();
                    if (leftPoint != null && down_Y2 > leftPoint.y) {
                        break;// 说明是在下一行
                    }
                    if (leftPoint != null && rightPoint != null && down_X2 >= leftPoint.x && down_X2 <= rightPoint.x) {
                        return c;
                    }

                }
            }
        }
        return null;
    }

    public interface Callback {
        List<BookChapterBean> getChapterList();

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
        void onCategoryFinish(List<BookChapterBean> chapters);

        /**
         * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
         *
         * @param count:页面的数量
         */
        void onPageCountChange(int count);

        /**
         * 作用：当页面改变的时候回调
         *
         * @param chapterIndex   章节序号
         * @param pageIndex      页数
         * @param resetReadAloud 是否重置朗读
         */
        void onPageChange(int chapterIndex, int pageIndex, boolean resetReadAloud);

        void vipPop();
    }
}
