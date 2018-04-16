//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IReadBookPresenter;
import com.monke.monkeybook.service.ReadAloudService;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.PremissionCheck;
import com.monke.monkeybook.view.impl.IReadBookView;
import com.monke.monkeybook.view.popupwindow.CheckAddShelfPop;
import com.monke.monkeybook.view.popupwindow.MoreSettingPop;
import com.monke.monkeybook.view.popupwindow.ReadAdjustPop;
import com.monke.monkeybook.view.popupwindow.ReadBookMenuMorePop;
import com.monke.monkeybook.view.popupwindow.ReadInterfacePop;
import com.monke.monkeybook.widget.ChapterListView;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;
import com.monke.monkeybook.widget.contentswitchview.ContentSwitchView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.util.Objects;

import at.markushi.ui.CircleButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.grantland.widget.AutofitTextView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.monke.monkeybook.presenter.ReadBookPresenterImpl.OPEN_FROM_OTHER;
import static com.monke.monkeybook.service.ReadAloudService.ActionNewReadAloud;
import static com.monke.monkeybook.service.ReadAloudService.PAUSE;
import static com.monke.monkeybook.service.ReadAloudService.PLAY;

public class ReadBookActivity extends MBaseActivity<IReadBookPresenter> implements IReadBookView {
    private final int ResultReplace = 101;
    private final int RESULT_OPEN_OTHER_PERMS = 102;
    public final int ResultSelectBg = 103;
    public final int ResultSelectFont = 104;

    @BindView(R.id.fl_content)
    FrameLayout flContent;
    @BindView(R.id.csv_book)
    ContentSwitchView csvBook;
    @BindView(R.id.fl_menu)
    FrameLayout flMenu;
    @BindView(R.id.v_menu_bg)
    View vMenuBg;
    @BindView(R.id.ll_menu_top)
    LinearLayout llMenuTop;
    @BindView(R.id.ll_menu_bottom)
    LinearLayout llMenuBottom;
    @BindView(R.id.iv_return)
    ImageButton ivReturn;
    @BindView(R.id.iv_refresh)
    ImageButton ivRefresh;
    @BindView(R.id.iv_more)
    ImageButton ivMenuMore;
    @BindView(R.id.atv_title)
    AutofitTextView atvTitle;
    @BindView(R.id.atv_url)
    AutofitTextView atvUrl;
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
    //主菜单动画
    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;

    private boolean aloudButton;
    private boolean hideStatusBar;
    private boolean isBind;
    private String noteUrl;
    private int aloudStatus;

    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private CheckAddShelfPop checkAddShelfPop;
    private ReadAdjustPop readAdjustPop;
    private ReadBookMenuMorePop readBookMenuMorePop;
    private ReadInterfacePop readInterfacePop;
    private MoreSettingPop moreSettingPop;
    private MoProgressHUD moProgressHUD;
    private Intent readAloudIntent;
    private ServiceConnection conn;
    private ContentSwitchView.LoadDataListener loadDataListener;

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
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setOrientation();
        setContentView(R.layout.activity_book_read);
        hideStatusBar = preferences.getBoolean("hide_status_bar", false);
        readAloudIntent = new Intent(this, ReadAloudService.class);
        readAloudIntent.setAction(ActionNewReadAloud);
        ReadBookControl readBookControl = ReadBookControl.getInstance();
        if (readBookControl.getKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        hideStatusBar(hideStatusBar);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPresenter.getBookShelf() != null) {
            outState.putString("noteUrl", mPresenter.getBookShelf().getNoteUrl());
            outState.putInt("aloudStatus", aloudStatus);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

    /**
     * 隐藏状态栏
     */
    private void hideStatusBar(Boolean hide) {
        if (hide) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 隐藏虚拟按键
     */
    private void hideNavigationBar() {
        if (preferences.getBoolean("hide_navigation_bar", false)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
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
        hideStatusBar(false);
        hideNavigationBar();
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
                        if (isBind) {
                            unbindService(conn);
                            isBind = false;
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
                                ibReadAloud.setImageResource(R.drawable.ic_pause_black_24dp);
                                llReadAloudTimer.setVisibility(View.VISIBLE);
                                break;
                            case PAUSE:
                                ibReadAloud.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                                llReadAloudTimer.setVisibility(View.VISIBLE);
                                break;
                            default:
                                ibReadAloud.setImageResource(R.drawable.ic_volume_up_black_24dp);
                                llReadAloudTimer.setVisibility(View.INVISIBLE);
                        }
                        ibReadAloud.getDrawable().mutate();
                        ibReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
                    }

                    @Override
                    public void upTimer(String text) {
                        tvReadAloudTimer.setText(text);
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

            @Override
            public void updateProgress(int chapterIndex, int pageIndex) {
                mPresenter.updateProgress(chapterIndex, pageIndex);
                if (mPresenter.getBookShelf().getChapterListSize() > 0) {
                    atvTitle.setText(mPresenter.getBookShelf().getChapterList(chapterIndex).getDurChapterName());
                    atvUrl.setText(mPresenter.getBookShelf().getChapterList(chapterIndex).getDurChapterUrl());
                } else {
                    atvTitle.setText("无章节");
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

        };
    }

    /**
     * 开始朗读
     */
    private void startReadAloud() {
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
                    isBind = ReadBookActivity.this.bindService(readAloudIntent, conn, Context.BIND_AUTO_CREATE);
                }
        }
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
                hideStatusBar(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vMenuBg.setOnClickListener(v -> popMenuOut());
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
                hideStatusBar(hideStatusBar);
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
        initCsvBook();
        ivCList.getDrawable().mutate();
        ivCList.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivAdjust.getDrawable().mutate();
        ivAdjust.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivInterface.getDrawable().mutate();
        ivInterface.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivSetting.getDrawable().mutate();
        ivSetting.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        if (preferences.getBoolean("nightTheme", false)) {
            ibNightTheme.setImageResource(R.drawable.ic_brightness_high_black_24dp);
        } else {
            ibNightTheme.setImageResource(R.drawable.ic_brightness_2_black_24dp);
        }
        //弹窗
        moProgressHUD = new MoProgressHUD(this);
        //界面设置
        readInterfacePop = new ReadInterfacePop(this, new ReadInterfacePop.OnChangeProListener() {
            @Override
            public void textSizeChange(int index) {
                csvBook.changeTextSize();
            }

            @Override
            public void lineSizeChange(float lineMultiplier) {
                csvBook.changeLineSize();
            }

            @Override
            public void bgChange(int index) {
                csvBook.changeBg();
            }

            @Override
            public void setFont(String path) {
                csvBook.setFont();
                csvBook.changeTextSize();
            }

            @Override
            public void setConvert() {
                BookshelfHelp.clearLineContent();
                recreate();
            }

            @Override
            public void setBold() {
                csvBook.setTextBold();
            }
        });
        //目录
        chapterListView.setOnChangeListener(new ChapterListView.OnChangeListener() {
            @Override
            public void animIn() {
                hideStatusBar(false);
            }

            @Override
            public void animOut() {
                hideStatusBar(hideStatusBar);
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
            public void showTitle(Boolean showTitle) {
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
        readAdjustPop.initLight();
        readBookMenuMorePop = new ReadBookMenuMorePop(this);
    }

    @Override
    public void setHpbReadProgressMax(int count) {
        hpbReadProgress.setMaxProgress(count);
    }

    private void initCsvBook() {
        csvBook.bookReadInit(() -> mPresenter.initData(ReadBookActivity.this));
    }

    @Override
    public void initPop() {
        //是否添加书架
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
        initChapterList();
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

        //菜单
        ivMenuMore.getDrawable().mutate();
        ivMenuMore.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivMenuMore.setOnClickListener(view -> readBookMenuMorePop
                .showAsDropDown(ivMenuMore, 0, DensityUtil.dp2px(ReadBookActivity.this, -3.5f)));

        //正文
        csvBook.setLoadDataListener(loadDataListener);

        //返回按钮
        ivReturn.getDrawable().mutate();
        ivReturn.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivReturn.setOnClickListener(view -> finish());

        //离线下载
        readBookMenuMorePop.setOnClickDownload(view -> {
            readBookMenuMorePop.dismiss();
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
        });

        //换源
        readBookMenuMorePop.setOnClickChangeSource(view -> {
            readBookMenuMorePop.dismiss();
            ReadBookActivity.this.popMenuOut();
            if (mPresenter.getBookShelf() != null) {
                moProgressHUD.showChangeSource(mPresenter.getBookShelf(), searchBookBean -> {
                    if (!Objects.equals(searchBookBean.getNoteUrl(), mPresenter.getBookShelf().getNoteUrl())) {
                        mPresenter.changeBookSource(searchBookBean);
                        csvBook.showLoading();
                    }
                });
            }
        });

        //刷新按钮
        ivRefresh.getDrawable().mutate();
        ivRefresh.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivRefresh.setOnClickListener(view -> {
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
        ibReadAloudTimer.getDrawable().mutate();
        ibReadAloudTimer.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ibReadAloudTimer.setOnClickListener(view -> ReadAloudService.setTimer(this));

        //朗读
        ibReadAloud.getDrawable().mutate();
        ibReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ibReadAloud.setOnClickListener(view -> {
            startReadAloud();
        });

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
        ibNightTheme.setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("nightTheme", !preferences.getBoolean("nightTheme", false));
            editor.apply();
            setNightTheme();
        });

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
        mPresenter.saveProgress();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (flMenu.getVisibility() == View.VISIBLE) {
                    popMenuOut();
                    return true;
                } else if (chapterListView.dismissChapterList()) {
                    return true;
                } else if (ReadAloudService.running && aloudStatus == PLAY) {
                    ReadAloudService.pause(this);
                    Toast.makeText(this, R.string.read_aloud_pause, Toast.LENGTH_SHORT).show();
                    return true;
                } else if (!mPresenter.getAdd() && checkAddShelfPop != null && !checkAddShelfPop.isShowing()) {
                    checkAddShelfPop.showAtLocation(flContent, Gravity.CENTER, 0, 0);
                    return true;
                } else {
                    finish();
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                popMenuIn();
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
    public void loadLocationBookError() {
        csvBook.loadError();
    }

    @Override
    public void showOnLineView() {
        ivMenuMore.setVisibility(View.VISIBLE);
        ivRefresh.setVisibility(View.VISIBLE);
        atvUrl.setVisibility(View.VISIBLE);
    }

    @Override
    public String getNoteUrl() {
        return noteUrl;
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

    @Override
    public void chapterChange(ChapterListBean chapterListBean) {
        chapterListView.upChapterList(chapterListBean);
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
            case ResultSelectBg:
                if (resultCode == RESULT_OK && null != data) {
                    readInterfacePop.setCustomBg(data.getData());
                }
                break;
            case ResultSelectFont:
                if (resultCode == RESULT_OK && null != data) {
                    String path = FileUtil.getPath(this, data.getData());
                    try {
                        //判断是否字体文件或字体是否损坏
                        Typeface typeface = Typeface.createFromFile(path);
                        readInterfacePop.setReadFonts(path);
                    } catch (Exception e) {
                        Toast.makeText(this, "不是字体文件", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showCheckPermission && mPresenter.getOpen_from() == OPEN_FROM_OTHER && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PremissionCheck.checkPremission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            showCheckPermission = true;
            mPresenter.openBookFromOther(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReadAloudService.stop(this);
    }

    @Override
    public void finish() {
        if (!AppActivityManager.getInstance().isExist(MainActivity.class)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        super.finish();
    }

}