package com.kunfei.bookshelf.model.analyzeRule;

import static com.kunfei.bookshelf.constant.AppConstant.DEFAULT_USER_AGENT;

import android.content.SharedPreferences;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

public class AnalyzeHeaders {
    private static SharedPreferences preferences = MApplication.getConfigPreferences();

    public static Map<String, String> getDefaultHeader() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("User-Agent", getDefaultUserAgent());
        return headerMap;
    }

    public static String getDefaultUserAgent() {
        return preferences.getString(MApplication.getInstance().getString(R.string.pk_user_agent), DEFAULT_USER_AGENT);
    }
}
