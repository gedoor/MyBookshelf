//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NetworkUtil {
    private static final Map<Integer, String> errorMap = new HashMap<>();

    public static final int SUCCESS = 10000;
    public static final int ERROR_CODE_NONET = 10001;
    public static final int ERROR_CODE_OUTTIME = 10002;
    public static final int ERROR_CODE_ANALY = 10003;

    static{
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
    public static String getAbsoluteURL(String baseURI, String relativePath) {
        String abURL = relativePath;
        try {
            URI base = new URI(baseURI);//基本网页URI
            URI abs = base.resolve(relativePath);//解析于上述网页的相对URL，得到绝对URI
            URL absURL = abs.toURL();//转成URL
            abURL = absURL.toString();
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return abURL;
    }
}
