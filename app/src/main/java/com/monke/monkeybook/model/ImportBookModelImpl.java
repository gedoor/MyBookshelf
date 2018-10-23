//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.impl.IImportBookModel;

import java.io.File;

import io.reactivex.Observable;

import static com.monke.monkeybook.utils.StringUtils.getString;

public class ImportBookModelImpl extends BaseModelImpl implements IImportBookModel {

    public static ImportBookModelImpl getInstance() {
        return new ImportBookModelImpl();
    }

    @Override
    public Observable<LocBookShelfBean> importBook(final File file) {
        return Observable.create(e -> {
            //判断文件是否存在

            boolean isNew = false;

            BookShelfBean bookShelfBean = BookshelfHelp.getBook(file.getAbsolutePath());
            if (bookShelfBean == null) {
                isNew = true;
                bookShelfBean = new BookShelfBean();
                bookShelfBean.setHasUpdate(true);
                bookShelfBean.setFinalDate(System.currentTimeMillis());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                bookShelfBean.setTag(BookShelfBean.LOCAL_TAG);
                bookShelfBean.setNoteUrl(file.getAbsolutePath());
                bookShelfBean.setAllowUpdate(false);

                BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
                String fileName = file.getName().replace(".txt", "").replace(".TXT", "");
                int authorIndex = fileName.indexOf("作者");
                if (authorIndex != -1) {
                    bookInfoBean.setAuthor(FormatWebText.getAuthor(fileName.substring(authorIndex)));
                    bookInfoBean.setName(fileName.substring(0, authorIndex));
                } else {
                    bookInfoBean.setAuthor("");
                    bookInfoBean.setName(fileName);
                }
                bookInfoBean.setFinalRefreshData(file.lastModified());
                bookInfoBean.setCoverUrl("");
                bookInfoBean.setNoteUrl(file.getAbsolutePath());
                bookInfoBean.setTag(BookShelfBean.LOCAL_TAG);
                bookInfoBean.setOrigin(getString(R.string.local));

                DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookInfoBean);
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
            }
            e.onNext(new LocBookShelfBean(isNew, bookShelfBean));
            e.onComplete();
        });
    }

}
