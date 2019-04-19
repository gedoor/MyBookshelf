package com.kunfei.bookshelf.web.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

public class ReturnData implements Parcelable {

    private boolean isSuccess;

    private int errorCode;

    private String errorMsg;

    private Object data;

    public ReturnData() {
    }

    protected ReturnData(Parcel in) {
        isSuccess = in.readByte() != 0;
        errorCode = in.readInt();
        errorMsg = in.readString();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSuccess ? 1 : 0));
        dest.writeInt(errorCode);
        dest.writeString(errorMsg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReturnData> CREATOR = new Creator<ReturnData>() {
        @Override
        public ReturnData createFromParcel(Parcel in) {
            return new ReturnData(in);
        }

        @Override
        public ReturnData[] newArray(int size) {
            return new ReturnData[size];
        }
    };

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

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
