package com.kunfei.bookshelf.model.analyzeRule;

import android.text.TextUtils;

import com.kunfei.bookshelf.utils.NetworkUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeByXPath {
    private JXDocument jxDocument;

    public void parse(Document doc) {
        jxDocument = JXDocument.create(doc);
    }

    public void parse(Elements doc) {
        jxDocument = new JXDocument(doc);
    }

    public Elements getElements(String xPath) {
        Elements elements = new Elements();
        List<Object> objects = jxDocument.sel(xPath);
        for (Object object : objects) {
            if (object instanceof Element) {
                elements.add((Element) object);
            }
        }
        return elements;
    }

    public List<String> getStringList(String xPath, String baseUrl) {
        List<String> stringList = new ArrayList<>();
        List<Object> objects = jxDocument.sel(xPath);
        for (Object object : objects) {
            if (object instanceof String) {
                if (!TextUtils.isEmpty(baseUrl)) {
                    String url = NetworkUtil.getAbsoluteURL(baseUrl, (String) object);
                    if (!stringList.contains(url)) {
                        stringList.add(url);
                    }
                } else {
                    stringList.add((String) object);
                }
            }
        }
        return stringList;
    }

    public String getString(String rule, String baseUrl) {
        String result;
        SourceRule sourceRule = splitSourceRule(rule);
        Object object = jxDocument.selOne(sourceRule.rule);
        if (!TextUtils.isEmpty(baseUrl)) {
            result = NetworkUtil.getAbsoluteURL(baseUrl, (String) object);
        } else if (object instanceof Element) {
            result = ((Element) object).html()
                    .replaceAll("(?i)<(br[\\s/]*|p.*?|div.*?|/p|/div)>", "\n")
                    .replaceAll("<.*?>", "");
        } else {
            result = (String) object;
        }
        if (!TextUtils.isEmpty(sourceRule.jsStr)) {
            result = (String) AnalyzeRule.evalJS(sourceRule.jsStr, result);
        }
        return result;
    }

    private SourceRule splitSourceRule(String rule) {
        SourceRule sourceRule = new SourceRule();
        String str[] = rule.split("@js:");
        sourceRule.rule = str[0];
        if (str.length > 1) {
            sourceRule.jsStr = str[1];
        }
        return sourceRule;
    }

    class SourceRule {
        String rule;
        String jsStr;
    }
}
