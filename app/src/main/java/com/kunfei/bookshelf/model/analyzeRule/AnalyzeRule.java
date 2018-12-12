package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import static android.text.TextUtils.isEmpty;


/**
 * Created by REFGD.
 * 统一解析接口
 */
public class AnalyzeRule {

    private Object _object;
    private Boolean _isJSON;


    private AnalyzeByXPath analyzeByXPath = null;
    private AnalyzeByJSoup analyzeByJSoup = null;
    private AnalyzeByJSonPath analyzeByJSonPath = null;

    public AnalyzeRule() {

    }

    AnalyzeRule(Object object, boolean isJSON) {
        _object = object;
        _isJSON = isJSON;
    }

    private AnalyzeByXPath getAnalyzeByXPath() {
        if (analyzeByXPath == null) {
            analyzeByXPath = new AnalyzeByXPath();
            analyzeByXPath.parse(((Element) _object).children());
        }
        return analyzeByXPath;
    }

    private AnalyzeByJSoup getAnalyzeByJSoup() {
        if (analyzeByJSoup == null) {
            analyzeByJSoup = new AnalyzeByJSoup();
            analyzeByJSoup.parse((Element) _object);
        }
        return analyzeByJSoup;
    }

    private AnalyzeByJSonPath getAnalyzeByJSonPath() {
        if (analyzeByJSonPath == null) {
            analyzeByJSonPath = new AnalyzeByJSonPath();
            if (_object instanceof String) {
                analyzeByJSonPath.parse(String.valueOf(_object));
            } else {
                analyzeByJSonPath.parse(_object);
            }
        }
        return analyzeByJSonPath;
    }

    public void setContent(String body) {
        if (body == null) throw new AssertionError("Content cannot be null");
        _isJSON = StringUtils.isJSONType(body);
        if (!_isJSON) {
            _object = Jsoup.parse(body);
        } else {
            _object = body;
        }
    }

    public List<String> getStringList(String rule, String baseUrl) {
        List<String> stringList;
        SourceRule source = new SourceRule(rule);
        switch (source.mode) {
            case JSon:
                stringList = new ArrayList<>();
                break;
            case XPath:
                stringList = getAnalyzeByXPath().getStringList(source.rule);
                break;
            default:
                stringList = getAnalyzeByJSoup().getAllResultList(source.rule);
        }
        if (!TextUtils.isEmpty(baseUrl)) {
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
        if (TextUtils.isEmpty(rule)) {
            return "";
        }
        String result = "";
        SourceRule source = new SourceRule(rule);
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
        if (!isEmpty(source.js)) {
            result = (String) AnalyzeRule.evalJS(source.js, result, _baseUrl);
        }
        return result;
    }

    public AnalyzeCollection getElements(String rule) {
        SourceRule source = new SourceRule(rule);
        switch (source.mode) {
            case JSon:
                return new AnalyzeCollection(getAnalyzeByJSonPath().readList(source.rule), _isJSON);
            case XPath:
                return new AnalyzeCollection(getAnalyzeByXPath().getElements(source.rule));
        }
        return new AnalyzeCollection(getAnalyzeByJSoup().getElements(source.rule));
    }

    class SourceRule {
        Mode mode;
        String rule;
        String js;

        SourceRule(String ruleStr) {
            if (_isJSON && !ruleStr.startsWith("@JSon:"))
                throw new AssertionError("Content analyze");
            String str[] = ruleStr.split("@js:");
            if (str[0].startsWith("@XPath:")) {
                mode = Mode.XPath;
                rule = str[0].substring(7);
            } else if (str[0].startsWith("@JSon:")) {
                mode = Mode.JSon;
                rule = str[0].substring(6);
            } else {
                mode = Mode.Default;
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

    private static Object evalJS(String jsStr, Object result, String baseUrl) {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("result", result);
        bindings.put("baseUrl", baseUrl);
        try {
            result = EngineHelper.INSTANCE.eval(jsStr, bindings);
        } catch (ScriptException ignored) {
        }
        return result;
    }

}
