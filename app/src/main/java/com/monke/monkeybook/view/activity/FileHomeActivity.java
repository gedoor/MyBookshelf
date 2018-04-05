package com.monke.monkeybook.view.activity;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SelectedFiles;
import com.monke.monkeybook.presenter.BookSourcePresenterImpl;
import com.monke.monkeybook.presenter.impl.IBookSourcePresenter;
import com.monke.monkeybook.utils.fileselectorutil.SDCardScanner;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class FileHomeActivity extends MBaseActivity implements OnClickListener {
    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private FileHomeActivity instance = this;
    private List<String> externalPaths;//所有外置存储路径，包括手机自带SD卡以及可插拔TF卡路径
    private int REQUEST=1000;
    private Animation animIn;
    public static final int FILE_SOURCE_SELECTOR=106;

    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.rl_file_home_big)
    RelativeLayout rlBig;
    @BindView(R.id.rl_file_home_sd)
    RelativeLayout rlSD;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_file_home);
    }


    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        initSelectedFiles();
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();

        rlBig.setOnClickListener(this);
        rlSD.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            externalPaths = SDCardScanner.getStorageData(this);
            switch (view.getId()) {
                case R.id.rl_file_home_big:
                    if (externalPaths != null && externalPaths.size() > 0) {
                        FileFolderActivity.actionStart(instance, externalPaths.get(0), REQUEST);
                    } else {
                        Toast.makeText(instance, "大容量存储暂不可用！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.rl_file_home_sd:
                    if (externalPaths != null && externalPaths.size() > 1) {
                        FileFolderActivity.actionStart(instance, externalPaths.get(1), REQUEST);
                    } else {
                        Toast.makeText(instance, "SD卡暂不可用！", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        } else {
            EasyPermissions.requestPermissions(this, "导入书籍",
                    FILE_SOURCE_SELECTOR, perms);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST) {
                //因为所有选择的文件都放在了SelectedFiles全局变量中，所以此处data不会有任何数据
                JSONArray array = new JSONArray();
                for (File file : SelectedFiles.files.values()) {
                    array.put(file.getAbsolutePath());
                }
                Intent intent = new Intent();
                //intent.putExtra(resultTag, array.toString());
                setResult(Activity.RESULT_OK, intent);
                clearSelectedFiles();
                finish();
            }
        }
    }

    /**
     * 初始化全局变量SelectedFiles中的数据
     */
    private void initSelectedFiles() {
        SelectedFiles.files = new HashMap<String, File>();
        SelectedFiles.totalFileSize = 0;
    }

    /**
     * 清空全局变量SelectedFiles中的数据
     */
    private void clearSelectedFiles() {
        SelectedFiles.files = null;
        SelectedFiles.totalFileSize = 0;
    }



    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
    }

    @Override
    protected IBookSourcePresenter initInjector() {
        return new BookSourcePresenterImpl();
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_file_selector);
        }
    }


}

