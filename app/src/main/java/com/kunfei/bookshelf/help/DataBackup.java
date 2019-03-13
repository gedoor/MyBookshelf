package com.kunfei.bookshelf.help;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kunfei.bookshelf.BuildConfig;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.TimeUtils;
import com.kunfei.bookshelf.utils.XmlUtils;
import com.kunfei.bookshelf.utils.ZipUtils;
import com.kunfei.bookshelf.utils.web_dav.WebDavFile;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.documentfile.provider.DocumentFile;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by GKF on 2018/1/30.
 * 数据备份
 */

public class DataBackup {

    public static DataBackup getInstance() {
        return new DataBackup();
    }

    public void autoSave() {
        Single.create((SingleOnSubscribe<Boolean>) e -> {
            long currentTime = System.currentTimeMillis();
            List<String> per = PermissionUtils.checkMorePermissions(MApplication.getInstance(), MApplication.PerList);
            if (per.isEmpty() && !BuildConfig.DEBUG) {
                File file = new File(FileUtils.getSdCardPath() + File.separator + "YueDu" + File.separator + "autoSave" + File.separator + "myBookShelf.json");
                if (file.exists()) {
                    if (currentTime - file.lastModified() < TimeUnit.DAYS.toMillis(1)) {
                        e.onSuccess(false);
                        return;
                    }
                }
                DocumentHelper.createDirIfNotExist(FileUtils.getSdCardPath(), "YueDu");
                String dirPath = FileUtils.getSdCardPath() + "/YueDu";
                DocumentHelper.createDirIfNotExist(dirPath, "autoSave");
                dirPath += "/autoSave";
                backupBookShelf(dirPath);
                backupBookSource(dirPath);
                backupSearchHistory(dirPath);
                backupReplaceRule(dirPath);
                backupConfig(dirPath);
                upload(dirPath);
                e.onSuccess(true);
            }
            e.onSuccess(false);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe();
    }

    public void run() {
        Single.create((SingleOnSubscribe<Boolean>) e -> {
            DocumentHelper.createDirIfNotExist(FileUtils.getSdCardPath(), "YueDu");
            String dirPath = FileUtils.getSdCardPath() + "/YueDu";
            backupBookShelf(dirPath);
            backupBookSource(dirPath);
            backupSearchHistory(dirPath);
            backupReplaceRule(dirPath);
            backupConfig(dirPath);
            upload(dirPath);
            e.onSuccess(true);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Boolean value) {
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

    private void upload(String dirPath) {
        List<String> filePaths = new ArrayList<>();
        filePaths.add(dirPath + "/myBookShelf.json");
        filePaths.add(dirPath + "/myBookSource.json");
        filePaths.add(dirPath + "/myBookSearchHistory.json");
        filePaths.add(dirPath + "/myBookReplaceRule.json");
        filePaths.add(dirPath + "/config.xml");
        String zipFilePath = FileHelp.getCachePath() + "/backup" + ".zip";
        try {
            FileHelp.deleteFile(zipFilePath);
            if (ZipUtils.zipFiles(filePaths, zipFilePath)) {
                if (WebDavHelp.initWebDav()) {
                    new  WebDavFile(WebDavHelp.getWebDavUrl() + "YueDu").makeAsDir();
                    String putUrl = WebDavHelp.getWebDavUrl() + "YueDu/backup" + TimeUtils.date2String(TimeUtils.getNowDate(), new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())) + ".zip";
                    WebDavFile webDavFile = new WebDavFile(putUrl);
                    webDavFile.upload(zipFilePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper())
                    .post(() -> Toast
                            .makeText(MApplication.getInstance(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                            .show());
        }
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
            if (docFile != null) {
                DocumentHelper.writeString(bookshelf, docFile);
            }
        }
        BookshelfHelp.getAllBook();
    }

    private void backupBookSource(String file) {
        List<BookSourceBean> bookSourceList = BookSourceManager.getAllBookSource();
        if (bookSourceList != null && bookSourceList.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(bookSourceList);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSource.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(str, docFile);
            }
        }
    }

    private void backupSearchHistory(String file) {
        List<SearchHistoryBean> searchHistoryBeans = DbHelper.getDaoSession().getSearchHistoryBeanDao()
                .queryBuilder().list();
        if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(searchHistoryBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSearchHistory.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(str, docFile);
            }
        }
    }

    private void backupReplaceRule(String file) {
        List<ReplaceRuleBean> replaceRuleBeans = ReplaceRuleManager.getAll();
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(replaceRuleBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookReplaceRule.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(str, docFile);
            }
        }
    }

    private void backupConfig(String file) {
        SharedPreferences pref = MApplication.getConfigPreferences();
        try (FileOutputStream out = new FileOutputStream(file + "/config.xml")) {
            XmlUtils.writeMapXml(pref.getAll(), out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
