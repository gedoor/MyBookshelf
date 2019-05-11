package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputLayout;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;

public class ReplaceRuleDialog {
    private Context context;
    private AppCompatEditText tieReplaceSummary;
    private AppCompatEditText tieReplaceRule;
    private AppCompatEditText tieReplaceTo;
    private AppCompatEditText tieUseTo;
    private CheckBox cbUseRegex;
    private TextView tvOk;

    private BaseDialog dialog;
    private ReplaceRuleBean replaceRuleBean;
    private BookShelfBean bookShelfBean;

    public static ReplaceRuleDialog builder(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean) {
        return new ReplaceRuleDialog(context, replaceRuleBean, bookShelfBean);
    }

    private ReplaceRuleDialog(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean) {
        this.context = context;
        this.replaceRuleBean = replaceRuleBean;
        this.bookShelfBean = bookShelfBean;
        dialog = new BaseDialog(context, R.style.alertDialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_replace_rule, null);
        bindView(view);
        dialog.setContentView(view);
    }

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceSummary = view.findViewById(R.id.til_replace_summary);
        tilReplaceSummary.setHint(context.getString(R.string.replace_rule_summary));
        TextInputLayout tilReplaceRule = view.findViewById(R.id.til_replace_rule);
        tilReplaceRule.setHint(context.getString(R.string.replace_rule));
        TextInputLayout tilReplaceTo = view.findViewById(R.id.til_replace_to);
        tilReplaceTo.setHint(context.getString(R.string.replace_to));
        TextInputLayout tilUseTo = view.findViewById(R.id.til_use_to);
        tilUseTo.setHint(context.getString(R.string.use_to));
        tieReplaceRule = view.findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = view.findViewById(R.id.tie_replace_summary);
        tieReplaceTo = view.findViewById(R.id.tie_replace_to);
        tieUseTo = view.findViewById(R.id.tie_use_to);
        cbUseRegex = view.findViewById(R.id.cb_use_regex);
        tvOk = view.findViewById(R.id.tv_ok);
        if (replaceRuleBean != null) {
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
            tieUseTo.setText(replaceRuleBean.getUseTo());
            cbUseRegex.setChecked(replaceRuleBean.getIsRegex());
        } else {
            replaceRuleBean = new ReplaceRuleBean();
            replaceRuleBean.setEnable(true);
            cbUseRegex.setChecked(MApplication.getConfigPreferences().getBoolean("useRegexInNewRule", false));
            if (bookShelfBean != null) {
                tieUseTo.setText(String.format("%s,%s", bookShelfBean.getBookInfoBean().getName(), bookShelfBean.getTag()));
            }
        }
    }

    public ReplaceRuleDialog setPositiveButton(Callback callback) {
        tvOk.setOnClickListener(v -> {
            replaceRuleBean.setReplaceSummary(getEditableText(tieReplaceSummary.getText()));
            replaceRuleBean.setRegex(getEditableText(tieReplaceRule.getText()));
            replaceRuleBean.setIsRegex(cbUseRegex.isChecked());
            replaceRuleBean.setReplacement(getEditableText(tieReplaceTo.getText()));
            replaceRuleBean.setUseTo(getEditableText(tieUseTo.getText()));
            callback.onPositiveButton(replaceRuleBean);
            dialog.dismiss();
        });
        return this;
    }

    private String getEditableText(Editable editable) {
        if (editable == null) {
            return "";
        }
        return editable.toString();
    }

    public ReplaceRuleDialog show() {
        dialog.show();
        return this;
    }

    public interface Callback {
        void onPositiveButton(ReplaceRuleBean replaceRuleBean);
    }

}
