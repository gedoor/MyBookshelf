//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.constant.BookType;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;

/**
 * 书架item Bean
 */

@Entity
public class BookShelfBean implements Cloneable, BaseBookBean {
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
    private Boolean replaceEnable = MApplication.getConfigPreferences().getBoolean("replaceEnableDefault", true);

    @Transient
    private Map<String, String> variableMap;

    @Transient
    private BookInfoBean bookInfoBean;

    public BookShelfBean() {

    }

    @Generated(hash = 451550884)
    public BookShelfBean(String noteUrl, Integer durChapter, Integer durChapterPage, Long finalDate, Boolean hasUpdate,
                         Integer newChapters, String tag, Integer serialNumber, Long finalRefreshData, Integer group,
                         String durChapterName, String lastChapterName, Integer chapterListSize, String customCoverPath,
                         Boolean allowUpdate, Boolean useReplaceRule, String variable, Boolean replaceEnable) {
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
        this.replaceEnable = replaceEnable;
    }

    @Override
    public Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookShelfBean.class);
        } catch (Exception ignored) {
        }
        return this;
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

    public int getDurChapter(int chapterListSize) {
        if (durChapter < 0 | chapterListSize == 0) {
            return 0;
        } else if (durChapter >= chapterListSize) {
            return chapterListSize - 1;
        }
        return durChapter;
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
        if (bookInfoBean == null) {
            bookInfoBean = new BookInfoBean();
            bookInfoBean.setNoteUrl(noteUrl);
            bookInfoBean.setTag(tag);
        }
        return bookInfoBean;
    }

    public void setBookInfoBean(BookInfoBean bookInfoBean) {
        this.bookInfoBean = bookInfoBean;
    }

    public boolean getHasUpdate() {
        return hasUpdate && !isAudio();
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

    public String getLastChapterName() {
        return this.lastChapterName;
    }

    public void setLastChapterName(String lastChapterName) {
        this.lastChapterName = lastChapterName;
    }

    public int getUnreadChapterNum() {
        int num = getChapterListSize() - getDurChapter() - 1;
        return num < 0 ? 0 : num;
    }

    public int getChapterListSize() {
        return this.chapterListSize == null ? 0 : this.chapterListSize;
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

    public Boolean getUseReplaceRule() {
        return this.useReplaceRule;
    }

    public void setUseReplaceRule(Boolean useReplaceRule) {
        this.useReplaceRule = useReplaceRule;
    }

    public boolean isAudio() {
        return Objects.equals(bookInfoBean.getBookSourceType(), BookType.AUDIO);
    }

    public Boolean getReplaceEnable() {
        return replaceEnable == null ? MApplication.getConfigPreferences().getBoolean("replaceEnableDefault", true) : replaceEnable;
    }

    public void setReplaceEnable(Boolean replaceEnable) {
        this.replaceEnable = replaceEnable;
    }
}