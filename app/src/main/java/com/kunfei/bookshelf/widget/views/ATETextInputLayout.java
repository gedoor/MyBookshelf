package com.kunfei.bookshelf.widget.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.google.android.material.textfield.TextInputLayout;
import com.kunfei.bookshelf.utils.Selector;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import androidx.annotation.Nullable;

public class ATETextInputLayout extends TextInputLayout {
    public ATETextInputLayout(Context context) {
        super(context);
        init(context);
    }

    public ATETextInputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ATETextInputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setHintTextColor(Selector.colorBuild().setDefaultColor(ThemeStore.accentColor(context)).create());
    }

    @Override
    public void draw(Canvas canvas) {

        super.draw(canvas);
    }
}
