package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.monke.basemvplib.IPresenter;
import com.monke.basemvplib.impl.BaseActivity;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;

import java.text.Format;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Created by GKF on 2017/12/15.
 * 关于
 */

public class AboutActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.vw_about)
    ViewGroup vwAbout;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    private Animation animIn;
    private Animation animOut;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.icon_monkovel_big_black)
                .setDescription(getString(R.string.about_description))
                .addEmail("kunfei.ge@gmail.com")
                .addWebsite("https://github.com/gedoor/MONKOVEL")
                .addGitHub("gedoor")
                .addItem(versionElement())
                .create();
        vwAbout.addView(aboutPage);
    }

    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);
    }

    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
    }

    private Element versionElement() {
        Element versionElement = new Element();
        String version = MApplication.getVersionName();
        versionElement.setTitle(String.format(getString(R.string.version_name), version));
        versionElement.setIconDrawable(R.drawable.ic_turned_in_not_black_24dp);
        return versionElement;
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
