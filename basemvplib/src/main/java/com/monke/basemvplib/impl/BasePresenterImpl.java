package com.monke.basemvplib.impl;

import android.support.annotation.NonNull;
import com.monke.basemvplib.IPresenter;
import com.monke.basemvplib.IView;

public abstract class BasePresenterImpl<T extends IView> implements IPresenter{
    protected T mView;

    @Override
    public void attachView(@NonNull IView iView) {
        mView = (T) iView;
    }
}
