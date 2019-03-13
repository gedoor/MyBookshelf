package com.kunfei.bookshelf.base;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kunfei.basemvplib.BaseFragment;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.MApplication;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class MBaseFragment<T extends IPresenter> extends BaseFragment<T> implements IView {
    public final static String start_share_ele = "start_with_share_ele";
    public final SharedPreferences preferences = MApplication.getConfigPreferences();
    protected T mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mPresenter = initInjector();
        attachView();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(createLayoutId(), container, false);
    }

    protected void startActivityByAnim(Intent intent, int animIn, int animExit) {
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(animIn, animExit);
    }

    protected void startActivityByAnim(Intent intent, @NonNull View view, @NonNull String transitionName, int animIn, int animExit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.putExtra(start_share_ele, true);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, transitionName);
            startActivity(intent, options.toBundle());
        } else {
            startActivityByAnim(intent, animIn, animExit);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachView();
    }


    public abstract int createLayoutId();

    /**
     * P层绑定   若无则返回null;
     */
    protected abstract T initInjector();

    /**
     * P层绑定V层
     */
    private void attachView() {
        if (null != mPresenter) {
            mPresenter.attachView(this);
        }
    }

    /**
     * P层解绑V层
     */
    private void detachView() {
        if (null != mPresenter) {
            mPresenter.detachView();
        }
    }

    public void toast(String msg) {
        Toast.makeText(this.getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public void toast(int id) {
        Toast.makeText(this.getActivity(), getString(id), Toast.LENGTH_SHORT).show();
    }
}
