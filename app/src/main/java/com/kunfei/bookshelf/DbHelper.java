//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.kunfei.bookshelf.dao.BookChapterBeanDao;
import com.kunfei.bookshelf.dao.BookInfoBeanDao;
import com.kunfei.bookshelf.dao.BookShelfBeanDao;
import com.kunfei.bookshelf.dao.BookSourceBeanDao;
import com.kunfei.bookshelf.dao.BookmarkBeanDao;
import com.kunfei.bookshelf.dao.CookieBeanDao;
import com.kunfei.bookshelf.dao.DaoMaster;
import com.kunfei.bookshelf.dao.DaoSession;
import com.kunfei.bookshelf.dao.ReplaceRuleBeanDao;
import com.kunfei.bookshelf.dao.SearchHistoryBeanDao;
import com.kunfei.bookshelf.dao.TxtChapterRuleBeanDao;

import org.greenrobot.greendao.database.Database;

import java.util.Locale;

public class DbHelper {
    private static DbHelper instance;
    private SQLiteDatabase db;
    private DaoSession mDaoSession;

    private DbHelper() {
        DaoOpenHelper mHelper = new DaoOpenHelper(MApplication.getInstance(), "monkebook_db", null);
        db = mHelper.getWritableDatabase();
        db.setLocale(Locale.CHINESE);
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public static DbHelper getInstance() {
        if (null == instance) {
            synchronized (DbHelper.class) {
                if (null == instance) {
                    instance = new DbHelper();
                }
            }
        }
        return instance;
    }

    public static DaoSession getDaoSession() {
        return getInstance().mDaoSession;
    }

    public static SQLiteDatabase getDb() {
        return getInstance().db;
    }

    public class DaoOpenHelper extends DaoMaster.OpenHelper {
        DaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            MigrationHelper.migrate(db,
                    new MigrationHelper.ReCreateAllTableListener() {
                        @Override
                        public void onCreateAllTables(Database db, boolean ifNotExists) {
                            DaoMaster.createAllTables(db, ifNotExists);
                        }

                        @Override
                        public void onDropAllTables(Database db, boolean ifExists) {
                            DaoMaster.dropAllTables(db, ifExists);
                        }
                    },
                    BookShelfBeanDao.class, BookInfoBeanDao.class, BookChapterBeanDao.class,
                    SearchHistoryBeanDao.class, BookSourceBeanDao.class,
                    ReplaceRuleBeanDao.class, BookmarkBeanDao.class, CookieBeanDao.class,
                    TxtChapterRuleBeanDao.class
            );
        }
    }

}
