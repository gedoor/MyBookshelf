//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.presenter.ImportBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IImportBookPresenter;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.PremissionCheck;
import com.monke.monkeybook.view.adapter.ImportBookAdapter;
import com.monke.monkeybook.view.impl.IImportBookView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImportBookActivity extends MBaseActivity<IImportBookPresenter> implements IImportBookView {
    private final int RESULT_CHOOSE_DIRECTORY = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_scan)
    TextView tvScan;
    @BindView(R.id.rl_loading)
    RotateLoading rlLoading;
    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.rcv_books)
    RecyclerView rcvBooks;
    @BindView(R.id.tv_select_dir)
    TextView tvSelectDir;
    @BindView(R.id.ll_scan)
    LinearLayout llScan;

    private ImportBookAdapter importBookAdapter;
    private MenuItem menuItem;

    private MoProgressHUD moProgressHUD;

    @Override
    protected IImportBookPresenter initInjector() {
        return new ImportBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_import);
    }

    @Override
    protected void initData() {

        importBookAdapter = new ImportBookAdapter(count -> {
            if (menuItem != null) {
                menuItem.setVisible(count != 0);
            }
        });
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        moProgressHUD = new MoProgressHUD(this);

        rcvBooks.setAdapter(importBookAdapter);
        rcvBooks.setLayoutManager(new LinearLayoutManager(this));

        mPresenter.searchLocationBook(new File(getIntent().getStringExtra("path")));
        llScan.setVisibility(View.INVISIBLE);
        rlLoading.start();
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_local);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_import, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单状态
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItem = menu.getItem(0);
        menuItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_import_book:
                //添加书籍
                moProgressHUD.showLoading("放入书架中...");
                mPresenter.importBooks(importBookAdapter.getSelectDatas());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void bindEvent() {
        tvScan.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PremissionCheck.checkPremission(ImportBookActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //申请权限
                ImportBookActivity.this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0x11);
            } else {
                mPresenter.searchLocationBook(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
                llScan.setVisibility(View.INVISIBLE);
                rlLoading.start();
            }
        });
        tvSelectDir.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PremissionCheck.checkPremission(ImportBookActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //申请权限
                ImportBookActivity.this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0x11);
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    startActivityForResult(intent, RESULT_CHOOSE_DIRECTORY);
                } else {
                    Toast.makeText(this, "当前系统版本不支持", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void firstRequest() {

    }

    @Override
    public void finish() {
        if (moProgressHUD.isShow()) {
            moProgressHUD.dismiss();
        }
    }

    @Override
    public void addNewBook(File newFile) {
        importBookAdapter.addData(newFile);
        tvCount.setText(String.format(getString(R.string.import_books_count), String.valueOf(importBookAdapter.getItemCount())));
    }

    @Override
    public void searchFinish() {
        rlLoading.stop();
        rlLoading.setVisibility(View.INVISIBLE);
        importBookAdapter.setCanCheck(true);
    }

    @Override
    public void addSuccess() {
        moProgressHUD.dismiss();
        Toast.makeText(this, "添加书籍成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addError() {
        moProgressHUD.showInfo("放入书架失败!");
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0x11) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && PremissionCheck.checkPremission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mPresenter.searchLocationBook(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
                llScan.setVisibility(View.INVISIBLE);
                rlLoading.start();
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    moProgressHUD.showTwoButton("去系统设置打开SD卡读写权限？", "取消", v -> moProgressHUD.dismiss(), "设置", v -> {
                        moProgressHUD.dismiss();
                        PremissionCheck.requestPermissionSetting(ImportBookActivity.this);
                    });
                } else {
                    Toast.makeText(this, "未获取SD卡读取权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean a = moProgressHUD.onKeyDown(keyCode, event);
        return a || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) switch (requestCode) {
            case RESULT_CHOOSE_DIRECTORY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String path = FileUtil.getPath(this, docUri);
                    if (path != null) {
                        mPresenter.searchLocationBook(new File(path));
                        llScan.setVisibility(View.INVISIBLE);
                        rlLoading.start();
                    }
                }
                break;
        }
    }
}