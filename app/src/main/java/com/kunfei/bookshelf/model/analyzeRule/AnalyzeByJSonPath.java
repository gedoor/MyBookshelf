package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyzeByJSonPath {
    private static final Pattern jsonRulePattern = Pattern.compile("(?<=\\{)\\$\\..+?(?=\\})");
    private ReadContext ctx;

    public AnalyzeByJSonPath parse(String json) {
        ctx = JsonPath.parse(json);
        return this;
    }

    public AnalyzeByJSonPath parse(Object json) {
        ctx = JsonPath.parse(json);
        return this;
    }

    public String read(String rule) {
        if (TextUtils.isEmpty(rule)) return null;
        String result = "";
        String rules[];
        String elementsType;
        if (rule.contains("&&")) {
            rules = rule.split("&&");
            elementsType = "&";
        } else {
            rules = rule.split("\\|\\|");
            elementsType = "|";
        }
        if (rules.length == 1) {
            if (!rule.contains("{$.")) {
                try {
                    Object object = ctx.read(rule);
                    if (object instanceof List) {
                        StringBuilder builder = new StringBuilder();
                        for (Object o : (List) object) {
                            builder.append(String.valueOf(o)).append("\n");
                        }
                        result = builder.toString();
                    } else {
                        result = String.valueOf(object);
                    }
                } catch (Exception ignored) {
                }
                return result;
            } else {
                result = rule;
                Matcher matcher = jsonRulePattern.matcher(rule);
                while (matcher.find()) {
                    result = result.replace(String.format("{%s}", matcher.group()), read(matcher.group()));
                }
                return result;
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (String rl : rules) {
                String temp = read(rl);
                if (!TextUtils.isEmpty(temp)) {
                    sb.append(temp);
                    if (elementsType.equals("|")) {
                        break;
                    }
                }
            }
            return sb.toString();
        }
    }

    List<String> readStringList(String rule) {
        List<String> result = new ArrayList<>();
        if (TextUtils.isEmpty(rule)) return result;
        String rules[];
        String elementsType;
        if (rule.contains("&&")) {
            rules = rule.split("&&");
            elementsType = "&";
        } else {
            rules = rule.split("\\|\\|");
            elementsType = "|";
        }
        if (rules.length == 1) {
            if (!rule.contains("{$.")) {
                try {
                    Object object = ctx.read(rule);
                    if (object == null) return result;
                    if (object instanceof List) {
                        for (Object o : ((List) object))
                            result.add(String.valueOf(o));
                    } else {
                        result.add(String.valueOf(object));
                    }
                } catch (Exception ignored) {
                }
                return result;
            } else {
                Matcher matcher = jsonRulePattern.matcher(rule);
                while (matcher.find()) {
                    List<String> stringList = readStringList(matcher.group());
                    for (String s : stringList) {
                        result.add(rule.replace(String.format("{%s}", matcher.group()), s));
                    }
                }
                return result;
            }
        } else {
            for (String rl : rules) {
                List<String> temp = readStringList(rl);
                if (!temp.isEmpty()) {
                    result.addAll(temp);
                    if (elementsType.equals("|")) {
                        break;
                    }
                }
            }
            return result;
        }
    }

    List<Object> readList(String rule) {
        List<Object> result = new ArrayList<>();
        if (TextUtils.isEmpty(rule)) return result;
        String elementsType;
        String rules[];
        if (rule.contains("&&")) {
            rules = rule.split("&&");
            elementsType = "&";
        } else if (rule.contains("%%")) {
            rules = rule.split("%%");
            elementsType = "%";
        } else {
            rules = rule.split("\\|\\|");
            elementsType = "|";
        }
        if (rules.length == 1) {
            try {
                return ctx.read(rules[0]);
            } catch (Exception e) {
                return null;
            }
        } else {
            List<List> results = new ArrayList<>();
            for (String rl : rules) {
                List temp = readList(rl);
                if (temp != null && !temp.isEmpty()) {
                    results.add(temp);
                    if (temp.size() > 0 && elementsType.equals("|")) {
                        break;
                    }
                }
            }
            if (results.size() > 0) {
                switch (elementsType) {
                    case "%":
                        for (int i = 0; i < results.get(0).size(); i++) {
                            for (List temp : results) {
                                if (i < temp.size()) {
                                    result.add(temp.get(i));
                                }
                            }
                        }
                        break;
                    default:
                        for (List temp : results) {
                            result.addAll(temp);
                        }
                }
            }
        }
        return result;
    }
}
