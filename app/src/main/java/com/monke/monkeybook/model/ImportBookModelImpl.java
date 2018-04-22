//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.impl.IImportBookModel;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;

public class ImportBookModelImpl extends BaseModelImpl implements IImportBookModel {

    public static ImportBookModelImpl getInstance() {
        return new ImportBookModelImpl();
    }

    @Override
    public Observable<LocBookShelfBean> importBook(final File book) {
        return Observable.create(e -> {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(book);
            byte[] buffer = new byte[2048];
            int len;
            while ((len = in.read(buffer, 0, 2048)) != -1) {
                md.update(buffer, 0, len);
            }
            in.close();

            String md5 = new BigInteger(1, md.digest()).toString(16);
            BookShelfBean bookShelfBean;
            bookShelfBean = BookshelfHelp.getBook(md5);
            Boolean isNew = true;
            if (bookShelfBean != null) {
                isNew = false;
                bookShelfBean.setBookInfoBean(DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder().where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).build().list().get(0));
            } else {
                bookShelfBean = new BookShelfBean();
                bookShelfBean.setFinalDate(System.currentTimeMillis());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                bookShelfBean.setTag(BookShelfBean.LOCAL_TAG);
                bookShelfBean.setNoteUrl(md5);

                bookShelfBean.getBookInfoBean().setAuthor("佚名");
                bookShelfBean.getBookInfoBean().setName(book.getName().replace(".txt", "").replace(".TXT", ""));
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
                bookShelfBean.getBookInfoBean().setCoverUrl("");
                bookShelfBean.getBookInfoBean().setNoteUrl(md5);
                bookShelfBean.getBookInfoBean().setTag(BookShelfBean.LOCAL_TAG);

                saveChapter(book, md5);
                DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
            }
            bookShelfBean.getBookInfoBean().setChapterList(DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder().where(ChapterListBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).orderAsc(ChapterListBeanDao.Properties.DurChapterIndex).build().list());
            e.onNext(new LocBookShelfBean(isNew, bookShelfBean));
            e.onComplete();
        });
    }

    private void saveChapter(File book, String md5) throws IOException {
        String regex = "第.{1,7}章.{0,}";

        String encoding;

        FileInputStream fis = new FileInputStream(book);
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        encoding = detector.getDetectedCharset();
        if (encoding == null || encoding.length() == 0)
            encoding = "utf-8";
        fis.close();

        int chapterPageIndex = 0;
        String title = null;
        StringBuilder contentBuilder = new StringBuilder();
        fis = new FileInputStream(book);
        InputStreamReader inputreader = new InputStreamReader(fis, encoding);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        while ((line = buffreader.readLine()) != null) {
            line = FormatWebText.getContent(line);
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(line);
            if (m.find()) {
                String temp = line.trim().substring(0, line.trim().indexOf("第"));
                if (temp.trim().length() > 0) {
                    contentBuilder.append(temp);
                }
                if (contentBuilder.toString().length() > 0) {
                    if (contentBuilder.toString().replaceAll("　", "").trim().length() > 0) {
                        saveDurChapterContent(md5, chapterPageIndex, title, contentBuilder.toString());
                        chapterPageIndex++;
                    }
                    contentBuilder.delete(0, contentBuilder.length());
                }
                title = line.trim().substring(line.trim().indexOf("第"));
            } else {
                if (contentBuilder.length() > 0) {
                    contentBuilder.append("\r\n\u3000\u3000").append(line);
                } else {
                    contentBuilder.append("\r\u3000\u3000").append(line);
                }
                if (title == null) {
                    title = line;
                }
            }
        }
        if (contentBuilder.length() > 0) {
            saveDurChapterContent(md5, chapterPageIndex, title, contentBuilder.toString());
            contentBuilder.delete(0, contentBuilder.length());
        }
        buffreader.close();
        inputreader.close();
        fis.close();
    }

    private void saveDurChapterContent(String md5, int chapterPageIndex, String name, String content) {
        ChapterListBean chapterListBean = new ChapterListBean();
        chapterListBean.setNoteUrl(md5);
        chapterListBean.setDurChapterIndex(chapterPageIndex);
        chapterListBean.setTag(BookShelfBean.LOCAL_TAG);
        chapterListBean.setDurChapterUrl(md5 + "_" + chapterPageIndex);
        chapterListBean.setDurChapterName(name);
        chapterListBean.setHasCache(true);
        chapterListBean.getBookContentBean().setDurChapterUrl(chapterListBean.getDurChapterUrl());
        chapterListBean.getBookContentBean().setTag(BookShelfBean.LOCAL_TAG);
        chapterListBean.getBookContentBean().setNoteUrl(md5);
        chapterListBean.getBookContentBean().setDurChapterIndex(chapterListBean.getDurChapterIndex());
        chapterListBean.getBookContentBean().setDurChapterContent(content);

        DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().insertOrReplace(chapterListBean.getBookContentBean());
        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplace(chapterListBean);
    }
}
