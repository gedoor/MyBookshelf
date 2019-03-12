package com.kunfei.bookshelf.help;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.bean.DownloadChapterBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.AppConstant;
import com.kunfei.bookshelf.dao.BookInfoBeanDao;
import com.kunfei.bookshelf.dao.BookShelfBeanDao;
import com.kunfei.bookshelf.dao.BookSourceBeanDao;
import com.kunfei.bookshelf.dao.BookmarkBeanDao;
import com.kunfei.bookshelf.dao.ChapterListBeanDao;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.utils.StringUtils;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by GKF on 2018/1/18.
 * 添加删除Book
 */

public class BookshelfHelp {

    public static Pattern chapterNamePattern = Pattern.compile("^(.*?第([\\d零〇一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟０-９\\s]+)[章节篇回集])[、，。　：:.\\s]*");
    private static HashMap<String, HashSet<Integer>> chapterCaches = getChapterCaches();

    private static HashMap<String, HashSet<Integer>> getChapterCaches() {
        HashMap<String, HashSet<Integer>> temp = new HashMap<>();
        File file = FileHelp.getFolder(AppConstant.BOOK_CACHE_PATH);
        try {
            String[] booksCached = file.list((dir, name) -> new File(dir, name).isDirectory());
            for (String bookPath : booksCached) {
                HashSet<Integer> chapterIndexS = new HashSet<>();
                file = new File(AppConstant.BOOK_CACHE_PATH + bookPath);
                String[] chapters = file.list((dir, name) -> name.matches("^\\d{5,}-.*" + FileHelp.SUFFIX_NB + "$"));
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

    public static String getCachePathName(DownloadChapterBean chapter) {
        return formatFolderName(chapter.getBookName() + "-" + chapter.getTag());
    }

    public static String getCachePathName(BookInfoBean book) {
        return formatFolderName(book.getName() + "-" + book.getTag());
    }

    public static String getCacheFileName(int chapterIndex, String chapterName) {
        return formatFileName(chapterIndex, chapterName);
    }

    public static void setChapterIsCached(String bookName, ChapterListBean chapter, boolean cached) {
        setChapterIsCached(bookName + "-" + chapter.getTag(), chapter.getDurChapterIndex(), cached);
    }

    public static boolean setChapterIsCached(String bookPathName, Integer index, boolean cached) {
        bookPathName = formatFolderName(bookPathName);
        if (!chapterCaches.containsKey(bookPathName))
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
    public static boolean isChapterCached(String folderName, int index, String fileName) {
        File file = new File(AppConstant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(index, fileName) + FileHelp.SUFFIX_NB);
        boolean cached = file.exists();
        setChapterIsCached(folderName, index, cached);
        return cached;
    }

    public static boolean isChapterCached(BookInfoBean book, BaseChapterBean chapter) {
        final String path = getCachePathName(book);
        return chapterCaches.containsKey(path) && chapterCaches.get(path).contains(chapter.getDurChapterIndex());
    }

    public static void clearCaches(boolean clearChapterList) {
        FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH);
        FileHelp.getFolder(AppConstant.BOOK_CACHE_PATH);
        chapterCaches.clear();
        if (clearChapterList)
            DbHelper.getDaoSession().getChapterListBeanDao().deleteAll();
    }

    /**
     * 删除章节文件
     */
    public static void delChapter(String folderName, int index, String fileName) {
        FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(index, fileName) + FileHelp.SUFFIX_NB);
        setChapterIsCached(folderName, index, false);
    }

    /**
     * 存储章节
     */
    public static synchronized boolean saveChapterInfo(String folderName, int index, String fileName, String content) {
        if (content == null) {
            return false;
        }
        File file = getBookFile(folderName, index, fileName);
        //获取流并存储
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(fileName + "\n\n");
            writer.write(content);
            writer.write("\n\n");
            writer.flush();
            setChapterIsCached(folderName, index, true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建或获取存储文件
     */
    public static File getBookFile(String folderName, int index, String fileName) {
        return FileHelp.getFile(AppConstant.BOOK_CACHE_PATH + formatFolderName(folderName)
                + File.separator + formatFileName(index, fileName) + FileHelp.SUFFIX_NB);
    }

    @SuppressLint("DefaultLocale")
    private static String formatFileName(int index, String fileName) {
        return String.format("%05d-%s", index, formatFolderName(fileName));
    }

    private static String formatFolderName(String folderName) {
        return folderName.replace("/", "")
                .replace(":", "")
                .replace(".", "");
    }

    /**
     * 根据目录名获取当前章节
     */
    public static int getDurChapter(BookShelfBean oldBook, BookShelfBean newBook) {
        int oldChapterSize = oldBook.getChapterListSize();
        if (oldChapterSize == 0)
            return 0;
        int oldChapterIndex = oldBook.getDurChapter();
        int oldChapterNum = oldBook.getChapter(oldBook.getDurChapter()).getChapterNum();
        String oldName = oldBook.getChapter(oldBook.getDurChapter()).getPureChapterName();
        int newChapterSize = newBook.getChapterListSize();
        int min = Math.max(0, Math.min(oldChapterIndex, oldChapterIndex - oldChapterSize + newChapterSize) - 10);
        int max = Math.min(newChapterSize - 1, Math.max(oldChapterIndex, oldChapterIndex - oldChapterSize + newChapterSize) + 10);
        double nameSim = 0;
        int newIndex = 0;
        int newNum = 0;
        if (!oldName.isEmpty()) {
            StringSimilarityService service = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
            for (int i = min; i <= max; i++) {
                String newName = newBook.getChapter(i).getPureChapterName();
                double temp = service.score(oldName, newName);
                if (temp > nameSim) {
                    nameSim = temp;
                    newIndex = i;
                }
            }
        }
        if (nameSim < 0.96 && oldChapterNum > 0) {
            for (int i = min; i <= max; i++) {
                int temp = newBook.getChapter(i).getChapterNum();
                if (temp == oldChapterNum) {
                    newNum = temp;
                    newIndex = i;
                    break;
                } else if (Math.abs(temp - oldChapterNum) < Math.abs(newNum - oldChapterNum)) {
                    newNum = temp;
                    newIndex = i;
                }
            }
        }
        if (nameSim > 0.96 || Math.abs(newNum - oldChapterNum) < 1) {
            return newIndex;
        } else {
            return Math.min(Math.max(0, newBook.getChapterListSize() - 1), oldChapterIndex);
        }
    }

    /**
     * 获取所有书籍
     */
    public static List<BookShelfBean> getAllBook() {
        List<BookShelfBean> bookShelfList = DbHelper.getDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookInfoBean bookInfoBean = DbHelper.getDaoSession().getBookInfoBeanDao().queryBuilder()
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

    /**
     * 获取书籍按分组
     */
    public static List<BookShelfBean> getBooksByGroup(int group) {
        List<BookShelfBean> bookShelfList = DbHelper.getDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.Group.eq(group))
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookInfoBean bookInfoBean = DbHelper.getDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).limit(1).build().unique();
            if (bookInfoBean != null) {
                bookShelfList.get(i).setBookInfoBean(bookInfoBean);
            } else {
                DbHelper.getDaoSession().getBookShelfBeanDao().delete(bookShelfList.get(i));
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    /**
     * 获取书籍按bookUrl
     */
    public static BookShelfBean getBook(String bookUrl) {
        BookShelfBean bookShelfBean = DbHelper.getDaoSession().getBookShelfBeanDao().load(bookUrl);
        if (bookShelfBean != null) {
            BookInfoBean bookInfoBean = DbHelper.getDaoSession().getBookInfoBeanDao().load(bookUrl);
            if (bookInfoBean != null) {
                bookInfoBean.setChapterList(getChapterList(bookInfoBean.getNoteUrl()));
                bookInfoBean.setBookmarkList(getBookmarkList(bookInfoBean.getName()));
                bookShelfBean.setBookInfoBean(bookInfoBean);
                return bookShelfBean;
            }
        }
        return null;
    }

    /**
     * 移除书籍
     */
    public static void removeFromBookShelf(BookShelfBean bookShelfBean, boolean keepCaches) {
        DbHelper.getDaoSession().getBookShelfBeanDao().deleteByKey(bookShelfBean.getNoteUrl());
        DbHelper.getDaoSession().getBookInfoBeanDao().deleteByKey(bookShelfBean.getBookInfoBean().getNoteUrl());
        delChapterList(bookShelfBean.getNoteUrl());
        if (!keepCaches) {
            String bookName = bookShelfBean.getBookInfoBean().getName();
            // 如果书架上有其他同名书籍，只删除本书源的缓存
            long bookNum = DbHelper.getDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.Name.eq(bookName)).count();
            if (bookNum > 0) {
                FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH + getCachePathName(bookShelfBean.getBookInfoBean()));
                chapterCaches.remove(getCachePathName(bookShelfBean.getBookInfoBean()));
                return;
            }
            // 没有同名书籍，删除本书所有的缓存
            try {
                File file = FileHelp.getFolder(AppConstant.BOOK_CACHE_PATH);
                String[] bookCaches = file.list((dir, name) -> new File(dir, name).isDirectory() && name.startsWith(bookName + "-"));
                for (String bookPath : bookCaches) {
                    FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH + bookPath);
                    chapterCaches.remove(bookPath);
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 是否在书架
     */
    public static boolean isInBookShelf(String bookUrl) {
        if (bookUrl == null) {
            return false;
        }

        long count = DbHelper.getDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl))
                .count();
        return count > 0;
    }

    /**
     * 移除书籍
     */
    public static void removeFromBookShelf(BookShelfBean bookShelfBean) {
        removeFromBookShelf(bookShelfBean, false);
    }

    public static void saveBookSource(BookSourceBean bookSourceBean) {
        if (bookSourceBean != null) {
            DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
        }
    }

    /**
     * 保存书籍
     */
    public static void saveBookToShelf(BookShelfBean bookShelfBean) {
        if (bookShelfBean.getErrorMsg() == null) {
            DbHelper.getDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelfBean.getChapterList());
            DbHelper.getDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
            DbHelper.getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
        }
    }

    /**
     * 搜索转书籍
     */
    public static BookShelfBean getBookFromSearchBook(SearchBookBean searchBookBean) {
        BookShelfBean bookShelfBean = new BookShelfBean();
        bookShelfBean.setTag(searchBookBean.getTag());
        bookShelfBean.setNoteUrl(searchBookBean.getNoteUrl());
        bookShelfBean.setFinalDate(System.currentTimeMillis());
        bookShelfBean.setDurChapter(0);
        bookShelfBean.setDurChapterPage(0);
        bookShelfBean.setVariable(searchBookBean.getVariable());
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
        List<ChapterListBean> chapterListBeans = DbHelper.getDaoSession().getChapterListBeanDao().queryBuilder()
                .where(ChapterListBeanDao.Properties.NoteUrl.eq(noteUrl))
                .orderAsc(ChapterListBeanDao.Properties.DurChapterIndex)
                .build()
                .list();
        if (chapterListBeans == null) {
            chapterListBeans = new ArrayList<>();
        }
        return chapterListBeans;
    }

    public static void delChapterList(String noteUrl) {
        DbHelper.getDaoSession().getChapterListBeanDao().queryBuilder()
                .where(ChapterListBeanDao.Properties.NoteUrl.eq(noteUrl))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public static void saveBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
    }

    public static void delBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
    }

    public static List<BookmarkBean> getBookmarkList(String bookName) {
        return DbHelper.getDaoSession().getBookmarkBeanDao().queryBuilder()
                .where(BookmarkBeanDao.Properties.BookName.eq(bookName))
                .orderAsc(BookmarkBeanDao.Properties.ChapterIndex)
                .build()
                .list();
    }

    public static String getReadProgress(BookShelfBean bookShelfBean) {
        return getReadProgress(bookShelfBean.getDurChapter(), bookShelfBean.getChapterListSize(), 0, 0);
    }

    public static String getReadProgress(int durChapterIndex, int chapterAll, int durPageIndex, int durPageAll) {
        DecimalFormat df = new DecimalFormat("0.0%");
        if (chapterAll == 0 || (durPageAll == 0 && durChapterIndex == 0)) {
            return "0.0%";
        } else if (durPageAll == 0) {
            return df.format((durChapterIndex + 1.0f) / chapterAll);
        }
        String percent = df.format(durChapterIndex * 1.0f / chapterAll + 1.0f / chapterAll * (durPageIndex + 1) / durPageAll);
        if (percent.equals("100.0%") && (durChapterIndex + 1 != chapterAll || durPageIndex + 1 != durPageAll)) {
            percent = "99.9%";
        }
        return percent;
    }

    public static BookSourceBean getBookSourceByTag(String tag) {
        if (tag == null)
            return null;
        return DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(tag)).unique();
    }

    public static int guessChapterNum(String name) {
        if (TextUtils.isEmpty(name) || name.matches("第.*?卷.*?第.*[章节回]"))
            return -1;
        Matcher matcher = chapterNamePattern.matcher(name);
        if (matcher.find()) {
            return StringUtils.stringToInt(matcher.group(2));
        }
        return -1;
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
                Collections.sort(books, (o1, o2) -> Long.compare(o2.getFinalDate(), o1.getFinalDate()));
                break;
            case "1":
                Collections.sort(books, (o1, o2) -> Long.compare(o2.getFinalRefreshData(), o1.getFinalRefreshData()));
                break;
            case "2":
                Collections.sort(books, (o1, o2) -> Integer.compare(o1.getSerialNumber(), o2.getSerialNumber()));
                break;
        }
    }

    /**
     * 清除书架
     */
    public static void clearBookshelf() {
        DbHelper.getDaoSession().getBookShelfBeanDao().deleteAll();
        DbHelper.getDaoSession().getBookInfoBeanDao().deleteAll();
        DbHelper.getDaoSession().getChapterListBeanDao().deleteAll();
    }

}
