package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.BookSourceBeanDao;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.BookSourcePresenter;
import com.kunfei.bookshelf.presenter.contract.BookSourceContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.BookSourceAdapter;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.qqtheme.framework.picker.FilePicker;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class BookSourceActivity extends MBaseActivity<BookSourceContract.Presenter> implements BookSourceContract.View {
    private final int IMPORT_SOURCE = 102;
    private final int REQUEST_QR = 202;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.searchView)
    SearchView searchView;

    private ItemTouchCallback itemTouchCallback;
    private boolean selectAll = true;
    private MenuItem groupItem;
    private SubMenu groupMenu;
    private SubMenu sortMenu;
    private BookSourceAdapter adapter;
    private MoDialogHUD moDialogHUD;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean isSearch;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, BookSourceActivity.class));
    }

    @Override
    protected BookSourceContract.Presenter initInjector() {
        return new BookSourcePresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_book_source);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        moDialogHUD = new MoDialogHUD(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        RxBus.get().post(RxBusTag.SOURCE_LIST_CHANGE, true);
        super.onDestroy();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void bindView() {
        super.bindView();
        initSearchView();
        initRecyclerView();
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        refreshBookSource();
    }

    private void initSearchView() {
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setTextSize(16);
        searchView.setQueryHint(getString(R.string.search_book_source));
        searchView.onActionViewExpanded();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                isSearch = !TextUtils.isEmpty(newText);
                refreshBookSource();
                return false;
            }
        });
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookSourceAdapter(this);
        recyclerView.setAdapter(adapter);
        itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setDragEnable(preferences.getInt("SourceSort", 0));
    }

    private void setDragEnable(int sort) {
        if (itemTouchCallback == null) {
            return;
        }
        adapter.setSort(sort);
        if (sort == 0) {
            itemTouchCallback.setDragEnable(true);
        } else {
            itemTouchCallback.setDragEnable(false);
        }
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            if (!bookSourceBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    private void selectAllDataS() {
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            bookSourceBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        saveDate(adapter.getDataList());
    }

    private void revertSelection() {
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            bookSourceBean.setEnable(!bookSourceBean.getEnable());
        }
        adapter.notifyDataSetChanged();
        saveDate(adapter.getDataList());
    }

    public void upSearchView(int size) {
        searchView.setQueryHint(getString(R.string.search_book_source_num, size));
    }

    @Override
    public void refreshBookSource() {
        if (isSearch) {
            String term = "%" + searchView.getQuery() + "%";
            List<BookSourceBean> sourceBeanList = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                    .whereOr(BookSourceBeanDao.Properties.BookSourceName.like(term),
                            BookSourceBeanDao.Properties.BookSourceGroup.like(term),
                            BookSourceBeanDao.Properties.BookSourceUrl.like(term))
                    .orderRaw(BookSourceManager.getBookSourceSort())
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
            adapter.resetDataS(sourceBeanList);
        } else {
            adapter.resetDataS(BookSourceManager.getAllBookSource());
        }
    }

    public void delBookSource(BookSourceBean bookSource) {
        mPresenter.delData(bookSource);
    }

    public void saveDate(BookSourceBean date) {
        mPresenter.saveData(date);
    }

    public void saveDate(List<BookSourceBean> date) {
        mPresenter.saveData(date);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.book_source_manage);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_source_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        groupItem = menu.findItem(R.id.action_group);
        groupMenu = groupItem.getSubMenu();
        sortMenu = menu.findItem(R.id.action_sort).getSubMenu();
        upGroupMenu();
        upSortMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_book_source:
                addBookSource();
                break;
            case R.id.action_select_all:
                selectAllDataS();
                break;
            case R.id.action_import_book_source_local:
                selectBookSourceFile();
                break;
            case R.id.action_import_book_source_onLine:
                importBookSourceOnLine();
                break;
            case R.id.action_import_book_source_rwm:
                scanBookSource();
                break;
            case R.id.action_revert_selection:
                revertSelection();
                break;
            case R.id.action_del_select:
                deleteDialog();
                break;
            case R.id.action_check_book_source:
                mPresenter.checkBookSource();
                break;
            case R.id.sort_manual:
                upSourceSort(0);
                break;
            case R.id.sort_auto:
                upSourceSort(1);
                break;
            case R.id.sort_pin_yin:
                upSourceSort(2);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        if (item.getGroupId() == R.id.source_group) {
            searchView.setQuery(item.getTitle(), true);
        }
        return super.onOptionsItemSelected(item);
    }

    public void upGroupMenu() {
        if (groupMenu == null) return;
        groupMenu.removeGroup(R.id.source_group);
        if (BookSourceManager.groupList.size() == 0) {
            groupItem.setVisible(false);
        } else {
            groupItem.setVisible(true);
            for (String groupName : new ArrayList<>(BookSourceManager.groupList)) {
                groupMenu.add(R.id.source_group, Menu.NONE, Menu.NONE, groupName);
            }
        }
    }

    private void upSortMenu() {
        sortMenu.getItem(0).setChecked(false);
        sortMenu.getItem(1).setChecked(false);
        sortMenu.getItem(2).setChecked(false);
        sortMenu.getItem(preferences.getInt("SourceSort", 0)).setChecked(true);
    }

    private void upSourceSort(int sort) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("SourceSort", sort);
        editor.apply();
        upSortMenu();
        setDragEnable(sort);
        BookSourceManager.refreshBookSource();
        refreshBookSource();
    }

    private void scanBookSource() {
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(intent, REQUEST_QR);
    }

    private void addBookSource() {
        Intent intent = new Intent(this, SourceEditActivity.class);
        startActivityForResult(intent, SourceEditActivity.EDIT_SOURCE);
    }

    private void deleteDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.del_msg)
                .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.delData(adapter.getSelectDataList()))
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                })
                .show();
        ATH.setAlertDialogTint(alertDialog);
    }

    private void selectBookSourceFile() {
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                FilePicker filePicker = new FilePicker(BookSourceActivity.this, FilePicker.FILE);
                filePicker.setBackgroundColor(getResources().getColor(R.color.background));
                filePicker.setTopBackgroundColor(getResources().getColor(R.color.background));
                filePicker.setAllowExtensions(getResources().getStringArray(R.array.text_suffix));
                filePicker.setOnFilePickListener(s -> mPresenter.importBookSourceLocal(s));
                filePicker.show();
                filePicker.getSubmitButton().setText(R.string.sys_file_picker);
                filePicker.getSubmitButton().setOnClickListener(view -> {
                    filePicker.dismiss();
                    selectFileSys();
                });
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                BookSourceActivity.this.toast(R.string.import_book_source);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                PermissionUtils.requestMorePermissions(BookSourceActivity.this, permission, MApplication.RESULT__PERMS);
            }
        });
    }

    private void importBookSourceOnLine() {
        String cacheUrl = ACache.get(this).getAsString("sourceUrl");
        moDialogHUD.showInputBox(getString(R.string.input_book_source_url),
                cacheUrl,
                new String[]{cacheUrl},
                inputText -> {
                    inputText = StringUtils.trim(inputText);
                    ACache.get(this).put("sourceUrl", inputText);
                    mPresenter.importBookSource(inputText);
                });
    }

    private void selectFileSys() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");//设置类型
        startActivityForResult(intent, IMPORT_SOURCE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                selectBookSourceFile();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                BookSourceActivity.this.toast(R.string.import_book_source);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                BookSourceActivity.this.toast(R.string.import_book_source);
                PermissionUtils.toAppSetting(BookSourceActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SourceEditActivity.EDIT_SOURCE:
                    refreshBookSource();
                    break;
                case IMPORT_SOURCE:
                    if (data != null && data.getData() != null) {
                        mPresenter.importBookSourceLocal(FileUtils.getPath(this, data.getData()));
                    }
                    break;
                case REQUEST_QR:
                    if (data != null) {
                        String result = data.getStringExtra("result");
                        mPresenter.importBookSource(result);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearch) {
                try {
                    //如果搜索框中有文字，则会先清空文字.
                    mSearchAutoComplete.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(llContent, msg, length);
    }

    @Override
    public void showSnackBar(String msg, int length) {
        super.showSnackBar(llContent, msg, length);
    }

}
