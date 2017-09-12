//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.model.IWebContentModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import java.util.List;

public class ContentYb3ModelImpl implements IWebContentModel{
    public static final String TAG = "http://www.yb3.cc";

    public static ContentYb3ModelImpl getInstance() {
        return new ContentYb3ModelImpl();
    }

    private ContentYb3ModelImpl() {

    }
    @Override
    public String analyBookcontent(String s, String realUrl) throws Exception {
        Document doc = Jsoup.parse(s);
        Element contentE = doc.getElementById("content");
        List<TextNode> contentTextNodes = contentE.textNodes();
        StringBuilder content = new StringBuilder();
        if(contentTextNodes!=null && contentTextNodes.size()>0){
            for (int i = 0; i < contentTextNodes.size(); i++) {
                if (contentTextNodes.get(i).text().trim().length() > 0) {
                    content.append("\u3000\u3000" + contentTextNodes.get(i).text().trim());
                    if (i < contentTextNodes.size() - 1) {
                        content.append("\r\n");
                    }
                }
            }
        }else{
            Elements contentEs = contentE.children();
            for (int i = 0; i < contentEs.size(); i++) {
                if (contentEs.get(i).text().toString().trim().replaceAll("　","").length() > 0) {
                    List<TextNode> tempTextNodes = contentEs.get(i).textNodes();
                    if(tempTextNodes!=null && tempTextNodes.size()>0){
                        for (int j = 0; j < tempTextNodes.size(); j++) {
                            String temp = tempTextNodes.get(j).text().trim();
                            temp = temp.replaceAll(" ","").replaceAll(" ","");
                            if (temp.length() > 0) {
                                content.append("\u3000\u3000" + temp);
                                content.append("\r\n");
                            }
                        }
                    }else{
                        content.append("\u3000\u3000" + contentEs.get(i).text().trim().replaceAll("　",""));
                        content.append("\r\n");
                    }
                }
            }
        }
        return content.toString();
    }
}
