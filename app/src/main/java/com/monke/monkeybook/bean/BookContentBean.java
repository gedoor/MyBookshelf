package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

import java.util.ArrayList;
import java.util.List;

/**
 * 书本缓存内容
 */
@Entity
public class BookContentBean implements Parcelable{
    @Id
    private String durChapterUrl; //对应BookInfoBean noteUrl;

    private int durChapterIndex;   //当前章节  （包括番外）

    private String durCapterContent; //当前章节内容

    private String tag;   //来源  某个网站/本地

    @Transient
    private Boolean isRight = true;

    @Transient
    private List<String> lineContent = new ArrayList<>();

    @Transient
    private float lineSize;

    public BookContentBean(){

    }

    public float getLineSize() {
        return lineSize;
    }

    public void setLineSize(float lineSize) {
        this.lineSize = lineSize;
    }

    protected BookContentBean(Parcel in) {
        durChapterUrl = in.readString();
        durChapterIndex = in.readInt();
        durCapterContent = in.readString();
        tag = in.readString();
        lineContent = in.createStringArrayList();
        isRight = in.readByte()!=0;
    }

    @Generated(hash = 1355824386)
    public BookContentBean(String durChapterUrl, int durChapterIndex,
            String durCapterContent, String tag) {
        this.durChapterUrl = durChapterUrl;
        this.durChapterIndex = durChapterIndex;
        this.durCapterContent = durCapterContent;
        this.tag = tag;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(durChapterUrl);
        dest.writeInt(durChapterIndex);
        dest.writeString(durCapterContent);
        dest.writeString(tag);
        dest.writeStringList(lineContent);
        dest.writeByte((byte) (isRight ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Transient
    public static final Creator<BookContentBean> CREATOR = new Creator<BookContentBean>() {
        @Override
        public BookContentBean createFromParcel(Parcel in) {
            return new BookContentBean(in);
        }

        @Override
        public BookContentBean[] newArray(int size) {
            return new BookContentBean[size];
        }
    };

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

    public String getDurCapterContent() {
        return durCapterContent;
    }

    public void setDurCapterContent(String durCapterContent) {
        this.durCapterContent = durCapterContent;
        if(durCapterContent==null || durCapterContent.length()==0)
            this.isRight = false;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getLineContent() {
        return lineContent;
    }

    public void setLineContent(List<String> lineContent) {
        this.lineContent = lineContent;
    }

    public Boolean getRight() {
        return isRight;
    }

    public void setRight(Boolean right) {
        isRight = right;
    }
}
