package com.kunfei.bookshelf.model.analyzeRule;

import static com.kunfei.bookshelf.constant.AppConstant.EXP_PATTERN;
import static com.kunfei.bookshelf.constant.AppConstant.JS_PATTERN;
import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;
import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;
import static com.kunfei.bookshelf.utils.NetworkUtils.headerPattern;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.help.JsExtensions;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.UrlEncoderUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.SimpleBindings;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by GKF on 2018/1/24.
 * 搜索URL规则解析
 */
@Keep
public class AnalyzeUrl implements JsExtensions {
    private static final Pattern pagePattern = Pattern.compile("(?<!@)\\{(.*?)\\}");
    private final BookSourceBean bookSource;
    private String baseUrl;
    private String ruleUrl;
    private String url;
    private String host;
    private String urlPath;
    private String queryStr;
    private final Map<String, String> queryMap = new LinkedHashMap<>();
    private final Map<String, String> headerMap = new HashMap<>();
    private String charCode = null;
    private UrlMode urlMode = UrlMode.DEFAULT;
    private String jsonBody = null;
    private final String searchKey;
    private final int searchPage;

    public AnalyzeUrl(String urlRule, Map<String, String> headerMap) throws Exception {
        this(urlRule, null, headerMap);
    }

    public AnalyzeUrl(String urlRule, String baseUrl, Map<String, String> headerMap) throws Exception {
        this(urlRule, baseUrl, null, headerMap);
    }

    public AnalyzeUrl(String urlRule, String baseUrl, BookSourceBean bookSource, Map<String, String> headerMap) throws Exception {
        this(urlRule, baseUrl, bookSource, null, 1, headerMap);
    }

    @SuppressLint("DefaultLocale")
    public AnalyzeUrl(String urlRule, String baseUrl, BookSourceBean bookSource, final String key, final int page, Map<String, String> headerMap) throws Exception {
        if (!TextUtils.isEmpty(baseUrl)) {
            this.baseUrl = headerPattern.matcher(baseUrl).replaceAll("");
        }
        this.bookSource = bookSource;
        this.searchKey = key;
        this.searchPage = page;
        ruleUrl = urlRule;
        //替换关键字
        if (!StringUtils.isTrimEmpty(key)) {
            // 处理searchKey=searchKey的情况
            if (ruleUrl.matches("=[\\s{(]*searchKey"))
                ruleUrl = ruleUrl.replaceFirst("=[\\s{(]*searchKey", "=" + key);
            else
                ruleUrl = ruleUrl.replace("searchKey", key);
        }
        //判断是否有下一页
        if (page > 1 && !ruleUrl.contains("searchPage"))
            throw new Exception("没有下一页");
        //替换js
        ruleUrl = replaceJs(ruleUrl);
        //解析Header
        ruleUrl = analyzeHeader(ruleUrl, headerMap);
        //分离编码规则
        ruleUrl = splitCharCode(ruleUrl);
        //设置页数
        ruleUrl = analyzePage(ruleUrl, page);
        //执行规则列表
        List<String> ruleList = splitRule(ruleUrl);
        for (String rule : ruleList) {
            if (rule.startsWith("<js>")) {
                rule = rule.substring(4, rule.lastIndexOf("<"));
                ruleUrl = (String) evalJS(rule, ruleUrl);
            } else if (rule.startsWith("@js:")) {
                rule = rule.substring(4);
                ruleUrl = (String) evalJS(rule, ruleUrl);
            } else {
                ruleUrl = rule.replace("@result", ruleUrl);
            }
        }
        //分离post参数
        String[] ruleUrlS = ruleUrl.split("@");
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
        if (urlMode == UrlMode.GET) {
            analyzeQuery(ruleUrlS[1]);
        } else if (urlMode == UrlMode.POST) {
            if (StringUtils.isJsonType(ruleUrlS[1])) {
                jsonBody = ruleUrlS[1];
            } else {
                analyzeQuery(ruleUrlS[1]);
            }
        }
    }

    /**
     * 解析Header
     */
    private String analyzeHeader(String ruleUrl, Map<String, String> headerMapF) {
        if (headerMapF != null) {
            headerMap.putAll(headerMapF);
        }
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
     * 分离编码规则
     */
    private String splitCharCode(String rule) {
        String[] ruleUrlS = rule.split("\\|");
        if (ruleUrlS.length > 1) {
            if (!TextUtils.isEmpty(ruleUrlS[1])) {
                String[] qtS = ruleUrlS[1].split("&");
                for (String qt : qtS) {
                    String[] gz = qt.split("=");
                    if (gz[0].equals("char")) {
                        charCode = gz[1];
                    }
                }
            }
        }
        return ruleUrlS[0];
    }

    /**
     * 解析页数
     */
    private String analyzePage(String ruleUrl, final Integer searchPage) {
        if (searchPage == null) return ruleUrl;
        Matcher matcher = pagePattern.matcher(ruleUrl);
        while (matcher.find()) {
            String[] pages = matcher.group(1).split(",");
            if (searchPage <= pages.length) {
                ruleUrl = ruleUrl.replace(matcher.group(), pages[searchPage - 1].trim());
            } else {
                ruleUrl = ruleUrl.replace(matcher.group(), pages[pages.length - 1].trim());
            }
        }
        return ruleUrl.replace("searchPage-1", String.valueOf(searchPage - 1))
                .replace("searchPage+1", String.valueOf(searchPage + 1))
                .replace("searchPage", String.valueOf(searchPage));
    }

    /**
     * 替换js
     */
    @SuppressLint("DefaultLocale")
    private String replaceJs(String ruleUrl) throws Exception {
        if (ruleUrl.contains("{{") && ruleUrl.contains("}}")) {
            Object jsEval;
            StringBuffer sb = new StringBuffer(ruleUrl.length());
            Matcher expMatcher = EXP_PATTERN.matcher(ruleUrl);
            while (expMatcher.find()) {
                jsEval = evalJS(expMatcher.group(1), ruleUrl);
                if (jsEval instanceof String) {
                    expMatcher.appendReplacement(sb, (String) jsEval);
                } else if (jsEval instanceof Double && ((Double) jsEval) % 1.0 == 0) {
                    expMatcher.appendReplacement(sb, String.format("%.0f", (Double) jsEval));
                } else {
                    expMatcher.appendReplacement(sb, String.valueOf(jsEval));
                }
            }
            expMatcher.appendTail(sb);
            ruleUrl = sb.toString();
        }
        return ruleUrl;
    }

    /**
     * 解析QueryMap
     */
    private void analyzeQuery(String allQuery) throws Exception {
        queryStr = allQuery;
        String[] queryS = allQuery.split("&");
        for (String query : queryS) {
            String[] queryM = query.split("=");
            String value = queryM.length > 1 ? queryM[1] : "";
            if (TextUtils.isEmpty(charCode)) {
                if (UrlEncoderUtils.hasUrlEncoded(value)) {
                    queryMap.put(queryM[0], value);
                } else {
                    queryMap.put(queryM[0], URLEncoder.encode(value, "UTF-8"));
                }
            } else if (charCode.equals("escape")) {
                queryMap.put(queryM[0], StringUtils.escape(value));
            } else {
                queryMap.put(queryM[0], URLEncoder.encode(value, charCode));
            }
        }
    }

    /**
     * 拆分规则
     */
    private List<String> splitRule(String ruleStr) {
        List<String> ruleList = new ArrayList<>();
        Matcher jsMatcher = JS_PATTERN.matcher(ruleStr);
        int start = 0;
        String tmp;
        while (jsMatcher.find()) {
            if (jsMatcher.start() > start) {
                tmp = ruleStr.substring(start, jsMatcher.start()).replaceAll("\n", "").trim();
                if (!TextUtils.isEmpty(tmp)) {
                    ruleList.add(tmp);
                }
            }
            ruleList.add(jsMatcher.group());
            start = jsMatcher.end();
        }
        if (ruleStr.length() > start) {
            tmp = ruleStr.substring(start).replaceAll("\n", "").trim();
            if (!TextUtils.isEmpty(tmp)) {
                ruleList.add(tmp);
            }
        }
        return ruleList;
    }

    /**
     * 分解URL
     */
    private void generateUrlPath(String ruleUrl) {
        url = NetworkUtils.getAbsoluteURL(baseUrl, ruleUrl);
        host = StringUtils.getBaseUrl(url);
        urlPath = url.substring(host.length());
    }

    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, Object result) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", this);
        bindings.put("baseUrl", baseUrl);
        bindings.put("searchPage", searchPage);
        bindings.put("searchKey", searchKey);
        bindings.put("source", bookSource);
        bindings.put("result", result);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }

    public String getCharCode() {
        return charCode;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return urlPath;
    }

    public String getRuleUrl() {
        return ruleUrl;
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

    public String getJsonBody() {
        return jsonBody;
    }

    public byte[] getPostData() {
        StringBuilder builder = new StringBuilder();
        Set<String> keys = queryMap.keySet();
        for (String key : keys) {
            builder.append(String.format("%s=%s&", key, queryMap.get(key)));
        }
        builder.deleteCharAt(builder.lastIndexOf("&"));
        return builder.toString().getBytes();
    }

    public RequestBody getPostBody() {
        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        return RequestBody.create(mediaType, jsonBody);
    }

    public UrlMode getUrlMode() {
        return urlMode;
    }

    public enum UrlMode {
        GET, POST, DEFAULT
    }
}
