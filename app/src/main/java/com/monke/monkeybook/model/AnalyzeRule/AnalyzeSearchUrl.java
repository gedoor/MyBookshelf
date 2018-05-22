package com.monke.monkeybook.model.AnalyzeRule;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/24.
 * 搜索URL规则解析
 */

public class AnalyzeSearchUrl {
    private String searchUrl;
    private String searchPath;
    private Map<String, String> queryMap;
    private String searchKey;
    private int searchPage;
    private String charCode;

    public AnalyzeSearchUrl(final String ruleUrl, final String key, final int page) throws Exception {
        searchKey = key;
        searchPage = page;
        String[] ruleUrlS = ruleUrl.split("\\|");
        if (ruleUrlS.length > 1) {
            analyzeQt(ruleUrlS[1]);
        }
        ruleUrlS[0] = setPage(ruleUrlS[0]);
        ruleUrlS = ruleUrlS[0].split("@");
        if (ruleUrlS.length == 1) {
            ruleUrlS = ruleUrlS[0].split("\\?");
        }
        if (ruleUrlS.length == 1) {
            String url = ruleUrlS[0].replace("searchKey", searchKey)
                    .replace("searchPage-1", String.valueOf(page - 1))
                    .replace("searchPage", String.valueOf(page));
            generateUrlPath(url);
            return;
        }
        generateUrlPath(ruleUrlS[0]);
        queryMap = getQueryMap(ruleUrlS[1]);
    }

    private String setPage(String urlStr) {
        Pattern pattern = Pattern.compile("(?<=\\{).+?(?=\\})");
        Matcher matcher = pattern.matcher(urlStr);
        if (matcher.find()) {
            String[] pages = matcher.group(0).split(",");
            if (searchPage <= pages.length) {
                urlStr = urlStr.replaceAll("\\{.*?\\}", pages[searchPage-1].trim());
            } else {
                urlStr = urlStr.replaceAll("\\{.*?\\}", pages[pages.length-1].trim());
            }
        }
        return urlStr;
    }

    private void analyzeQt(final String qtRule) throws Exception {
        String[] qtS = qtRule.split("&");
        for (String qt : qtS) {
            String[] gz = qt.split("=");
            if (gz[0].equals("char")) {
                charCode = gz[1];
                searchKey = URLEncoder.encode(searchKey, charCode);
            }
        }
    }

    private Map<String, String> getQueryMap(String allQuery) throws Exception {
        String[] queryS = allQuery.split("&");
        Map<String, String> map = new HashMap<>();
        for (String query : queryS) {
            String[] queryM = query.split("=");
            switch (queryM[1]) {
                case "searchKey":
                    map.put(queryM[0], searchKey);
                    break;
                case "searchPage":
                    map.put(queryM[0], String.valueOf(searchPage));
                    break;
                case "searchPage-1":
                    map.put(queryM[0], String.valueOf(searchPage - 1));
                    break;
                default:
                    if (isEmpty(charCode)) {
                        map.put(queryM[0], queryM[1]);
                    } else {
                        map.put(queryM[0], URLEncoder.encode(queryM[1], charCode));
                    }
                    break;
            }
        }
        return map;
    }

    private void generateUrlPath(String ruleUrl) throws Exception {
        URL url = new URL(ruleUrl);
        searchUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
        searchPath = ruleUrl.replace(searchUrl, "");
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
