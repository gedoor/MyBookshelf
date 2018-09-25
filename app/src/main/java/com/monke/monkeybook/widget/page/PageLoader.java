package com.monke.monkeybook.widget.page;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

import static android.widget.Toast.*;
import static com.monke.monkeybook.bean.BookShelfBean.LOCAL_TAG;

/**
 * Created by newbiechen on 17-7-1.
 */

public abstract class PageLoader {
    private static final String TAG = "PageLoader";

    // 当前页面的状态
    public static final int STATUS_LOADING = 1;         // 正在加载
    public static final int STATUS_FINISH = 2;          // 加载完成
    public static final int STATUS_ERROR = 3;           // 加载错误 (一般是网络加载情况)
    public static final int STATUS_EMPTY = 4;           // 空数据
    public static final int STATUS_PARING = 5;          // 正在解析 (装载本地数据)
    public static final int STATUS_PARSE_ERROR = 6;     // 本地文件解析错误(暂未被使用)
    public static final int STATUS_CATEGORY_EMPTY = 7;  // 获取到的目录为空
    public static final int STATUS_HY = 8;              // 换源
    // 默认的显示参数配置
    private static final int CONTENT_MARGIN_HEIGHT = 6;
    public static final int DEFAULT_MARGIN_HEIGHT = 20;
    public static final int DEFAULT_MARGIN_WIDTH = 15;
    private static final int DEFAULT_TIP_SIZE = 12;
    private static final int EXTRA_TITLE_SIZE = 1;

    // 书本对象
    protected BookShelfBean mCollBook;
    // 监听器
    protected OnPageChangeListener mPageChangeListener;

    private Context mContext;
    // 页面显示类
    protected PageView mPageView;
    // 当前显示的页
    private TxtPage mCurPage;
    // 上一章的页面列表缓存
    private List<TxtPage> mPrePageList;
    // 当前章节的页面列表
    private List<TxtPage> mCurPageList;
    // 下一章的页面列表缓存
    private List<TxtPage> mNextPageList;

    // 绘制电池的画笔
    private Paint mBatteryPaint;
    // 绘制提示的画笔(章节名称和时间)
    private TextPaint mTipPaint;
    // 绘制标题的画笔
    private TextPaint mTitlePaint;
    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private Paint mBgPaint;
    // 绘制小说内容的画笔
    private TextPaint mTextPaint;
    // 阅读器的配置选项
    private ReadBookControl mSettingManager;
    // 被遮盖的页，或者认为被取消显示的页
    private TxtPage mCancelPage;


    private Disposable mPreLoadDisposable;

    /*****************params**************************/
    // 当前的状态
    protected HashMap<Integer, Integer> mStatus = new HashMap<>();
    //errorMsg
    private String errorMsg;
    // 判断章节列表是否加载完成
    protected boolean isChapterListPrepare;

    // 是否打开过章节
    private boolean isChapterOpen;
    private boolean isFirstOpen = true;
    private boolean isClose;
    // 页面的翻页效果模式
    private PageMode mPageMode;
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
    //字体的颜色
    private int mTextColor;
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
    //当前页面的背景
    private int mBgColor;

    // 当前章
    protected int mCurChapterPos = 0;
    //上一章的记录
    private int mLastChapterPos = 0;
    private int goPagePos = 0;

    /*****************************init params*******************************/
    public PageLoader(PageView pageView, BookShelfBean collBook) {
        mPageView = pageView;
        mContext = pageView.getContext();
        mCollBook = collBook;

        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        // 初始化PageView
        initPageView();
        // 初始化书籍
        prepareBook();
    }

    private int getChapterPageStatus(Integer chapter) {
        if(mStatus.containsKey(chapter)) {
            return mStatus.get(chapter);
        } else {
            return STATUS_LOADING;
        }
    }

    public void setChapterPageStatus(Integer status) {
        mStatus.put(mCurChapterPos, status);
    }

    private void setChapterPageStatus(Integer chapter, Integer status) {
            mStatus.put(chapter, status);
    }

    private void initData() {
        // 获取配置管理器
        mSettingManager = ReadBookControl.getInstance();
        // 获取配置参数
        mPageMode = mSettingManager.getPageMode(mSettingManager.getPageMode());
        // 初始化参数
        mMarginTop = mSettingManager.getHideStatusBar()
                ? ScreenUtils.dpToPx(mSettingManager.getPaddingTop() + DEFAULT_MARGIN_HEIGHT)
                : ScreenUtils.dpToPx(mSettingManager.getPaddingTop());
        mMarginBottom = ScreenUtils.dpToPx(mSettingManager.getPaddingBottom() + DEFAULT_MARGIN_HEIGHT);
        mMarginLeft = ScreenUtils.dpToPx(mSettingManager.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(mSettingManager.getPaddingRight());
        contentMarginHeight = ScreenUtils.dpToPx(CONTENT_MARGIN_HEIGHT);
        // 配置文字有关的参数
        setUpTextParams();
    }

    /**
     * 作用：设置与文字相关的参数
     */
    private void setUpTextParams() {
        // 文字大小
        mTextSize = ScreenUtils.spToPx(mSettingManager.getTextSize());
        mTitleSize = mTextSize + ScreenUtils.spToPx(EXTRA_TITLE_SIZE);
        // 行间距(大小为字体的一半)
        mTextInterval = (int) (mTextSize / 2 * mSettingManager.getLineMultiplier());
        mTitleInterval = (int) (mTitleSize / 2 * mSettingManager.getLineMultiplier());
        // 段落间距(大小为字体的高度)
        mTextPara = (int) (mTextSize / 2 * mSettingManager.getLineMultiplier() * mSettingManager.getParagraphSize());
        mTitlePara = (int) (mTitleSize / 2 * mSettingManager.getLineMultiplier() * mSettingManager.getParagraphSize());
    }

    public void initPaint() {
        Typeface typeface;
        try {
            if (mSettingManager.getFontPath() != null || "".equals(mSettingManager.getFontPath())) {
                typeface = Typeface.createFromFile(mSettingManager.getFontPath());
            } else {
                typeface = Typeface.SANS_SERIF;
            }
        } catch (Exception e) {
            makeText(mContext, "字体文件未找,到恢复默认字体", LENGTH_SHORT).show();
            mSettingManager.setReadBookFont(null);
            typeface = Typeface.SANS_SERIF;
        }
        // 绘制提示的画笔
        mTipPaint = new TextPaint();
        mTipPaint.setColor(mTextColor);
        mTipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        mTipPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE)); // Tip默认的字体大小
        mTipPaint.setTypeface(Typeface.create(typeface, Typeface.NORMAL));
        mTipPaint.setAntiAlias(true);
        mTipPaint.setSubpixelText(true);

        // 绘制标题的画笔
        mTitlePaint = new TextPaint();
        mTitlePaint.setColor(mTextColor);
        mTitlePaint.setTextSize(mTitleSize);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        mTitlePaint.setAntiAlias(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        int bold = mSettingManager.getTextBold() ? Typeface.BOLD : Typeface.NORMAL;
        mTextPaint.setTypeface(Typeface.create(typeface, bold));
        mTextPaint.setAntiAlias(true);

        // 绘制背景的画笔
        mBgPaint = new Paint();
        mBgPaint.setColor(mBgColor);

        // 绘制电池的画笔
        mBatteryPaint = new Paint();
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setDither(true);

        // 初始化页面样式
        setPageStyle();
    }

    private void initPageView() {
        //配置参数
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);
    }

    /**
     * 刷新界面
     */
    public void refreshUi() {
        initData();
        initPaint();
        initPageView();
        skipToChapter(mCurChapterPos, mCurPage.position);
    }

    /**
     * 刷新当前章节
     */
    @SuppressLint("DefaultLocale")
    public void refreshDurChapter() {
        BookshelfHelp.delChapter(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),
                String.format("%d-%s", mCurChapterPos, mCollBook.getChapterList(mCurChapterPos).getDurChapterName()));
        BookshelfHelp.setChapterIsCached(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),mCurChapterPos,false);
        skipToChapter(mCurChapterPos);
    }

    /**
     * 换源结束
     */
    public void changeSourceFinish(BookShelfBean bookShelfBean) {
        mCollBook = bookShelfBean;
        mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
        skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
    }

    /**
     * 跳转到上一章
     */
    public boolean skipPreChapter() {
        goPagePos = 0;
        if (!hasPrevChapter()) {
            return false;
        }

        // 载入上一章。
        if (parsePrevChapter()) {
            mCurPage = getCurPage(0);
            pagingEnd();
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.resetScroll();
        mPageView.drawCurPage();
        return true;
    }

    /**
     * 跳转到下一章
     */
    public boolean skipNextChapter() {
        goPagePos = 0;
        if (!hasNextChapter()) {
            return false;
        }

        //判断是否达到章节的终止点
        if (parseNextChapter()) {
            mCurPage = getCurPage(0);
            pagingEnd();
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.resetScroll();
        mPageView.drawCurPage();
        return true;
    }

    /**
     * 跳转到指定章节页
     */
    public void skipToChapter(int chapterPos, int pagePos) {
        goPagePos = pagePos;
        // 设置参数
        mCurChapterPos = chapterPos;

        // 将上一章的缓存设置为null
        mPrePageList = null;
        // 如果当前下一章缓存正在执行，则取消
        if (mPreLoadDisposable != null) {
            mPreLoadDisposable.dispose();
        }
        // 将下一章缓存设置为null
        mNextPageList = null;
        mStatus.clear();

        // 打开指定章节
        openChapter(pagePos);
    }

    /**
     * 跳转到指定章节
     * @param pos:从 0 开始。
     */
    public void skipToChapter(int pos) {
        goPagePos = 0;
        // 设置参数
        mCurChapterPos = pos;

        // 将上一章的缓存设置为null
        mPrePageList = null;
        // 如果当前下一章缓存正在执行，则取消
        if (mPreLoadDisposable != null) {
            mPreLoadDisposable.dispose();
        }
        // 将下一章缓存设置为null
        mNextPageList = null;
        mStatus.clear();

        // 打开指定章节
        openChapter(0);
    }

    /**
     * 跳转到指定的页
     */
    public boolean skipToPage(int pos) {
        if (!isChapterListPrepare) {
            return false;
        }
        mCurPage = getCurPage(pos);
        mPageView.resetScroll();
        mPageView.drawCurPage();
        pagingEnd();
        return true;
    }

    /**
     * 翻到上一页
     */
    public boolean skipToPrePage() {
        return mPageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     */
    public boolean skipToNextPage() {
        return mPageView.autoNextPage();
    }

    /**
     * 翻到下一页,无动画
     */
    public boolean noAnimationToNextPage() {
        if (getPagePos() < getPageSize() - 1) {
            skipToPage(getPagePos() + 1);
            return true;
        }
        return skipNextChapter();
    }

    /**
     * 翻页完成
     */
    public void pagingEnd() {
        mPageView.upPagePos(mCurChapterPos, mCurPage.position);
        mPageView.setContentDescription(getContext(getPagePos()));
        mPageChangeListener.onPageChange(mCurChapterPos, getPagePos());
    }

    /**
     * 更新时间
     */
    public void updateTime() {
        if (!mPageView.isRunning() && mSettingManager.getHideStatusBar() && mSettingManager.getShowTimeBattery()) {
            mPageView.drawCurPage();
        }
    }

    /**
     * 更新电量
     */
    public void updateBattery(int level) {
        mBatteryLevel = level;

        if (!mPageView.isRunning() && mSettingManager.getHideStatusBar() && mSettingManager.getShowTimeBattery()) {
            mPageView.drawCurPage();
        }
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
        // 取消缓存
        mPrePageList = null;
        mNextPageList = null;
        mStatus.clear();

        // 如果当前已经显示数据
        if (isChapterListPrepare && getPageStatus() == STATUS_FINISH) {
            // 重新计算当前页面
            dealLoadPageList(mCurChapterPos);

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (mCurPage.position >= mCurPageList.size()) {
                mCurPage.position = mCurPageList.size() - 1;
            }

            // 重新获取指定页面
            mCurPage = mCurPageList.get(mCurPage.position);
        }

        mPageView.drawCurPage();
    }

    /**
     * 设置页面样式
     */
    public void setPageStyle() {

        // 设置当前颜色样式
        mTextColor = mSettingManager.getTextColor();
        mBgColor = mSettingManager.getBgColor();

        mTipPaint.setColor(mTextColor);
        mTitlePaint.setColor(mTextColor);
        mTextPaint.setColor(mTextColor);

        mBgPaint.setColor(mBgColor);

        mPageView.drawCurPage();
    }

    /**
     * 设置翻页动画
     */
    public void setPageMode(PageMode pageMode) {
        mPageMode = pageMode;

        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);

        // 重新绘制当前页
        mPageView.resetScroll();
        mPageView.drawCurPage();
    }

    /**
     * 设置内容与屏幕的间距 单位为 px
     */
    public void upMargin() {
        mMarginTop = mSettingManager.getHideStatusBar()
                ? ScreenUtils.dpToPx(mSettingManager.getPaddingTop() + DEFAULT_MARGIN_HEIGHT)
                : ScreenUtils.dpToPx(mSettingManager.getPaddingTop());
        mMarginBottom = ScreenUtils.dpToPx(mSettingManager.getPaddingBottom() + DEFAULT_MARGIN_HEIGHT);
        mMarginLeft = ScreenUtils.dpToPx(mSettingManager.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(mSettingManager.getPaddingRight());

        prepareDisplay(mDisplayWidth, mDisplayHeight);

        mPageView.drawCurPage();
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
     * 获取当前页的状态
     */
    public int getPageStatus() {
        return getChapterPageStatus(mCurChapterPos);
    }

    /**
     * 获取书籍信息
     */
    public BookShelfBean getCollBook() {
        return mCollBook;
    }

    public int getChapterPos() {
        return mCurChapterPos;
    }

    /**
     * 获取当前页的页码
     */
    public int getPagePos() {
        return mCurPage == null ? 0 : mCurPage.position;
    }

    /**
     * 初始化书籍
     */
    private void prepareBook() {

        mCurChapterPos = mCollBook.getDurChapter();
        mLastChapterPos = mCurChapterPos;
    }

    /**
     * 打开指定章节
     */
    public synchronized void openChapter(int pagePos) {

        isFirstOpen = false;

        if (!mPageView.isPrepare()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            setChapterPageStatus(STATUS_LOADING);
            mPageView.drawCurPage();
            return;
        }

        // 如果获取到的章节目录为空
        if (mCollBook.getChapterList().isEmpty()) {
            setChapterPageStatus(STATUS_CATEGORY_EMPTY);
            mPageView.drawCurPage();
            return;
        }

        if (parseCurChapter()) {
            if (goPagePos != 0) {
                pagePos = goPagePos;
                goPagePos = 0;
                isChapterOpen = false;
            }
            // 如果章节从未打开
            if (!isChapterOpen) {

                // 防止记录页的页号，大于当前最大页号
                if (pagePos >= mCurPageList.size()) {
                    pagePos = mCurPageList.size() - 1;
                }
                mCurPage = getCurPage(pagePos);
                mCancelPage = mCurPage;
                // 切换状态
                isChapterOpen = true;
            } else {
                mCurPage = getCurPage(0);
            }
            setChapterPageStatus(STATUS_FINISH);
        } else {
            mCurPage = new TxtPage();
        }
        pagingEnd();
        mPageView.drawCurPage();
    }

    /**
     * 更新状态
     */
    public void setStatus(int status) {
        setChapterPageStatus(status);
        errorMsg = "";
        mPageView.drawCurPage();
    }

    /**
     * 加载错误
     */
    public void chapterError(String msg) {
        setChapterPageStatus(STATUS_ERROR);
        errorMsg = msg;
        mPageView.drawCurPage();
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        isChapterListPrepare = false;
        isClose = true;

        if (mPreLoadDisposable != null) {
            mPreLoadDisposable.dispose();
        }

        clearList(mCurPageList);
        clearList(mNextPageList);

        mCurPageList = null;
        mNextPageList = null;
        mPageView = null;
        mCurPage = null;
        mStatus.clear();
    }

    private void clearList(List list) {
        if (list != null) {
            list.clear();
        }
    }

    public boolean isClose() {
        return isClose;
    }

    public boolean isChapterOpen() {
        return isChapterOpen;
    }

    public int getPageSize() {
        return mCurPageList == null ? 0 : mCurPageList.size();
    }

    /**
     * 获取正文
     */
    public String getContext(int pagePos) {
        if (mCurPageList == null || mCurPageList.size() <= pagePos) {
            return null;
        }
        TxtPage txtPage = mCurPageList.get(pagePos);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < txtPage.lines.size(); i++) {
            s.append(txtPage.lines.get(i));
        }
        return s.toString();
    }

    /**
     * 加载页面列表
     */
    private List<TxtPage> loadPageList(int chapterPos) throws Exception {
        // 获取章节
        ChapterListBean chapter = mCollBook.getChapterList(chapterPos);
        // 判断章节是否存在
        if (!hasChapterData(chapter)) {
            return null;
        }
        // 获取章节的文本流
        BufferedReader reader = getChapterReader(chapter);
        return loadPageList(chapter, reader);
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
     * 绘制页面
     */
    public void drawPage(Bitmap bitmap) {
        drawBackground(mPageView.getBgBitmap());
        drawContent(bitmap);
        //更新绘制
        mPageView.invalidate();
    }

    /**
     * 绘制背景
     */
    @SuppressLint("DefaultLocale")
    private void drawBackground(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        int tipMarginHeight = ScreenUtils.dpToPx(3);
        int tipMarginWidth = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH);
        String percent;
        if (mSettingManager.bgIsColor()) {
            canvas.drawColor(mSettingManager.getBgColor());
        } else {
            Rect mDestRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(mSettingManager.getBgBitmap(), null, mDestRect, null);
        }

        if (!mCollBook.getChapterList().isEmpty()) {
            //初始化标题的参数
            //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部的位置
            if (!mSettingManager.getHideStatusBar()) {
                float tipBottom = mDisplayHeight - tipMarginHeight - mTipPaint.getFontMetrics().bottom;
                float tipLeft;
                if (getPageStatus() != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        //绘制标题
                        percent = mCollBook.getChapterList(mCurChapterPos).getDurChapterName();
                        percent = ChapterContentHelp.replaceContent(mCollBook, percent);
                        percent = TextUtils.ellipsize(percent, mTipPaint, mDisplayWidth - tipMarginWidth * 2,
                                TextUtils.TruncateAt.END).toString();
                        canvas.drawText(percent, tipMarginWidth, tipBottom, mTipPaint);
                    }
                } else {
                    //绘制总进度
                    percent = BookshelfHelp.getReadProgress(mCurChapterPos, mCollBook.getChapterListSize(), mCurPage.position, mCurPageList.size());
                    tipLeft = mDisplayWidth - tipMarginWidth - mTipPaint.measureText(percent);
                    canvas.drawText(percent, tipLeft, tipBottom, mTipPaint);
                    //绘制页码
                    if(mPageMode != PageMode.SCROLL) {
                        percent = String.format("%d/%d", mCurPage.position + 1, mCurPageList.size());
                        tipLeft = tipLeft - tipMarginWidth - mTipPaint.measureText(percent);
                        canvas.drawText(percent, tipLeft, tipBottom, mTipPaint);
                    }
                    //绘制标题
                    percent = mCollBook.getChapterList(mCurChapterPos).getDurChapterName();
                    percent = ChapterContentHelp.replaceContent(mCollBook, percent);
                    percent = TextUtils.ellipsize(percent, mTipPaint, tipLeft - tipMarginWidth, TextUtils.TruncateAt.END).toString();
                    canvas.drawText(percent, tipMarginWidth, tipBottom, mTipPaint);
                }
                if (mSettingManager.getShowLine()) {
                    //绘制分隔线
                    tipBottom = mDisplayHeight - ScreenUtils.dpToPx(DEFAULT_MARGIN_HEIGHT);
                    canvas.drawRect(tipMarginWidth, tipBottom, mDisplayWidth - tipMarginWidth, tipBottom + ScreenUtils.dpToPx(1), mTextPaint);
                }
            } else {
                float tipBottom = tipMarginHeight - mTipPaint.getFontMetrics().top;
                if (getPageStatus() != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        //绘制标题
                        percent = mCollBook.getChapterList(mCurChapterPos).getDurChapterName();
                        percent = ChapterContentHelp.replaceContent(mCollBook, percent);
                        percent = TextUtils.ellipsize(percent, mTipPaint, mDisplayWidth - tipMarginWidth * 2,
                                TextUtils.TruncateAt.END).toString();
                        canvas.drawText(percent, tipMarginWidth, tipBottom, mTipPaint);
                    }
                } else {
                    //绘制标题
                    percent = mCollBook.getChapterList(mCurChapterPos).getDurChapterName();
                    percent = ChapterContentHelp.replaceContent(mCollBook, percent);
                    percent = TextUtils.ellipsize(percent, mTipPaint, mDisplayWidth - tipMarginWidth * 2,
                            TextUtils.TruncateAt.END).toString();
                    canvas.drawText(percent, tipMarginWidth, tipBottom, mTipPaint);
                    //绘制页码
                    tipBottom = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
                    percent = String.format("%d/%d", mCurPage.position + 1, mCurPageList.size());
                    canvas.drawText(percent, tipMarginWidth, tipBottom, mTipPaint);
                    //绘制总进度
                    if (mSettingManager.getShowTimeBattery()) {
                        percent = BookshelfHelp.getReadProgress(mCurChapterPos, mCollBook.getChapterListSize(), mCurPage.position, mCurPageList.size());
                        canvas.drawText(percent, (mDisplayWidth - mTipPaint.measureText(percent)) / 2, tipBottom, mTipPaint);
                    } else {
                        float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
                        String readProgress = BookshelfHelp.getReadProgress(mCurChapterPos, mCollBook.getChapterListSize(), mCurPage.position, mCurPageList.size());
                        float x = mDisplayWidth - tipMarginWidth - mTipPaint.measureText(readProgress) - ScreenUtils.dpToPx(4);
                        canvas.drawText(readProgress, x, y, mTipPaint);
                    }
                }
                if (mSettingManager.getShowLine()) {
                    //绘制分隔线
                    tipBottom = ScreenUtils.dpToPx(DEFAULT_MARGIN_HEIGHT - 1);
                    canvas.drawRect(tipMarginWidth, tipBottom, mDisplayWidth - tipMarginWidth, ScreenUtils.dpToPx(DEFAULT_MARGIN_HEIGHT), mTextPaint);
                }
            }
        }

        int visibleRight = mDisplayWidth - tipMarginWidth;
        if (mSettingManager.getHideStatusBar() && mSettingManager.getShowTimeBattery()) {
            //绘制电池
            int visibleBottom = mDisplayHeight - tipMarginHeight - ScreenUtils.dpToPx(2);

            int outFrameWidth = (int) mTipPaint.measureText("xxx");
            int outFrameHeight = (int) mTipPaint.getTextSize() - ScreenUtils.dpToPx(2);

            int polarHeight = ScreenUtils.dpToPx(6);
            int polarWidth = ScreenUtils.dpToPx(2);
            int border = 1;
            int innerMargin = 1;

            //电极的制作
            int polarLeft = visibleRight - polarWidth;
            int polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2;
            Rect polar = new Rect(polarLeft, polarTop, visibleRight,
                    polarTop + polarHeight - ScreenUtils.dpToPx(2));

            mBatteryPaint.setColor(mTextColor);
            mBatteryPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(polar, mBatteryPaint);

            //外框的制作
            int outFrameLeft = polarLeft - outFrameWidth;
            int outFrameTop = visibleBottom - outFrameHeight;
            int outFrameBottom = visibleBottom - ScreenUtils.dpToPx(2);
            Rect outFrame = new Rect(outFrameLeft, outFrameTop, polarLeft, outFrameBottom);

            mBatteryPaint.setStyle(Paint.Style.STROKE);
            mBatteryPaint.setStrokeWidth(border);
            canvas.drawRect(outFrame, mBatteryPaint);

            //内框的制作
            float innerWidth = (outFrame.width() - innerMargin * 2 - border) * (mBatteryLevel / 100.0f);
            RectF innerFrame = new RectF(outFrameLeft + border + innerMargin, outFrameTop + border + innerMargin,
                    outFrameLeft + border + innerMargin + innerWidth, outFrameBottom - border - innerMargin);

            mBatteryPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(innerFrame, mBatteryPaint);

            //绘制当前时间
            //底部的字显示的位置Y
            float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
            String time = StringUtils.dateConvert(System.currentTimeMillis(), Constant.FORMAT_TIME);
            float x = outFrameLeft - mTipPaint.measureText(time) - ScreenUtils.dpToPx(4);
            canvas.drawText(time, x, y, mTipPaint);
        }
    }


    /**
     * 绘制内容
     */
    private void drawContent(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);

        if (mPageMode == PageMode.SCROLL) {
            if (mSettingManager.bgIsColor()) {
                canvas.drawColor(mSettingManager.getBgColor());
            } else {
                Rect mDestRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                canvas.drawBitmap(mSettingManager.getBgBitmap(), null, mDestRect, null);
            }
        }

        if (getPageStatus() != STATUS_FINISH) {
            //绘制字体
            String tip = "";
            switch (getPageStatus()) {
                case STATUS_LOADING:
                    tip = "正在拼命加载中...";
                    break;
                case STATUS_ERROR:
                    tip = String.format("加载失败\n%s", errorMsg);
                    break;
                case STATUS_EMPTY:
                    tip = "文章内容为空";
                    break;
                case STATUS_PARING:
                    tip = "正在排版请等待...";
                    break;
                case STATUS_PARSE_ERROR:
                    tip = "文件解析错误";
                    break;
                case STATUS_CATEGORY_EMPTY:
                    tip = "目录列表为空";
                    break;
                case STATUS_HY:
                    tip = "正在换源请等待...";
            }

            //将提示语句放到正中间
            Layout tempLayout = new StaticLayout(tip, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
            List<String> linesData = new ArrayList<>();
            for (int i = 0; i < tempLayout.getLineCount(); i++) {
                linesData.add(tip.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
            }
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            float textHeight = fontMetrics.top - fontMetrics.bottom;
            float pivotY = (mDisplayHeight - textHeight * linesData.size()) / 2;
            for (String str : linesData) {
                float textWidth = mTextPaint.measureText(str);
                float pivotX = (mDisplayWidth - textWidth) / 2;
                canvas.drawText(str, pivotX, pivotY, mTextPaint);
                pivotY = pivotY + textHeight + mTextInterval;
            }
        } else {
            float top;

            if (mPageMode == PageMode.SCROLL) {
                top = contentMarginHeight - mTextPaint.getFontMetrics().top;
            } else {
                top = mSettingManager.getHideStatusBar()
                        ? mMarginTop + contentMarginHeight - mTextPaint.getFontMetrics().top
                        : mPageView.getStatusBarHeight() + mMarginTop + contentMarginHeight - mTextPaint.getFontMetrics().top;
            }

            //设置总距离
            int interval = mTextInterval + (int) mTextPaint.getTextSize();
            int para = mTextPara + (int) mTextPaint.getTextSize();
            int titleInterval = mTitleInterval + (int) mTitlePaint.getTextSize();
            int titlePara = mTitlePara + (int) mTextPaint.getTextSize();
            String str = null;

            //对标题进行绘制
            for (int i = 0; i < mCurPage.titleLines; ++i) {
                str = mCurPage.lines.get(i);

                //计算文字显示的起始点
                int start = (int) (mDisplayWidth - mTitlePaint.measureText(str)) / 2;
                //进行绘制
                canvas.drawText(str, start, top, mTitlePaint);

                //设置尾部间距
                if (i == mCurPage.titleLines - 1) {
                    top += titlePara;
                } else {
                    //行间距
                    top += titleInterval;
                }
            }

            if (mCurPage.lines == null) {
                return;
            }
            //对内容进行绘制
            for (int i = mCurPage.titleLines; i < mCurPage.lines.size(); ++i) {
                str = mCurPage.lines.get(i);
                Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                //drawScaledText(canvas,str,width,mTextPaint,top);

                if (needScale(str)) {
                    //canvas.drawText(str, mMarginLeft, top, mTextPaint);
                    drawScaledText(canvas, str, width, mTextPaint, top);
                } else {
                    canvas.drawText(str, mMarginLeft, top, mTextPaint);
                }


                //
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
    public void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginLeft - mMarginRight;
        mVisibleHeight = mSettingManager.getHideStatusBar()
                ? mDisplayHeight - mMarginTop - mMarginBottom
                : mDisplayHeight - mMarginTop - mMarginBottom - mPageView.getStatusBarHeight();

        // 重置 PageMode
        mPageView.setPageMode(mPageMode, mMarginTop, mMarginBottom);

        if (!isChapterOpen) {
            // 展示加载界面
            mPageView.drawCurPage();
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (!isFirstOpen) {
                // 打开书籍
                openChapter(mCollBook.getDurChapterPage());
            }
        } else {
            // 如果章节已显示，那么就重新计算页面
            if (getPageStatus() == STATUS_FINISH) {
                dealLoadPageList(mCurChapterPos);
                // 重新设置文章指针的位置
                mCurPage = getCurPage(mCurPage.position);
            }
            mPageView.drawCurPage();
        }
    }

    /**
     * 翻阅上一页
     */
    public boolean prev() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (getPageStatus() == STATUS_FINISH) {
            // 先查看是否存在上一页
            TxtPage prevPage = getPrevPage();
            if (prevPage != null) {
                mCancelPage = mCurPage;
                mCurPage = prevPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasPrevChapter()) {
            return false;
        }

        mCancelPage = mCurPage;
        if (parsePrevChapter()) {
            mCurPage = getPrevLastPage();
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawNextPage();
        return true;
    }

    /**
     * 解析上一章数据
     */
    boolean parsePrevChapter() {
        // 加载上一章数据
        int prevChapter = mCurChapterPos - 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = prevChapter;

        // 当前章缓存为下一章
        if(getChapterPageStatus(mLastChapterPos) == STATUS_FINISH || getChapterPageStatus(mLastChapterPos) == STATUS_EMPTY)
            mNextPageList = mCurPageList;
        else {
            mNextPageList = null;
        }

        // 判断是否具有上一章缓存
        if (mPrePageList != null) {
            mCurPageList = mPrePageList;
            mPrePageList = null;

            // 回调
            chapterChangeCallback();
        } else {
            dealLoadPageList(prevChapter);
            goPagePos = 999;
        }
        return mCurPageList != null;
    }

    /**
     * 判断是否上一章节为空
     */
    private boolean hasPrevChapter() {
        return mCurChapterPos - 1 >= 0;
    }

    /**
     * 翻到下一页
     */
    boolean next() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (getPageStatus() == STATUS_FINISH) {
            // 先查看是否存在下一页
            TxtPage nextPage = getNextPage();
            if (nextPage != null) {
                mCancelPage = mCurPage;
                mCurPage = nextPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasNextChapter()) {
            return false;
        }

        mCancelPage = mCurPage;
        // 解析下一章数据
        if (parseNextChapter() && mCurPageList != null && mCurPageList.size() > 0) {
            mCurPage = mCurPageList.get(0);
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawNextPage();
        return true;
    }

    /**
     * 判断是否到达目录最后一章
     */
    private boolean hasNextChapter() {
        return mCurChapterPos + 1 < mCollBook.getChapterListSize();
    }

    /**
     * 解析数据
     */
    boolean parseCurChapter() {
        dealLoadPageList(mCurChapterPos);
        // 预加载下一页面
        preLoadNextChapter();
        return mCurPageList != null;
    }

    /**
     * 解析下一章数据
     */
    boolean parseNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = nextChapter;

        // 将当前章的页面列表，作为上一章缓存
        if(getChapterPageStatus(mLastChapterPos) == STATUS_FINISH || getChapterPageStatus(mLastChapterPos) == STATUS_EMPTY)
            mPrePageList = mCurPageList;
        else
            mPrePageList = null;

        // 是否下一章数据已经预加载了
        if (mNextPageList != null) {
            mCurPageList = mNextPageList;
            mNextPageList = null;
            // 回调
            chapterChangeCallback();
        } else {
            // 处理页面解析
            dealLoadPageList(nextChapter);
        }
        // 预加载下一页面
        preLoadNextChapter();
        return mCurPageList != null;
    }

    void dealLoadPageList(int chapterPos) {
        try {
            mCurPageList = loadPageList(chapterPos);
            if (mCurPageList != null) {
                if (mCurPageList.isEmpty()) {
                    setChapterPageStatus(chapterPos, STATUS_EMPTY);
                    // 添加一个空数据
                    TxtPage page = new TxtPage();
                    page.lines = new ArrayList<>(1);
                    mCurPageList.add(page);
                } else {
                    setChapterPageStatus(chapterPos, STATUS_FINISH);
                }
            } else {
                setChapterPageStatus(chapterPos, STATUS_LOADING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCurPageList = null;
            setChapterPageStatus(chapterPos, STATUS_ERROR);
            Log.e("MonkeyBook", e.getLocalizedMessage());
            errorMsg = "未知错误";
        }

        // 回调
        chapterChangeCallback();
    }

    private void chapterChangeCallback() {
        if (mPageChangeListener != null) {
            mPageChangeListener.onChapterChange(mCurChapterPos);
            mPageChangeListener.onPageCountChange(mCurPageList != null ? mCurPageList.size() : 0);
        }
    }

    /**
     * 预加载下一章
     */
    private void preLoadNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (!hasNextChapter()
                || !hasChapterData(mCollBook.getChapterList(nextChapter))) {
            return;
        }

        //如果之前正在加载则取消
        if (mPreLoadDisposable != null) {
            mPreLoadDisposable.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<List<TxtPage>>) e -> e.onSuccess(loadPageList(nextChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<TxtPage>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadDisposable = d;
                    }

                    @Override
                    public void onSuccess(List<TxtPage> pages) {
                        mNextPageList = pages;
                    }

                    @Override
                    public void onError(Throwable e) {
                        //无视错误
                    }
                });
    }

    /**
     * 取消翻页
     */
    public void pageCancel() {
        if (mCurPage.position == 0 && mCurChapterPos > mLastChapterPos) { // 加载到下一章取消了
            if (mPrePageList != null) {
                cancelNextChapter();
            } else {
                if (parsePrevChapter()) {
                    mCurPage = getPrevLastPage();
                } else {
                    mCurPage = new TxtPage();
                }
            }
        } else if (mCurPageList == null
                || (mCurPage.position == mCurPageList.size() - 1
                && mCurChapterPos < mLastChapterPos)) {  // 加载上一章取消了

            if (mNextPageList != null) {
                cancelPreChapter();
            } else {
                if (parseNextChapter()) {
                    mCurPage = mCurPageList.get(0);
                } else {
                    mCurPage = new TxtPage();
                }
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            mCurPage = mCancelPage;
        }
    }

    private void cancelNextChapter() {
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;

        mNextPageList = mCurPageList;
        mCurPageList = mPrePageList;
        mPrePageList = null;

        chapterChangeCallback();

        mCurPage = getPrevLastPage();
        mCancelPage = null;
    }

    private void cancelPreChapter() {
        // 重置位置点
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;
        // 重置页面列表
        mPrePageList = mCurPageList;
        mCurPageList = mNextPageList;
        mNextPageList = null;

        chapterChangeCallback();

        mCurPage = getCurPage(0);
        mCancelPage = null;
    }

    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     */
    private List<TxtPage> loadPageList(ChapterListBean chapter, BufferedReader br) {
        //生成的页面
        List<TxtPage> pages = new ArrayList<>();
        //使用流的方式加载
        List<String> lines = new ArrayList<>();
        int rHeight = mVisibleHeight - contentMarginHeight * 2;
        int titleLinesCount = 0;
        try {
            boolean showTitle = true; // 是否展示标题
            String paragraph = chapter.getDurChapterName() + "\n"; //默认展示标题
            if (mCollBook.getTag().equals(LOCAL_TAG)) {
                br.readLine();
            }
            if (!mSettingManager.getShowTitle()) {
                showTitle = false;
                paragraph = null;
            }
            while (showTitle || (paragraph = br.readLine()) != null) {
                paragraph = ChapterContentHelp.replaceContent(mCollBook, paragraph);
                paragraph = ChapterContentHelp.toTraditional(mSettingManager, paragraph);
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph.replaceAll("\\s", " ").trim();
                    // 如果只有换行符，那么就不执行
                    if (paragraph.equals("")) continue;
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
                setChapterPageStatus(chapter.getDurChapterIndex(), STATUS_FINISH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(br);
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

    /**
     * 获取初始显示的页面
     */
    private TxtPage getCurPage(int pos) {
        if (mCurPageList != null && mCurPageList.size() > 0) {
            pos = pos < 0 ? 0 : pos;
            pos = pos > mCurPageList.size() - 1 ? mCurPageList.size() - 1 : pos;
            return mCurPageList.get(pos);
        }
        return null;
    }

    /**
     * 获取上一个页面
     */
    private TxtPage getPrevPage() {
        int pos = mCurPage.position - 1;
        if (pos >= 0 && mCurPageList.size() > 0) {
            return mCurPageList.get(pos);
        }
        return null;
    }

    /**
     * 获取下一的页面
     */
    private TxtPage getNextPage() {
        int pos = mCurPage.position + 1;
        if (pos < mCurPageList.size() && mCurPageList.size() > 0) {
            return mCurPageList.get(pos);
        }
        return null;
    }

    /**
     * 获取上一个章节的最后一页
     */
    private TxtPage getPrevLastPage() {
        int pos = mCurPageList.size() - 1;
        return mCurPageList.get(pos);
    }

    /**
     * 根据当前状态，决定是否能够翻页
     */
    private boolean canTurnPage() {

        if (!isChapterListPrepare) {
            return false;
        }

        if (getPageStatus() == STATUS_PARSE_ERROR || getPageStatus() == STATUS_PARING) {
            return false;
        }
        return true;
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
         * 作用：请求加载章节内容
         *
         * @param chapterIndex:需要下载的章节
         */
        void requestChapters(int chapterIndex);

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
