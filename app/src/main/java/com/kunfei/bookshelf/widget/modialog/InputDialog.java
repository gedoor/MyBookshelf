package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.views.ATEAutoCompleteTextView;

/**
 * 输入框
 */
public class InputDialog {
    private TextView tvTitle;
    private ATEAutoCompleteTextView etInput;
    private TextView tvOk;

    private Context context;
    private BaseDialog dialog;

    public static InputDialog builder(Context context) {
        return new InputDialog(context);
    }

    private InputDialog(Context context) {
        this.context = context;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        bindView(view);
        dialog = new BaseDialog(context, R.style.alertDialogTheme);
        dialog.setContentView(view);
    }

    public InputDialog setDefaultValue(String defaultValue) {
        if (defaultValue != null) {
            etInput.setTextSize(2, 16); // 2 --> sp
            etInput.setText(defaultValue);
            etInput.setSelectAllOnFocus(true);
        }
        return this;
    }

    public InputDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public InputDialog setAdapterValues(String[] adapterValues) {
        if (adapterValues != null) {
            ArrayAdapter mAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, adapterValues);
            etInput.setAdapter(mAdapter);
        }
        return this;
    }

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tvTitle = view.findViewById(R.id.tv_title);
        etInput = view.findViewById(R.id.et_input);
        tvOk = view.findViewById(R.id.tv_ok);
    }

    public InputDialog setCallback(Callback callback) {
        tvOk.setOnClickListener(view -> {
            callback.setInputText(etInput.getText().toString());
            dialog.dismiss();
        });
        return this;
    }

    public void show() {
        dialog.show();
    }

    /**
     * 输入book地址确定
     */
    public interface Callback {
        void setInputText(String inputText);
    }
}
