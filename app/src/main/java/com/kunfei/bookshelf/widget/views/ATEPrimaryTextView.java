package com.kunfei.bookshelf.widget.views;

import android.content.Context;
import android.util.AttributeSet;

import com.kunfei.bookshelf.utils.theme.ThemeStore;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATEPrimaryTextView extends AppCompatTextView {

    public ATEPrimaryTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ATEPrimaryTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATEPrimaryTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setTextColor(ThemeStore.textColorPrimary(context));
    }
}
