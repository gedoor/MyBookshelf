package com.kunfei.bookshelf.model;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.dao.TxtChapterRuleBeanDao;

import java.util.ArrayList;
import java.util.List;

public class TxtChapterRuleManager {

    public static List<TxtChapterRuleBean> getAll() {
        List<TxtChapterRuleBean> beans = DbHelper.getDaoSession().getTxtChapterRuleBeanDao().loadAll();
        if (beans.isEmpty()) {
            beans.add(getDefault());
        }
        return beans;
    }

    public static List<TxtChapterRuleBean> getEnabled() {
        List<TxtChapterRuleBean> beans = DbHelper.getDaoSession().getTxtChapterRuleBeanDao().queryBuilder()
                .where(TxtChapterRuleBeanDao.Properties.Enable.eq(true))
                .list();
        if (beans.isEmpty()) {
            beans.add(getDefault());
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

    public static TxtChapterRuleBean getDefault() {
        TxtChapterRuleBean txtChapterRuleBean = new TxtChapterRuleBean();
        txtChapterRuleBean.setSerialNumber(0);
        txtChapterRuleBean.setName("默认正则");
        txtChapterRuleBean.setRule("^(.{0,8})(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节卷集部篇回场])(.{0,30})$");
        save(txtChapterRuleBean);
        return txtChapterRuleBean;
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
