package com.monke.monkeybook.widget.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.utils.BitmapUtil;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

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
    // 默认的显示参数配置
    private static final int DEFAULT_MARGIN_HEIGHT = 28;
    private static final int DEFAULT_MARGIN_WIDTH = 15;
    private static final int DEFAULT_TIP_SIZE = 12;
    private static final int EXTRA_TITLE_SIZE = 4;

    // 当前章节列表
    protected List<ChapterListBean> mChapterList;
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
    // 绘制提示的画笔
    private Paint mTipPaint;
    // 绘制标题的画笔
    private Paint mTitlePaint;
    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private Paint mBgPaint;
    // 绘制小说内容的画笔
    private TextPaint mTextPaint;
    // 阅读器的配置选项
    private ReadBookControl mSettingManager;
    // 被遮盖的页，或者认为被取消显示的页
    private TxtPage mCancelPage;


    private Disposable mPreLoadDisp;

    /*****************params**************************/
    // 当前的状态
    protected int mStatus = STATUS_LOADING;
    // 判断章节列表是否加载完成
    protected boolean isChapterListPrepare;

    // 是否打开过章节
    private boolean isChapterOpen;
    private boolean isFirstOpen = true;
    private boolean isClose;
    // 页面的翻页效果模式
    private PageMode mPageMode;
    // 加载器的颜色主题
    private PageStyle mPageStyle;
    //当前是否是夜间模式
    private boolean isNightMode;
    //书籍绘制区域的宽高
    private int mVisibleWidth;
    private int mVisibleHeight;
    //应用的宽高
    private int mDisplayWidth;
    private int mDisplayHeight;
    //间距
    private int mMarginWidth;
    private int mMarginHeight;
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
        mChapterList = new ArrayList<>(1);

        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        // 初始化PageView
        initPageView();
        // 初始化书籍
        prepareBook();
    }

    private void initData() {
        // 获取配置管理器
        mSettingManager = ReadBookControl.getInstance();
        // 获取配置参数
        mPageMode = mSettingManager.getPageMode(mSettingManager.getPageMode());
        mPageStyle = PageStyle.values()[1];
        // 初始化参数
        mMarginWidth = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH);
        mMarginHeight = ScreenUtils.dpToPx(DEFAULT_MARGIN_HEIGHT);
        // 配置文字有关的参数
        setUpTextParams(mSettingManager.getTextSize(), mSettingManager.getLineMultiplier());
    }

    /**
     * 作用：设置与文字相关的参数
     *
     * @param textSize
     */
    private void setUpTextParams(int textSize, float textLine) {
        // 文字大小
        mTextSize = textSize;
        mTitleSize = mTextSize + ScreenUtils.spToPx(EXTRA_TITLE_SIZE);
        // 行间距(大小为字体的一半)
        mTextInterval = (int) (mTextSize / 2 * textLine);
        mTitleInterval = (int) (mTitleSize / 2 * textLine);
        // 段落间距(大小为字体的高度)
        mTextPara = (int) (mTextSize * textLine);
        mTitlePara = (int) (mTitleSize * textLine);
    }

    private void initPaint() {
        // 绘制提示的画笔
        mTipPaint = new Paint();
        mTipPaint.setColor(mTextColor);
        mTipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        mTipPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE)); // Tip默认的字体大小
        mTipPaint.setAntiAlias(true);
        mTipPaint.setSubpixelText(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);

        // 绘制标题的画笔
        mTitlePaint = new TextPaint();
        mTitlePaint.setColor(mTextColor);
        mTitlePaint.setTextSize(mTitleSize);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTitlePaint.setAntiAlias(true);

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
        mPageView.setPageMode(mPageMode);
        mPageView.setBgColor(mBgColor);
    }

    /****************************** public method***************************/
    /**
     * 跳转到上一章
     *
     * @return
     */
    public boolean skipPreChapter() {
        goPagePos = 0;
        if (!hasPrevChapter()) {
            return false;
        }

        // 载入上一章。
        if (parsePrevChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 跳转到下一章
     *
     * @return
     */
    public boolean skipNextChapter() {
        goPagePos = 0;
        if (!hasNextChapter()) {
            return false;
        }

        //判断是否达到章节的终止点
        if (parseNextChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawCurPage(false);
        return true;
    }

    public void skipToChapter(int chapterPos, int pagePos) {
        goPagePos = pagePos;
        // 设置参数
        mCurChapterPos = chapterPos;

        // 将上一章的缓存设置为null
        mPrePageList = null;
        // 如果当前下一章缓存正在执行，则取消
        if (mPreLoadDisp != null) {
            mPreLoadDisp.dispose();
        }
        // 将下一章缓存设置为null
        mNextPageList = null;

        // 打开指定章节
        openChapter(pagePos);
    }

    /**
     * 跳转到指定章节
     *
     * @param pos:从 0 开始。
     */
    public void skipToChapter(int pos) {
        goPagePos = 0;
        // 设置参数
        mCurChapterPos = pos;

        // 将上一章的缓存设置为null
        mPrePageList = null;
        // 如果当前下一章缓存正在执行，则取消
        if (mPreLoadDisp != null) {
            mPreLoadDisp.dispose();
        }
        // 将下一章缓存设置为null
        mNextPageList = null;

        // 打开指定章节
        openChapter(0);
    }

    /**
     * 跳转到指定的页
     *
     * @param pos
     */
    public boolean skipToPage(int pos) {
        if (!isChapterListPrepare) {
            return false;
        }
        mCurPage = getCurPage(pos);
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 翻到上一页
     *
     * @return
     */
    public boolean skipToPrePage() {
        return mPageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     *
     * @return
     */
    public boolean skipToNextPage() {
        return mPageView.autoNextPage();
    }

    /**
     * 更新时间
     */
    public void updateTime() {
        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true);
        }
    }

    /**
     * 更新电量
     *
     * @param level
     */
    public void updateBattery(int level) {
        mBatteryLevel = level;

        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true);
        }
    }

    /**
     * 设置提示的文字大小
     *
     * @param textSize:单位为 px。
     */
    public void setTipTextSize(int textSize) {
        mTipPaint.setTextSize(textSize);

        // 如果屏幕大小加载完成
        mPageView.drawCurPage(false);
    }

    /**
     * 设置文字相关参数
     *
     * @param textSize
     */
    public void setTextSize(int textSize, float lineSize) {
        // 设置文字相关参数
        setUpTextParams(textSize, lineSize);

        // 设置画笔的字体大小
        mTextPaint.setTextSize(mTextSize);
        // 设置标题的字体大小
        mTitlePaint.setTextSize(mTitleSize);
        // 取消缓存
        mPrePageList = null;
        mNextPageList = null;

        // 如果当前已经显示数据
        if (isChapterListPrepare && mStatus == STATUS_FINISH) {
            // 重新计算当前页面
            dealLoadPageList(mCurChapterPos);

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (mCurPage.position >= mCurPageList.size()) {
                mCurPage.position = mCurPageList.size() - 1;
            }

            // 重新获取指定页面
            mCurPage = mCurPageList.get(mCurPage.position);
        }

        mPageView.drawCurPage(false);
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

        mPageView.drawCurPage(false);
    }

    /**
     * 翻页动画
     *
     * @param pageMode:翻页模式
     * @see PageMode
     */
    public void setPageMode(PageMode pageMode) {
        mPageMode = pageMode;

        mPageView.setPageMode(mPageMode);

        // 重新绘制当前页
        mPageView.drawCurPage(false);
    }

    /**
     * 设置内容与屏幕的间距
     *
     * @param marginWidth  :单位为 px
     * @param marginHeight :单位为 px
     */
    public void setMargin(int marginWidth, int marginHeight) {
        mMarginWidth = marginWidth;
        mMarginHeight = marginHeight;

        // 如果是滑动动画，则需要重新创建了
        if (mPageMode == PageMode.SCROLL) {
            mPageView.setPageMode(PageMode.SCROLL);
        }

        mPageView.drawCurPage(false);
    }

    /**
     * 设置页面切换监听
     *
     * @param listener
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;

        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            mPageChangeListener.onCategoryFinish(mChapterList);
        }
    }

    /**
     * 获取当前页的状态
     *
     * @return
     */
    public int getPageStatus() {
        return mStatus;
    }

    /**
     * 获取书籍信息
     *
     * @return
     */
    public BookShelfBean getCollBook() {
        return mCollBook;
    }

    /**
     * 获取章节目录。
     *
     * @return
     */
    public List<ChapterListBean> getChapterCategory() {
        return mChapterList;
    }

    /**
     * 获取当前页的页码
     *
     * @return
     */
    public int getPagePos() {
        return mCurPage.position;
    }

    /**
     * 获取当前章节的章节位置
     *
     * @return
     */
    public int getChapterPos() {
        return mCurChapterPos;
    }

    /**
     * 获取距离屏幕的高度
     *
     * @return
     */
    public int getMarginHeight() {
        return mMarginHeight;
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
    public void openChapter(int pagePos) {

        isFirstOpen = false;

        if (!mPageView.isPrepare()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mStatus = STATUS_LOADING;
            mPageView.drawCurPage(false);
            return;
        }

        // 如果获取到的章节目录为空
        if (mChapterList.isEmpty()) {
            mStatus = STATUS_CATEGORY_EMPTY;
            mPageView.drawCurPage(false);
            return;
        }

        if (goPagePos != 0) {
            pagePos = goPagePos;
            goPagePos = 0;
            isChapterOpen = false;
        }

        if (parseCurChapter()) {
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
        } else {
            mCurPage = new TxtPage();
        }
        mPageChangeListener.onPageChange(mCurChapterPos, mCurPage.position);
        mPageView.drawCurPage(false);
    }

    public void chapterError() {
        //加载错误
        mStatus = STATUS_ERROR;
        mPageView.drawCurPage(false);
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        isChapterListPrepare = false;
        isClose = true;

        if (mPreLoadDisp != null) {
            mPreLoadDisp.dispose();
        }

        clearList(mChapterList);
        clearList(mCurPageList);
        clearList(mNextPageList);

        mChapterList = null;
        mCurPageList = null;
        mNextPageList = null;
        mPageView = null;
        mCurPage = null;
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

    public String getContext() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < mCurPage.lines.size(); i++) {
            s.append(mCurPage.lines.get(i));
        }
        return s.toString();
    }

    /**
     * 加载页面列表
     *
     * @param chapterPos:章节序号
     * @return
     */
    private List<TxtPage> loadPageList(int chapterPos) throws Exception {
        // 获取章节
        ChapterListBean chapter = mChapterList.get(chapterPos);
        // 判断章节是否存在
        if (!hasChapterData(chapter)) {
            return null;
        }
        // 获取章节的文本流
        BufferedReader reader = getChapterReader(chapter);
        List<TxtPage> chapters = loadPages(chapter, reader);
        return chapters;
    }

    /*******************************abstract method***************************************/

    /**
     * 刷新章节列表
     */
    public abstract void refreshChapterList();

    /**
     * 获取章节的文本流
     *
     * @param chapter
     * @return
     */
    protected abstract BufferedReader getChapterReader(ChapterListBean chapter) throws Exception;

    /**
     * 章节数据是否存在
     *
     * @return
     */
    protected abstract boolean hasChapterData(ChapterListBean chapter);

    /***********************************default method***********************************************/

    void drawPage(Bitmap bitmap, boolean isUpdate) {
        drawBackground(mPageView.getBgBitmap(), isUpdate);
        if (!isUpdate) {
            drawContent(bitmap);
        }
        //更新绘制
        mPageView.invalidate();
    }

    private void drawBackground(Bitmap bitmap, boolean isUpdate) {
        Canvas canvas = new Canvas(bitmap);
        int tipMarginHeight = ScreenUtils.dpToPx(3);
        if (!isUpdate) {
            /****绘制背景****/
            if (mSettingManager.bgIsColor()) {
                canvas.drawColor(mSettingManager.getBgColor());
            } else {
                Bitmap mBgBitmap = mSettingManager.getBgBitmap();
                mBgBitmap = BitmapUtil.scaleImage(mBgBitmap, bitmap.getWidth(), bitmap.getHeight());
                canvas.drawBitmap(mBgBitmap,0, 0, null);
            }

            if (!mChapterList.isEmpty()) {
                /*****初始化标题的参数********/
                //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部的位置
                float tipTop = mSettingManager.getHideStatusBar()
                        ? tipMarginHeight - mTipPaint.getFontMetrics().top
                        : mPageView.getStatusBarHeight() + tipMarginHeight - mTipPaint.getFontMetrics().top;
                //根据状态不一样，数据不一样
                if (mStatus != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        canvas.drawText(mChapterList.get(mCurChapterPos).getDurChapterName()
                                , mMarginWidth, tipTop, mTipPaint);
                    }
                } else {
                    canvas.drawText(mCurPage.title, mMarginWidth, tipTop, mTipPaint);
                }

                /******绘制页码********/
                // 底部的字显示的位置Y
                float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
                // 只有finish的时候采用页码
                if (mStatus == STATUS_FINISH) {
                    String percent = (mCurPage.position + 1) + "/" + mCurPageList.size();
                    canvas.drawText(percent, mMarginWidth, y, mTipPaint);
                }
            }
        } else {
            //擦除区域
            mBgPaint.setColor(mBgColor);
            canvas.drawRect(mDisplayWidth / 2, mDisplayHeight - mMarginHeight + ScreenUtils.dpToPx(2), mDisplayWidth, mDisplayHeight, mBgPaint);
        }

        /******绘制电池********/

        int visibleRight = mDisplayWidth - mMarginWidth;
        int visibleBottom = mDisplayHeight - tipMarginHeight;

        int outFrameWidth = (int) mTipPaint.measureText("xxx");
        int outFrameHeight = (int) mTipPaint.getTextSize();

        int polarHeight = ScreenUtils.dpToPx(6);
        int polarWidth = ScreenUtils.dpToPx(2);
        int border = 1;
        int innerMargin = 1;

        //电极的制作
        int polarLeft = visibleRight - polarWidth;
        int polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2;
        Rect polar = new Rect(polarLeft, polarTop, visibleRight,
                polarTop + polarHeight - ScreenUtils.dpToPx(2));

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

        /******绘制当前时间********/
        //底部的字显示的位置Y
        float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
        String time = StringUtils.dateConvert(System.currentTimeMillis(), Constant.FORMAT_TIME);
        float x = outFrameLeft - mTipPaint.measureText(time) - ScreenUtils.dpToPx(4);
        canvas.drawText(time, x, y, mTipPaint);
    }

    private void drawContent(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);

        if (mPageMode == PageMode.SCROLL) {
            canvas.drawColor(mBgColor);
        }
        /******绘制内容****/

        if (mStatus != STATUS_FINISH) {
            //绘制字体
            String tip = "";
            switch (mStatus) {
                case STATUS_LOADING:
                    tip = "正在拼命加载中...";
                    break;
                case STATUS_ERROR:
                    tip = "加载失败(点击边缘重试)";
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
            }

            //将提示语句放到正中间
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            float textHeight = fontMetrics.top - fontMetrics.bottom;
            float textWidth = mTextPaint.measureText(tip);
            float pivotX = (mDisplayWidth - textWidth) / 2;
            float pivotY = (mDisplayHeight - textHeight) / 2;
            canvas.drawText(tip, pivotX, pivotY, mTextPaint);
        } else {
            float top;

            if (mPageMode == PageMode.SCROLL) {
                top = -mTextPaint.getFontMetrics().top;
            } else {
                top = mSettingManager.getHideStatusBar() ? mMarginHeight - mTextPaint.getFontMetrics().top
                        : mPageView.getStatusBarHeight() + mMarginHeight - mTextPaint.getFontMetrics().top;
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

                //设置顶部间距
                if (i == 0) {
                    top += mTitlePara;
                }

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

            //对内容进行绘制
            for (int i = mCurPage.titleLines; i < mCurPage.lines.size(); ++i) {
                str = mCurPage.lines.get(i);

                canvas.drawText(str, mMarginWidth, top, mTextPaint);
                if (str.endsWith("\n")) {
                    top += para;
                } else {
                    top += interval;
                }
            }
        }
    }

    void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginWidth * 2;
        mVisibleHeight = mSettingManager.getHideStatusBar() ? mDisplayHeight - mMarginHeight * 2
                : mDisplayHeight - mMarginHeight * 2 - mPageView.getStatusBarHeight();

        // 重置 PageMode
        mPageView.setPageMode(mPageMode);

        if (!isChapterOpen) {
            // 展示加载界面
            mPageView.drawCurPage(false);
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (!isFirstOpen) {
                // 打开书籍
                openChapter(mCollBook.getDurChapterPage());
            }
        } else {
            // 如果章节已显示，那么就重新计算页面
            if (mStatus == STATUS_FINISH) {
                dealLoadPageList(mCurChapterPos);
                // 重新设置文章指针的位置
                mCurPage = getCurPage(mCurPage.position);
            }
            mPageView.drawCurPage(false);
        }
    }

    /**
     * 翻阅上一页
     *
     * @return
     */
    boolean prev() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (mStatus == STATUS_FINISH) {
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
     * @return:数据是否解析成功
     */
    boolean parsePrevChapter() {
        // 加载上一章数据
        int prevChapter = mCurChapterPos - 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = prevChapter;

        // 当前章缓存为下一章
        mNextPageList = mCurPageList;

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

    private boolean hasPrevChapter() {
        //判断是否上一章节为空
        if (mCurChapterPos - 1 < 0) {
            return false;
        }
        return true;
    }

    /**
     * 翻到下一页
     *
     * @return:是否允许翻页
     */
    boolean next() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (mStatus == STATUS_FINISH) {
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
        if (parseNextChapter()) {
            mCurPage = mCurPageList.get(0);
            mPageChangeListener.onPageChange(mCurChapterPos, mCurPage.position);
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawNextPage();
        return true;
    }

    private boolean hasNextChapter() {
        // 判断是否到达目录最后一章
        if (mCurChapterPos + 1 >= mChapterList.size()) {
            return false;
        }
        return true;
    }

    boolean parseCurChapter() {
        // 解析数据
        dealLoadPageList(mCurChapterPos);
        // 预加载下一页面
        preLoadNextChapter();
        return mCurPageList != null ? true : false;
    }

    /**
     * 解析下一章数据
     *
     * @return:返回解析成功还是失败
     */
    boolean parseNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = nextChapter;

        // 将当前章的页面列表，作为上一章缓存
        mPrePageList = mCurPageList;

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

    private void dealLoadPageList(int chapterPos) {
        try {
            mCurPageList = loadPageList(chapterPos);
            if (mCurPageList != null) {
                if (mCurPageList.isEmpty()) {
                    mStatus = STATUS_EMPTY;

                    // 添加一个空数据
                    TxtPage page = new TxtPage();
                    page.lines = new ArrayList<>(1);
                    mCurPageList.add(page);
                } else {
                    mStatus = STATUS_FINISH;
                }
            } else {
                mStatus = STATUS_LOADING;
            }
        } catch (Exception e) {
            e.printStackTrace();

            mCurPageList = null;
            mStatus = STATUS_ERROR;
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

    // 预加载下一章
    private void preLoadNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (!hasNextChapter()
                || !hasChapterData(mChapterList.get(nextChapter))) {
            return;
        }

        //如果之前正在加载则取消
        if (mPreLoadDisp != null) {
            mPreLoadDisp.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<List<TxtPage>>) e -> e.onSuccess(loadPageList(nextChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<TxtPage>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadDisp = d;
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

    // 取消翻页
    void pageCancel() {
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

    /**************************************private method********************************************/
    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     * @return
     */
    private List<TxtPage> loadPages(ChapterListBean chapter, BufferedReader br) {
        //生成的页面
        List<TxtPage> pages = new ArrayList<>();
        //使用流的方式加载
        List<String> lines = new ArrayList<>();
        int rHeight = mVisibleHeight;
        int titleLinesCount = 0;
        boolean showTitle = true; // 是否展示标题
        String paragraph = chapter.getDurChapterName();//默认展示标题
        try {
            while (showTitle || (paragraph = br.readLine()) != null) {
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph.replaceAll("\\s", "");
                    // 如果只有换行符，那么就不执行
                    if (paragraph.equals("")) continue;
                    paragraph = StringUtils.halfToFull("  " + paragraph + "\n");
                } else {
                    //设置 title 的顶部间距
                    rHeight -= mTitlePara;
                }
                int wordCount = 0;
                String subStr = null;
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
                        rHeight = mVisibleHeight;
                        titleLinesCount = 0;

                        continue;
                    }

                    //测量一行占用的字节数
                    if (showTitle) {
                        wordCount = mTitlePaint.breakText(paragraph,
                                true, mVisibleWidth, null);
                    } else {
                        wordCount = mTextPaint.breakText(paragraph,
                                true, mVisibleWidth, null);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(br);
        }
        return pages;
    }


    /**
     * @return:获取初始显示的页面
     */
    private TxtPage getCurPage(int pos) {
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(mCurChapterPos, pos);
        }
        return mCurPageList.get(pos);
    }

    /**
     * @return:获取上一个页面
     */
    private TxtPage getPrevPage() {
        int pos = mCurPage.position - 1;
        if (pos < 0) {
            return null;
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(mCurChapterPos, pos);
        }
        return mCurPageList.get(pos);
    }

    /**
     * @return:获取下一的页面
     */
    private TxtPage getNextPage() {
        int pos = mCurPage.position + 1;
        if (pos >= mCurPageList.size()) {
            return null;
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(mCurChapterPos, pos);
        }
        return mCurPageList.get(pos);
    }

    /**
     * @return:获取上一个章节的最后一页
     */
    private TxtPage getPrevLastPage() {
        int pos = mCurPageList.size() - 1;

        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(mCurChapterPos, pos);
        }

        return mCurPageList.get(pos);
    }

    /**
     * 根据当前状态，决定是否能够翻页
     *
     * @return
     */
    private boolean canTurnPage() {

        if (!isChapterListPrepare) {
            return false;
        }

        if (mStatus == STATUS_PARSE_ERROR
                || mStatus == STATUS_PARING) {
            return false;
        } else if (mStatus == STATUS_ERROR) {
            mStatus = STATUS_LOADING;
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
