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

    public AnalyzeRule(){

    }

    public AnalyzeRule(Object object, boolean isJSON){
        _object = object;
        _isJSON = isJSON;
    }

    private AnalyzeByXPath getAnalyzeByXPath(){
        if(analyzeByXPath==null) {
            analyzeByXPath = new AnalyzeByXPath();
            analyzeByXPath.parse(((Element) _object).children());
        }
        return analyzeByXPath;
    }

    private AnalyzeByJSoup getAnalyzeByJSoup(){
        if(analyzeByJSoup==null) {
            analyzeByJSoup = new AnalyzeByJSoup();
            analyzeByJSoup.parse((Element) _object);
        }
        return analyzeByJSoup;
    }

    private AnalyzeByJSonPath getAnalyzeByJSonPath(){
        if(analyzeByJSonPath==null) {
            analyzeByJSonPath = new AnalyzeByJSonPath();
            if(_object instanceof String){
                analyzeByJSonPath.parse(String.valueOf(_object));
            }else{
                analyzeByJSonPath.parse(_object);
            }
        }
        return analyzeByJSonPath;
    }

    public void setContent(String body){
        if (body==null) throw new AssertionError("Content cannot be null");
        _isJSON = StringUtils.isJSONType(body);
        if(!_isJSON){
            _object = Jsoup.parse(body);
        }else{
            _object = body;
        }
    }

    public List<String> getStringList(String rule, String baseUrl){
        List<String> stringList;
        SourceRule source = new SourceRule(rule);
        switch (source.mode) {
            case JSon:
                stringList =  new ArrayList<>();
                break;
            case XPath:
                stringList =  getAnalyzeByXPath().getStringList(source.rule);
                break;
            default:
                stringList =   getAnalyzeByJSoup().getAllResultList(source.rule);
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

    public String getString(String rule, String _baseUrl){
        if (TextUtils.isEmpty(rule)) {
            return "";
        }
        SourceRule source = new SourceRule(rule);
        switch (source.mode) {
            case JSon:
                if (TextUtils.isEmpty(_baseUrl)) {
                    return getAnalyzeByJSonPath().read(source.rule);
                } else {
                    return NetworkUtil.getAbsoluteURL(_baseUrl, getAnalyzeByJSonPath().read(source.rule));
                }
            case XPath:
                if (TextUtils.isEmpty(_baseUrl)) {
                    return getAnalyzeByXPath().getString(source.rule, _baseUrl);
                } else {
                    return NetworkUtil.getAbsoluteURL(_baseUrl, getAnalyzeByXPath().getString(source.rule, _baseUrl));
                }
        }
        if (TextUtils.isEmpty(_baseUrl)) {
            return getAnalyzeByJSoup().getResult(source.rule);
        } else {
            return NetworkUtil.getAbsoluteURL(_baseUrl, getAnalyzeByJSoup().getResultUrl(source.rule));
        }
    }

    public AnalyzeCollection getElements(String rule){
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

        SourceRule(String ruleStr) {
            if (_isJSON && !ruleStr.startsWith("@JSon:")) throw new AssertionError("Content analyze");

            if (ruleStr.startsWith("@XPath:")) {
                mode = Mode.XPath;
                rule = ruleStr.substring(7);
            } else if (ruleStr.startsWith("@JSon:")) {
                mode = Mode.JSon;
                rule = ruleStr.substring(6);
            } else {
                mode = Mode.Default;
                rule = ruleStr;
            }
        }

    }

    private enum Mode {
        XPath, JSon, Default
    }

    private static class EngineHelper{
        private static final ScriptEngine INSTANCE = new ScriptEngineManager().getEngineByName("rhino");
    }

    public static Object evalJS(String jsStr, Object result){
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("result", result);
        try {
            result = EngineHelper.INSTANCE.eval(jsStr, bindings);
        } catch (ScriptException ignored) {
        }
        return result;
    }

}
