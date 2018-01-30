package com.monke.monkeybook.model.content;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GKF on 2018/1/24.
 * 搜索URL规则解析
 */

public class AnalyzeSearchUrl {
    private String searchUrl;
    private String searchPath;
    private Map<String, String> queryMap;

    public AnalyzeSearchUrl(String ruleUrl, String key, int page) throws MalformedURLException {
        String[] temp = ruleUrl.split("\\?|@");
        if (temp.length != 2) {
            return;
        }
        URL url = new URL(temp[0]);
        searchUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
        searchPath = url.getPath();
        String[] queryS = temp[1].split("&");
        queryMap = new HashMap<>();
        for (String query : queryS) {
            String[] queryM = query.split("=");
            switch (queryM[1]) {
                case "searchKey":
                    queryMap.put(queryM[0], key);
                    break;
                case "searchPage":
                    queryMap.put(queryM[0], String.valueOf(page));
                    break;
                case "searchPage-1":
                    queryMap.put(queryM[0], String.valueOf(page - 1));
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
