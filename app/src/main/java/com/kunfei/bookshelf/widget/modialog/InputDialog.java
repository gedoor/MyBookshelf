package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.views.ATEAutoCompleteTextView;

import java.util.List;

/**
 * 输入框
 */
public class InputDialog extends BaseDialog {
    private boolean showDel = false;
    private TextView tvTitle;
    private ATEAutoCompleteTextView etInput;
    private TextView tvOk;
    private Callback callback = null;
    private Context context;

    public static InputDialog builder(Context context) {
        return new InputDialog(context);
    }

    private InputDialog(Context context) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        setContentView(view);
        bindView(view);
    }

    public InputDialog setShowDel(boolean showDel) {
        this.showDel = showDel;
        return this;
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

    public InputDialog setAdapterValues(List<String> adapterValues) {
        if (adapterValues != null) {
            MyAdapter mAdapter = new MyAdapter(context, adapterValues);
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
        this.callback = callback;
        tvOk.setOnClickListener(view -> {
            callback.setInputText(etInput.getText().toString());
            dismiss();
        });
        return this;
    }

    class MyAdapter extends ArrayAdapter {

        MyAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, R.layout.item_1line_text_and_del, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_1line_text_and_del, parent, false);
            } else {
                view = convertView;
            }
            TextView tv = view.findViewById(R.id.text);
            ImageView iv = view.findViewById(R.id.iv_del);
            if (showDel) {
                iv.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.GONE);
            }
            String value = String.valueOf(getItem(position));
            tv.setText(value);
            iv.setOnClickListener(v -> {
                remove(value);
                if (callback != null) {
                    callback.delete(value);
                }
                etInput.showDropDown();
            });
            return view;
        }
    }


    /**
     * 输入book地址确定
     */
    public interface Callback {
        void setInputText(String inputText);

        void delete(String value);
    }
}
