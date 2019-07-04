package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookmarkBean;

public class BookmarkDialog extends BaseDialog {
    private Context context;
    private TextView tvChapterName;
    private EditText tvContent;
    private View llEdit;
    private View tvOk;
    private BookmarkBean bookmarkBean;
    private View tvSave;
    private View tvDel;

    public static BookmarkDialog builder(Context context, @NonNull BookmarkBean bookmarkBean, boolean isAdd) {
        return new BookmarkDialog(context, bookmarkBean, isAdd);
    }

    private BookmarkDialog(Context context, @NonNull BookmarkBean bookmarkBean, boolean isAdd) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        this.bookmarkBean = bookmarkBean;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_bookmark, null);
        bindView(view);
        setContentView(view);
        tvChapterName.setText(bookmarkBean.getChapterName());
        tvContent.setText(bookmarkBean.getContent());
        if (isAdd) {
            llEdit.setVisibility(View.GONE);
            tvOk.setVisibility(View.VISIBLE);
        } else {
            llEdit.setVisibility(View.VISIBLE);
            tvOk.setVisibility(View.GONE);
        }
    }

    private void bindView(View view) {
        tvChapterName = view.findViewById(R.id.tvChapterName);
        tvContent = view.findViewById(R.id.tie_content);
        tvOk = view.findViewById(R.id.tv_ok);
        tvSave = view.findViewById(R.id.tv_save);
        tvDel = view.findViewById(R.id.tv_del);
        llEdit = view.findViewById(R.id.llEdit);
    }

    public BookmarkDialog setPositiveButton(Callback callback) {
        tvChapterName.setOnClickListener(v -> {
            callback.openChapter(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex());
            dismiss();
        });
        tvOk.setOnClickListener(v -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            callback.saveBookmark(bookmarkBean);
            dismiss();
        });
        tvSave.setOnClickListener(v -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            callback.saveBookmark(bookmarkBean);
            dismiss();
        });
        tvDel.setOnClickListener(v -> {
            callback.delBookmark(bookmarkBean);
            dismiss();
        });
        return this;
    }

    public interface Callback {
        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void openChapter(int chapterIndex, int pageIndex);
    }

}
