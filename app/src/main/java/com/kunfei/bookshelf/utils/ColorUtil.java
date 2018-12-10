package com.kunfei.bookshelf.utils;

public class ColorUtil {

    public static String intToString(int intColor) {
        return String.format("#%06X", 0xFFFFFF & intColor);
    }

}
