package com.kunfei.basemvplib.impl;


import androidx.annotation.NonNull;

public interface IPresenter {
    /**
     * 注入View，使之能够与View相互响应
     */
    void attachView(@NonNull IView iView);

    /**
     * 释放资源，如果使用了网络请求 可以在此执行IModel.cancelRequest()
     */
    void detachView();
}
