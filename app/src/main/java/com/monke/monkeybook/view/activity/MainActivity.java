//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.LauncherIcon;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.MainPresenterImpl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.MainContract;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.view.adapter.BookShelfGridAdapter;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.OnItemClickListenerTwo;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends MBaseActivity<MainContract.Presenter> implements MainContract.View {
    private static final int REQUEST_SETTING = 210;
    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private static final int FILE_SELECT_RESULT = 13;
    private static final int REQUEST_CODE_SIGN_IN = 14;

    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.rv_bookshelf)
    RecyclerView rvBookshelf;
    @BindView(R.id.main_view)
    LinearLayout mainView;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    private TextView tvUser;
    private Switch swNightTheme;
    private int group;
    private BookShelfGridAdapter bookShelfGridAdapter;
    private BookShelfListAdapter bookShelfListAdapter;
    private boolean viewIsList;
    private ActionBarDrawerToggle mDrawerToggle;
    private MoProgressHUD moProgressHUD;
    private long exitTime = 0;
    private String bookPx;

    @Override
    protected MainContract.Presenter initInjector() {
        return new MainPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            group = savedInstanceState.getInt("group");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("group", group);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_main);
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
        bookPx = preferences.getString(getString(R.string.pk_bookshelf_px), "0");
        if (viewIsList) {
            bookShelfListAdapter = new BookShelfListAdapter(this, getNeedAnim());
        } else {
            bookShelfGridAdapter = new BookShelfGridAdapter(this, getNeedAnim());
        }
    }

    private List<BookShelfBean> getBookshelfList() {
        if (viewIsList) {
            return bookShelfListAdapter.getBooks();
        } else {
            return bookShelfGridAdapter.getBooks();
        }
    }

    private boolean getNeedAnim() {
        return preferences.getBoolean(getString(R.string.pk_bookshelf_anim), false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupActionBar();
        initDrawer();
        upGroup(group);
        moProgressHUD = new MoProgressHUD(this);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        if (viewIsList) {
            rvBookshelf.setAdapter(bookShelfListAdapter);
            rvBookshelf.setLayoutManager(new LinearLayoutManager(this));
        } else {
            rvBookshelf.setAdapter(bookShelfGridAdapter);
            rvBookshelf.setLayoutManager(new GridLayoutManager(this, 3));
        }
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
    protected void bindEvent() {
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.queryBookShelf(NetworkUtil.isNetWorkAvailable(), group);
            if (!NetworkUtil.isNetWorkAvailable()) {
                Toast.makeText(MainActivity.this, "无网络，请打开网络后再试。", Toast.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        });
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        if (bookPx.equals("2")) {
            itemTouchHelpCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        } else {
            itemTouchHelpCallback.setDragEnable(false);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        }
        if (viewIsList) {
            bookShelfListAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfListAdapter.getItemTouchCallbackListener());
        } else {
            bookShelfGridAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfGridAdapter.getItemTouchCallbackListener());
        }

    }

    private OnItemClickListenerTwo getAdapterListener() {
        return new OnItemClickListenerTwo() {
            @Override
            public void onClick(View view, int index) {
                BookShelfBean bookShelfBean = getBookshelfList().get(index);
                bookShelfBean.setHasUpdate(false);
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
                Intent intent = new Intent(MainActivity.this, ReadBookActivity.class);
                intent.putExtra("from", ReadBookPresenterImpl.OPEN_FROM_APP);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                try {
                    BitIntentDataManager.getInstance().putData(key, bookShelfBean.clone());
                } catch (CloneNotSupportedException e) {
                    BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                    e.printStackTrace();
                }
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onLongClick(View view, int index) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, getBookshelfList().get(index));
                startActivityByAnim(intent, view, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem pauseMenu = menu.getItem(6);
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
            case R.id.action_search:
                //点击搜索
                startActivityByAnim(new Intent(this, SearchBookActivity.class),
                        toolbar, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.action_library:
                startActivityByAnim(new Intent(this, FindBookActivity.class),
                        toolbar, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out);
                break;
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
            case R.id.action_download:
                startActivity(new Intent(this, DownloadActivity.class));
                break;
            case R.id.action_download_all:
                mPresenter.downloadAll();
                break;
            case R.id.action_list_grid:
                editor.putBoolean("bookshelfIsList", !viewIsList);
                editor.apply();
                recreate();
                break;
            case R.id.action_change_icon:
                LauncherIcon.Change();
                break;
            case R.id.action_clearBookshelf:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_bookshelf)
                        .setMessage(R.string.clear_bookshelf_s)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.clearBookshelf())
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> { })
                        .show();
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
            this.group = group;
            mPresenter.queryBookShelf(false, group);
        }
        switch (group) {
            case 1:
                navigationView.setCheckedItem(R.id.action_group_yf);
                break;
            default:
                navigationView.setCheckedItem(R.id.action_group_zg);
                break;
        }
    }

    //侧边栏按钮
    private void setUpNavigationView() {
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(this).inflate(R.layout.navigation_header, null);
        navigationView.addHeaderView(headerView);
        tvUser = headerView.findViewById(R.id.tv_user);
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
            switch (menuItem.getItemId()) {
                case R.id.action_group_zg:
                    upGroup(0);
                    break;
                case R.id.action_group_yf:
                    upGroup(1);
                    break;
                case R.id.action_book_source_manage:
                    startActivity(new Intent(MainActivity.this, BookSourceActivity.class));
                    break;
                case R.id.action_replace_rule:
                    startActivity(new Intent(MainActivity.this, ReplaceRuleActivity.class));
                    break;
                case R.id.action_setting:
                    startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), REQUEST_SETTING);
                    break;
                case R.id.action_about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
                case R.id.action_donate:
                    startActivity(new Intent(MainActivity.this, DonateActivity.class));
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
            drawer.closeDrawers();
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

        //弃用bug图标
        PackageManager packageManager = MApplication.getInstance().getPackageManager();
        ComponentName componentNameBookMain = new ComponentName(MApplication.getInstance(), "com.monke.monkeybook.view.activity.WelcomeBookActivity");
        ComponentName componentNameBook = new ComponentName(MApplication.getInstance(), "com.monke.monkeybook.BookIcon");

        if (packageManager.getComponentEnabledSetting(componentNameBook) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            //启用
            packageManager.setComponentEnabledSetting(componentNameBookMain,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            //禁用
            packageManager.setComponentEnabledSetting(componentNameBook,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private boolean haveRefresh() {
        boolean isRecreate = getIntent().getBooleanExtra("isRecreate", false);
        getIntent().putExtra("isRecreate", true);
        return preferences.getBoolean(getString(R.string.pk_auto_refresh), false) && !isRecreate;
    }

    @Override
    protected void firstRequest() {
        versionUpRun();
        if (NetworkUtil.isNetWorkAvailable()) {
            mPresenter.queryBookShelf(haveRefresh(), group);
        } else {
            mPresenter.queryBookShelf(false, group);
            Toast.makeText(this, "无网络，自动刷新失败！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList) {
        if (viewIsList) {
            bookShelfListAdapter.replaceAll(bookShelfBeanList, bookPx);
        } else {
            bookShelfGridAdapter.replaceAll(bookShelfBeanList, bookPx);
        }
    }

    @Override
    public void refreshBook(String noteUrl) {
        if (viewIsList) {
            bookShelfListAdapter.refreshBook(noteUrl);
        } else {
            bookShelfGridAdapter.refreshBook(noteUrl);
        }
    }

    @Override
    public void activityRefreshView() {

    }

    @Override
    public void dismissHUD() {
        moProgressHUD.dismiss();
    }

    @Override
    public void refreshError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
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
    public SharedPreferences getPreferences() {
        return preferences;
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

    @Override
    public void recreate(){
        super.recreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Snackbar.make(rvBookshelf, "再按一次退出程序", Snackbar.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

}