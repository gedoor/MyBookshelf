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
        List<String> stringList = new ArrayList<>();
        List<Object> objects = jxDocument.sel(xPath);
        for (Object object : objects) {
            if (object instanceof String) {
                stringList.add((String) object);
            }
        }
        return stringList;
    }

    public String getString(String rule, String baseUrl) {
        String result;
        Object object = jxDocument.selOne(rule);
        if (!TextUtils.isEmpty(baseUrl)) {
            result = NetworkUtil.getAbsoluteURL(baseUrl, (String) object);
        } else if (object instanceof Element) {
            result = ((Element) object).html()
                    .replaceAll("(?i)<(br[\\s/]*|p.*?|div.*?|/p|/div)>", "\n")
                    .replaceAll("<.*?>", "");
        } else {
            result = (String) object;
        }
        return result;
    }
}
