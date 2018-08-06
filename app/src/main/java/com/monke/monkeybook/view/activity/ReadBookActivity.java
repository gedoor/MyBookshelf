//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IReadBookPresenter;
import com.monke.monkeybook.service.ReadAloudService;
import com.monke.monkeybook.utils.BatteryUtil;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.PremissionCheck;
import com.monke.monkeybook.utils.SystemUtil;
import com.monke.monkeybook.utils.barUtil.BarHide;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.impl.IReadBookView;
import com.monke.monkeybook.view.popupwindow.CheckAddShelfPop;
import com.monke.monkeybook.view.popupwindow.MoreSettingPop;
import com.monke.monkeybook.view.popupwindow.ReadAdjustPop;
import com.monke.monkeybook.view.popupwindow.ReadInterfacePop;
import com.monke.monkeybook.widget.ChapterListView;
import com.monke.monkeybook.widget.modialog.EditBookmarkView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;
import com.monke.monkeybook.widget.page.PageLoader;
import com.monke.monkeybook.widget.page.PageView;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.grantland.widget.AutofitTextView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static com.monke.monkeybook.presenter.ReadBookPresenterImpl.OPEN_FROM_OTHER;
import static com.monke.monkeybook.service.ReadAloudService.ActionNewReadAloud;
import static com.monke.monkeybook.service.ReadAloudService.NEXT;
import static com.monke.monkeybook.service.ReadAloudService.PAUSE;
import static com.monke.monkeybook.service.ReadAloudService.PLAY;

public class ReadBookActivity extends MBaseActivity<IReadBookPresenter> implements IReadBookView {
    private final int ResultReplace = 101;
    private final int RESULT_OPEN_OTHER_PERMS = 102;
    public final int ResultSelectFont = 104;
    public final int ResultStyleSet = 105;

    @BindView(R.id.fl_content)
    FrameLayout flContent;
    @BindView(R.id.fl_menu)
    FrameLayout flMenu;
    @BindView(R.id.v_menu_bg)
    View vMenuBg;
    @BindView(R.id.ll_menu_bottom)
    LinearLayout llMenuBottom;
    @BindView(R.id.tv_pre)
    TextView tvPre;
    @BindView(R.id.tv_next)
    TextView tvNext;
    @BindView(R.id.hpb_read_progress)
    MHorProgressBar hpbReadProgress;
    @BindView(R.id.ll_catalog)
    LinearLayout llCatalog;
    @BindView(R.id.ll_light)
    LinearLayout llLight;
    @BindView(R.id.ll_font)
    LinearLayout llFont;
    @BindView(R.id.ll_setting)
    LinearLayout llSetting;
    @BindView(R.id.clp_chapterList)
    ChapterListView chapterListView;
    @BindView(R.id.tv_read_aloud_timer)
    TextView tvReadAloudTimer;
    @BindView(R.id.ll_read_aloud_timer)
    LinearLayout llReadAloudTimer;
    @BindView(R.id.ivCList)
    ImageView ivCList;
    @BindView(R.id.ivAdjust)
    ImageView ivAdjust;
    @BindView(R.id.ivInterface)
    ImageView ivInterface;
    @BindView(R.id.ivSetting)
    ImageView ivSetting;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.atv_url)
    AutofitTextView atvUrl;
    @BindView(R.id.ll_menu_top)
    LinearLayout llMenuTop;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.ll_ISB)
    LinearLayout llISB;
    @BindView(R.id.llNavigationBar)
    LinearLayout llNavigationBar;
    @BindView(R.id.fabReadAloud)
    FloatingActionButton fabReadAloud;
    @BindView(R.id.fab_read_aloud_timer)
    FloatingActionButton fabReadAloudTimer;
    @BindView(R.id.fabReplaceRule)
    FloatingActionButton fabReplaceRule;
    @BindView(R.id.fabNightTheme)
    FloatingActionButton fabNightTheme;
    @BindView(R.id.pageView)
    PageView pageView;
    @BindView(R.id.read_tv_page_tip)
    TextView readTvPageTip;

    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;
    private ActionBar actionBar;
    private Timer mTimer;
    private PageLoader mPageLoader;

    private String noteUrl;
    private Boolean isAdd = false; //判断是否已经添加进书架
    private int aloudStatus;
    private int screenTimeOut;

    private Menu menu;
    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private CheckAddShelfPop checkAddShelfPop;
    private ReadAdjustPop readAdjustPop;
    private ReadInterfacePop readInterfacePop;
    private MoreSettingPop moreSettingPop;
    private MoProgressHUD moProgressHUD;
    private ThisBatInfoReceiver batInfoReceiver;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    @SuppressLint("SimpleDateFormat")
    private DateFormat dfTime = new SimpleDateFormat("HH:mm");

    private Boolean showCheckPermission = false;


    @Override
    protected IReadBookPresenter initInjector() {
        return new ReadBookPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            noteUrl = savedInstanceState.getString("noteUrl");
            aloudStatus = savedInstanceState.getInt("aloudStatus");
            isAdd = savedInstanceState.getBoolean("isAdd");
        }
        readBookControl.setLineChange(System.currentTimeMillis());
        readBookControl.initTextDrawableIndex();
        super.onCreate(savedInstanceState);
        screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[readBookControl.getScreenTimeOut()];
        batInfoReceiver = new ThisBatInfoReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batInfoReceiver, filter);
    }

    @Override
    protected void onCreateActivity() {
        setOrientation();
        setContentView(R.layout.activity_book_read);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPresenter.getBookShelf() != null) {
            outState.putString("noteUrl", mPresenter.getBookShelf().getNoteUrl());
            outState.putInt("aloudStatus", aloudStatus);
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
                llNavigationBar.setPadding(0, 0, 0, ImmersionBar.getNavigationBarHeight(this));
            }
        }

        if (flMenu.getVisibility() == View.VISIBLE) {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
        } else if (chapterListView.getVisibility() == View.VISIBLE) {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            if (readBookControl.getHideNavigationBar()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
            } else {
                mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
            }
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
            } else if (readBookControl.getHideNavigationBar()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
            } else {
                mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
            }

        }

        mImmersionBar.init();
        keepScreenOn(screenTimeOut != 0);
        screenOff();
    }

    public void keepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void screenOff() {
        int screenOffTime = screenTimeOut * 1000 - SystemUtil.getScreenOffTime(this);
        if (screenOffTime > 0) {
            if (mTimer != null) {
                mTimer.cancel();
            }
            mTimer = new Timer();
            keepScreenOn(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> keepScreenOn(false));
                }
            }, screenOffTime);
        } else if (screenTimeOut != -1) {
            keepScreenOn(false);
        }
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
                vMenuBg.setOnClickListener(v -> popMenuOut());
                initImmersionBar();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        menuBottomIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_in);
        //隐藏菜单
        menuTopOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_out);
        menuTopOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                vMenuBg.setOnClickListener(null);
                initImmersionBar();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flMenu.setVisibility(View.INVISIBLE);
                initImmersionBar();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        menuBottomOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_out);

    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        mPresenter.initData(this);
        llISB.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0);
        //图标眷色
        ivCList.getDrawable().mutate();
        ivCList.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivAdjust.getDrawable().mutate();
        ivAdjust.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivInterface.getDrawable().mutate();
        ivInterface.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivSetting.getDrawable().mutate();
        ivSetting.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        if (isNightTheme()) {
            fabNightTheme.setImageResource(R.drawable.ic_daytime_24dp);
        } else {
            fabNightTheme.setImageResource(R.drawable.ic_brightness);
        }
        //弹窗
        moProgressHUD = new MoProgressHUD(this);

        initReadInterfacePop();
        initReadAdjustPop();
        initMoreSettingPop();

    }

    /**
     * 调节
     */
    private void initReadAdjustPop() {
        readAdjustPop = new ReadAdjustPop(this, new ReadAdjustPop.OnAdjustListener() {
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
                    Toast.makeText(ReadBookActivity.this, "跟随系统需要重新开始朗读", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 界面设置
     */
    private void initReadInterfacePop() {
        readInterfacePop = new ReadInterfacePop(this, new ReadInterfacePop.OnChangeProListener() {

            @Override
            public void upPageMode() {
                mPageLoader.setPageMode(readBookControl.getPageMode(readBookControl.getPageMode()));
            }

            @Override
            public void upTextSize() {
                mPageLoader.setTextSize(readBookControl.getTextSize(), readBookControl.getLineMultiplier());
            }

            @Override
            public void upMargin() {
                readBookControl.setLineChange(System.currentTimeMillis());
                mPageLoader.setMargin(readBookControl.getPaddingTop(), readBookControl.getPaddingBottom(), readBookControl.getPaddingLeft(), readBookControl.getPaddingRight());
            }

            @Override
            public void bgChange() {
                mPageLoader.setPageStyle();
                readBookControl.initTextDrawableIndex();
                initImmersionBar();
            }

            @Override
            public void refresh() {
                mPageLoader.initPaint();
            }

        });
    }

    /**
     * 其它设置
     */
    private void initMoreSettingPop() {
        moreSettingPop = new MoreSettingPop(this, new MoreSettingPop.OnChangeProListener() {
            @Override
            public void keepScreenOnChange(int keepScreenOn) {
                screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[keepScreenOn];
                keepScreenOn(screenTimeOut != 0);
            }

            @Override
            public void reLoad() {
                readBookControl.setLineChange(System.currentTimeMillis());
                recreate();
            }
        });
    }

    @Override
    public void setHpbReadProgressMax(int count) {
        hpbReadProgress.setMaxProgress(count);
    }

    /**
     * 加载阅读页面
     */
    private void initPageView() {
        //获取页面加载器
        mPageLoader = pageView.getPageLoader(this, mPresenter.getBookShelf());
        mPageLoader.updateBattery(BatteryUtil.getLevel(this));
        mPageLoader.setOnPageChangeListener(
                new PageLoader.OnPageChangeListener() {

                    @Override
                    public void onChapterChange(int pos) {
                        actionBar.setTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
                        if (mPresenter.getBookShelf().getChapterListSize() > 0) {
                            atvUrl.setText(mPresenter.getBookShelf().getChapterList(pos).getDurChapterUrl());
                        } else {
                            atvUrl.setText("");
                        }

                        if (mPresenter.getBookShelf().getChapterListSize() == 1) {
                            tvPre.setEnabled(false);
                            tvNext.setEnabled(false);
                        } else {
                            if (pos == 1) {
                                tvPre.setEnabled(false);
                                tvNext.setEnabled(true);
                            } else if (pos == mPresenter.getBookShelf().getChapterListSize() - 1) {
                                tvPre.setEnabled(true);
                                tvNext.setEnabled(false);
                            } else {
                                tvPre.setEnabled(true);
                                tvNext.setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void requestChapters(int chapterIndex) {
                        mPresenter.loadContent(chapterIndex);
                        //隐藏提示
                        readTvPageTip.setVisibility(GONE);
                    }

                    @Override
                    public void onCategoryFinish(List<ChapterListBean> chapters) {
                        initChapterList();
                    }

                    @Override
                    public void onPageCountChange(int count) {
                        hpbReadProgress.setMaxProgress(Math.max(0, count - 1));
                        hpbReadProgress.setDurProgress(0);
                        // 如果处于错误状态，那么就冻结使用
                        if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING
                                || mPageLoader.getPageStatus() == PageLoader.STATUS_ERROR) {
                            hpbReadProgress.setEnabled(false);
                        } else {
                            hpbReadProgress.setEnabled(true);
                        }
                    }

                    @Override
                    public void onPageChange(int chapterIndex, int pageIndex) {
                        mPresenter.getBookShelf().setDurChapter(chapterIndex);
                        mPresenter.getBookShelf().setDurChapterPage(pageIndex);
                        mPresenter.saveProgress();
                        hpbReadProgress.post(
                                () -> hpbReadProgress.setDurProgress(pageIndex)
                        );
                        if ((aloudStatus == NEXT | aloudStatus == PLAY) & pageIndex >= 0) {
                            if (mPageLoader.getContext(pageIndex) != null) {
                                ReadAloudService.play(ReadBookActivity.this, false, mPageLoader.getContext(pageIndex));
                            }
                        }
                    }
                }
        );
        pageView.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                return true;
            }

            @Override
            public void center() {
                popMenuIn();
            }

            @Override
            public void prePage() {
            }

            @Override
            public void nextPage() {
            }

            @Override
            public void cancel() {
            }
        });
        mPageLoader.refreshChapterList();
    }

    /**
     * 初始化目录列表
     */
    @Override
    public void initChapterList() {
        chapterListView.setOnChangeListener(new ChapterListView.OnChangeListener() {
            @Override
            public void animIn() {
                initImmersionBar();
            }

            @Override
            public void animOut() {
                initImmersionBar();
            }
        });
        chapterListView.setData(mPresenter.getBookShelf(), new ChapterListView.OnItemClickListener() {
            @Override
            public void itemClick(int index, int page, int tabPosition) {
                mPageLoader.skipToChapter(index, page);
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean, int tabPosition) {
                chapterListView.dismissChapterList();
                showBookmark(bookmarkBean);
            }
        });
    }

    @Override
    protected void bindEvent() {
        //阅读进度
        hpbReadProgress.setProgressListener(new OnProgressListener() {
            @Override
            public void moveStartProgress(float dur) {

            }

            @Override
            public void durProgressChange(float dur) {

            }

            @Override
            public void moveStopProgress(float dur) {
                int realDur = (int) Math.ceil(dur);
                if ((realDur) != mPresenter.getBookShelf().getDurChapterPage()) {
                    mPageLoader.skipToPage(realDur);
                }
                if (hpbReadProgress.getDurProgress() != realDur)
                    hpbReadProgress.setDurProgress(realDur);
            }

            @Override
            public void setDurProgress(float dur) {

            }
        });

        //打开URL
        atvUrl.setOnClickListener(view -> {
            try {
                String url = atvUrl.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.can_not_open, Toast.LENGTH_SHORT).show();
            }
        });

        //朗读定时
        fabReadAloudTimer.getDrawable().mutate();
        fabReadAloudTimer.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabReadAloudTimer.setOnClickListener(view -> ReadAloudService.setTimer(this));

        //朗读
        fabReadAloud.getDrawable().mutate();
        fabReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabReadAloud.setOnClickListener(view -> onMediaButton());
        //长按停止朗读
        fabReadAloud.setOnLongClickListener(view -> {
            ReadAloudService.stop(this);
            return true;
        });

        //替换
        fabReplaceRule.getDrawable().mutate();
        fabReplaceRule.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabReplaceRule.setOnClickListener(view -> {
            popMenuOut();
            Intent intent = new Intent(this, ReplaceRuleActivity.class);
            startActivityForResult(intent, ResultReplace);
        });

        //夜间模式
        fabNightTheme.getDrawable().mutate();
        fabNightTheme.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabNightTheme.setOnClickListener(view -> setNightTheme(!isNightTheme()));

        //上一章
        tvPre.setOnClickListener(view -> {
            if (mPresenter.getBookShelf() != null) {
                mPageLoader.skipPreChapter();
            }
        });

        //下一章
        tvNext.setOnClickListener(view -> {
            if (mPresenter.getBookShelf() != null) {
                mPageLoader.skipNextChapter();
            }
        });

        //目录
        llCatalog.setOnClickListener(view -> {
            ReadBookActivity.this.popMenuOut();
            if (chapterListView.hasData()) {
                new Handler().postDelayed(() -> chapterListView.show(mPresenter.getBookShelf().getDurChapter()), menuTopOut.getDuration());
            }
        });

        //亮度
        llLight.setOnClickListener(view -> {
            ReadBookActivity.this.popMenuOut();
            new Handler().postDelayed(() -> readAdjustPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0), menuTopOut.getDuration());
        });

        //界面
        llFont.setOnClickListener(view -> {
            ReadBookActivity.this.popMenuOut();
            new Handler().postDelayed(() -> readInterfacePop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0), menuTopOut.getDuration());
        });

        //设置
        llSetting.setOnClickListener(view -> {
            ReadBookActivity.this.popMenuOut();
            new Handler().postDelayed(() -> moreSettingPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0), menuTopOut.getDuration());
        });

        tvReadAloudTimer.setOnClickListener(null);
    }

    @Override
    public void startLoadingBook() {
        initPageView();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        showOnLineView();
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
                moProgressHUD.showText(mPageLoader.getContext(mPageLoader.getPagePos()));
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
        ReadBookActivity.this.popMenuOut();
        if (mPageLoader != null) {
            mPageLoader.refresh();
        }
    }

    /**
     * 书签
     */
    private void showBookmark(BookmarkBean bookmarkBean) {
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
                bookmarkBean.setChapterName(mPresenter.getChapterTitle(mPresenter.getBookShelf().getDurChapter()));
            }
            moProgressHUD.showBookmark(bookmarkBean, isAdd, new EditBookmarkView.OnBookmarkClick() {
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
                    mPageLoader.skipToChapter(chapterIndex, pageIndex);
                }
            });
        }

    }

    /**
     * 换源
     */
    private void changeSource() {
        ReadBookActivity.this.popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            moProgressHUD.showChangeSource(this, mPresenter.getBookShelf(), searchBookBean -> {
                if (!Objects.equals(searchBookBean.getNoteUrl(), mPresenter.getBookShelf().getNoteUrl())) {
                    mPresenter.changeBookSource(searchBookBean);
                }
            });
        }
    }

    /**
     * 下载
     */
    private void download() {
        ReadBookActivity.this.popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            //弹出离线下载界面
            int endIndex = mPresenter.getBookShelf().getChapterListSize() - 1;
            moProgressHUD.showDownloadList(mPresenter.getBookShelf().getDurChapter(), endIndex,
                    mPresenter.getBookShelf().getChapterListSize(),
                    (start, end) -> {
                        moProgressHUD.dismiss();
                        mPresenter.addDownload(start, end);
                    });
        }
    }

    /**
     * 隐藏菜单
     */
    private void popMenuOut() {
        if (flMenu.getVisibility() == View.VISIBLE) {
            llMenuTop.startAnimation(menuTopOut);
            llMenuBottom.startAnimation(menuBottomOut);
        }
    }

    /**
     * 显示菜单
     */
    private void popMenuIn() {
        flMenu.setVisibility(View.VISIBLE);
        llMenuTop.startAnimation(menuTopIn);
        llMenuBottom.startAnimation(menuBottomIn);
    }

    @Override
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新朗读状态
     */
    @Override
    public void upAloudState(int status) {
        aloudStatus = status;
        switch (status) {
            case NEXT:
                if (!mPageLoader.skipToNextPage()) {
                    ReadAloudService.stop(this);
                }
                break;
            case PLAY:
                fabReadAloud.setImageResource(R.drawable.ic_pause2);
                llReadAloudTimer.setVisibility(View.VISIBLE);
                break;
            case PAUSE:
                fabReadAloud.setImageResource(R.drawable.ic_play2);
                llReadAloudTimer.setVisibility(View.VISIBLE);
                break;
            default:
                fabReadAloud.setImageResource(R.drawable.ic_read_aloud);
                llReadAloudTimer.setVisibility(View.INVISIBLE);
        }
        fabReadAloud.getDrawable().mutate();
        fabReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void upAloudTimer(String text) {
        fabReadAloudTimer.setContentDescription(text);
        tvReadAloudTimer.setText(text);
    }

    @Override
    public void speakIndex(int speakIndex) {
//        runOnUiThread(() -> csvBook.speakStart(speakIndex));
    }

    /**
     * 检查是否加入书架
     */
    public boolean checkAddShelf() {
        if (isAdd || mPresenter.getBookShelf() == null) {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (flMenu.getVisibility() == View.VISIBLE) {
                    finish();
                    return true;
                } else if (chapterListView.dismissChapterList()) {
                    return true;
                } else if (ReadAloudService.running && aloudStatus == PLAY) {
                    ReadAloudService.pause(this);
                    Toast.makeText(this, R.string.read_aloud_pause, Toast.LENGTH_SHORT).show();
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
            } else if (flMenu.getVisibility() != View.VISIBLE & chapterListView.getVisibility() != View.VISIBLE) {
                if (readBookControl.getCanKeyTurn() && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    mPageLoader.skipToNextPage();
                    return true;
                } else if (readBookControl.getCanKeyTurn() && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    mPageLoader.skipToPrePage();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_SPACE) {
                    mPageLoader.skipToNextPage();
                    return true;
                }
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (flMenu.getVisibility() != View.VISIBLE & chapterListView.getVisibility() != View.VISIBLE) {
            if (readBookControl.getCanKeyTurn()
                    && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
                return false;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void loadLocationBookError(String errMsg) {
        if (mPageLoader != null) {
            mPageLoader.chapterError();
        }
    }

    @Override
    public void showOnLineView() {
        if (mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            atvUrl.setVisibility(View.VISIBLE);
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    if (menu.getItem(i).getGroupId() == R.id.menuOnLine) {
                        menu.getItem(i).setVisible(true);
                        menu.getItem(i).setEnabled(true);
                    }
                }
            }
        } else if (mPresenter.getBookShelf() != null && mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            atvUrl.setVisibility(View.GONE);
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    if (menu.getItem(i).getGroupId() == R.id.menuOnLine) {
                        menu.getItem(i).setVisible(false);
                        menu.getItem(i).setEnabled(false);
                    }
                }
            }
        }
    }

    @Override
    public String getNoteUrl() {
        if (isEmpty(noteUrl)) {
            noteUrl = readBookControl.getLastNoteUrl();
        }
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
    public void finishContent() {
        if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
            mPageLoader.openChapter(mPresenter.getBookShelf().getDurChapterPage());
        }
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void showLoading(String msg) {
        moProgressHUD.showLoading(msg);
    }

    @Override
    public void dismissLoading() {
        moProgressHUD.dismiss();
    }

    @Override
    public void openBookFromOther() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            mPresenter.openBookFromOther(this);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.open_from_other),
                    RESULT_OPEN_OTHER_PERMS, perms);
        }
    }

    /**
     * 更新目录
     */
    @Override
    public void chapterChange(ChapterListBean chapterListBean) {
        if (chapterListView.hasData()) {
            chapterListView.upChapterList(chapterListBean);
        }
    }

    /**
     * 朗读按钮
     */
    @Override
    public void onMediaButton() {
        if (!ReadAloudService.running) {
            aloudStatus = ReadAloudService.STOP;
        }
        switch (aloudStatus) {
            case PAUSE:
                ReadAloudService.resume(this);
                break;
            case PLAY:
                ReadAloudService.pause(this);
                break;
            default:
                ReadBookActivity.this.popMenuOut();
                if (mPresenter.getBookShelf() != null) {
                    ReadAloudService.play(this, true, mPageLoader.getContext(mPageLoader.getPagePos()));
                }
        }
    }

    @AfterPermissionGranted(RESULT_OPEN_OTHER_PERMS)
    private void onResultOpenOtherPerms() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            mPresenter.openBookFromOther(this);
        } else {
            Toast.makeText(this, "未获取到权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ResultReplace:
                recreate();
                break;
            case ResultSelectFont:
                if (resultCode == RESULT_OK && null != data) {
                    String path = FileUtil.getPath(this, data.getData());
                    try {
                        //判断是否字体文件或字体是否损坏
                        Typeface.createFromFile(path);
                        readInterfacePop.setReadFonts(path);
                    } catch (Exception e) {
                        Toast.makeText(this, "不是字体文件", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case ResultStyleSet:
                if (resultCode == RESULT_OK) {
                    readBookControl.initTextDrawableIndex();
                    mPageLoader.initPaint();
                    readInterfacePop.setBg();
                }
                break;
        }
        initImmersionBar();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onResume() {
        super.onResume();
        screenOff();
        if (readBookControl.getHideStatusBar()) {
            if (mPageLoader != null) {
                mPageLoader.updateTime();
                mPageLoader.updateBattery(BatteryUtil.getLevel(this));
            }
        }
        if (showCheckPermission && mPresenter.getOpen_from() == OPEN_FROM_OTHER && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PremissionCheck.checkPremission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            showCheckPermission = true;
            mPresenter.openBookFromOther(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batInfoReceiver);
        ReadAloudService.stop(this);
        mPageLoader.closeBook();
        mPageLoader = null;
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
        super.finish();
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
    }


}
