//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.BuildConfig;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.BaseTabActivity;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.DataBackup;
import com.monke.monkeybook.help.LauncherIcon;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.UpdateManager;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.MainPresenterImpl;
import com.monke.monkeybook.presenter.contract.MainContract;
import com.monke.monkeybook.view.fragment.BookListFragment;
import com.monke.monkeybook.view.fragment.FindBookFragment;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.monke.monkeybook.help.Constant.BOOK_GROUPS;
import static com.monke.monkeybook.utils.NetworkUtil.isNetWorkAvailable;

public class MainActivity extends BaseTabActivity<MainContract.Presenter> implements MainContract.View, BookListFragment.CallBackValue {
    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private static final int FILE_SELECT_RESULT = 13;
    private static String[] mTitles = new String[]{"书架", "发现"};

    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_view)
    CoordinatorLayout mainView;
    @BindView(R.id.card_search)
    CardView cardSearch;

    private Switch swNightTheme;
    private int group;
    private boolean viewIsList;
    private ActionBarDrawerToggle mDrawerToggle;
    private MoProgressHUD moProgressHUD;
    private long exitTime = 0;
    private boolean resumed = false;

    @Override
    protected MainContract.Presenter initInjector() {
        return new MainPresenterImpl();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("resumed", resumed);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
        viewIsList = preferences.getBoolean("bookshelfIsList", true);
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

    /**************abstract***********/
    @Override
    protected List<Fragment> createTabFragments() {
        BookListFragment bookListFragment = new BookListFragment();
        FindBookFragment findBookFragment = new FindBookFragment();
        return Arrays.asList(bookListFragment, findBookFragment);
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList(mTitles);
    }

    @Override
    protected void bindView() {
        super.bindView();
        setSupportActionBar(toolbar);
        setupActionBar();
        initDrawer();
        initTabLayout();
        upGroup(group);
        moProgressHUD = new MoProgressHUD(this);

        //点击跳转搜索页
        cardSearch.setOnClickListener(view -> startActivityByAnim(new Intent(this, SearchBookActivity.class),
                toolbar, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out));
    }

    //初始化TabLayout和ViewPager
    private void initTabLayout(){

        //TabLayout使用自定义Item
        for (int i = 0; i < mTlIndicator.getTabCount(); i++) {
            TabLayout.Tab tab = mTlIndicator.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(tab_icon(mTitles[i], null));
                if (tab.getCustomView() != null) {
                    View tabView = (View) tab.getCustomView().getParent();
                    tabView.setTag(i);
                    //设置第一个Item的点击事件(当下标为0时触发)
                    if (i==0){
                        tabView.setOnClickListener(view -> {
                            if (tabView.isSelected()){
                                //切换书架
                                PopupMenu popupMenu = new PopupMenu(this, view);
                                for (int j = 0; j < BOOK_GROUPS.length; j++) {
                                    popupMenu.getMenu().add(0, 0, j, BOOK_GROUPS[j]);
                                }
                                popupMenu.setOnMenuItemClickListener(menuItem -> {
                                    upGroup(menuItem.getOrder());
                                    return true;
                                });
                                popupMenu.show();
                            }
                        });
                    }
                }
            }
        }
    }

    private void updateTabItemText(int group){
        TabLayout.Tab tab = mTlIndicator.getTabAt(0);
        //首先移除原先View
        assert tab != null;
        final ViewParent customParent= tab.getCustomView().getParent();
        if (customParent != null) {
            ((ViewGroup) customParent).removeView(tab.getCustomView());
        }

        tab.setCustomView(tab_icon(BOOK_GROUPS[group], R.drawable.ic_arrow_drop_down_black_24dp));
        View tabView = (View) tab.getCustomView().getParent();
        tabView.setTag(0);
    }

    private View tab_icon(String name,Integer iconID){
        View tabView =  LayoutInflater.from(this).inflate(R.layout.tab_view_icon_right,null);
        TextView tv = tabView.findViewById(R.id.tabtext);
        tv.setText(name);
        ImageView im = tabView.findViewById(R.id.tabicon);
        if (iconID != null) {
            im.setVisibility(View.VISIBLE);
            im.setImageResource(iconID);
        } else {
            im.setVisibility(View.GONE);
        }
        return tabView;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 这个必须要，没有的话进去的默认是个箭头。。正常应该是三横杠的
        mDrawerToggle.syncState();
        if (swNightTheme != null) {
            swNightTheme.setChecked(isNightTheme());
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem pauseMenu = menu.findItem(R.id.action_list_grid);
        if (viewIsList) {
            pauseMenu.setTitle(R.string.action_grid);
        } else {
            pauseMenu.setTitle(R.string.action_list);
        }
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
        SharedPreferences.Editor editor = preferences.edit();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_local:
                if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
                    startActivity(new Intent(this, ImportBookActivity.class));
                } else {
                    EasyPermissions.requestPermissions(this, getString(R.string.import_book_source),
                            FILE_SELECT_RESULT, MApplication.PerList);
                }
                break;
            case R.id.action_add_url:
                moProgressHUD.showInputBox("添加书籍网址", null, inputText -> mPresenter.addBookUrl(inputText));
                break;
            case R.id.action_download_all:
                if (!isNetWorkAvailable())
                    toast("网络连接不可用，无法下载！");
                else
                    RxBus.get().post(RxBusTag.DOWNLOAD_ALL, 1000);
                break;
            case R.id.action_list_grid:
                editor.putBoolean("bookshelfIsList", !viewIsList);
                editor.apply();
                recreate();
                break;
            case R.id.action_clear_cache:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_content)
                        .setMessage("是否同时删除已下载的书籍目录？")
                        .setPositiveButton("是", (dialog, which) -> BookshelfHelp.clearCaches(true))
                        .setNegativeButton("否", (dialogInterface, i) -> BookshelfHelp.clearCaches(false))
                        .show();
                break;
            case R.id.action_clearBookshelf:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_bookshelf)
                        .setMessage(R.string.clear_bookshelf_s)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.clearBookshelf())
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> { })
                        .show();
                break;
            case R.id.action_change_icon:
                LauncherIcon.Change();
                break;
            case android.R.id.home:
                if (drawer.isDrawerOpen(GravityCompat.START)
                        ) {
                    drawer.closeDrawers();
                } else {
                    drawer.openDrawer(GravityCompat.START);
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
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        drawer.addDrawerListener(mDrawerToggle);

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

    //侧边栏按钮
    private void setUpNavigationView() {
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(this).inflate(R.layout.navigation_header, null);
        navigationView.addHeaderView(headerView);
        ColorStateList colorStateList = getResources().getColorStateList(R.color.navigation_color);
        navigationView.setItemTextColor(colorStateList);
        navigationView.setItemIconTintList(colorStateList);
        Menu drawerMenu = navigationView.getMenu();
        swNightTheme = drawerMenu.findItem(R.id.action_night_theme).getActionView().findViewById(R.id.sw_night_theme);
        swNightTheme.setChecked(isNightTheme());
        swNightTheme.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                setNightTheme(b);
            }
        });
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawer.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.action_book_source_manage:
                    new Handler().postDelayed(() -> BookSourceActivity.startThis(this), 200);
                    break;
                case R.id.action_replace_rule:
                    new Handler().postDelayed(() -> ReplaceRuleActivity.startThis(this), 200);
                    break;
                case R.id.action_download:
                    new Handler().postDelayed(() -> DownloadActivity.startThis(this), 200);
                    break;
                case R.id.action_setting:
                    new Handler().postDelayed(() -> SettingActivity.startThis(this), 200);
                    break;
                case R.id.action_about:
                    new Handler().postDelayed(() -> AboutActivity.startThis(this), 200);
                    break;
                case R.id.action_donate:
                    new Handler().postDelayed(() -> DonateActivity.startThis(this), 200);
                    break;
                case R.id.action_backup:
                    backup();
                    break;
                case R.id.action_restore:
                    restore();
                    break;
                case R.id.action_night_theme:
                    swNightTheme.setChecked(!isNightTheme());
                    setNightTheme(!isNightTheme());
                    break;
            }
            return true;
        });
    }

    //备份
    private void backup() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.backup_confirmation)
                    .setMessage(R.string.backup_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.backupData())
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> { })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.backup_permission),
                    BACKUP_RESULT, MApplication.PerList);
        }
    }

    @AfterPermissionGranted(BACKUP_RESULT)
    private void backupResult() {
        backup();
    }

    //恢复
    private void restore() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.restore_confirmation)
                    .setMessage(R.string.restore_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.restoreData())
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> { })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.restore_permission),
                    RESTORE_RESULT, MApplication.PerList);
        }
    }

    @AfterPermissionGranted(RESTORE_RESULT)
    private void restoreResult() {
        restore();
    }

    @AfterPermissionGranted(FILE_SELECT_RESULT)
    private void fileSelectResult() {
        startActivityByAnim(new Intent(MainActivity.this, ImportBookActivity.class), 0, 0);
    }

    private void versionUpRun() {
        if (preferences.getInt("versionCode", 0) != MApplication.getVersionCode()) {
            //保存版本号
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", MApplication.getVersionCode());
            editor.apply();
            //书源为空时加载默认书源
            BookSourceManage.initDefaultBookSource(this);
            //更新日志
            moProgressHUD.showAssetMarkdown("updateLog.md");
        }
    }

    private void requestPermission() {
        if (!EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            EasyPermissions.requestPermissions(this, "本软件需要存储权限来缓存书籍信息",
                    MApplication.RESULT__PERMS, MApplication.PerList);
        }
    }

    @Override
    protected void firstRequest() {
        if (!isRecreate) {
            versionUpRun();
            requestPermission();
            if (!BuildConfig.DEBUG && TextUtils.isEmpty(ACache.get(this).getAsString("checkUpdate"))) {
                UpdateManager.getInstance(this).checkUpdate(false);
            }
        }
    }

    @Override
    public void dismissHUD() {
        moProgressHUD.dismiss();
    }

    @Override
    public void refreshError(String error) {
        toast(error);
    }

    @Override
    public void showLoading(String msg) {
        moProgressHUD.showLoading(msg);
    }

    @Override
    public void onRestore(String msg) {
        moProgressHUD.showLoading(msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else if (mTlIndicator.getSelectedTabPosition() != 0){
            Objects.requireNonNull(mTlIndicator.getTabAt(0)).select();
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawers();
                    return true;
                }
                exit();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            if (getCurrentFocus() != null) {
                showSnackBar("再按一次退出程序");
            }
            exitTime = System.currentTimeMillis();
        } else {
            DataBackup.getInstance().autoSave();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

}
