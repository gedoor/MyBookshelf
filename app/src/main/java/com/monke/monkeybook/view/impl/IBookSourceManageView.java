package com.monke.monkeybook.view.impl;

import android.view.View;
import android.widget.LinearLayout;

import com.monke.basemvplib.impl.IView;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public interface IBookSourceManageView extends IView {

    void refreshBookSource();

    View getView();
}
