//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;

/**
 * 书架item Bean
 */

@Entity
public class BookShelfBean implements Parcelable, Cloneable, BaseBookBean {
    @Transient
    public static final String LOCAL_TAG = "loc_book";
    @Transient
    private String errorMsg;
    @Transient
    private boolean isLoading;

    @Id
    private String noteUrl; //对应BookInfoBean noteUrl;
    private Integer durChapter = 0;   //当前章节 （包括番外）
    private Integer durChapterPage = 0;  // 当前章节位置   用页码
    private Long finalDate = System.currentTimeMillis();  //最后阅读时间
    private Boolean hasUpdate = false;  //是否有更新
    private Integer newChapters = 0;  //更新章节数
    private String tag;
    private Integer serialNumber = 0; //手动排序
    private Long finalRefreshData = System.currentTimeMillis();  //章节最后更新时间
    private Integer group = 0;
    private String durChapterName;
    private String lastChapterName;
    private Integer chapterListSize = 0;
    private String customCoverPath;
    private Boolean allowUpdate = true;
    private Boolean useReplaceRule = true;
    private String variable;

    @Transient
    private Map<String, String> variableMap;

    @Transient
    private BookInfoBean bookInfoBean = new BookInfoBean();

    public BookShelfBean() {

    }

    @Transient
    public static final Creator<BookShelfBean> CREATOR = new Creator<BookShelfBean>() {
        @Override
        public BookShelfBean createFromParcel(Parcel in) {
            return new BookShelfBean(in);
        }

        @Override
        public BookShelfBean[] newArray(int size) {
            return new BookShelfBean[size];
        }
    };

    protected BookShelfBean(Parcel in) {
        noteUrl = in.readString();
        durChapter = in.readInt();
        durChapterPage = in.readInt();
        finalDate = in.readLong();
        tag = in.readString();
        bookInfoBean = in.readParcelable(BookInfoBean.class.getClassLoader());
        serialNumber = in.readInt();
        finalRefreshData = in.readLong();
        group = in.readInt();
        durChapterName = in.readString();
        lastChapterName = in.readString();
        chapterListSize = in.readInt();
        customCoverPath = in.readString();
        allowUpdate = in.readByte() != 0 && !tag.equals(LOCAL_TAG);
        useReplaceRule = in.readByte() != 0;
        variable = in.readString();
    }

    @Generated(hash = 63205856)
    public BookShelfBean(String noteUrl, Integer durChapter, Integer durChapterPage, Long finalDate, Boolean hasUpdate,
                         Integer newChapters, String tag, Integer serialNumber, Long finalRefreshData, Integer group,
                         String durChapterName, String lastChapterName, Integer chapterListSize, String customCoverPath,
                         Boolean allowUpdate, Boolean useReplaceRule, String variable) {
        this.noteUrl = noteUrl;
        this.durChapter = durChapter;
        this.durChapterPage = durChapterPage;
        this.finalDate = finalDate;
        this.hasUpdate = hasUpdate;
        this.newChapters = newChapters;
        this.tag = tag;
        this.serialNumber = serialNumber;
        this.finalRefreshData = finalRefreshData;
        this.group = group;
        this.durChapterName = durChapterName;
        this.lastChapterName = lastChapterName;
        this.chapterListSize = chapterListSize;
        this.customCoverPath = customCoverPath;
        this.allowUpdate = allowUpdate;
        this.useReplaceRule = useReplaceRule;
        this.variable = variable;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeInt(group);
        dest.writeInt(durChapter);
        dest.writeInt(durChapterPage);
        dest.writeLong(finalDate);
        dest.writeString(tag);
        dest.writeParcelable(bookInfoBean, flags);
        dest.writeInt(serialNumber);
        dest.writeLong(finalRefreshData);
        dest.writeString(durChapterName);
        dest.writeString(lastChapterName);
        dest.writeInt(chapterListSize);
        dest.writeString(customCoverPath);
        dest.writeByte((byte) (allowUpdate ? 1 : 0));
        dest.writeByte((byte) (useReplaceRule ? 1 : 0));
        dest.writeString(variable);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BookShelfBean bookShelfBean = (BookShelfBean) super.clone();
        bookShelfBean.noteUrl = noteUrl;
        bookShelfBean.tag = tag;
        bookShelfBean.bookInfoBean = (BookInfoBean) bookInfoBean.clone();
        return bookShelfBean;
    }

    @Override
    public String getVariable() {
        return this.variable;
    }

    @Override
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public void putVariable(String key, String value) {
        if (variableMap == null) {
            variableMap = new HashMap<>();
        }
        variableMap.put(key, value);
        variable = new Gson().toJson(variableMap);
    }

    @Override
    public Map<String, String> getVariableMap() {
        if (variableMap == null && !TextUtils.isEmpty(variable)) {
            variableMap = new Gson().fromJson(variable, MAP_STRING);
        }
        return variableMap;
    }

    public ChapterListBean getChapter(int index) {
        if (realChapterListEmpty()) {
            ChapterListBean chapterListBean = new ChapterListBean();
            chapterListBean.setDurChapterName("暂无");
            chapterListBean.setDurChapterUrl("暂无");
            return chapterListBean;
        } else if (0 <= index && index < getChapterList().size()) {
            return getChapterList().get(index);
        } else {
            durChapter = getChapterList().size() - 1;
            return getChapterList().get(durChapter);
        }
    }

    public BookmarkBean getBookmark(int index) {
        if (realBookmarkListEmpty() || index < 0) {
            return null;
        } else if (index < getBookmarkList().size()) {
            return getBookmarkList().get(index);
        } else {
            return getBookmarkList().get(getChapterList().size() - 1);
        }
    }

    @Override
    public String getNoteUrl() {
        return noteUrl;
    }

    @Override
    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public int getDurChapter() {
        return durChapter < 0 ? 0 : durChapter;
    }

    public List<ChapterListBean> getChapterList() {
        return getBookInfoBean().getChapterList();
    }

    public int getDurChapterPage() {
        return durChapterPage < 0 ? 0 : durChapterPage;
    }

    public long getFinalDate() {
        return finalDate;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public BookInfoBean getBookInfoBean() {
        return bookInfoBean;
    }

    public void setBookInfoBean(BookInfoBean bookInfoBean) {
        this.bookInfoBean = bookInfoBean;
    }

    public boolean getHasUpdate() {
        return hasUpdate;
    }

    public int getNewChapters() {
        return newChapters;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public long getFinalRefreshData() {
        return this.finalRefreshData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public int getGroup() {
        return this.group == null ? 0 : this.group;
    }

    public void setDurChapter(Integer durChapter) {
        this.durChapter = durChapter;
    }

    public void setDurChapterPage(Integer durChapterPage) {
        this.durChapterPage = durChapterPage;
    }

    public void setFinalDate(Long finalDate) {
        this.finalDate = finalDate;
    }

    public void setHasUpdate(Boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public void setNewChapters(Integer newChapters) {
        this.newChapters = newChapters;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setFinalRefreshData(Long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public String getDurChapterName() {
        return this.durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    public void upDurChapterName() {
        if (getChapterList().size() > durChapter) {
            durChapterName = getChapterList().get(durChapter).getDurChapterName();
        }
    }

    public String getLastChapterName() {
        return this.lastChapterName;
    }

    public void setLastChapterName(String lastChapterName) {
        this.lastChapterName = lastChapterName;
    }

    public void upLastChapterName() {
        if (getChapterList().size() > 0) {
            lastChapterName = getChapterList().get(getChapterListSize() - 1).getDurChapterName();
        }
    }

    public int getUnreadChapterNum() {
        int num = getChapterListSize() - getDurChapter() - 1;
        return num < 0 ? 0 : num;
    }

    public int getChapterListSize() {
        if (getChapterList().size() == 0) {
            return this.chapterListSize == null ? 0 : this.chapterListSize;
        }
        return getChapterList().size();
    }

    public void setChapterListSize(Integer chapterListSize) {
        this.chapterListSize = chapterListSize;
    }

    public String getCustomCoverPath() {
        return this.customCoverPath;
    }

    public void setCustomCoverPath(String customCoverPath) {
        this.customCoverPath = customCoverPath;
    }

    public Boolean getAllowUpdate() {
        return allowUpdate == null ? true : allowUpdate;
    }

    public void setAllowUpdate(Boolean allowUpdate) {
        this.allowUpdate = allowUpdate;
    }

    public boolean realChapterListEmpty() {
        return getChapterList().isEmpty();
    }

    public void setChapterList(List<ChapterListBean> chapterList) {
        this.bookInfoBean.setChapterList(chapterList);
    }

    public boolean realBookmarkListEmpty() {
        return getBookmarkList().isEmpty();
    }

    public List<BookmarkBean> getBookmarkList() {
        return this.bookInfoBean.getBookmarkList();
    }

    public int getBookmarkListSize() {
        return getBookmarkList().size();
    }

    public Boolean getUseReplaceRule() {
        return this.useReplaceRule;
    }

    public void setUseReplaceRule(Boolean useReplaceRule) {
        this.useReplaceRule = useReplaceRule;
    }

}