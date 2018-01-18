package com.monke.monkeybook.help;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2018/1/18.
 */

public class BookShelf {

    public static void removeFromBookShelf(BookShelfBean bookShelf) {
        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().deleteByKey(bookShelf.getNoteUrl());
        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().deleteByKey(bookShelf.getBookInfoBean().getNoteUrl());
        List<String> keys = new ArrayList<String>();
        if(bookShelf.getBookInfoBean().getChapterList().size()>0){
            for(int i = 0; i<bookShelf.getBookInfoBean().getChapterList().size(); i++){
                keys.add(bookShelf.getBookInfoBean().getChapterList(i).getDurChapterUrl());
            }
        }
        DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().deleteByKeyInTx(keys);
        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteInTx(bookShelf.getBookInfoBean().getChapterList());
    }
}
