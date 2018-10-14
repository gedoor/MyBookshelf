package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;
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
    private String baseURL;
    private Element element;

    public AnalyzeElement(Element element, String baseURL) {
        this.element = element;
        this.baseURL = baseURL;
    }

    /**
     * 获取Elements
     */
    public static Elements getElements(Element temp, String rule) {
        Elements elements = new Elements();
        if (temp == null || isEmpty(rule)) {
            return elements;
        }
        boolean isAnd;
        String[] ruleStrS;
        if (rule.contains("&")) {
            isAnd = true;
            ruleStrS = rule.split("&");
        } else {
            isAnd = false;
            ruleStrS = rule.split("\\|");
        }
        for (String ruleStr : ruleStrS) {
            Elements tempS = getElementsSingle(temp, ruleStr);
            elements.addAll(tempS);
            if (elements.size() > 0 && !isAnd) {
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
                    case "children":
                        elements.addAll(temp.children());
                        break;
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
                    case "text":
                        elements.addAll(temp.getElementsContainingOwnText(rules[1]));
                        break;
                }
                if (rulePc.length > 1) {
                    String[] rulePcs = rulePc[1].split(":");
                    if (rulePcs.length < elements.size() - 1) {
                        for (String pc : rulePcs) {
                            if (pc.equals("%")) {
                                elements.set(elements.size() - 1, null);
                            } else if (Integer.parseInt(pc) < elements.size()) {
                                elements.set(Integer.parseInt(pc), null);
                            }
                        }
                        Elements es = new Elements();
                        es.add(null);
                        elements.removeAll(es);
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            // Log.e("getElements", rule + e.getMessage());
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
        String[] ruleStrS = ruleStr.trim().split("#");
        if (ruleStrS.length > 1) {
            regex = ruleStrS[1];
        }
        if (isEmpty(ruleStrS[0])) {
            result = element.data();
        } else {
            boolean isAnd;
            if (ruleStrS[0].contains("&")) {
                isAnd = true;
                ruleStrS = ruleStrS[0].split("&");
            } else {
                isAnd = false;
                ruleStrS = ruleStrS[0].split("\\|");
            }
            List<String> textS = new ArrayList<>();
            for (String ruleStrX : ruleStrS) {
                List<String> temp = getResultList(ruleStrX);
                if (temp != null) {
                    textS.addAll(temp);
                }
                if (textS.size() > 0 && !isAnd) {
                    break;
                }
            }
            if (textS.size() == 0) {
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
                    for (Element element : elements) {
                        List<TextNode> contentEs = element.textNodes();
                        for (int i = 0; i < contentEs.size(); i++) {
                            String temp = contentEs.get(i).text().trim();
                            temp = FormatWebText.getContent(temp);
                            if (!TextUtils.isEmpty(temp)) {
                                textS.add(temp);
                            }
                        }
                    }
                    break;
                case "html":
                    String html = elements.html();
                    String[] htmlS = html.replaceAll("<(br|p.*?|div.*?|/p|/div)>", "\n")
                            .replaceAll("<.*?>", "")
                            .split("\n");
                    for (String temp : htmlS) {
                        if (!TextUtils.isEmpty(FormatWebText.getContent(temp))) {
                            textS.add(temp);
                        }
                    }
                    break;
                default:
                    String absURL = NetworkUtil.getAbsoluteURL(baseURL, elements.get(0).attr(lastRule));
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

