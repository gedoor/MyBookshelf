package com.monke.monkeybook.model.AnalyzeRule;

import android.util.Log;

import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.utils.NetworkUtil;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/25.
 * 书源规则解析
 */

public class AnalyzeElement {
    private String baseURI;
    private Element element;

    public AnalyzeElement(Element element, String baseURI) {
        this.element = element;
        this.baseURI = baseURI;
    }

    /**
     * 获取Elements
     */
    public static Elements getElements(Element temp, String rule) {
        Elements elements = new Elements();
        if (temp == null || isEmpty(rule)) {
            return elements;
        }
        String[] ruleStrS = rule.split("\\|");
        for (String ruleStr : ruleStrS) {
            elements = getElementsSingle(temp, ruleStr);
            if (elements.size() > 0) {
                break;
            }
        }
        return elements;
    }

    /**
     * 获取Elements按照一个规则
     */
    private static Elements getElementsSingle(Element temp, String rule) {
        Elements elements = new Elements();
        try {
            String[] rs = rule.split("@");
            if (rs.length > 1) {
                elements.add(temp);
                for (String rl : rs) {
                    Elements es = new Elements();
                    for (Element et : elements) {
                        es.addAll(getElements(et, rl));
                    }
                    elements.clear();
                    elements.addAll(es);
                }
            } else {
                String[] rulePc = rule.split("!");
                String[] rules = rulePc[0].split("\\.");
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
                        break;
                    case "id":
                        elements.add(temp.getElementById(rules[1]));
                        break;
                }
                if (rulePc.length > 1) {
                    String[] rulePcs = rulePc[1].split(":");
                    for (String pc : rulePcs) {
                        if (pc.equals("%")) {
                            elements.set(elements.size() - 1, null);
                        } else {
                            elements.set(Integer.parseInt(pc), null);
                        }
                    }
                    Elements es = new Elements();
                    es.add(null);
                    elements.removeAll(es);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getElements", e.getMessage());
        }
        return elements;
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
            result = element.data();
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
        Elements elements = new Elements();
        elements.add(element);
        String[] rules = ruleStr.split("@");
        for (int i = 0; i < rules.length - 1; i++) {
            Elements es = new Elements();
            for (Element elt : elements) {
                es.addAll(getElementsSingle(elt, rules[i]));
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
    private List<String> getResultLast(Elements elements, String lastRule) {
        try {
            List<String> textS = new ArrayList<>();
            switch (lastRule) {
                case "text":
                    for (Element element : elements) {
                        String text = element.text();
                        textS.add(text);
                    }
                    break;
                case "textNodes":
                    List<TextNode> contentEs = elements.get(0).textNodes();
                    for (int i = 0; i < contentEs.size(); i++) {
                        String temp = contentEs.get(i).text().trim();
                        temp = FormatWebText.getContent(temp);
                        if (temp.length() > 0) {
                            textS.add(temp);
                        }
                    }
                    break;
                default:
                    String absURL = NetworkUtil.getAbsoluteURL(baseURI, elements.get(0).attr(lastRule));
                    textS.add(absURL);
            }
            return textS;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getResultList", e.getMessage());
            return null;
        }
    }

}

