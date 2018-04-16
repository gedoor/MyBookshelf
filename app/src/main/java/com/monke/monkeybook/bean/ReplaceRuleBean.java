package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by GKF on 2018/2/7.
 * 阅读内容替换规则
 */
@Entity
public class ReplaceRuleBean implements Parcelable {
    @Id(autoincrement = true)
    private Long id;
    //描述
    private String replaceSummary;
    //替换规则
    private String regex;
    //替换为
    private String replacement;

    private Boolean enable;
    @OrderBy
    private int serialNumber;

    private ReplaceRuleBean(Parcel in) {
        id  = in.readLong();
        regex = in.readString();
        replacement = in.readString();
        replaceSummary = in.readString();
        enable = in.readByte() != 0;
        serialNumber = in.readInt();
    }

    @Generated(hash = 298152313)
    public ReplaceRuleBean(Long id, String replaceSummary, String regex, String replacement,
            Boolean enable, int serialNumber) {
        this.id = id;
        this.replaceSummary = replaceSummary;
        this.regex = regex;
        this.replacement = replacement;
        this.enable = enable;
        this.serialNumber = serialNumber;
    }

    @Generated(hash = 582692869)
    public ReplaceRuleBean() {
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(regex);
        parcel.writeString(replacement);
        parcel.writeString(replaceSummary);
        parcel.writeByte((byte) (enable ? 1 : 0));
        parcel.writeInt(serialNumber);
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

    public Boolean getEnable() {
        if (enable == null) {
            return false;
        }
        return this.enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
