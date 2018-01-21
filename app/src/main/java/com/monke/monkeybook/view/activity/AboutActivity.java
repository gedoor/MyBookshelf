package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.help.Donate;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GKF on 2017/12/15.
 * 关于
 */

public class AboutActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.vw_version)
    CardView vwVersion;
    @BindView(R.id.tv_donate)
    TextView tvDonate;
    @BindView(R.id.vw_donate)
    CardView vwDonate;
    @BindView(R.id.tv_scoring)
    TextView tvScoring;
    @BindView(R.id.vw_scoring)
    CardView vwScoring;
    @BindView(R.id.tv_git)
    TextView tvGit;
    @BindView(R.id.vw_git)
    CardView vwGit;
    @BindView(R.id.tv_disclaimer)
    TextView tvDisclaimer;
    @BindView(R.id.vw_disclaimer)
    CardView vwDisclaimer;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_mail)
    TextView tvMail;
    @BindView(R.id.vw_mail)
    CardView vwMail;

    private Animation animIn;
    private Animation animOut;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        tvVersion.setText(String.format(getString(R.string.version_name), MApplication.getVersionName()));
    }

    @Override
    protected void bindEvent() {
        vwDonate.setOnClickListener(view -> {
            Donate.aliDonate(this);
        });
        vwScoring.setOnClickListener(view -> {
            openIntent(Intent.ACTION_VIEW, "market://details?id=" + getPackageName());
        });
        vwMail.setOnClickListener(view -> {
            openIntent(Intent.ACTION_SENDTO, "mailto:kunfei.ge@gmail.com");
        });
        vwGit.setOnClickListener(view -> {
            openIntent(Intent.ACTION_VIEW, "https://github.com/gedoor/MONKOVEL");
        });
        vwDisclaimer.setOnClickListener(view -> {
            openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MONKOVEL/disclaimer.html");
        });
    }

    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
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
            actionBar.setTitle(R.string.about);
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
