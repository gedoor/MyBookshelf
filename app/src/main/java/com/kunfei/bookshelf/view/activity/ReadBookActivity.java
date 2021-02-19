//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.kunfei.basemvplib.AppActivityManager;
import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.dao.TxtChapterRuleBeanDao;
import com.kunfei.bookshelf.databinding.ActivityBookReadBinding;
import com.kunfei.bookshelf.help.ChapterContentHelp;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.help.permission.Permissions;
import com.kunfei.bookshelf.help.permission.PermissionsCompat;
import com.kunfei.bookshelf.help.storage.Backup;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.model.TxtChapterRuleManager;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.ReadBookContract;
import com.kunfei.bookshelf.service.ReadAloudService;
import com.kunfei.bookshelf.utils.BatteryUtil;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.SystemUtil;
import com.kunfei.bookshelf.utils.bar.BarHide;
import com.kunfei.bookshelf.utils.bar.ImmersionBar;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.popupwindow.CheckAddShelfPop;
import com.kunfei.bookshelf.view.popupwindow.MoreSettingPop;
import com.kunfei.bookshelf.view.popupwindow.ReadAdjustMarginPop;
import com.kunfei.bookshelf.view.popupwindow.ReadAdjustPop;
import com.kunfei.bookshelf.view.popupwindow.ReadBottomMenu;
import com.kunfei.bookshelf.view.popupwindow.ReadInterfacePop;
import com.kunfei.bookshelf.view.popupwindow.ReadLongPressPop;
import com.kunfei.bookshelf.widget.modialog.BookmarkDialog;
import com.kunfei.bookshelf.widget.modialog.ChangeSourceDialog;
import com.kunfei.bookshelf.widget.modialog.DownLoadDialog;
import com.kunfei.bookshelf.widget.modialog.InputDialog;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;
import com.kunfei.bookshelf.widget.modialog.ReplaceRuleDialog;
import com.kunfei.bookshelf.widget.page.PageLoader;
import com.kunfei.bookshelf.widget.page.PageLoaderNet;
import com.kunfei.bookshelf.widget.page.PageView;
import com.kunfei.bookshelf.widget.page.TxtChapter;
import com.kunfei.bookshelf.widget.page.animation.PageAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import kotlin.Unit;


/**
 * 阅读界面
 */
public class ReadBookActivity extends MBaseActivity<ReadBookContract.Presenter> implements ReadBookContract.View, View.OnTouchListener {

    private ActivityBookReadBinding binding;
    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;
    private ActionBar actionBar;
    private PageLoader mPageLoader;
    private Handler mHandler = new Handler();
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

    private boolean autoPage = false;
    private boolean aloudNextPage;
    private int lastX, lastY;

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
        binding = ActivityBookReadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && readBookControl.getToLh()) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(lp);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPresenter.getBookShelf() != null) {
            outState.putString("noteUrl", mPresenter.getBookShelf().getNoteUrl());
            outState.putBoolean("isAdd", isAdd);
            String key = String.valueOf(System.currentTimeMillis());
            String bookKey = "book" + key;
            getIntent().putExtra("bookKey", bookKey);
            BitIntentDataManager.getInstance().putData(bookKey, mPresenter.getBookShelf().clone());
            String chapterListKey = "chapterList" + key;
            getIntent().putExtra("chapterListKey", chapterListKey);
            BitIntentDataManager.getInstance().putData(chapterListKey, mPresenter.getChapterList());
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
                binding.readMenuBottom.setNavigationBarHeight(ImmersionBar.getNavigationBarHeight(this));
            }
        }

        if (binding.readMenuBottom.getVisibility() == View.VISIBLE) {
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
        int barColorType = readBookControl.getNavBarColor();
        if (binding.readMenuBottom.getVisibility() == View.VISIBLE
                || binding.readAdjustPop.getVisibility() == View.VISIBLE
                || binding.readInterfacePop.getVisibility() == View.VISIBLE
                || binding.readAdjustMarginPop.getVisibility() == View.VISIBLE
                || binding.moreSettingPop.getVisibility() == View.VISIBLE) {
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

    /**
     * 取消亮屏保持
     */
    private void unKeepScreenOn() {
        keepScreenOn(false);
    }

    /**
     * @param keepScreenOn 是否保持亮屏
     */
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
            binding.pbNextPage.setVisibility(View.VISIBLE);
            //每页按字数计算一次时间
            nextPageTime = mPageLoader.curPageLength() * 60 * 1000 / readBookControl.getCPM();
            if (0 == nextPageTime) nextPageTime = 1000;
            binding.pbNextPage.setMax(nextPageTime);
            mHandler.postDelayed(upHpbNextPage, upHpbInterval);
            mHandler.postDelayed(autoPageRunnable, nextPageTime);
        } else {
            binding.pbNextPage.setVisibility(View.INVISIBLE);
        }
        binding.readMenuBottom.setAutoPage(autoPage);
    }

    /**
     * 更新自动翻页进度条
     */
    private void upHpbNextPage() {
        nextPageTime = nextPageTime - upHpbInterval;
        binding.pbNextPage.setProgress(nextPageTime);
        mHandler.postDelayed(upHpbNextPage, upHpbInterval);
    }

    /**
     * 停止自动翻页
     */
    private void autoPageStop() {
        autoPage = false;
        autoPage();
    }

    /**
     * 下一页
     */
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
        menuTopIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                initImmersionBar();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.vMenuBg.setOnClickListener(v -> popMenuOut());
                initImmersionBar();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        menuBottomIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_in);
        menuBottomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                initImmersionBar();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.vMenuBg.setOnClickListener(v -> popMenuOut());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //隐藏菜单
        menuTopOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_out);
        menuTopOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.vMenuBg.setOnClickListener(null);
                initImmersionBar();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flMenu.setVisibility(View.INVISIBLE);
                binding.llMenuTop.setVisibility(View.INVISIBLE);
                binding.readMenuBottom.setVisibility(View.INVISIBLE);
                binding.readAdjustPop.setVisibility(View.INVISIBLE);
                binding.readAdjustMarginPop.setVisibility(View.INVISIBLE);
                binding.readInterfacePop.setVisibility(View.INVISIBLE);
                binding.moreSettingPop.setVisibility(View.INVISIBLE);
                initImmersionBar();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        menuBottomOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_out);
        menuBottomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.vMenuBg.setOnClickListener(null);
                initImmersionBar();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flMenu.setVisibility(View.INVISIBLE);
                binding.llMenuTop.setVisibility(View.INVISIBLE);
                binding.readMenuBottom.setVisibility(View.INVISIBLE);
                binding.readAdjustPop.setVisibility(View.INVISIBLE);
                binding.readAdjustMarginPop.setVisibility(View.INVISIBLE);
                binding.readInterfacePop.setVisibility(View.INVISIBLE);
                binding.moreSettingPop.setVisibility(View.INVISIBLE);
                initImmersionBar();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (MApplication.isEInkMode) {
            menuTopIn.setDuration(0);
            menuTopOut.setDuration(0);
            menuBottomIn.setDuration(0);
            menuBottomOut.setDuration(0);
        }
    }

    @Override
    protected void bindView() {
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();
        mPresenter.initData(this);
        binding.appBar.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
        binding.appBar.setBackgroundColor(ThemeStore.primaryColor(this));
        binding.readMenuBottom.setFabNightTheme(isNightTheme());
        //弹窗
        moDialogHUD = new MoDialogHUD(this);
        initBottomMenu();
        initReadInterfacePop();
        initReadAdjustPop();
        initReadAdjustMarginPop();
        initMoreSettingPop();
        initMediaPlayer();
        initReadLongPressPop();
        binding.pageView.setBackground(readBookControl.getTextBackground(this));
        binding.cursorLeft.getDrawable().setColorFilter(ThemeStore.accentColor(this), PorterDuff.Mode.SRC_ATOP);
        binding.cursorRight.getDrawable().setColorFilter(ThemeStore.accentColor(this), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * 初始化播放界面
     */
    private void initMediaPlayer() {
        binding.mediaPlayerPop.setIvChapterClickListener(v -> ChapterListActivity.startThis(ReadBookActivity.this, mPresenter.getBookShelf(), mPresenter.getChapterList()));
        binding.mediaPlayerPop.setIvTimerClickListener(v -> ReadAloudService.setTimer(getContext(), 10));
        binding.mediaPlayerPop.setIvCoverBgClickListener(v -> {
            binding.flMenu.setVisibility(View.VISIBLE);
            binding.llMenuTop.setVisibility(View.VISIBLE);
            binding.llMenuTop.startAnimation(menuTopIn);
        });
        binding.mediaPlayerPop.setPlayClickListener(v -> onMediaButton(ReadAloudService.ActionMediaPlay));
        binding.mediaPlayerPop.setPrevClickListener(v -> {
            mPresenter.getBookShelf().setDurChapterPage(0);
            mPageLoader.skipToPrePage();
        });
        binding.mediaPlayerPop.setNextClickListener(v -> {
            mPresenter.getBookShelf().setDurChapterPage(0);
            mPageLoader.skipToNextPage();
        });
        binding.mediaPlayerPop.setCallback(dur -> ReadAloudService.setProgress(ReadBookActivity.this, dur));
    }

    /**
     * 初始化底部菜单
     */
    private void initBottomMenu() {
        binding.readMenuBottom.setListener(new ReadBottomMenu.Callback() {
            @Override
            public void skipToPage(int page) {
                if (mPageLoader != null) {
                    mPageLoader.skipToPage(page);
                }
            }

            @Override
            public void onMediaButton() {
                ReadBookActivity.this.onMediaButton(ReadAloudService.ActionMediaPlay);
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
                ReplaceRuleActivity.startThis(ReadBookActivity.this, mPresenter.getBookShelf());
            }

            @Override
            public void openChapterList() {
                ReadBookActivity.this.popMenuOut();
                if (!mPresenter.getChapterList().isEmpty()) {
                    mHandler.postDelayed(() -> ChapterListActivity.startThis(ReadBookActivity.this, mPresenter.getBookShelf(), mPresenter.getChapterList()), menuTopOut.getDuration());
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
     * 初始化调节
     */
    private void initReadAdjustPop() {
        binding.readAdjustPop.setListener(this, new ReadAdjustPop.Callback() {
            @Override
            public void speechRateFollowSys() {
                if (ReadAloudService.running) {
                    ReadAloudService.stop(ReadBookActivity.this);
                }
            }

            @Override
            public void changeSpeechRate(int speechRate) {
                if (ReadAloudService.running) {
                    ReadAloudService.pause(ReadBookActivity.this);
                    ReadAloudService.resume(ReadBookActivity.this);
                }
            }
        });
    }

    /**
     * 初始化调节
     */
    private void initReadAdjustMarginPop() {
        binding.readAdjustMarginPop.setListener(this, new ReadAdjustMarginPop.Callback() {

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
            public void refresh() {
                if (mPageLoader != null) {
                    mPageLoader.refreshUi();
                }
            }

        });
    }

    /**
     * 初始化界面设置
     */
    private void initReadInterfacePop() {
        binding.readInterfacePop.setListener(this, new ReadInterfacePop.Callback() {

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
                binding.pageView.setBackground(readBookControl.getTextBackground(ReadBookActivity.this));
                initImmersionBar();
                if (mPageLoader != null) {
                    mPageLoader.refreshUi();
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
     * 初始化其它设置
     */
    private void initMoreSettingPop() {
        binding.moreSettingPop.setListener(new MoreSettingPop.Callback() {
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void bindEvent() {
        binding.tvChapterName.setOnClickListener(v -> {
            if (mPresenter.getBookSource() != null) {
                SourceEditActivity.startThis(this, mPresenter.getBookSource());
            }
        });
        //打开URL
        binding.tvChapterUrl.setOnClickListener(view -> {
            try {
                String url = binding.tvChapterUrl.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                toast(R.string.can_not_open);
            }
        });

        binding.cursorLeft.setOnTouchListener(this);
        binding.cursorRight.setOnTouchListener(this);
        binding.flContent.setOnTouchListener(this);
    }

    /**
     * 开始加载
     */
    @Override
    public void startLoadingBook() {
        initPageView();
        binding.mediaPlayerPop.setCover(mPresenter.getBookShelf().getCustomCoverPath() != null ? mPresenter.getBookShelf().getCustomCoverPath()
                : mPresenter.getBookShelf().getBookInfoBean().getCoverUrl());
    }

    /**
     * 加载阅读页面
     */
    private void initPageView() {
        mPageLoader = binding.pageView.getPageLoader(this, mPresenter.getBookShelf(),
                new PageLoader.Callback() {
                    @Override
                    public List<BookChapterBean> getChapterList() {
                        return mPresenter.getChapterList();
                    }

                    /**
                     * @param pos:切换章节的序号
                     */
                    @Override
                    public void onChapterChange(int pos) {
                        if (mPresenter.getChapterList().isEmpty()) return;
                        if (pos >= mPresenter.getChapterList().size()) return;
                        mPresenter.getBookShelf().setDurChapterName(mPresenter.getChapterList().get(pos).getDurChapterName());
                        actionBar.setTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
                        if (mPresenter.getBookShelf().getChapterListSize() > 0) {
                            binding.tvChapterName.setVisibility(View.VISIBLE);
                            binding.tvChapterName.setText(mPresenter.getChapterList().get(pos).getDurChapterName());
                            binding.tvChapterUrl.setVisibility(View.VISIBLE);
                            binding.tvChapterUrl.setText(NetworkUtils.getAbsoluteURL(mPresenter.getBookShelf().getBookInfoBean().getChapterUrl(),
                                    mPresenter.getChapterList().get(pos).getDurChapterUrl()));
                        } else {
                            binding.tvChapterName.setVisibility(View.GONE);
                            binding.tvChapterUrl.setVisibility(View.GONE);
                        }

                        if (mPresenter.getBookShelf().getChapterListSize() == 1) {
                            binding.readMenuBottom.setTvPre(false);
                            binding.readMenuBottom.setTvNext(false);
                        } else {
                            if (pos == 0) {
                                binding.readMenuBottom.setTvPre(false);
                                binding.readMenuBottom.setTvNext(true);
                            } else if (pos == mPresenter.getBookShelf().getChapterListSize() - 1) {
                                binding.readMenuBottom.setTvPre(true);
                                binding.readMenuBottom.setTvNext(false);
                            } else {
                                binding.readMenuBottom.setTvPre(true);
                                binding.readMenuBottom.setTvNext(true);
                            }
                        }
                    }

                    /**
                     * @param chapters：返回章节目录
                     */
                    @Override
                    public void onCategoryFinish(List<BookChapterBean> chapters) {
                        mPresenter.setChapterList(chapters);
                        mPresenter.getBookShelf().setChapterListSize(chapters.size());
                        mPresenter.getBookShelf().setDurChapterName(chapters.get(mPresenter.getBookShelf().getDurChapter()).getDurChapterName());
                        mPresenter.getBookShelf().setLastChapterName(chapters.get(mPresenter.getChapterList().size() - 1).getDurChapterName());
                        mPresenter.saveProgress();
                    }

                    /**
                     * 总页数变化
                     */
                    @Override
                    public void onPageCountChange(int count) {
                        binding.readMenuBottom.getReadProgress().setMax(Math.max(0, count - 1));
                        binding.readMenuBottom.getReadProgress().setProgress(0);
                        // 如果处于错误状态，那么就冻结使用
                        if (mPageLoader.getPageStatus() == TxtChapter.Status.LOADING
                                || mPageLoader.getPageStatus() == TxtChapter.Status.ERROR) {
                            binding.readMenuBottom.getReadProgress().setEnabled(false);
                        } else {
                            binding.readMenuBottom.getReadProgress().setEnabled(true);
                        }
                    }

                    /**
                     * 翻页成功
                     */
                    @Override
                    public void onPageChange(int chapterIndex, int pageIndex, boolean resetReadAloud) {
                        mPresenter.getBookShelf().setDurChapter(chapterIndex);
                        mPresenter.getBookShelf().setDurChapterPage(pageIndex);
                        mPresenter.saveProgress();
                        binding.readMenuBottom.getReadProgress().post(
                                () -> binding.readMenuBottom.getReadProgress().setProgress(pageIndex)
                        );
                        Long end = mPresenter.getDurChapter().getEnd();
                        int audioSize = end != null ? end.intValue() : 0;
                        binding.mediaPlayerPop.upAudioSize(audioSize);
                        binding.mediaPlayerPop.upAudioDur(mPresenter.getBookShelf().getDurChapterPage());
                        if (mPresenter.getBookShelf().isAudio() && mPageLoader.getPageStatus() == TxtChapter.Status.FINISH) {
                            if (binding.mediaPlayerPop.getVisibility() != View.VISIBLE) {
                                binding.mediaPlayerPop.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (binding.mediaPlayerPop.getVisibility() == View.VISIBLE) {
                                binding.mediaPlayerPop.setVisibility(View.GONE);
                            }
                        }
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
                            onMediaButton(ReadAloudService.ActionMediaPlay);
                            return;
                        }
                        autoPage();
                    }

                    @Override
                    public void vipPop() {
                        moDialogHUD.showTwoButton(ReadBookActivity.this.getString(R.string.donate_s), "领取红包", (v) -> {
                                    DonateActivity.getZfbHb(ReadBookActivity.this);
                                    mHandler.postDelayed(() -> {
                                        ReadBookActivity.this.refreshDurChapter();
                                        moDialogHUD.dismiss();
                                    }, 2000);
                                },
                                "关注公众号",
                                (v) -> {
                                    ClipboardManager clipboard = (ClipboardManager) ReadBookActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clipData = ClipData.newPlainText(null, "开源阅读软件");
                                    if (clipboard != null) {
                                        clipboard.setPrimaryClip(clipData);
                                        toast("[开源阅读软件],已复制成功,可到微信搜索");
                                    }
                                    MApplication.getInstance().upDonateHb();
                                    mHandler.postDelayed(() -> {
                                        ReadBookActivity.this.refreshDurChapter();
                                        moDialogHUD.dismiss();
                                    }, 1000);
                                },
                                true);
                    }
                }
        );
        mPageLoader.updateBattery(BatteryUtil.getLevel(this));
        binding.pageView.setTouchListener(new PageView.TouchListener() {
            @Override
            public void onTouch() {
                screenOffTimerStart();
            }

            @Override
            public void center() {
                popMenuIn();
            }

            @Override
            public void onTouchClearCursor() {
                binding.cursorLeft.setVisibility(View.INVISIBLE);
                binding.cursorRight.setVisibility(View.INVISIBLE);
                binding.readLongPress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLongPress() {
                if (!binding.pageView.isRunning()) {
                    selectTextCursorShow();
                    showAction(binding.cursorLeft);
                }
            }
        });
        mPageLoader.refreshChapterList();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.getId() == R.id.cursor_left || v.getId() == R.id.cursor_right) {
            int ea = event.getAction();
            //final int screenWidth = dm.widthPixels;
            //final int screenHeight = dm.heightPixels;
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                    lastY = (int) event.getRawY();

                    binding.readLongPress.setVisibility(View.INVISIBLE);

                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    int l = v.getLeft() + dx;
                    int b = v.getBottom() + dy;
                    int r = v.getRight() + dx;
                    int t = v.getTop() + dy;

                    v.layout(l, t, r, b);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    v.postInvalidate();

                    //移动过程中要画线
                    binding.pageView.setSelectMode(PageView.SelectMode.SelectMoveForward);

                    int hh = binding.cursorLeft.getHeight();
                    int ww = binding.cursorLeft.getWidth();

                    if (v.getId() == R.id.cursor_left) {
                        binding.pageView.setFirstSelectTxtChar(binding.pageView.getCurrentTxtChar(lastX + ww, lastY - hh));
                    } else {
                        binding.pageView.setLastSelectTxtChar(binding.pageView.getCurrentTxtChar(lastX - ww, lastY - hh));
                    }

                    binding.pageView.invalidate();

                    break;
                case MotionEvent.ACTION_UP:
                    showAction(v);
                    //v.layout(l, t, r, b);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    public void showAction(View clickView) {

        binding.readLongPress.setVisibility(View.VISIBLE);
        //如果太靠右，则靠左
        int[] aa = ScreenUtils.getScreenSize(this);
        if ((binding.cursorLeft.getX() + ScreenUtils.dpToPx(200)) > aa[0]) {
            binding.readLongPress.setX(aa[0] - ScreenUtils.dpToPx(200));
        } else {
            binding.readLongPress.setX(binding.cursorLeft.getX() + binding.cursorLeft.getWidth() + ScreenUtils.dpToPx(5));
        }

        //如果太靠上
        if ((binding.cursorLeft.getY() - ScreenUtils.spToPx(readBookControl.getTextSize()) - ScreenUtils.dpToPx(60)) < 0) {
            binding.readLongPress.setY(binding.cursorLeft.getY() - ScreenUtils.spToPx(readBookControl.getTextSize()));
        } else {
            binding.readLongPress.setY(binding.cursorLeft.getY() - ScreenUtils.spToPx(readBookControl.getTextSize()) - ScreenUtils.dpToPx(40));
        }

    }

    /**
     * 显示
     */
    private void selectTextCursorShow() {
        if (binding.pageView.getFirstSelectTxtChar() == null || binding.pageView.getLastSelectTxtChar() == null)
            return;
        //show Cursor on current position
        cursorShow();
        //set current word selected
        binding.pageView.invalidate();

        hideSnackBar();
    }

    private void cursorShow() {

        binding.cursorLeft.setVisibility(View.VISIBLE);
        binding.cursorRight.setVisibility(View.VISIBLE);
        int hh = binding.cursorLeft.getHeight();
        int ww = binding.cursorLeft.getWidth();
        if (binding.pageView.getFirstSelectTxtChar() != null) {
            binding.cursorLeft.setX(binding.pageView.getFirstSelectTxtChar().getTopLeftPosition().x - ww);
            binding.cursorLeft.setY(binding.pageView.getFirstSelectTxtChar().getBottomLeftPosition().y);
            binding.cursorRight.setX(binding.pageView.getFirstSelectTxtChar().getBottomRightPosition().x);
            binding.cursorRight.setY(binding.pageView.getFirstSelectTxtChar().getBottomRightPosition().y);
        }
    }

    /**
     * 长按选择按钮
     */
    private void initReadLongPressPop() {
        binding.readLongPress.setListener(new ReadLongPressPop.OnBtnClickListener() {
            @Override
            public void copySelect() {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, binding.pageView.getSelectStr());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clipData);
                    toast("所选内容已经复制到剪贴板");
                }

                binding.cursorLeft.setVisibility(View.INVISIBLE);
                binding.cursorRight.setVisibility(View.INVISIBLE);
                binding.readLongPress.setVisibility(View.INVISIBLE);
                binding.pageView.clearSelect();

            }

            @Override
            public void replaceSelect() {
                ReplaceRuleBean oldRuleBean = new ReplaceRuleBean();
                oldRuleBean.setReplaceSummary(binding.pageView.getSelectStr().trim());
                oldRuleBean.setEnable(true);
                oldRuleBean.setRegex(binding.pageView.getSelectStr().trim());
                oldRuleBean.setIsRegex(false);
                oldRuleBean.setReplacement("");
                oldRuleBean.setSerialNumber(0);
                oldRuleBean.setUseTo(String.format("%s,%s", mPresenter.getBookShelf().getBookInfoBean().getName(), mPresenter.getBookShelf().getTag()));

                ReplaceRuleDialog.builder(ReadBookActivity.this, oldRuleBean, mPresenter.getBookShelf(), ReplaceRuleDialog.DefaultUI)
                        .setPositiveButton(replaceRuleBean1 ->
                                ReplaceRuleManager.saveData(replaceRuleBean1)
                                        .subscribe(new MySingleObserver<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean aBoolean) {
                                                binding.cursorLeft.setVisibility(View.INVISIBLE);
                                                binding.cursorRight.setVisibility(View.INVISIBLE);
                                                binding.readLongPress.setVisibility(View.INVISIBLE);

                                                binding.pageView.setSelectMode(PageView.SelectMode.Normal);

                                                moDialogHUD.dismiss();

                                                refresh(false);
                                            }
                                        })).show();
            }

            @Override
            public void replaceSelectAd() {
                String selectString = binding.pageView.getSelectStr();


                if (selectString != null) {
                    String spacer = null;
                    String name = (mPresenter.getBookShelf().getBookInfoBean().getName());
                    if (name != null)
                        if (name.trim().length() > 0)
                            spacer = "|" + Pattern.quote(name.trim());
//                        spacer = "|" + Matcher.quoteReplacement(name.trim());

                    name = (mPresenter.getBookShelf().getBookInfoBean().getAuthor());
                    if (name != null)
                        if (name.trim().length() > 0)
                            if (spacer != null)
                                spacer = spacer + "|" + Pattern.quote(name.trim());
                            else
                                spacer = "|" + Pattern.quote(name.trim());
                            String rule="(\\s*\n\\s*" + spacer + ")";
                    selectString = ReplaceRuleManager.formateAdRule(
                            selectString.replaceAll(rule, "\n")
                    );

                    Log.i("selectString.afterAd2",selectString);

                }


                ReplaceRuleBean oldRuleBean = new ReplaceRuleBean();
                oldRuleBean.setReplaceSummary(getString(R.string.replace_ad) + "-" + mPresenter.getBookShelf().getTag());
                oldRuleBean.setEnable(true);
                oldRuleBean.setRegex(selectString);
                oldRuleBean.setIsRegex(false);
                oldRuleBean.setReplacement("");
                oldRuleBean.setSerialNumber(0);
                oldRuleBean.setUseTo(String.format(mPresenter.getBookShelf().getTag()));

                ReplaceRuleDialog.builder(ReadBookActivity.this, oldRuleBean, mPresenter.getBookShelf(), ReplaceRuleDialog.AddAdUI)
                        .setPositiveButton(replaceRuleBean1 ->
                                ReplaceRuleManager.mergeAdRules(replaceRuleBean1)
                                        .subscribe(new MySingleObserver<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean aBoolean) {
                                                binding.cursorLeft.setVisibility(View.INVISIBLE);
                                                binding.cursorRight.setVisibility(View.INVISIBLE);
                                                binding.readLongPress.setVisibility(View.INVISIBLE);

                                                binding.pageView.setSelectMode(PageView.SelectMode.Normal);

                                                moDialogHUD.dismiss();

                                                refresh(false);
                                            }
                                        })).show();

            }
        });
    }


    /**
     * 设置ToolBar
     */
    private void setupActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 添加菜单
     */
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
            case R.id.enable_replace:
                mPresenter.getBookShelf().setReplaceEnable(!mPresenter.getBookShelf().getReplaceEnable());
                refresh(false);
                break;
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
                    moDialogHUD.showText(mPageLoader.getAllContent());
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
        if (!NetworkUtils.isNetWorkAvailable()) {
            toast("网络不可用，无法刷新当前章节!");
            return;
        }
        ReadBookActivity.this.popMenuOut();
        if (mPageLoader != null) {
            if (mPageLoader instanceof PageLoaderNet) {
                ((PageLoaderNet) mPageLoader).refreshDurChapter();
            }
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
            BookmarkDialog.builder(this, bookmarkBean, isAdd)
                    .setPositiveButton(new BookmarkDialog.Callback() {
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
                    }).show();
        }

    }

    @Override
    public void skipToChapter(int chapterIndex, int pageIndex) {
        if (mPageLoader != null) {
            mPageLoader.skipToChapter(chapterIndex, pageIndex);
        }
    }

    /**
     * 自动换源
     */
    public void autoChangeSource() {
        mPresenter.autoChangeSource();
    }

    /**
     * 换源
     */
    private void changeSource() {
        if (!NetworkUtils.isNetWorkAvailable()) {
            toast(R.string.network_connection_unavailable);
            return;
        }
        ReadBookActivity.this.popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            ChangeSourceDialog.builder(this, mPresenter.getBookShelf())
                    .setCallback(searchBookBean -> {
                        if (!Objects.equals(searchBookBean.getNoteUrl(), mPresenter.getBookShelf().getNoteUrl())) {
                            if (mPageLoader != null) {
                                mPageLoader.setStatus(TxtChapter.Status.CHANGE_SOURCE);
                            }
                            mPresenter.changeBookSource(searchBookBean);
                        }
                    }).show();
        }
    }

    /**
     * 下载
     */
    private void download() {
        if (!NetworkUtils.isNetWorkAvailable()) {
            toast(R.string.network_connection_unavailable);
            return;
        }
        ReadBookActivity.this.popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            //弹出离线下载界面
            int endIndex = mPresenter.getBookShelf().getChapterListSize() - 1;
            DownLoadDialog.builder(this, mPresenter.getBookShelf().getDurChapter(), endIndex, mPresenter.getBookShelf().getChapterListSize())
                    .setPositiveButton((start, end) -> mPresenter.addDownload(start, end)).show();
        }
    }

    /**
     * 设置编码
     */
    private void setCharset() {
        final String charset = mPresenter.getBookShelf().getBookInfoBean().getCharset();
        String[] a = new String[]{"UTF-8", "GB2312", "GBK", "Unicode", "UTF-16", "UTF-16LE", "ASCII"};
        List<String> values = new ArrayList<>(Arrays.asList(a));
        InputDialog.builder(this)
                .setTitle(getString(R.string.input_charset))
                .setDefaultValue(charset)
                .setAdapterValues(values)
                .setCallback(new InputDialog.Callback() {
                    @Override
                    public void setInputText(String inputText) {
                        inputText = inputText.trim();
                        if (!Objects.equals(charset, inputText)) {
                            mPresenter.getBookShelf().getBookInfoBean().setCharset(inputText);
                            mPresenter.saveProgress();
                            if (mPageLoader != null) {
                                mPageLoader.updateChapter();
                            }
                        }
                    }

                    @Override
                    public void delete(String value) {

                    }
                }).show();
    }

    /**
     * 设置TXT目录正则
     */
    private void setTextChapterRegex() {
        if (mPresenter.getBookShelf().getNoteUrl().toLowerCase().matches(".*\\.txt")) {
            int checkedItem = 0;
            List<TxtChapterRuleBean> ruleBeanList = TxtChapterRuleManager.getEnabled();
            List<String> ruleNameList = new ArrayList<>();
            String rule = mPresenter.getBookShelf().getBookInfoBean().getChapterUrl();
            if (!TextUtils.isEmpty(rule)) {
                TxtChapterRuleBean ruleBean = DbHelper.getDaoSession().getTxtChapterRuleBeanDao().queryBuilder()
                        .where(TxtChapterRuleBeanDao.Properties.Rule.eq(rule))
                        .limit(1).unique();
                if (ruleBean != null) {
                    if (!ruleBean.getEnable()) {
                        ruleBeanList.add(ruleBean);
                        checkedItem = ruleBeanList.size() - 1;
                    } else {
                        checkedItem = ruleBeanList.indexOf(ruleBean);
                    }
                } else {
                    ruleBean = new TxtChapterRuleBean();
                    ruleBean.setName(rule);
                    ruleBean.setRule(rule);
                    ruleBeanList.add(ruleBean);
                    checkedItem = ruleBeanList.size() - 1;
                }
            }
            for (TxtChapterRuleBean bean : ruleBeanList) {
                ruleNameList.add(bean.getName());
            }
            if (checkedItem < 0) {
                checkedItem = 0;
            }
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.alertDialogTheme)
                    .setTitle("选择目录正则")
                    .setSingleChoiceItems(ruleNameList.toArray(new String[0]), checkedItem, (dialog1, which) -> {
                        if (which < 0) return;
                        mPresenter.getBookShelf().getBookInfoBean().setChapterUrl(ruleBeanList.get(which).getRule());
                        mPresenter.saveProgress();
                        if (mPageLoader != null) {
                            mPageLoader.updateChapter();
                        }
                        dialog1.dismiss();
                    })
                    .setPositiveButton("管理正则", (dialog12, which) -> TxtChapterRuleActivity.startThis(ReadBookActivity.this))
                    .show();
            ATH.setAlertDialogTint(dialog);
        }
    }

    /**
     * 显示调节
     */
    private void readAdjustIn() {
        binding.flMenu.setVisibility(View.VISIBLE);
        binding.readAdjustPop.show();
        binding.readAdjustPop.setVisibility(View.VISIBLE);
        binding.readAdjustPop.startAnimation(menuBottomIn);
    }

    /**
     * 显示自定义边界调节
     */
    public void readAdjustMarginIn() {
        binding.flMenu.setVisibility(View.VISIBLE);
        binding.readAdjustMarginPop.show();
        binding.readAdjustMarginPop.setVisibility(View.VISIBLE);
        binding.readAdjustMarginPop.startAnimation(menuBottomIn);
    }

    /**
     * 显示界面设置
     */
    private void readInterfaceIn() {
        binding.flMenu.setVisibility(View.VISIBLE);
        binding.readInterfacePop.setVisibility(View.VISIBLE);
        binding.readInterfacePop.startAnimation(menuBottomIn);
    }

    /**
     * 显示更多设置
     */
    private void moreSettingIn() {
        binding.flMenu.setVisibility(View.VISIBLE);
        binding.moreSettingPop.setVisibility(View.VISIBLE);
        binding.moreSettingPop.startAnimation(menuBottomIn);
    }

    /**
     * 显示菜单
     */
    private void popMenuIn() {
        binding.flMenu.setVisibility(View.VISIBLE);
        binding.llMenuTop.setVisibility(View.VISIBLE);
        binding.readMenuBottom.setVisibility(View.VISIBLE);
        binding.llMenuTop.startAnimation(menuTopIn);
        binding.readMenuBottom.startAnimation(menuBottomIn);
        hideSnackBar();
    }

    /**
     * 隐藏菜单
     */
    private void popMenuOut() {
        if (binding.flMenu.getVisibility() == View.VISIBLE) {
            if (binding.llMenuTop.getVisibility() == View.VISIBLE) {
                binding.llMenuTop.startAnimation(menuTopOut);
            }
            if (binding.readMenuBottom.getVisibility() == View.VISIBLE) {
                binding.readMenuBottom.startAnimation(menuBottomOut);
            }
            if (binding.moreSettingPop.getVisibility() == View.VISIBLE) {
                binding.moreSettingPop.startAnimation(menuBottomOut);
            }
            if (binding.readAdjustPop.getVisibility() == View.VISIBLE) {
                binding.readAdjustPop.startAnimation(menuBottomOut);
            }
            if (binding.readInterfacePop.getVisibility() == View.VISIBLE) {
                binding.readInterfacePop.startAnimation(menuBottomOut);
            }
            if (binding.readAdjustMarginPop.getVisibility() == View.VISIBLE) {
                binding.readAdjustMarginPop.startAnimation(menuBottomOut);
            }
        }
    }

    /**
     * 朗读
     */
    private void readAloud() {
        aloudNextPage = false;
        String unReadContent = mPageLoader.getUnReadContent();
        if (mPresenter.getBookShelf() != null && mPageLoader != null && !StringUtils.isTrimEmpty(unReadContent)) {
            ReadAloudService.play(ReadBookActivity.this, false, unReadContent,
                    mPresenter.getBookShelf().getBookInfoBean().getName(),
                    ChapterContentHelp.getInstance().replaceContent(mPresenter.getBookShelf().getBookInfoBean().getName(),
                            mPresenter.getBookShelf().getTag(),
                            mPresenter.getBookShelf().getDurChapterName(),
                            mPresenter.getBookShelf().getReplaceEnable()),
                    mPresenter.getBookShelf().isAudio(),
                    mPresenter.getBookShelf().getDurChapterPage());
        }
    }

    /**
     * 检查是否加入书架
     */
    public boolean checkAddShelf() {
        if (isAdd || mPresenter.getBookShelf() == null
                || TextUtils.isEmpty(mPresenter.getBookShelf().getBookInfoBean().getName())) {
            return true;
        } else if (mPresenter.getChapterList().isEmpty()) {
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
                checkAddShelfPop.showAtLocation(binding.flContent, Gravity.CENTER, 0, 0);
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
                binding.readMenuBottom.setFabReadAloudImage(R.drawable.ic_pause_outline_24dp);
                binding.readMenuBottom.setReadAloudTimer(true);
                binding.mediaPlayerPop.setFabReadAloudImage(R.drawable.ic_pause_24dp);
                binding.mediaPlayerPop.setSeekBarEnable(true);
                break;
            case PAUSE:
                binding.readMenuBottom.setFabReadAloudImage(R.drawable.ic_play_outline_24dp);
                binding.readMenuBottom.setReadAloudTimer(true);
                binding.mediaPlayerPop.setFabReadAloudImage(R.drawable.ic_play_24dp);
                binding.mediaPlayerPop.setSeekBarEnable(false);
                break;
            default:
                binding.readMenuBottom.setFabReadAloudImage(R.drawable.ic_read_aloud);
                binding.readMenuBottom.setReadAloudTimer(false);
                binding.mediaPlayerPop.setFabReadAloudImage(R.drawable.ic_play_24dp);
                binding.pageView.drawPage(0);
                binding.pageView.invalidate();
                binding.pageView.drawPage(-1);
                binding.pageView.drawPage(1);
                binding.pageView.invalidate();
        }
    }

    /**
     * 更新定时
     */
    @Override
    public void upAloudTimer(String text) {
        binding.readMenuBottom.setReadAloudTimer(text);
    }

    /**
     * 开始朗读第start个字符
     */
    @Override
    public void readAloudStart(int start) {
        aloudNextPage = true;
        if (mPageLoader != null) {
            mPageLoader.readAloudStart(start);
        }
    }

    /**
     * 朗读长度
     */
    @Override
    public void readAloudLength(int readAloudLength) {
        if (mPageLoader != null && aloudNextPage) {
            mPageLoader.readAloudLength(readAloudLength);
        }
    }

    /**
     * 刷新
     */
    @Override
    public void refresh(boolean recreate) {
        if (recreate) {
            recreate();
        } else {
            binding.flContent.setBackground(readBookControl.getTextBackground(this));
            if (mPageLoader != null) {
                mPageLoader.refreshUi();
            }
            binding.readInterfacePop.setBg();
            initImmersionBar();
        }
    }

    /**
     * 按键事件
     */
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
                if (binding.readInterfacePop.getVisibility() == View.VISIBLE
                        || binding.readAdjustPop.getVisibility() == View.VISIBLE
                        || binding.readAdjustMarginPop.getVisibility() == View.VISIBLE
                        || binding.moreSettingPop.getVisibility() == View.VISIBLE) {
                    popMenuOut();
                    return true;
                } else if (binding.flMenu.getVisibility() == View.VISIBLE) {
                    finish();
                    return true;
                } else if (ReadAloudService.running && aloudStatus == ReadAloudService.Status.PLAY) {
                    ReadAloudService.pause(this);
                    if (!mPresenter.getBookShelf().isAudio()) {
                        toast(R.string.read_aloud_pause);
                    }
                    return true;
                } else {
                    finish();
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (binding.flMenu.getVisibility() == View.VISIBLE) {
                    popMenuOut();
                } else {
                    popMenuIn();
                }
                return true;
            } else if (binding.flMenu.getVisibility() != View.VISIBLE) {
                if (keyCode == preferences.getInt("nextKeyCode", 0)) {
                    if (mPageLoader != null && keyCode != 0) {
                        mPageLoader.skipToNextPage();
                    }
                    return true;
                }
                if (keyCode == preferences.getInt("prevKeyCode", 0)) {
                    if (mPageLoader != null && keyCode != 0) {
                        mPageLoader.skipToPrePage();
                    }
                    return true;
                }
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
        if (binding.flMenu.getVisibility() != View.VISIBLE) {
            if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.Status.PLAY)
                    && keyCode != 0
                    && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                    || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    || keyCode == preferences.getInt("nextKeyCode", 0)
                    || keyCode == preferences.getInt("prevKeyCode", 0))) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 更新菜单
     */
    @Override
    public void upMenu() {
        if (menu == null) return;
        boolean onLine = mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG);
        if (onLine) {
            binding.tvChapterUrl.setVisibility(View.VISIBLE);
            binding.atvLine.setVisibility(View.VISIBLE);
        } else {
            binding.tvChapterUrl.setVisibility(View.GONE);
            binding.atvLine.setVisibility(View.GONE);
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
                    menu.getItem(i).setVisible(mPresenter.getBookSource() != null && !TextUtils.isEmpty(mPresenter.getBookSource().getLoginUrl()));
                    break;
            }
            if (menu.getItem(i).getItemId() == R.id.enable_replace) {
                if (mPresenter.getBookShelf() != null && mPresenter.getBookShelf().getReplaceEnable()) {
                    menu.getItem(i).setChecked(true);
                } else {
                    menu.getItem(i).setChecked(false);
                }
            }
        }

    }

    /**
     * 更新音频长度
     */
    @Override
    public void upAudioSize(int audioSize) {
        binding.mediaPlayerPop.upAudioSize(audioSize);
    }

    /**
     * 更新播放进度
     */
    @Override
    public void upAudioDur(int audioDur) {
        binding.mediaPlayerPop.upAudioDur(audioDur);
        mPresenter.getBookShelf().setDurChapterPage(audioDur);
        mPresenter.saveProgress();
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
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.please_grant_storage_permission)
                .onGranted((requestCode) -> {
                    mPresenter.openBookFromOther(ReadBookActivity.this);
                    return Unit.INSTANCE;
                })
                .request();
    }

    /**
     * 朗读按钮
     */
    @Override
    public void onMediaButton(String cmd) {
        if (!ReadAloudService.running) {
            aloudStatus = ReadAloudService.Status.STOP;
            SystemUtil.ignoreBatteryOptimization(this);
        }
        switch (aloudStatus) {
            case PAUSE:
                switch (cmd) {
                    case ReadAloudService.ActionMediaPlay:
                        ReadAloudService.resume(this);
                        binding.readMenuBottom.setFabReadAloudText(getString(R.string.read_aloud));
                        break;
                    case ReadAloudService.ActionMediaPrev:
                        //停止倒计时
                        ReadAloudService.setTimer(getContext(), ReadAloudService.maxTimeMinute + 1);
                        //语音提示倒计时结束
                        ReadAloudService.tts_ui_timer_stop(this);
                        break;
                    case ReadAloudService.ActionMediaNext:
                        //翻到上一章并开始朗读
                        if (mPageLoader != null) {
                            mPageLoader.skipPreChapter();
                        }
                        ReadAloudService.resume(this);
                        binding.readMenuBottom.setFabReadAloudText(getString(R.string.read_aloud));
                        break;
                }
                break;
            case PLAY:
                switch (cmd) {
                    case ReadAloudService.ActionMediaPlay:
                        ReadAloudService.pause(this);
                        binding.readMenuBottom.setFabReadAloudText(getString(R.string.read_aloud_pause));
                        break;
                    case ReadAloudService.ActionMediaPrev:
                        //倒计时增加
                        ReadAloudService.setTimer(getContext(), 10);
                        //语音提示剩余时间
                        ReadAloudService.tts_ui_timer_remaining(this);
                        break;
                    case ReadAloudService.ActionMediaNext:
                        //翻到下一章
                        if (mPageLoader != null) {
                            mPageLoader.skipNextChapter();
                        }
                        break;
                }
                break;
            default:
                ReadBookActivity.this.popMenuOut();
                readAloud();
        }
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
        SoftInputUtil.hideIMM(getCurrentFocus());
        if (batInfoReceiver == null) {
            batInfoReceiver = new ThisBatInfoReceiver();
            batInfoReceiver.registerThis();
        }
        screenOffTimerStart();
        if (mPageLoader != null) {
            if (!mPageLoader.updateBattery(BatteryUtil.getLevel(this))) {
                mPageLoader.updateTime();
            }
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
        Backup.INSTANCE.autoBack();
        super.finish();
    }

    @Override
    public void changeSourceFinish(BookShelfBean book) {
        if (mPageLoader != null && mPageLoader instanceof PageLoaderNet) {
            ((PageLoaderNet) mPageLoader).changeSourceFinish(book);
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
