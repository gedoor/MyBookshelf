package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.presenter.ReplaceRulePresenter;
import com.kunfei.bookshelf.presenter.contract.ReplaceRuleContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.ReplaceRuleAdapter;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.qqtheme.framework.picker.FilePicker;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

    private MoDialogHUD moDialogHUD;
    private ReplaceRuleAdapter adapter;
    private boolean selectAll = true;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, ReplaceRuleActivity.class));
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
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initRecyclerView();
        moDialogHUD = new MoDialogHUD(this);
    }

    @Override
    protected void initData() {

    }

    private void initRecyclerView() {
        recyclerViewBookSource.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplaceRuleAdapter(this);
        recyclerViewBookSource.setAdapter(adapter);
        adapter.resetDataS(ReplaceRuleManager.getAll());
        ItemTouchCallback itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBookSource);

    }

    public void editReplaceRule(ReplaceRuleBean replaceRuleBean) {
        moDialogHUD.showPutReplaceRule(replaceRuleBean, ruleBean -> {
            Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
                ReplaceRuleManager.saveData(ruleBean);
                e.onNext(ReplaceRuleManager.getAll());
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
        ReplaceRuleManager.addDataS(adapter.getDataList());
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
                moDialogHUD.showInputBox(getString(R.string.input_replace_url),
                        cacheUrl,
                        new String[]{cacheUrl},
                        inputText -> {
                            inputText = StringUtils.trim(inputText);
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
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                FilePicker filePicker = new FilePicker(ReplaceRuleActivity.this, FilePicker.FILE);
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
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                ReplaceRuleActivity.this.toast(R.string.import_book_source);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                PermissionUtils.requestMorePermissions(ReplaceRuleActivity.this, MApplication.PerList, MApplication.RESULT__PERMS);
            }
        });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                selectReplaceRuleFile();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                ReplaceRuleActivity.this.toast(R.string.import_book_source);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                ReplaceRuleActivity.this.toast(R.string.import_book_source);
                PermissionUtils.toAppSetting(ReplaceRuleActivity.this);
            }
        });
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
        adapter.resetDataS(ReplaceRuleManager.getAll());
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
