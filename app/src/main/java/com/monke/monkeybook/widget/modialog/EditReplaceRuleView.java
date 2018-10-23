package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditReplaceRuleView {
    private AppCompatEditText tieReplaceSummary;
    private AppCompatEditText tieReplaceRule;
    private AppCompatEditText tieReplaceTo;
    private AppCompatEditText tieUseTo;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnSaveReplaceRule saveReplaceRule;
    private Context context;
    private ReplaceRuleBean replaceRuleBean;

    private EditReplaceRuleView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
    }

    public static EditReplaceRuleView getInstance(MoProgressView moProgressView) {
        return new EditReplaceRuleView(moProgressView);
    }

    void showEditReplaceRule(ReplaceRuleBean replaceRuleBean, final OnSaveReplaceRule saveReplaceRule, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.saveReplaceRule = saveReplaceRule;

        if (replaceRuleBean != null) {
            this.replaceRuleBean = replaceRuleBean;
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
            tieUseTo.setText(replaceRuleBean.getUseTo());
        } else {
            this.replaceRuleBean = new ReplaceRuleBean();
            this.replaceRuleBean.setEnable(true);
        }
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_replace_rule, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceSummary = moProgressView.findViewById(R.id.til_replace_summary);
        tilReplaceSummary.setHint(context.getString(R.string.replace_rule_summary));
        TextInputLayout tilReplaceRule = moProgressView.findViewById(R.id.til_replace_rule);
        tilReplaceRule.setHint(context.getString(R.string.replace_rule));
        TextInputLayout tilReplaceTo = moProgressView.findViewById(R.id.til_replace_to);
        tilReplaceTo.setHint(context.getString(R.string.replace_to));
        TextInputLayout tilUseTo = moProgressView.findViewById(R.id.til_use_to);
        tilUseTo.setHint(context.getString(R.string.use_to));
        tieReplaceRule = moProgressView.findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = moProgressView.findViewById(R.id.tie_replace_summary);
        tieReplaceTo = moProgressView.findViewById(R.id.tie_replace_to);
        tieUseTo = moProgressView.findViewById(R.id.tie_use_to);

        View tvOk = moProgressView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            replaceRuleBean.setReplaceSummary(tieReplaceSummary.getText().toString());
            replaceRuleBean.setRegex(tieReplaceRule.getText().toString());
            replaceRuleBean.setReplacement(tieReplaceTo.getText().toString());
            replaceRuleBean.setUseTo(tieUseTo.getText().toString());
            saveReplaceRule.saveReplaceRule(replaceRuleBean);
            moProgressHUD.dismiss();
        });
        ImmersionBar.resetBoxPosition((Activity) context, moProgressView, R.id.cv_root);
    }

    /**
     * 输入替换规则完成
     */
    public interface OnSaveReplaceRule {
        void saveReplaceRule(ReplaceRuleBean replaceRuleBean);
    }
}
