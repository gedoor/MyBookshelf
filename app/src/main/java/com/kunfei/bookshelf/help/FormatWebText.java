package com.kunfei.bookshelf.help;

import android.text.TextUtils;

/**
 * Created by GKF on 2017/12/27.
 * 去除空格等
 */

public class FormatWebText {

    public static String getContent(String str) {
        if (str == null) {
            return null;
        }
        return str
                .replace("\r", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace("&nbsp;", "")
                .replaceAll("\\s", " ")
                .trim();
    }

    public static String getBookName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return trim(str.replace("&nbsp;", "")
                .replace(":", "：")
                .replace(",", "，")
                .replaceAll("[\\u3000 ]+", "")
                .replaceAll("\\s", " ")
                .replaceAll("[?？!！。~《》【】]", "")
                .replaceAll("([(（].*[）)])", ""));
    }

    public static String getAuthor(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return trim(str.replace("&nbsp;", "")
                .replaceAll("[：:()【】\\[\\]（）\\u3000 ]+", "")
                .replaceAll("\\s", " ")
                .replaceAll("作.*?者", ""));
    }

    public static String trim(String s) {
        String result = "";
        if (null != s && !"".equals(s)) {
            result = s.replaceAll("^[　*| *| *|//s*]*", "").replaceAll("[　*| *| *|//s*]*$", "");
        }
        return result;
    }
}
