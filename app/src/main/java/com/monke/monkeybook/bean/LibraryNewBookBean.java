package com.monke.monkeybook.bean;

public class LibraryNewBookBean {
    private String name;
    private String url;
    private String tag;
    private String orgin;

    public LibraryNewBookBean(String name, String url, String tag, String orgin) {
        this.name = name;
        this.url = url;
        this.tag = tag;
        this.orgin = orgin;
    }

    public String getOrgin() {
        return orgin;
    }

    public void setOrgin(String orgin) {
        this.orgin = orgin;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
