package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class AnalyzeByJSonPath {
    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");
    ReadContext ctx;

    public AnalyzeByJSonPath(String json) {
        ctx = JsonPath.parse(json);
    }

    public void parse(String json) {
        ctx = JsonPath.parse(json);
    }

    public void parse(Object json) {
        ctx = JsonPath.parse(json);
    }

    public String read(String rule) {
        if (TextUtils.isEmpty(rule)) return null;
        String result = "";
        SourceRule sourceRule = splitSourceRule(rule);
        Object object = ctx.read(sourceRule.rule);
        if (object instanceof List) {
            object = ((List<String>) object).get(0);
        }
        if (!TextUtils.isEmpty(sourceRule.jsStr)) {
            try {
                engine.put("result", object);
                result = (String) engine.eval(sourceRule.jsStr);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        } else {
            if (object instanceof Integer) {
                result = Integer.toString((Integer) object);
            } else {
                result = (String) object;
            }
        }
        return result;
    }

    private SourceRule splitSourceRule(String rule) {
        SourceRule sourceRule = new SourceRule();
        String str[] = rule.split("@js:");
        sourceRule.rule = str[0];
        if (str.length > 1) {
            sourceRule.jsStr = str[1];
        }
        return sourceRule;
    }

    class SourceRule {
        String rule;
        String jsStr;
    }
}
