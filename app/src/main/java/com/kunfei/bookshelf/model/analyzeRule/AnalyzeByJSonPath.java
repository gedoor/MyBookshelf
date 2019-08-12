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

    public AnalyzeByJSonPath parse(Object json) {
        if (json instanceof String) {
            ctx = JsonPath.parse((String) json);
        } else {
            ctx = JsonPath.parse(json);
        }
        return this;
    }

    public String getString(String rule) {
        if (TextUtils.isEmpty(rule)) return null;
        String result = "";
        String[] rules;
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
                            builder.append(o).append("\n");
                        }
                        result = builder.toString().replaceAll("\\n$", "");
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
                    result = result.replace(String.format("{%s}", matcher.group()), getString(matcher.group()));
                }
                return result;
            }
        } else {
            List<String> textS = new ArrayList<>();
            for (String rl : rules) {
                String temp = getString(rl);
                if (!TextUtils.isEmpty(temp)) {
                    textS.add(temp);
                    if (elementsType.equals("|")) {
                        break;
                    }
                }
            }
            return TextUtils.join(",", textS).trim();
        }
    }

    List<String> getStringList(String rule) {
        List<String> result = new ArrayList<>();
        if (TextUtils.isEmpty(rule)) return result;
        String[] rules;
        String elementsType;
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
                    List<String> stringList = getStringList(matcher.group());
                    for (String s : stringList) {
                        result.add(rule.replace(String.format("{%s}", matcher.group()), s));
                    }
                }
                return result;
            }
        } else {
            List<List<String>> results = new ArrayList<>();
            for (String rl : rules) {
                List<String> temp = getStringList(rl);
                if (temp != null && !temp.isEmpty()) {
                    results.add(temp);
                    if (temp.size() > 0 && elementsType.equals("|")) {
                        break;
                    }
                }
            }
            if (results.size() > 0) {
                if ("%".equals(elementsType)) {
                    for (int i = 0; i < results.get(0).size(); i++) {
                        for (List<String> temp : results) {
                            if (i < temp.size()) {
                                result.add(temp.get(i));
                            }
                        }
                    }
                } else {
                    for (List<String> temp : results) {
                        result.addAll(temp);
                    }
                }
            }
            return result;
        }
    }

    Object getObject(String rule) {
        return ctx.read(rule);
    }

    List<Object> getList(String rule) {
        List<Object> result = new ArrayList<>();
        if (TextUtils.isEmpty(rule)) return result;
        String elementsType;
        String[] rules;
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
                List temp = getList(rl);
                if (temp != null && !temp.isEmpty()) {
                    results.add(temp);
                    if (temp.size() > 0 && elementsType.equals("|")) {
                        break;
                    }
                }
            }
            if (results.size() > 0) {
                if ("%".equals(elementsType)) {
                    for (int i = 0; i < results.get(0).size(); i++) {
                        for (List temp : results) {
                            if (i < temp.size()) {
                                result.add(temp.get(i));
                            }
                        }
                    }
                } else {
                    for (List temp : results) {
                        result.addAll(temp);
                    }
                }
            }
        }
        return result;
    }
}
