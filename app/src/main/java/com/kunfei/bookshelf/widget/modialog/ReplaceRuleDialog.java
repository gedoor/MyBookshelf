package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;

public class ReplaceRuleDialog extends BaseDialog {
    private Context context;
    private AppCompatEditText tieReplaceSummary;
    private AppCompatEditText tieReplaceRule;
    private AppCompatEditText tieReplaceTo;
    private AppCompatEditText tieUseTo;
    private CheckBox cbUseRegex;
    private TextView tvOk;

    private ReplaceRuleBean replaceRuleBean;
    private BookShelfBean bookShelfBean;

    private TextView replace_ad_intro, tvtitle;
    private View til_replace_to;

    // 替换规则编辑UI的模式  1 默认  2 广告话术  3 添加广告话术
    private int ReplaceUIMode = 1;
    public static int DefaultUI = 1, AdUI = 2, AddAdUI = 3;
    private String str_summary = "";


    public static ReplaceRuleDialog builder(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean, int replaceUIMode) {
        return new ReplaceRuleDialog(context, replaceRuleBean, bookShelfBean, replaceUIMode);
    }


    public static ReplaceRuleDialog builder(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean) {
        return new ReplaceRuleDialog(context, replaceRuleBean, bookShelfBean);
    }

    private ReplaceRuleDialog(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        this.replaceRuleBean = replaceRuleBean;
        this.bookShelfBean = bookShelfBean;

        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_replace_rule, null);
        bindView(view);
        setContentView(view);

    }

    private ReplaceRuleDialog(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean, int replaceUIMod) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        this.replaceRuleBean = replaceRuleBean;
        this.bookShelfBean = bookShelfBean;
        this.ReplaceUIMode = replaceUIMod;

        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_replace_rule, null);
        bindView(view);
        setContentView(view);
    }

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        tieReplaceRule = view.findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = view.findViewById(R.id.tie_replace_summary);
        tieReplaceTo = view.findViewById(R.id.tie_replace_to);
        tieUseTo = view.findViewById(R.id.tie_use_to);
        cbUseRegex = view.findViewById(R.id.cb_use_regex);
        tvOk = view.findViewById(R.id.tv_ok);
        replace_ad_intro = view.findViewById(R.id.replace_ad_intro);
        tvtitle = view.findViewById(R.id.title);
        til_replace_to=view.findViewById(R.id.til_replace_to);
        if (replaceRuleBean != null) {
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
            tieUseTo.setText(replaceRuleBean.getUseTo());
            cbUseRegex.setChecked(replaceRuleBean.getIsRegex());

            // 初始化广告话术规则的UI
            if (ReplaceUIMode == DefaultUI) {
                if (replaceRuleBean.getReplaceSummary().matches("^" + view.getContext().getString(R.string.replace_ad) + ".*"))
                    ReplaceUIMode = AdUI;
            }
            if (ReplaceUIMode > DefaultUI) {
//                tieReplaceTo.setVisibility(View.GONE);
                til_replace_to.setVisibility(View.GONE);
                cbUseRegex.setVisibility(View.GONE);
                replace_ad_intro.setVisibility(View.VISIBLE);
                tieReplaceSummary.setInputType(EditorInfo.TYPE_NULL);
                tieReplaceRule.setMaxLines(8);

                if (ReplaceUIMode == AdUI) {
                    tvtitle.setText(view.getContext().getString(R.string.replace_ad_title));
                } else {
                    tvtitle.setText(view.getContext().getString(R.string.replace_add_ad_title));
                }
                str_summary = view.getContext().getString(R.string.replace_ad);
                TextWatcher mTextWatcher = new TextWatcher() {
                    private CharSequence temp;

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        temp = s;
                        String str=s.toString().trim();
                        if (str.replaceAll("[\\s,]", "").length() > 0)
                            tieReplaceSummary.setText(str_summary + "-" + str);
                        else
                            tieReplaceSummary.setText(str_summary);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
/*                        if (s.toString().replaceAll("[\\s,]", "").length() > 0)
                            tieReplaceSummary.setText(str_summary + "-" + temp);
                        else
                            tieReplaceSummary.setText(str_summary);*/
                    }
                };
                tieUseTo.addTextChangedListener(mTextWatcher);

            }
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
            dismiss();
        });
        return this;
    }

    private String getEditableText(Editable editable) {
        if (editable == null) {
            return "";
        }
        return editable.toString();
    }

    public interface Callback {
        void onPositiveButton(ReplaceRuleBean replaceRuleBean);
    }


}
