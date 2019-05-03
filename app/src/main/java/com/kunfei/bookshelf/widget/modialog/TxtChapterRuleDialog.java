package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.widget.views.ATEEditText;

public class TxtChapterRuleDialog {

    private ATEEditText tieRuleName;
    private ATEEditText tieRuleRegex;

    private AlertDialog.Builder builder;
    private TxtChapterRuleBean txtChapterRuleBean;


    public TxtChapterRuleDialog(Context context, TxtChapterRuleBean txtChapterRuleBean) {
        if (txtChapterRuleBean != null) {
            this.txtChapterRuleBean = txtChapterRuleBean.copy();
        }
        builder = new AlertDialog.Builder(context, R.style.alertDialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_txt_chpater_rule, null);
        tieRuleName = view.findViewById(R.id.tie_rule_name);
        tieRuleRegex = view.findViewById(R.id.tie_rule_regex);
        builder.setView(view);
        builder.setTitle(context.getString(R.string.txt_chapter_regex));
        builder.setNegativeButton(R.string.cancel, null);
        if (txtChapterRuleBean != null) {
            tieRuleName.setText(txtChapterRuleBean.getName());
            tieRuleRegex.setText(txtChapterRuleBean.getRule());
        }
    }

    public TxtChapterRuleDialog setPositiveButton(CallBack callBack) {
        builder.setPositiveButton("确认", (dialog, which) -> {
            if (txtChapterRuleBean == null) {
                txtChapterRuleBean = new TxtChapterRuleBean();
            }
            txtChapterRuleBean.setName(getEditableText(tieRuleName.getText()));
            txtChapterRuleBean.setRule(getEditableText(tieRuleRegex.getText()));
            callBack.onPositiveButton(txtChapterRuleBean);
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
        builder.show();
        return this;
    }

    public interface CallBack {
        void onPositiveButton(TxtChapterRuleBean txtChapterRuleBean);
    }

}
