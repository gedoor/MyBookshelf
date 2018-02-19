package com.monke.monkeybook.help;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2018/1/18.
 * 添加删除Book
 */

public class BookShelf {

    public static BookShelfBean getBook(String bookUrl) {
        List<BookShelfBean> bookShelfBeanS = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl)).build().list();
        if (!(bookShelfBeanS == null || bookShelfBeanS.size() == 0)) {
            return bookShelfBeanS.get(0);
        } else {
            return null;
        }
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
}
