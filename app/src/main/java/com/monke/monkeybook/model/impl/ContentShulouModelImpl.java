//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.model.IWebContentModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ContentShulouModelImpl implements IWebContentModel{
    public static final String TAG = "http://www.shulou.cc";

    public static ContentShulouModelImpl getInstance(){
        return new ContentShulouModelImpl();
    }
    private ContentShulouModelImpl(){

    }

    @Override
    public String analyBookcontent(String s, String realUrl) throws Exception{
        Document doc = Jsoup.parse(s);
        Element contentE = doc.getElementById("content");
        String contentString = contentE.toString();
        contentString = contentString.replaceAll(" ", "").replaceAll("\n", "").replaceAll("&nbsp;", "").replaceAll("<divid=\"content\">", "").replaceAll("</div>", "").replaceAll("<p></p>","").replaceAll("<br><br>", "\r\n\u3000\u3000");
        contentString = "\u3000\u3000" + contentString;
        return contentString;
    }
}
