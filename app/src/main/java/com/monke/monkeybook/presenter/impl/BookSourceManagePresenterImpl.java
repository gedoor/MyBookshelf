package com.monke.monkeybook.presenter.impl;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.impl.BasePresenterImpl;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.presenter.IBookSourceManagePresenter;
import com.monke.monkeybook.view.IBookSourceManageView;

import java.util.List;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public class BookSourceManagePresenterImpl extends BasePresenterImpl<IBookSourceManageView> implements IBookSourceManagePresenter {

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void saveDate(List<BookSourceBean> bookSourceBeans) {
        int i = 0;

    }
}
