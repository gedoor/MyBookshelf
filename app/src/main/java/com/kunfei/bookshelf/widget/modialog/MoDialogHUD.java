package com.kunfei.bookshelf.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.utils.SoftInputUtil;

/**
 * 对话框
 */
public class MoDialogHUD {
    private Boolean isFinishing = false;

    private Context context;
    private ViewGroup decorView;//activity的根View
    private ViewGroup rootView;// mSharedView 的 根View
    private MoDialogView mSharedView;

    private OnDismissListener dismissListener;
    private Animation inAnim;
    private Animation outAnim;

    private Boolean canBack = false;
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

    public MoDialogHUD(Context context) {
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

    private void initViews() {
        decorView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = new FrameLayout(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootView.setLayoutParams(layoutParams);
        rootView.setClickable(true);
        rootView.setBackgroundColor(context.getResources().getColor(R.color.btn_bg_press_tp));

        mSharedView = new MoDialogView(context);

    }

    private Animation getInAnimation() {
        return AnimationUtils.loadAnimation(context, R.anim.moprogress_in);
    }

    private Animation getOutAnimation() {
        return AnimationUtils.loadAnimation(context, R.anim.moprogress_out);
    }

    private boolean isShowing() {
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
            SoftInputUtil.hideIMM(rootView);
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

    private void dismissImmediately() {
        if (dismissListener != null) {
            dismissListener.onDismiss();
            dismissListener = null;
        }
        if (mSharedView != null && rootView != null && mSharedView.getParent() != null) {
            new Handler().post(() -> {
                rootView.removeView(mSharedView);
                decorView.removeView(rootView);
            });
        }
        isFinishing = false;
    }

    /**
     * 返回键事件
     */
    public Boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isShowing()) {
                if (canBack) {
                    dismiss();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 加载动画
     */
    public void showLoading(String msg) {
        initCenter();
        initAnimation();
        canBack = false;
        rootView.setOnClickListener(null);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.showLoading(msg);
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 单个按钮的提示信息
     */
    public void showInfo(String msg) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(null);
        mSharedView.showInfo(msg, v -> dismiss());
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 单个按钮的提示信息
     */
    public void showInfo(String msg, String btnText, View.OnClickListener listener) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(null);
        mSharedView.showInfo(msg, btnText, listener);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 两个不同等级的按钮
     */
    public void showTwoButton(String msg, String b_f, View.OnClickListener c_f, String b_s, View.OnClickListener c_s) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(null);
        mSharedView.showTwoButton(msg, b_f, c_f, b_s, c_s);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 显示一段文本
     */
    public void showText(String text) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        mSharedView.showText(text);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 显示asset Markdown
     */
    public void showAssetMarkdown(String assetFileName) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        mSharedView.showAssetMarkdown(assetFileName);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 离线下载
     */
    public void showDownloadList(int startIndex, int endIndex, int all, DownLoadView.OnClickDownload clickDownload) {
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

    /**
     * 换源
     */
    public void showChangeSource(BookShelfBean bookShelf, ChangeSourceView.OnClickSource clickSource) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        ChangeSourceView changeSourceView = ChangeSourceView.getInstance(mSharedView);
        changeSourceView.showChangeSource(bookShelf, clickSource, this);
        dismissListener = changeSourceView::onDestroy;
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 弹出输入框
     */
    public void showInputBox(String title, String defaultValue, String[] adapterValues, InputView.OnInputOk onInputOk) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        InputView.getInstance(mSharedView)
                .showInputView(onInputOk, this, title, defaultValue, adapterValues);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 编辑替换规则
     */
    public void showPutReplaceRule(ReplaceRuleBean replaceRuleBean, EditReplaceRuleView.OnSaveReplaceRule onSaveReplaceRule) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        EditReplaceRuleView.getInstance(mSharedView)
                .showEditReplaceRule(replaceRuleBean, onSaveReplaceRule, this);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    /**
     * 书签
     */
    public void showBookmark(BookmarkBean bookmarkBean, boolean isAdd, EditBookmarkView.OnBookmarkClick bookmarkClick) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        EditBookmarkView.getInstance(mSharedView)
                .showBookmark(bookmarkBean, isAdd, bookmarkClick, this);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    public void showImageText(Bitmap bitmap, String text) {
        initCenter();
        initAnimation();
        canBack = true;
        rootView.setOnClickListener(v -> dismiss());
        mSharedView.showImageText(bitmap, text);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.getChildAt(0).startAnimation(inAnim);
    }

    private interface OnDismissListener {
        void onDismiss();
    }
}
