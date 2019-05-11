package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.widget.views.ATEEditText;

public class TxtChapterRuleDialog {

    private ATEEditText tieRuleName;
    private ATEEditText tieRuleRegex;
    private TextView tvOk;

    private BaseDialog dialog;
    private TxtChapterRuleBean txtChapterRuleBean;

    public static TxtChapterRuleDialog builder(Context context, TxtChapterRuleBean txtChapterRuleBean) {
        return new TxtChapterRuleDialog(context, txtChapterRuleBean);
    }

    private TxtChapterRuleDialog(Context context, TxtChapterRuleBean txtChapterRuleBean) {
        if (txtChapterRuleBean != null) {
            this.txtChapterRuleBean = txtChapterRuleBean.copy();
        }
        dialog = new BaseDialog(context, R.style.alertDialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_txt_chpater_rule, null);
        bindView(view);
        dialog.setContentView(view);
    }

    private void bindView(View view) {
        tieRuleName = view.findViewById(R.id.tie_rule_name);
        tieRuleRegex = view.findViewById(R.id.tie_rule_regex);
        tvOk = view.findViewById(R.id.tv_ok);
        if (txtChapterRuleBean != null) {
            tieRuleName.setText(txtChapterRuleBean.getName());
            tieRuleRegex.setText(txtChapterRuleBean.getRule());
        }
    }

    public TxtChapterRuleDialog setPositiveButton(Callback callback) {
        tvOk.setOnClickListener(v -> {
            if (txtChapterRuleBean == null) {
                txtChapterRuleBean = new TxtChapterRuleBean();
            }
            txtChapterRuleBean.setName(getEditableText(tieRuleName.getText()));
            txtChapterRuleBean.setRule(getEditableText(tieRuleRegex.getText()));
            callback.onPositiveButton(txtChapterRuleBean);
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

    public TxtChapterRuleDialog show() {
        dialog.show();
        return this;
    }

    public interface Callback {
        void onPositiveButton(TxtChapterRuleBean txtChapterRuleBean);
    }

}
