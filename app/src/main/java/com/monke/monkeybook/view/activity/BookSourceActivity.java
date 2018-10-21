package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.LinearLayout;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.BookSourcePresenterImpl;
import com.monke.monkeybook.presenter.contract.BookSourceContract;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.view.adapter.BookSourceAdapter;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.qqtheme.framework.picker.FilePicker;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class BookSourceActivity extends MBaseActivity<BookSourceContract.Presenter> implements BookSourceContract.View {
    public static final int EDIT_SOURCE = 101;
    private final int IMPORT_SOURCE = 102;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.searchView)
    SearchView searchView;

    private boolean selectAll = true;
    private MenuItem groupItem;
    private SubMenu groupMenu;
    private BookSourceAdapter adapter;
    private MoProgressHUD moProgressHUD;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean isSearch;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, BookSourceActivity.class));
    }

    @Override
    protected BookSourceContract.Presenter initInjector() {
        return new BookSourcePresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_recycler_serach_vew);
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
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initSearchView();
        initRecyclerView();
        moProgressHUD = new MoProgressHUD(this);
    }

    private void initSearchView() {
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
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
        adapter.resetDataS(BookSourceManage.getAllBookSource());
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        itemTouchHelpCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchHelpCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
            List<BookSourceBean> sourceBeanList = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .whereOr(BookSourceBeanDao.Properties.BookSourceName.like(term),
                            BookSourceBeanDao.Properties.BookSourceGroup.like(term),
                            BookSourceBeanDao.Properties.BookSourceUrl.like(term))
                    .orderRaw("-WEIGHT ASC")
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
            adapter.resetDataS(sourceBeanList);
        } else {
            adapter.resetDataS(BookSourceManage.getAllBookSource());
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
        upGroupMenu();
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
                String cacheUrl = ACache.get(this).getAsString("sourceUrl");
                moProgressHUD.showInputBox("输入书源网址", TextUtils.isEmpty(cacheUrl) ? getString(R.string.default_source_url) : cacheUrl,
                        inputText -> {
                            ACache.get(this).put("sourceUrl", inputText);
                            mPresenter.importBookSource(inputText);
                        });
                break;
            case R.id.action_revert_selection:
                revertSelection();
                break;
            case R.id.action_del_select:
                mPresenter.delData(adapter.getSelectDataList());
                break;
            case R.id.action_reset_book_source:
                mPresenter.importBookSource(getString(R.string.default_source_url));
                break;
            case R.id.action_check_book_source:
                mPresenter.checkBookSource();
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
        if (BookSourceManage.groupList.size() == 0) {
            groupItem.setVisible(false);
        } else {
            groupItem.setVisible(true);
            for (String groupName : new ArrayList<>(BookSourceManage.groupList)) {
                groupMenu.add(R.id.source_group, Menu.NONE, Menu.NONE, groupName);
            }
        }
    }

    private void addBookSource() {
        Intent intent = new Intent(this, SourceEditActivity.class);
        startActivityForResult(intent, EDIT_SOURCE);
    }

    private void selectBookSourceFile() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            FilePicker filePicker = new FilePicker(this, FilePicker.FILE);
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
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.import_book_source),
                    MApplication.RESULT__PERMS, MApplication.PerList);
        }
    }

    private void selectFileSys() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");//设置类型
        startActivityForResult(intent, IMPORT_SOURCE);
    }

    @AfterPermissionGranted(MApplication.RESULT__PERMS)
    private void resultImportPerms() {
        selectBookSourceFile();
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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case EDIT_SOURCE:
                    refreshBookSource();
                    break;
                case IMPORT_SOURCE:
                    if (data != null) {
                        mPresenter.importBookSourceLocal(FileUtil.getPath(this, data.getData()));
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
