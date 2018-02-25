package com.monke.monkeybook.model;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.content.DefaultModelImpl;
import com.monke.monkeybook.model.content.GxwztvBookModelImpl;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.util.ArrayList;
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
            selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .where(BookSourceBeanDao.Properties.Enable.eq(true))
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
        }
        if (selectedBookSource.size() == 0) {
            selectedBookSource = getAllBookSource();
        }
        return selectedBookSource;
    }

    public static List<BookSourceBean> getAllBookSource() {
        if (allBookSource == null) {
            allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
        }
        if (allBookSource.size() == 0) {
            allBookSource = saveBookSourceToDb();
        }
        return allBookSource;
    }

    public static List<BookSourceBean> saveBookSourceToDb() {
        List<BookSourceBean> bookSourceBeans = new ArrayList<>();
        bookSourceBeans.add(getBookSourceXBQ());
        bookSourceBeans.add(getBookSourceLD());
        bookSourceBeans.add(getBookSourceWZ());
        addBookSource(bookSourceBeans);
        return allBookSource;
    }

    public static void refreshBookSource() {
        allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
        selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
    }

    public static void addBookSource(List<BookSourceBean> bookSourceBeans) {
        refreshBookSource();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            addBookSource(bookSourceBean);
        }
        refreshBookSource();
    }

    public static void addBookSource(BookSourceBean bookSourceBean) {
        if (bookSourceBean.getBookSourceUrl().endsWith("/")) {
            bookSourceBean.setBookSourceUrl(bookSourceBean.getBookSourceUrl().substring(0, bookSourceBean.getBookSourceUrl().lastIndexOf("/")));
        }
        List<BookSourceBean> temp = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl())).list();
        if (temp != null && temp.size() > 0) {
            bookSourceBean.setSerialNumber(temp.get(0).getSerialNumber());
            bookSourceBean.setEnable(temp.get(0).getEnable());
        }
        if (bookSourceBean.getSerialNumber() == 0) {
            bookSourceBean.setSerialNumber(allBookSource.size() + 1);
        }
        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
    }

    private static BookSourceBean getBookSourceWZ() {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl("http://www.gxwztv.com");
        bookSourceBean.setBookSourceName("梧州中文台");
        bookSourceBean.setEnable(true);
        bookSourceBean.setRuleSearchUrl("http://www.gxwztv.com/search.htm?keyword=searchKey&pn=searchPage-1");
        bookSourceBean.setRuleSearchList("class.list-group-item!0:%");
        bookSourceBean.setRuleSearchAuthor("class.col-xs-2.0@text");
        bookSourceBean.setRuleSearchKind("class.col-xs-1.0@text");
        bookSourceBean.setRuleSearchLastChapter("class.col-xs-4.0@tag.a.0@text");
        bookSourceBean.setRuleSearchName("class.col-xs-3.0@tag.a.0@text");
        bookSourceBean.setRuleSearchNoteUrl("class.col-xs-3.0@tag.a.0@href");
        bookSourceBean.setRuleSearchCoverUrl("");
        bookSourceBean.setRuleBookName("class.active.0@text");
        bookSourceBean.setRuleBookAuthor("class.col-xs-12 list-group-item no-border.0@tag.small.0@text");
        bookSourceBean.setRuleIntroduce("class.panel panel-default mt20.0@id.shot@text");
        bookSourceBean.setRuleCoverUrl("class.panel-body.0@class.img-thumbnail.0@src");
        bookSourceBean.setRuleChapterUrl("class.list-group-item tac.0@tag.a.0@href");
        bookSourceBean.setRuleChapterList("id.chapters-list@tag.a");
        bookSourceBean.setRuleChapterName("text");
        bookSourceBean.setRuleContentUrl("href");
        bookSourceBean.setRuleBookContent("id.txtContent@textNodes");

        return bookSourceBean;
    }

    private static BookSourceBean getBookSourceLD() {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl("http://www.lingdiankanshu.co");
        bookSourceBean.setBookSourceName("零点看书");
        bookSourceBean.setEnable(true);
        bookSourceBean.setRuleSearchUrl("http://zhannei.baidu.com/cse/search?s=16865089933227718744&q=searchKey&p=searchPage-1");
        bookSourceBean.setRuleSearchList("class.result-item");
        bookSourceBean.setRuleSearchAuthor("class.result-game-item-info-tag.0@tag.span.1@text");
        bookSourceBean.setRuleSearchKind("class.result-game-item-info-tag.1@tag.span.1@text");
        bookSourceBean.setRuleSearchLastChapter("class.result-game-item-info-tag.3@tag.a.0@text");
        bookSourceBean.setRuleSearchName("class.result-item-title.0@tag.a.0@text");
        bookSourceBean.setRuleSearchNoteUrl("class.result-item-title.0@tag.a.0@href");
        bookSourceBean.setRuleSearchCoverUrl("tag.img.0@src");
        bookSourceBean.setRuleBookName("class.box_con.0@id.info@tag.h1.0@text");
        bookSourceBean.setRuleBookAuthor("class.box_con.0@id.info@tag.p.0@text");
        bookSourceBean.setRuleIntroduce("id.intro@textNodes");
        bookSourceBean.setRuleCoverUrl("id.fmimg@tag.img.0@src");
        bookSourceBean.setRuleChapterList("id.list@tag.dd");
        bookSourceBean.setRuleChapterName("tag.a.0@text");
        bookSourceBean.setRuleContentUrl("tag.a.0@href");
        bookSourceBean.setRuleBookContent("id.content@textNodes");

        return bookSourceBean;
    }

    private static BookSourceBean getBookSourceXBQ() {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl("http://www.xxbiquge.com");
        bookSourceBean.setBookSourceName("新笔趣阁");
        bookSourceBean.setEnable(true);
        bookSourceBean.setRuleSearchUrl("http://zhannei.baidu.com/cse/search?s=5199337987683747968&q=searchKey&p=searchPage-1");
        bookSourceBean.setRuleSearchList("class.result-item");
        bookSourceBean.setRuleSearchAuthor("class.result-game-item-info-tag.0@tag.span.1@text");
        bookSourceBean.setRuleSearchKind("class.result-game-item-info-tag.1@tag.span.1@text");
        bookSourceBean.setRuleSearchLastChapter("class.result-game-item-info-tag.3@tag.a.0@text");
        bookSourceBean.setRuleSearchName("class.result-item-title.0@tag.a.0@text");
        bookSourceBean.setRuleSearchNoteUrl("class.result-item-title.0@tag.a.0@href");
        bookSourceBean.setRuleSearchCoverUrl("tag.img.0@src");
        bookSourceBean.setRuleBookName("class.box_con.0@id.info@tag.h1.0@text");
        bookSourceBean.setRuleBookAuthor("class.box_con.0@id.info@tag.p.0@text");
        bookSourceBean.setRuleIntroduce("id.intro@tag.p@text");
        bookSourceBean.setRuleCoverUrl("id.fmimg@tag.img.0@src");
        bookSourceBean.setRuleChapterList("id.list@tag.dd");
        bookSourceBean.setRuleChapterName("tag.a.0@text");
        bookSourceBean.setRuleContentUrl("tag.a.0@href");
        bookSourceBean.setRuleBookContent("id.content@textNodes");

        return bookSourceBean;
    }

    //获取book source class
    static IStationBookModel getBookSourceModel(String tag) {
        switch (tag) {
            case BookShelfBean.LOCAL_TAG:
                return null;
            case GxwztvBookModelImpl.TAG:
                return GxwztvBookModelImpl.getInstance();
            default:
                return DefaultModelImpl.getInstance(tag);
        }
    }
}
