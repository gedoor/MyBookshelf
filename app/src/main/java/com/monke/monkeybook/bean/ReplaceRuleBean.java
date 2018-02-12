package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by GKF on 2018/2/7.
 * 阅读内容替换规则
 */
@Entity
public class ReplaceRuleBean implements Parcelable {
    private String replaceSummary;
    @Id
    private String regex;
    private String replacement;


    private ReplaceRuleBean(Parcel in) {
        regex = in.readString();
        replacement = in.readString();
        replaceSummary = in.readString();
    }

    @Generated(hash = 533850999)
    public ReplaceRuleBean(String replaceSummary, String regex, String replacement) {
        this.replaceSummary = replaceSummary;
        this.regex = regex;
        this.replacement = replacement;
    }

    @Generated(hash = 582692869)
    public ReplaceRuleBean() {
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(regex);
        parcel.writeString(replacement);
        parcel.writeString(replaceSummary);
    }

    @Transient
    public static final Creator<ReplaceRuleBean> CREATOR = new Creator<ReplaceRuleBean>() {
        @Override
        public ReplaceRuleBean createFromParcel(Parcel in) {
            return new ReplaceRuleBean(in);
        }

        @Override
        public ReplaceRuleBean[] newArray(int size) {
            return new ReplaceRuleBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getReplaceSummary() {
        return this.replaceSummary;
    }

    public void setReplaceSummary(String replaceSummary) {
        this.replaceSummary = replaceSummary;
    }

    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getReplacement() {
        return this.replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

}
