package com.kunfei.bookshelf.widget.views;

import android.content.Context;
import android.util.AttributeSet;

import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATEEditText extends AppCompatEditText {

    public ATEEditText(Context context) {
        super(context);
        init(context, null);
    }

    public ATEEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATEEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ATH.setTint(this, ThemeStore.accentColor(context));
    }
}
