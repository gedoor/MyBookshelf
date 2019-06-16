package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class CookieBean implements Parcelable {

    @Id
    private String url;
    private String cookie;

    private CookieBean(Parcel in) {
        url = in.readString();
        cookie = in.readString();
    }

    @Generated(hash = 517179762)
    public CookieBean(String url, String cookie) {
        this.url = url;
        this.cookie = cookie;
    }

    @Generated(hash = 769081142)
    public CookieBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(cookie);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCookie() {
        return cookie == null ? "" : cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public static final Creator<CookieBean> CREATOR = new Creator<CookieBean>() {
        @Override
        public CookieBean createFromParcel(Parcel in) {
            return new CookieBean(in);
        }

        @Override
        public CookieBean[] newArray(int size) {
            return new CookieBean[size];
        }
    };
}
