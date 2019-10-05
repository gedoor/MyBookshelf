package com.kunfei.bookshelf.help;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.AppConstant;
import com.kunfei.bookshelf.dao.BookChapterBeanDao;
import com.kunfei.bookshelf.dao.BookInfoBeanDao;
import com.kunfei.bookshelf.dao.BookShelfBeanDao;
import com.kunfei.bookshelf.dao.BookmarkBeanDao;
import com.kunfei.bookshelf.utils.StringUtils;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by GKF on 2018/1/18.
 * 添加删除Book
 */

public class BookshelfHelp {

    private static Pattern chapterNamePattern = Pattern.compile("^(.*?第([\\d零〇一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟０-９\\s]+)[章节篇回集])[、，。　：:.\\s]*");

    public static String getCachePathName(String bookName, String tag) {
        return formatFolderName(bookName + "-" + tag);
    }

    @SuppressLint("DefaultLocale")
    public static String getCacheFileName(int chapterIndex, String chapterName) {
        return String.format("%05d-%s", chapterIndex, formatFolderName(chapterName));
    }

    public static boolean isChapterCached(String bookName, String tag, BaseChapterBean chapter, boolean isAudio) {
        if (isAudio) {
            BookContentBean contentBean = DbHelper.getDaoSession().getBookContentBeanDao().load(chapter.getDurChapterUrl());
            if (contentBean == null) return false;
            if (contentBean.outTime()) {
                DbHelper.getDaoSession().getBookContentBeanDao().delete(contentBean);
                return false;
            }
            return !TextUtils.isEmpty(contentBean.getDurChapterContent());
        }
        File file = new File(AppConstant.BOOK_CACHE_PATH + getCachePathName(bookName, tag)
                + File.separator + getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName()) + FileHelp.SUFFIX_NB);
        return file.exists();
    }

    public static String getChapterCache(BookShelfBean bookShelfBean, BookChapterBean chapter) {
        if (bookShelfBean.isAudio()) {
            BookContentBean contentBean = DbHelper.getDaoSession().getBookContentBeanDao().load(chapter.getDurChapterUrl());
            if (contentBean == null) return null;
            if (contentBean.outTime()) {
                DbHelper.getDaoSession().getBookContentBeanDao().delete(contentBean);
                return null;
            }
            return contentBean.getDurChapterContent();
        }
        File file = new File(AppConstant.BOOK_CACHE_PATH
                + formatFolderName(BookshelfHelp.getCachePathName(bookShelfBean.getBookInfoBean().getName(), bookShelfBean.getTag()))
                + File.separator + getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName()) + FileHelp.SUFFIX_NB);
        if (!file.exists()) return null;

        byte[] contentByte = DocumentHelper.getBytes(file);
        return new String(contentByte, StandardCharsets.UTF_8);
    }

    public static void clearCaches(boolean clearChapterList) {
        FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH);
        FileHelp.getFolder(AppConstant.BOOK_CACHE_PATH);
        if (clearChapterList)
            DbHelper.getDaoSession().getBookChapterBeanDao().deleteAll();
    }

    /**
     * 删除章节文件
     */
    public static void delChapter(String folderName, int index, String fileName) {
        FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH + folderName
                + File.separator + getCacheFileName(index, fileName) + FileHelp.SUFFIX_NB);
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
                + File.separator + getCacheFileName(index, fileName) + FileHelp.SUFFIX_NB);
    }

    private static String formatFolderName(String folderName) {
        return folderName.replaceAll("[\\\\/:*?\"<>|.]", "");
    }

    /**
     * 根据目录名获取当前章节
     */
    public static int getDurChapter(int oldDurChapterIndex, int oldChapterListSize, String oldDurChapterName, List<BookChapterBean> newChapterList) {
        if (oldChapterListSize == 0)
            return 0;
        int oldChapterNum = getChapterNum(oldDurChapterName);
        String oldName = getPureChapterName(oldDurChapterName);
        int newChapterSize = newChapterList.size();
        int min = Math.max(0, Math.min(oldDurChapterIndex, oldDurChapterIndex - oldChapterListSize + newChapterSize) - 10);
        int max = Math.min(newChapterSize - 1, Math.max(oldDurChapterIndex, oldDurChapterIndex - oldChapterListSize + newChapterSize) + 10);
        double nameSim = 0;
        int newIndex = 0;
        int newNum = 0;
        if (!oldName.isEmpty()) {
            StringSimilarityService service = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
            for (int i = min; i <= max; i++) {
                String newName = getPureChapterName(newChapterList.get(i).getDurChapterName());
                double temp = service.score(oldName, newName);
                if (temp > nameSim) {
                    nameSim = temp;
                    newIndex = i;
                }
            }
        }
        if (nameSim < 0.96 && oldChapterNum > 0) {
            for (int i = min; i <= max; i++) {
                int temp = getChapterNum(newChapterList.get(i).getDurChapterName());
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
            return Math.min(Math.max(0, newChapterList.size() - 1), oldDurChapterIndex);
        }
    }

    private static int getChapterNum(String chapterName) {
        if (chapterName != null) {
            Matcher matcher = chapterNamePattern.matcher(chapterName);
            if (matcher.find()) {
                return StringUtils.stringToInt(matcher.group(2));
            }
        }
        return -1;
    }

    private static String getPureChapterName(String chapterName) {
        return chapterName == null ? ""
                : StringUtils.fullToHalf(chapterName).replaceAll("\\s", "")
                .replaceAll("^第.*?章|[(\\[][^()\\[\\]]{2,}[)\\]]$", "")
                .replaceAll("[^\\w\\u4E00-\\u9FEF〇\\u3400-\\u4DBF\\u20000-\\u2A6DF\\u2A700-\\u2EBEF]", "");
        // 所有非字母数字中日韩文字 CJK区+扩展A-F区
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
                bookShelfBean.setBookInfoBean(bookInfoBean);
                return bookShelfBean;
            }
        }
        return null;
    }

    public static List<BookInfoBean> searchBookInfo(String key) {
        return DbHelper.getDaoSession().getBookInfoBeanDao().queryBuilder()
                .where(BookInfoBeanDao.Properties.Name.like("%" + key + "%"))
                .list();
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
                FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH + getCachePathName(bookShelfBean.getBookInfoBean().getName(), bookShelfBean.getTag()));
                return;
            }
            // 没有同名书籍，删除本书所有的缓存
            try {
                File file = FileHelp.getFolder(AppConstant.BOOK_CACHE_PATH);
                String[] bookCaches = file.list((dir, name) -> new File(dir, name).isDirectory() && name.startsWith(bookName + "-"));
                for (String bookPath : bookCaches) {
                    FileHelp.deleteFile(AppConstant.BOOK_CACHE_PATH + bookPath);
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

    /**
     * 保存书籍
     */
    public static void saveBookToShelf(BookShelfBean bookShelfBean) {
        if (bookShelfBean.getErrorMsg() == null) {
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
        BookInfoBean bookInfo = bookShelfBean.getBookInfoBean();
        bookInfo.setNoteUrl(searchBookBean.getNoteUrl());
        bookInfo.setAuthor(searchBookBean.getAuthor());
        bookInfo.setCoverUrl(searchBookBean.getCoverUrl());
        bookInfo.setName(searchBookBean.getName());
        bookInfo.setTag(searchBookBean.getTag());
        bookInfo.setOrigin(searchBookBean.getOrigin());
        bookInfo.setIntroduce(searchBookBean.getIntroduce());
        bookInfo.setChapterUrl(searchBookBean.getChapterUrl());
        bookInfo.setBookInfoHtml(searchBookBean.getBookInfoHtml());
        bookShelfBean.setVariable(searchBookBean.getVariable());
        return bookShelfBean;
    }

    public static List<BookChapterBean> getChapterList(String noteUrl) {
        return DbHelper.getDaoSession().getBookChapterBeanDao().queryBuilder()
                .where(BookChapterBeanDao.Properties.NoteUrl.eq(noteUrl))
                .orderAsc(BookChapterBeanDao.Properties.DurChapterIndex)
                .build()
                .list();
    }

    public static void delChapterList(String noteUrl) {
        DbHelper.getDaoSession().getBookChapterBeanDao().queryBuilder()
                .where(BookChapterBeanDao.Properties.NoteUrl.eq(noteUrl))
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

    public static String formatAuthor(String author) {
        if (author == null) {
            return "";
        }
        return author.replaceAll("作\\s*者[\\s:：]*", "").replaceAll("\\s+", " ").trim();
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
        DbHelper.getDaoSession().getBookChapterBeanDao().deleteAll();
    }

}
