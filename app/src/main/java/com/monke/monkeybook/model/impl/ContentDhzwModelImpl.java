package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.model.IWebContentModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

import java.util.List;

public class ContentDhzwModelImpl implements IWebContentModel{
    public static final String TAG = "http://www.dhzw.org";

    public static ContentDhzwModelImpl getInstance() {
        return new ContentDhzwModelImpl();
    }

    private ContentDhzwModelImpl() {

    }
    @Override
    public String analyBookcontent(String s, String realUrl) throws Exception {
        Document doc = Jsoup.parse(s);
        List<TextNode> contentEs = doc.getElementById("BookText").textNodes();
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < contentEs.size(); i++) {
            String temp = contentEs.get(i).text().trim();
            temp = temp.replaceAll(" ","").replaceAll(" ","");
            if (temp.length() > 0) {
                content.append("\u3000\u3000" + contentEs.get(i).text().trim().replaceAll("　",""));
                if (i < contentEs.size() - 1) {
                    content.append("\r\n");
                }
            }
        }
        return content.toString();
    }
}
