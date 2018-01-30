package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Objects;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/14.
 * 书源信息
 */
@Entity
public class BookSourceBean implements Parcelable, Cloneable {
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
    private String ruleChapterList;
    private String ruleChapterName;
    private String ruleContentUrl;
    private String ruleBookContent;
    private String ruleSearchUrl;
    private String ruleSearchList;
    private String ruleSearchName;
    private String ruleSearchAuthor;
    private String ruleSearchKind;
    private String ruleSearchLastChapter;
    private String ruleSearchCoverUrl;
    private String ruleSearchNoteUrl;

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
        ruleChapterList = in.readString();
        ruleChapterName = in.readString();
        ruleContentUrl = in.readString();
        ruleBookContent = in.readString();
        ruleSearchUrl = in.readString();
        ruleSearchList = in.readString();
        ruleSearchName = in.readString();
        ruleSearchAuthor = in.readString();
        ruleSearchKind = in.readString();
        ruleSearchLastChapter = in.readString();
        ruleSearchCoverUrl = in.readString();
        ruleSearchNoteUrl = in.readString();
    }

    @Generated(hash = 260114574)
    public BookSourceBean(String bookSourceUrl, String bookSourceName, int serialNumber,
                          boolean enable, String ruleBookName, String ruleBookAuthor, String ruleChapterUrl,
                          String ruleCoverUrl, String ruleIntroduce, String ruleChapterList,
                          String ruleChapterName, String ruleContentUrl, String ruleBookContent,
                          String ruleSearchUrl, String ruleSearchList, String ruleSearchName,
                          String ruleSearchAuthor, String ruleSearchKind, String ruleSearchLastChapter,
                          String ruleSearchCoverUrl, String ruleSearchNoteUrl) {
        this.bookSourceUrl = bookSourceUrl;
        this.bookSourceName = bookSourceName;
        this.serialNumber = serialNumber;
        this.enable = enable;
        this.ruleBookName = ruleBookName;
        this.ruleBookAuthor = ruleBookAuthor;
        this.ruleChapterUrl = ruleChapterUrl;
        this.ruleCoverUrl = ruleCoverUrl;
        this.ruleIntroduce = ruleIntroduce;
        this.ruleChapterList = ruleChapterList;
        this.ruleChapterName = ruleChapterName;
        this.ruleContentUrl = ruleContentUrl;
        this.ruleBookContent = ruleBookContent;
        this.ruleSearchUrl = ruleSearchUrl;
        this.ruleSearchList = ruleSearchList;
        this.ruleSearchName = ruleSearchName;
        this.ruleSearchAuthor = ruleSearchAuthor;
        this.ruleSearchKind = ruleSearchKind;
        this.ruleSearchLastChapter = ruleSearchLastChapter;
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
    }

    public BookSourceBean() {
    }

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
        parcel.writeString(ruleChapterList);
        parcel.writeString(ruleChapterName);
        parcel.writeString(ruleContentUrl);
        parcel.writeString(ruleBookContent);
        parcel.writeString(ruleSearchUrl);
        parcel.writeString(ruleSearchList);
        parcel.writeString(ruleSearchName);
        parcel.writeString(ruleSearchAuthor);
        parcel.writeString(ruleSearchKind);
        parcel.writeString(ruleSearchLastChapter);
        parcel.writeString(ruleSearchCoverUrl);
        parcel.writeString(ruleSearchNoteUrl);
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

    public String getRuleSearchName() {
        return this.ruleSearchName;
    }

    public void setRuleSearchName(String ruleSearchName) {
        this.ruleSearchName = ruleSearchName;
    }

    public String getRuleSearchAuthor() {
        return this.ruleSearchAuthor;
    }

    public void setRuleSearchAuthor(String ruleSearchAuthor) {
        this.ruleSearchAuthor = ruleSearchAuthor;
    }

    public String getRuleSearchKind() {
        return this.ruleSearchKind;
    }

    public void setRuleSearchKind(String ruleSearchKind) {
        this.ruleSearchKind = ruleSearchKind;
    }

    public String getRuleSearchLastChapter() {
        return this.ruleSearchLastChapter;
    }

    public void setRuleSearchLastChapter(String ruleSearchLastChapter) {
        this.ruleSearchLastChapter = ruleSearchLastChapter;
    }

    public String getRuleSearchCoverUrl() {
        return this.ruleSearchCoverUrl;
    }

    public void setRuleSearchCoverUrl(String ruleSearchCoverUrl) {
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
    }

    public String getRuleSearchNoteUrl() {
        return this.ruleSearchNoteUrl;
    }

    public void setRuleSearchNoteUrl(String ruleSearchNoteUrl) {
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
    }

    public String getRuleSearchList() {
        return this.ruleSearchList;
    }

    public void setRuleSearchList(String ruleSearchList) {
        this.ruleSearchList = ruleSearchList;
    }

    public String getRuleChapterList() {
        return this.ruleChapterList;
    }

    public void setRuleChapterList(String ruleChapterList) {
        this.ruleChapterList = ruleChapterList;
    }

    public String getRuleChapterName() {
        return this.ruleChapterName;
    }

    public void setRuleChapterName(String ruleChapterName) {
        this.ruleChapterName = ruleChapterName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookSourceBean) {
            BookSourceBean bs = (BookSourceBean) obj;
            if (!Objects.equals(bookSourceUrl, bs.bookSourceUrl) && (!isEmpty(bookSourceUrl) || !isEmpty(bs.bookSourceUrl)))
                return false;
            if (!Objects.equals(bookSourceName, bs.bookSourceName) && (!isEmpty(bookSourceName) || !isEmpty(bs.bookSourceName)))
                return false;
            if (!Objects.equals(ruleBookName, bs.ruleBookName) && (!isEmpty(ruleBookName) || !isEmpty(bs.ruleBookName)))
                return false;
            if (!Objects.equals(ruleBookAuthor, bs.ruleBookAuthor) && (!isEmpty(ruleBookAuthor) || !isEmpty(bs.ruleBookAuthor)))
                return false;
            if (!Objects.equals(ruleChapterUrl, bs.ruleChapterUrl) && (!isEmpty(ruleChapterUrl) || !isEmpty(bs.ruleChapterUrl)))
                return false;
            if (!Objects.equals(ruleCoverUrl, bs.ruleCoverUrl) && (!isEmpty(ruleCoverUrl) || !isEmpty(bs.ruleCoverUrl)))
                return false;
            if (!Objects.equals(ruleIntroduce, bs.ruleIntroduce) && (!isEmpty(ruleIntroduce) || !isEmpty(bs.ruleIntroduce)))
                return false;
            if (!Objects.equals(ruleChapterList, bs.ruleChapterList) && (!isEmpty(ruleChapterList) || !isEmpty(bs.ruleChapterList)))
                return false;
            if (!Objects.equals(ruleChapterName, bs.ruleChapterName) && (!isEmpty(ruleChapterName) || !isEmpty(bs.ruleChapterName)))
                return false;
            if (!Objects.equals(ruleContentUrl, bs.ruleContentUrl) && (!isEmpty(ruleContentUrl) || !isEmpty(bs.ruleContentUrl)))
                return false;
            if (!Objects.equals(ruleBookContent, bs.ruleBookContent) && (!isEmpty(ruleBookContent) || !isEmpty(bs.ruleBookContent)))
                return false;
            if (!Objects.equals(ruleSearchUrl, bs.ruleSearchUrl) && (!isEmpty(ruleSearchUrl) || !isEmpty(bs.ruleSearchUrl)))
                return false;
            if (!Objects.equals(ruleSearchList, bs.ruleSearchList) && (!isEmpty(ruleSearchList) || !isEmpty(bs.ruleSearchList)))
                return false;
            if (!Objects.equals(ruleSearchName, bs.ruleSearchName) && (!isEmpty(ruleSearchName) || !isEmpty(bs.ruleSearchName)))
                return false;
            if (!Objects.equals(ruleSearchAuthor, bs.ruleSearchAuthor) && (!isEmpty(ruleSearchAuthor) || !isEmpty(bs.ruleSearchAuthor)))
                return false;
            if (!Objects.equals(ruleSearchKind, bs.ruleSearchKind) && (!isEmpty(ruleSearchKind) || !isEmpty(bs.ruleSearchKind)))
                return false;
            if (!Objects.equals(ruleSearchLastChapter, bs.ruleSearchLastChapter) && (!isEmpty(ruleSearchLastChapter) || !isEmpty(bs.ruleSearchLastChapter)))
                return false;
            if (!Objects.equals(ruleSearchCoverUrl, bs.ruleSearchCoverUrl) && (!isEmpty(ruleSearchCoverUrl) || !isEmpty(bs.ruleSearchCoverUrl)))
                return false;
            if (!Objects.equals(ruleSearchNoteUrl, bs.ruleSearchNoteUrl) && (!isEmpty(ruleSearchNoteUrl) || !isEmpty(bs.ruleSearchNoteUrl)))
                return false;
            return true;
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
