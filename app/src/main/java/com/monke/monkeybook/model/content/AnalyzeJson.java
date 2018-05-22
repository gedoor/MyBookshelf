package com.monke.monkeybook.model.content;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class AnalyzeJson {
    private JSONObject jsonObject;
    private String baseURI;

    AnalyzeJson(JSONObject jsonObject, String baseURI) {
        this.jsonObject = jsonObject;
        this.baseURI = baseURI;
    }

    static List<JSONObject> getJsonObjects(JSONObject temp, String rule) {
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

    static List<JSONObject> getJsonObjectsSingle(JSONObject temp, String rule) {
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



}
