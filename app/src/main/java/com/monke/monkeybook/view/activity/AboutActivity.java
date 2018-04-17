package com.monke.monkeybook.view.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
    @BindView(R.id.tv_source_rule)
    TextView tvSourceRule;
    @BindView(R.id.vw_source_rule)
    CardView vwSourceRule;
    @BindView(R.id.tv_update)
    TextView tvUpdate;
    @BindView(R.id.vw_update)
    CardView vwUpdate;
    @BindView(R.id.tv_qq)
    TextView tvQq;
    @BindView(R.id.vw_qq)
    CardView vwQq;
    @BindView(R.id.vw_source_share)
    CardView vwSourceShare;
    @BindView(R.id.tv_app_summary)
    TextView tvAppSummary;
    @BindView(R.id.tv_source_share)
    TextView tvSourceShare;

    private Animation animIn;

    private String qq = "701903217";

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
        Animation animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        tvVersion.setText(String.format(getString(R.string.version_name), MApplication.getVersionName()));
        tvQq.setText(String.format("QQ讨论群:%s", qq));

        tvAppSummary.getCompoundDrawablesRelative()[1].mutate();
        tvAppSummary.getCompoundDrawablesRelative()[1].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvDonate.getCompoundDrawablesRelative()[0].mutate();
        tvDonate.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvGit.getCompoundDrawablesRelative()[0].mutate();
        tvGit.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvMail.getCompoundDrawablesRelative()[0].mutate();
        tvMail.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvQq.getCompoundDrawablesRelative()[0].mutate();
        tvQq.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvScoring.getCompoundDrawablesRelative()[0].mutate();
        tvScoring.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvSourceRule.getCompoundDrawablesRelative()[0].mutate();
        tvSourceRule.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvDisclaimer.getCompoundDrawablesRelative()[0].mutate();
        tvDisclaimer.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvUpdate.getCompoundDrawablesRelative()[0].mutate();
        tvUpdate.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvVersion.getCompoundDrawablesRelative()[0].mutate();
        tvVersion.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);

        tvSourceShare.getCompoundDrawablesRelative()[0].mutate();
        tvSourceShare.getCompoundDrawablesRelative()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    protected void bindEvent() {
        vwDonate.setOnClickListener(view -> {
            Intent intent = new Intent(this, DonateActivity.class);
            startActivity(intent);
        });
        vwScoring.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "market://details?id=" + getPackageName()));
        vwMail.setOnClickListener(view -> openIntent(Intent.ACTION_SENDTO, "mailto:kunfei.ge@gmail.com"));
        vwGit.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://github.com/gedoor/MyBookshelf"));
        vwSourceRule.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/sourcerule.html"));
        vwDisclaimer.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://gedoor.github.io/MyBookshelf/disclaimer.html"));
        vwUpdate.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://github.com/gedoor/MyBookshelf/releases"));
        vwSourceShare.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "https://github.com/gedoor/MyBookshelf/tree/master/docs/book_source"));
        tvQq.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, qq);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(this, "已拷贝", Toast.LENGTH_SHORT).show();
            }
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
