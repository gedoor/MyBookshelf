package com.monke.monkeybook.view.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.Donate;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GKF on 2018/1/13.
 * 捐赠页面
 */

public class DonateActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.vw_zfb_tz)
    CardView vwZfbTz;
    @BindView(R.id.vw_zfb_hb)
    CardView vwZfbHb;
    @BindView(R.id.vw_zfb_rwm)
    CardView vwZfbRwm;
    @BindView(R.id.vw_wx_rwm)
    CardView vwWxRwm;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.vw_qq_rwm)
    CardView vwQqRwm;
    @BindView(R.id.vw_zfb_hb_kl)
    CardView vwZfbHbKl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_donate);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void bindEvent() {
        vwZfbTz.setOnClickListener(view -> Donate.aliDonate(this));
        vwZfbHb.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/zfbhbrwm.png"));
        vwZfbRwm.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/zfbskrwm.jpg"));
        vwWxRwm.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/wxskrwm.jpg"));
        vwQqRwm.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/qqskrwm.jpg"));
        vwZfbHbKl.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, "537954522");
            if (clipboard != null) {
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(this, "红包码已复制\n 打开支付宝首页搜索“537954522” 立即领红包", Toast.LENGTH_SHORT).show();
            }
            try {
                PackageManager packageManager = this.getApplicationContext().getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "打开支付宝失败,请手动打开支付宝", Toast.LENGTH_SHORT).show();
            } finally {
                ACache.get(this).put("getZfbHb", "True", ACache.TIME_DAY);
            }
        });
    }

    void openIntent(String intentName, String address) {
        try {
            Intent intent = new Intent(intentName);
            intent.setData(Uri.parse(address));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.can_not_open, Toast.LENGTH_SHORT).show();
        }
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.donate);
        }
    }

    //菜单
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

}
