//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.Donate;
import com.monke.monkeybook.presenter.IMainPresenter;
import com.monke.monkeybook.presenter.impl.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.impl.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.MainPresenterImpl;
import com.monke.monkeybook.view.IMainView;
import com.monke.monkeybook.view.adapter.BookShelfGridAdapter;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.popupwindow.DownloadListPop;
import com.monke.monkeybook.widget.refreshview.OnRefreshWithProgressListener;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends MBaseActivity<IMainPresenter> implements IMainView {
    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private BookShelfGridAdapter bookShelfGridAdapter;
    private BookShelfListAdapter bookShelfListAdapter;
    private boolean viewIsList;

    private ActionBarDrawerToggle mDrawerToggle;

    private DownloadListPop downloadListPop;

    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rf_rv_shelf)
    RefreshRecyclerView rfRvShelf;

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

        downloadListPop = new DownloadListPop(MainActivity.this);

        if (viewIsList) {
            rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfListAdapter, new LinearLayoutManager(this));
        } else {
            rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfGridAdapter, new LinearLayoutManager(this));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 这个必须要，没有的话进去的默认是个箭头。。正常应该是三横杠的
        mDrawerToggle.syncState();
    }

    @Override
    protected void bindEvent() {
        bindRvShelfEvent();
        if (viewIsList) {
            bookShelfListAdapter.setItemClickListener(getAdapterListener());
        } else {
            bookShelfGridAdapter.setItemClickListener(getAdapterListener());
        }
    }

    private RefreshRecyclerViewAdapter.OnItemClickListener getAdapterListener() {
        return new RefreshRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void toSearch() {
                //点击去选书
                startActivityByAnim(new Intent(MainActivity.this, LibraryActivity.class), 0, 0);
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
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = preferences.edit();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                //点击搜索
                startActivityByAnim(new Intent(this, SearchActivity.class),
                        toolbar, "to_search", android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.action_download:
                downloadListPop.showAsDropDown(toolbar);
                break;
            case R.id.action_list_grid:
                editor.putBoolean("bookshelfIsList", !viewIsList);
                editor.apply();
                finish();
                startActivity(getIntent());
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
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawer.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.action_library:
                    startActivityByAnim(new Intent(MainActivity.this, LibraryActivity.class), 0, 0);
                    break;
                case R.id.action_add_local:
                    startActivityByAnim(new Intent(MainActivity.this, ImportBookActivity.class), 0, 0);
                    break;
                case R.id.action_book_source_manage:
                    startActivityByAnim(new Intent(MainActivity.this, BookSourceManageActivity.class), 0, 0);
                    break;
                case R.id.action_setting:
                    startActivityByAnim(new Intent(MainActivity.this, SettingActivity.class), 0, 0);
                    break;
                case R.id.action_about:
                    startActivityByAnim(new Intent(MainActivity.this, AboutActivity.class), 0, 0);
                    break;
                case R.id.action_donate:
                    Donate.aliDonate(this);
                    break;
                case R.id.action_backup:
                    backup();
                    break;
                case R.id.action_restore:
                    restore();
                    break;
            }
            return true;
        });
    }
    //备份
    private void backup() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.backup_confirmation)
                    .setMessage(R.string.backup_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        mPresenter.backupBookShelf();
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.backup_permission),
                    BACKUP_RESULT, perms);
        }
    }
    //恢复
    private void restore() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.restore_confirmation)
                    .setMessage(R.string.restore_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        mPresenter.restoreBookShelf();
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.restore_permission),
                    RESTORE_RESULT, perms);
        }
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

    @Override
    protected void firstRequest() {
        mPresenter.queryBookShelf(preferences.getBoolean(getString(R.string.pk_auto_refresh), false));
    }

    @Override
    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList) {
        if (viewIsList) {
            bookShelfListAdapter.replaceAll(bookShelfBeanList);
        } else {
            bookShelfGridAdapter.replaceAll(bookShelfBeanList);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(BACKUP_RESULT)
    private void backupResult() {
        backup();
    }

    @AfterPermissionGranted(RESTORE_RESULT)
    private void restoreResult() {
        restore();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadListPop.onDestroy();
    }

    private long exitTime = 0;

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
}