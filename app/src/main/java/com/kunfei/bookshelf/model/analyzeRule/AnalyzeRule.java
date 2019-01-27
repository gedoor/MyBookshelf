package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.kunfei.bookshelf.bean.BaseBookBean;
import com.kunfei.bookshelf.model.content.DefaultModel;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import retrofit2.Call;

import static com.kunfei.bookshelf.help.Constant.MAP_STRING;


/**
 * Created by REFGD.
 * 统一解析接口
 */
public class AnalyzeRule {
    private static final Pattern putPattern = Pattern.compile("@put:\\{.+?\\}", Pattern.CASE_INSENSITIVE);
    private static final Pattern getPattern = Pattern.compile("@get:\\{.+?\\}", Pattern.CASE_INSENSITIVE);

    private BaseBookBean book;
    private Object _object;
    private Boolean _isJSON;

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

    public void setContent(String body) {
        if (body == null) throw new AssertionError("Content cannot be null");
        _isJSON = StringUtils.isJsonType(body);
        if (!_isJSON) {
            _object = Jsoup.parse(body);
        } else {
            _object = body;
        }
        objectChangedXP = true;
        objectChangedJS = true;
        objectChangedJP = true;
    }

    public void setContent(Object object, boolean isJSON) {
        _object = object;
        _isJSON = isJSON;
        objectChangedXP = true;
        objectChangedJS = true;
        objectChangedJP = true;
    }

    private AnalyzeByXPath getAnalyzeByXPath() {
        if (analyzeByXPath == null || objectChangedXP) {
            analyzeByXPath = new AnalyzeByXPath();
            analyzeByXPath.parse(((Element) _object).children());
            objectChangedXP = false;
        }
        return analyzeByXPath;
    }

    private AnalyzeByJSoup getAnalyzeByJSoup() {
        if (analyzeByJSoup == null || objectChangedJS) {
            analyzeByJSoup = new AnalyzeByJSoup();
            analyzeByJSoup.parse((Element) _object);
            objectChangedJS = false;
        }
        return analyzeByJSoup;
    }

    private AnalyzeByJSonPath getAnalyzeByJSonPath() {
        if (analyzeByJSonPath == null || objectChangedJP) {
            analyzeByJSonPath = new AnalyzeByJSonPath();
            if (_object instanceof String) {
                analyzeByJSonPath.parse(String.valueOf(_object));
            } else {
                analyzeByJSonPath.parse(_object);
            }
            objectChangedJP = false;
        }
        return analyzeByJSonPath;
    }

    public List<String> getStringList(String rule) {
        return getStringList(rule, null);
    }

    public List<String> getStringList(String rule, String baseUrl) {
        List<String> stringList;
        SourceRule source = new SourceRule(rule);
        switch (source.mode) {
            case JSon:
                stringList = getAnalyzeByJSonPath().readStringList(source.rule);
                break;
            case XPath:
                stringList = getAnalyzeByXPath().getStringList(source.rule);
                break;
            default:
                stringList = getAnalyzeByJSoup().getAllResultList(source.rule);
        }
        if (!StringUtils.isTrimEmpty(baseUrl)) {
            List<String> urlList = new ArrayList<>();
            for (String url : stringList) {
                url = NetworkUtil.getAbsoluteURL(baseUrl, url);
                if (!urlList.contains(url)) {
                    urlList.add(url);
                }
            }
            return urlList;
        }
        return stringList;
    }

    public String getString(String rule) {
        return getString(rule, null);
    }

    public String getString(String rule, String _baseUrl) {
        if (StringUtils.isTrimEmpty(rule)) {
            return null;
        }
        String result = "";
        SourceRule source = new SourceRule(rule);
        if (!StringUtils.isTrimEmpty(source.rule)) {
            switch (source.mode) {
                case JSon:
                    result = getAnalyzeByJSonPath().read(source.rule);
                    break;
                case XPath:
                    result = getAnalyzeByXPath().getString(source.rule, _baseUrl);
                    break;
                case Default:
                    if (TextUtils.isEmpty(_baseUrl)) {
                        result = getAnalyzeByJSoup().getResult(source.rule);
                    } else {
                        result = getAnalyzeByJSoup().getResultUrl(source.rule);
                    }
            }
        } else {
            result = String.valueOf(_object);
        }
        if (!StringUtils.isTrimEmpty(source.js)) {
            result = (String) evalJS(source.js, result, _baseUrl);
        }
        if (!StringUtils.isTrimEmpty(_baseUrl)) {
            result = NetworkUtil.getAbsoluteURL(_baseUrl, result);
        }
        return result;
    }

    public AnalyzeCollection getElements(String rule) {
        AnalyzeCollection collection;
        SourceRule source = new SourceRule(rule);
        if (!StringUtils.isTrimEmpty(source.rule)) {
            switch (source.mode) {
                case JSon:
                    collection = new AnalyzeCollection(getAnalyzeByJSonPath().readList(source.rule), true);
                    break;
                case XPath:
                    collection = new AnalyzeCollection(getAnalyzeByXPath().getElements(source.rule));
                    break;
                default:
                    collection = new AnalyzeCollection(getAnalyzeByJSoup().getElements(source.rule));
            }
            if (!StringUtils.isTrimEmpty(source.js)) {
                collection = (AnalyzeCollection) evalJS(source.js, collection, null);
            }
            return collection;
        } else if (!StringUtils.isTrimEmpty(source.js)) {
            return (AnalyzeCollection) evalJS(source.js, _object, null);
        }
        return null;
    }

    private void analyzeVariable(Map<String, String> putVariable) {
        for (Map.Entry<String, String> entry : putVariable.entrySet()) {
            if (book != null) {
                book.putVariable(entry.getKey(), getString(entry.getValue()));
            }
        }
    }

    class SourceRule {
        Mode mode;
        String rule;
        String js;

        SourceRule(String ruleStr) {
            //分离put规则
            Matcher putMatcher = putPattern.matcher(ruleStr);
            if (putMatcher.find()) {
                String find = putMatcher.group(0);
                ruleStr = ruleStr.replace(find, "");
                find = find.substring(5);
                try {
                    Map<String, String> putVariable = new Gson().fromJson(find, MAP_STRING);
                    analyzeVariable(putVariable);
                } catch (Exception ignored) {
                }
            }
            //替换get值
            Matcher getMatcher = getPattern.matcher(ruleStr);
            while (getMatcher.find()) {
                String find = getMatcher.group();
                String value = "";
                if (book != null && book.getVariableMap() != null) {
                    value = book.getVariableMap().get(find.substring(6, find.length() - 1));
                    if (value == null) value = "";
                }
                ruleStr = ruleStr.replace(find, value);
            }
            String str[] = ruleStr.split("@js:");
            if (StringUtils.startWithIgnoreCase(str[0], "@XPath:")) {
                mode = Mode.XPath;
                rule = str[0].substring(7);
            } else if (StringUtils.startWithIgnoreCase(str[0], "@JSon:")) {
                mode = Mode.JSon;
                rule = str[0].substring(6);
            } else {
                if (_isJSON) {
                    mode = Mode.JSon;
                } else {
                    mode = Mode.Default;
                }
                rule = str[0];
            }
            if (str.length > 1) {
                js = str[1];
            }
        }

    }

    private enum Mode {
        XPath, JSon, Default
    }

    private static class EngineHelper {
        private static final ScriptEngine INSTANCE = new ScriptEngineManager().getEngineByName("rhino");
    }

    private Object evalJS(String jsStr, Object result, String baseUrl) {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", this);
        bindings.put("result", result);
        bindings.put("baseUrl", baseUrl);
        try {
            result = EngineHelper.INSTANCE.eval(jsStr, bindings);
        } catch (ScriptException ignored) {
        }
        return result;
    }

    /**
     * js实现跨域访问,不能删
     */
    public String ajax(String url) {
        try {
            Call<String> call = DefaultModel.getRetrofitString(url)
                    .create(IHttpGetApi.class).getWebContentCall(url, null);
            return call.execute().body();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * js实现解码,不能删
     */
    public String base64Decoder(String base64) {
        return new String(Base64.decode(base64.getBytes(), Base64.DEFAULT));
    }
}
