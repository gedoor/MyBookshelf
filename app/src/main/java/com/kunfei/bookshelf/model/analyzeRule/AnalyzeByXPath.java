package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeByXPath {
    private JXDocument jxDocument;

    public AnalyzeByXPath parse(Object doc) {
        if (doc instanceof Document) {
            jxDocument = JXDocument.create((Document) doc);
        } else {
            String html = doc.toString();
            // 给表格标签添加完整的框架结构,否则会丢失表格标签;html标准不允许表格标签独立在table之外
            if (html.endsWith("</td>")) {
                html = String.format("<tr>%s</tr>", html);
            }
            if (html.endsWith("</tr>") || html.endsWith("</tbody>")) {
                html = String.format("<table>%s</table>", html);
            }
            jxDocument = JXDocument.create(html);
        }
        return this;
    }

    List<JXNode> getElements(String xPath) {
        if (TextUtils.isEmpty(xPath)) {
            return null;
        }
        List<JXNode> jxNodes = new ArrayList<>();
        String elementsType;
        String[] rules;
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
            return jxDocument.selN(rules[0]);
        } else {
            List<List<JXNode>> results = new ArrayList<>();
            for (String rl : rules) {
                List<JXNode> temp = getElements(rl);
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
                        for (List<JXNode> temp : results) {
                            if (i < temp.size()) {
                                jxNodes.add(temp.get(i));
                            }
                        }
                    }
                } else {
                    for (List<JXNode> temp: results) {
                        jxNodes.addAll(temp);
                    }
                }
            }
        }
        return jxNodes;
    }

    List<String> getStringList(String xPath) {
        List<String> result = new ArrayList<>();
        String elementsType;
        String[] rules;
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
            List<JXNode> jxNodes = jxDocument.selN(xPath);
            for (JXNode jxNode : jxNodes) {
                /*if(jxNode.isString()){
                    result.add(String.valueOf(jxNode));
                }*/
                result.add(String.valueOf(jxNode));
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
        }
        return result;
    }

    public String getString(String rule) {
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
            /*Object object = jxDocument.selOne(rule);
            result = object instanceof Element ? ((Element) object).html() : (String) object;*/
            List<JXNode> jxNodes = jxDocument.selN(rule);
            if(jxNodes == null) return null;
            return TextUtils.join(",", jxNodes);
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
