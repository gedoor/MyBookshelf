package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.help.permission.Permissions;
import com.kunfei.bookshelf.help.permission.PermissionsCompat;
import com.kunfei.bookshelf.model.TxtChapterRuleManager;
import com.kunfei.bookshelf.presenter.TxtChapterRulePresenter;
import com.kunfei.bookshelf.presenter.contract.TxtChapterRuleContract;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.TxtChapterRuleAdapter;
import com.kunfei.bookshelf.widget.filepicker.picker.FilePicker;
import com.kunfei.bookshelf.widget.modialog.TxtChapterRuleDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Unit;

public class TxtChapterRuleActivity extends MBaseActivity<TxtChapterRuleContract.Presenter> implements TxtChapterRuleContract.View {
    private final int requestImport = 102;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private TxtChapterRuleAdapter adapter;
    private boolean selectAll = true;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, TxtChapterRuleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected TxtChapterRuleContract.Presenter initInjector() {
        return new TxtChapterRulePresenter();
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_recycler_vew);
    }

    @Override
    protected void initData() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initRecyclerView();
        refresh();
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.txt_chapter_regex);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_replace_rule_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_replace_rule:
                editChapterRule(null);
                break;
            case R.id.action_select_all:
                selectAllDataS();
                break;
            case R.id.action_import:
                selectReplaceRuleFile();
                break;
            case R.id.action_import_onLine:

                break;
            case R.id.action_del_all:
                mPresenter.delData(adapter.getData());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TxtChapterRuleAdapter(this);
        recyclerView.setAdapter(adapter);
        ItemTouchCallback itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void editChapterRule(TxtChapterRuleBean txtChapterRuleBean) {
        TxtChapterRuleDialog.builder(this, txtChapterRuleBean)
                .setPositiveButton(txtChapterRuleBean1 -> {
                    if (txtChapterRuleBean != null) {
                        TxtChapterRuleManager.del(txtChapterRuleBean);
                    }
                    TxtChapterRuleManager.save(txtChapterRuleBean1);
                    refresh();
                })
                .show();
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (TxtChapterRuleBean ruleBean : adapter.getData()) {
            if (ruleBean.getEnable() == null || !ruleBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    private void selectAllDataS() {
        for (TxtChapterRuleBean ruleBean : adapter.getData()) {
            ruleBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        TxtChapterRuleManager.save(adapter.getData());
    }

    public void delData(TxtChapterRuleBean ruleBean) {
        mPresenter.delData(ruleBean);
    }

    public void saveDataS() {
        mPresenter.saveData(adapter.getData());
    }

    @Override
    public void refresh() {
        adapter.resetDataS(TxtChapterRuleManager.getAll());
    }

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(llContent, msg, length);
    }

    private void selectReplaceRuleFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.get_storage_per)
                .onGranted((requestCode) -> {
                    FilePicker filePicker = new FilePicker(TxtChapterRuleActivity.this, FilePicker.FILE);
                    filePicker.setBackgroundColor(getResources().getColor(R.color.background));
                    filePicker.setTopBackgroundColor(getResources().getColor(R.color.background));
                    filePicker.setItemHeight(30);
                    filePicker.setAllowExtensions(getResources().getStringArray(R.array.text_suffix));
                    filePicker.setOnFilePickListener(s -> mPresenter.importDataSLocal(s));
                    filePicker.show();
                    filePicker.getSubmitButton().setText(R.string.sys_file_picker);
                    filePicker.getSubmitButton().setOnClickListener(view -> {
                        filePicker.dismiss();
                        selectFileSys();
                    });
                    return Unit.INSTANCE;
                })
                .request();
    }

    private void selectFileSys() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");//设置类型
        startActivityForResult(intent, requestImport);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case requestImport:
                if (data != null) {
                    mPresenter.importDataSLocal(FileUtils.getPath(this, data.getData()));
                }
                break;
        }
    }
}
