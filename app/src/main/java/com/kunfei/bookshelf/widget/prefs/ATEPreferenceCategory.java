package com.kunfei.bookshelf.widget.prefs;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.kunfei.bookshelf.utils.theme.ThemeStore;

@SuppressWarnings("unused")
public class ATEPreferenceCategory extends PreferenceCategory {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ATEPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ATEPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ATEPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ATEPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTextColor(ThemeStore.accentColor(view.getContext()));//设置title文本的颜色
        }
    }

}
