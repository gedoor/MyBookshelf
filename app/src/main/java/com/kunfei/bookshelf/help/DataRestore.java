package com.kunfei.bookshelf.help;

import android.content.SharedPreferences;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.model.TxtChapterRuleManager;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.GsonUtils;
import com.kunfei.bookshelf.utils.XmlUtils;

import java.io.File;
import java.io.FileInputStream;
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

    public Boolean run() {
        String dirPath = FileUtils.getSdCardPath() + File.separator + "YueDu";
        restoreBookSource(dirPath);
        restoreBookShelf(dirPath);
        restoreSearchHistory(dirPath);
        restoreReplaceRule(dirPath);
        restoreTxtChapterRule(dirPath);
        restoreConfig(dirPath);
        return true;
    }

    private void restoreBookShelf(String file) {
        try {
            String json = DocumentHelper.readString("myBookShelf.json", file);
            if (json == null) return;
            List<BookShelfBean> bookShelfList = GsonUtils.parseJArray(json, BookShelfBean.class);
            if (bookShelfList == null) return;
            for (BookShelfBean bookshelf : bookShelfList) {
                if (bookshelf.getNoteUrl() != null) {
                    DbHelper.getDaoSession().getBookShelfBeanDao().insertOrReplace(bookshelf);
                }
                if (bookshelf.getBookInfoBean().getNoteUrl() != null) {
                    DbHelper.getDaoSession().getBookInfoBeanDao().insertOrReplace(bookshelf.getBookInfoBean());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreBookSource(String file) {
        try {
            String json = DocumentHelper.readString("myBookSource.json", file);
            if (json == null) return;
            List<BookSourceBean> bookSourceBeans = GsonUtils.parseJArray(json, BookSourceBean.class);
            if (bookSourceBeans == null) return;
            BookSourceManager.addBookSource(bookSourceBeans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreSearchHistory(String file) {
        try {
            String json = DocumentHelper.readString("myBookSearchHistory.json", file);
            if (json == null) return;
            List<SearchHistoryBean> searchHistoryBeans = GsonUtils.parseJArray(json, SearchHistoryBean.class);
            if (searchHistoryBeans == null) return;
            DbHelper.getDaoSession().getSearchHistoryBeanDao().insertOrReplaceInTx(searchHistoryBeans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreReplaceRule(String file) {
        try {
            String json = DocumentHelper.readString("myBookReplaceRule.json", file);
            if (json == null) return;
            List<ReplaceRuleBean> replaceRuleBeans = GsonUtils.parseJArray(json, ReplaceRuleBean.class);
            if (replaceRuleBeans == null) return;
            ReplaceRuleManager.addDataS(replaceRuleBeans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreTxtChapterRule(String file) {
        try {
            String json = DocumentHelper.readString("myTxtChapterRule.json", file);
            if (json == null) return;
            List<TxtChapterRuleBean> ruleBeanList = GsonUtils.parseJArray(json, TxtChapterRuleBean.class);
            if (ruleBeanList == null) return;
            TxtChapterRuleManager.save(ruleBeanList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreConfig(String dirPath) {
        try {
            Map<String, ?> entries = null;
            try (FileInputStream ins = new FileInputStream(dirPath + File.separator + "config.xml")) {
                entries = XmlUtils.readMapXml(ins);
            } catch (Exception ignored) {
            }
            if (entries == null || entries.isEmpty()) return;
            long donateHb = MApplication.getConfigPreferences().getLong("DonateHb", 0);
            donateHb = donateHb > System.currentTimeMillis() ? 0 : donateHb;
            SharedPreferences.Editor editor = MApplication.getConfigPreferences().edit();
            editor.clear();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                String type = v.getClass().getSimpleName();

                switch (type) {
                    case "Integer":
                        editor.putInt(key, (Integer) v);
                        break;
                    case "Boolean":
                        editor.putBoolean(key, (Boolean) v);
                        break;
                    case "String":
                        editor.putString(key, (String) v);
                        break;
                    case "Float":
                        editor.putFloat(key, (Float) v);
                        break;
                    case "Long":
                        editor.putLong(key, (Long) v);
                        break;
                }
            }
            editor.putLong("DonateHb", donateHb);
            editor.putInt("versionCode", MApplication.getVersionCode());
            editor.apply();
            LauncherIcon.ChangeIcon(MApplication.getConfigPreferences().getString("launcher_icon", MApplication.getInstance().getString(R.string.icon_main)));
            ReadBookControl.getInstance().updateReaderSettings();
            MApplication.getInstance().upThemeStore();
            MApplication.getInstance().initNightTheme();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
