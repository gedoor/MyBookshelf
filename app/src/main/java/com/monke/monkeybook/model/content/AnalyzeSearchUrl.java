package com.monke.monkeybook.model.content;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

    public AnalyzeSearchUrl(String ruleUrl, String key, int page) throws Exception {
        String[] bm = ruleUrl.split("\\|");
        if (bm.length > 1) {
            String[] qtS = bm[1].split("&");
            for (String qt : qtS) {
                String[] gz = qt.split("=");
                if (gz[0].equals("char")) {
                    key = URLEncoder.encode(key, gz[1]);
                }
            }
        }
        String[] temp = bm[0].split("\\?|@");
        if (temp.length == 1) {
            String url = temp[0].replace("searchKey", key)
                    .replace("searchPage-1", String.valueOf(page - 1))
                    .replace("searchPage", String.valueOf(page));
            generateUrlPath(ruleUrl);
            return;
        }
        generateUrlPath(temp[0]);
        generateQueryMap(temp[1], key, page);
    }

    private void generateQueryMap(String allQuery, String key, int page) {
        String[] queryS = allQuery.split("&");
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

    private void generateUrlPath(String ruleUrl) throws Exception {
        URL url = new URL(ruleUrl);
        searchUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
        searchPath = url.getPath();
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
