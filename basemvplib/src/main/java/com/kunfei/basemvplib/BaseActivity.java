package com.kunfei.basemvplib;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.monke.basemvplib.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity<T extends IPresenter> extends AppCompatActivity implements IView {
    public final static String start_share_ele= "start_with_share_ele";
    public static final int SUCCESS = 1;
    public static final int ERROR = -1;
    protected Bundle savedInstanceState;
    protected T mPresenter;
    protected boolean isRecreate;
    private Boolean startShareAnim = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        if(getIntent()!=null){
            isRecreate = getIntent().getBooleanExtra("isRecreate", false);
            startShareAnim = getIntent().getBooleanExtra(start_share_ele,false);
        }
        AppActivityManager.getInstance().add(this);
        initSDK();
        onCreateActivity();
        mPresenter = initInjector();
        attachView();
        initData();
        bindView();
        bindEvent();
        firstRequest();
    }

    /**
     * 首次逻辑操作
     */
    protected void firstRequest() {

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

    /**
     * SDK初始化
     */
    protected void initSDK() {

    }

    /**
     * P层绑定   若无则返回null;
     */
    protected abstract T initInjector();

    /**
     * 布局载入  setContentView()
     */
    protected abstract void onCreateActivity();

    /**
     * 数据初始化
     */
    protected abstract void initData();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachView();
        AppActivityManager.getInstance().remove(this);
    }

    @Override
    public void recreate() {
        getIntent().putExtra("isRecreate", true);
        super.recreate();
    }

    /////////Toast//////////////////

    public void toast(String msg) {
        toast(msg, Toast.LENGTH_SHORT, 0);
    }

    public void toast(String msg, int state) {
        toast(msg, Toast.LENGTH_LONG, state);
    }

    public void toast(int strId) {
        toast(strId, 0);
    }

    public void toast(int strId, int state) {
        toast(getString(strId), Toast.LENGTH_LONG, state);
    }

    public void toast(String msg, int length, int state) {
        Toast toast = Toast.makeText(this, msg, length);
        try {
            if (state == SUCCESS) {
                toast.getView().getBackground().setColorFilter(getResources().getColor(R.color.success), PorterDuff.Mode.SRC_IN);
            } else if (state == ERROR) {
                toast.getView().getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_IN);
            }
        } catch (Exception ignored) {
        }
        toast.show();
    }

    ////////////////////////////////启动Activity转场动画/////////////////////////////////////////////

    protected void startActivityForResultByAnim(Intent intent, int requestCode, int animIn, int animExit) {
        startActivityForResult(intent, requestCode);
        overridePendingTransition(animIn, animExit);
    }

    protected void startActivityByAnim(Intent intent, int animIn, int animExit) {
        startActivity(intent);
        overridePendingTransition(animIn, animExit);
    }

    protected void startActivityByAnim(Intent intent, @NonNull View view, @NonNull String transitionName, int animIn, int animExit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.putExtra(start_share_ele,true);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, view, transitionName);
            startActivity(intent, options.toBundle());
        } else {
            startActivityByAnim(intent, animIn, animExit);
        }
    }

    public Context getContext(){
        return this;
    }

    public Boolean getStart_share_ele() {
        return startShareAnim;
    }
}