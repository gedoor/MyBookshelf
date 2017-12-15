package com.monke.monkeybook.view.impl;

import android.os.Bundle;
import android.view.View;

import com.monke.basemvplib.IPresenter;
import com.monke.basemvplib.impl.BaseActivity;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;

import java.text.Format;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Created by GKF on 2017/12/15.
 * 关于
 */

public class AboutActivity extends MBaseActivity {
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
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.icon_monkovel_big_black)
                .setDescription(getString(R.string.about_description))
                .addEmail("kunfei.ge@gmail.com")
                .addWebsite("https://github.com/gedoor/MONKOVEL")
                .addGitHub("gedoor")
                .addItem(versionElement())
                .create();
        setContentView(aboutPage);
    }

    @Override
    protected void initData() {

    }

    private Element versionElement() {
        Element versionElement = new Element();
        String version = MApplication.getVersionName();
        versionElement.setTitle(String.format(getString(R.string.version_name), version));
        versionElement.setIconDrawable(R.drawable.ic_turned_in_not_black_24dp);
        return versionElement;
    }

}
