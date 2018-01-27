package com.monke.monkeybook.model;

import android.widget.Toast;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.listener.OnObservableListener;
import com.monke.monkeybook.model.content.DefaultModelImpl;
import com.monke.monkeybook.model.content.GxwztvBookModelImpl;
import com.monke.monkeybook.model.content.LingdiankanshuModelImpl;
import com.monke.monkeybook.model.content.XBQGModelImpl;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
        allBookSource.clear();
        allBookSource.add(getBookSourceDd());
        allBookSource.add(getBookSourceBy());
        allBookSource.add(getBookSource(XBQGModelImpl.TAG, XBQGModelImpl.name, 3));
        allBookSource.add(getBookSource(LingdiankanshuModelImpl.TAG, LingdiankanshuModelImpl.name, 4));
        allBookSource.add(getBookSource(GxwztvBookModelImpl.TAG, GxwztvBookModelImpl.name, 5));

        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(allBookSource);

        refreshBookSource();
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

    static BookSourceBean getBookSource(String bookSourceUrl, String bookSourceName, int serialNumber) {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl(bookSourceUrl);
        bookSourceBean.setBookSourceName(bookSourceName);
        bookSourceBean.setSerialNumber(serialNumber);
        bookSourceBean.setEnable(true);
        return bookSourceBean;
    }

    public static void addBookSource(BookSourceBean oldBookSource, BookSourceBean newBookSource, OnObservableListener observableListener) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (oldBookSource != null) {
                newBookSource.setSerialNumber(oldBookSource.getSerialNumber());
                newBookSource.setEnable(oldBookSource.getEnable());
                DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().delete(oldBookSource);
            }
            if (newBookSource.getSerialNumber() == 0) {
                newBookSource.setSerialNumber(allBookSource.size() + 1);
            }
            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(newBookSource);
            BookSourceManage.refreshBookSource();
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        observableListener.success();
                    }

                    @Override
                    public void onError(Throwable e) {
                        observableListener.error();
                    }
                });

    }

    static BookSourceBean getBookSourceBy() {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl("https://www.zwdu.com");
        bookSourceBean.setBookSourceName("八一中文");
        bookSourceBean.setSerialNumber(1);
        bookSourceBean.setEnable(true);
        bookSourceBean.setRuleSearchUrl("https://www.zwdu.com/search.php?keyword=searchKey&page=searchPage");
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

    static BookSourceBean getBookSourceDd() {
        BookSourceBean bookSourceBean = new BookSourceBean();
        bookSourceBean.setBookSourceUrl("http://www.23us.so/");
        bookSourceBean.setBookSourceName("顶点小说");
        bookSourceBean.setSerialNumber(2);
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
        bookSourceBean.setRuleIntroduce("id.content@tag.dd.3@tag.p.1@text");
        bookSourceBean.setRuleChapterUrl("ic.content@class.read.0@href");

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
            default:
                return DefaultModelImpl.getInstance(tag);
        }
    }
}
