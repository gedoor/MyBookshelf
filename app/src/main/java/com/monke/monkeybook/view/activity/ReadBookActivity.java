//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
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
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IReadBookPresenter;
import com.monke.monkeybook.service.ReadAloudService;
import com.monke.monkeybook.utils.BatteryUtil;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.PremissionCheck;
import com.monke.monkeybook.utils.barUtil.BarHide;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.impl.IReadBookView;
import com.monke.monkeybook.view.popupwindow.CheckAddShelfPop;
import com.monke.monkeybook.view.popupwindow.MoreSettingPop;
import com.monke.monkeybook.view.popupwindow.ReadAdjustPop;
import com.monke.monkeybook.view.popupwindow.ReadInterfacePop;
import com.monke.monkeybook.widget.ChapterListView;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;
import com.monke.monkeybook.widget.contentswitchview.ContentSwitchView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import at.markushi.ui.CircleButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.grantland.widget.AutofitTextView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.text.TextUtils.isEmpty;
import static com.monke.monkeybook.presenter.ReadBookPresenterImpl.OPEN_FROM_OTHER;
import static com.monke.monkeybook.service.ReadAloudService.ActionNewReadAloud;
import static com.monke.monkeybook.service.ReadAloudService.PAUSE;
import static com.monke.monkeybook.service.ReadAloudService.PLAY;

public class ReadBookActivity extends MBaseActivity<IReadBookPresenter> implements IReadBookView {
    private final int ResultReplace = 101;
    private final int RESULT_OPEN_OTHER_PERMS = 102;
    public final int ResultSelectFont = 104;
    public final int ResultStyleSet = 105;

    @BindView(R.id.fl_content)
    FrameLayout flContent;
    @BindView(R.id.csv_book)
    ContentSwitchView csvBook;
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
    @BindView(R.id.ib_read_aloud)
    CircleButton ibReadAloud;
    @BindView(R.id.ib_replace_rule)
    CircleButton ibReplaceRule;
    @BindView(R.id.ib_night_theme)
    CircleButton ibNightTheme;
    @BindView(R.id.ib_read_aloud_timer)
    CircleButton ibReadAloudTimer;
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

    //主菜单动画
    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;
    private ActionBar actionBar;
    private boolean isMenuShow;

    private boolean aloudButton;
    private boolean hideStatusBar;
    private String noteUrl;
    private Boolean isAdd = false; //判断是否已经添加进书架
    private int aloudStatus;
    private boolean fromMediaButton = false;

    private Menu menu;
    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private CheckAddShelfPop checkAddShelfPop;
    private ReadAdjustPop readAdjustPop;
    private ReadInterfacePop readInterfacePop;
    private MoreSettingPop moreSettingPop;
    private MoProgressHUD moProgressHUD;
    private Intent readAloudIntent;
    private ServiceConnection conn;
    private ThisBatInfoReceiver batInfoReceiver;
    private ContentSwitchView.LoadDataListener loadDataListener;
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
        hideStatusBar = readBookControl.getHideStatusBar();
        readAloudIntent = new Intent(this, ReadAloudService.class);
        readAloudIntent.setAction(ActionNewReadAloud);
        Intent intent = this.getIntent();
        fromMediaButton = intent.getBooleanExtra("readAloud", false);
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
                llMenuBottom.setPadding(0, 0, 0, ImmersionBar.getNavigationBarHeight(this));
            }
        }
        if (isMenuShow) {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
        } else {
            if (!isImmersionBarEnabled()) {
                mImmersionBar.statusBarDarkFont(false);
            } else if (readBookControl.getDarkStatusIcon()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }

                if (hideStatusBar && readBookControl.getHideNavigationBar()) {
                    mImmersionBar.hideBar(BarHide.FLAG_HIDE_BAR);
                } else if (hideStatusBar){
                    mImmersionBar.hideBar(BarHide.FLAG_HIDE_STATUS_BAR);
                } else if (readBookControl.getHideNavigationBar()) {
                    mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
                } else {
                    mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
                }

        }

        mImmersionBar.init();
    }

    @Override
    protected void initData() {
        initServiceConn();
        initLoadDataListener();
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
        initCsvBook();
        llISB.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0);
        if (readBookControl.getKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
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
            ibNightTheme.setImageResource(R.drawable.ic_daytime_24dp);
        } else {
            ibNightTheme.setImageResource(R.drawable.ic_brightness_2_black_24dp_new);
        }
        //弹窗
        moProgressHUD = new MoProgressHUD(this);
        //目录初始化
        chapterListView.setOnChangeListener(new ChapterListView.OnChangeListener() {
            @Override
            public void animIn() {
                isMenuShow = true;
                initImmersionBar();
            }

            @Override
            public void animOut() {
                isMenuShow = false;
                initImmersionBar();
            }
        });
        //界面设置
        readInterfacePop = new ReadInterfacePop(this, new ReadInterfacePop.OnChangeProListener() {
            @Override
            public void textSizeChange() {
                readBookControl.setLineChange(System.currentTimeMillis());
                csvBook.changeTextSize();
            }

            @Override
            public void lineSizeChange() {
                readBookControl.setLineChange(System.currentTimeMillis());
                csvBook.changeLineSize();
            }

            @Override
            public void bgChange() {
                csvBook.changeBg();
                readBookControl.initTextDrawableIndex();
                initImmersionBar();
            }

            @Override
            public void setFont() {
                readBookControl.setLineChange(System.currentTimeMillis());
                csvBook.setFont();
                recreate();
            }

            @Override
            public void setConvert() {
                readBookControl.setLineChange(System.currentTimeMillis());
                csvBook.changeTextSize();
            }

            @Override
            public void setBold() {
                csvBook.setTextBold();
            }

        });
        //其它设置
        moreSettingPop = new MoreSettingPop(this, new MoreSettingPop.OnChangeProListener() {
            @Override
            public void keepScreenOnChange(Boolean keepScreenOn) {
                if (keepScreenOn) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void reLoad() {
                readBookControl.setLineChange(System.currentTimeMillis());
                recreate();
            }
        });
        //调节
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

    @Override
    public void setHpbReadProgressMax(int count) {
        hpbReadProgress.setMaxProgress(count);
    }

    private void initCsvBook() {
        csvBook.bookReadInit(() -> mPresenter.initData(ReadBookActivity.this));
    }

    /**
     * 初始化目录列表
     */
    @Override
    public void initChapterList() {
        chapterListView.setData(mPresenter.getBookShelf(), index -> csvBook
                .setInitData(index, mPresenter.getBookShelf().getChapterListSize(),
                        BookContentView.DurPageIndexBegin));
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
                if ((realDur) != csvBook.getDurContentView().getDurPageIndex()) {
                    csvBook.setInitData(mPresenter.getBookShelf().getDurChapter(),
                            mPresenter.getBookShelf().getChapterListSize(),
                            realDur);
                }
                if (hpbReadProgress.getDurProgress() != realDur)
                    hpbReadProgress.setDurProgress(realDur);
            }

            @Override
            public void setDurProgress(float dur) {

            }
        });

        //正文
        csvBook.setLoadDataListener(loadDataListener);

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
        ibReadAloudTimer.getDrawable().mutate();
        ibReadAloudTimer.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ibReadAloudTimer.setOnClickListener(view -> ReadAloudService.setTimer(this));

        //朗读
        ibReadAloud.getDrawable().mutate();
        ibReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ibReadAloud.setOnClickListener(view -> onMediaButton());

        //替换
        ibReplaceRule.getDrawable().mutate();
        ibReplaceRule.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ibReplaceRule.setOnClickListener(view -> {
            popMenuOut();
            Intent intent = new Intent(this, ReplaceRuleActivity.class);
            startActivityForResult(intent, ResultReplace);
        });

        //夜间模式
        ibNightTheme.getDrawable().mutate();
        ibNightTheme.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ibNightTheme.setOnClickListener(view -> setNightTheme(!isNightTheme()));

        //上一章
        tvPre.setOnClickListener(view -> {
            if (mPresenter.getBookShelf() != null) {
                csvBook.setInitData(mPresenter.getBookShelf().getDurChapter() - 1,
                        mPresenter.getBookShelf().getChapterListSize(),
                        BookContentView.DurPageIndexBegin);
            }
        });

        //下一章
        tvNext.setOnClickListener(view -> {
            if (mPresenter.getBookShelf() != null) {
                csvBook.setInitData(mPresenter.getBookShelf().getDurChapter() + 1,
                        mPresenter.getBookShelf().getChapterListSize(),
                        BookContentView.DurPageIndexBegin);
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
    public Paint getPaint() {
        return csvBook.getTextPaint();
    }

    @Override
    public void initContentSuccess(int durChapterIndex, int chapterAll, int durPageIndex) {
        csvBook.setInitData(durChapterIndex, chapterAll, durPageIndex);
    }

    @Override
    public void startLoadingBook() {
        csvBook.startLoading();
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
                refresh();
                break;
            case R.id.action_download:
                download();
                break;
            case R.id.action_copy_text:
                popMenuOut();
                moProgressHUD.showText(csvBook.getDurContentView().getContent());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 刷新
     */
    private void refresh() {
        ReadBookActivity.this.popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().deleteByKey(mPresenter.getBookShelf()
                    .getDurChapterListBean().getDurChapterUrl());
            mPresenter.getBookShelf().getDurChapterListBean()
                    .setBookContentBean(null);
            csvBook.setInitData(mPresenter.getBookShelf().getDurChapter(),
                    mPresenter.getBookShelf().getChapterListSize(),
                    BookContentView.DurPageIndexBegin);
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
                    csvBook.showLoading();
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
        isMenuShow = false;
        if (flMenu.getVisibility() == View.VISIBLE) {
            llMenuTop.startAnimation(menuTopOut);
            llMenuBottom.startAnimation(menuBottomOut);
        }
    }

    /**
     * 显示菜单
     */
    private void popMenuIn() {
        isMenuShow = true;
        flMenu.setVisibility(View.VISIBLE);
        llMenuTop.startAnimation(menuTopIn);
        llMenuBottom.startAnimation(menuBottomIn);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 朗读服务
     */
    private void initServiceConn() {
        conn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //返回一个MsgService对象
                ReadAloudService readAloudService = ((ReadAloudService.MyBinder) service).getService();
                readAloudService.setAloudServiceListener(new ReadAloudService.AloudServiceListener() {
                    @Override
                    public void stopService() {
                        csvBook.readAloudStop();
                        try {
                            unbindService(conn);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void readAloudNext() {
                        runOnUiThread(() -> csvBook.readAloudNext());
                    }

                    @Override
                    public void showMassage(String msg) {
                        runOnUiThread(() -> toast(msg));
                    }

                    @Override
                    public void setStatus(int status) {
                        aloudStatus = status;
                        switch (status) {
                            case PLAY:
                                ibReadAloud.setImageResource(R.drawable.ic_pause2);
                                llReadAloudTimer.setVisibility(View.VISIBLE);
                                break;
                            case PAUSE:
                                ibReadAloud.setImageResource(R.drawable.ic_play2);
                                llReadAloudTimer.setVisibility(View.VISIBLE);
                                break;
                            default:
                                ibReadAloud.setImageResource(R.drawable.ic_read_aloud);
                                llReadAloudTimer.setVisibility(View.INVISIBLE);
                        }
                        ibReadAloud.getDrawable().mutate();
                        ibReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
                    }

                    @Override
                    public void upTimer(String text) {
                        ibReadAloud.setContentDescription(text);
                        tvReadAloudTimer.setText(text);
                    }

                    @Override
                    public void speakStart(int speakIndex) {
                        runOnUiThread(() -> csvBook.speakStart(speakIndex));
                    }

                });
            }
        };
    }

    /**
     * 正文事件
     */
    private void initLoadDataListener() {
        loadDataListener = new ContentSwitchView.LoadDataListener() {
            @Override
            public void loadData(BookContentView bookContentView, long qtag, int chapterIndex, int pageIndex) {
                mPresenter.loadContent(bookContentView, qtag, chapterIndex, pageIndex);
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void updateProgress(int chapterIndex, int pageIndex) {
                mPresenter.updateProgress(chapterIndex, pageIndex);
                actionBar.setTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
                if (mPresenter.getBookShelf().getChapterListSize() > 0) {
                    atvUrl.setText(mPresenter.getBookShelf().getChapterList(chapterIndex).getDurChapterUrl());
                } else {
                    atvUrl.setText("");
                }

                if (mPresenter.getBookShelf().getChapterListSize() == 1) {
                    tvPre.setEnabled(false);
                    tvNext.setEnabled(false);
                } else {
                    if (chapterIndex == 1) {
                        tvPre.setEnabled(false);
                        tvNext.setEnabled(true);
                    } else if (chapterIndex == mPresenter.getBookShelf().getChapterListSize() - 1) {
                        tvPre.setEnabled(true);
                        tvNext.setEnabled(false);
                    } else {
                        tvPre.setEnabled(true);
                        tvNext.setEnabled(true);
                    }
                }
            }

            @Override
            public String getChapterTitle(int chapterIndex) {
                return mPresenter.getChapterTitle(chapterIndex);
            }

            @Override
            public void initData(int lineCount) {
                mPresenter.setPageLineCount(lineCount);
                mPresenter.setPageWidth(csvBook.getContentWidth());
                mPresenter.initContent();
            }

            @Override
            public void showMenu() {
                popMenuIn();
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void setHpbReadProgress(int pageIndex, int pageAll) {
                hpbReadProgress.setMaxProgress(pageAll - 1);
                if (hpbReadProgress.getDurProgress() != pageIndex)
                    hpbReadProgress.setDurProgress(pageIndex);
            }

            @Override
            public void readAloud(String content) {
                readAloudIntent.putExtra("aloudButton", aloudButton);
                readAloudIntent.putExtra("content", content);
                startService(readAloudIntent);
                aloudButton = false;
            }

            @Override
            public void openChapterList() {
                if (chapterListView.hasData()) {
                    new Handler().postDelayed(() -> chapterListView.show(mPresenter.getBookShelf().getDurChapter()), menuTopOut.getDuration());
                }
            }

            @Override
            public void curPageFinish() {
                if (fromMediaButton) {
                    fromMediaButton = false;
                    onMediaButton();
                }
            }

        };
    }

    /**
     * 检查是否加入书架
     */
    public boolean checkAddShelf() {
        if (isAdd) {
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
            } else {
                Boolean temp = csvBook.onKeyDown(keyCode, event);
                if (temp) {
                    return true;
                }
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Boolean temp = csvBook.onKeyUp(keyCode, event);
        return temp || super.onKeyUp(keyCode, event);
    }

    @Override
    public void loadLocationBookError(String errMsg) {
        csvBook.loadError(errMsg);
    }

    @Override
    public void showOnLineView() {
        if (mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            atvUrl.setVisibility(View.VISIBLE);
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setVisible(true);
                    menu.getItem(i).setEnabled(true);
                }
            }
        } else if (mPresenter.getBookShelf() != null && mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            atvUrl.setVisibility(View.GONE);
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setVisible(false);
                    menu.getItem(i).setEnabled(false);
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
    public ContentSwitchView getCsvBook() {
        return csvBook;
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
                    aloudButton = true;
                    csvBook.readAloudStart();
                    ReadBookActivity.this.bindService(readAloudIntent, conn, Context.BIND_AUTO_CREATE);
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
                    csvBook.changeBg();
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
        if (hideStatusBar) {
            csvBook.upTime(dfTime.format(Calendar.getInstance().getTime()));
            csvBook.upBattery(BatteryUtil.getLevel(this));
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
            if (hideStatusBar) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    csvBook.upTime(dfTime.format(Calendar.getInstance().getTime()));
                } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    csvBook.upBattery(level);
                }
            }
        }
    }


}