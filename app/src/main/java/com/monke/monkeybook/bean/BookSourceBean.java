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

    private String ruleBookName;
    private String ruleBookAuthor;
    private String ruleChapterUrl;
    private String ruleCoverUrl;
    private String ruleIntroduce;
    private String ruleContentUrl;
    private String ruleBookContent;
    private String ruleSearchUrl;

    protected BookSourceBean(Parcel in) {
        bookSourceUrl = in.readString();
        bookSourceName = in.readString();
        serialNumber = in.readInt();
        enable = in.readByte() != 0;

        ruleBookName = in.readString();
        ruleBookAuthor = in.readString();
        ruleChapterUrl = in.readString();
        ruleCoverUrl = in.readString();
        ruleIntroduce = in.readString();
        ruleContentUrl = in.readString();
        ruleBookContent = in.readString();
        ruleSearchUrl = in.readString();
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

    public String getBookSourceName() {

        return bookSourceName;
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    public String getBookSourceUrl() {
        return bookSourceUrl;
    }

    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }

    @Generated(hash = 892064787)
    public BookSourceBean(String bookSourceUrl, String bookSourceName, int serialNumber,
            boolean enable, String ruleBookName, String ruleBookAuthor, String ruleChapterUrl,
            String ruleCoverUrl, String ruleIntroduce, String ruleContentUrl,
            String ruleBookContent, String ruleSearchUrl) {
        this.bookSourceUrl = bookSourceUrl;
        this.bookSourceName = bookSourceName;
        this.serialNumber = serialNumber;
        this.enable = enable;
        this.ruleBookName = ruleBookName;
        this.ruleBookAuthor = ruleBookAuthor;
        this.ruleChapterUrl = ruleChapterUrl;
        this.ruleCoverUrl = ruleCoverUrl;
        this.ruleIntroduce = ruleIntroduce;
        this.ruleContentUrl = ruleContentUrl;
        this.ruleBookContent = ruleBookContent;
        this.ruleSearchUrl = ruleSearchUrl;
    }

    @Generated(hash = 1512565980)
    public BookSourceBean() {
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

        parcel.writeString(ruleBookName);
        parcel.writeString(ruleBookAuthor);
        parcel.writeString(ruleChapterUrl);
        parcel.writeString(ruleCoverUrl);
        parcel.writeString(ruleIntroduce);
        parcel.writeString(ruleContentUrl);
        parcel.writeString(ruleBookContent);
        parcel.writeString(ruleSearchUrl);
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

    public String getRuleBookName() {
        return this.ruleBookName;
    }

    public void setRuleBookName(String ruleBookName) {
        this.ruleBookName = ruleBookName;
    }

    public String getRuleBookAuthor() {
        return this.ruleBookAuthor;
    }

    public void setRuleBookAuthor(String ruleBookAutoher) {
        this.ruleBookAuthor = ruleBookAutoher;
    }

    public String getRuleChapterUrl() {
        return this.ruleChapterUrl;
    }

    public void setRuleChapterUrl(String ruleChapterUrl) {
        this.ruleChapterUrl = ruleChapterUrl;
    }

    public String getRuleCoverUrl() {
        return this.ruleCoverUrl;
    }

    public void setRuleCoverUrl(String ruleCoverUrl) {
        this.ruleCoverUrl = ruleCoverUrl;
    }

    public String getRuleIntroduce() {
        return this.ruleIntroduce;
    }

    public void setRuleIntroduce(String ruleIntroduce) {
        this.ruleIntroduce = ruleIntroduce;
    }

    public String getRuleBookContent() {
        return this.ruleBookContent;
    }

    public void setRuleBookContent(String ruleBookContent) {
        this.ruleBookContent = ruleBookContent;
    }

    public String getRuleSearchUrl() {
        return this.ruleSearchUrl;
    }

    public void setRuleSearchUrl(String ruleSearchUrl) {
        this.ruleSearchUrl = ruleSearchUrl;
    }

    public String getRuleContentUrl() {
        return this.ruleContentUrl;
    }

    public void setRuleContentUrl(String ruleContentUrl) {
        this.ruleContentUrl = ruleContentUrl;
    }
}
