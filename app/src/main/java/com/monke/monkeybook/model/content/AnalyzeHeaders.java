package com.monke.monkeybook.model.content;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

class AnalyzeHeaders {
    private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());

    static Map<String, String> getMap(String headerStr) {
        Map<String, String> headerMap = new HashMap<>();
        if (isEmpty(headerStr)) {
            headerMap.put("User-Agent", getDefaultUserAgent());
            return headerMap;
        }
        String headerStrS[] = headerStr.split(",");
        boolean hasUserAgent = false;
        for (String hs : headerStrS) {
            String key = hs.substring(1, hs.indexOf(":"));
            String value = hs.substring(hs.indexOf(":")+1);
            headerMap.put(key, value);
            if (key.equalsIgnoreCase("User-Agent")) {
                hasUserAgent = true;
            }
        }
        if (!hasUserAgent) {
            headerMap.put("User-Agent", getDefaultUserAgent());
        }
        return headerMap;
    }

    private static String getDefaultUserAgent() {
        return preferences.getString(MApplication.getInstance().getString(R.string.pk_user_agent),
                MApplication.getInstance().getString(R.string.pv_user_agent));
    }
}
