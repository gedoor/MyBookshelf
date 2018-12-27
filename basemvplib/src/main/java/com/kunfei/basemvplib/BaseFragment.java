package com.kunfei.basemvplib;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment<T extends IPresenter> extends Fragment implements IView {
    protected View view;
    protected Bundle savedInstanceState;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        initSDK();
        view = createView(inflater, container);
        initData();
        bindView();
        bindEvent();
        firstRequest();
        return view;
    }

    /**
     * 事件触发绑定
     */
    protected void bindEvent() {

    }

    /**
     * 控件绑定
     */
    protected void bindView() {

    }

    /**
     * 数据初始化
     */
    protected void initData() {

    }

    /**
     * 首次逻辑操作
     */
    protected void firstRequest() {

    }

    /**
     * 加载布局
     */
    protected abstract View createView(LayoutInflater inflater, ViewGroup container);

    /**
     * 第三方SDK初始化
     */
    protected void initSDK() {

    }
}
