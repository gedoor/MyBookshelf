package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ReplaceRuleBean;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditReplaceRuleView {
    private EditText etUrl;
    private TextView tvOk;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnSaveReplaceRule saveReplaceRule;
    private Context context;

    public static EditReplaceRuleView getInstance(MoProgressView moProgressView) {
        return new EditReplaceRuleView(moProgressView);
    }

    private EditReplaceRuleView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        tvOk.setOnClickListener(view -> {
            ReplaceRuleBean replaceRuleBean = new ReplaceRuleBean();
            replaceRuleBean.setEnable(true);
            replaceRuleBean.setReplaceSummary("");
            replaceRuleBean.setRegex("");
            replaceRuleBean.setReplacement("");
            saveReplaceRule.saveReplaceRule(replaceRuleBean);
            moProgressHUD.dismiss();
        });
    }

    void showEditReplaceRule(ReplaceRuleBean replaceRuleBean, final OnSaveReplaceRule saveReplaceRule, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.saveReplaceRule = saveReplaceRule;

    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_replace_rule, moProgressView, true);

        etUrl = moProgressView.findViewById(R.id.et_book_url);
        tvOk = moProgressView.findViewById(R.id.tv_ok);
    }

    /**
     * 输入替换规则完成
     */
    public interface OnSaveReplaceRule {
        void saveReplaceRule(ReplaceRuleBean replaceRuleBean);
    }
}
