package com.kunfei.bookshelf.bean;

import java.util.LinkedHashSet;
import java.util.List;

public class WebChapterBean {
    private String url;

    private List<BookChapterBean> data;

    private LinkedHashSet<String> nextUrlList;

    public WebChapterBean(String url) {
        this.url = url;
    }

    public WebChapterBean(List<BookChapterBean> data, LinkedHashSet<String> nextUrlList) {
        this.data = data;
        this.nextUrlList = nextUrlList;
    }

    public List<BookChapterBean> getData() {
        return data;
    }

    public void setData(List<BookChapterBean> data) {
        this.data = data;
    }

    public LinkedHashSet<String> getNextUrlList() {
        return nextUrlList;
    }

    public String getUrl() {
        return url;
    }

    public boolean noData() {
        return data == null;
    }
}
