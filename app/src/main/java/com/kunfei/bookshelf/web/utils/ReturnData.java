package com.kunfei.bookshelf.web.utils;


public class ReturnData {

    private boolean isSuccess;

    private int errorCode;

    private String errorMsg;

    private Object data;

    public ReturnData() {
        this.isSuccess = false;
        this.errorMsg = "未知错误,请联系开发者!";
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public ReturnData setErrorMsg(String errorMsg) {
        this.isSuccess = false;
        this.errorMsg = errorMsg;
        return this;
    }

    public Object getData() {
        return data;
    }

    public ReturnData setData(Object data) {
        this.isSuccess = true;
        this.errorMsg = "";
        this.data = data;
        return this;
    }
}
