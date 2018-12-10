package com.kunfei.bookshelf.model.analyzeRule;

import android.content.SharedPreferences;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

public class AnalyzeHeaders {
    private static SharedPreferences preferences = MApplication.getInstance().getConfigPreferences();

    public static Map<String, String> getMap(String userAgent) {
        Map<String, String> headerMap = new HashMap<>();
        if (isEmpty(userAgent)) {
            headerMap.put("User-Agent", getDefaultUserAgent());
            return headerMap;
        } else {
            headerMap.put("User-Agent", userAgent);
        }
        return headerMap;
    }

    public static String getUserAgent(String userAgent) {
        if (isEmpty(userAgent)) {
            return getDefaultUserAgent();
        } else {
            return userAgent;
        }
    }

    private static String getDefaultUserAgent() {
        return preferences.getString(MApplication.getInstance().getString(R.string.pk_user_agent),
                MApplication.getInstance().getString(R.string.pv_user_agent));
    }
}
