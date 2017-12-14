package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;

/**
 * Created by GKF on 2017/12/14.
 * 书源信息
 */

public class BookSourceBean{

    private String bookSourceUrl;

    private String bookSourceName;

    public String getBookSourceSearchUrl() {
        return bookSourceSearchUrl;
    }

    public void setBookSourceSearchUrl(String bookSourceSearchUrl) {
        this.bookSourceSearchUrl = bookSourceSearchUrl;
    }

    public String getBookSourceName() {

        return bookSourceName;
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    private String bookSourceSearchUrl;

    public String getBookSourceUrl() {
        return bookSourceUrl;
    }

    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }

    public BookSourceBean() {

    }

}
