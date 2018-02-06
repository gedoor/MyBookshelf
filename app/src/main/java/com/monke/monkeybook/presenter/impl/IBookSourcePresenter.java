package com.monke.monkeybook.presenter.impl;

import android.net.Uri;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.bean.BookSourceBean;

import java.util.List;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public interface IBookSourcePresenter extends IPresenter {

    void saveDate(List<BookSourceBean> bookSourceBeans);

    void delDate(BookSourceBean bookSourceBean);

    void importBookSource(Uri uri);
}
