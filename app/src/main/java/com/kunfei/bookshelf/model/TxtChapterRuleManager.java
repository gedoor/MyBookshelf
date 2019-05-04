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
        List<TxtChapterRuleBean> ruleBeanList = new ArrayList<>();
        TxtChapterRuleBean ruleBean1 = new TxtChapterRuleBean();
        ruleBean1.setSerialNumber(0);
        ruleBean1.setName("默认正则1");
        ruleBean1.setRule("^(.{0,8})(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节卷集部篇回场])(.{0,30})$");
        ruleBeanList.add(ruleBean1);
        TxtChapterRuleBean ruleBean2 = new TxtChapterRuleBean();
        ruleBean2.setSerialNumber(1);
        ruleBean2.setName("默认正则2");
        ruleBean2.setRule("^([0-9]{1,5})([\\,\\.，-])(.{1,20})$");
        ruleBeanList.add(ruleBean2);
        TxtChapterRuleBean ruleBean3 = new TxtChapterRuleBean();
        ruleBean3.setSerialNumber(2);
        ruleBean3.setName("默认正则3");
        ruleBean3.setRule("^(\\s{0,4})([\\(【《]?(卷)?)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([\\.:： \f\t])(.{0,30})$");
        ruleBeanList.add(ruleBean3);
        TxtChapterRuleBean ruleBean4 = new TxtChapterRuleBean();
        ruleBean4.setSerialNumber(3);
        ruleBean4.setName("默认正则4");
        ruleBean4.setRule("^(\\s{0,4})([\\(（【《])(.{0,30})([\\)）】》])(\\s{0,2})$");
        ruleBeanList.add(ruleBean4);
        TxtChapterRuleBean ruleBean5 = new TxtChapterRuleBean();
        ruleBean5.setSerialNumber(4);
        ruleBean5.setName("默认正则5");
        ruleBean5.setRule("^(\\s{0,4})(正文)(.{0,20})$");
        ruleBeanList.add(ruleBean5);
        TxtChapterRuleBean ruleBean6 = new TxtChapterRuleBean();
        ruleBean6.setSerialNumber(5);
        ruleBean6.setName("默认正则6");
        ruleBean6.setRule("^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$");
        ruleBeanList.add(ruleBean6);
        DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplaceInTx(ruleBeanList);
        return ruleBeanList;
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
