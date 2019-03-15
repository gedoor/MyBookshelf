package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.UpdateInfoBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.UpdateManager;
import com.kunfei.bookshelf.service.UpdateService;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import java.io.File;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.noties.markwon.Markwon;

public class UpdateActivity extends MBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_markdown)
    TextView tvMarkdown;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_download_progress)
    TextView tvDownloadProgress;
    @BindView(R.id.ll_download)
    LinearLayout llDownload;
    @BindView(R.id.pb_download)
    ProgressBar progressBar;
    @BindView(R.id.tv_install_update)
    TextView tvInstallUpdate;

    private UpdateInfoBean updateInfo;
    private MenuItem menuItemDownload;

    public static void startThis(Context context, UpdateInfoBean updateInfoBean) {
        Intent intent = new Intent(context, UpdateActivity.class);
        intent.putExtra("updateInfo", updateInfoBean);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_update);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    @Override
    protected void initData() {
        updateInfo = getIntent().getParcelableExtra("updateInfo");
        if (updateInfo != null) {
            Markwon.setMarkdown(tvMarkdown, updateInfo.getDetail());
        }
    }

    /**
     * 控件绑定
     */
    @Override
    protected void bindView() {
        super.bindView();
        tvInstallUpdate.setOnClickListener(view -> {
            if (updateInfo != null) {
                String url = updateInfo.getUrl();
                String fileName = url.substring(url.lastIndexOf("/"));
                File apkFile = new File(UpdateManager.getSavePath(fileName));
                UpdateManager.getInstance(this).installApk(apkFile);
            } else {
                Toast.makeText(this, R.string.non_update_url, Toast.LENGTH_SHORT).show();
            }
        });
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
                if (UpdateService.isRunning) {
                    UpdateService.stopThis(this);
                } else {
                    tvDownloadProgress.setText(getString(R.string.progress_show, 0, 100));
                    UpdateService.startThis(this, updateInfo);
                }
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
            if (UpdateService.isRunning) {
                menuItemDownload.setTitle("取消下载");
                llDownload.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                tvInstallUpdate.setVisibility(View.GONE);
            } else if (apkFile.exists()) {
                menuItemDownload.setTitle("重新下载");
                llDownload.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                tvInstallUpdate.setVisibility(View.VISIBLE);
            } else {
                menuItemDownload.setTitle("下载更新");
                llDownload.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                tvInstallUpdate.setVisibility(View.GONE);
            }
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UPDATE_APK_STATE)})
    public void updateState(Integer state) {
        upMenu();
        if (state > 0) {
            progressBar.setProgress(state);
            tvDownloadProgress.setText(getString(R.string.progress_show, state, 100));
        }
    }
}
