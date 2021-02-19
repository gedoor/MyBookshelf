//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.BuildConfig;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseTabActivity;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.databinding.ActivityMainBinding;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.help.ProcessTextHelp;
import com.kunfei.bookshelf.help.permission.Permissions;
import com.kunfei.bookshelf.help.permission.PermissionsCompat;
import com.kunfei.bookshelf.help.storage.BackupRestoreUi;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.presenter.BookSourcePresenter;
import com.kunfei.bookshelf.presenter.MainPresenter;
import com.kunfei.bookshelf.presenter.contract.MainContract;
import com.kunfei.bookshelf.service.WebService;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.NavigationViewUtil;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.fragment.BookListFragment;
import com.kunfei.bookshelf.view.fragment.FindBookFragment;
import com.kunfei.bookshelf.widget.modialog.InputDialog;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;

import static com.kunfei.bookshelf.utils.NetworkUtils.isNetWorkAvailable;

public class MainActivity extends BaseTabActivity<MainContract.Presenter> implements MainContract.View,
        BookListFragment.CallbackValue {
    private final int requestSource = 14;
    private String[] mTitles;
    private final int REQUEST_QR = 202;

    private ActivityMainBinding binding;
    private AppCompatImageView vwNightTheme;
    private int group;
    private ActionBarDrawerToggle mDrawerToggle;
    private MoDialogHUD moDialogHUD;
    private long exitTime = 0;
    private boolean resumed = false;
    private Handler handler = new Handler();

    @Override
    protected MainContract.Presenter initInjector() {
        return new MainPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            resumed = savedInstanceState.getBoolean("resumed");
        }
        group = preferences.getInt("bookshelfGroup", 0);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("resumed", resumed);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    public void onResume() {
        super.onResume();

        String shared_url = preferences.getString("shared_url", "");
        if (shared_url.length() > 1) {
            InputDialog.builder(this)
                    .setTitle(getString(R.string.add_book_url))
                    .setDefaultValue(shared_url)
                    .setCallback(new InputDialog.Callback() {
                        @Override
                        public void setInputText(String inputText) {
                            inputText = StringUtils.trim(inputText);
                            mPresenter.addBookUrl(inputText);
                        }

                        @Override
                        public void delete(String value) {

                        }
                    }).show();
            preferences.edit()
                    .putString("shared_url", "")
                    .apply();
        }
    }


    /**
     * 沉浸状态栏
     */
    @Override
    public void initImmersionBar() {
        super.initImmersionBar();
    }

    @Override
    protected void initData() {
        mTitles = new String[]{getString(R.string.bookshelf), getString(R.string.find)};
    }

    @Override
    public boolean isRecreate() {
        return isRecreate;
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected List<Fragment> createTabFragments() {
        BookListFragment bookListFragment = null;
        FindBookFragment findBookFragment = null;
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BookListFragment) {
                bookListFragment = (BookListFragment) fragment;
            } else if (fragment instanceof FindBookFragment) {
                findBookFragment = (FindBookFragment) fragment;
            }
        }
        if (bookListFragment == null)
            bookListFragment = new BookListFragment();
        if (findBookFragment == null)
            findBookFragment = new FindBookFragment();
        return Arrays.asList(bookListFragment, findBookFragment);
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList(mTitles);
    }

    @Override
    protected void bindView() {
        super.bindView();
        setSupportActionBar(binding.mainView.toolbar);
        setupActionBar();
        binding.mainView.cardSearch.setCardBackgroundColor(ThemeStore.primaryColorDark(this));
        initDrawer();
        initTabLayout();
        upGroup(group);
        moDialogHUD = new MoDialogHUD(this);
        if (!preferences.getBoolean("behaviorMain", true)) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) binding.mainView.toolbar.getLayoutParams();
            params.setScrollFlags(0);
        }
        //点击跳转搜索页
        binding.mainView.cardSearch.setOnClickListener(view -> startActivityByAnim(new Intent(this, SearchBookActivity.class),
                binding.mainView.toolbar, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out));
    }

    //初始化TabLayout和ViewPager
    private void initTabLayout() {
        mTlIndicator.setBackgroundColor(ThemeStore.backgroundColor(this));
        mTlIndicator.setSelectedTabIndicatorColor(ThemeStore.accentColor(this));
        //TabLayout使用自定义Item
        for (int i = 0; i < mTlIndicator.getTabCount(); i++) {
            TabLayout.Tab tab = mTlIndicator.getTabAt(i);
            if (tab == null) return;
            tab.setCustomView(tab_icon(mTitles[i]));
            View customView = tab.getCustomView();
            if (customView == null) return;
            TextView tv = customView.findViewById(R.id.tabtext);
            tab.setContentDescription(String.format("%s,%s", tv.getText(), getString(R.string.click_on_selected_show_menu)));
            ImageView im = customView.findViewById(R.id.tabicon);
            if (tab.isSelected()) {
                im.setVisibility(View.VISIBLE);
            } else {
                im.setVisibility(View.GONE);
            }
        }
        mTlIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView == null) return;
                ImageView im = customView.findViewById(R.id.tabicon);
                im.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView == null) return;
                ImageView im = customView.findViewById(R.id.tabicon);
                im.setVisibility(View.GONE);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                View tabView = (View) Objects.requireNonNull(tab.getCustomView()).getParent();
                if (tab.getPosition() == 0) {
                    showBookGroupMenu(tabView);
                } else {
                    showFindMenu(tabView);
                }
            }
        });
    }

    /**
     * 显示分组菜单
     */
    private void showBookGroupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        for (int j = 0; j < getResources().getStringArray(R.array.book_group_array).length; j++) {
            popupMenu.getMenu().add(0, 0, j, getResources().getStringArray(R.array.book_group_array)[j]);
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            upGroup(menuItem.getOrder());
            return true;
        });
        popupMenu.setOnDismissListener(popupMenu1 -> updateTabItemIcon(0, false));
        popupMenu.show();
        updateTabItemIcon(0, true);
    }

    /**
     * 显示发现菜单
     */
    private void showFindMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(0, 0, 0, getString(R.string.switch_display_style));
        popupMenu.getMenu().add(0, 0, 1, getString(R.string.clear_find_cache));
        boolean findTypeIsFlexBox = preferences.getBoolean("findTypeIsFlexBox", true);
        boolean showFindLeftView = preferences.getBoolean("showFindLeftView", true);
        if (findTypeIsFlexBox) {
            popupMenu.getMenu().add(0, 0, 2, showFindLeftView ? "隐藏左侧栏" : "显示左侧栏");
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            FindBookFragment findBookFragment = getFindFragment();
            switch (menuItem.getOrder()) {
                case 0:
                    preferences.edit()
                            .putBoolean("findTypeIsFlexBox", !findTypeIsFlexBox)
                            .apply();
                    if (findBookFragment != null) {
                        findBookFragment.upStyle();
                    }
                    break;
                case 1:
                    ACache.get(this, "findCache").clear();
                    if (findBookFragment != null) {
                        findBookFragment.refreshData();
                    }
                    break;
                case 2:
                    preferences.edit()
                            .putBoolean("showFindLeftView", !showFindLeftView)
                            .apply();
                    if (findBookFragment != null) {
                        findBookFragment.upUI();
                    }
                    break;
            }
            return true;
        });
        popupMenu.setOnDismissListener(popupMenu1 -> updateTabItemIcon(1, false));
        popupMenu.show();
        updateTabItemIcon(1, true);
    }

    /**
     * 更新Tab图标
     */
    private void updateTabItemIcon(int index, boolean showMenu) {
        TabLayout.Tab tab = mTlIndicator.getTabAt(index);
        if (tab == null) return;
        View customView = tab.getCustomView();
        if (customView == null) return;
        ImageView im = customView.findViewById(R.id.tabicon);
        if (showMenu) {
            im.setImageResource(R.drawable.ic_arrow_drop_up);
        } else {
            im.setImageResource(R.drawable.ic_arrow_drop_down);
        }
    }

    /**
     * 更新Tab文字
     */
    private void updateTabItemText(int group) {
        TabLayout.Tab tab = mTlIndicator.getTabAt(0);
        if (tab == null) return;
        View customView = tab.getCustomView();
        if (customView == null) return;
        TextView tv = customView.findViewById(R.id.tabtext);
        tv.setText(getResources().getStringArray(R.array.book_group_array)[group]);
        tab.setContentDescription(String.format("%s,%s", tv.getText(), getString(R.string.click_on_selected_show_menu)));
    }

    private View tab_icon(String name) {
        @SuppressLint("InflateParams")
        View tabView = LayoutInflater.from(this).inflate(R.layout.tab_view_icon_right, null);
        TextView tv = tabView.findViewById(R.id.tabtext);
        tv.setText(name);
        ImageView im = tabView.findViewById(R.id.tabicon);
        im.setVisibility(View.VISIBLE);
        im.setImageResource(R.drawable.ic_arrow_drop_down);
        return tabView;
    }

    public ViewPager getViewPager() {
        return mVp;
    }

    public BookListFragment getBookListFragment() {
        try {
            return (BookListFragment) mFragmentList.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public FindBookFragment getFindFragment() {
        try {
            return (FindBookFragment) mFragmentList.get(1);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 这个必须要，没有的话进去的默认是个箭头。。正常应该是三横杠的
        mDrawerToggle.syncState();
        if (vwNightTheme != null) {
            upThemeVw();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_local:
                new PermissionsCompat.Builder(this)
                        .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                        .rationale(R.string.import_per)
                        .onGranted((requestCode) -> {
                            startActivity(new Intent(MainActivity.this, ImportBookActivity.class));
                            return Unit.INSTANCE;
                        })
                        .request();
                break;
            case R.id.action_add_url:
                InputDialog.builder(this)
                        .setTitle(getString(R.string.add_book_url))
                        .setCallback(new InputDialog.Callback() {
                            @Override
                            public void setInputText(String inputText) {
                                inputText = StringUtils.trim(inputText);
                                mPresenter.addBookUrl(inputText);
                            }

                            @Override
                            public void delete(String value) {

                            }
                        }).show();
                break;
            case R.id.action_add_qrcode:

                Intent intent = new Intent(this, QRCodeScanActivity.class);
                startActivityForResult(intent, REQUEST_QR);
                break;
            case R.id.action_download_all:
                if (!isNetWorkAvailable()) {
                    toast(R.string.network_connection_unavailable);
                } else {
                    RxBus.get().post(RxBusTag.DOWNLOAD_ALL, 10000);
                }
                break;
            case R.id.menu_bookshelf_layout:
                selectBookshelfLayout();
                break;
            case R.id.action_arrange_bookshelf:
                if (getBookListFragment() != null) {
                    getBookListFragment().setArrange(true);
                }
                break;
            case R.id.action_web_start:
                boolean startedThisTime = WebService.startThis(this);
                if (!startedThisTime) {
                    toast(getString(R.string.web_service_already_started_hint));
                }
                break;
            case android.R.id.home:
                if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                    binding.drawer.closeDrawers();
                } else {
                    binding.drawer.openDrawer(GravityCompat.START, !MApplication.isEInkMode);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //初始化侧边栏
    private void initDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, binding.drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        binding.drawer.addDrawerListener(mDrawerToggle);

        setUpNavigationView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);

    }

    private void upGroup(int group) {
        if (this.group != group) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("bookshelfGroup", group);
            editor.apply();
        }
        this.group = group;
        RxBus.get().post(RxBusTag.UPDATE_GROUP, group);
        RxBus.get().post(RxBusTag.REFRESH_BOOK_LIST, false);
        //更换Tab文字
        updateTabItemText(group);

    }

    /**
     * 侧边栏按钮
     */
    private void setUpNavigationView() {
        binding.navigationView.setBackgroundColor(ThemeStore.backgroundColor(this));
        NavigationViewUtil.setItemIconColors(binding.navigationView, getResources().getColor(R.color.tv_text_default), ThemeStore.accentColor(this));
        NavigationViewUtil.disableScrollbar(binding.navigationView);
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(this).inflate(R.layout.navigation_header, null);
        AppCompatImageView imageView = headerView.findViewById(R.id.iv_read);
        imageView.setColorFilter(ThemeStore.accentColor(this));
        binding.navigationView.addHeaderView(headerView);
        Menu drawerMenu = binding.navigationView.getMenu();
        vwNightTheme = drawerMenu.findItem(R.id.action_theme).getActionView().findViewById(R.id.iv_theme_day_night);
        upThemeVw();
        vwNightTheme.setOnClickListener(view -> setNightTheme(!isNightTheme()));
        binding.navigationView.setNavigationItemSelectedListener(menuItem -> {
            binding.drawer.closeDrawer(GravityCompat.START, !MApplication.isEInkMode);
            switch (menuItem.getItemId()) {
                case R.id.action_book_source_manage:
                    handler.postDelayed(() -> BookSourceActivity.startThis(this, requestSource), 200);
                    break;
                case R.id.action_replace_rule:
                    handler.postDelayed(() -> ReplaceRuleActivity.startThis(this, null), 200);
                    break;
                case R.id.action_download:
                    handler.postDelayed(() -> DownloadActivity.startThis(this), 200);
                    break;
                case R.id.action_setting:
                    handler.postDelayed(() -> SettingActivity.startThis(this), 200);
                    break;
                case R.id.action_about:
                    handler.postDelayed(() -> AboutActivity.startThis(this), 200);
                    break;
                case R.id.action_donate:
                    handler.postDelayed(() -> DonateActivity.startThis(this), 200);
                    break;
                case R.id.action_backup:
                    handler.postDelayed(() -> BackupRestoreUi.INSTANCE.backup(this), 200);
                    break;
                case R.id.action_restore:
                    handler.postDelayed(() -> BackupRestoreUi.INSTANCE.restore(this), 200);
                    break;
                case R.id.action_theme:
                    handler.postDelayed(() -> ThemeSettingActivity.startThis(this), 200);
                    break;
            }
            return true;
        });
    }

    /**
     * 更新主题切换按钮
     */
    private void upThemeVw() {
        if (isNightTheme()) {
            vwNightTheme.setImageResource(R.drawable.ic_daytime);
            vwNightTheme.setContentDescription(getString(R.string.click_to_day));
        } else {
            vwNightTheme.setImageResource(R.drawable.ic_brightness);
            vwNightTheme.setContentDescription(getString(R.string.click_to_night));
        }
        vwNightTheme.getDrawable().mutate().setColorFilter(ThemeStore.accentColor(this), PorterDuff.Mode.SRC_ATOP);
    }

    private void selectBookshelfLayout() {
        new AlertDialog.Builder(this)
                .setTitle("选择书架布局")
                .setItems(R.array.bookshelf_layout, (dialog, which) -> {
                    preferences.edit().putInt("bookshelfLayout", which).apply();
                    recreate();
                }).show();
    }

    /**
     * 新版本运行
     */
    private void versionUpRun() {
        if (preferences.getInt("versionCode", 0) != MApplication.getVersionCode()) {
            //保存版本号
            preferences.edit()
                    .putInt("versionCode", MApplication.getVersionCode())
                    .apply();
            //更新日志
            moDialogHUD.showAssetMarkdown("updateLog.md");
        }
    }

    @Override
    protected void firstRequest() {
        if (!isRecreate) {
            versionUpRun();
        }
        if (!Objects.equals(MApplication.downloadPath, FileHelp.getFilesPath())) {
            new PermissionsCompat.Builder(this)
                    .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                    .rationale(R.string.get_storage_per)
                    .request();
        }
        handler.postDelayed(() -> {
            UpLastChapterModel.getInstance().startUpdate();
            if (BuildConfig.DEBUG) {
                ProcessTextHelp.setProcessTextEnable(false);
            }
        }, 60 * 1000);
    }

    @Override
    public void dismissHUD() {
        moDialogHUD.dismiss();
    }

    public void onRestore(String msg) {
        moDialogHUD.showLoading(msg);
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else if (mTlIndicator.getSelectedTabPosition() != 0) {
            Objects.requireNonNull(mTlIndicator.getTabAt(0)).select();
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                    binding.drawer.closeDrawer(GravityCompat.START, !MApplication.isEInkMode);
                    return true;
                }
                exit();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 退出
     */
    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showSnackBar(binding.mainView.toolbar, getString(R.string.double_click_exit));
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    @Override
    protected void onDestroy() {
        UpLastChapterModel.destroy();
        DbHelper.getDaoSession().getBookContentBeanDao().deleteAll();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BackupRestoreUi.INSTANCE.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case requestSource:
                if (resultCode == RESULT_OK) {
                    FindBookFragment findBookFragment = getFindFragment();
                    if (findBookFragment != null) {
                        findBookFragment.refreshData();
                    }
                }
                break;
            case REQUEST_QR:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("result");
                    if (!StringUtils.isTrimEmpty(result)) {
                        result=result.trim();
                        // 如果只有书源,则导入书源
                        if(result.replaceAll("(\\s|\n)*","").matches("^\\{.*$")) {
                            new BookSourcePresenter().importBookSource(result);
                            break;
                        }
//                        String[] string=result.split("#",2);
//                        mPresenter.addBookUrl(string[0]);
                        mPresenter.addBookUrl(result);
                    }
                }
                break;
        }
    }

}
