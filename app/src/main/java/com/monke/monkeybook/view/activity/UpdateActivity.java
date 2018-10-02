package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.UpdateInfoBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.UpdateManager;
import com.monke.monkeybook.service.UpdateService;
import com.zzhoujay.richtext.RichText;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateActivity extends MBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_markdown)
    TextView tvMarkdown;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    private UpdateInfoBean updateInfo;
    private MenuItem menuItemDownload;

    public static void startThis(Context context, UpdateInfoBean updateInfoBean) {
        Intent intent = new Intent(context, UpdateActivity.class);
        intent.putExtra("updateInfo", updateInfoBean);
        context.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_update);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    @Override
    protected void initData() {
        updateInfo = getIntent().getParcelableExtra("updateInfo");
        RichText.fromMarkdown(updateInfo.getDetail()).into(tvMarkdown);
        tvMarkdown.setBackgroundColor(Color.WHITE);
        tvMarkdown.setTextColor(Color.BLACK);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.new_version);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItemDownload = menu.findItem(R.id.action_download);
        upMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_download:
                UpdateService.startThis(this, updateInfo);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void upMenu() {
        if (updateInfo != null && menuItemDownload != null) {
            File apkFile = new File(UpdateManager.getSavePath(updateInfo.getUrl().substring(updateInfo.getUrl().lastIndexOf("/"))));
            if (UpdateService.isRuning) {
                menuItemDownload.setTitle("取消下载");
            } else if (apkFile.exists()) {
                menuItemDownload.setTitle("重新下载");
            } else {
                menuItemDownload.setTitle("下载更新");
            }
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,tags = {@Tag(RxBusTag.UPDATE_APK_STATE)})
    public void updateState(Integer state) {
        upMenu();
    }
}
