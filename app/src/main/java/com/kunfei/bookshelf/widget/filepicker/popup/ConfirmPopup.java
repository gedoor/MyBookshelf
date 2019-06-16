package com.kunfei.bookshelf.widget.filepicker.popup;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.kunfei.bookshelf.widget.filepicker.util.ConvertUtils;

/**
 * 带确定及取消按钮的弹窗
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/10/21
 */
@SuppressWarnings("WeakerAccess")
public abstract class ConfirmPopup<V extends View> extends BasicPopup<View> {
    protected boolean topLineVisible = true;
    protected int topLineColor = 0xFF33B5E5;
    protected int topLineHeightPixels = 1;//px
    protected int topBackgroundColor = Color.WHITE;
    protected int topHeight = 40;//dp
    protected int topPadding = 15;//dp
    protected int contentLeftAndRightPadding = 0;//dp
    protected int contentTopAndBottomPadding = 0;//dp
    protected boolean cancelVisible = true;
    protected CharSequence cancelText = "";
    protected CharSequence submitText = "";
    protected CharSequence titleText = "";
    protected int cancelTextColor = 0xFF33B5E5;
    protected int submitTextColor = 0xFF33B5E5;
    protected int titleTextColor = Color.BLACK;
    protected int pressedTextColor = 0XFF0288CE;
    protected int cancelTextSize = 0;
    protected int submitTextSize = 0;
    protected int titleTextSize = 0;
    protected int backgroundColor = Color.WHITE;
    protected TextView cancelButton, submitButton;
    protected View titleView;
    protected View headerView, centerView, footerView;

    public ConfirmPopup(Activity activity) {
        super(activity);
        cancelText = activity.getString(android.R.string.cancel);
        submitText = activity.getString(android.R.string.ok);
    }

    /**
     * 设置顶部标题栏下划线颜色
     */
    public void setTopLineColor(@ColorInt int topLineColor) {
        this.topLineColor = topLineColor;
    }

    /**
     * 设置顶部标题栏下划线高度，单位为px
     */
    public void setTopLineHeight(int topLineHeightPixels) {
        this.topLineHeightPixels = topLineHeightPixels;
    }

    /**
     * 设置顶部标题栏背景颜色
     */
    public void setTopBackgroundColor(@ColorInt int topBackgroundColor) {
        this.topBackgroundColor = topBackgroundColor;
    }

    /**
     * 设置顶部标题栏高度（单位为dp）
     */
    public void setTopHeight(@IntRange(from = 10, to = 80) int topHeight) {
        this.topHeight = topHeight;
    }

    /**
     * 设置顶部按钮左边及右边边距（单位为dp）
     */
    public void setTopPadding(int topPadding) {
        this.topPadding = topPadding;
    }

    /**
     * 设置顶部标题栏下划线是否显示
     */
    public void setTopLineVisible(boolean topLineVisible) {
        this.topLineVisible = topLineVisible;
    }

    /**
     * 设置内容上下左右边距（单位为dp）
     */
    public void setContentPadding(int leftAndRight, int topAndBottom) {
        this.contentLeftAndRightPadding = leftAndRight;
        this.contentTopAndBottomPadding = topAndBottom;
    }

    /**
     * 设置顶部标题栏取消按钮是否显示
     */
    public void setCancelVisible(boolean cancelVisible) {
        if (null != cancelButton) {
            cancelButton.setVisibility(cancelVisible ? View.VISIBLE : View.GONE);
        } else {
            this.cancelVisible = cancelVisible;
        }
    }

    /**
     * 设置顶部标题栏取消按钮文字
     */
    public void setCancelText(CharSequence cancelText) {
        if (null != cancelButton) {
            cancelButton.setText(cancelText);
        } else {
            this.cancelText = cancelText;
        }
    }

    /**
     * 设置顶部标题栏取消按钮文字
     */
    public void setCancelText(@StringRes int textRes) {
        setCancelText(activity.getString(textRes));
    }

    /**
     * 设置顶部标题栏确定按钮文字
     */
    public void setSubmitText(CharSequence submitText) {
        if (null != submitButton) {
            submitButton.setText(submitText);
        } else {
            this.submitText = submitText;
        }
    }

    /**
     * 设置顶部标题栏确定按钮文字
     */
    public void setSubmitText(@StringRes int textRes) {
        setSubmitText(activity.getString(textRes));
    }

    /**
     * 设置顶部标题栏标题文字
     */
    public void setTitleText(CharSequence titleText) {
        if (titleView != null && titleView instanceof TextView) {
            ((TextView) titleView).setText(titleText);
        } else {
            this.titleText = titleText;
        }
    }

    /**
     * 设置顶部标题栏标题文字
     */
    public void setTitleText(@StringRes int textRes) {
        setTitleText(activity.getString(textRes));
    }

    /**
     * 设置顶部标题栏取消按钮文字颜色
     */
    public void setCancelTextColor(@ColorInt int cancelTextColor) {
        if (null != cancelButton) {
            cancelButton.setTextColor(cancelTextColor);
        } else {
            this.cancelTextColor = cancelTextColor;
        }
    }

    /**
     * 设置顶部标题栏确定按钮文字颜色
     */
    public void setSubmitTextColor(@ColorInt int submitTextColor) {
        if (null != submitButton) {
            submitButton.setTextColor(submitTextColor);
        } else {
            this.submitTextColor = submitTextColor;
        }
    }

    /**
     * 设置顶部标题栏标题文字颜色
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        if (null != titleView && titleView instanceof TextView) {
            ((TextView) titleView).setTextColor(titleTextColor);
        } else {
            this.titleTextColor = titleTextColor;
        }
    }

    /**
     * 设置按下时的文字颜色
     */
    public void setPressedTextColor(int pressedTextColor) {
        this.pressedTextColor = pressedTextColor;
    }

    /**
     * 设置顶部标题栏取消按钮文字大小（单位为sp）
     */
    public void setCancelTextSize(@IntRange(from = 10, to = 40) int cancelTextSize) {
        this.cancelTextSize = cancelTextSize;
    }

    /**
     * 设置顶部标题栏确定按钮文字大小（单位为sp）
     */
    public void setSubmitTextSize(@IntRange(from = 10, to = 40) int submitTextSize) {
        this.submitTextSize = submitTextSize;
    }

    /**
     * 设置顶部标题栏标题文字大小（单位为sp）
     */
    public void setTitleTextSize(@IntRange(from = 10, to = 40) int titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    /**
     * 设置选择器主体背景颜色
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public TextView getCancelButton() {
        if (null == cancelButton) {
            throw new NullPointerException("please call show at first");
        }
        return cancelButton;
    }

    public TextView getSubmitButton() {
        if (null == submitButton) {
            throw new NullPointerException("please call show at first");
        }
        return submitButton;
    }

    /**
     * @see #makeHeaderView()
     * @see #makeCenterView()
     * @see #makeFooterView()
     */
    @Override
    protected final View makeContentView() {
        LinearLayout rootLayout = new LinearLayout(activity);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rootLayout.setBackgroundColor(backgroundColor);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER);
        rootLayout.setPadding(0, 0, 0, 0);
        rootLayout.setClipToPadding(false);
        View headerView = makeHeaderView();
        if (headerView != null) {
            rootLayout.addView(headerView);
        }
        if (topLineVisible) {
            View lineView = new View(activity);
            lineView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, topLineHeightPixels));
            lineView.setBackgroundColor(topLineColor);
            rootLayout.addView(lineView);
        }
        if (centerView == null) {
            centerView = makeCenterView();
        }
        int lr = 0;
        int tb = 0;
        if (contentLeftAndRightPadding > 0) {
            lr = ConvertUtils.toPx(activity, contentLeftAndRightPadding);
        }
        if (contentTopAndBottomPadding > 0) {
            tb = ConvertUtils.toPx(activity, contentTopAndBottomPadding);
        }
        centerView.setPadding(lr, tb, lr, tb);
        ViewGroup vg = (ViewGroup) centerView.getParent();
        if (vg != null) {
            //IllegalStateException: The specified child already has a parent
            vg.removeView(centerView);
        }
        rootLayout.addView(centerView, new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
        View footerView = makeFooterView();
        if (footerView != null) {
            rootLayout.addView(footerView);
        }
        return rootLayout;
    }

    @Nullable
    protected View makeHeaderView() {
        if (null != headerView) {
            return headerView;
        }
        RelativeLayout topButtonLayout = new RelativeLayout(activity);
        int height = ConvertUtils.toPx(activity, topHeight);
        topButtonLayout.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, height));
        topButtonLayout.setBackgroundColor(topBackgroundColor);
        topButtonLayout.setGravity(Gravity.CENTER_VERTICAL);

        cancelButton = new TextView(activity);
        cancelButton.setVisibility(cancelVisible ? View.VISIBLE : View.GONE);
        RelativeLayout.LayoutParams cancelParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        cancelParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        cancelParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setBackgroundColor(Color.TRANSPARENT);
        cancelButton.setGravity(Gravity.CENTER);
        int padding = ConvertUtils.toPx(activity, topPadding);
        cancelButton.setPadding(padding, 0, padding, 0);
        if (!TextUtils.isEmpty(cancelText)) {
            cancelButton.setText(cancelText);
        }
        cancelButton.setTextColor(ConvertUtils.toColorStateList(cancelTextColor, pressedTextColor));
        if (cancelTextSize != 0) {
            cancelButton.setTextSize(cancelTextSize);
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onCancel();
            }
        });
        topButtonLayout.addView(cancelButton);

        if (null == titleView) {
            TextView textView = new TextView(activity);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            int margin = ConvertUtils.toPx(activity, topPadding);
            titleParams.leftMargin = margin;
            titleParams.rightMargin = margin;
            titleParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            titleParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            textView.setLayoutParams(titleParams);
            textView.setGravity(Gravity.CENTER);
            if (!TextUtils.isEmpty(titleText)) {
                textView.setText(titleText);
            }
            textView.setTextColor(titleTextColor);
            if (titleTextSize != 0) {
                textView.setTextSize(titleTextSize);
            }
            titleView = textView;
        }
        topButtonLayout.addView(titleView);

        submitButton = new TextView(activity);
        RelativeLayout.LayoutParams submitParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        submitParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        submitParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        submitButton.setLayoutParams(submitParams);
        submitButton.setBackgroundColor(Color.TRANSPARENT);
        submitButton.setGravity(Gravity.CENTER);
        submitButton.setPadding(padding, 0, padding, 0);
        if (!TextUtils.isEmpty(submitText)) {
            submitButton.setText(submitText);
        }
        submitButton.setTextColor(ConvertUtils.toColorStateList(submitTextColor, pressedTextColor));
        if (submitTextSize != 0) {
            submitButton.setTextSize(submitTextSize);
        }
        submitButton.setOnClickListener(v -> {
            dismiss();
            onSubmit();
        });
        topButtonLayout.addView(submitButton);

        return topButtonLayout;
    }

    @NonNull
    protected abstract V makeCenterView();

    @Nullable
    protected View makeFooterView() {
        if (null != footerView) {
            return footerView;
        }
        return null;
    }

    protected void onSubmit() {

    }

    protected void onCancel() {

    }

}
