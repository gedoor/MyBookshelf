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
        _isJSON = StringUtils.isJsonType(body);
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
            result = (String) AnalyzeRule.evalJS(source.js, result, _baseUrl);
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
                collection = (AnalyzeCollection) AnalyzeRule.evalJS(source.js, collection, null);
            }
            return collection;
        } else if (!StringUtils.isTrimEmpty(source.js)) {
            return (AnalyzeCollection) AnalyzeRule.evalJS(source.js, _object, null);
        }
        return null;
    }

    class SourceRule {
        Mode mode;
        String rule;
        String js;

        SourceRule(String ruleStr) {
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
