package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.activity.ReplaceRuleActivity;
import com.monke.monkeybook.widget.flowlayout.TagFlowLayout;

import static java.security.AccessController.getContext;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditReplaceRuleView {
    private TextInputEditText tieReplaceSummary;
    private TextInputEditText tieReplaceRule;
    private TextInputEditText tieReplaceTo;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnSaveReplaceRule saveReplaceRule;
    private Context context;
    private ReplaceRuleBean replaceRuleBean;

    public static EditReplaceRuleView getInstance(MoProgressView moProgressView) {
        return new EditReplaceRuleView(moProgressView);
    }

    private EditReplaceRuleView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
    }

    void showEditReplaceRule(ReplaceRuleBean replaceRuleBean, final OnSaveReplaceRule saveReplaceRule, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.saveReplaceRule = saveReplaceRule;

        if (replaceRuleBean != null) {
            this.replaceRuleBean = replaceRuleBean;
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
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
        tieReplaceRule = moProgressView.findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = moProgressView.findViewById(R.id.tie_replace_summary);
        tieReplaceTo = moProgressView.findViewById(R.id.tie_replace_to);

        View tvOk = moProgressView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            replaceRuleBean.setReplaceSummary(tieReplaceSummary.getText().toString());
            replaceRuleBean.setRegex(tieReplaceRule.getText().toString());
            replaceRuleBean.setReplacement(tieReplaceTo.getText().toString());
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
