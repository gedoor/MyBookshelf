package com.monke.monkeybook.help;

import android.content.SharedPreferences;
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
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.SharedPreferencesUtil;
import com.monke.monkeybook.utils.XmlUtils;

import java.io.FileOutputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * Created by GKF on 2018/1/30.
 * 数据备份
 */

public class DataBackup {

    public static DataBackup getInstance() {
        return new DataBackup();
    }


    public void autoSave() {
        long currentTime = System.currentTimeMillis();
        if (EasyPermissions.hasPermissions(MApplication.getInstance(), MApplication.PerList)) {
            long lastBackupTime = (long) SharedPreferencesUtil.getData(MApplication.getInstance(), "backupTime", 0L);
            if (currentTime - lastBackupTime > 24 * 3600 * 1000) {
                DocumentHelper.createDirIfNotExist(FileUtil.getSdCardPath(), "YueDu");
                String dirPath = FileUtil.getSdCardPath() + "/YueDu";
                DocumentHelper.createDirIfNotExist(dirPath, "autoSave");
                dirPath += "/autoSave";
                backupBookShelf(dirPath);
                backupBookSource(dirPath);
                backupSearchHistory(dirPath);
                backupReplaceRule(dirPath);
                backupConfig(dirPath);
                SharedPreferencesUtil.saveData("backupTime", currentTime);
            }
        }
    }

    public void run() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DocumentHelper.createDirIfNotExist(FileUtil.getSdCardPath(), "YueDu");
            String dirPath = FileUtil.getSdCardPath() + "/YueDu";
            backupBookShelf(dirPath);
            backupBookSource(dirPath);
            backupSearchHistory(dirPath);
            backupReplaceRule(dirPath);
            backupConfig(dirPath);

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

    private void backupBookShelf(String file) {
        List<BookShelfBean> bookShelfList = BookshelfHelp.getAllBook();
        if (bookShelfList != null && bookShelfList.size() > 0) {
            for (BookShelfBean bookshelf : bookShelfList) {
                bookshelf.getBookInfoBean().setChapterList(null);
            }
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String bookshelf = gson.toJson(bookShelfList);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookShelf.json", file);
            DocumentHelper.writeString(bookshelf, docFile);
        }
        BookshelfHelp.getAllBook();
    }

    private void backupBookSource(String file) {
        List<BookSourceBean> bookSourceList = BookSourceManage.getAllBookSource();
        if (bookSourceList != null && bookSourceList.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(bookSourceList);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSource.json", file);
            DocumentHelper.writeString(str, docFile);
        }
    }

    private void backupSearchHistory(String file) {
        List<SearchHistoryBean> searchHistoryBeans = DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao()
                .queryBuilder().list();
        if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(searchHistoryBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSearchHistory.json", file);
            DocumentHelper.writeString(str, docFile);
        }
    }

    private void backupReplaceRule(String file) {
        List<ReplaceRuleBean> replaceRuleBeans = ReplaceRuleManage.getAll();
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(replaceRuleBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookReplaceRule.json", file);
            DocumentHelper.writeString(str, docFile);
        }
    }

    private void backupConfig(String file) {
        SharedPreferences pref = MApplication.getInstance().getConfigPreferences();
        /*
        DocumentFile docFile = DocumentHelper.createFileIfNotExist("config.json", file);
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(pref.getAll());
        DocumentHelper.writeString(json, docFile);
        */
        try (FileOutputStream out = new FileOutputStream(file + "/config.xml")) {
            XmlUtils.writeMapXml(pref.getAll(), out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
