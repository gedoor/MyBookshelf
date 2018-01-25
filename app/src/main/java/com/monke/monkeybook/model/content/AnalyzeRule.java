package com.monke.monkeybook.model.content;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/25.
 */

public class AnalyzeRule {
    private Element element;

    public AnalyzeRule(Element element) {
        this.element = element;
    }

    public String getResult(String rule) {
        if (isEmpty(rule)) {
            return "";
        }
        String[] rules = rule.split("@");
        String[] temp = rules[0].split(" ");
        switch (rules.length) {
            case 2:
                return element.getElementsByClass(temp[0]).get(Integer.parseInt(rules[1])).getElementsByTag(temp[1]).text();
            case 3:
                return element.getElementsByClass(temp[0]).get(Integer.parseInt(rules[1])).getElementsByTag(temp[1]).attr(rules[2]);
            default:
                return null;
        }
    }

}
