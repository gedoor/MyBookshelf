//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

public class DownloadChapterBean implements Parcelable, BaseChapterBean {
    private String noteUrl;

    private int durChapterIndex;  //当前章节数

    private String durChapterUrl;  //当前章节对应的文章地址

    private String durChapterName;  //当前章节名称

    private String tag;

    private String bookName;

    protected DownloadChapterBean(Parcel in) {
        noteUrl = in.readString();
        durChapterIndex = in.readInt();
        durChapterUrl = in.readString();
        durChapterName = in.readString();
        tag = in.readString();
        bookName = in.readString();
    }

    @Generated(hash = 301211198)
    public DownloadChapterBean() {
    }

    @Transient
    public static final Creator<DownloadChapterBean> CREATOR = new Creator<DownloadChapterBean>() {
        @Override
        public DownloadChapterBean createFromParcel(Parcel in) {
            return new DownloadChapterBean(in);
        }

        @Override
        public DownloadChapterBean[] newArray(int size) {
            return new DownloadChapterBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeInt(durChapterIndex);
        dest.writeString(durChapterUrl);
        dest.writeString(durChapterName);
        dest.writeString(tag);
        dest.writeString(bookName);
    }

    @Override
    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    @Override
    public int getDurChapterIndex() {
        return durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    @Override
    public String getDurChapterUrl() {
        return durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    @Override
    public String getDurChapterName() {
        return durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

}
