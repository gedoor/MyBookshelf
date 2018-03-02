package com.monke.monkeybook.model.content;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

class AnalyzeHeaders {

    static Map<String, String> getMap(String headerStr) {
        Map<String, String> headerMap = new HashMap<>();
        if (isEmpty(headerStr)) {
            headerMap.put("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
            return headerMap;
        }
        String headerStrS[] = headerStr.split(",");
        for (String hs : headerStrS) {
            String key = hs.substring(1, hs.indexOf(":"));
            String value = hs.substring(hs.indexOf(":")+1);
            headerMap.put(key, value);
        }
        return headerMap;
    }
}
