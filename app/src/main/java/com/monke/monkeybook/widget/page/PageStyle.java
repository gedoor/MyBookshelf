package com.monke.monkeybook.widget.page;

import android.support.annotation.ColorRes;

import com.monke.monkeybook.R;


/**
 * Created by newbiechen on 2018/2/5.
 * 作用：页面的展示风格。
 */

public enum PageStyle {
    STYLE_0(R.color.nb_read_font_1, R.color.nb_read_bg_1),
    STYLE_1(R.color.nb_read_font_2, R.color.nb_read_bg_2),
    STYLE_2(R.color.nb_read_font_3, R.color.nb_read_bg_3),
    STYLE_3(R.color.nb_read_font_4, R.color.nb_read_bg_4),
    STYLE_4(R.color.nb_read_font_5, R.color.nb_read_bg_5),;

    private int fontColor;
    private int bgColor;

    PageStyle(@ColorRes int fontColor, @ColorRes int bgColor) {
        this.fontColor = fontColor;
        this.bgColor = bgColor;
    }

    public int getFontColor() {
        return fontColor;
    }

    public int getBgColor() {
        return bgColor;
    }
}
