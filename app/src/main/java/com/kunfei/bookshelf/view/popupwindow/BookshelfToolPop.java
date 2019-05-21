package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.kunfei.bookshelf.R;

public class BookshelfToolPop extends PopupWindow {

    public BookshelfToolPop(Context context, OnClickListener onClickListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.pop_bookshelf_tool, null);
        this.setContentView(view);

        setTouchable(true);
        setOutsideTouchable(false);
        setFocusable(false);

        View back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> dismiss());

    }

    public interface OnClickListener {

        void click(String text);

        void unArrange();
    }

}
