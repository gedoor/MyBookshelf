package com.monke.monkeybook.base.observer;

import com.monke.monkeybook.utils.NetworkUtil;

public class SimpleObserClass<T> {
    private int code;
    private T t;

    public SimpleObserClass(T t){
        this(t,NetworkUtil.SUCCESS);
    }

    public SimpleObserClass(T t,int code){
        this.t = t;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Boolean success(){
        return code == NetworkUtil.SUCCESS;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
