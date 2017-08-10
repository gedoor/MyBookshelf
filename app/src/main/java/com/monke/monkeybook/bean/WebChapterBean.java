package com.monke.monkeybook.bean;

public class WebChapterBean<T> {
    private T data;

    private Boolean next;

    public WebChapterBean(T data,Boolean next){
        this.data = data;
        this.next = next;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getNext() {
        return next;
    }

    public void setNext(Boolean next) {
        this.next = next;
    }
}
