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
    private View llEdit;
    private View tvOk;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnBookmarkClick bookmarkClick;
    private Context context;
    private BookmarkBean bookmarkBean;

    private EditBookmarkView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
    }

    public static EditBookmarkView getInstance(MoProgressView moProgressView) {
        return new EditBookmarkView(moProgressView);
    }

    void showBookmark(@NotNull BookmarkBean bookmarkBean, boolean isAdd, final OnBookmarkClick bookmarkClick, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.bookmarkClick = bookmarkClick;
        this.bookmarkBean = bookmarkBean;

        if (isAdd) {
            llEdit.setVisibility(View.GONE);
            tvOk.setVisibility(View.VISIBLE);
        } else {
            llEdit.setVisibility(View.VISIBLE);
            tvOk.setVisibility(View.GONE);
        }

        tvChapterName.setText(bookmarkBean.getChapterName());
        tvContent.setText(bookmarkBean.getContent());
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_bookmark, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceTo = moProgressView.findViewById(R.id.til_content);
        tilReplaceTo.setHint(context.getString(R.string.content));
        tvChapterName = moProgressView.findViewById(R.id.tvChapterName);
        tvChapterName.setOnClickListener(view -> {
            bookmarkClick.openChapter(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex());
            moProgressHUD.dismiss();
        });
        tvContent = moProgressView.findViewById(R.id.tie_content);

        tvOk = moProgressView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            bookmarkClick.saveBookmark(bookmarkBean);
            moProgressHUD.dismiss();
        });

        View tvSave = moProgressView.findViewById(R.id.tv_save);
        tvSave.setOnClickListener(view -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            bookmarkClick.saveBookmark(bookmarkBean);
            moProgressHUD.dismiss();
        });
        View tvDel = moProgressView.findViewById(R.id.tv_del);
        tvDel.setOnClickListener(view -> {
            bookmarkClick.delBookmark(bookmarkBean);
            moProgressHUD.dismiss();
        });

        llEdit = moProgressView.findViewById(R.id.llEdit);

        ImmersionBar.resetBoxPosition((Activity) context, moProgressView, R.id.cv_root);
    }

    /**
     * 输入替换规则完成
     */
    public interface OnBookmarkClick {
        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void openChapter(int chapterIndex, int pageIndex);
    }
}
