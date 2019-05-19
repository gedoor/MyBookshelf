package com.kunfei.bookshelf.bean;

public class TwoDataBean<T, S> {
    private T data1;
    private S data2;

    public TwoDataBean(T data1, S data2) {
        this.data1 = data1;
        this.data2 = data2;
    }

    public T getData1() {
        return data1;
    }

    public S getData2() {
        return data2;
    }
}
