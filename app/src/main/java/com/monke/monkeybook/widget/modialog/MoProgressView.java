package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.victor.loading.rotate.RotateLoading;

/**
 * 对话框
 */
public class MoProgressView extends LinearLayout {
    private Context context;

    public MoProgressView(Context context) {
        this(context, null);
    }

    public MoProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setOrientation(VERTICAL);
    }

    //转圈的载入
    public void showLoading(String text) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_loading, this, true);
        TextView msgTv = findViewById(R.id.msg_tv);
        if (text != null && text.length() > 0) {
            msgTv.setText(text);
        }

        RotateLoading rlLoading = findViewById(R.id.rl_loading);
        rlLoading.start();
    }

    //单个按钮的信息提示框
    public void showInfo(String msg, final OnClickListener listener) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_infor, this, true);
        View llContent = findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        TextView msgTv = findViewById(R.id.msg_tv);
        msgTv.setText(msg);
        TextView tvClose = findViewById(R.id.tv_close);
        tvClose.setOnClickListener(listener);
    }

    //单个按钮的信息提示框
    public void showInfo(String msg, String btnText, final OnClickListener listener) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_infor, this, true);
        View llContent = findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        TextView msgTv = findViewById(R.id.msg_tv);
        msgTv.setText(msg);
        TextView tvClose = findViewById(R.id.tv_close);
        tvClose.setText(btnText);
        tvClose.setOnClickListener(listener);
    }

    //////////////////////两个不同等级的按钮//////////////////////
    public void showTwoButton(String msg, String b_f, OnClickListener c_f, String b_s, OnClickListener c_s) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_two, this, true);
        TextView tvMsg = findViewById(R.id.tv_msg);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        TextView tvDone = findViewById(R.id.tv_done);
        tvMsg.setText(msg);
        tvCancel.setText(b_f);
        tvCancel.setOnClickListener(c_f);
        tvDone.setText(b_s);
        tvDone.setOnClickListener(c_s);
    }

    /**
     * 显示一段文本
     */
    public void showText(String text) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_text_large, this, true);
        TextView textView = findViewById(R.id.tv_can_copy);
        textView.setText(text);
    }
}