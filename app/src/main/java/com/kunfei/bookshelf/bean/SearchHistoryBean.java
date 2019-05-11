//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class SearchHistoryBean {
    @Id(autoincrement = true)
    private Long id = null;
    private int type;
    private String content;
    private long date;

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SearchHistoryBean(int type, String content, long date) {
        this.type = type;
        this.content = content;
        this.date = date;
    }

    @Generated(hash = 488115752)
    public SearchHistoryBean(Long id, int type, String content, long date) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.date = date;
    }

    @Generated(hash = 1570282321)
    public SearchHistoryBean() {

    }

}
