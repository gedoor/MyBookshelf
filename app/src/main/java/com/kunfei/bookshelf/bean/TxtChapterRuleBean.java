package com.kunfei.bookshelf.bean;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;

import java.util.Objects;

@Entity
public class TxtChapterRuleBean {

    @Id
    private String name;
    private String rule;
    @OrderBy
    private Integer serialNumber;
    private Boolean enable;

    @Generated(hash = 2018686288)
    public TxtChapterRuleBean(String name, String rule, Integer serialNumber,
                              Boolean enable) {
        this.name = name;
        this.rule = rule;
        this.serialNumber = serialNumber;
        this.enable = enable;
    }

    @Generated(hash = 382733400)
    public TxtChapterRuleBean() {
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TxtChapterRuleBean) {
            return Objects.equals(this.name, ((TxtChapterRuleBean) obj).name);
        }
        return false;
    }

    public TxtChapterRuleBean copy() {
        TxtChapterRuleBean ruleBean = new TxtChapterRuleBean();
        ruleBean.setName(name);
        ruleBean.setRule(rule);
        ruleBean.setEnable(enable);
        ruleBean.setSerialNumber(serialNumber);
        return ruleBean;
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

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Boolean getEnable() {
        return enable == null ? true : enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
