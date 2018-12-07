package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.utils.NetworkUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeByXPath {
    private JXDocument jxDocument;

    public AnalyzeByXPath(Document doc) {
        jxDocument = JXDocument.create(doc);
    }

    public AnalyzeByXPath(Elements doc) {
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

    public String getString(String xPath, String baseUrl) {
        String str = (String) jxDocument.selOne(xPath);
        if (!TextUtils.isEmpty(baseUrl)) {
            str = NetworkUtil.getAbsoluteURL(baseUrl, str);
        }
        return str;
    }

}
