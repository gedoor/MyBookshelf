//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 书本缓存内容
 */
public class BookContentBean implements Parcelable{
    private String noteUrl; //对应BookInfoBean noteUrl;

    private String durChapterUrl;

    private int durChapterIndex;   //当前章节  （包括番外）

    private String durChapterContent; //当前章节内容

    private String tag;   //来源  某个网站/本地

    private Boolean isRight = true;

    public BookContentBean(){

    }

    protected BookContentBean(Parcel in) {
        durChapterUrl = in.readString();
        durChapterIndex = in.readInt();
        durChapterContent = in.readString();
        tag = in.readString();
        isRight = in.readByte()!=0;
        noteUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(durChapterUrl);
        dest.writeInt(durChapterIndex);
        dest.writeString(durChapterContent);
        dest.writeString(tag);
        dest.writeByte((byte) (isRight ? 1 : 0));
        dest.writeString(noteUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BookContentBean> CREATOR = new Creator<BookContentBean>() {
        @Override
        public BookContentBean createFromParcel(Parcel in) {
            return new BookContentBean(in);
        }

        @Override
        public BookContentBean[] newArray(int size) {
            return new BookContentBean[size];
        }
    };

    public String getDurChapterUrl() {
        return durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    public int getDurChapterIndex() {
        return durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public String getDurChapterContent() {
        return durChapterContent;
    }

    public void setDurChapterContent(String durChapterContent) {
        this.durChapterContent = durChapterContent;
        if(durChapterContent ==null || durChapterContent.length()==0)
            this.isRight = false;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getRight() {
        return isRight;
    }

    public void setRight(Boolean right) {
        isRight = right;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }
}
