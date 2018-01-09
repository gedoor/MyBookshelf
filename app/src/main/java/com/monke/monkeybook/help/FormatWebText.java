package com.monke.monkeybook.help;

/**
 * Created by GKF on 2017/12/27.
 * 去除空格等
 */

public class FormatWebText {

    public static String getContent(String str) {
        return str
                .replaceAll(" ", "")
                .replaceAll(" ", "")
                .replaceAll("　", "")
                .replaceAll("\r","")
                .replaceAll("\n", "")
                .replaceAll("\t", "")
                .replaceAll("&nbsp;", "")
                .trim();
    }

    public static String getAuthor(String str) {
        return str
                .replaceAll(" ", "")
                .replaceAll("  ", "")
                .replaceAll("作者：", "");
    }
}
