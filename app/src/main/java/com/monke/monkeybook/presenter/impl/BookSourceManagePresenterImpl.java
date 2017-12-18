package com.monke.monkeybook.presenter.impl;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.impl.BasePresenterImpl;
import com.monke.monkeybook.presenter.IBookSourceManagePresenter;
import com.monke.monkeybook.view.IBookSourceManageView;

/**
 * Created by GKF on 2017/12/18.
 */

public class BookSourceManagePresenterImpl extends BasePresenterImpl<IBookSourceManageView> implements IBookSourceManagePresenter {

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }
}
