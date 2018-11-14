//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.monke.monkeybook.R;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.utils.MD5Utils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.monke.monkeybook.bean.BookShelfBean.LOCAL_TAG;
import static com.monke.monkeybook.utils.StringUtils.getString;
import static com.monke.monkeybook.widget.page.PageLoaderEpub.readBook;

/**
 * 书本信息
 */
@Entity
public class BookInfoBean implements Parcelable,Cloneable{

    private String name; //小说名
    private String tag;
    @Id
    private String noteUrl;  //如果是来源网站   则小说根地址 /如果是本地  则是小说本地MD5
    private String chapterUrl;  //章节目录地址
    private long finalRefreshData;  //章节最后更新时间
    private String coverUrl; //小说封面
    private String author;//作者
    private String introduce; //简介
    private String origin; //来源
    private String charset;//编码

    @Transient
    private List<ChapterListBean> chapterList = new ArrayList<>();    //章节列表
    @Transient
    private List<BookmarkBean> bookmarkList = new ArrayList<>();    //书签列表

    @Transient
    private static String coverPath = FileHelp.getCachePath() + "/cover/";

    public BookInfoBean(){

    }

    @Transient
    public static final Creator<BookInfoBean> CREATOR = new Creator<BookInfoBean>() {
        @Override
        public BookInfoBean createFromParcel(Parcel in) {
            return new BookInfoBean(in);
        }

        @Override
        public BookInfoBean[] newArray(int size) {
            return new BookInfoBean[size];
        }
    };

    protected BookInfoBean(Parcel in) {
        name = in.readString();
        tag = in.readString();
        noteUrl = in.readString();
        chapterUrl = in.readString();
        finalRefreshData = in.readLong();
        coverUrl = in.readString();
        author = in.readString();
        introduce = in.readString();
        origin = in.readString();
        charset = in.readString();
        chapterList = in.createTypedArrayList(ChapterListBean.CREATOR);
        bookmarkList = in.createTypedArrayList(BookmarkBean.CREATOR);
    }

    @Generated(hash = 1022173528)
    public BookInfoBean(String name, String tag, String noteUrl, String chapterUrl, long finalRefreshData, String coverUrl,
            String author, String introduce, String origin, String charset) {
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
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(tag);
        dest.writeString(noteUrl);
        dest.writeString(chapterUrl);
        dest.writeLong(finalRefreshData);
        dest.writeString(coverUrl);
        dest.writeString(author);
        dest.writeString(introduce);
        dest.writeString(origin);
        dest.writeString(charset);
        dest.writeTypedList(chapterList);
        dest.writeTypedList(bookmarkList);
    }

    @Override
    public int describeContents() {
        return 0;
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
        if(chapterList !=null){
            List<ChapterListBean> newListC = new ArrayList<>();
            Iterator<ChapterListBean> iteratorC = chapterList.iterator();
            while(iteratorC.hasNext()){
                newListC.add((ChapterListBean) iteratorC.next().clone());
            }
            bookInfoBean.setChapterList(newListC);
        }
        if (bookmarkList != null) {
            List<BookmarkBean> newListM = new ArrayList<>();
            Iterator<BookmarkBean> iteratorM = bookmarkList.iterator();
            while(iteratorM.hasNext()){
                newListM.add((BookmarkBean) iteratorM.next().clone());
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
            return new ArrayList<>();
        } else {
            return chapterList;
        }
    }

    public void setChapterList(List<ChapterListBean> chapterlist) {
        this.chapterList = chapterlist;
    }

    public long getFinalRefreshData() {
        return finalRefreshData;
    }

    public void setFinalRefreshData(long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public String getCoverUrl() {
        if (isEpub() && TextUtils.isEmpty(coverUrl) && !(new File(coverUrl)).exists()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    BookInfoBean.this.extractEpubCoverImage();
                }
            });
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
        this.author = author;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getOrigin() {
        return TextUtils.isEmpty(origin) && tag.equals(LOCAL_TAG) ? getString(R.string.local) : origin;
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
            Bitmap cover = BitmapFactory.decodeStream(readBook(new File(noteUrl)).getCoverImage().getInputStream());
            String md5Path = coverPath + MD5Utils.strToMd5By32(noteUrl) + ".jpg";
            FileOutputStream out = new FileOutputStream(new File(md5Path));
            cover.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            coverUrl = md5Path;
            DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isEpub() {
        return tag.equals(LOCAL_TAG) && noteUrl.toLowerCase().matches(".*\\.epub$");
    }

}