//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.content;

import com.monke.monkeybook.model.IWebContentModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import java.util.List;

public class Content44pqModelImpl implements IWebContentModel{
    public static final String TAG = "http://www.44pq.net";

    public static Content44pqModelImpl getInstance() {
        return new Content44pqModelImpl();
    }

    private Content44pqModelImpl() {

    }
    @Override
    public String analyBookcontent(String s, String realUrl) throws Exception {
        Document doc = Jsoup.parse(s);
        List<TextNode> contentEs = doc.getElementById("content").textNodes();
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < contentEs.size(); i++) {
            String temp = contentEs.get(i).text().trim();
            temp = temp.replaceAll(" ","").replaceAll(" ","");
            if (temp.length() > 0) {
                content.append("\u3000\u3000" + temp);
                if (i < contentEs.size() - 1) {
                    content.append("\r\n");
                }
            }
        }
        return content.toString();
    }
}
