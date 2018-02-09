package com.monke.monkeybook.view.impl;

import android.view.View;

import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookSourceBean;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public interface IBookSourceView extends IView {

    void refreshBookSource();

    View getView();
}
