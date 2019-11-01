package com.kunfei.bookshelf.model.analyzeRule;

import android.annotation.SuppressLint;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BaseBookBean;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.SimpleBindings;

import retrofit2.Response;

import static android.text.TextUtils.isEmpty;
import static com.kunfei.bookshelf.constant.AppConstant.EXP_PATTERN;
import static com.kunfei.bookshelf.constant.AppConstant.JS_PATTERN;
import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;
import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;
import static com.kunfei.bookshelf.utils.NetworkUtils.headerPattern;


/**
 * Created by REFGD.
 * 统一解析接口
 */
@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
public class AnalyzeRule {
    private static final Pattern putPattern = Pattern.compile("@put:(\\{[^}]+?\\})", Pattern.CASE_INSENSITIVE);
    private static final Pattern getPattern = Pattern.compile("@get:\\{([^}]+?)\\}", Pattern.CASE_INSENSITIVE);

    private BaseBookBean book;
    private Object object;
    private Boolean isJSON = false;
    private String baseUrl = null;

    private AnalyzeByXPath analyzeByXPath = null;
    private AnalyzeByJSoup analyzeByJSoup = null;
    private AnalyzeByJSonPath analyzeByJSonPath = null;

    private boolean objectChangedXP = false;
    private boolean objectChangedJS = false;
    private boolean objectChangedJP = false;

    public AnalyzeRule(BaseBookBean bookBean) {
        book = bookBean;
    }

    public void setBook(BaseBookBean book) {
        this.book = book;
    }

    public AnalyzeRule setContent(Object body) {
        return setContent(body, baseUrl);
    }

    public Object getContent() {
        return object;
    }

    public AnalyzeRule setContent(Object body, String baseUrl) {
        if (body == null) throw new AssertionError("Content cannot be null");
        isJSON = StringUtils.isJsonType(String.valueOf(body));
        object = body;
        if (baseUrl != null) {
            this.baseUrl = headerPattern.matcher(baseUrl).replaceAll("");
        }
        objectChangedXP = true;
        objectChangedJS = true;
        objectChangedJP = true;
        return this;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * 获取XPath解析类
     */
    private AnalyzeByXPath getAnalyzeByXPath(Object o) {
        if (o != object) {
            return new AnalyzeByXPath().parse(o);
        }
        return getAnalyzeByXPath();
    }

    private AnalyzeByXPath getAnalyzeByXPath() {
        if (analyzeByXPath == null || objectChangedXP) {
            analyzeByXPath = new AnalyzeByXPath();
            analyzeByXPath.parse(object);
            objectChangedXP = false;
        }
        return analyzeByXPath;
    }

    /**
     * 获取JSOUP解析类
     */
    private AnalyzeByJSoup getAnalyzeByJSoup(Object o) {
        if (o != object) {
            return new AnalyzeByJSoup().parse(o);
        }
        return getAnalyzeByJSoup();
    }

    private AnalyzeByJSoup getAnalyzeByJSoup() {
        if (analyzeByJSoup == null || objectChangedJS) {
            analyzeByJSoup = new AnalyzeByJSoup();
            analyzeByJSoup.parse(object);
            objectChangedJS = false;
        }
        return analyzeByJSoup;
    }

    /**
     * 获取JSON解析类
     */
    private AnalyzeByJSonPath getAnalyzeByJSonPath(Object o) {
        if (o != object) {
            return new AnalyzeByJSonPath().parse(o);
        }
        return getAnalyzeByJSonPath();
    }

    private AnalyzeByJSonPath getAnalyzeByJSonPath() {
        if (analyzeByJSonPath == null || objectChangedJP) {
            analyzeByJSonPath = new AnalyzeByJSonPath();
            analyzeByJSonPath.parse(object);
            objectChangedJP = false;
        }
        return analyzeByJSonPath;
    }

    /**
     * 获取文本列表
     */
    public List<String> getStringList(String rule) throws Exception {
        return getStringList(rule, false);
    }

    public List<String> getStringList(String rule, boolean isUrl) throws Exception {
        if (isEmpty(rule)) return null;
        List<SourceRule> ruleList = splitSourceRule(rule);
        return getStringList(ruleList, isUrl);
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getStringList(List<SourceRule> ruleList, boolean isUrl) throws Exception {
        Object result = null;
        if (!ruleList.isEmpty()) result = object;
        for (SourceRule rule : ruleList) {
            if (!isEmpty(rule.rule)) {
                switch (rule.mode) {
                    case Js:
                        result = evalJS(rule.rule, result);
                        break;
                    case JSon:
                        result = getAnalyzeByJSonPath(result).getStringList(rule.rule);
                        break;
                    case XPath:
                        result = getAnalyzeByXPath(result).getStringList(rule.rule);
                        break;
                    default:
                        result = getAnalyzeByJSoup(result).getStringList(rule.rule);
                }
            }
            if (!isEmpty(rule.replaceRegex) && result instanceof List) {
                List<String> newList = new ArrayList<>();
                for (Object item : (List) result) {
                    newList.add(replaceRegex(String.valueOf(item), rule));
                }
                result = newList;
            } else if (!isEmpty(rule.replaceRegex)) {
                result = replaceRegex(String.valueOf(result), rule);
            }
        }
        if (result == null) return new ArrayList<>();
        if (result instanceof String) {
            result = Arrays.asList(StringUtils.formatHtml((String) result).split("\n"));
        }
        if (isUrl && !isEmpty(baseUrl)) {
            List<String> urlList = new ArrayList<>();
            for (Object url : (List) result) {
                String absoluteURL = NetworkUtils.getAbsoluteURL(baseUrl, String.valueOf(url));
                if (!urlList.contains(absoluteURL) && !isEmpty(absoluteURL)) {
                    urlList.add(absoluteURL);
                }
            }
            return urlList;
        }
        return (List<String>) result;
    }

    /**
     * 获取文本
     */
    public String getString(String rule) throws Exception {
        return getString(rule, false);
    }

    public String getString(String ruleStr, boolean isUrl) throws Exception {
        if (isEmpty(ruleStr)) return null;
        List<SourceRule> ruleList = splitSourceRule(ruleStr);
        return getString(ruleList, isUrl);
    }

    public String getString(List<SourceRule> ruleList) throws Exception {
        return getString(ruleList, false);
    }

    public String getString(List<SourceRule> ruleList, boolean isUrl) throws Exception {
        Object result = null;
        if (!ruleList.isEmpty()) result = object;
        for (SourceRule rule : ruleList) {
            if (!StringUtils.isTrimEmpty(rule.rule)) {
                switch (rule.mode) {
                    case Js:
                        result = evalJS(rule.rule, result);
                        break;
                    case JSon:
                        result = getAnalyzeByJSonPath(result).getString(rule.rule);
                        break;
                    case XPath:
                        result = getAnalyzeByXPath(result).getString(rule.rule);
                        break;
                    case Default:
                        if (isUrl && !isEmpty(baseUrl)) {
                            result = getAnalyzeByJSoup(result).getString0(rule.rule);
                        } else {
                            result = getAnalyzeByJSoup(result).getString(rule.rule);
                        }
                }
            }
            if (!isEmpty(rule.replaceRegex)) {
                result = replaceRegex(String.valueOf(result), rule);
            }
        }
        if (result == null) return "";
        if (isUrl && !StringUtils.isTrimEmpty(baseUrl)) {
            return NetworkUtils.getAbsoluteURL(baseUrl, String.valueOf(result));
        }
        return String.valueOf(result);
    }

    /**
     * 获取Element
     */
    public Object getElement(String ruleStr) throws Exception {
        List<SourceRule> ruleList = splitSourceRule(ruleStr);
        Object result = object;
        for (SourceRule rule : ruleList) {
            switch (rule.mode) {
                case Js:
                    result = evalJS(rule.rule, result);
                    break;
                case JSon:
                    result = getAnalyzeByJSonPath(result).getObject(rule.rule);
                    break;
                case XPath:
                    result = getAnalyzeByXPath(result).getElements(rule.rule);
                    break;
                default:
                    result = getAnalyzeByJSoup(result).getElements(rule.rule);
            }
            if (!isEmpty(rule.replaceRegex) && result instanceof String) {
                result = replaceRegex(String.valueOf(result), rule);
            }
        }
        return result;
    }

    /**
     * 获取列表
     */
    @SuppressWarnings("unchecked")
    public List<Object> getElements(String ruleStr) throws Exception {
        List<SourceRule> ruleList = splitSourceRule(ruleStr);
        Object result = null;
        if (!ruleList.isEmpty()) result = object;
        for (SourceRule rule : ruleList) {
            switch (rule.mode) {
                case Js:
                    result = evalJS(rule.rule, result);
                    break;
                case JSon:
                    result = getAnalyzeByJSonPath(result).getList(rule.rule);
                    break;
                case XPath:
                    result = getAnalyzeByXPath(result).getElements(rule.rule);
                    break;
                default:
                    result = getAnalyzeByJSoup(result).getElements(rule.rule);
            }
            if (!isEmpty(rule.replaceRegex) && result instanceof String) {
                result = replaceRegex(String.valueOf(result), rule);
            }
        }
        if (result == null) {
            return new ArrayList<>();
        }
        //noinspection ConstantConditions
        return (List<Object>) result;
    }

    /**
     * 保存变量
     */
    private void putRule(Map<String, String> map) throws Exception {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (book != null) {
                book.putVariable(entry.getKey(), getString(entry.getValue()));
            }
        }
    }

    /**
     * 分离并执行put规则
     */
    private String splitPutRule(String ruleStr) throws Exception {
        Matcher putMatcher = putPattern.matcher(ruleStr);
        while (putMatcher.find()) {
            ruleStr = ruleStr.replace(putMatcher.group(), "");
            Map<String, String> map = new Gson().fromJson(putMatcher.group(1), MAP_STRING);
            putRule(map);
        }
        return ruleStr;
    }

    /**
     * 替换@get
     */
    public String replaceGet(String ruleStr) {
        Matcher getMatcher = getPattern.matcher(ruleStr);
        while (getMatcher.find()) {
            String value = "";
            if (book != null && book.getVariableMap() != null) {
                value = book.getVariableMap().get(getMatcher.group(1));
                if (value == null) value = "";
            }
            ruleStr = ruleStr.replace(getMatcher.group(), value);
        }
        return ruleStr;
    }

    /**
     * 正则替换
     */
    private String replaceRegex(String result, SourceRule rule) {
        if (!isEmpty(rule.replaceRegex)) {
            if (rule.replaceFirst) {
                Pattern pattern = Pattern.compile(rule.replaceRegex);
                Matcher matcher = pattern.matcher(String.valueOf(result));
                if (matcher.find()) {
                    result = matcher.group(0).replaceFirst(rule.replaceRegex, rule.replacement);
                } else {
                    result = "";
                }
            } else {
                result = String.valueOf(result).replaceAll(rule.replaceRegex, rule.replacement);
            }
        }
        return result;
    }

    /**
     * 替换JS
     */
    @SuppressLint("DefaultLocale")
    private String replaceJs(String ruleStr) throws Exception {
        if (ruleStr.contains("{{") && ruleStr.contains("}}")) {
            Object jsEval;
            StringBuffer sb = new StringBuffer(ruleStr.length());
            Matcher expMatcher = EXP_PATTERN.matcher(ruleStr);
            while (expMatcher.find()) {
                jsEval = evalJS(expMatcher.group(1), object);
                if (jsEval instanceof String) {
                    expMatcher.appendReplacement(sb, (String) jsEval);
                } else if (jsEval instanceof Double && ((Double) jsEval) % 1.0 == 0) {
                    expMatcher.appendReplacement(sb, String.format("%.0f", (Double) jsEval));
                } else {
                    expMatcher.appendReplacement(sb, String.valueOf(jsEval));
                }
            }
            expMatcher.appendTail(sb);
            ruleStr = sb.toString();
        }
        return ruleStr;
    }

    /**
     * 分解规则生成规则列表
     */
    public List<SourceRule> splitSourceRule(String ruleStr) throws Exception {
        List<SourceRule> ruleList = new ArrayList<>();
        if (isEmpty(ruleStr)) return ruleList;
        //检测Mode
        Mode mode;
        if (StringUtils.startWithIgnoreCase(ruleStr, "@XPath:")) {
            mode = Mode.XPath;
            ruleStr = ruleStr.substring(7);
        } else if (StringUtils.startWithIgnoreCase(ruleStr, "@JSon:")) {
            mode = Mode.JSon;
            ruleStr = ruleStr.substring(6);
        } else {
            if (isJSON) {
                mode = Mode.JSon;
            } else {
                mode = Mode.Default;
            }
        }
        //分离put规则
        ruleStr = splitPutRule(ruleStr);
        //替换get值
        ruleStr = replaceGet(ruleStr);
        //替换js
        ruleStr = replaceJs(ruleStr);
        //拆分为列表
        int start = 0;
        String tmp;
        Matcher jsMatcher = JS_PATTERN.matcher(ruleStr);
        while (jsMatcher.find()) {
            if (jsMatcher.start() > start) {
                tmp = ruleStr.substring(start, jsMatcher.start()).replaceAll("\n", "").trim();
                if (!isEmpty(tmp)) {
                    ruleList.add(new SourceRule(tmp, mode));
                }
            }
            ruleList.add(new SourceRule(jsMatcher.group(), Mode.Js));
            start = jsMatcher.end();
        }
        if (ruleStr.length() > start) {
            tmp = ruleStr.substring(start).replaceAll("\n", "").trim();
            if (!isEmpty(tmp)) {
                ruleList.add(new SourceRule(tmp, mode));
            }
        }
        return ruleList;
    }

    /**
     * 规则类
     */
    public class SourceRule {
        Mode mode;
        String rule;
        String replaceRegex = "";
        String replacement = "";
        boolean replaceFirst = false;

        SourceRule(String ruleStr, Mode mainMode) {
            this.mode = mainMode;
            if (mode == Mode.Js) {
                if (ruleStr.startsWith("<js>")) {
                    rule = ruleStr.substring(4, ruleStr.lastIndexOf("<"));
                } else {
                    rule = ruleStr.substring(4);
                }
            } else {
                if (StringUtils.startWithIgnoreCase(ruleStr, "@XPath:")) {
                    mode = Mode.XPath;
                    rule = ruleStr.substring(7);
                } else if (StringUtils.startWithIgnoreCase(ruleStr, "//")) {//XPath特征很明显,无需配置单独的识别标头
                    mode = Mode.XPath;
                    rule = ruleStr;
                } else if (StringUtils.startWithIgnoreCase(ruleStr, "@JSon:")) {
                    mode = Mode.JSon;
                    rule = ruleStr.substring(6);
                } else if (ruleStr.startsWith("$.")) {
                    mode = Mode.JSon;
                    rule = ruleStr;
                } else {
                    rule = ruleStr;
                }
                //分离正则表达式
                String[] ruleStrS = rule.trim().split("##");
                rule = ruleStrS[0];
                if (ruleStrS.length > 1) {
                    replaceRegex = ruleStrS[1];
                }
                if (ruleStrS.length > 2) {
                    replacement = ruleStrS[2];
                }
                if (ruleStrS.length > 3) {
                    replaceFirst = true;
                }
            }
        }

    }

    private enum Mode {
        XPath, JSon, Default, Js
    }

    public String put(String key, String value) {
        if (book != null) {
            book.putVariable(key, value);
        }
        return value;
    }

    public String get(String key) {
        if (book == null) {
            return null;
        }
        if (book.getVariableMap() == null) {
            return null;
        }
        return book.getVariableMap().get(key);
    }

    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, Object result) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", this);
        bindings.put("result", result);
        bindings.put("baseUrl", baseUrl);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }

    /**
     * js实现跨域访问,不能删
     */
    public String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(urlStr);
            Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                    .blockingFirst();
            return response.body();
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    /**
     * js实现解码,不能删
     */
    public String base64Decoder(String base64) {
        return StringUtils.base64Decode(base64);
    }

    /**
     * 章节数转数字
     */
    public String toNumChapter(String s) {
        if (s == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("(第)(.+?)(章)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(1) + StringUtils.stringToInt(matcher.group(2)) + matcher.group(3);
        }
        return s;
    }

}
