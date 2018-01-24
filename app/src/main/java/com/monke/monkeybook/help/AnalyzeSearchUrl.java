package com.monke.monkeybook.help;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GKF on 2018/1/24.
 */

public class AnalyzeSearchUrl {
    private String searchUrl;
    private String searchPath;
    private Map<String, String> queryMap;

    public AnalyzeSearchUrl(String ruleUrl, String key, int page) throws MalformedURLException {
        String[] temp = ruleUrl.split("\\?");
        URL url = new URL(temp[0]);
        String[] queryS = temp[1].split("&");
        for (String query : queryS) {
            String[] queryM = query.split("=");
            switch (queryM[1]) {
                case "searchKey":
                    queryMap.put(queryM[0], key);
                    break;
                case "searchPage":
                    queryMap.put(queryM[0], String.valueOf(page));
                    break;
                default:
                    queryMap.put(queryM[0], queryM[1]);
                    break;
            }
        }
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public String getSearchPath() {
        return searchPath;
    }

    public Map<String, String> getQueryMap() {
        return queryMap;
    }
}
