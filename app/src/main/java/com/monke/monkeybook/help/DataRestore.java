package com.monke.monkeybook.help;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.utils.SharedPreferencesUtil;
import com.monke.monkeybook.utils.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by GKF on 2018/1/30.
 * 数据恢复
 */

public class DataRestore {

    public static DataRestore getInstance() {
        return new DataRestore();
    }

    public Boolean run() throws Exception {
        String dirPath = FileUtil.getSdCardPath() + "/YueDu";
        restoreConfig(dirPath);
        restoreBookSource(dirPath);
        restoreBookShelf(dirPath);
        restoreSearchHistory(dirPath);
        restoreReplaceRule(dirPath);
        return true;
    }

    private void restoreConfig(String dirPath) {
        String json = FileHelper.readString("config.json", dirPath);
        if (json != null) {
            try {
                Map<String, ?> entries = new Gson().fromJson(json, new TypeToken<Map<String, ?>>() {
                }.getType());
                for (Map.Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();
                    SharedPreferencesUtil.saveData(MApplication.getInstance(), key, v);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreBookShelf(String file) throws Exception {
        String json = FileHelper.readString("myBookShelf.json", file);
        if (json != null) {
            List<BookShelfBean> bookShelfList = new Gson().fromJson(json, new TypeToken<List<BookShelfBean>>() {
            }.getType());
            for (BookShelfBean bookshelf : bookShelfList) {
                if (bookshelf.getNoteUrl() != null) {
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookshelf);
                }
                if (bookshelf.getBookInfoBean().getNoteUrl() != null) {
                    DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookshelf.getBookInfoBean());
                }
            }
        }
    }

    private void restoreBookSource(String file) throws Exception {
        String json = FileHelper.readString("myBookSource.json", file);
        if (json != null) {
            List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
            }.getType());
            for (int i = 0; i < bookSourceBeans.size(); i++) {
                bookSourceBeans.get(i).setSerialNumber(i + 1);
            }
            BookSourceManage.addBookSource(bookSourceBeans);
        }
    }

    private void restoreSearchHistory(String file) throws Exception {
        String json = FileHelper.readString("myBookSearchHistory.json", file);
        if (json != null) {
            List<SearchHistoryBean> searchHistoryBeans = new Gson().fromJson(json, new TypeToken<List<SearchHistoryBean>>() {
            }.getType());
            if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
                DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao().insertOrReplaceInTx(searchHistoryBeans);
            }
        }
    }

    private void restoreReplaceRule(String file) throws Exception {
        String json = FileHelper.readString("myBookReplaceRule.json", file);
        if (json != null) {
            List<ReplaceRuleBean> replaceRuleBeans = new Gson().fromJson(json, new TypeToken<List<ReplaceRuleBean>>() {
            }.getType());
            ReplaceRuleManage.addDataS(replaceRuleBeans);
        }
    }
}
