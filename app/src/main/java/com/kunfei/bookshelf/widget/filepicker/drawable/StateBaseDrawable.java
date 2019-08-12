package com.kunfei.bookshelf.widget.filepicker.drawable;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * 按下状态与普通状态下显示不同的图片或颜色
 * <br />
 * Author:李玉江[QQ:1032694760]
 * DateTime:2017/01/01 05:30
 * Builder:Android Studio
 */
public abstract class StateBaseDrawable extends StateListDrawable {

    protected void addState(Drawable pressed) {
        addState(new ColorDrawable(Color.TRANSPARENT), pressed);
    }

    protected void addState(Drawable normal, Drawable pressed) {
        addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, pressed);
        addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, pressed);
        addState(new int[]{android.R.attr.state_enabled}, normal);
        addState(new int[]{android.R.attr.state_focused}, pressed);
        addState(new int[]{android.R.attr.state_window_focused}, normal);
        addState(new int[]{}, normal);
    }

}
