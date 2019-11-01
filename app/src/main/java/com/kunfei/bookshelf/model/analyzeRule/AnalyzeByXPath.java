package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeByXPath {
    private JXDocument jxDocument;
    private JXNode jxNode;

    public AnalyzeByXPath parse(Object doc) {
        if (doc instanceof JXNode) {
            jxNode = (JXNode) doc;
            if (!jxNode.isElement()) {
                jxDocument = strToJXDocument(doc.toString());
                jxNode = null;
            }
        } else if (doc instanceof Document) {
            jxDocument = JXDocument.create((Document) doc);
            jxNode = null;
        } else if (doc instanceof Element) {
            jxDocument = JXDocument.create(new Elements((Element) doc));
            jxNode = null;
        } else if (doc instanceof Elements) {
            jxDocument = JXDocument.create((Elements) doc);
            jxNode = null;
        } else {
            jxDocument = strToJXDocument(doc.toString());
            jxNode = null;
        }
        return this;
    }

    private JXDocument strToJXDocument(String html) {
        if (html.endsWith("</td>")) {
            html = String.format("<tr>%s</tr>", html);
        }
        if (html.endsWith("</tr>") || html.endsWith("</tbody>")) {
            html = String.format("<table>%s</table>", html);
        }
        return JXDocument.create(html);
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
            if (jxNode != null) {
                return jxNode.sel(rules[0]);
            }
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
                    for (List<JXNode> temp : results) {
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
            List<JXNode> jxNodes;
            if (jxNode != null) {
                jxNodes = jxNode.sel(xPath);
            } else {
                jxNodes = jxDocument.selN(xPath);
            }
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
            List<JXNode> jxNodes;
            if (jxNode != null) {
                jxNodes = jxNode.sel(rule);
            } else {
                jxNodes = jxDocument.selN(rule);
            }
            if (jxNodes == null) return null;
            return TextUtils.join(",", jxNodes);
        } else {
            List<String> textS = new ArrayList<>();
            for (String rl : rules) {
                String temp = getString(rl);
                if (!TextUtils.isEmpty(temp)) {
                    textS.add(temp);
                    if (elementsType.equals("|")) {
                        break;
                    }
                }
            }
            return TextUtils.join(",", textS).trim();
        }
    }
}
