package com.monke.monkeybook.model.analyzeRule;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import java.util.List;

public class AnalyzeByXPath {
    JXDocument jxDocument;

    AnalyzeByXPath(Elements doc) {
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



}
