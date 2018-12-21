//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.utils.StringUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Objects;
import java.util.regex.Matcher;

/**
 * 章节列表
 */
@Entity
public class ChapterListBean implements Parcelable, Cloneable, BaseChapterBean {

    private String noteUrl; //对应BookInfoBean noteUrl;

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

    @Override
    public int hashCode() {
        return durChapterUrl.hashCode();
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String getDurChapterName() {
        return this.durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        if (durChapterName != null) {
            durChapterName = durChapterName.trim();
            Matcher matcher = BookshelfHelp.chapterNamePattern.matcher(durChapterName);
            if (matcher.find()) {
                int num = StringUtils.stringToInt(matcher.group(2));
                this.durChapterName = num > 0 ? matcher.replaceFirst("第" + num + "章 ") : matcher.replaceFirst("$1 ");
                return;
            }
        }
        this.durChapterName = durChapterName;
    }

    public String getPureChapterName() {
        return durChapterName == null ? ""
                : StringUtils.fullToHalf(durChapterName).replaceAll("\\s", "")
                .replaceAll("^第.*?章|[(\\[][^()\\[\\]]{2,}[)\\]]$", "")
                .replaceAll("[^\\w\\u4E00-\\u9FEF〇\\u3400-\\u4DBF\\u20000-\\u2A6DF\\u2A700-\\u2EBEF]", "");
        // 所有非字母数字中日韩文字 CJK区+扩展A-F区
    }

    public int getChapterNum() {
        if (durChapterName != null) {
            Matcher matcher = BookshelfHelp.chapterNamePattern.matcher(durChapterName);
            if (matcher.find()) {
                return StringUtils.stringToInt(matcher.group(2));
            }
        }
        return -1;
    }

    @Override
    public String getDurChapterUrl() {
        return this.durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    @Override
    public int getDurChapterIndex() {
        return this.durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    @Override
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

    public Boolean getHasCache(BookInfoBean bookInfoBean) {
        return BookshelfHelp.isChapterCached(bookInfoBean, this);
    }
}
