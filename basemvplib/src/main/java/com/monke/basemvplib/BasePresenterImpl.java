package com.monke.basemvplib;

import android.support.annotation.NonNull;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;

public abstract class BasePresenterImpl<T extends IView> implements IPresenter {
    protected T mView;

    @Override
    public void attachView(@NonNull IView iView) {
        mView = (T) iView;
    }
}
