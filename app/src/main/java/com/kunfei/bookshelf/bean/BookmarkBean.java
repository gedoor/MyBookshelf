package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class BookmarkBean implements Parcelable,Cloneable{

    @Id
    private Long id = System.currentTimeMillis();
    private String noteUrl;
    private String bookName;
    private String chapterName;
    private Integer chapterIndex;
    private Integer pageIndex;
    private String content;
    

    protected BookmarkBean(Parcel in) {
        id = in.readLong();
        noteUrl = in.readString();
        bookName = in.readString();
        chapterName = in.readString();
        chapterIndex = in.readInt();
        pageIndex = in.readInt();
        content = in.readString();
    }

    @Generated(hash = 1176037419)
    public BookmarkBean(Long id, String noteUrl, String bookName, String chapterName,
            Integer chapterIndex, Integer pageIndex, String content) {
        this.id = id;
        this.noteUrl = noteUrl;
        this.bookName = bookName;
        this.chapterName = chapterName;
        this.chapterIndex = chapterIndex;
        this.pageIndex = pageIndex;
        this.content = content;
    }

    @Generated(hash = 1612540172)
    public BookmarkBean() {
    }

    public static final Creator<BookmarkBean> CREATOR = new Creator<BookmarkBean>() {
        @Override
        public BookmarkBean createFromParcel(Parcel in) {
            return new BookmarkBean(in);
        }

        @Override
        public BookmarkBean[] newArray(int size) {
            return new BookmarkBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(noteUrl);
        parcel.writeString(bookName);
        parcel.writeString(chapterName);
        parcel.writeInt(chapterIndex);
        parcel.writeInt(pageIndex);
        parcel.writeString(content);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        BookmarkBean bookmarkBean = (BookmarkBean) super.clone();
        bookmarkBean.id = id;
        bookmarkBean.noteUrl = noteUrl;
        bookmarkBean.bookName = bookName;
        bookmarkBean.chapterIndex = chapterIndex;
        bookmarkBean.chapterName = chapterName;
        bookmarkBean.pageIndex = pageIndex;
        bookmarkBean.content = content;

        return bookmarkBean;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getChapterName() {
        return this.chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public Integer getChapterIndex() {
        return this.chapterIndex;
    }

    public void setChapterIndex(Integer chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBookName() {
        return this.bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPageIndex() {
        return this.pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }
}
