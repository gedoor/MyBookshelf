//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.utils.MD5Utils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.widget.page.PageLoaderEpub;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 书本信息
 */
@Entity
public class BookInfoBean implements Cloneable {

    private String name; //小说名
    private String tag;
    @Id
    private String noteUrl;  //如果是来源网站,则小说根地址,如果是本地则是小说本地MD5
    private String chapterUrl;  //章节目录地址,本地目录正则
    private long finalRefreshData;  //章节最后更新时间
    private String coverUrl; //小说封面
    private String author;//作者
    private String introduce; //简介
    private String origin; //来源
    private String charset;//编码
    private String bookSourceType;

    @Transient
    private List<ChapterListBean> chapterList = new ArrayList<>();    //章节列表
    @Transient
    private List<BookmarkBean> bookmarkList = new ArrayList<>();    //书签列表

    @Transient
    private static String coverPath = FileHelp.getCachePath() + "/cover/";

    public BookInfoBean() {

    }

    @Generated(hash = 906814482)
    public BookInfoBean(String name, String tag, String noteUrl, String chapterUrl, long finalRefreshData, String coverUrl, String author, String introduce,
                        String origin, String charset, String bookSourceType) {
        this.name = name;
        this.tag = tag;
        this.noteUrl = noteUrl;
        this.chapterUrl = chapterUrl;
        this.finalRefreshData = finalRefreshData;
        this.coverUrl = coverUrl;
        this.author = author;
        this.introduce = introduce;
        this.origin = origin;
        this.charset = charset;
        this.bookSourceType = bookSourceType;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        BookInfoBean bookInfoBean = (BookInfoBean) super.clone();
        bookInfoBean.name = name;
        bookInfoBean.tag = tag;
        bookInfoBean.noteUrl = noteUrl;
        bookInfoBean.chapterUrl = chapterUrl;
        bookInfoBean.coverUrl = coverUrl;
        bookInfoBean.author = author;
        bookInfoBean.introduce = introduce;
        bookInfoBean.origin = origin;
        bookInfoBean.charset = charset;
        if (chapterList != null) {
            List<ChapterListBean> newListC = new ArrayList<>();
            for (ChapterListBean chapterListBean : chapterList) {
                newListC.add((ChapterListBean) chapterListBean.clone());
            }
            bookInfoBean.setChapterList(newListC);
        }
        if (bookmarkList != null) {
            List<BookmarkBean> newListM = new ArrayList<>();
            for (BookmarkBean bookmarkBean : bookmarkList) {
                newListM.add((BookmarkBean) bookmarkBean.clone());
            }
            bookInfoBean.setBookmarkList(newListM);
        }
        return bookInfoBean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public List<ChapterListBean> getChapterList() {
        if (chapterList == null) {
            chapterList = new ArrayList<>();
        }
        return chapterList;
    }

    public void setChapterList(List<ChapterListBean> chapterList) {
        this.chapterList = chapterList;
    }

    public long getFinalRefreshData() {
        return finalRefreshData;
    }

    public void setFinalRefreshData(long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public String getCoverUrl() {
        if (isEpub() && (TextUtils.isEmpty(coverUrl) || !(new File(coverUrl)).exists())) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    extractEpubCoverImage();
                }
            });
            return "";
        }
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = BookshelfHelp.formatAuthor(author);
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getOrigin() {
        return TextUtils.isEmpty(origin) && tag.equals(BookShelfBean.LOCAL_TAG) ? StringUtils.getString(R.string.local) : origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public List<BookmarkBean> getBookmarkList() {
        if (bookmarkList == null) {
            return new ArrayList<>();
        }
        return bookmarkList;
    }

    public void setBookmarkList(List<BookmarkBean> bookmarkList) {
        this.bookmarkList = bookmarkList;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    private void extractEpubCoverImage() {
        try {
            FileHelp.createFolderIfNotExists(coverPath);
            Bitmap cover = BitmapFactory.decodeStream(Objects.requireNonNull(PageLoaderEpub.readBook(new File(noteUrl))).getCoverImage().getInputStream());
            String md5Path = coverPath + MD5Utils.strToMd5By16(noteUrl) + ".jpg";
            FileOutputStream out = new FileOutputStream(new File(md5Path));
            cover.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            setCoverUrl(md5Path);
            DbHelper.getDaoSession().getBookInfoBeanDao().insertOrReplace(this);
        } catch (Exception ignored) {
        }
    }

    private boolean isEpub() {
        return tag.equals(BookShelfBean.LOCAL_TAG) && noteUrl.toLowerCase().matches(".*\\.epub$");
    }

    public String getBookSourceType() {
        return this.bookSourceType;
    }

    public void setBookSourceType(String bookSourceType) {
        this.bookSourceType = bookSourceType;
    }

}