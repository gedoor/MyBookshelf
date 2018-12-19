package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (!rule.contains("{")) {
            try {
                Object object = ctx.read(rule);
                if (object instanceof List) {
                    object = ((List) object).get(0);
                }
                result = String.valueOf(object);
            } catch (Exception ignored) {
            }
            return result;
        } else {
            result = rule;
            Pattern pattern = Pattern.compile("(?<=\\{).+?(?=\\})");
            Matcher matcher = pattern.matcher(rule);
            while (matcher.find()) {
                result = result.replace(String.format("{%s}", matcher.group()), read(matcher.group()));
            }
            return result;
        }
    }

    List<Object> readList(String rule) {
        try {
            return ctx.read(rule);
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }
}
