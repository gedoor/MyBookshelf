package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by GKF on 2017/12/14.
 * 书源信息
 */
@Entity
public class BookSourceBean  implements Parcelable,Cloneable{
    @Id
    private String bookSourceUrl;

    private String bookSourceName;

    private int serialNumber;

    private boolean enable;

    protected BookSourceBean(Parcel in) {
        bookSourceUrl = in.readString();
        bookSourceName = in.readString();
        serialNumber = in.readInt();
        enable = in.readByte() != 0;
        bookSourceSearchUrl = in.readString();
    }

    public static final Creator<BookSourceBean> CREATOR = new Creator<BookSourceBean>() {
        @Override
        public BookSourceBean createFromParcel(Parcel in) {
            return new BookSourceBean(in);
        }

        @Override
        public BookSourceBean[] newArray(int size) {
            return new BookSourceBean[size];
        }
    };

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

    @Generated(hash = 681452226)
    public BookSourceBean(String bookSourceUrl, String bookSourceName, int serialNumber,
            boolean enable, String bookSourceSearchUrl) {
        this.bookSourceUrl = bookSourceUrl;
        this.bookSourceName = bookSourceName;
        this.serialNumber = serialNumber;
        this.enable = enable;
        this.bookSourceSearchUrl = bookSourceSearchUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bookSourceUrl);
        parcel.writeString(bookSourceName);
        parcel.writeInt(serialNumber);
        parcel.writeByte((byte) (enable ? 1 : 0));
        parcel.writeString(bookSourceSearchUrl);
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public boolean getEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
