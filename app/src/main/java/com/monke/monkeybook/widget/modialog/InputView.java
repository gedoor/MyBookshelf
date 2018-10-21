package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;

/**
 * 输入框
 */

public class InputView {
    private TextView tvTitle;
    private EditText etInput;
    private TextView tvOk;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnInputOk onInputOk;
    private Context context;

    private InputView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        tvOk.setOnClickListener(view -> {
            onInputOk.setInputText(etInput.getText().toString());
            moProgressHUD.dismiss();
        });
    }

    public static InputView getInstance(MoProgressView moProgressView) {
        return new InputView(moProgressView);
    }

    void showInputView(final OnInputOk onInputOk, MoProgressHUD moProgressHUD, String title, String defaultValue) {
        this.moProgressHUD = moProgressHUD;
        this.onInputOk = onInputOk;
        tvTitle.setText(title);
        if (defaultValue != null) {
            etInput.setTextSize(2, 16); // 2 --> sp
            etInput.setText(defaultValue);
            etInput.setSelectAllOnFocus(true);
        }
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_input, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tvTitle = moProgressView.findViewById(R.id.tv_title);
        etInput = moProgressView.findViewById(R.id.et_input);
        tvOk = moProgressView.findViewById(R.id.tv_ok);

        ImmersionBar.resetBoxPosition((Activity) context, moProgressView, R.id.cv_root);
    }

    /**
     * 输入book地址确定
     */
    public interface OnInputOk {
        void setInputText(String inputText);
    }
}
