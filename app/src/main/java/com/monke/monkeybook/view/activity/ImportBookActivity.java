//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.presenter.IImportBookPresenter;
import com.monke.monkeybook.presenter.impl.ImportBookPresenterImpl;
import com.monke.monkeybook.utils.PremissionCheck;
import com.monke.monkeybook.view.IImportBookView;
import com.monke.monkeybook.view.adapter.ImportBookAdapter;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;
import com.victor.loading.rotate.RotateLoading;
import java.io.File;

public class ImportBookActivity extends MBaseActivity<IImportBookPresenter> implements IImportBookView {
    private LinearLayout llContent;
    private ImageButton ivReturn;
    private TextView tvScan;

    private RotateLoading rlLoading;
    private TextView tvCount;

    private TextView tvAddshelf;

    private RecyclerView rcvBooks;

    private ImportBookAdapter importBookAdapter;

    private Animation animIn;
    private Animation animOut;

    private MoProgressHUD moProgressHUD;
    @Override
    protected IImportBookPresenter initInjector() {
        return new ImportBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_importbook);
    }

    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);

        importBookAdapter = new ImportBookAdapter(count -> tvAddshelf.setVisibility(count == 0 ? View.INVISIBLE : View.VISIBLE));
    }

    @Override
    protected void bindView() {
        moProgressHUD = new MoProgressHUD(this);

        llContent = (LinearLayout) findViewById(R.id.ll_content);
        ivReturn = (ImageButton) findViewById(R.id.iv_return);
        tvScan = (TextView) findViewById(R.id.tv_scan);

        rlLoading = (RotateLoading) findViewById(R.id.rl_loading);
        tvCount = (TextView) findViewById(R.id.tv_count);

        tvAddshelf = (TextView) findViewById(R.id.tv_addshelf);

        rcvBooks = (RecyclerView) findViewById(R.id.rcv_books);
        rcvBooks.setAdapter(importBookAdapter);
        rcvBooks.setLayoutManager(new LinearLayoutManager(this));
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
                mPresenter.searchLocationBook();
                tvScan.setVisibility(View.INVISIBLE);
                rlLoading.start();
            }
        });
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ImportBookActivity.super.finish();
                overridePendingTransition(0, 0);
                isExiting = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ivReturn.setOnClickListener(v -> finish());

        tvAddshelf.setOnClickListener(v -> {
            //添加书籍
            moProgressHUD.showLoading("放入书架中...");
            mPresenter.importBooks(importBookAdapter.getSelectDatas());
        });
    }

    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
    }

    private Boolean isExiting = false;

    @Override
    public void finish() {
        if (!isExiting) {
            if(moProgressHUD.isShow()){
                moProgressHUD.dismiss();
            }
            isExiting = true;
            llContent.startAnimation(animOut);
        }
    }

    @Override
    public void addNewBook(File newFile) {
        importBookAdapter.addData(newFile);
        tvCount.setText(String.format(getString(R.string.tv_importbook_count), String.valueOf(importBookAdapter.getItemCount())));
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
        Toast.makeText(this,"添加书籍成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addError() {
        moProgressHUD.showInfo("放入书架失败!");
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 0x11){
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && PremissionCheck.checkPremission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mPresenter.searchLocationBook();
                tvScan.setVisibility(View.INVISIBLE);
                rlLoading.start();
            }else{
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    moProgressHUD.showTwoButton("去系统设置打开SD卡读写权限？", "取消", v -> moProgressHUD.dismiss(), "设置", v -> {
                        moProgressHUD.dismiss();
                        PremissionCheck.requestPermissionSetting(ImportBookActivity.this);
                    });
                }else{
                    Toast.makeText(this, "未获取SD卡读取权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean a = moProgressHUD.onKeyDown(keyCode,event);
        if(a)
            return a;
        return super.onKeyDown(keyCode, event);
    }
}