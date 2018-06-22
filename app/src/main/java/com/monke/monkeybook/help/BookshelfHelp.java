package com.monke.monkeybook.help;

import android.content.Context;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.BookmarkBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DbHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by GKF on 2018/1/18.
 * 添加删除Book
 */

public class BookshelfHelp {

    public static List<BookShelfBean> getAllBook() {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookInfoBean bookInfoBean = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).limit(1).build().unique();
            if (bookInfoBean != null) {
                bookInfoBean.setChapterList(getChapterList(bookInfoBean.getNoteUrl()));
                bookInfoBean.setBookmarkList(getBookmarkList(bookInfoBean.getName()));
                bookShelfList.get(i).setBookInfoBean(bookInfoBean);
            } else {
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().delete(bookShelfList.get(i));
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    public static List<BookShelfBean> getBooksByGroup(int group) {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.Group.eq(group))
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookInfoBean bookInfoBean = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).limit(1).build().unique();
            if (bookInfoBean != null) {
                bookInfoBean.setChapterList(getChapterList(bookInfoBean.getNoteUrl()));
                bookInfoBean.setBookmarkList(getBookmarkList(bookInfoBean.getName()));
                bookShelfList.get(i).setBookInfoBean(bookInfoBean);
            } else {
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().delete(bookShelfList.get(i));
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    public static BookShelfBean getBook(String bookUrl) {
        BookShelfBean bookShelfBean = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl)).build().unique();
        if (bookShelfBean != null) {
            List<BookInfoBean> temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).limit(1).build().list();
            if (temp != null && temp.size() > 0) {
                BookInfoBean bookInfoBean = temp.get(0);
                bookInfoBean.setChapterList(getChapterList(bookInfoBean.getNoteUrl()));
                bookInfoBean.setBookmarkList(getBookmarkList(bookInfoBean.getName()));
                bookShelfBean.setBookInfoBean(bookInfoBean);
                return bookShelfBean;
            }
        }
        return null;
    }

    public static void removeFromBookShelf(BookShelfBean bookShelfBean) {
        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().deleteByKey(bookShelfBean.getNoteUrl());
        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().deleteByKey(bookShelfBean.getBookInfoBean().getNoteUrl());
        List<String> keys = new ArrayList<>();
        if (bookShelfBean.getChapterListSize() > 0) {
            for (int i = 0; i < bookShelfBean.getChapterListSize(); i++) {
                keys.add(bookShelfBean.getChapterList(i).getDurChapterUrl());
            }
        }
        DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().deleteByKeyInTx(keys);
        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteInTx(bookShelfBean.getChapterList());
    }

    public static void saveBookToShelf(BookShelfBean bookShelfBean) {
        if (bookShelfBean.getErrorMsg() == null) {
            DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelfBean.getChapterList());
            DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
            DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
        }
    }

    public static BookShelfBean getBookFromSearchBook(SearchBookBean searchBookBean) {
        BookShelfBean bookShelfBean = new BookShelfBean();
        bookShelfBean.setTag(searchBookBean.getTag());
        bookShelfBean.setNoteUrl(searchBookBean.getNoteUrl());
        bookShelfBean.setFinalDate(System.currentTimeMillis());
        bookShelfBean.setDurChapter(0);
        bookShelfBean.setDurChapterPage(0);
        BookInfoBean bookInfo = new BookInfoBean();
        bookInfo.setNoteUrl(searchBookBean.getNoteUrl());
        bookInfo.setAuthor(searchBookBean.getAuthor());
        bookInfo.setCoverUrl(searchBookBean.getCoverUrl());
        bookInfo.setName(searchBookBean.getName());
        bookInfo.setTag(searchBookBean.getTag());
        bookInfo.setOrigin(searchBookBean.getOrigin());
        bookShelfBean.setBookInfoBean(bookInfo);
        return bookShelfBean;
    }

    private static List<ChapterListBean> getChapterList(String noteUrl) {
        return DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder()
                .where(ChapterListBeanDao.Properties.NoteUrl.eq(noteUrl))
                .orderAsc(ChapterListBeanDao.Properties.DurChapterIndex)
                .build()
                .list();
    }

    public static void saveBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
    }

    public static void delBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
    }

    public static List<BookmarkBean> getBookmarkList(String bookName) {
        return DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().queryBuilder()
                .where(BookmarkBeanDao.Properties.BookName.eq(bookName))
                .orderAsc(BookmarkBeanDao.Properties.ChapterIndex)
                .build()
                .list();
    }

    public static String getGroupName(Context context, int group) {
        switch (group) {
            case 1:
                return context.getString(R.string.group_yf);
            default:
                return context.getString(R.string.group_zg);
        }
    }

    public static String getReadProgress(BookShelfBean bookShelfBean) {
        return getReadProgress(bookShelfBean.getDurChapter(), bookShelfBean.getChapterListSize(), 0, 0);
    }

    public static String getReadProgress(int durChapterIndex, int chapterAll, int durPageIndex, int durPageAll) {
        DecimalFormat df = new DecimalFormat("0.00%");
        if (chapterAll == 0) {
            return "0.00%";
        } else if (durPageAll == 0) {
            return df.format(durChapterIndex * 1.0f / chapterAll);
        }
        return df.format(durChapterIndex * 1.0f / chapterAll + 1.0f / chapterAll * (durPageIndex + 1) / durPageAll);
    }

    /**
     * 排序
     */
    public static void order(List<BookShelfBean> books, String bookshelfOrder) {
        if (books == null || books.size() == 0) {
            return;
        }
        switch (bookshelfOrder) {
            case "0":
                Collections.sort(books, (o1, o2) -> {
                    if (o1.getFinalDate() - o2.getFinalDate() > 0) {
                        return -1;
                    } else if (o1.getFinalDate() - o2.getFinalDate() < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
                break;
            case "1":
                Collections.sort(books, (o1, o2) -> {
                    if (o1.getFinalRefreshData() - o2.getFinalRefreshData() > 0) {
                        return -1;
                    } else if (o1.getFinalRefreshData() - o2.getFinalRefreshData() < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
                break;
            case "2":
                Collections.sort(books, (o1, o2) -> o1.getSerialNumber() - o2.getSerialNumber());
                break;
        }
    }

}
