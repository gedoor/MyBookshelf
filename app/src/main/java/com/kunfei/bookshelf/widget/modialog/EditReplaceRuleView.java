package com.kunfei.bookshelf.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.material.textfield.TextInputLayout;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.utils.SoftInputUtil;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditReplaceRuleView {
    private AppCompatEditText tieReplaceSummary;
    private AppCompatEditText tieReplaceRule;
    private AppCompatEditText tieReplaceTo;
    private AppCompatEditText tieUseTo;
    private CheckBox cbUseRegex;

    private MoDialogHUD moDialogHUD;
    private MoDialogView moDialogView;
    private OnSaveReplaceRule saveReplaceRule;
    private Context context;
    private ReplaceRuleBean replaceRuleBean;

    private EditReplaceRuleView(MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        bindView();
    }

    public static EditReplaceRuleView getInstance(MoDialogView moDialogView) {
        return new EditReplaceRuleView(moDialogView);
    }

    void showEditReplaceRule(ReplaceRuleBean replaceRuleBean, final OnSaveReplaceRule saveReplaceRule, MoDialogHUD moDialogHUD) {
        this.moDialogHUD = moDialogHUD;
        this.saveReplaceRule = saveReplaceRule;

        if (replaceRuleBean != null) {
            this.replaceRuleBean = replaceRuleBean;
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
            tieUseTo.setText(replaceRuleBean.getUseTo());
            cbUseRegex.setChecked(replaceRuleBean.getIsRegex());
        } else {
            this.replaceRuleBean = new ReplaceRuleBean();
            this.replaceRuleBean.setEnable(true);
            cbUseRegex.setChecked(MApplication.getConfigPreferences().getBoolean("useRegexInNewRule", false));
        }
    }

    private void bindView() {
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.mo_dialog_replace_rule, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceSummary = moDialogView.findViewById(R.id.til_replace_summary);
        tilReplaceSummary.setHint(context.getString(R.string.replace_rule_summary));
        TextInputLayout tilReplaceRule = moDialogView.findViewById(R.id.til_replace_rule);
        tilReplaceRule.setHint(context.getString(R.string.replace_rule));
        TextInputLayout tilReplaceTo = moDialogView.findViewById(R.id.til_replace_to);
        tilReplaceTo.setHint(context.getString(R.string.replace_to));
        TextInputLayout tilUseTo = moDialogView.findViewById(R.id.til_use_to);
        tilUseTo.setHint(context.getString(R.string.use_to));
        tieReplaceRule = moDialogView.findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = moDialogView.findViewById(R.id.tie_replace_summary);
        tieReplaceTo = moDialogView.findViewById(R.id.tie_replace_to);
        tieUseTo = moDialogView.findViewById(R.id.tie_use_to);
        cbUseRegex = moDialogView.findViewById(R.id.cb_use_regex);

        View tvOk = moDialogView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            replaceRuleBean.setReplaceSummary(tieReplaceSummary.getText().toString());
            replaceRuleBean.setRegex(tieReplaceRule.getText().toString());
            replaceRuleBean.setIsRegex(cbUseRegex.isChecked());
            replaceRuleBean.setReplacement(tieReplaceTo.getText().toString());
            replaceRuleBean.setUseTo(tieUseTo.getText().toString());
            saveReplaceRule.saveReplaceRule(replaceRuleBean);
            moDialogHUD.dismiss();
        });
        SoftInputUtil.resetBoxPosition((Activity) context, moDialogView, R.id.cv_root);
    }

    /**
     * 输入替换规则完成
     */
    public interface OnSaveReplaceRule {
        void saveReplaceRule(ReplaceRuleBean replaceRuleBean);
    }
}
