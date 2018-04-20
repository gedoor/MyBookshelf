//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.LauncherIcon;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.MainPresenterImpl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IMainPresenter;
import com.monke.monkeybook.view.adapter.BookShelfGridAdapter;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.impl.IMainView;
import com.monke.monkeybook.view.popupwindow.DownloadListPop;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;
import com.monke.monkeybook.widget.refreshview.OnRefreshWithProgressListener;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends MBaseActivity<IMainPresenter> implements IMainView {
    private static final int REQUEST_SETTING = 210;
    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private static final int FILESELECT_RESULT = 13;
    private static final int REQUESTCODE_FROM_ACTIVITY = 110;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rf_rv_shelf)
    RefreshRecyclerView rfRvShelf;
    @BindView(R.id.iv_drawer_top)
    ImageView ivDrawerTop;

    private Switch swNightTheme;

    private BookShelfGridAdapter bookShelfGridAdapter;
    private BookShelfListAdapter bookShelfListAdapter;
    private boolean viewIsList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DownloadListPop downloadListPop;
    private MoProgressHUD moProgressHUD;
    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private long exitTime = 0;
    private boolean onRestore = false;
    private String bookPx;

    @Override
    protected IMainPresenter initInjector() {
        return new MainPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initData() {
        bookPx = preferences.getString(getString(R.string.pk_bookshelf_px), "0");
        viewIsList = preferences.getBoolean("bookshelfIsList", true);
        if (viewIsList) {
            bookShelfListAdapter = new BookShelfListAdapter();
        } else {
            bookShelfGridAdapter = new BookShelfGridAdapter();
        }
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
        moProgressHUD = new MoProgressHUD(this);
        downloadListPop = new DownloadListPop(MainActivity.this);

        if (viewIsList) {
            rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfListAdapter, new LinearLayoutManager(this));
        } else {
            rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfGridAdapter.setHeaderView(rfRvShelf.getHeader()),
                    bookShelfGridAdapter, new GridLayoutManager(this, 3));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 这个必须要，没有的话进去的默认是个箭头。。正常应该是三横杠的
        mDrawerToggle.syncState();
        if (swNightTheme != null) {
            swNightTheme.setChecked(preferences.getBoolean("nightTheme", false));
        }
    }

    @Override
    protected void bindEvent() {
        bindRvShelfEvent();
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        if (bookPx.equals("2")) {
            itemTouchHelpCallback.setDragEnable(true);
        }
        if (viewIsList) {
            bookShelfListAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfListAdapter.getItemTouchCallbackListener());
        } else {
            bookShelfGridAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfGridAdapter.getItemTouchCallbackListener());
        }
        rfRvShelf.setItemTouchHelperCallback(itemTouchHelpCallback);
    }

    private RefreshRecyclerViewAdapter.OnItemClickListener getAdapterListener() {
        return new RefreshRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void toSearch() {
                //点击去选书
                startActivityByAnim(new Intent(MainActivity.this, FindBookActivity.class), 0, 0);
            }

            @Override
            public void onClick(BookShelfBean bookShelfBean, int index) {
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
            public void onLongClick(View animView, BookShelfBean bookShelfBean, int index) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = preferences.edit();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                //点击搜索
                startActivityByAnim(new Intent(this, SearchBookActivity.class),
                        toolbar, "to_search", android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.action_add_url:
                moProgressHUD.showInputBox("添加书籍网址", null, inputText -> mPresenter.addBookUrl(inputText));
                break;
            case R.id.action_download:
                downloadListPop.showAsDropDown(toolbar);
                break;
            case R.id.action_list_grid:
                editor.putBoolean("bookshelfIsList", !viewIsList);
                editor.apply();
                recreate();
                break;
            case R.id.action_change_icon:
                LauncherIcon.Change();
                break;
            case R.id.action_clear_content:
                BookshelfHelp.clearLineContent();
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

    //侧边栏按钮
    private void setUpNavigationView() {
        ivDrawerTop.getDrawable().mutate();
        ivDrawerTop.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        Menu drawerMenu = navigationView.getMenu();
        swNightTheme = drawerMenu.findItem(R.id.action_night_theme).getActionView().findViewById(R.id.sw_night_theme);
        swNightTheme.setChecked(preferences.getBoolean("nightTheme", false));
        swNightTheme.setOnClickListener(view -> saveNightTheme(swNightTheme.isChecked()));
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawer.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.action_library:
                    startActivityByAnim(new Intent(MainActivity.this, FindBookActivity.class), 0, 0);
                    break;
                case R.id.action_add_local:
                    if (EasyPermissions.hasPermissions(this, perms)) {
                        startActivityByAnim(new Intent(MainActivity.this, FileFolderActivity.class), 0, 0);
                    } else {
                        EasyPermissions.requestPermissions(this, getString(R.string.import_book_source),
                                FILESELECT_RESULT, perms);
                    }
                    //startActivityByAnim(new Intent(MainActivity.this, ImportBookActivity.class), 0, 0);
                    break;
                case R.id.action_book_source_manage:
                    startActivityByAnim(new Intent(MainActivity.this, BookSourceActivity.class), 0, 0);
                    break;
                case R.id.action_replace_rule:
                    startActivityByAnim(new Intent(MainActivity.this, ReplaceRuleActivity.class), 0, 0);
                    break;
                case R.id.action_setting:
                    startActivityForResultByAnim(new Intent(MainActivity.this, SettingActivity.class), REQUEST_SETTING,0, 0);
                    break;
                case R.id.action_about:
                    startActivityByAnim(new Intent(MainActivity.this, AboutActivity.class), 0, 0);
                    break;
                case R.id.action_donate:
                    startActivityByAnim(new Intent(MainActivity.this, DonateActivity.class), 0, 0);
                    break;
                case R.id.action_backup:
                    backup();
                    break;
                case R.id.action_restore:
                    restore();
                    break;
                case R.id.action_night_theme:
                    swNightTheme.setChecked(!swNightTheme.isChecked());
                    saveNightTheme(swNightTheme.isChecked());
                    break;
            }
            return true;
        });
    }

    private void saveNightTheme(Boolean isNightTheme) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("nightTheme", isNightTheme);
        editor.apply();
        if (isNightTheme) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate();
    }

    //备份
    private void backup() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.backup_confirmation)
                    .setMessage(R.string.backup_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.backupData())
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.backup_permission),
                    BACKUP_RESULT, perms);
        }
    }

    @AfterPermissionGranted(BACKUP_RESULT)
    private void backupResult() {
        backup();
    }

    //恢复
    private void restore() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.restore_confirmation)
                    .setMessage(R.string.restore_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.restoreData())
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.restore_permission),
                    RESTORE_RESULT, perms);
        }
    }

    @AfterPermissionGranted(RESTORE_RESULT)
    private void restoreResult() {
        restore();
    }

    @AfterPermissionGranted(FILESELECT_RESULT)
    private void fileSelectResult() {
        startActivityByAnim(new Intent(MainActivity.this, FileFolderActivity.class), 0, 0);
    }

    private void bindRvShelfEvent() {
        //下拉刷新
        rfRvShelf.setBaseRefreshListener(new OnRefreshWithProgressListener() {
            @Override
            public int getMaxProgress() {
                if (viewIsList) {
                    return bookShelfListAdapter.getBooks().size();
                } else {
                    return bookShelfGridAdapter.getBooks().size();
                }
            }

            @Override
            public void startRefresh() {
                mPresenter.queryBookShelf(true);
            }
        });
    }

    private void fistOpenRun() {
        if (preferences.getInt("versionCode", 0) != MApplication.getVersionCode()) {
            //书源为空时加载默认书源
            BookSourceManage.initDefaultBookSource(this);
            //保存版本号
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", MApplication.getVersionCode());
            editor.apply();
        }
    }

    @Override
    protected void firstRequest() {
        fistOpenRun();
        mPresenter.queryBookShelf(preferences.getBoolean(getString(R.string.pk_auto_refresh), false));
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
    public void activityRefreshView() {
        //执行刷新响应
        rfRvShelf.startRefresh();
    }

    @Override
    public void refreshFinish() {
        rfRvShelf.finishRefresh(false, true);
        if (onRestore) {
            moProgressHUD.dismiss();
        }
    }

    @Override
    public void refreshError(String error) {
        refreshFinish();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshRecyclerViewItemAdd() {
        rfRvShelf.getRpb().setDurProgress(rfRvShelf.getRpb().getDurProgress() + 1);
    }

    @Override
    public void setRecyclerMaxProgress(int x) {
        rfRvShelf.getRpb().setMaxProgress(x);
    }

    @Override
    public void showLoading(String msg) {
        moProgressHUD.showLoading(msg);
    }

    @Override
    public void onRestore() {
        onRestore = true;
        moProgressHUD.showLoading(getString(R.string.restore_success));
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
    protected void onDestroy() {
        super.onDestroy();
        downloadListPop.onDestroy();
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Snackbar.make(rfRvShelf, "再按一次退出程序", Snackbar.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTING) {
            if (!bookPx.equals(preferences.getString(getString(R.string.pk_bookshelf_px), "0"))) {
                recreate();
            }
        }
    }
}