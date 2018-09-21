//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Objects;
import org.greenrobot.greendao.DaoException;
import com.monke.monkeybook.dao.DaoSession;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;

/**
 * 章节列表
 */
@Entity
public class ChapterListBean implements Parcelable,Cloneable{

    private String noteUrl; //对应BookInfoBean noteUrl;
    @ToOne(joinProperty = "noteUrl")
    private BookInfoBean bookInfo;

    private int durChapterIndex;  //当前章节数
    @Id
    private String durChapterUrl;  //当前章节对应的文章地址
    private String durChapterName;  //当前章节名称
    private String tag;
    //章节内容在文章中的起始位置(本地)
    private Long start;
    //章节内容在文章中的终止位置(本地)
    private Long end;

    protected ChapterListBean(Parcel in) {
        noteUrl = in.readString();
        durChapterIndex = in.readInt();
        durChapterUrl = in.readString();
        durChapterName = in.readString();
        tag = in.readString();
        start = in.readLong();
        end = in.readLong();
    }

    @Generated(hash = 1504053071)
    public ChapterListBean(String noteUrl, int durChapterIndex, String durChapterUrl, String durChapterName, String tag,
            Long start, Long end) {
        this.noteUrl = noteUrl;
        this.durChapterIndex = durChapterIndex;
        this.durChapterUrl = durChapterUrl;
        this.durChapterName = durChapterName;
        this.tag = tag;
        this.start = start;
        this.end = end;
    }

    @Generated(hash = 1096893365)
    public ChapterListBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeInt(durChapterIndex);
        dest.writeString(durChapterUrl);
        dest.writeString(durChapterName);
        dest.writeString(tag);
        dest.writeLong(start);
        dest.writeLong(end);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    
    @Transient
    public static final Creator<ChapterListBean> CREATOR = new Creator<ChapterListBean>() {
        @Override
        public ChapterListBean createFromParcel(Parcel in) {
            return new ChapterListBean(in);
        }

        @Override
        public ChapterListBean[] newArray(int size) {
            return new ChapterListBean[size];
        }
    };
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 74829519)
    private transient ChapterListBeanDao myDao;
    @Generated(hash = 1509892206)
    private transient String bookInfo__resolvedKey;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ChapterListBean chapterListBean = (ChapterListBean) super.clone();
        chapterListBean.noteUrl = noteUrl;
        chapterListBean.durChapterUrl = durChapterUrl;
        chapterListBean.durChapterName = durChapterName;
        chapterListBean.tag = tag;
        return chapterListBean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChapterListBean) {
            ChapterListBean chapterListBean = (ChapterListBean) obj;
            return Objects.equals(chapterListBean.durChapterUrl, durChapterUrl);
        } else {
            return false;
        }
    }

    public Boolean getHasCache() {
        return BookshelfHelp.isChapterCached(this);
    }

    public void setHasCache(Boolean hasCache) {
        BookshelfHelp.setChapterIsCached(this, hasCache);
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDurChapterName() {
        return this.durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        if (durChapterName != null) {
            this.durChapterName = durChapterName.replaceAll("^(第[\\d零〇一二两三四五六七八九十百千万\\s]+[章节篇回集])[、，。　：:.\\s]*", "$1 ");
        } else {
            this.durChapterName = null;
        }
    }

    public String getDurChapterUrl() {
        return this.durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    public int getDurChapterIndex() {
        return this.durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public Long getStart() {
        return this.start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return this.end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1678998200)
    public BookInfoBean getBookInfo() {
        String __key = this.noteUrl;
        if (bookInfo__resolvedKey == null || bookInfo__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BookInfoBeanDao targetDao = daoSession.getBookInfoBeanDao();
            BookInfoBean bookInfoNew = targetDao.load(__key);
            synchronized (this) {
                bookInfo = bookInfoNew;
                bookInfo__resolvedKey = __key;
            }
        }
        return bookInfo;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 955140842)
    public void setBookInfo(BookInfoBean bookInfo) {
        synchronized (this) {
            this.bookInfo = bookInfo;
            noteUrl = bookInfo == null ? null : bookInfo.getNoteUrl();
            bookInfo__resolvedKey = noteUrl;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 180114398)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getChapterListBeanDao() : null;
    }

}
