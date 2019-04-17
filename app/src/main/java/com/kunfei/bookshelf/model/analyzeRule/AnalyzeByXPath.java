package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeByXPath {
    private JXDocument jxDocument;

    public AnalyzeByXPath parse(String doc) {
        // 给表格标签添加完整的框架结构,否则会丢失表格标签;html标准不允许表格标签独立在table之外
        if (doc.endsWith("</td>")) {
            doc = "<tr>" + doc + "</tr>";
        }
        if (doc.endsWith("</tr>") || doc.endsWith("</tbody>")) {
            doc = "<table>" + doc + "</table>";
        }
        jxDocument = JXDocument.create(doc);
        return this;
    }

    Elements getElements(String xPath) {
        if (TextUtils.isEmpty(xPath)) {
            return null;
        }
        Elements elements = new Elements();
        String elementsType;
        String rules[];
        if (xPath.contains("&&")) {
            rules = xPath.split("&&");
            elementsType = "&";
        } else if (xPath.contains("%%")) {
            rules = xPath.split("%%");
            elementsType = "%";
        } else {
            rules = xPath.split("\\|\\|");
            elementsType = "|";
        }
        if (rules.length == 1) {
            try {
                List<Object> objects = jxDocument.sel(rules[0]);
                for (Object object : objects) {
                    if (object instanceof Element) {
                        elements.add((Element) object);
                    }
                }
                return elements;
            } catch (Exception e) {
                return null;
            }
        } else {
            List<Elements> results = new ArrayList<>();
            for (String rl : rules) {
                Elements temp = getElements(rl);
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
                            for (Elements temp : results) {
                                if (i < temp.size()) {
                                    elements.add(temp.get(i));
                                }
                            }
                        }
                        break;
                    default:
                        for (Elements temp : results) {
                            elements.addAll(temp);
                        }
                }
            }
        }
        return elements;
    }

    List<String> getStringList(String xPath) {
        List<String> result = new ArrayList<>();
        String elementsType;
        String rules[];
        if (xPath.contains("&&")) {
            rules = xPath.split("&&");
            elementsType = "&";
        } else if (xPath.contains("%%")) {
            rules = xPath.split("%%");
            elementsType = "%";
        } else {
            rules = xPath.split("\\|\\|");
            elementsType = "|";
        }
        if (rules.length == 1) {
            List<Object> objects = jxDocument.sel(xPath);
            for (Object object : objects) {
                if (object instanceof String) {
                    result.add((String) object);
                }
            }
            return result;
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
                switch (elementsType) {
                    case "%":
                        for (int i = 0; i < results.get(0).size(); i++) {
                            for (List<String> temp : results) {
                                if (i < temp.size()) {
                                    result.add(temp.get(i));
                                }
                            }
                        }
                        break;
                    default:
                        for (List<String> temp : results) {
                            result.addAll(temp);
                        }
                }
            }
        }
        return result;
    }

    public String getString(String rule) {
        String result;
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
            Object object = jxDocument.selOne(rule);
            result = object instanceof Element ? ((Element) object).html() : (String) object;
            return result;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String rl : rules) {
                String temp = getString(rl);
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
}
