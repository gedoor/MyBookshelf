package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.views.ATEAutoCompleteTextView;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * 输入框
 */
public class PageKeyDialog extends BaseDialog {
    private TextView tvTitle;
    private ATEAutoCompleteTextView etInput;
    private TextView tvOk;
    private Context context;

    public static PageKeyDialog builder(Context context) {
        return new PageKeyDialog(context);
    }

    private PageKeyDialog(Context context) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        setContentView(view);
        bindView(view);
    }

    public PageKeyDialog setDefaultValue(String defaultValue) {
        if (defaultValue != null) {
            etInput.setTextSize(2, 16); // 2 --> sp
            etInput.setText(defaultValue);
            etInput.setSelectAllOnFocus(true);
        }
        return this;
    }

    public PageKeyDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tvTitle = view.findViewById(R.id.tv_title);
        etInput = view.findViewById(R.id.et_input);
        tvOk = view.findViewById(R.id.tv_ok);
    }

    public PageKeyDialog setCallback(Callback callback) {
        tvOk.setOnClickListener(view -> {
            callback.setInputText(etInput.getText().toString());
            dismiss();
        });
        return this;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode != KEYCODE_BACK) {
            etInput.setText(String.valueOf(keyCode));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 输入book地址确定
     */
    public interface Callback {
        void setInputText(String inputText);
    }
}
