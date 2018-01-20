package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 对话框
 */
public class MoProgressHUD {
    private Boolean isFinishing = false;

    private Context context;
    private ViewGroup decorView;//activity的根View
    private ViewGroup rootView;// mSharedView 的 根View
    private MoProgressView mSharedView;


    private Animation inAnim;
    private Animation outAnim;

    private Boolean canBack = false;

    public MoProgressHUD(Context context) {
        this.context = context;
        initViews();
        initCenter();
        initAnimation();
    }

    private void initAnimation() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }

    private void initFromTopRight() {
        inAnim = AnimationUtils.loadAnimation(context, R.anim.moprogress_in_top_right);
        outAnim = AnimationUtils.loadAnimation(context, R.anim.moprogress_out_top_right);
    }

    private void initFromBottomRight() {
        inAnim = AnimationUtils.loadAnimation(context, R.anim.moprogress_in_bottom_right);
        outAnim = AnimationUtils.loadAnimation(context, R.anim.moprogress_out_bottom_right);
    }

    private void initFromBottomAnimation() {
        inAnim = AnimationUtils.loadAnimation(context, R.anim.moprogress_bottom_in);
        outAnim = AnimationUtils.loadAnimation(context, R.anim.moprogress_bottom_out);
    }

    private void initCenter() {
        mSharedView.setGravity(Gravity.CENTER);
        if (mSharedView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSharedView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMargins(0, 0, 0, 0);
                mSharedView.setLayoutParams(layoutParams);
            }
            mSharedView.setPadding(0, 0, 0, 0);
        }
    }

    private void initBottom() {
        mSharedView.setGravity(Gravity.BOTTOM);
        if (mSharedView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSharedView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMargins(0, 0, 0, 0);
                mSharedView.setLayoutParams(layoutParams);
            }
            mSharedView.setPadding(0, 0, 0, 0);
        }
    }

    private void initMarRightTop() {
        mSharedView.setGravity(Gravity.RIGHT | Gravity.TOP);
        if (mSharedView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSharedView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMargins(0, 0, 0, 0);
                mSharedView.setLayoutParams(layoutParams);
            }
            mSharedView.setPadding(0, 0, 0, 0);
        }
    }

    public void initViews() {
        decorView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = new FrameLayout(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootView.setLayoutParams(layoutParams);
        rootView.setClickable(true);
        rootView.setBackgroundColor(context.getResources().getColor(R.color.btn_bg_press_tp));

        mSharedView = new MoProgressView(context);

    }

    public Animation getInAnimation() {
        return AnimationUtils.loadAnimation(context, R.anim.moprogress_in);
    }

    public Animation getOutAnimation() {
        return AnimationUtils.loadAnimation(context, R.anim.moprogress_out);
    }

    public boolean isShowing() {
        return rootView.getParent() != null;
    }

    private void onAttached() {
        decorView.addView(rootView);
        if (mSharedView.getParent() != null)
            ((ViewGroup) mSharedView.getParent()).removeView(mSharedView);
        rootView.addView(mSharedView);

        isFinishing = false;
    }

    public void dismiss() {
        //消失动画
        if (mSharedView != null && rootView != null && mSharedView.getParent() != null) {
            hideIMM(rootView);
            if (!isFinishing) {
                new Handler().post(() -> {
                    outAnim.setAnimationListener(outAnimListener);
                    mSharedView.getChildAt(0).startAnimation(outAnim);
                });
            }
        }
    }

    public Boolean isShow() {
        return (mSharedView != null && mSharedView.getParent() != null);
    }

    private Animation.AnimationListener outAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isFinishing = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            dismissImmediately();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private void dismissImmediately() {
        if (mSharedView != null && rootView != null && mSharedView.getParent() != null) {
            new Handler().post(() -> {
                rootView.removeView(mSharedView);
                decorView.removeView(rootView);
            });
        }
        isFinishing = false;
    }

    //隐藏输入法
    private void hideIMM(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //转圈的载入
    public void showLoading() {
        showLoading(null);
    }

    //同上
    public void showLoading(String msg) {
        initCenter();
        initAnimation();
        canBack = false;
        rootView.setBackgroundColor(Color.parseColor("#00000000"));
        rootView.setOnClickListener(null);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.showLoading(msg);
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    //单个按钮的提示信息
    public void showInfo(String msg) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setBackgroundColor(Color.parseColor("#00000000"));
        rootView.setOnClickListener(null);
        mSharedView.showInfo(msg, v -> dismiss());
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    //单个按钮的提示信息
    public void showInfo(String msg, String btnText, View.OnClickListener listener) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setBackgroundColor(Color.parseColor("#CC000000"));
        rootView.setOnClickListener(null);
        mSharedView.showInfo(msg, btnText, listener);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    //////////////////////两个不同等级的按钮//////////////////////
    public void showTwoButton(String msg, String b_f, View.OnClickListener c_f, String b_s, View.OnClickListener c_s) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setBackgroundColor(Color.parseColor("#CC000000"));
        rootView.setOnClickListener(null);
        mSharedView.showTwoButton(msg, b_f, c_f, b_s, c_s);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    ////////////////////离线章节选择////////////////////////////
    public interface OnClickDownload {
        void download(int start, int end);
    }

    public void showDownloadList(int startIndex, int endIndex, int all, OnClickDownload clickDownload) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        DownLoadView.getInstance(mSharedView)
                .showDownloadList(startIndex, endIndex, all, clickDownload, v -> dismiss());
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }
    //////////////////////////////////////////////////////////

    ////////////////////换源////////////////////////////
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }

    public void showChangeSource(BookShelfBean bookShelf, OnClickSource clickSource) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        ChangeSourceView.getInstance(mSharedView)
                .showChangeSource(bookShelf, clickSource, this);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }
    //////////////////////////////////////////////////////////

    ////////////////////添加书籍地址////////////////////////////
    public interface OnPutBookUrl {
        void addBookUrl(String bookUrl);
    }

    public void showPutBookUrl(OnPutBookUrl onPutBookUrl) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        EditBookUrlView.getInstance(mSharedView)
                .showEditBookUrl(onPutBookUrl, this);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }
    //////////////////////////////////////////////////////////

    public Boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isShowing() && canBack) {
                dismiss();
                return true;
            }
        }
        return false;
    }

    public Boolean getCanBack() {
        return canBack;
    }

    public Boolean onPressBack() {
        if (isShowing() && canBack) {
            dismiss();
            return true;
        }
        return false;
    }
}