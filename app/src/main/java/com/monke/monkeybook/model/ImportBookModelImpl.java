//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.impl.IImportBookModel;

import java.io.File;

import io.reactivex.Observable;

public class ImportBookModelImpl extends BaseModelImpl implements IImportBookModel {

    public static ImportBookModelImpl getInstance() {
        return new ImportBookModelImpl();
    }

    @Override
    public Observable<LocBookShelfBean> importBook(final File file) {
        return Observable.create(e -> {
            //判断文件是否存在

            boolean isNew = false;

            BookShelfBean bookShelfBean;
            bookShelfBean = BookshelfHelp.getBook(file.getAbsolutePath());
            if (bookShelfBean == null) {
                isNew = true;
                bookShelfBean = new BookShelfBean();
                bookShelfBean.setHasUpdate(true);
                bookShelfBean.setFinalDate(file.lastModified());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                bookShelfBean.setTag(BookShelfBean.LOCAL_TAG);
                bookShelfBean.setNoteUrl(file.getAbsolutePath());

                bookShelfBean.getBookInfoBean().setAuthor("");
                bookShelfBean.getBookInfoBean().setName(file.getName().replace(".txt", "").replace(".TXT", ""));
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
                bookShelfBean.getBookInfoBean().setCoverUrl("");
                bookShelfBean.getBookInfoBean().setNoteUrl(file.getAbsolutePath());
                bookShelfBean.getBookInfoBean().setTag(BookShelfBean.LOCAL_TAG);

                DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
            }
            e.onNext(new LocBookShelfBean(isNew, bookShelfBean));
            e.onComplete();
        });
    }

}
