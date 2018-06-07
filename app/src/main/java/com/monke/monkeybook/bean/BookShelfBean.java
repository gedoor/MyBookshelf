//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.monke.monkeybook.widget.contentswitchview.BookContentView;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.List;

/**
 * 书架item Bean
 */

@Entity
public class BookShelfBean implements Parcelable,Cloneable{
    @Transient
    public static final String LOCAL_TAG = "loc_book";
    @Transient
    private String errorMsg;
    @Transient
    private boolean isLoading;

    @Id
    private String noteUrl; //对应BookInfoBean noteUrl;
    private int durChapter;   //当前章节 （包括番外）
    private int durChapterPage = BookContentView.DurPageIndexBegin;  // 当前章节位置   用页码
    private long finalDate;  //最后阅读时间
    private boolean hasUpdate;  //是否有更新
    private int newChapters;  //更新章节数
    private String tag;
    private int serialNumber; //手动排序
    private long finalRefreshData;  //章节最后更新时间

    @Transient
    private BookInfoBean bookInfoBean = new BookInfoBean();

    public BookShelfBean(){

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

    protected BookShelfBean(Parcel in) {
        noteUrl = in.readString();
        durChapter = in.readInt();
        durChapterPage = in.readInt();
        finalDate = in.readLong();
        tag = in.readString();
        bookInfoBean = in.readParcelable(BookInfoBean.class.getClassLoader());
        serialNumber = in.readInt();
        finalRefreshData = in.readLong();
    }

    @Generated(hash = 826920685)
    public BookShelfBean(String noteUrl, int durChapter, int durChapterPage, long finalDate,
            boolean hasUpdate, int newChapters, String tag, int serialNumber,
            long finalRefreshData) {
        this.noteUrl = noteUrl;
        this.durChapter = durChapter;
        this.durChapterPage = durChapterPage;
        this.finalDate = finalDate;
        this.hasUpdate = hasUpdate;
        this.newChapters = newChapters;
        this.tag = tag;
        this.serialNumber = serialNumber;
        this.finalRefreshData = finalRefreshData;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeInt(durChapter);
        dest.writeInt(durChapterPage);
        dest.writeLong(finalDate);
        dest.writeString(tag);
        dest.writeParcelable(bookInfoBean, flags);
        dest.writeInt(serialNumber);
        dest.writeLong(finalRefreshData);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BookShelfBean bookShelfBean = (BookShelfBean) super.clone();
        bookShelfBean.noteUrl = noteUrl;
        bookShelfBean.tag = tag;
        bookShelfBean.bookInfoBean = (BookInfoBean) bookInfoBean.clone();
        return bookShelfBean;
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public int getDurChapter() {
        return durChapter;
    }

    public void setDurChapter(int durChapter) {
        this.durChapter = durChapter;
    }

    //获取当前章节
    public ChapterListBean getDurChapterListBean() {
        return getChapterList(durChapter);
    }

    //获取最新章节
    public ChapterListBean getLastChapterListBean() {
        return getChapterList(bookInfoBean.getChapterList().size() - 1);
    }

    public ChapterListBean getChapterList(int index) {

        if (getChapterList() == null || getChapterListSize() == 0 || index < 0) {
            ChapterListBean chapterListBean = new ChapterListBean();
            chapterListBean.setDurChapterName("暂无");
            chapterListBean.setDurChapterUrl("暂无");
            return chapterListBean;
        } else if (index < getChapterListSize()) {
            return getChapterList().get(index);
        } else {
            durChapter = getChapterListSize() - 1;
            return getChapterList().get(getChapterListSize() - 1);
        }
    }

    public List<ChapterListBean> getChapterList() {
        return getBookInfoBean().getChapterList();
    }

    public int getChapterListSize() {
        return getChapterList().size();
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

    public boolean getHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public int getNewChapters() {
        return newChapters;
    }

    public void setNewChapters(int newChapters) {
        this.newChapters = newChapters;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public long getFinalRefreshData() {
        return this.finalRefreshData;
    }

    public void setFinalRefreshData(long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }
}