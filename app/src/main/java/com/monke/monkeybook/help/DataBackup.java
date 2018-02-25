package com.monke.monkeybook.help;

import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.model.ReplaceRuleManage;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/30.
 * 数据备份
 */

public class DataBackup {

    public static DataBackup getInstance() {
        return new DataBackup();
    }

    public void run() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            FileHelper.createDirIfNotExist(Environment.getExternalStorageDirectory().getPath(), "Documents");
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (file == null) {
                e.onNext(false);
            }
            backupBookShelf(file);
            backupBookSource(file);
            backupSearchHistory(file);
            backupReplaceRule(file);

            e.onNext(true);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            Toast.makeText(MApplication.getInstance(), R.string.backup_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MApplication.getInstance(), R.string.backup_fail, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(MApplication.getInstance(), R.string.backup_fail, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void backupBookShelf(File file) {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        if (bookShelfList != null && bookShelfList.size() > 0) {
            for (BookShelfBean bookshelf : bookShelfList) {
                bookshelf.getBookInfoBean().setChapterList(null);
            }
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String bookshelf = gson.toJson(bookShelfList);
            DocumentFile docFile = FileHelper.createFileIfNotExist("myBookShelf.xml", file.getPath());
            FileHelper.writeString(bookshelf, docFile);
        }
    }

    private void backupBookSource(File file) {
        List<BookSourceBean> bookSourceList = BookSourceManage.getAllBookSource();
        if (bookSourceList != null && bookSourceList.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(bookSourceList);
            DocumentFile docFile = FileHelper.createFileIfNotExist("myBookSource.xml", file.getPath());
            FileHelper.writeString(str, docFile);
        }
    }

    private void backupSearchHistory(File file) {
        List<SearchHistoryBean> searchHistoryBeans = DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao()
                .queryBuilder().list();
        if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(searchHistoryBeans);
            DocumentFile docFile = FileHelper.createFileIfNotExist("myBookSearchHistory.xml", file.getPath());
            FileHelper.writeString(str, docFile);
        }
    }

    private void backupReplaceRule(File file) {
        List<ReplaceRuleBean> replaceRuleBeans = ReplaceRuleManage.getAll();
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(replaceRuleBeans);
            DocumentFile docFile = FileHelper.createFileIfNotExist("myBookReplaceRule.xml", file.getPath());
            FileHelper.writeString(str, docFile);
        }
    }

}
