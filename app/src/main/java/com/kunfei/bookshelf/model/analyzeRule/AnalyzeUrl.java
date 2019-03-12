package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.kunfei.bookshelf.utils.StringUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;
import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;

/**
 * Created by GKF on 2018/1/24.
 * 搜索URL规则解析
 */

public class AnalyzeUrl {
    private static final Pattern headerPattern = Pattern.compile("@Header:\\{.+?\\}", Pattern.CASE_INSENSITIVE);
    private static final Pattern pagePattern = Pattern.compile("(?<=\\{).+?(?=\\})");

    private String url;
    private String hostUrl;
    private String urlPath;
    private String queryStr;
    private Map<String, String> queryMap = new HashMap<>();
    private Map<String, String> headerMap = new HashMap<>();
    private String charCode;
    private UrlMode urlMode = UrlMode.DEFAULT;

    public AnalyzeUrl(String ruleUrl, final String key, final Integer page, Map<String, String> headerMapF) throws Exception {
        //解析Header
        ruleUrl = analyzeHeader(ruleUrl, headerMapF);
        //替换关键字
        if (!StringUtils.isTrimEmpty(key)) {
            ruleUrl = ruleUrl.replace("searchKey", key);
        }
        //分离编码规则
        String[] ruleUrlS = ruleUrl.split("\\|");
        if (ruleUrlS.length > 1) {
            analyzeOther(ruleUrlS[1]);
        }
        //设置页数
        if (page != null) {
            setPage(ruleUrlS, page);
        }
        //分离post参数
        ruleUrlS = ruleUrlS[0].split("@");
        if (ruleUrlS.length > 1) {
            urlMode = UrlMode.POST;
        } else {
            //分离get参数
            ruleUrlS = ruleUrlS[0].split("\\?");
            if (ruleUrlS.length > 1) {
                urlMode = UrlMode.GET;
            }
        }
        generateUrlPath(ruleUrlS[0]);
        if (urlMode != UrlMode.DEFAULT) {
            analyzeQuery(ruleUrlS[1]);
        }
    }

    /**
     * 解析Header
     */
    private String analyzeHeader(String ruleUrl, Map<String, String> headerMapF) {
        headerMap.putAll(headerMapF);
        Matcher matcher = headerPattern.matcher(ruleUrl);
        if (matcher.find()) {
            String find = matcher.group(0);
            ruleUrl = ruleUrl.replace(find, "");
            find = find.substring(8);
            try {
                Map<String, String> map = new Gson().fromJson(find, MAP_STRING);
                headerMap.putAll(map);
            } catch (Exception ignored) {
            }
        }
        return ruleUrl;
    }

    /**
     * 解析页数
     */
    private void setPage(final String[] ruleUrlS, final int searchPage) {
        Matcher matcher = pagePattern.matcher(ruleUrlS[0]);
        if (matcher.find()) {
            String[] pages = matcher.group(0).split(",");
            if (searchPage <= pages.length) {
                ruleUrlS[0] = ruleUrlS[0].replaceAll("\\{.*?\\}", pages[searchPage - 1].trim());
            } else {
                ruleUrlS[0] = ruleUrlS[0].replaceAll("\\{.*?\\}", pages[pages.length - 1].trim());
            }
        }
        ruleUrlS[0] = ruleUrlS[0].replace("searchPage-1", String.valueOf(searchPage - 1))
                .replace("searchPage+1", String.valueOf(searchPage + 1))
                .replace("searchPage", String.valueOf(searchPage));
    }

    /**
     * 解析编码规则
     */
    private void analyzeOther(final String qtRule) {
        if (TextUtils.isEmpty(qtRule)) return;
        String[] qtS = qtRule.split("&");
        for (String qt : qtS) {
            String[] gz = qt.split("=");
            if (gz[0].equals("char")) {
                charCode = gz[1];
            }
        }
    }

    /**
     * 解析QueryMap
     */
    private void analyzeQuery(String allQuery) throws Exception {
        if (isEmpty(charCode)) {
            queryStr = URLEncoder.encode(allQuery, "UTF-8");
        } else {
            queryStr = URLEncoder.encode(allQuery, charCode);
        }
        String[] queryS = allQuery.split("&");
        for (String query : queryS) {
            String[] queryM = query.split("=");
            String value = queryM.length > 1 ? queryM[1] : "";
            if (isEmpty(charCode)) {
                queryMap.put(queryM[0], value);
            } else if (charCode.equals("escape")) {
                queryMap.put(queryM[0], StringUtils.escape(value));
            } else {
                queryMap.put(queryM[0], URLEncoder.encode(value, charCode));
            }
        }
    }

    private void generateUrlPath(String ruleUrl) {
        url = ruleUrl;
        hostUrl = StringUtils.getBaseUrl(ruleUrl);
        urlPath = ruleUrl.substring(hostUrl.length());
    }

    public String getHost() {
        return hostUrl;
    }

    public String getPath() {
        return urlPath;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public String getQueryStr() {
        return queryStr;
    }

    public UrlMode getUrlMode() {
        return urlMode;
    }

    public enum UrlMode {
        GET, POST, DEFAULT
    }
}
