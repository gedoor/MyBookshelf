//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
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

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseTabActivity;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ChapterContentHelp;
import com.kunfei.bookshelf.help.LauncherIcon;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.presenter.MainPresenter;
import com.kunfei.bookshelf.presenter.contract.MainContract;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.utils.theme.NavigationViewUtil;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.fragment.BookListFragment;
import com.kunfei.bookshelf.view.fragment.FindBookFragment;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kunfei.bookshelf.utils.NetworkUtil.isNetWorkAvailable;

public class MainActivity extends BaseTabActivity<MainContract.Presenter> implements MainContract.View, BookListFragment.CallBackValue {
    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private static final int FILE_SELECT_RESULT = 13;
    private String[] mTitles;

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

    private AppCompatImageView vwNightTheme;
    private int group;
    private boolean viewIsList;
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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        String shared_url = preferences.getString("shared_url", "");
        assert shared_url != null;
        if (shared_url.length() > 1) {
            moDialogHUD.showInputBox(getString(R.string.add_book_url),
                    shared_url,
                    null,
                    inputText -> {
                        inputText = StringUtils.trim(inputText);
                        mPresenter.addBookUrl(inputText);
                    });

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
        viewIsList = preferences.getBoolean("bookshelfIsList", true);
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
        cardSearch.setCardBackgroundColor(ThemeStore.primaryColorDark(this));
        initDrawer();
        initTabLayout();
        upGroup(group);
        moDialogHUD = new MoDialogHUD(this);
        if (!preferences.getBoolean("behaviorMain", true)) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(0);
        }
        //点击跳转搜索页
        cardSearch.setOnClickListener(view -> startActivityByAnim(new Intent(this, SearchBookActivity.class),
                toolbar, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out));
    }

    //初始化TabLayout和ViewPager
    private void initTabLayout() {
        mTlIndicator.setBackgroundColor(ThemeStore.backgroundColor(this));
        mTlIndicator.setSelectedTabIndicatorColor(ThemeStore.accentColor(this));
        //TabLayout使用自定义Item
        for (int i = 0; i < mTlIndicator.getTabCount(); i++) {
            TabLayout.Tab tab = mTlIndicator.getTabAt(i);
            if (tab == null) return;
            if (i == 0) { //设置第一个Item的点击事件(当下标为0时触发)
                tab.setCustomView(tab_icon(mTitles[i], R.drawable.ic_arrow_drop_down_black_24dp));
            } else {
                tab.setCustomView(tab_icon(mTitles[i], R.drawable.ic_arrow_drop_down_black_24dp));
            }
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
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("findTypeIsFlexBox", !preferences.getBoolean("findTypeIsFlexBox", true));
            editor.apply();
            RxBus.get().post(RxBusTag.UP_FIND_STYLE, new Object());
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
            im.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
        } else {
            im.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
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

    private View tab_icon(String name, Integer iconID) {
        @SuppressLint("InflateParams")
        View tabView = LayoutInflater.from(this).inflate(R.layout.tab_view_icon_right, null);
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
        if (vwNightTheme != null) {
            upThemeVw();
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
                PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
                    @Override
                    public void onHasPermission() {
                        startActivity(new Intent(MainActivity.this, ImportBookActivity.class));
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDown(String... permission) {
                        MainActivity.this.toast(R.string.import_per);
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                        PermissionUtils.requestMorePermissions(MainActivity.this, permission, FILE_SELECT_RESULT);
                    }
                });
                break;
            case R.id.action_add_url:
                moDialogHUD.showInputBox(getString(R.string.add_book_url),
                        null,
                        null,
                        inputText -> {
                            inputText = StringUtils.trim(inputText);
                            mPresenter.addBookUrl(inputText);
                        });
                break;
            case R.id.action_download_all:
                if (!isNetWorkAvailable())
                    toast(R.string.network_connection_unavailable);
                else
                    RxBus.get().post(RxBusTag.DOWNLOAD_ALL, 10000);
                break;
            case R.id.action_list_grid:
                editor.putBoolean("bookshelfIsList", !viewIsList);
                editor.apply();
                recreate();
                break;
            case R.id.action_clear_cache:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_content)
                        .setMessage(getString(R.string.sure_del_download_book))
                        .setPositiveButton(R.string.yes, (dialog, which) -> BookshelfHelp.clearCaches(true))
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> BookshelfHelp.clearCaches(false))
                        .show();
                break;
            case R.id.action_clearBookshelf:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_bookshelf)
                        .setMessage(R.string.clear_bookshelf_s)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.clearBookshelf())
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        })
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

    /**
     * 侧边栏按钮
     */
    private void setUpNavigationView() {
        NavigationViewUtil.setItemTextColors(navigationView, getResources().getColor(R.color.tv_text_default), ThemeStore.accentColor(this));
        NavigationViewUtil.setItemIconColors(navigationView, getResources().getColor(R.color.tv_text_default), ThemeStore.accentColor(this));
        NavigationViewUtil.disableScrollbar(navigationView);
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(this).inflate(R.layout.navigation_header, null);
        navigationView.addHeaderView(headerView);
        Menu drawerMenu = navigationView.getMenu();
        vwNightTheme = drawerMenu.findItem(R.id.action_theme).getActionView().findViewById(R.id.iv_theme_day_night);
        upThemeVw();
        vwNightTheme.setOnClickListener(view -> setNightTheme(!isNightTheme()));
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawer.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.action_book_source_manage:
                    handler.postDelayed(() -> BookSourceActivity.startThis(this), 200);
                    break;
                case R.id.action_replace_rule:
                    handler.postDelayed(() -> ReplaceRuleActivity.startThis(this), 200);
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
                    handler.postDelayed(this::backup, 200);
                    break;
                case R.id.action_restore:
                    handler.postDelayed(this::restore, 200);
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
            vwNightTheme.setImageResource(R.drawable.ic_daytime_24dp);
            vwNightTheme.setContentDescription(getString(R.string.click_to_day));
        } else {
            vwNightTheme.setImageResource(R.drawable.ic_brightness);
            vwNightTheme.setContentDescription(getString(R.string.click_to_night));
        }
        vwNightTheme.getDrawable().mutate().setColorFilter(ThemeStore.accentColor(this), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * 备份
     */
    private void backup() {
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.backup_confirmation)
                        .setMessage(R.string.backup_message)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.backupData())
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        })
                        .show();
                ATH.setAlertDialogTint(alertDialog);
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                MainActivity.this.toast(R.string.backup_permission);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                PermissionUtils.requestMorePermissions(MainActivity.this, permission, BACKUP_RESULT);
            }
        });
    }

    /**
     * 恢复
     */
    private void restore() {
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.restore_confirmation)
                        .setMessage(R.string.restore_message)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.restoreData())
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        })
                        .show();
                ATH.setAlertDialogTint(alertDialog);
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                MainActivity.this.toast(R.string.restore_permission);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                PermissionUtils.requestMorePermissions(MainActivity.this, permission, RESTORE_RESULT);
            }
        });
    }

    /**
     * 新版本运行
     */
    private void versionUpRun() {
        if (preferences.getInt("versionCode", 0) != MApplication.getVersionCode()) {
            //保存版本号
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", MApplication.getVersionCode());
            editor.apply();
            //更新日志
            moDialogHUD.showAssetMarkdown("updateLog.md");
        }
    }

    /**
     * 获取权限
     */
    private void requestPermission() {
        List<String> per = PermissionUtils.checkMorePermissions(this, MApplication.PerList);
        if (per.size() > 0) {
            toast(R.string.get_storage_per);
            PermissionUtils.requestMorePermissions(this, per, MApplication.RESULT__PERMS);
        }
    }

    @Override
    protected void firstRequest() {
        if (!isRecreate) {
            versionUpRun();
            requestPermission();
            handler.postDelayed(this::preloadReader, 200);
        }
        handler.postDelayed(() -> UpLastChapterModel.getInstance().startUpdate(), 60 * 1000);
    }

    @Override
    public void dismissHUD() {
        moDialogHUD.dismiss();
    }

    public void onRestore(String msg) {
        moDialogHUD.showLoading(msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                switch (requestCode) {
                    case FILE_SELECT_RESULT:
                        startActivity(new Intent(MainActivity.this, ImportBookActivity.class));
                        break;
                    case BACKUP_RESULT:
                        backup();
                        break;
                    case RESTORE_RESULT:
                        restore();
                        break;
                }
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                switch (requestCode) {
                    case FILE_SELECT_RESULT:
                        MainActivity.this.toast(R.string.import_book_per);
                        break;
                    case BACKUP_RESULT:
                        MainActivity.this.toast(R.string.backup_permission);
                        break;
                    case RESTORE_RESULT:
                        MainActivity.this.toast(R.string.restore_permission);
                        break;
                }
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                switch (requestCode) {
                    case FILE_SELECT_RESULT:
                        MainActivity.this.toast(R.string.import_book_per);
                        break;
                    case BACKUP_RESULT:
                        MainActivity.this.toast(R.string.backup_permission);
                        break;
                    case RESTORE_RESULT:
                        MainActivity.this.toast(R.string.restore_permission);
                        break;
                }
                PermissionUtils.toAppSetting(MainActivity.this);
            }
        });
    }

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

    /**
     * 退出
     */
    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showSnackBar(toolbar, getString(R.string.double_click_exit));
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        UpLastChapterModel.getInstance().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void preloadReader() {
        AsyncTask.execute(() -> {
            ReadBookControl.getInstance();
            ChapterContentHelp.getInstance();
        });
    }

}
