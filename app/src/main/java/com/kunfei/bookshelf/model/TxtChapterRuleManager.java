package com.kunfei.bookshelf.model;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.dao.TxtChapterRuleBeanDao;
import com.kunfei.bookshelf.utils.GsonUtils;
import com.kunfei.bookshelf.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TxtChapterRuleManager {

    public static List<TxtChapterRuleBean> getAll() {
        List<TxtChapterRuleBean> beans = DbHelper.getDaoSession().getTxtChapterRuleBeanDao().loadAll();
        if (beans.isEmpty()) {
            return getDefault();
        }
        return beans;
    }

    public static List<TxtChapterRuleBean> getEnabled() {
        List<TxtChapterRuleBean> beans = DbHelper.getDaoSession().getTxtChapterRuleBeanDao().queryBuilder()
                .where(TxtChapterRuleBeanDao.Properties.Enable.eq(true))
                .list();
        if (beans.isEmpty()) {
            return getAll();
        }
        return beans;
    }

    public static List<String> enabledRuleList() {
        List<TxtChapterRuleBean> beans = getEnabled();
        List<String> ruleList = new ArrayList<>();
        for (TxtChapterRuleBean chapterRuleBean : beans) {
            ruleList.add(chapterRuleBean.getRule());
        }
        return ruleList;
    }

    public static List<TxtChapterRuleBean> getDefault() {
        String json = null;
        try {
            InputStream inputStream = MApplication.getInstance().getAssets().open("txtChapterRule.json");
            json = IOUtils.toString(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<TxtChapterRuleBean> ruleBeanList = GsonUtils.parseJArray(json, TxtChapterRuleBean.class);
        if (ruleBeanList != null) {
            DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplaceInTx(ruleBeanList);
            return ruleBeanList;
        }
        return new ArrayList<>();
    }

    public static void del(TxtChapterRuleBean txtChapterRuleBean) {
        DbHelper.getDaoSession().getTxtChapterRuleBeanDao().delete(txtChapterRuleBean);
    }

    public static void del(List<TxtChapterRuleBean> ruleBeanList) {
        for (TxtChapterRuleBean ruleBean : ruleBeanList) {
            del(ruleBean);
        }
    }

    public static void save(TxtChapterRuleBean txtChapterRuleBean) {
        if (txtChapterRuleBean.getSerialNumber() == null) {
            txtChapterRuleBean.setSerialNumber((int) DbHelper.getDaoSession().getTxtChapterRuleBeanDao().queryBuilder().count());
        }
        DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplace(txtChapterRuleBean);
    }

    public static void save(List<TxtChapterRuleBean> txtChapterRuleBeans) {
        DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplaceInTx(txtChapterRuleBeans);
    }
}
