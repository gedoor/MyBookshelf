package com.kunfei.bookshelf.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.utils.SoftInputUtil;

import androidx.annotation.NonNull;


/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditBookmarkView {
    private TextView tvChapterName;
    private EditText tvContent;
    private View llEdit;
    private View tvOk;

    private MoDialogHUD moDialogHUD;
    private MoDialogView moDialogView;
    private OnBookmarkClick bookmarkClick;
    private Context context;
    private BookmarkBean bookmarkBean;

    private EditBookmarkView(MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        bindView();
    }

    public static EditBookmarkView getInstance(MoDialogView moDialogView) {
        return new EditBookmarkView(moDialogView);
    }

    void showBookmark(@NonNull BookmarkBean bookmarkBean, boolean isAdd, final OnBookmarkClick bookmarkClick, MoDialogHUD moDialogHUD) {
        this.moDialogHUD = moDialogHUD;
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
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.mo_dialog_bookmark, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceTo = moDialogView.findViewById(R.id.til_content);
        tilReplaceTo.setHint(context.getString(R.string.content));
        tvChapterName = moDialogView.findViewById(R.id.tvChapterName);
        tvChapterName.setOnClickListener(view -> {
            bookmarkClick.openChapter(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex());
            moDialogHUD.dismiss();
        });
        tvContent = moDialogView.findViewById(R.id.tie_content);

        tvOk = moDialogView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            bookmarkClick.saveBookmark(bookmarkBean);
            moDialogHUD.dismiss();
        });

        View tvSave = moDialogView.findViewById(R.id.tv_save);
        tvSave.setOnClickListener(view -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            bookmarkClick.saveBookmark(bookmarkBean);
            moDialogHUD.dismiss();
        });
        View tvDel = moDialogView.findViewById(R.id.tv_del);
        tvDel.setOnClickListener(view -> {
            bookmarkClick.delBookmark(bookmarkBean);
            moDialogHUD.dismiss();
        });

        llEdit = moDialogView.findViewById(R.id.llEdit);

        SoftInputUtil.resetBoxPosition((Activity) context, moDialogView, R.id.cv_root);
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
