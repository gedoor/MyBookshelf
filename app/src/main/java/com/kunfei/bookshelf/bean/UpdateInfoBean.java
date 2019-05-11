package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateInfoBean implements Parcelable {
    private String lastVersion;
    private String url;
    private String detail;
    private Boolean upDate;

    public UpdateInfoBean() {

    }

    protected UpdateInfoBean(Parcel in) {
        lastVersion = in.readString();
        url = in.readString();
        detail = in.readString();
        upDate = in.readByte() != 0;
    }

    public static final Creator<UpdateInfoBean> CREATOR = new Creator<UpdateInfoBean>() {
        @Override
        public UpdateInfoBean createFromParcel(Parcel in) {
            return new UpdateInfoBean(in);
        }

        @Override
        public UpdateInfoBean[] newArray(int size) {
            return new UpdateInfoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(lastVersion);
        parcel.writeString(url);
        parcel.writeString(detail);
        parcel.writeByte((byte) (upDate ? 1 : 0));
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Boolean getUpDate() {
        return upDate;
    }

    public void setUpDate(Boolean upDate) {
        this.upDate = upDate;
    }
}
