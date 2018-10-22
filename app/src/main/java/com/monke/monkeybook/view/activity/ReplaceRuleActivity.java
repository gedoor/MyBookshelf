package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.presenter.ReplaceRulePresenterImpl;
import com.monke.monkeybook.presenter.contract.ReplaceRuleContract;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.view.adapter.ReplaceRuleAdapter;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.qqtheme.framework.picker.FilePicker;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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
    RecyclerView recyclerViewBookSource;

    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private MoProgressHUD moProgressHUD;
    private ReplaceRuleAdapter adapter;
    private boolean selectAll = true;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, ReplaceRuleActivity.class));
    }

    @Override
    protected ReplaceRuleContract.Presenter initInjector() {
        return new ReplaceRulePresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_recycler_vew);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initRecyclerView();
        moProgressHUD = new MoProgressHUD(this);
    }

    @Override
    protected void initData() {

    }

    private void initRecyclerView() {
        recyclerViewBookSource.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplaceRuleAdapter(this);
        recyclerViewBookSource.setAdapter(adapter);
        adapter.resetDataS(ReplaceRuleManage.getAll());
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        itemTouchHelpCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchHelpCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBookSource);

    }

    public void editReplaceRule(ReplaceRuleBean replaceRuleBean) {
        moProgressHUD.showPutReplaceRule(replaceRuleBean, ruleBean -> {
            Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
                ReplaceRuleManage.saveData(ruleBean);
                e.onNext(ReplaceRuleManage.getAll());
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                        @Override
                        public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                            adapter.resetDataS(replaceRuleBeans);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        });
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (ReplaceRuleBean replaceRuleBean : adapter.getDataList()) {
            if (replaceRuleBean.getEnable() == null || !replaceRuleBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    private void selectAllDataS() {
        for (ReplaceRuleBean replaceRuleBean : adapter.getDataList()) {
            replaceRuleBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        ReplaceRuleManage.addDataS(adapter.getDataList());
    }

    public void delData(ReplaceRuleBean replaceRuleBean) {
        mPresenter.delData(replaceRuleBean);
    }

    public void saveDataS() {
        mPresenter.saveData(adapter.getDataList());
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
                moProgressHUD.showInputBox("输入替换规则网址", TextUtils.isEmpty(cacheUrl) ? getString(R.string.default_replace_url) : cacheUrl,
                        inputText -> {
                            ACache.get(this).put("replaceUrl", inputText);
                            mPresenter.importDataS(inputText);
                        });
                break;
            case R.id.action_del_all:
                mPresenter.delData(adapter.getDataList());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectReplaceRuleFile() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            FilePicker filePicker = new FilePicker(this, FilePicker.FILE);
            filePicker.setBackgroundColor(getResources().getColor(R.color.background));
            filePicker.setTopBackgroundColor(getResources().getColor(R.color.background));
            filePicker.setItemHeight(30);
            filePicker.setAllowExtensions(getResources().getStringArray(R.array.text_suffix));
            filePicker.setOnFilePickListener(s -> {
                mPresenter.importDataSLocal(s);
            });
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
        selectReplaceRuleFile();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
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
                    mPresenter.importDataSLocal(FileUtil.getPath(this, data.getData()));
                }
                break;
        }
    }

    @Override
    public void refresh() {
        adapter.resetDataS(ReplaceRuleManage.getAll());
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
