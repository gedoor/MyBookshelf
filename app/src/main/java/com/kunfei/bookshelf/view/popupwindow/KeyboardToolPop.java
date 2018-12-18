package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kunfei.bookshelf.R;

public class KeyboardToolPop extends PopupWindow {

    public KeyboardToolPop(Context context, OnClickListener onClickListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.pop_soft_keyboard_top_tool, null);
        this.setContentView(view);

        setTouchable(true);
        setOutsideTouchable(false);
        setFocusable(false);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED); //解决遮盖输入法

        LinearLayout linearLayout = getContentView().findViewById(R.id.ll_content);

        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            TextView tv = (TextView) linearLayout.getChildAt(i);
            tv.setOnClickListener(v -> {
                if (onClickListener != null) {
                    onClickListener.click(((TextView) v).getText().toString());
                }
            });
        }
    }

    public interface OnClickListener {
        void click(String text);
    }

}
