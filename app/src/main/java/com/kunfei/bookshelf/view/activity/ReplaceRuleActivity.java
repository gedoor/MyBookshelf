package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.help.permission.Permissions;
import com.kunfei.bookshelf.help.permission.PermissionsCompat;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.presenter.ReplaceRulePresenter;
import com.kunfei.bookshelf.presenter.contract.ReplaceRuleContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.ReplaceRuleAdapter;
import com.kunfei.bookshelf.widget.filepicker.picker.FilePicker;
import com.kunfei.bookshelf.widget.modialog.InputDialog;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;
import com.kunfei.bookshelf.widget.modialog.ReplaceRuleDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Unit;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class ReplaceRuleActivity extends MBaseActivity<ReplaceRuleContract.Presenter> implements ReplaceRuleContract.View {
    private final int IMPORT_SOURCE = 102;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BookShelfBean bookShelfBean;
    private MoDialogHUD moDialogHUD;
    private ReplaceRuleAdapter adapter;
    private boolean selectAll = true;

    public static void startThis(Context context, BookShelfBean shelfBean) {
        String key = String.valueOf(System.currentTimeMillis());
        Intent intent = new Intent(context, ReplaceRuleActivity.class);
        BitIntentDataManager.getInstance().putData(key, shelfBean);
        intent.putExtra("data_key", key);
        context.startActivity(intent);
    }

    @Override
    protected ReplaceRuleContract.Presenter initInjector() {
        return new ReplaceRulePresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_recycler_vew);
    }

    @Override
    protected void initData() {
        String dataKey = getIntent().getStringExtra("data_key");
        if (!TextUtils.isEmpty(dataKey)) {
            bookShelfBean = (BookShelfBean) BitIntentDataManager.getInstance().getData(dataKey);
        }
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initRecyclerView();
        moDialogHUD = new MoDialogHUD(this);
        refresh();
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        adapter = new ReplaceRuleAdapter(this);
        recyclerView.setAdapter(adapter);
        ItemTouchCallback itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void editReplaceRule(ReplaceRuleBean replaceRuleBean) {
        ReplaceRuleDialog.builder(this, replaceRuleBean, bookShelfBean)
                .setPositiveButton(replaceRuleBean1 ->
                        ReplaceRuleManager.saveData(replaceRuleBean1)
                                .subscribe(new MySingleObserver<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        refresh();
                                    }
                                })).show();
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (ReplaceRuleBean replaceRuleBean : adapter.getData()) {
            if (replaceRuleBean.getEnable() == null || !replaceRuleBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    private void selectAllDataS() {
        for (ReplaceRuleBean replaceRuleBean : adapter.getData()) {
            replaceRuleBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        ReplaceRuleManager.addDataS(adapter.getData());
    }

    public void delData(ReplaceRuleBean replaceRuleBean) {
        mPresenter.delData(replaceRuleBean);
    }

    public void saveDataS() {
        mPresenter.saveData(adapter.getData());
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.replace_rule_title);
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
                editReplaceRule(null);
                break;
            case R.id.action_select_all:
                selectAllDataS();
                break;
            case R.id.action_import:
                selectReplaceRuleFile();
                break;
            case R.id.action_import_onLine:
                String cacheUrl = ACache.get(this).getAsString("replaceUrl");
                String[] cacheUrls = cacheUrl == null ? new String[]{} : cacheUrl.split(";");
                List<String> urlList = new ArrayList<>(Arrays.asList(cacheUrls));
                InputDialog.builder(this)
                        .setTitle(getString(R.string.input_replace_url))
                        .setDefaultValue(cacheUrl)
                        .setAdapterValues(urlList)
                        .setCallback(new InputDialog.Callback() {
                            @Override
                            public void setInputText(String inputText) {
                                inputText = StringUtils.trim(inputText);
                                if (!urlList.contains(inputText)) {
                                    urlList.add(0, inputText);
                                    ACache.get(ReplaceRuleActivity.this).put("replaceUrl", TextUtils.join(";", urlList));
                                }
                                mPresenter.importDataS(inputText);
                            }

                            @Override
                            public void delete(String value) {

                            }
                        }).show();
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

    private void selectReplaceRuleFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.get_storage_per)
                .onGranted((requestCode) -> {
                    FilePicker filePicker = new FilePicker(ReplaceRuleActivity.this, FilePicker.FILE);
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
        startActivityForResult(intent, IMPORT_SOURCE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMPORT_SOURCE:
                if (data != null) {
                    mPresenter.importDataSLocal(FileUtils.getPath(this, data.getData()));
                }
                break;
        }
    }

    @Override
    public void refresh() {
        ReplaceRuleManager.getAll()
                .subscribe(new MySingleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onSuccess(List<ReplaceRuleBean> replaceRuleBeans) {
                        adapter.resetDataS(replaceRuleBeans);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        RxBus.get().post(RxBusTag.UPDATE_READ, false);
        super.onDestroy();
    }

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(llContent, msg, length);
    }
}
