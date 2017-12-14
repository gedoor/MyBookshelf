//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

/**
 * 书架item Bean
 */

@Entity
public class BookShelfBean implements Parcelable,Cloneable{
    @Transient
    public static final long REFRESH_TIME = 5*60*1000;   //更新时间间隔 至少
    @Transient
    public static final String LOCAL_TAG = "loc_book";

    @Id
    private String noteUrl; //对应BookInfoBean noteUrl;

    private int durChapter;   //当前章节 （包括番外）

    private int durChapterPage = BookContentView.DURPAGEINDEXBEGIN;  // 当前章节位置   用页码

    private long finalDate;  //最后阅读时间

    private boolean hasUpdate;  //是否有更新

    private int newChapters;  //更新章节数

    private String tag;

    @Transient
    private BookInfoBean bookInfoBean = new BookInfoBean();

    public BookShelfBean(){

    }

    protected BookShelfBean(Parcel in) {
        noteUrl = in.readString();
        durChapter = in.readInt();
        durChapterPage = in.readInt();
        finalDate = in.readLong();
        tag = in.readString();
        bookInfoBean = in.readParcelable(BookInfoBean.class.getClassLoader());
    }

    @Generated(hash = 189691701)
    public BookShelfBean(String noteUrl, int durChapter, int durChapterPage, long finalDate,
            boolean hasUpdate, int newChapters, String tag) {
        this.noteUrl = noteUrl;
        this.durChapter = durChapter;
        this.durChapterPage = durChapterPage;
        this.finalDate = finalDate;
        this.hasUpdate = hasUpdate;
        this.newChapters = newChapters;
        this.tag = tag;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeInt(durChapter);
        dest.writeInt(durChapterPage);
        dest.writeLong(finalDate);
        dest.writeString(tag);
        dest.writeParcelable(bookInfoBean, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Transient
    public static final Creator<BookShelfBean> CREATOR = new Creator<BookShelfBean>() {
        @Override
        public BookShelfBean createFromParcel(Parcel in) {
            return new BookShelfBean(in);
        }

        @Override
        public BookShelfBean[] newArray(int size) {
            return new BookShelfBean[size];
        }
    };

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public int getDurChapter() {
        return durChapter;
    }

    //获取当前章节
    public ChapterListBean getDurChapterListBean() {
        return bookInfoBean.getChapterlist().get(durChapter);
    }
    //获取最新章节
    public ChapterListBean getLastChapterListBean() {
        return bookInfoBean.getChapterlist().get(bookInfoBean.getChapterlist().size()-1);
    }

    public void setDurChapter(int durChapter) {
        this.durChapter = durChapter;
    }

    public int getDurChapterPage() {
        return durChapterPage;
    }

    public void setDurChapterPage(int durChapterPage) {
        this.durChapterPage = durChapterPage;
    }

    public long getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(long finalDate) {
        this.finalDate = finalDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public BookInfoBean getBookInfoBean() {
        return bookInfoBean;
    }

    public void setBookInfoBean(BookInfoBean bookInfoBean) {
        this.bookInfoBean = bookInfoBean;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public boolean getHasUpdate() {
        return hasUpdate;
    }

    public int getNewChapters() {
        return newChapters;
    }

    public void setNewChapters(int newChapters) {
        this.newChapters = newChapters;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BookShelfBean bookShelfBean = (BookShelfBean) super.clone();
        bookShelfBean.noteUrl = noteUrl;
        bookShelfBean.tag = tag;
        bookShelfBean.bookInfoBean = (BookInfoBean) bookInfoBean.clone();
        return bookShelfBean;
    }
}