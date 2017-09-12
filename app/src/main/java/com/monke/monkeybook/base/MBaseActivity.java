//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.base;

import com.monke.basemvplib.IPresenter;
import com.monke.basemvplib.impl.BaseActivity;
import com.umeng.analytics.MobclickAgent;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T>{
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
