package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;

@Entity
public class TextChapterRuleBean {

    @Id
    private String name;
    private String rule;
    @OrderBy
    private int serialNumber;

    @Generated(hash = 2028231213)
    public TextChapterRuleBean(String name, String rule, int serialNumber) {
        this.name = name;
        this.rule = rule;
        this.serialNumber = serialNumber;
    }

    @Generated(hash = 851861211)
    public TextChapterRuleBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
}
