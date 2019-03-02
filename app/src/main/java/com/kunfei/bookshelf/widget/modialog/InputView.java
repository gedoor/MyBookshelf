package com.kunfei.bookshelf.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.Selector;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

/**
 * 输入框
 */
public class InputView {
    private TextView tvTitle;
    private AutoCompleteTextView etInput;
    private TextView tvOk;

    private MoDialogHUD moDialogHUD;
    private MoDialogView moDialogView;
    private OnInputOk onInputOk;
    private Context context;

    private InputView(MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        bindView();
        tvOk.setOnClickListener(view -> {
            onInputOk.setInputText(etInput.getText().toString());
            moDialogHUD.dismiss();
        });
    }

    public static InputView getInstance(MoDialogView moDialogView) {
        return new InputView(moDialogView);
    }

    void showInputView(final OnInputOk onInputOk, MoDialogHUD moDialogHUD, String title, String defaultValue, String[] adapterValues) {
        this.moDialogHUD = moDialogHUD;
        this.onInputOk = onInputOk;
        tvTitle.setText(title);
        if (defaultValue != null) {
            etInput.setTextSize(2, 16); // 2 --> sp
            etInput.setText(defaultValue);
            etInput.setSelectAllOnFocus(true);
        }
        if (adapterValues != null) {
            ArrayAdapter mAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, adapterValues);
            etInput.setAdapter(mAdapter);
        }
    }

    private void bindView() {
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.mo_dialog_input, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tvTitle = moDialogView.findViewById(R.id.tv_title);
        etInput = moDialogView.findViewById(R.id.et_input);
        tvOk = moDialogView.findViewById(R.id.tv_ok);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            etInput.setBackgroundTintList(Selector.colorBuild().setFocusedColor(ThemeStore.accentColor(context)).create());
        }
        SoftInputUtil.resetBoxPosition((Activity) context, moDialogView, R.id.cv_root);
    }

    /**
     * 输入book地址确定
     */
    public interface OnInputOk {
        void setInputText(String inputText);
    }
}
