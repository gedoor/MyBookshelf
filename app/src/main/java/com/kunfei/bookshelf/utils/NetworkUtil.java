//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NetworkUtil {
    public static final int SUCCESS = 10000;
    public static final int ERROR_CODE_NONET = 10001;
    public static final int ERROR_CODE_OUTTIME = 10002;
    public static final int ERROR_CODE_ANALY = 10003;
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String> errorMap = new HashMap<>();

    static {
        errorMap.put(ERROR_CODE_NONET, MApplication.getInstance().getString(R.string.net_error_10001));
        errorMap.put(ERROR_CODE_OUTTIME, MApplication.getInstance().getString(R.string.net_error_10002));
        errorMap.put(ERROR_CODE_ANALY, MApplication.getInstance().getString(R.string.net_error_10003));
    }

    public static String getErrorTip(int code) {
        return errorMap.get(code);
    }

    public static boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) MApplication.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } else {
            return false;
        }
    }

    /**
     * 获取绝对地址
     */
    public static String getAbsoluteURL(String baseURL, String relativePath) {
        String header = null;
        if (StringUtils.startWithIgnoreCase(relativePath, "@header:")) {
            header = relativePath.substring(0, relativePath.indexOf("}") + 1);
            relativePath = relativePath.substring(header.length());
        }
        try {
            URL absoluteUrl = new URL(baseURL);
            URL parseUrl = new URL(absoluteUrl, relativePath);
            relativePath = parseUrl.toString();
            if (header != null) {
                relativePath = header + relativePath;
            }
            return relativePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relativePath;
    }

    public static boolean isUrl(String urlStr) {
        String regex = "^(https?)://.+$";//设置正则表达式
        return urlStr.matches(regex);
    }
}
