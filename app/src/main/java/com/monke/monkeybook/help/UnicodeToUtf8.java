package com.monke.monkeybook.help;


import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by GKF on 2018/1/3.
 */

public class UnicodeToUtf8 {

    public static String getStr(String s) {
        return s;
    }

    public static String getStr(byte[] bytes) {
        try {
            return new String(bytes, "gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
