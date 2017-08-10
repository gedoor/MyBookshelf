package com.monke.monkeybook.bean;

import java.util.List;

public class LibraryKindBookListBean {
    private String kindName;
    private String kindUrl;
    private List<SearchBookBean> books;

    public String getKindName() {
        return kindName;
    }

    public void setKindName(String kindName) {
        this.kindName = kindName;
    }

    public List<SearchBookBean> getBooks() {
        return books;
    }

    public void setBooks(List<SearchBookBean> books) {
        this.books = books;
    }

    public String getKindUrl() {
        return kindUrl;
    }

    public void setKindUrl(String kindUrl) {
        this.kindUrl = kindUrl;
    }
}
