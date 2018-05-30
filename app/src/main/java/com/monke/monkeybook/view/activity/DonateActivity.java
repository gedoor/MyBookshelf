package com.monke.monkeybook.view.activity;

import android.content.Intent;
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
        vwZfbHb.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW,
                "https://render.alipay.com/p/f/fd-j6lzqrgm/guiderofmklvtvw.html?shareId=2088002567472225&campStr=p1j%2BdzkZl018zOczaHT4Z5CLdPVCgrEXq89JsWOx1gdt05SIDMPg3PTxZbdPw9dL&sign=OqN%2FEToAWmYOBperSICGSOtBs36JILq1%2BshguP6l51U%3D&scene=offlinePaymentNewSns"));
        vwZfbRwm.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/zfbskrwm.jpg"));
        vwWxRwm.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/wxskrwm.jpg"));
        vwQqRwm.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/qqskrwm.jpg"));
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
