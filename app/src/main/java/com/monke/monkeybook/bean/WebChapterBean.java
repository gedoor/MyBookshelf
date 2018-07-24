//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

public class WebChapterBean<T> {
    private T data;

    private String nextUrl;

    public WebChapterBean(T data,String nextUrl){
        this.data = data;
        this.nextUrl = nextUrl;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }
}
