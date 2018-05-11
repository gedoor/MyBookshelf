package com.monke.monkeybook.model.content;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.text.TextUtils.isEmpty;

public class AnalyzeJson {
    private JSONObject jsonObject;


    public JSONArray getJsonArray(String jsonStr, String rule) throws JSONException {
        jsonObject = new JSONObject(jsonStr);
        if (isEmpty(jsonStr) || isEmpty(rule)) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        String[] ruleStrS = rule.split("\\|");
        for (String ruleStr : ruleStrS) {
            jsonArray = getJsonArray(jsonObject, ruleStr);
            if (jsonArray.length() > 0) {
                break;
            }
        }
        return jsonArray;
    }

    private JSONArray getJsonArray(JSONObject jsonObject, String rule) {
        JSONArray jsonArray = new JSONArray();

        String[] rs = rule.split("@");

        return jsonArray;
    }

}
