package com.kunfei.bookshelf.widget.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.Selector;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

public class ATEStrokeTextView extends AppCompatTextView {
    public ATEStrokeTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ATEStrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATEStrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ATEStrokeTextView);

        setBackground(Selector.shapeBuild()
                .setCornerRadius(a.getDimensionPixelSize(R.styleable.ATEStrokeTextView_cornerRadius, 1))
                .setStrokeWidth(ScreenUtils.dpToPx(1))
                .setDisabledStrokeColor(context.getResources().getColor(R.color.md_grey_500))
                .setDefaultStrokeColor(ThemeStore.textColorSecondary(context))
                .setSelectedStrokeColor(ThemeStore.accentColor(context))
                .setPressedBgColor(context.getResources().getColor(R.color.transparent30))
                .create());
        setTextColor(Selector.colorBuild()
                .setDefaultColor(ThemeStore.textColorSecondary(context))
                .setSelectedColor(ThemeStore.accentColor(context))
                .setDisabledColor(context.getResources().getColor(R.color.md_grey_500))
                .create());
    }
}
