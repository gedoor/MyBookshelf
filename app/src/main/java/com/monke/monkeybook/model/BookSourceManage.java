package com.monke.monkeybook.model;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.content.DefaultModelImpl;
import com.monke.monkeybook.model.content.ZwduModelImpl;
import com.monke.monkeybook.model.content.GxwztvBookModelImpl;
import com.monke.monkeybook.model.content.LingdiankanshuModelImpl;
import com.monke.monkeybook.model.content.XBQGModelImpl;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.util.List;

/**
 * Created by GKF on 2017/12/15.
 * 所有书源
 */

public class BookSourceManage {
    private static List<BookSourceBean> selectedBookSource;
    private static List<BookSourceBean> allBookSource;

    public static BookSourceManage getInstance() {
        return new BookSourceManage();
    }

    static List<BookSourceBean> getSelectedBookSource() {
        if (selectedBookSource == null) {
            selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder().list();
        }
        if (selectedBookSource.size() == 0) {
            selectedBookSource = getAllBookSource();
        }
        return selectedBookSource;
    }

    public static List<BookSourceBean> getAllBookSource() {
        if (allBookSource == null) {
            allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder().list();
        }
        if (allBookSource.size() == 0) {
            allBookSource = saveBookSourceToDb();
        }
        return allBookSource;
    }

    public static List<BookSourceBean> saveBookSourceToDb() {
        allBookSource.clear();
        allBookSource.add(getBookSourceDd());
        allBookSource.add(getBookSource(XBQGModelImpl.TAG, XBQGModelImpl.name, 1));
        allBookSource.add(getBookSource(LingdiankanshuModelImpl.TAG, LingdiankanshuModelImpl.name, 2));
        allBookSource.add(getBookSource(GxwztvBookModelImpl.TAG, GxwztvBookModelImpl.name, 3));
        allBookSource.add(getBookSource(ZwduModelImpl.TAG, ZwduModelImpl.name, 4));

        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(allBookSource);
        selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder().list();

        return allBookSource;
    }

    static BookSourceBean getBookSource(String bookSourceUrl, String bookSourceName, int serialNumber) {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl(bookSourceUrl);
        bookSourceBean.setBookSourceName(bookSourceName);
        bookSourceBean.setSerialNumber(serialNumber);
        bookSourceBean.setEnable(true);
        return bookSourceBean;
    }

    static BookSourceBean getBookSourceDd() {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl("http://www.23us.so/");
        bookSourceBean.setBookSourceName("顶点小说");
        bookSourceBean.setSerialNumber(5);
        bookSourceBean.setEnable(true);
        bookSourceBean.setRuleSearchUrl("http://zhannei.baidu.com/cse/search?s=8053757951023821596&q=searchKey&p=searchPage");
        bookSourceBean.setRuleSearchList("class.result-item");
        bookSourceBean.setRuleSearchAuthor("class.result-game-item-info-tag.0@tag.span.1@text");
        bookSourceBean.setRuleSearchKind("class.result-game-item-info-tag.1@tag.span.1@text");
        bookSourceBean.setRuleSearchLastChapter("class.result-game-item-info-tag.3@tag.a.0@text");
        bookSourceBean.setRuleSearchName("class.result-item-title.0@tag.a.0@text");
        bookSourceBean.setRuleSearchNoteUrl("class.result-item-title.0@tag.a.0@href");
        bookSourceBean.setRuleSearchCoverUrl("class.result-game-item-pic.0@tag.img.0@src");
        bookSourceBean.setRuleChapterList("id.at@tag.td");
        bookSourceBean.setRuleChapterName("tag.a.0@text");
        bookSourceBean.setRuleContentUrl("tag.a.0@href");
        bookSourceBean.setRuleBookContent("id.contents@textNodes");

        return bookSourceBean;
    }

    //获取book source class
    static IStationBookModel getBookSourceModel(String tag) {
        switch (tag) {
            case BookShelfBean.LOCAL_TAG:
                return null;
            case GxwztvBookModelImpl.TAG:
                return GxwztvBookModelImpl.getInstance();
            case LingdiankanshuModelImpl.TAG:
                return LingdiankanshuModelImpl.getInstance();
            case XBQGModelImpl.TAG:
                return XBQGModelImpl.getInstance();
            case ZwduModelImpl.TAG:
                return ZwduModelImpl.getInstance();
            default:
                return DefaultModelImpl.getInstance(tag);
        }
    }
}
