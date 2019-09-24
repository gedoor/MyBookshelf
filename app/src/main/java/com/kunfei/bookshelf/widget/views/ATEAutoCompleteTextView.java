package com.kunfei.bookshelf.widget.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.kunfei.bookshelf.utils.Selector;
import com.kunfei.bookshelf.utils.theme.ThemeStore;


public class ATEAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public ATEAutoCompleteTextView(Context context) {
        super(context);
        init(context);
    }

    public ATEAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ATEAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackgroundTintList(Selector.colorBuild()
                    .setFocusedColor(ThemeStore.accentColor(context))
                    .setDefaultColor(ThemeStore.textColorPrimary(context))
                    .create());
        }
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        performFiltering(getText(), KeyEvent.KEYCODE_UNKNOWN);
    }
}
