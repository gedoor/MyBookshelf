package com.monke.monkeybook.help;

import android.content.Context;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.DownloadChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.BookmarkBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DbHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by GKF on 2018/1/18.
 * 添加删除Book
 */

public class BookshelfHelp {

    private static HashMap<String, HashSet<Integer>> getChapterCaches(){
        HashMap<String, HashSet<Integer>> temp = new HashMap<>();
        File file = FileHelp.getFolder(Constant.BOOK_CACHE_PATH);
        try {
            String[] booksCached = file.list((dir, name) -> new File(dir, name).isDirectory());
            for (String bookPath : booksCached) {
                HashSet<Integer> chapterIndexS = new HashSet<>();
                file = new File(Constant.BOOK_CACHE_PATH + bookPath);
                String[] chapters = file.list((dir, name) -> name.matches("^\\d+-.*" + FileHelp.SUFFIX_NB + "$"));
                for (String chapter : chapters) {
                    chapterIndexS.add(
                            Integer.parseInt(chapter.substring(0, chapter.indexOf('-')))
                    );
                }
                temp.put(bookPath, chapterIndexS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    private static HashMap<String, HashSet<Integer>> chapterCaches = getChapterCaches();

    public static String getCachePathName(DownloadChapterBean chapter) {
        return formatFileName(chapter.getBookName() + "-" + chapter.getTag());
    }

    public static String getCachePathName(BookInfoBean book) {
        return formatFileName(book.getName() + "-" + book.getTag());
    }

    public static void setChapterIsCached(String bookName, ChapterListBean chapter, boolean cached) {
        setChapterIsCached(bookName + "-" + chapter.getTag(), chapter.getDurChapterIndex(), cached);
    }

    public static boolean setChapterIsCached(String bookPathName, Integer index, boolean cached) {
        bookPathName = formatFileName(bookPathName);
        if(!chapterCaches.containsKey(bookPathName))
            chapterCaches.put(bookPathName, new HashSet<>());
        if (cached)
            return chapterCaches.get(bookPathName).add(index);
        else
            return chapterCaches.get(bookPathName).remove(index);
    }

    /**
     * 根据文件名判断是否被缓存过 (因为可能数据库显示被缓存过，但是文件中却没有的情况，所以需要根据文件判断是否被缓存过)
     */
    // be careful to use this method, the storage path (folderName) has been changed
    public static boolean isChapterCached(String folderName, String fileName) {
        File file = new File(Constant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(fileName) + FileHelp.SUFFIX_NB);
        return file.exists();
    }

    public static boolean isChapterCached(BookInfoBean book, ChapterListBean chapter) {
        final String path = getCachePathName(book);
        return chapterCaches.containsKey(path) && chapterCaches.get(path).contains(chapter.getDurChapterIndex());
    }

    /**
     * 删除章节文件
     */
    public static void delChapter(String folderName, String fileName) {
        FileHelp.deleteFile(Constant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(fileName) + FileHelp.SUFFIX_NB);
    }

    /**
     * 存储章节
     */
    public static void saveChapterInfo(String folderName, String fileName, String content) {
        if (content == null) {
            return;
        }
        File file = getBookFile(folderName, formatFileName(fileName));
        //获取流并存储
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建或获取存储文件
     */
    public static File getBookFile(String folderName, String fileName) {
        return FileHelp.getFile(Constant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(fileName) + FileHelp.SUFFIX_NB);
    }

    private static String formatFileName(String fileName) {
        return fileName.replace("/", "")
                .replace(":", "")
                .replace(".", "");
    }

    public static List<BookShelfBean> getAllBook() {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookInfoBean bookInfoBean = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).limit(1).build().unique();
            if (bookInfoBean != null) {
                bookShelfList.get(i).setBookInfoBean(bookInfoBean);
            } else {
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
        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteInTx(bookShelfBean.getChapterList());
        FileHelp.deleteFile(Constant.BOOK_CACHE_PATH + getCachePathName(bookShelfBean.getBookInfoBean()));
        chapterCaches.remove(getCachePathName(bookShelfBean.getBookInfoBean()));
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
        bookInfo.setIntroduce(searchBookBean.getIntroduce());
        bookInfo.setChapterUrl(searchBookBean.getChapterUrl());
        bookShelfBean.setBookInfoBean(bookInfo);
        return bookShelfBean;
    }

    public static List<ChapterListBean> getChapterList(String noteUrl) {
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
        DecimalFormat df = new DecimalFormat("0.0%");
        if (chapterAll == 0) {
            return "0.0%";
        } else if (durPageAll == 0) {
            return df.format(durChapterIndex * 1.0f / chapterAll);
        }
        String percent = df.format(durChapterIndex * 1.0f / chapterAll + 1.0f / chapterAll * (durPageIndex + 1) / durPageAll);
        if (percent.equals("100.0%") && (durChapterIndex + 1 != chapterAll || durPageIndex + 1 != durPageAll) ) {
            percent = "99.9%";
        }
        return percent;
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

    public static void clearBookshelf() {
        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().deleteAll();
        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().deleteAll();
        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteAll();
    }

}
