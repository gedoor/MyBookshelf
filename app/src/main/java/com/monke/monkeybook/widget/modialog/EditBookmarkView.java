package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;

import org.jetbrains.annotations.NotNull;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditBookmarkView {
    private TextView tvChapterName;
    private TextInputEditText tvContent;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnSaveBookmark onSaveBookmark;
    private Context context;
    private BookmarkBean bookmarkBean;

    public static EditBookmarkView getInstance(MoProgressView moProgressView) {
        return new EditBookmarkView(moProgressView);
    }

    private EditBookmarkView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
    }

    void showEditReplaceRule(@NotNull BookmarkBean bookmarkBean, final OnSaveBookmark onSaveBookmark, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.onSaveBookmark = onSaveBookmark;

        this.bookmarkBean = bookmarkBean;

    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_bookmark, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceSummary = moProgressView.findViewById(R.id.til_replace_summary);
        tilReplaceSummary.setHint(context.getString(R.string.replace_rule_summary));
        TextInputLayout tilReplaceRule = moProgressView.findViewById(R.id.til_replace_rule);
        tilReplaceRule.setHint(context.getString(R.string.replace_rule));
        TextInputLayout tilReplaceTo = moProgressView.findViewById(R.id.til_replace_to);
        tilReplaceTo.setHint(context.getString(R.string.replace_to));
        tvChapterName = moProgressView.findViewById(R.id.tvChapterName);
        tvContent = moProgressView.findViewById(R.id.tie_content);

        View tvOk = moProgressView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            onSaveBookmark.saveBookmark(bookmarkBean);
            moProgressHUD.dismiss();
        });
        ImmersionBar.resetBoxPosition((Activity) context, moProgressView, R.id.cv_root);
    }

    /**
     * 输入替换规则完成
     */
    public interface OnSaveBookmark {
        void saveBookmark(BookmarkBean bookmarkBean);
    }
}
