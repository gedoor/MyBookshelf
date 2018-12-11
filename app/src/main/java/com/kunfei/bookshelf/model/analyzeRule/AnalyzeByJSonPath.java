package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeByJSonPath {
    private ReadContext ctx;

    public void parse(String json) {
        ctx = JsonPath.parse(json);
    }

    public void parse(Object json) {
        ctx = JsonPath.parse(json);
    }

    public String read(String rule) {
        if (TextUtils.isEmpty(rule)) return null;
        String result = null;
        SourceRule sourceRule = splitSourceRule(rule);
        try {
            Object object = ctx.read(sourceRule.rule);
            if (object instanceof List) {
                object = ((List<String>) object).get(0);
            }
            if (!TextUtils.isEmpty(sourceRule.jsStr)) {
                result = (String) AnalyzeRule.evalJS(sourceRule.jsStr, object);
            } else {
                if (object instanceof Integer) {
                    result = Integer.toString((Integer) object);
                } else {
                    result = (String) object;
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public List<Object> readList(String rule) {
        SourceRule sourceRule = splitSourceRule(rule);
        try {
            return ctx.read(sourceRule.rule);
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
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
