//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.content;

import com.monke.monkeybook.model.IWebContentModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import java.util.List;

public class ContentSnwx8ModelImpl implements IWebContentModel{
    public static final String TAG = "http://www.snwx8.com";

    public static ContentSnwx8ModelImpl getInstance() {
        return new ContentSnwx8ModelImpl();
    }

    private ContentSnwx8ModelImpl() {

    }
    @Override
    public String analyBookcontent(String s, String realUrl) throws Exception {
        Document doc = Jsoup.parse(s);
        List<TextNode> contentEs = doc.getElementById("BookText").textNodes();
        StringBuilder content = new StringBuilder();
        for(int i=0;i<contentEs.size();i++){
            String temp = contentEs.get(i).text().trim();
            temp = temp.replaceAll(" ","").replaceAll(" ","");
            if(temp.length()>0){
                content.append("\u3000\u3000" + temp);
                if (i < contentEs.size() - 1) {
                    content.append("\r\n");
                }
            }
        }
        return content.toString();
    }
}
