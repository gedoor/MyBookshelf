package com.monke.monkeybook.model.content;

import org.jsoup.nodes.Document;
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

    public String getResult(String ruleStr) {
        if (isEmpty(ruleStr)) {
            return "";
        }
        Elements elements = new Elements();
        elements.add(element);
        String[] rules = ruleStr.split("@");
        for (int i = 0; i < rules.length - 1; i++) {
            Elements es = getElements(elements.get(0), rules[i]);
            elements.clear();
            elements = es;
        }
        if (rules[rules.length - 1].equals("text")) {
            return elements.get(0).text();
        } else {
            return elements.attr(rules[rules.length - 1]);
        }
    }

    public static Elements getElements(Element temp, String rule) {
        Elements elements = new Elements();
        String[] rules = rule.split("\\.");
        switch (rules[0]) {
            case "class":
                if (rules.length == 3) {
                    elements.add(temp.getElementsByClass(rules[1]).get(Integer.parseInt(rules[2])));
                } else {
                    elements.addAll(temp.getElementsByClass(rules[1]));
                }
                break;
            case "tag":
                if (rules.length == 3) {
                    elements.add(temp.getElementsByTag(rules[1]).get(Integer.parseInt(rules[2])));
                } else {
                    elements.addAll(temp.getElementsByTag(rules[1]));
                }
            case "id":
                elements.add(temp.getElementById(rules[1]));
        }
        return elements;
    }
}
