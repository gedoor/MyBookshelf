package com.monke.monkeybook.bean;

public class FindKindBean {
    private String tag;
    private String kindName;
    private String kindUrl;

    public FindKindBean() {

    }

    public FindKindBean(String tag, String kindName, String kindUrl) {
        this.tag = tag;
        this.kindName = kindName;
        this.kindUrl = kindUrl;
    }

    public String getKindName() {
        return kindName;
    }

    public void setKindName(String kindName) {
        this.kindName = kindName;
    }

    public String getKindUrl() {
        return kindUrl;
    }

    public void setKindUrl(String kindUrl) {
        this.kindUrl = kindUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
