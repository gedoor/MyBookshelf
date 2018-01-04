package com.monke.monkeybook.common.api;

/**
 * Created by GKF on 2018/1/4.
 */

public class Result<T> {
    public int code;
    public String msg;
    public T data;
}
