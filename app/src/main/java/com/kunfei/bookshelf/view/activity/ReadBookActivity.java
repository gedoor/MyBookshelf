//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.AppActivityManager;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.ChapterContentHelp;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.ReadBookContract;
import com.kunfei.bookshelf.service.ReadAloudService;
import com.kunfei.bookshelf.utils.BatteryUtil;
import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.SystemUtil;
import com.kunfei.bookshelf.utils.bar.BarHide;
import com.kunfei.bookshelf.utils.bar.ImmersionBar;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.popupwindow.CheckAddShelfPop;
import com.kunfei.bookshelf.view.popupwindow.MoreSettingPop;
import com.kunfei.bookshelf.view.popupwindow.ReadAdjustPop;
import com.kunfei.bookshelf.view.popupwindow.ReadBottomMenu;
import com.kunfei.bookshelf.view.popupwindow.ReadInterfacePop;
import com.kunfei.bookshelf.widget.modialog.EditBookmarkView;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;
import com.kunfei.bookshelf.widget.page.PageLoader;
import com.kunfei.bookshelf.widget.page.PageView;
import com.kunfei.bookshelf.widget.page.TxtChapter;
import com.kunfei.bookshelf.widget.page.animation.PageAnimation;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 阅读界面
 */
public class ReadBookActivity extends MBaseActivity<ReadBookContract.Presenter> implements ReadBookContract.View {

    @BindView(R.id.fl_content)
    FrameLayout flContent;
    @BindView(R.id.fl_menu)
    FrameLayout flMenu;
    @BindView(R.id.v_menu_bg)
    View vMenuBg;
    @BindView(R.id.ll_menu_bottom)
    ReadBottomMenu llMenuBottom;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_chapter_name)
    TextView tvChapterName;
    @BindView(R.id.tv_chapter_url)
    TextView tvUrl;
    @BindView(R.id.atv_line)
    View atvLine;
    @BindView(R.id.ll_menu_top)
    LinearLayout llMenuTop;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.ll_ISB)
    LinearLayout llISB;
    @BindView(R.id.pageView)
    PageView pageView;
    @BindView(R.id.readAdjustPop)
    ReadAdjustPop readAdjustPop;
    @BindView(R.id.readInterfacePop)
    ReadInterfacePop readInterfacePop;
    @BindView(R.id.moreSettingPop)
    MoreSettingPop moreSettingPop;
    @BindView(R.id.pb_nextPage)
    ProgressBar progressBarNextPage;

    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;
    private ActionBar actionBar;
    private PageLoader mPageLoader;
    private Handler mHandler  = new Handler();
    private Runnable autoPageRunnable;
    private Runnable keepScreenRunnable;
    private Runnable upHpbNextPage;
    private int nextPageTime;
    private String noteUrl;
    private Boolean isAdd = false; //判断是否已经添加进书架
    private ReadAloudService.Status aloudStatus = ReadAloudService.Status.STOP;
    private int screenTimeOut;
    private int upHpbInterval = 100;
    private Menu menu;
    private CheckAddShelfPop checkAddShelfPop;
    private MoDialogHUD moDialogHUD;
    private ThisBatInfoReceiver batInfoReceiver;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    private Boolean showCheckPermission = false;
    private boolean autoPage = false;
    private boolean aloudNextPage;

    @Override
    protected ReadBookContract.Presenter initInjector() {
        return new ReadBookPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            noteUrl = savedInstanceState.getString("noteUrl");
            isAdd = savedInstanceState.getBoolean("isAdd");
        }
        readBookControl.initTextDrawableIndex();
        super.onCreate(savedInstanceState);
        screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[readBookControl.getScreenTimeOut()];
        keepScreenRunnable = this::unKeepScreenOn;
        autoPageRunnable = this::nextPage;
        upHpbNextPage = this::upHpbNextPage;
    }

    @Override
    protected void onCreateActivity() {
        setOrientation(readBookControl.getScreenDirection());
        setContentView(R.layout.activity_book_read);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPresenter.getBookShelf() != null) {
            outState.putString("noteUrl", mPresenter.getBookShelf().getNoteUrl());
            outState.putBoolean("isAdd", isAdd);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            initImmersionBar();
        }
    }

    /**
     * 状态栏
     */
    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        if (readBookControl.getHideNavigationBar()) {
            mImmersionBar.fullScreen(true);
            if (ImmersionBar.hasNavigationBar(this)) {
                llMenuBottom.setNavigationBar(ImmersionBar.getNavigationBarHeight(this));
            }
        }

        if (readBookControl.getHideStatusBar()) {
            progressBarNextPage.setY(0);
        } else {
            progressBarNextPage.setY(ImmersionBar.getStatusBarHeight(this));
        }

        if (llMenuBottom.getVisibility() == View.VISIBLE) {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
            changeNavigationBarColor(false);
        } else {
            if (!isImmersionBarEnabled()) {
                mImmersionBar.statusBarDarkFont(false);
            } else if (readBookControl.getDarkStatusIcon()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }

            if (readBookControl.getHideStatusBar() && readBookControl.getHideNavigationBar()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_BAR);
            } else if (readBookControl.getHideStatusBar()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_STATUS_BAR);
                changeNavigationBarColor(true);
            } else if (readBookControl.getHideNavigationBar()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
            } else {
                mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
                changeNavigationBarColor(true);
            }

        }

        mImmersionBar.init();
        screenOffTimerStart();
    }

    /**
     * 修改导航栏颜色
     */
    private void changeNavigationBarColor(boolean hideBarDivider) {
        if (hideBarDivider) {
            mImmersionBar.hideBarDivider();
        } else {
            mImmersionBar.showBarDivider();
        }
        int barColorType = readBookControl.getNavbarColor();
        if (llMenuBottom.getVisibility() == View.VISIBLE
                || readAdjustPop.getVisibility() == View.VISIBLE
                || readInterfacePop.getVisibility() == View.VISIBLE
                || moreSettingPop.getVisibility() == View.VISIBLE) {
            barColorType = 0;
        }
        switch (barColorType) {
            case 1:
                mImmersionBar.navigationBarDarkFont(false, 0.2f);
                mImmersionBar.navigationBarColor(R.color.black);
                break;
            case 2:
                mImmersionBar.navigationBarDarkFont(true, 0.2f);
                mImmersionBar.navigationBarColor(R.color.white);
                break;
            case 3:
                mImmersionBar.navigationBarDarkFont(readBookControl.getDarkStatusIcon(), 0.2f);
                mImmersionBar.navigationBarColorInt(readBookControl.getBgColor());
                break;
            default:

                break;
        }
    }

    private void unKeepScreenOn() {
        keepScreenOn(false);
    }

    public void keepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 重置黑屏时间
     */
    private void screenOffTimerStart() {
        if (screenTimeOut < 0) {
            keepScreenOn(true);
            return;
        }
        int screenOffTime = screenTimeOut * 1000 - SystemUtil.getScreenOffTime(this);
        if (screenOffTime > 0) {
            mHandler.removeCallbacks(keepScreenRunnable);
            keepScreenOn(true);
            mHandler.postDelayed(keepScreenRunnable, screenOffTime);
        } else {
            keepScreenOn(false);
        }
    }

    /**
     * 自动翻页
     */
    private void autoPage() {
        mHandler.removeCallbacks(upHpbNextPage);
        mHandler.removeCallbacks(autoPageRunnable);
        if (autoPage) {
            progressBarNextPage.setVisibility(View.VISIBLE);
            nextPageTime = readBookControl.getClickSensitivity() * 1000;
            progressBarNextPage.setMax(nextPageTime);
            mHandler.postDelayed(upHpbNextPage, upHpbInterval);
            mHandler.postDelayed(autoPageRunnable, nextPageTime);
        } else {
            progressBarNextPage.setVisibility(View.INVISIBLE);
        }
        llMenuBottom.setAutoPage(autoPage);
    }

    private void upHpbNextPage() {
        nextPageTime = nextPageTime - upHpbInterval;
        progressBarNextPage.setProgress(nextPageTime);
        mHandler.postDelayed(upHpbNextPage, upHpbInterval);
    }

    private void autoPageStop() {
        autoPage = false;
        autoPage();
    }

    private void nextPage() {
        runOnUiThread(() -> {
            screenOffTimerStart();
            if (mPageLoader != null) {
                mPageLoader.skipToNextPage();
            }
        });
    }

    @Override
    protected void initData() {
        mPresenter.saveProgress();
        //显示菜单
        menuTopIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_in);
        menuBottomIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_in);
        menuBottomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                initImmersionBar();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vMenuBg.setOnClickListener(v -> popMenuOut());
                initImmersionBar();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //隐藏菜单
        menuTopOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_out);
        menuBottomOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_out);
        menuBottomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                vMenuBg.setOnClickListener(null);
                initImmersionBar();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flMenu.setVisibility(View.INVISIBLE);
                llMenuTop.setVisibility(View.INVISIBLE);
                llMenuBottom.setVisibility(View.INVISIBLE);
                readAdjustPop.setVisibility(View.INVISIBLE);
                readInterfacePop.setVisibility(View.INVISIBLE);
                moreSettingPop.setVisibility(View.INVISIBLE);
                initImmersionBar();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        mPresenter.initData(this);
        appBar.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
        appBar.setBackgroundColor(ThemeStore.primaryColor(this));
        llMenuBottom.setFabNightTheme(isNightTheme());
        //弹窗
        moDialogHUD = new MoDialogHUD(this);
        initBottomMenu();
        initReadInterfacePop();
        initReadAdjustPop();
        initMoreSettingPop();
        pageView.setBackground(readBookControl.getTextBackground(this));
    }

    /**
     * 底部菜单
     */
    private void initBottomMenu() {
        llMenuBottom.setListener(new ReadBottomMenu.OnMenuListener() {
            @Override
            public void skipToPage(int page) {
                if (mPageLoader != null) {
                    mPageLoader.skipToPage(page);
                }
            }

            @Override
            public void onMediaButton() {
                ReadBookActivity.this.onMediaButton();
            }

            @Override
            public void autoPage() {
                if (ReadAloudService.running) {
                    ReadBookActivity.this.toast(R.string.aloud_can_not_auto_page);
                    return;
                }
                ReadBookActivity.this.autoPage = !ReadBookActivity.this.autoPage;
                ReadBookActivity.this.autoPage();
            }

            @Override
            public void setNightTheme() {
                ReadBookActivity.this.setNightTheme(!isNightTheme());
            }

            @Override
            public void skipPreChapter() {
                if (mPresenter.getBookShelf() != null) {
                    mPageLoader.skipPreChapter();
                }
            }

            @Override
            public void skipNextChapter() {
                if (mPresenter.getBookShelf() != null) {
                    mPageLoader.skipNextChapter();
                }
            }

            @Override
            public void openReplaceRule() {
                popMenuOut();
                ReplaceRuleActivity.startThis(ReadBookActivity.this);
            }

            @Override
            public void openChapterList() {
                ReadBookActivity.this.popMenuOut();
                if (mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().realChapterListEmpty()) {
                    mHandler.postDelayed(() -> ChapterListActivity.startThis(ReadBookActivity.this, mPresenter.getBookShelf()), menuTopOut.getDuration());
                }
            }

            @Override
            public void openAdjust() {
                ReadBookActivity.this.popMenuOut();
                mHandler.postDelayed(ReadBookActivity.this::readAdjustIn, menuBottomOut.getDuration() + 100);
            }

            @Override
            public void openReadInterface() {
                ReadBookActivity.this.popMenuOut();
                mHandler.postDelayed(ReadBookActivity.this::readInterfaceIn, menuBottomOut.getDuration() + 100);
            }

            @Override
            public void openMoreSetting() {
                ReadBookActivity.this.popMenuOut();
                mHandler.postDelayed(ReadBookActivity.this::moreSettingIn, menuBottomOut.getDuration() + 100);
            }

            @Override
            public void toast(int id) {
                ReadBookActivity.this.toast(id);
            }

            @Override
            public void dismiss() {
                popMenuOut();
            }
        });
    }


    /**
     * 调节
     */
    private void initReadAdjustPop() {
        readAdjustPop.setListener(this, new ReadAdjustPop.OnAdjustListener() {
            @Override
            public void changeSpeechRate(int speechRate) {
                if (ReadAloudService.running) {
                    ReadAloudService.pause(ReadBookActivity.this);
                    ReadAloudService.resume(ReadBookActivity.this);
                }
            }

            @Override
            public void speechRateFollowSys() {
                if (ReadAloudService.running) {
                    ReadAloudService.stop(ReadBookActivity.this);
                }
            }
        });
    }

    /**
     * 界面设置
     */
    private void initReadInterfacePop() {
        readInterfacePop.setListener(this, new ReadInterfacePop.OnChangeProListener() {

            @Override
            public void upPageMode() {
                if (mPageLoader != null) {
                    mPageLoader.setPageMode(PageAnimation.Mode.getPageMode(readBookControl.getPageMode()));
                }
            }

            @Override
            public void upTextSize() {
                if (mPageLoader != null) {
                    mPageLoader.setTextSize();
                }
            }

            @Override
            public void upMargin() {
                if (mPageLoader != null) {
                    mPageLoader.upMargin();
                }
            }

            @Override
            public void bgChange() {
                readBookControl.initTextDrawableIndex();
                pageView.setBackground(readBookControl.getTextBackground(ReadBookActivity.this));
                initImmersionBar();
                if (mPageLoader != null) {
                    mPageLoader.setPageStyle();
                }
            }

            @Override
            public void refresh() {
                if (mPageLoader != null) {
                    mPageLoader.refreshUi();
                }
            }

        });
    }

    /**
     * 其它设置
     */
    private void initMoreSettingPop() {
        moreSettingPop.setListener(new MoreSettingPop.OnChangeProListener() {
            @Override
            public void upBar() {
                initImmersionBar();
            }

            @Override
            public void keepScreenOnChange(int keepScreenOn) {
                screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[keepScreenOn];
                screenOffTimerStart();
            }

            @Override
            public void recreate() {
                ReadBookActivity.this.recreate();
            }

            @Override
            public void refreshPage() {
                if (mPageLoader != null) {
                    mPageLoader.refreshUi();
                }
            }
        });
    }

    @Override
    protected void bindEvent() {
        //打开URL
        tvUrl.setOnClickListener(view -> {
            try {
                String url = tvUrl.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                toast(R.string.can_not_open);
            }
        });
    }

    @Override
    public void startLoadingBook() {
        initPageView();
    }

    /**
     * 加载阅读页面
     */
    private void initPageView() {
        mPageLoader = pageView.getPageLoader(this, mPresenter.getBookShelf());
        mPageLoader.updateBattery(BatteryUtil.getLevel(this));
        mPageLoader.setOnPageChangeListener(
                new PageLoader.OnPageChangeListener() {

                    @Override
                    public void onChapterChange(int pos) {
                        mPresenter.getBookShelf().upDurChapterName();
                        mPresenter.getBookShelf().upLastChapterName();
                        actionBar.setTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
                        if (mPresenter.getBookShelf().getChapterListSize() > 0) {
                            tvChapterName.setText(mPresenter.getBookShelf().getChapter(pos).getDurChapterName());
                            tvUrl.setText(mPresenter.getBookShelf().getChapter(pos).getDurChapterUrl());
                        } else {
                            tvChapterName.setText("");
                            tvUrl.setText("");
                        }

                        if (mPresenter.getBookShelf().getChapterListSize() == 1) {
                            llMenuBottom.setTvPre(false);
                            llMenuBottom.setTvNext(false);
                        } else {
                            if (pos == 0) {
                                llMenuBottom.setTvPre(false);
                                llMenuBottom.setTvNext(true);
                            } else if (pos == mPresenter.getBookShelf().getChapterListSize() - 1) {
                                llMenuBottom.setTvPre(true);
                                llMenuBottom.setTvNext(false);
                            } else {
                                llMenuBottom.setTvPre(true);
                                llMenuBottom.setTvNext(true);
                            }
                        }
                    }

                    @Override
                    public void onCategoryFinish(List<ChapterListBean> chapters) {
                        mPresenter.getBookShelf().getBookInfoBean().setChapterList(chapters);
                        mPresenter.getBookShelf().setChapterListSize(chapters.size());
                        mPresenter.getBookShelf().upDurChapterName();
                        mPresenter.getBookShelf().upLastChapterName();
                        mPresenter.saveProgress();
                    }

                    @Override
                    public void onPageCountChange(int count) {
                        llMenuBottom.getReadProgress().setMax(Math.max(0, count - 1));
                        llMenuBottom.getReadProgress().setProgress(0);
                        // 如果处于错误状态，那么就冻结使用
                        if (mPageLoader.getPageStatus() == TxtChapter.Status.LOADING
                                || mPageLoader.getPageStatus() == TxtChapter.Status.ERROR) {
                            llMenuBottom.getReadProgress().setEnabled(false);
                        } else {
                            llMenuBottom.getReadProgress().setEnabled(true);
                        }
                    }

                    @Override
                    public void onPageChange(int chapterIndex, int pageIndex, boolean resetReadAloud) {
                        mPresenter.getBookShelf().setDurChapter(chapterIndex);
                        mPresenter.getBookShelf().setDurChapterPage(pageIndex);
                        mPresenter.saveProgress();
                        llMenuBottom.getReadProgress().post(
                                () -> llMenuBottom.getReadProgress().setProgress(pageIndex)
                        );
                        if ((ReadAloudService.running)) {
                            if (resetReadAloud) {
                                readAloud();
                                return;
                            }
                            if (pageIndex == 0) {
                                readAloud();
                                return;
                            }
                        }

                        //启动朗读
                        if (getIntent().getBooleanExtra("readAloud", false)
                                && pageIndex >= 0 && mPageLoader.getContent() != null) {
                            getIntent().putExtra("readAloud", false);
                            onMediaButton();
                            return;
                        }
                        autoPage();
                    }
                }
        );
        pageView.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                screenOffTimerStart();
                return true;
            }

            @Override
            public void center() {
                popMenuIn();
            }

        });
        mPageLoader.refreshChapterList();
    }


    //设置ToolBar
    private void setupActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_read_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        upMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 菜单事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_change_source:
                changeSource();
                break;
            case R.id.action_refresh:
                refreshDurChapter();
                break;
            case R.id.action_download:
                download();
                break;
            case R.id.add_bookmark:
                showBookmark(null);
                break;
            case R.id.action_copy_text:
                popMenuOut();
                if (mPageLoader != null) {
                    moDialogHUD.showText(mPageLoader.getContent());
                }
                break;
            case R.id.disable_book_source:
                mPresenter.disableDurBookSource();
                break;
            case R.id.action_book_info:
                BookInfoEditActivity.startThis(this, mPresenter.getBookShelf().getNoteUrl());
                break;
            case R.id.action_set_charset:
                setCharset();
                break;
            case R.id.update_chapter_list:
                if (mPageLoader != null) {
                    mPageLoader.updateChapter();
                }
                break;
            case R.id.action_set_regex:
                setTextChapterRegex();
                break;
            case R.id.action_login:
                SourceLoginActivity.startThis(this, mPresenter.getBookSource());
                break;
            case R.id.action_get_hb:
                DonateActivity.getZfbHb(this);
                upMenu();
                mHandler.postDelayed(this::refreshDurChapter, 2000);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 刷新当前章节
     */
    private void refreshDurChapter() {
        if (!NetworkUtil.isNetWorkAvailable()) {
            toast("网络不可用，无法刷新当前章节!");
            return;
        }
        ReadBookActivity.this.popMenuOut();
        if (mPageLoader != null) {
            mPageLoader.refreshDurChapter();
        }
    }

    /**
     * 书签
     */
    @Override
    public void showBookmark(BookmarkBean bookmarkBean) {
        this.popMenuOut();
        boolean isAdd = false;
        if (mPresenter.getBookShelf() != null) {
            if (bookmarkBean == null) {
                isAdd = true;
                bookmarkBean = new BookmarkBean();
                bookmarkBean.setNoteUrl(mPresenter.getBookShelf().getNoteUrl());
                bookmarkBean.setBookName(mPresenter.getBookShelf().getBookInfoBean().getName());
                bookmarkBean.setChapterIndex(mPresenter.getBookShelf().getDurChapter());
                bookmarkBean.setPageIndex(mPresenter.getBookShelf().getDurChapterPage());
                bookmarkBean.setChapterName(mPresenter.getBookShelf().getDurChapterName());
            }
            moDialogHUD.showBookmark(bookmarkBean, isAdd, new EditBookmarkView.OnBookmarkClick() {
                @Override
                public void saveBookmark(BookmarkBean bookmarkBean) {
                    mPresenter.saveBookmark(bookmarkBean);
                }

                @Override
                public void delBookmark(BookmarkBean bookmarkBean) {
                    mPresenter.delBookmark(bookmarkBean);
                }

                @Override
                public void openChapter(int chapterIndex, int pageIndex) {
                    skipToChapter(chapterIndex, pageIndex);
                }
            });
        }

    }

    @Override
    public void skipToChapter(int chapterIndex, int pageIndex) {
        if (mPageLoader != null) {
            mPageLoader.skipToChapter(chapterIndex, pageIndex);
        }
    }

    /**
     * 换源
     */
    private void changeSource() {
        if (!NetworkUtil.isNetWorkAvailable()) {
            toast(R.string.network_connection_unavailable);
            return;
        }
        ReadBookActivity.this.popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            moDialogHUD.showChangeSource(mPresenter.getBookShelf(), searchBookBean -> {
                if (!Objects.equals(searchBookBean.getNoteUrl(), mPresenter.getBookShelf().getNoteUrl())) {
                    mPageLoader.setStatus(TxtChapter.Status.CHANGE_SOURCE);
                    mPresenter.changeBookSource(searchBookBean);
                }
            });
        }
    }

    /**
     * 下载
     */
    private void download() {
        if (!NetworkUtil.isNetWorkAvailable()) {
            toast(R.string.network_connection_unavailable);
            return;
        }
        ReadBookActivity.this.popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            //弹出离线下载界面
            int endIndex = mPresenter.getBookShelf().getChapterListSize() - 1;
            moDialogHUD.showDownloadList(mPresenter.getBookShelf().getDurChapter(), endIndex,
                    mPresenter.getBookShelf().getChapterListSize(),
                    (start, end) -> {
                        moDialogHUD.dismiss();
                        mPresenter.addDownload(start, end);
                    });
        }
    }

    /**
     * 设置编码
     */
    private void setCharset() {
        final String charset = mPresenter.getBookShelf().getBookInfoBean().getCharset();
        moDialogHUD.showInputBox(getString(R.string.input_charset),
                charset,
                new String[]{"UTF-8", "GB2312", "GBK", "Unicode", "UTF-16", "UTF-16LE", "ASCII"},
                (inputText -> {
                    inputText = inputText.trim();
                    if (!Objects.equals(charset, inputText)) {
                        mPresenter.getBookShelf().getBookInfoBean().setCharset(inputText);
                        mPresenter.saveProgress();
                        if (mPageLoader != null) {
                            mPageLoader.updateChapter();
                        }
                    }
                }));
    }

    /**
     * 设置TXT目录正则
     */
    private void setTextChapterRegex() {
        if (mPresenter.getBookShelf().getNoteUrl().toLowerCase().matches(".*\\.txt")) {
            final String regex = mPresenter.getBookShelf().getBookInfoBean().getChapterUrl();
            moDialogHUD.showInputBox(getString(R.string.text_chapter_list_rule),
                    regex,
                    null,
                    (inputText -> {
                        if (!Objects.equals(regex, inputText)) {
                            mPresenter.getBookShelf().getBookInfoBean().setChapterUrl(inputText);
                            mPresenter.saveProgress();
                            if (mPageLoader != null) {
                                mPageLoader.updateChapter();
                            }
                        }
                    }));
        }
    }

    private void readAdjustIn() {
        flMenu.setVisibility(View.VISIBLE);
        readAdjustPop.show();
        readAdjustPop.setVisibility(View.VISIBLE);
        readAdjustPop.startAnimation(menuBottomIn);
    }

    private void readInterfaceIn() {
        flMenu.setVisibility(View.VISIBLE);
        readInterfacePop.setVisibility(View.VISIBLE);
        readInterfacePop.startAnimation(menuBottomIn);
    }

    private void moreSettingIn() {
        flMenu.setVisibility(View.VISIBLE);
        moreSettingPop.setVisibility(View.VISIBLE);
        moreSettingPop.startAnimation(menuBottomIn);
    }

    /**
     * 显示菜单
     */
    private void popMenuIn() {
        flMenu.setVisibility(View.VISIBLE);
        llMenuTop.setVisibility(View.VISIBLE);
        llMenuBottom.setVisibility(View.VISIBLE);
        llMenuTop.startAnimation(menuTopIn);
        llMenuBottom.startAnimation(menuBottomIn);
        hideSnackBar();
    }

    /**
     * 隐藏菜单
     */
    private void popMenuOut() {
        if (flMenu.getVisibility() == View.VISIBLE) {
            if (llMenuTop.getVisibility() == View.VISIBLE) {
                llMenuTop.startAnimation(menuTopOut);
                llMenuBottom.startAnimation(menuBottomOut);
            }
            if (moreSettingPop.getVisibility() == View.VISIBLE) {
                moreSettingPop.startAnimation(menuBottomOut);
            }
            if (readAdjustPop.getVisibility() == View.VISIBLE) {
                readAdjustPop.startAnimation(menuBottomOut);
            }
            if (readInterfacePop.getVisibility() == View.VISIBLE) {
                readInterfacePop.startAnimation(menuBottomOut);
            }
        }
    }

    private void readAloud() {
        aloudNextPage = false;
        if (mPresenter.getBookShelf() != null && mPageLoader != null && !StringUtils.isTrimEmpty(mPageLoader.getUnReadContent())) {
            ReadAloudService.play(ReadBookActivity.this,
                    false,
                    mPageLoader.getUnReadContent(),
                    mPresenter.getBookShelf().getBookInfoBean().getName(),
                    ChapterContentHelp.getInstance().replaceContent(mPresenter.getBookShelf().getBookInfoBean().getName(), mPresenter.getBookShelf().getTag(), mPresenter.getBookShelf().getDurChapterName()));
        }
    }

    /**
     * 检查是否加入书架
     */
    public boolean checkAddShelf() {
        if (isAdd || mPresenter.getBookShelf() == null
                || TextUtils.isEmpty(mPresenter.getBookShelf().getBookInfoBean().getName())) {
            return true;
        } else if (mPresenter.getBookShelf().realChapterListEmpty()) {
            mPresenter.removeFromShelf();
            return true;
        } else {
            if (checkAddShelfPop == null) {
                checkAddShelfPop = new CheckAddShelfPop(this, mPresenter.getBookShelf().getBookInfoBean().getName(),
                        new CheckAddShelfPop.OnItemClickListener() {
                            @Override
                            public void clickExit() {
                                mPresenter.removeFromShelf();
                            }

                            @Override
                            public void clickAddShelf() {
                                mPresenter.addToShelf(null);
                                checkAddShelfPop.dismiss();
                            }
                        });
            }
            if (!checkAddShelfPop.isShowing()) {
                checkAddShelfPop.showAtLocation(flContent, Gravity.CENTER, 0, 0);
            }
            return false;
        }
    }

    /**
     * 更新朗读状态
     */
    @Override
    public void upAloudState(ReadAloudService.Status status) {
        aloudStatus = status;
        autoPageStop();
        switch (status) {
            case NEXT:
                if (mPageLoader == null) {
                    ReadAloudService.stop(this);
                    break;
                }
                if (!mPageLoader.skipNextChapter()) {
                    ReadAloudService.stop(this);
                }
                break;
            case PLAY:
                llMenuBottom.setFabReadAloudImage(R.drawable.ic_pause_outline_24dp);
                llMenuBottom.setReadAloudTimer(true);
                break;
            case PAUSE:
                llMenuBottom.setFabReadAloudImage(R.drawable.ic_play_outline_24dp);
                llMenuBottom.setReadAloudTimer(true);
                break;
            default:
                llMenuBottom.setFabReadAloudImage(R.drawable.ic_read_aloud);
                llMenuBottom.setReadAloudTimer(false);
                pageView.drawPage(0);
                pageView.invalidate();
                pageView.drawPage(-1);
                pageView.drawPage(1);
                pageView.invalidate();
        }
    }

    @Override
    public void upAloudTimer(String text) {
        llMenuBottom.setReadAloudTimer(text);
    }

    @Override
    public void readAloudStart(int start) {
        aloudNextPage = true;
        if (mPageLoader != null) {
            mPageLoader.readAloudStart(start);
        }
    }

    @Override
    public void readAloudLength(int readAloudLength) {
        if (mPageLoader != null && aloudNextPage) {
            mPageLoader.readAloudLength(readAloudLength);
        }
    }

    @Override
    public void refresh(boolean recreate) {
        if (recreate) {
            recreate();
        } else {
            flContent.setBackground(readBookControl.getTextBackground(this));
            if (mPageLoader != null) {
                mPageLoader.refreshUi();
            }
            readInterfacePop.setBg();
            initImmersionBar();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        boolean isDown = action == 0;

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return isDown ? this.onKeyDown(keyCode, event) : this.onKeyUp(keyCode, event);
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 按键事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (readInterfacePop.getVisibility() == View.VISIBLE
                        || readAdjustPop.getVisibility() == View.VISIBLE
                        || moreSettingPop.getVisibility() == View.VISIBLE) {
                    popMenuOut();
                    return true;
                } else if (flMenu.getVisibility() == View.VISIBLE) {
                    finish();
                    return true;
                } else if (ReadAloudService.running && aloudStatus == ReadAloudService.Status.PLAY) {
                    ReadAloudService.pause(this);
                    toast(R.string.read_aloud_pause);
                    return true;
                } else {
                    finish();
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (flMenu.getVisibility() == View.VISIBLE) {
                    popMenuOut();
                } else {
                    popMenuIn();
                }
                return true;
            } else if (flMenu.getVisibility() != View.VISIBLE) {
                if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.Status.PLAY) && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    if (mPageLoader != null) {
                        mPageLoader.skipToNextPage();
                    }
                    return true;
                } else if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.Status.PLAY) && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    if (mPageLoader != null) {
                        mPageLoader.skipToPrePage();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_SPACE) {
                    nextPage();
                    return true;
                }
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (flMenu.getVisibility() != View.VISIBLE) {
            if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.Status.PLAY)
                    && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void upMenu() {
        if (menu == null) return;
        boolean onLine = mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG);
        if (onLine) {
            tvUrl.setVisibility(View.VISIBLE);
            atvLine.setVisibility(View.VISIBLE);
        } else {
            tvUrl.setVisibility(View.GONE);
            atvLine.setVisibility(View.GONE);
        }
        for (int i = 0; i < menu.size(); i++) {
            switch (menu.getItem(i).getGroupId()) {
                case R.id.menuOnLine:
                    menu.getItem(i).setVisible(onLine);
                    menu.getItem(i).setEnabled(onLine);
                    break;
                case R.id.menuLocal:
                    menu.getItem(i).setVisible(!onLine);
                    menu.getItem(i).setEnabled(!onLine);
                    break;
                case R.id.menu_text:
                    boolean isTxt = mPresenter.getBookShelf() != null && mPresenter.getBookShelf().getNoteUrl().toLowerCase().endsWith(".txt");
                    menu.getItem(i).setVisible(isTxt);
                    menu.getItem(i).setEnabled(isTxt);
                    break;
                case R.id.menu_login:
                    if (mPresenter.getBookSource() != null && !TextUtils.isEmpty(mPresenter.getBookSource().getLoginUrl())) {
                        menu.getItem(i).setVisible(true);
                    } else {
                        menu.getItem(i).setVisible(false);
                    }
                    break;
                case R.id.menu_get_zfb_hb:
                    if (MApplication.getInstance().getDonateHb()) {
                        menu.getItem(i).setVisible(false);
                    } else {
                        menu.getItem(i).setVisible(true);
                    }
                    break;
            }

        }

    }

    @Override
    public String getNoteUrl() {
        return noteUrl;
    }

    @Override
    public Boolean getAdd() {
        return isAdd;
    }

    @Override
    public void setAdd(Boolean isAdd) {
        this.isAdd = isAdd;
    }

    @Override
    public void openBookFromOther() {
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                mPresenter.openBookFromOther(ReadBookActivity.this);
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                ReadBookActivity.this.toast(R.string.open_from_other);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                PermissionUtils.requestMorePermissions(ReadBookActivity.this, permission, MApplication.RESULT__PERMS);
            }
        });
    }

    /**
     * 朗读按钮
     */
    @Override
    public void onMediaButton() {
        if (!ReadAloudService.running) {
            aloudStatus = ReadAloudService.Status.STOP;
            SystemUtil.ignoreBatteryOptimization(this);
        }
        switch (aloudStatus) {
            case PAUSE:
                ReadAloudService.resume(this);
                llMenuBottom.setFabReadAloudText(getString(R.string.read_aloud));
                break;
            case PLAY:
                ReadAloudService.pause(this);
                llMenuBottom.setFabReadAloudText(getString(R.string.read_aloud_pause));
                break;
            default:
                ReadBookActivity.this.popMenuOut();
                readAloud();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                mPresenter.openBookFromOther(ReadBookActivity.this);
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                ReadBookActivity.this.toast(R.string.open_local_book_per);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                ReadBookActivity.this.toast(R.string.open_local_book_per);
                PermissionUtils.toAppSetting(ReadBookActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initImmersionBar();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onResume() {
        super.onResume();
        if (batInfoReceiver == null) {
            batInfoReceiver = new ThisBatInfoReceiver();
            batInfoReceiver.registerThis();
        }
        screenOffTimerStart();
        if (mPageLoader != null) {
            if (!mPageLoader.updateBattery(BatteryUtil.getLevel(this))) {
                pageView.invalidate();
            }
        }
        if (showCheckPermission && mPresenter.getOpen_from() == ReadBookPresenter.OPEN_FROM_OTHER && PermissionUtils.checkMorePermissions(this, MApplication.PerList).isEmpty()) {
            showCheckPermission = true;
            mPresenter.openBookFromOther(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (batInfoReceiver != null) {
            batInfoReceiver.unregisterThis();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batInfoReceiver != null) {
            batInfoReceiver.unregisterThis();
        }
        ReadAloudService.stop(this);
        if (mPageLoader != null) {
            mPageLoader.closeBook();
            mPageLoader = null;
        }
    }

    /**
     * 结束
     */
    @Override
    public void finish() {
        if (!checkAddShelf()) {
            return;
        }
        if (!AppActivityManager.getInstance().isExist(MainActivity.class)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        RxBus.get().post(RxBusTag.AUTO_BACKUP, true);
        super.finish();
    }

    @Override
    public void changeSourceFinish(BookShelfBean book) {
        if (mPageLoader != null) {
            mPageLoader.changeSourceFinish(book);
        }
    }

    /**
     * 时间和电量广播
     */
    class ThisBatInfoReceiver extends BroadcastReceiver {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (readBookControl.getHideStatusBar()) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    if (mPageLoader != null) {
                        mPageLoader.updateTime();
                    }
                } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    if (mPageLoader != null) {
                        mPageLoader.updateBattery(level);
                    }
                }
            }
        }

        public void registerThis() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            ReadBookActivity.this.registerReceiver(batInfoReceiver, filter);
        }

        public void unregisterThis() {
            ReadBookActivity.this.unregisterReceiver(batInfoReceiver);
            batInfoReceiver = null;
        }
    }

}
