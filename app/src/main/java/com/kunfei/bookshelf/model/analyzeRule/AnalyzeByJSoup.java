package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import com.kunfei.bookshelf.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/25.
 * 书源规则解析
 */

public class AnalyzeByJSoup {
    private Element element;

    public AnalyzeByJSoup parse(Object doc) {
        if (doc instanceof Element) {
            element = (Element) doc;
        } else if (doc instanceof JXNode) {
            JXNode jxNode = (JXNode) doc;
            if (jxNode.isElement()) {
                element = jxNode.asElement();
            } else {
                element = Jsoup.parse(jxNode.value().toString());
            }
        } else {
            element = Jsoup.parse(doc.toString());
        }
        return this;
    }

    /**
     * 获取列表
     */
    Elements getElements(String rule) {
        return getElements(element, rule);
    }

    /**
     * 合并内容列表,得到内容
     */
    String getString(String ruleStr) {
        if (isEmpty(ruleStr)) {
            return null;
        }
        List<String> textS = getStringList(ruleStr);
        if (textS.size() == 0) {
            return null;
        }
        return TextUtils.join(",", textS).trim();
    }

    /**
     * 获取一个字符串
     **/
    String getString0(String ruleStr) {
        List<String> urlList = getStringList(ruleStr);
        if (!urlList.isEmpty()) {
            return urlList.get(0);
        }
        return "";
    }

    /**
     * 获取所有内容列表
     */
    List<String> getStringList(String ruleStr) {
        List<String> textS = new ArrayList<>();
        if (isEmpty(ruleStr)) {
            return textS;
        }
        //拆分规则
        SourceRule sourceRule = new SourceRule(ruleStr);
        if (isEmpty(sourceRule.elementsRule)) {
            textS.add(element.data());
        } else {
            String elementsType;
            String[] ruleStrS;
            if (sourceRule.elementsRule.contains("&")) {
                elementsType = "&";
                ruleStrS = sourceRule.elementsRule.split("&+");
            } else if (sourceRule.elementsRule.contains("%%")) {
                elementsType = "%";
                ruleStrS = sourceRule.elementsRule.split("%%");
            } else {
                elementsType = "|";
                if (sourceRule.isCss) {
                    ruleStrS = sourceRule.elementsRule.split("\\|\\|");
                } else {
                    ruleStrS = sourceRule.elementsRule.split("\\|+");
                }
            }
            List<List<String>> results = new ArrayList<>();
            for (String ruleStrX : ruleStrS) {
                List<String> temp;
                if (sourceRule.isCss) {
                    int lastIndex = ruleStrX.lastIndexOf('@');
                    temp = getResultLast(element.select(ruleStrX.substring(0, lastIndex)), ruleStrX.substring(lastIndex + 1));
                } else {
                    temp = getResultList(ruleStrX);
                }
                if (temp != null && !temp.isEmpty()) {
                    results.add(temp);
                    if (!results.isEmpty() && elementsType.equals("|")) {
                        break;
                    }
                }
            }
            if (results.size() > 0) {
                if ("%".equals(elementsType)) {
                    for (int i = 0; i < results.get(0).size(); i++) {
                        for (List<String> temp : results) {
                            if (i < temp.size()) {
                                textS.add(temp.get(i));
                            }
                        }
                    }
                } else {
                    for (List<String> temp : results) {
                        textS.addAll(temp);
                    }
                }
            }
        }
        if (!isEmpty(sourceRule.replaceRegex)) {
            List<String> tempList = new ArrayList<>(textS);
            textS.clear();
            for (String text : tempList) {
                text = text.replaceAll(sourceRule.replaceRegex, sourceRule.replacement);
                if (text.length() > 0) {
                    textS.add(text);
                }
            }
        }
        return textS;
    }

    /**
     * 获取Elements
     */
    private Elements getElements(Element temp, String rule) {
        Elements elements = new Elements();
        if (temp == null || isEmpty(rule)) {
            return elements;
        }
        SourceRule sourceRule = new SourceRule(rule);
        String elementsType;
        String[] ruleStrS;
        if (sourceRule.elementsRule.contains("&")) {
            elementsType = "&";
            ruleStrS = sourceRule.elementsRule.split("&+");
        } else if (sourceRule.elementsRule.contains("%")) {
            elementsType = "%";
            ruleStrS = sourceRule.elementsRule.split("%+");
        } else {
            elementsType = "|";
            if (sourceRule.isCss) {
                ruleStrS = sourceRule.elementsRule.split("\\|\\|");
            } else {
                ruleStrS = sourceRule.elementsRule.split("\\|+");
            }
        }
        List<Elements> elementsList = new ArrayList<>();
        if (sourceRule.isCss) {
            for (String ruleStr : ruleStrS) {
                Elements tempS = temp.select(ruleStr);
                elementsList.add(tempS);
                if (tempS.size() > 0 && elementsType.equals("|")) {
                    break;
                }
            }
        } else {
            for (String ruleStr : ruleStrS) {
                Elements tempS = getElementsSingle(temp, ruleStr);
                elementsList.add(tempS);
                if (tempS.size() > 0 && elementsType.equals("|")) {
                    break;
                }
            }
        }
        if (elementsList.size() > 0) {
            if ("%".equals(elementsType)) {
                for (int i = 0; i < elementsList.get(0).size(); i++) {
                    for (Elements es : elementsList) {
                        if (i < es.size()) {
                            elements.add(es.get(i));
                        }
                    }
                }
            } else {
                for (Elements es : elementsList) {
                    elements.addAll(es);
                }
            }
        }
        return elements;
    }

    private Elements filterElements(Elements elements, String[] rules) {
        if (rules == null || rules.length < 2) return elements;
        Elements selectedEls = new Elements();
        for (Element ele : elements) {
            boolean isOk = false;
            switch (rules[0]) {
                case "class":
                    isOk = ele.getElementsByClass(rules[1]).size() > 0;
                    break;
                case "id":
                    isOk = ele.getElementById(rules[1]) != null;
                    break;
                case "tag":
                    isOk = ele.getElementsByTag(rules[1]).size() > 0;
                    break;
                case "text":
                    isOk = ele.getElementsContainingOwnText(rules[1]).size() > 0;
                    break;
            }
            if (isOk) {
                selectedEls.add(ele);
            }
        }
        return selectedEls;
    }

    /**
     * 获取Elements按照一个规则
     */
    private Elements getElementsSingle(Element temp, String rule) {
        Elements elements = new Elements();
        try {
            String[] rs = rule.trim().split("@");
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
                String[] rulePcx = rule.split("!");
                String[] rulePc = rulePcx[0].trim().split(">");
                String[] rules = rulePc[0].trim().split("\\.");
                String[] filterRules = null;
                boolean needFilterElements = rulePc.length > 1 && !isEmpty(rulePc[1].trim());
                if (needFilterElements) {
                    filterRules = rulePc[1].trim().split("\\.");
                    filterRules[0] = filterRules[0].trim();
                    List<String> validKeys = Arrays.asList("class", "id", "tag", "text");
                    if (filterRules.length < 2 || !validKeys.contains(filterRules[0]) || isEmpty(filterRules[1].trim())) {
                        needFilterElements = false;
                    }
                    filterRules[1] = filterRules[1].trim();
                }
                switch (rules[0]) {
                    case "children":
                        Elements children = temp.children();
                        if (needFilterElements)
                            children = filterElements(children, filterRules);
                        elements.addAll(children);
                        break;
                    case "class":
                        Elements elementsByClass = temp.getElementsByClass(rules[1]);
                        if (rules.length == 3) {
                            int index = Integer.parseInt(rules[2]);
                            if (index < 0) {
                                elements.add(elementsByClass.get(elementsByClass.size() + index));
                            } else {
                                elements.add(elementsByClass.get(index));
                            }
                        } else {
                            if (needFilterElements)
                                elementsByClass = filterElements(elementsByClass, filterRules);
                            elements.addAll(elementsByClass);
                        }
                        break;
                    case "tag":
                        Elements elementsByTag = temp.getElementsByTag(rules[1]);
                        if (rules.length == 3) {
                            int index = Integer.parseInt(rules[2]);
                            if (index < 0) {
                                elements.add(elementsByTag.get(elementsByTag.size() + index));
                            } else {
                                elements.add(elementsByTag.get(index));
                            }
                        } else {
                            if (needFilterElements)
                                elementsByTag = filterElements(elementsByTag, filterRules);
                            elements.addAll(elementsByTag);
                        }
                        break;
                    case "id":
                        Elements elementsById = Collector.collect(new Evaluator.Id(rules[1]), temp);
                        if (rules.length == 3) {
                            int index = Integer.parseInt(rules[2]);
                            if (index < 0) {
                                elements.add(elementsById.get(elementsById.size() + index));
                            } else {
                                elements.add(elementsById.get(index));
                            }
                        } else {
                            if (needFilterElements)
                                elementsById = filterElements(elementsById, filterRules);
                            elements.addAll(elementsById);
                        }
                        break;
                    case "text":
                        Elements elementsByText = temp.getElementsContainingOwnText(rules[1]);
                        if (needFilterElements)
                            elementsByText = filterElements(elementsByText, filterRules);
                        elements.addAll(elementsByText);
                        break;
                    default:
                        elements.addAll(temp.select(rulePcx[0]));
                }
                if (rulePcx.length > 1) {
                    String[] rulePcs = rulePcx[1].split(":");
                    for (String pc : rulePcs) {
                        int pcInt = Integer.parseInt(pc);
                        if (pcInt < 0 && elements.size() + pcInt >= 0) {
                            elements.set(elements.size() + pcInt, null);
                        } else if (Integer.parseInt(pc) < elements.size()) {
                            elements.set(Integer.parseInt(pc), null);
                        }
                    }
                    Elements es = new Elements();
                    es.add(null);
                    elements.removeAll(es);
                }
            }
        } catch (Exception ignore) {
        }
        return elements;
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
        List<String> textS = new ArrayList<>();
        List<String> cText = new ArrayList<>();
        try {
            switch (lastRule) {
                case "text":
                    for (Element element : elements) {
                        String text = element.text();
                        cText.add(text);
                    }
                    textS.add(TextUtils.join("\n", cText));
                    break;
                case "textNodes":
                    for (Element element : elements) {
                        List<TextNode> contentEs = element.textNodes();
                        for (int i = 0; i < contentEs.size(); i++) {
                            String temp = contentEs.get(i).text().trim();
                            if (!isEmpty(temp)) {
                                cText.add(temp);
                            }
                        }
                    }
                    textS.add(TextUtils.join("\n", cText));
                    break;
                case "ownText":
                    for (Element element : elements) {
                        cText.add(element.ownText());
                    }
                    textS.add(TextUtils.join("\n", cText));
                    break;
                case "html":
                    elements.select("script").remove();
                    String html = elements.html();
                    textS.add(html);
                    break;
                default:
                    for (Element element : elements) {
                        String url = element.attr(lastRule);
                        if (!TextUtils.isEmpty(url) && !textS.contains(url)) {
                            textS.add(url);
                        }
                    }
            }
        } catch (Exception ignore) {
        }
        return textS;
    }

    class SourceRule {
        boolean isCss = false;
        String elementsRule;
        String replaceRegex = "";
        String replacement = "";

        SourceRule(String ruleStr) {
            if (StringUtils.startWithIgnoreCase(ruleStr, "@CSS:")) {
                isCss = true;
                elementsRule = ruleStr.substring(5).trim();
                return;
            }
            String[] ruleStrS;
            //分离正则表达式
            ruleStrS = ruleStr.trim().split("#");
            elementsRule = ruleStrS[0];
            if (ruleStrS.length > 1) {
                replaceRegex = ruleStrS[1];
            }
            if (ruleStrS.length > 2) {
                replacement = ruleStrS[2];
            }
        }
    }

}
