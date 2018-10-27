package com.monke.monkeybook.widget.page;

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

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.widget.animation.PageAnimation;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

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

    // 书本对象
    BookShelfBean mCollBook;
    // 监听器
    OnPageChangeListener mPageChangeListener;

    private Context mContext;
    // 页面显示类
    PageView mPageView;
    // 上一章的页面列表缓存
    private TxtChapter mPreChapter;
    // 当前章节的页面列表
    TxtChapter mCurChapter;
    // 下一章的页面列表缓存
    private TxtChapter mNextChapter;

    // 绘制电池的画笔
    private Paint mBatteryPaint;
    // 绘制提示的画笔(章节名称和时间)
    private TextPaint mTipPaint;
    // 绘制标题的画笔
    private TextPaint mTitlePaint;
    // 绘制小说内容的画笔
    private TextPaint mTextPaint;
    // 阅读器的配置选项
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    /*****************params**************************/
    // 判断章节列表是否加载完成
    boolean isChapterListPrepare;
    private boolean isClose;
    // 页面的翻页效果模式
    private Enum.PageMode mPageMode;
    //书籍绘制区域的宽高
    private int mVisibleWidth;
    private int mVisibleHeight;
    //应用的宽高
    private int mDisplayWidth;
    private int mDisplayHeight;
    //间距
    private int mMarginTop;
    private int mMarginBottom;
    private int mMarginLeft;
    private int mMarginRight;
    private int contentMarginHeight;
    private int defaultMarginWidth;

    //标题的大小
    private int mTitleSize;
    //字体的大小
    private int mTextSize;
    //行间距
    private int mTextInterval;
    //标题的行间距
    private int mTitleInterval;
    //段落距离(基于行间距的额外距离)
    private int mTextPara;
    private int mTitlePara;
    //电池的百分比
    private int mBatteryLevel;

    // 当前章
    int mCurChapterPos;
    int mCurPagePos;

    /*****************************init params*******************************/
    PageLoader(PageView pageView, BookShelfBean collBook) {
        mPageView = pageView;
        mContext = pageView.getContext();
        mCollBook = collBook;
        mCurChapterPos = mCollBook.getDurChapter();
        mCurPagePos = mCollBook.getDurChapterPage();

        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        // 初始化PageView
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);
    }

    private void initData() {
        // 获取配置参数
        mPageMode = readBookControl.getPageMode(readBookControl.getPageMode());
        // 初始化参数
        mMarginTop = readBookControl.getHideStatusBar()
                ? ScreenUtils.dpToPx(readBookControl.getPaddingTop() + DEFAULT_MARGIN_HEIGHT)
                : ScreenUtils.dpToPx(readBookControl.getPaddingTop());
        mMarginBottom = ScreenUtils.dpToPx(readBookControl.getPaddingBottom() + DEFAULT_MARGIN_HEIGHT);
        mMarginLeft = ScreenUtils.dpToPx(readBookControl.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(readBookControl.getPaddingRight());
        contentMarginHeight = ScreenUtils.dpToPx(CONTENT_MARGIN_HEIGHT);
        defaultMarginWidth = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH);
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
        // 行间距(大小为字体的一半)
        mTextInterval = (int) (mTextSize / 2 * readBookControl.getLineMultiplier());
        mTitleInterval = (int) (mTitleSize / 2 * readBookControl.getLineMultiplier());
        // 段落间距(大小为字体的高度)
        mTextPara = (int) (mTextSize / 2 * readBookControl.getLineMultiplier() * readBookControl.getParagraphSize());
        mTitlePara = (int) (mTitleSize / 2 * readBookControl.getLineMultiplier() * readBookControl.getParagraphSize());
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
        mTitlePaint.setAntiAlias(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(readBookControl.getTextColor());
        mTextPaint.setTextSize(mTextSize);
        int bold = readBookControl.getTextBold() ? Typeface.BOLD : Typeface.NORMAL;
        mTextPaint.setTypeface(Typeface.create(typeface, bold));
        mTextPaint.setAntiAlias(true);

        // 绘制电池的画笔
        mBatteryPaint = new Paint();
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setDither(true);

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

        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    /**
     * 设置页面样式
     */
    public void setPageStyle() {

        mTipPaint.setColor(readBookControl.getTextColor());
        mTitlePaint.setColor(readBookControl.getTextColor());
        mTextPaint.setColor(readBookControl.getTextColor());

        skipToChapter(mCurChapterPos, mCurPagePos);
    }

    /**
     * 设置翻页动画
     */
    public void setPageMode(Enum.PageMode pageMode) {
        mPageMode = pageMode;

        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);

        // 重新绘制当前页
        mPageView.resetScroll();
        openChapter(mCurPagePos);
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
            mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
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
        BookshelfHelp.delChapter(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()), mCurChapterPos, mCollBook.getChapterList(mCurChapterPos).getDurChapterName());
        skipToChapter(mCurChapterPos, 0);
    }

    /**
     * 换源结束
     */
    public void changeSourceFinish(BookShelfBean bookShelfBean) {
        if (bookShelfBean == null) {
            openChapter(mCollBook.getDurChapterPage());
        } else {
            mCollBook = bookShelfBean;
            mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
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

        openChapter(mCurPagePos);
        chapterChangeCallback();
        pagingEnd(PageAnimation.Direction.NONE);
    }

    /**
     * 跳转到下一章
     */
    public boolean skipNextChapter() {
        if (mCurChapterPos + 1 >= mCollBook.getChapterListSize()) {
            return false;
        }

        //载入下一章
        mCurChapterPos = mCurChapterPos + 1;
        mCurPagePos = 0;
        mPreChapter = mCurChapter;
        mCurChapter = mNextChapter;
        mNextChapter = null;
        parseNextChapter();

        openChapter(mCurPagePos);
        chapterChangeCallback();
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
        mCurPagePos = pos;
        openChapter(mCurPagePos);
    }

    /**
     * 翻到上一页
     */
    public void skipToPrePage() {
        mPageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     */
    public void skipToNextPage() {
        mPageView.autoNextPage();
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
        if (!mPageView.isRunning() && readBookControl.getHideStatusBar() && readBookControl.getShowTimeBattery()) {
            if (mPageMode == Enum.PageMode.SCROLL) {
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
        mBatteryLevel = level;
        if (!mPageView.isRunning() && readBookControl.getHideStatusBar() && readBookControl.getShowTimeBattery()) {
            if (mPageMode == Enum.PageMode.SCROLL) {
                mPageView.drawBackground(0);
            } else {
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
    public Enum.PageStatus getPageStatus() {
        return mCurChapter != null ? mCurChapter.getStatus() : Enum.PageStatus.LOADING;
    }

    /**
     * 获取书籍信息
     */
    public BookShelfBean getCollBook() {
        return mCollBook;
    }

    /**
     * 获取当前章节位置
     */
    public int getCurChapterPos() {
        return mCurChapterPos;
    }

    /**
     * 获取当前页的页码
     */
    public int getCurPagePos() {
        return mCurPagePos;
    }

    /**
     * 更新状态
     */
    public void setStatus(Enum.PageStatus status) {
        mCurChapter.setStatus(status);
        if (mPageMode != Enum.PageMode.SCROLL) {
            upPage();
        } else {
            mPageView.resetScroll();
            mPageView.drawPage(0);
        }
        mPageView.invalidate();
    }

    /**
     * 加载错误
     */
    void chapterError(String msg) {
        if (mCurChapter == null) {
            mCurChapter = new TxtChapter(mCurChapterPos);
        }
        mCurChapter.setStatus(Enum.PageStatus.ERROR);
        mCurChapter.setMsg(msg);
        if (mPageMode != Enum.PageMode.SCROLL) {
            upPage();
        } else {
            mPageView.resetScroll();
            mPageView.drawPage(0);
        }
        mPageView.invalidate();
    }

    /**
     * 获取正文
     */
    public String getContent(int pagePos) {
        if (mCurChapter.getStatus() != Enum.PageStatus.FINISH || mCurChapter.getPageSize() <= pagePos) {
            return null;
        }
        TxtPage txtPage = mCurChapter.getPage(pagePos);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < txtPage.lines.size(); i++) {
            s.append(txtPage.lines.get(i));
        }
        return s.toString();
    }

    /**
     * 刷新章节列表
     */
    public abstract void refreshChapterList();

    /**
     * 获取章节的文本流
     */
    protected abstract BufferedReader getChapterReader(ChapterListBean chapter) throws Exception;

    /**
     * 章节数据是否存在
     */
    protected abstract boolean hasChapterData(ChapterListBean chapter);

    /**
     * 打开当前章节指定页
     */
    void openChapter(int pagePos) {
        if (mCurChapter == null) {
            mCurChapter = new TxtChapter(mCurChapterPos);
        }

        mCurPagePos = pagePos;
        if (!mPageView.isPrepare()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mCurChapter.setStatus(Enum.PageStatus.LOADING);
            mPageView.drawPage(0);
            return;
        }

        // 如果获取到的章节目录为空
        if (mCollBook.getChapterList().isEmpty()) {
            mCurChapter.setStatus(Enum.PageStatus.CATEGORY_EMPTY);
            mPageView.drawPage(0);
            return;
        }

        parseCurChapter();
        if (mPageMode == Enum.PageMode.SCROLL) {
            mPageView.resetScroll();
            mPageView.drawPage(0);
        } else {
            upPage();
        }
        mPageView.invalidate();
        chapterChangeCallback();
        pagingEnd(PageAnimation.Direction.NONE);
    }

    public void upPage() {
        if (mPageMode != Enum.PageMode.SCROLL) {
            if (mCurPagePos > 0 || mCurChapter.getPosition() > 0) {
                mPageView.drawPage(-1);
            }
            if (mCurPagePos < mCurChapter.getPageSize() - 1 || mCurChapter.getPosition() < mCollBook.getChapterList().size() - 1) {
                mPageView.drawPage(1);
            }
            mPageView.drawPage(0);
        } else {
            mPageView.drawBackground(0);
            mPageView.drawContent(1);
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
                } else if (mCurChapterPos < mCollBook.getChapterListSize() - 1) {
                    mCurChapterPos = mCurChapterPos + 1;
                    mCurPagePos = 0;
                    mPreChapter = mCurChapter;
                    mCurChapter = mNextChapter;
                    mNextChapter = null;
                    parseNextChapter();
                    chapterChangeCallback();
                }
                mPageView.drawPage(1);
                break;
            case PRE:
                if (mCurPagePos > 0) {
                    mCurPagePos = mCurPagePos - 1;
                } else if (mCurChapterPos > 0){
                    mCurChapterPos = mCurChapterPos - 1;
                    mCurPagePos = mPreChapter.getPageSize() - 1;
                    mNextChapter = mCurChapter;
                    mCurChapter = mPreChapter;
                    mPreChapter = null;
                    parsePrevChapter();
                    chapterChangeCallback();
                }
                mPageView.drawPage(-1);
                break;
        }
        mPageView.setContentDescription(getContent(getCurPagePos()));
        mCollBook.setDurChapter(mCurChapterPos);
        mCollBook.setDurChapterPage(mCurPagePos);
        mPageChangeListener.onPageChange(mCurChapterPos, getCurPagePos());
    }

    /**
     * 绘制页面
     * pageOnCur: 位于当前页的位置, 小于0上一页, 0 当前页, 大于0下一页
     */
    void drawPage(Bitmap bgBitmap, Bitmap bitmap, int pageOnCur) {
        TxtChapter txtChapter;
        TxtPage txtPage;
        if (mCurChapter == null) {
            mCurChapter = dealLoadPageList(mCurChapterPos);
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
            drawBackground(bgBitmap, txtChapter, txtPage);
            drawContent(bitmap, txtChapter, txtPage);
        }
    }

    /**
     * 绘制背景
     */
    @SuppressLint("DefaultLocale")
    private void drawBackground(Bitmap bitmap, TxtChapter txtChapter, TxtPage txtPage) {
        if (bitmap == null) return;
        Canvas canvas = new Canvas(bitmap);
        if (readBookControl.bgIsColor()) {
            canvas.drawColor(readBookControl.getBgColor());
        } else {
            Rect mDestRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(readBookControl.getBgBitmap(), null, mDestRect, null);
        }
        int defMarginHeight = ScreenUtils.dpToPx(DEFAULT_MARGIN_HEIGHT);
        Paint.FontMetrics fontMetrics = mTipPaint.getFontMetrics();
        final float tipMarginHeight = (defMarginHeight + fontMetrics.top - fontMetrics.bottom) / 2;
        final float tipBottomTop = tipMarginHeight - fontMetrics.top;
        final float tipBottomBot = mDisplayHeight - fontMetrics.bottom - tipMarginHeight;
        final float tipDistance = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH);
        final float tipMarginLeft = readBookControl.getTipMarginChange() ? mMarginLeft : defaultMarginWidth;
        final float tipMarginRight = readBookControl.getTipMarginChange() ? mMarginRight : defaultMarginWidth;
        final float displayRightEnd = mDisplayWidth - tipMarginRight;
        final float tipVisibleWidth = mDisplayWidth - tipMarginLeft - tipMarginRight;
        boolean hideStatusBar = readBookControl.getHideStatusBar();
        boolean showTimeBattery = readBookControl.getShowTimeBattery();

        if (!mCollBook.getChapterList().isEmpty()) {
            String title = isChapterListPrepare ? mCollBook.getChapterList(txtChapter.getPosition()).getDurChapterName() : "";
            title = ChapterContentHelp.replaceContent(mCollBook, title);
            String page = (txtChapter.getStatus() != Enum.PageStatus.FINISH) ? ""
                    : String.format("%d/%d", txtPage.position + 1, txtChapter.getPageSize());
            String progress = (txtChapter.getStatus() != Enum.PageStatus.FINISH) ? ""
                    : BookshelfHelp.getReadProgress(mCurChapterPos, mCollBook.getChapterListSize(), mCurPagePos, mCurChapter.getPageSize());

            float tipBottom;
            float tipLeft;
            //初始化标题的参数
            //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部的位置
            if (!hideStatusBar) { //显示状态栏
                if (txtChapter.getStatus() != Enum.PageStatus.FINISH) {
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
                    tipBottom = mDisplayHeight - defMarginHeight;
                    canvas.drawRect(tipMarginLeft, tipBottom, displayRightEnd, tipBottom + 2, mTipPaint);
                }
            } else { //隐藏状态栏
                if (getPageStatus() != Enum.PageStatus.FINISH) {
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
                    tipBottom = defMarginHeight - 2;
                    canvas.drawRect(tipMarginLeft, tipBottom, displayRightEnd, tipBottom + 2, mTipPaint);
                }
            }
        }

        int visibleRight = (int) displayRightEnd;
        if (hideStatusBar && showTimeBattery) {
            //绘制当前时间
            String time = StringUtils.dateConvert(System.currentTimeMillis(), Constant.FORMAT_TIME);
            float timeTipLeft = (mDisplayWidth - mTipPaint.measureText(time)) / 2;
            canvas.drawText(time, timeTipLeft, tipBottomBot, mTipPaint);

            //绘制电池
            int outFrameWidth = (int) mTipPaint.measureText("xxx");
            int outFrameHeight = (int) mTipPaint.getTextSize() - ScreenUtils.dpToPx(4);
            int visibleBottom = mDisplayHeight - (defMarginHeight - outFrameHeight) / 2;

            int polarHeight = ScreenUtils.dpToPx(4);
            int polarWidth = ScreenUtils.dpToPx(2);
            int border = 1;
            int innerMargin = 1;

            //电极的制作
            int polarLeft = visibleRight - polarWidth;
            int polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2;
            Rect polar = new Rect(polarLeft, polarTop, visibleRight, polarTop + polarHeight);

            mBatteryPaint.setColor(readBookControl.getTextColor());
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

    /**
     * 绘制内容
     */
    private void drawContent(Bitmap bitmap, TxtChapter txtChapter, TxtPage txtPage) {
        if (bitmap == null) return;
        Canvas canvas = new Canvas(bitmap);
        if (mPageMode == Enum.PageMode.SCROLL) {
            bitmap.eraseColor(Color.TRANSPARENT);
        }

        //设置总距离
        int interval = mTextInterval + (int) mTextPaint.getTextSize();
        int para = mTextPara + (int) mTextPaint.getTextSize();
        int titleInterval = mTitleInterval + (int) mTitlePaint.getTextSize();
        int titlePara = mTitlePara + (int) mTextPaint.getTextSize();
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;

        if (txtChapter.getStatus() != Enum.PageStatus.FINISH) {
            //绘制字体
            String tip = "";
            switch (txtChapter.getStatus()) {
                case LOADING:
                    tip = "正在拼命加载中...";
                    break;
                case ERROR:
                    tip = String.format("加载失败\n%s", txtChapter.getMsg());
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

            //将提示语句放到正中间
            Layout tempLayout = new StaticLayout(tip, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
            List<String> linesData = new ArrayList<>();
            for (int i = 0; i < tempLayout.getLineCount(); i++) {
                linesData.add(tip.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
            }
            float pivotY = (mDisplayHeight - (textHeight + interval) * linesData.size()) / 3;
            for (String str : linesData) {
                float textWidth = mTextPaint.measureText(str);
                float pivotX = (mDisplayWidth - textWidth) / 2;
                canvas.drawText(str, pivotX, pivotY, mTextPaint);
                pivotY += interval;
            }
        } else {
            float top = contentMarginHeight - fontMetrics.ascent;
            if (mPageMode != Enum.PageMode.SCROLL) {
                top += readBookControl.getHideStatusBar() ? mMarginTop : mPageView.getStatusBarHeight() + mMarginTop;
            }

            //对标题进行绘制
            String str;
            for (int i = 0; i < txtPage.titleLines; ++i) {
                str = txtPage.lines.get(i);

                //计算文字显示的起始点
                int start = (int) (mDisplayWidth - mTitlePaint.measureText(str)) / 2;
                //进行绘制
                canvas.drawText(str, start, top, mTitlePaint);

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
                Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);

                if (needScale(str)) {
                    drawScaledText(canvas, str, width, mTextPaint, top);
                } else {
                    canvas.drawText(str, mMarginLeft, top, mTextPaint);
                }

                //设置尾部间距
                if (str.endsWith("\n")) {
                    top += para;
                } else {
                    top += interval;
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
        if (getPageStatus() == Enum.PageStatus.FINISH) {
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
        if (getPageStatus() == Enum.PageStatus.FINISH) {
            // 先查看是否存在下一页
            if (mCurPagePos + pageOnCur < mCurChapter.getPageSize() - 1) {
                return true;
            }
        }
        return mCurChapterPos + 1 < mCollBook.getChapterListSize();
    }

    /**
     * 解析数据
     */
    void parseCurChapter() {
        if (mCurChapter.getStatus() != Enum.PageStatus.FINISH) {
            mCurChapter = dealLoadPageList(mCurChapterPos);
        }
        parsePrevChapter();
        parseNextChapter();
    }

    /**
     * 解析上一章数据
     */
    void parsePrevChapter() {
        final int prevChapterPos = mCurChapterPos - 1;
        if ((mPreChapter == null || mPreChapter.getStatus() != Enum.PageStatus.FINISH)
                && prevChapterPos >= 0) {
            mPreChapter = dealLoadPageList(prevChapterPos);
        }
    }

    /**
     * 解析下一章数据
     */
    void parseNextChapter() {
        final int nextChapterPos = mCurChapterPos + 1;
        if ((mNextChapter == null || mNextChapter.getStatus() != Enum.PageStatus.FINISH)
                && nextChapterPos < mCollBook.getChapterList().size()) {
            mNextChapter = dealLoadPageList(nextChapterPos);
        }
    }


    /**
     * @param chapterPos　章节Pos
     * @return 章节数据
     */
    TxtChapter dealLoadPageList(int chapterPos) {
        TxtChapter txtChapter = new TxtChapter(chapterPos);
        txtChapter.setStatus(Enum.PageStatus.LOADING);
        // 获取章节
        ChapterListBean chapter = mCollBook.getChapterList(chapterPos);
        // 判断章节是否存在
        if (!mPageView.isPrepare() || !hasChapterData(chapter)) {
            return txtChapter;
        }
        // 获取章节的文本流
        BufferedReader reader = null;
        List<TxtPage> pages = null;
        try {
            reader = getChapterReader(chapter);
            pages = loadPageList(chapter, reader);
        } catch (Exception e) {
            IOUtils.close(reader);
            e.printStackTrace();
        }
        if (pages != null) {
            txtChapter.setTxtPageList(pages);
            txtChapter.setStatus(Enum.PageStatus.FINISH);
            if (txtChapter.getTxtPageList().isEmpty()) {
                txtChapter.setStatus(Enum.PageStatus.EMPTY);
                // 添加一个空数据
                TxtPage page = new TxtPage();
                page.lines = new ArrayList<>(1);
                txtChapter.getTxtPageList().add(page);
            }
        }
        return txtChapter;
    }

    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     */
    private List<TxtPage> loadPageList(ChapterListBean chapter, BufferedReader br) throws Exception {
        //生成的页面
        List<TxtPage> pages = new ArrayList<>();
        //使用流的方式加载
        List<String> lines = new ArrayList<>();
        int rHeight = mVisibleHeight - contentMarginHeight * 2;
        int titleLinesCount = 0;
        boolean showTitle = true; // 是否展示标题
        String paragraph = chapter.getDurChapterName() + "\n"; //默认展示标题
        br.readLine(); //去除标题行
        if (!readBookControl.getShowTitle()) {
            showTitle = false;
            paragraph = null;
        }
        while (showTitle || (paragraph = br.readLine()) != null) {
            paragraph = ChapterContentHelp.replaceContent(mCollBook, paragraph);
            paragraph = ChapterContentHelp.toTraditional(readBookControl, paragraph);
            paragraph = paragraph.replaceAll("\\s", " ").trim();
            // 如果只有换行符，那么就不执行
            if (paragraph.equals("")) continue;
            // 重置段落
            if (!showTitle) {
                paragraph = StringUtils.halfToFull("  ") + paragraph + "\n";
            }
            int wordCount;
            String subStr;
            while (paragraph.length() > 0) {
                //当前空间，是否容得下一行文字
                if (showTitle) {
                    rHeight -= mTitlePaint.getTextSize();
                } else {
                    rHeight -= mTextPaint.getTextSize();
                }
                // 一页已经填充满了，创建 TextPage
                if (rHeight <= 0) {
                    // 创建Page
                    TxtPage page = new TxtPage();
                    page.position = pages.size();
                    page.title = chapter.getDurChapterName();
                    page.lines = new ArrayList<>(lines);
                    page.titleLines = titleLinesCount;
                    pages.add(page);
                    // 重置Lines
                    lines.clear();
                    rHeight = mVisibleHeight - contentMarginHeight * 2;
                    titleLinesCount = 0;

                    continue;
                }

                //测量一行占用的字节数
                if (showTitle) {
                    Layout tempLayout = new StaticLayout(paragraph, mTitlePaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    wordCount = tempLayout.getLineEnd(0);
                } else {
                    Layout tempLayout = new StaticLayout(paragraph, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    wordCount = tempLayout.getLineEnd(0);

                }

                subStr = paragraph.substring(0, wordCount);
                if (!subStr.equals("\n")) {
                    //将一行字节，存储到lines中
                    lines.add(subStr);

                    //设置段落间距
                    if (showTitle) {
                        titleLinesCount += 1;
                        rHeight -= mTitleInterval;
                    } else {
                        rHeight -= mTextInterval;
                    }
                }
                //裁剪
                paragraph = paragraph.substring(wordCount);
            }

            //增加段落的间距
            if (!showTitle && lines.size() != 0) {
                rHeight = rHeight - mTextPara + mTextInterval;
            }

            if (showTitle) {
                rHeight = rHeight - mTitlePara + mTitleInterval;
                showTitle = false;
            }
        }

        if (lines.size() != 0) {
            //创建Page
            TxtPage page = new TxtPage();
            page.position = pages.size();
            page.title = chapter.getDurChapterName();
            page.lines = new ArrayList<>(lines);
            page.titleLines = titleLinesCount;
            pages.add(page);
            //重置Lines
            lines.clear();
        }
        return pages;
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
            mPageChangeListener.onChapterChange(mCurChapterPos);
            mPageChangeListener.onPageCountChange(mCurChapter != null ? mCurChapter.getPageSize() : 0);
        }
    }

    /**
     * 根据当前状态，决定是否能够翻页
     */
    private boolean canNotTurnPage() {
        return !isChapterListPrepare
                || getPageStatus() == Enum.PageStatus.CHANGE_SOURCE;
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        isChapterListPrepare = false;
        isClose = true;

        mPreChapter = null;
        mCurChapter = null;
        mNextChapter = null;
        mPageView = null;
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
        void onPageChange(int chapterIndex, int pageIndex);
    }
}
