//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 书本缓存内容
 */
@Entity
public class BookContentBean {
    private String noteUrl; //对应BookInfoBean noteUrl;
    @Id
    private String durChapterUrl;

    private Integer durChapterIndex;   //当前章节  （包括番外）

    private String durChapterContent; //当前章节内容

    private String tag;   //来源  某个网站/本地

    private Long timeMillis;

    public BookContentBean() {

    }

    @Generated(hash = 695554675)
    public BookContentBean(String noteUrl, String durChapterUrl,
                           Integer durChapterIndex, String durChapterContent, String tag,
                           Long timeMillis) {
        this.noteUrl = noteUrl;
        this.durChapterUrl = durChapterUrl;
        this.durChapterIndex = durChapterIndex;
        this.durChapterContent = durChapterContent;
        this.tag = tag;
        this.timeMillis = timeMillis;
    }

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
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public void setDurChapterIndex(Integer durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public Long getTimeMillis() {
        return this.timeMillis;
    }

    public void setTimeMillis(Long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public boolean outTime() {
        if (timeMillis == null) {
            return true;
        }
        return timeMillis < System.currentTimeMillis();
    }

}
