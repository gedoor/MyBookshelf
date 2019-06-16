package com.kunfei.bookshelf.widget.filepicker.drawable;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.ColorInt;

/**
 * 按下状态与普通状态下显示不同的颜色
 * <br />
 * Author:李玉江[QQ:1032694760]
 * DateTime:2017/01/01 05:30
 * Builder:Android Studio
 */
public class StateColorDrawable extends StateBaseDrawable {

    public StateColorDrawable(@ColorInt int pressedColor) {
        this(Color.TRANSPARENT, pressedColor);
    }

    public StateColorDrawable(@ColorInt int normalColor, @ColorInt int pressedColor) {
        addState(new ColorDrawable(normalColor), new ColorDrawable(pressedColor));
    }

}
