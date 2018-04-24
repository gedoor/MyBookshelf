package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Objects;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/14.
 * 书源信息
 */
@Entity
public class BookSourceBean implements Parcelable, Cloneable {
    @Id
    private String bookSourceUrl;
    private String bookSourceName;
    private String bookSourceGroup;
    @OrderBy
    private int serialNumber;
    private boolean enable;
    private String ruleFindUrl;
    private String ruleSearchUrl;
    private String ruleSearchList;
    private String ruleSearchName;
    private String ruleSearchAuthor;
    private String ruleSearchKind;
    private String ruleSearchLastChapter;
    private String ruleSearchCoverUrl;
    private String ruleSearchNoteUrl;
    private String ruleBookName;
    private String ruleBookAuthor;
    private String ruleChapterUrl;
    private String ruleCoverUrl;
    private String ruleIntroduce;
    private String ruleChapterList;
    private String ruleChapterName;
    private String ruleContentUrl;
    private String ruleBookContent;
    private String httpUserAgent;

    @Transient
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

    protected BookSourceBean(Parcel in) {
        bookSourceUrl = in.readString();
        bookSourceName = in.readString();
        bookSourceGroup = in.readString();
        serialNumber = in.readInt();
        enable = in.readByte() != 0;

        ruleFindUrl = in.readString();
        ruleSearchUrl = in.readString();
        ruleSearchList = in.readString();
        ruleSearchName = in.readString();
        ruleSearchAuthor = in.readString();
        ruleSearchKind = in.readString();
        ruleSearchLastChapter = in.readString();
        ruleSearchCoverUrl = in.readString();
        ruleSearchNoteUrl = in.readString();
        ruleBookName = in.readString();
        ruleBookAuthor = in.readString();
        ruleChapterUrl = in.readString();
        ruleCoverUrl = in.readString();
        ruleIntroduce = in.readString();
        ruleChapterList = in.readString();
        ruleChapterName = in.readString();
        ruleContentUrl = in.readString();
        ruleBookContent = in.readString();
        httpUserAgent = in.readString();
    }

    @Generated(hash = 1298007385)
    public BookSourceBean(String bookSourceUrl, String bookSourceName, String bookSourceGroup, int serialNumber, boolean enable,
            String ruleFindUrl, String ruleSearchUrl, String ruleSearchList, String ruleSearchName, String ruleSearchAuthor,
            String ruleSearchKind, String ruleSearchLastChapter, String ruleSearchCoverUrl, String ruleSearchNoteUrl, String ruleBookName,
            String ruleBookAuthor, String ruleChapterUrl, String ruleCoverUrl, String ruleIntroduce, String ruleChapterList,
            String ruleChapterName, String ruleContentUrl, String ruleBookContent, String httpUserAgent) {
        this.bookSourceUrl = bookSourceUrl;
        this.bookSourceName = bookSourceName;
        this.bookSourceGroup = bookSourceGroup;
        this.serialNumber = serialNumber;
        this.enable = enable;
        this.ruleFindUrl = ruleFindUrl;
        this.ruleSearchUrl = ruleSearchUrl;
        this.ruleSearchList = ruleSearchList;
        this.ruleSearchName = ruleSearchName;
        this.ruleSearchAuthor = ruleSearchAuthor;
        this.ruleSearchKind = ruleSearchKind;
        this.ruleSearchLastChapter = ruleSearchLastChapter;
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
        this.ruleBookName = ruleBookName;
        this.ruleBookAuthor = ruleBookAuthor;
        this.ruleChapterUrl = ruleChapterUrl;
        this.ruleCoverUrl = ruleCoverUrl;
        this.ruleIntroduce = ruleIntroduce;
        this.ruleChapterList = ruleChapterList;
        this.ruleChapterName = ruleChapterName;
        this.ruleContentUrl = ruleContentUrl;
        this.ruleBookContent = ruleBookContent;
        this.httpUserAgent = httpUserAgent;
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
        parcel.writeString(bookSourceGroup);
        parcel.writeInt(serialNumber);
        parcel.writeByte((byte) (enable ? 1 : 0));

        parcel.writeString(ruleFindUrl);
        parcel.writeString(ruleSearchUrl);
        parcel.writeString(ruleSearchList);
        parcel.writeString(ruleSearchName);
        parcel.writeString(ruleSearchAuthor);
        parcel.writeString(ruleSearchKind);
        parcel.writeString(ruleSearchLastChapter);
        parcel.writeString(ruleSearchCoverUrl);
        parcel.writeString(ruleSearchNoteUrl);
        parcel.writeString(ruleBookName);
        parcel.writeString(ruleBookAuthor);
        parcel.writeString(ruleChapterUrl);
        parcel.writeString(ruleCoverUrl);
        parcel.writeString(ruleIntroduce);
        parcel.writeString(ruleChapterList);
        parcel.writeString(ruleChapterName);
        parcel.writeString(ruleContentUrl);
        parcel.writeString(ruleBookContent);
        parcel.writeString(httpUserAgent);
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

    public String getHttpUserAgent() {
        return this.httpUserAgent;
    }

    public void setHttpUserAgent(String httpHeaders) {
        this.httpUserAgent = httpHeaders;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookSourceBean) {
            BookSourceBean bs = (BookSourceBean) obj;
            return stringEquals(bookSourceUrl, bs.bookSourceUrl)
                    && stringEquals(bookSourceName, bs.bookSourceName)
                    && stringEquals(bookSourceGroup, bs.bookSourceGroup)
                    && stringEquals(ruleBookName, bs.ruleBookName)
                    && stringEquals(ruleBookAuthor, bs.ruleBookAuthor)
                    && stringEquals(ruleChapterUrl, bs.ruleChapterUrl)
                    && stringEquals(ruleCoverUrl, bs.ruleCoverUrl)
                    && stringEquals(ruleIntroduce, bs.ruleIntroduce)
                    && stringEquals(ruleChapterList, bs.ruleChapterList)
                    && stringEquals(ruleChapterName, bs.ruleChapterName)
                    && stringEquals(ruleContentUrl, bs.ruleContentUrl)
                    && stringEquals(ruleBookContent, bs.ruleBookContent)
                    && stringEquals(ruleSearchUrl, bs.ruleSearchUrl)
                    && stringEquals(ruleSearchList, bs.ruleSearchList)
                    && stringEquals(ruleSearchName, bs.ruleSearchName)
                    && stringEquals(ruleSearchAuthor, bs.ruleSearchAuthor)
                    && stringEquals(ruleSearchKind, bs.ruleSearchKind)
                    && stringEquals(ruleSearchLastChapter, bs.ruleSearchLastChapter)
                    && stringEquals(ruleSearchCoverUrl, bs.ruleSearchCoverUrl)
                    && stringEquals(ruleSearchNoteUrl, bs.ruleSearchNoteUrl)
                    && stringEquals(httpUserAgent, bs.httpUserAgent);
        }
        return false;
    }

    private Boolean stringEquals(String str1, String str2) {
        return Objects.equals(str1, str2) || (isEmpty(str1) && isEmpty(str2));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getRuleFindUrl() {
        return this.ruleFindUrl;
    }

    public void setRuleFindUrl(String ruleFindUrl) {
        this.ruleFindUrl = ruleFindUrl;
    }

    public String getBookSourceGroup() {
        return this.bookSourceGroup;
    }

    public void setBookSourceGroup(String bookSourceGroup) {
        this.bookSourceGroup = bookSourceGroup;
    }
}
