package com.monke.monkeybook.help;

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
                .replaceAll("\\s", " ")
                .replace("\r", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace("&nbsp;", "")
                .trim();
    }

    public static String getAuthor(String str) {
        if (str == null) {
            return "";
        }
        return str
                .replaceAll("\\s", " ")
                .replace("&nbsp;", "")
                .replace("作者", "")
                .replace("：", "")
                .replace(":", "")
                .replace("(", "")
                .replace(")", "")
                .trim();
    }
}
