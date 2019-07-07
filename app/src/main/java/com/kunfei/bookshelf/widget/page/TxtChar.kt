package com.kunfei.bookshelf.widget.page

import android.graphics.Point


class TxtChar {
    var chardata: Char = ' '//字符数据

    var selected: Boolean? = false//当前字符是否被选中

    //记录文字的左上右上左下右下四个点坐标
    var topLeftPosition: Point? = null//左上
    var topRightPosition: Point? = null//右上
    var bottomLeftPosition: Point? = null//左下
    var bottomRightPosition: Point? = null//右下

    var charWidth = 0f//字符宽度
    var Index = 0//当前字符位置

    override fun toString(): String {
        return ("ShowChar [chardata=" + chardata + ", Selected=" + selected + ", TopLeftPosition=" + topLeftPosition
                + ", TopRightPosition=" + topRightPosition + ", BottomLeftPosition=" + bottomLeftPosition
                + ", BottomRightPosition=" + bottomRightPosition + ", charWidth=" + charWidth + ", Index=" + Index
                + "]");
    }
}