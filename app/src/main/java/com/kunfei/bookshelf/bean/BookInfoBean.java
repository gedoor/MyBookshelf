//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.BookType;
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
    private String bookInfoHtml;
    @Transient
    private String chapterListHtml;

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
    protected Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookInfoBean.class);
        } catch (Exception ignored) {
        }
        return this;
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

    public long getFinalRefreshData() {
        return finalRefreshData;
    }

    public void setFinalRefreshData(long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public String getCoverUrl() {
        if (isEpub() && (TextUtils.isEmpty(coverUrl) || !(new File(coverUrl)).exists())) {
            extractEpubCoverImage();
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

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    private void extractEpubCoverImage() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileHelp.createFolderIfNotExists(coverUrl);
                    Bitmap cover = BitmapFactory.decodeStream(Objects.requireNonNull(PageLoaderEpub.readBook(new File(noteUrl))).getCoverImage().getInputStream());
                    String md5Path = FileHelp.getCachePath() + File.separator + "cover" + File.separator + MD5Utils.strToMd5By16(noteUrl) + ".jpg";
                    FileOutputStream out = new FileOutputStream(new File(md5Path));
                    cover.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                    setCoverUrl(md5Path);
                    DbHelper.getDaoSession().getBookInfoBeanDao().insertOrReplace(BookInfoBean.this);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private boolean isEpub() {
        return Objects.equals(tag, BookShelfBean.LOCAL_TAG) && noteUrl.toLowerCase().matches(".*\\.epub$");
    }

    public String getBookSourceType() {
        return this.bookSourceType;
    }

    public void setBookSourceType(String bookSourceType) {
        this.bookSourceType = bookSourceType;
    }

    public boolean isAudio() {
        return Objects.equals(BookType.AUDIO, bookSourceType);
    }

    public String getBookInfoHtml() {
        return bookInfoHtml;
    }

    public void setBookInfoHtml(String bookInfoHtml) {
        this.bookInfoHtml = bookInfoHtml;
    }

    public String getChapterListHtml() {
        return chapterListHtml;
    }

    public void setChapterListHtml(String chapterListHtml) {
        this.chapterListHtml = chapterListHtml;
    }
}