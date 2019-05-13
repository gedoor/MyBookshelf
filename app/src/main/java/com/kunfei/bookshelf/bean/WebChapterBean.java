package com.kunfei.bookshelf.bean;

import java.util.LinkedHashSet;
import java.util.List;

public class WebChapterBean {
    private String url;

    private List<ChapterListBean> data;

    private LinkedHashSet<String> nextUrlList;

    public WebChapterBean(String url) {
        this.url = url;
    }

    public WebChapterBean(List<ChapterListBean> data, LinkedHashSet<String> nextUrlList) {
        this.data = data;
        this.nextUrlList = nextUrlList;
    }

    public List<ChapterListBean> getData() {
        return data;
    }

    public void setData(List<ChapterListBean> data) {
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
