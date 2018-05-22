package com.monke.monkeybook.model.AnalyzeRule;

import android.util.Log;

import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * 书源规则解析
 */
public class AnalyzeJson {
    private JSONObject jsonObject;

    public AnalyzeJson(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public static List<JSONObject> getJsonObjects(JSONObject temp, String rule) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        if (temp == null || isEmpty(rule)) {
            return jsonObjects;
        }
        String[] ruleStrS = rule.split("\\|");
        for (String ruleStr : ruleStrS) {
            jsonObjects = getJsonObjectsSingle(temp, ruleStr);
            if (jsonObjects.size() > 0) {
                break;
            }
        }
        return jsonObjects;
    }

    private static List<JSONObject> getJsonObjectsSingle(JSONObject temp, String rule) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        try {
            String[] rs = rule.split("@");
            if (rs.length > 1) {
                jsonObjects.add(temp);
                for (String rl : rs) {
                    List<JSONObject> jos = new ArrayList<>();
                    for (JSONObject et : jsonObjects) {
                        jos.addAll(getJsonObjects(et, rl));
                    }
                    jsonObjects.clear();
                    jsonObjects.addAll(jos);
                }
            } else {
                String[] rulePc = rule.split("!");
                String[] rules = rulePc[0].split("\\.");
                switch (rules[0]) {
                    case "JSONObject":
                        jsonObjects.add(temp.getJSONObject(rules[1]));
                        break;
                    case "JSONArray":
                        JSONArray jsonArray = temp.getJSONArray(rules[1]);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObjects.add(jsonArray.getJSONObject(i));
                        }
                        break;
                }
                if (rulePc.length > 1) {
                    String[] rulePcs = rulePc[1].split(":");
                    for (String pc : rulePcs) {
                        if (pc.equals("%")) {
                            jsonObjects.set(jsonObjects.size() - 1, null);
                        } else {
                            jsonObjects.set(Integer.parseInt(pc), null);
                        }
                    }
                    List<JSONObject> es = new ArrayList<>();
                    es.add(null);
                    jsonObjects.removeAll(es);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObjects;
    }


    /**
     * 合并内容列表,得到内容
     */
    public String getResult(String ruleStr) {
        if (isEmpty(ruleStr)) {
            return null;
        }
        String regex = null;
        String result = "";
        //分离正则表达式
        String[] ruleStrS = ruleStr.split("#");
        if (ruleStrS.length > 1) {
            regex = ruleStrS[1];
        }
        if (isEmpty(ruleStrS[0])) {
            result = jsonObject.toString();
        } else {
            ruleStrS = ruleStrS[0].split("\\|");
            List<String> textS = null;
            for (String ruleStrX : ruleStrS) {
                textS = getResultList(ruleStrX);
                if (textS != null) {
                    break;
                }
            }
            if (textS == null) {
                return null;
            }
            StringBuilder content = new StringBuilder();
            for (String text : textS) {
                text = FormatWebText.getContent(text);
                if (textS.size() > 1) {
                    if (text.length() > 0) {
                        if (content.length() > 0) {
                            content.append("\r\n");
                        }
                        content.append("\u3000\u3000").append(text);
                    }
                } else {
                    content.append(text);
                }
                result = content.toString();
            }
        }
        if (!isEmpty(regex)) {
            result = result.replaceAll(regex, "");
        }
        return result;
    }

    /**
     * 获取内容列表
     */
    private List<String> getResultList(String ruleStr) {
        if (isEmpty(ruleStr)) {
            return null;
        }
        List<JSONObject> elements = new ArrayList<>();
        elements.add(jsonObject);
        String[] rules = ruleStr.split("@");
        for (int i = 0; i < rules.length - 1; i++) {
            List<JSONObject> es = new ArrayList<>();
            for (JSONObject elt : elements) {
                es.addAll(getJsonObjectsSingle(elt, rules[i]));
            }
            elements.clear();
            elements = es;
        }
        if (elements.isEmpty()) {
            return null;
        }
        return getResultLast(elements, rules[rules.length - 1]);
    }

    /**
     * 根据最后一个规则获取内容
     */
    private List<String> getResultLast(List<JSONObject> elements, String lastRule) {
        try {
            List<String> textS = new ArrayList<>();
            for (JSONObject element : elements) {
                String text = element.getString(lastRule);
                textS.add(text);
            }
            return textS;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getResultList", e.getMessage());
            return null;
        }
    }

}
