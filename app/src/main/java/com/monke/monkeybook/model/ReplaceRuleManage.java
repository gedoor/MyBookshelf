package com.monke.monkeybook.model;

import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.ReplaceRuleBeanDao;

import java.util.List;

/**
 * Created by GKF on 2018/2/12.
 * 替换规则管理
 */

public class ReplaceRuleManage {
    private static List<ReplaceRuleBean> replaceRuleBeansEnabled;
    private static List<ReplaceRuleBean> replaceRuleBeansAll;

    public static List<ReplaceRuleBean> getEnabled() {
        if (replaceRuleBeansEnabled == null) {
            replaceRuleBeansEnabled = DbHelper.getInstance().getmDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                    .list();
        }
        return replaceRuleBeansEnabled;
    }

    public static List<ReplaceRuleBean> getAll() {
        if (replaceRuleBeansAll == null) {
            replaceRuleBeansAll = DbHelper.getInstance().getmDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .list();
        }
        return replaceRuleBeansAll;
    }

    public static void saveData(ReplaceRuleBean replaceRuleBean) {
        DbHelper.getInstance().getmDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
        refreshDataS();
    }

    public static void delData(ReplaceRuleBean replaceRuleBean) {
        DbHelper.getInstance().getmDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        refreshDataS();
    }

    public static void addDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            DbHelper.getInstance().getmDaoSession().getReplaceRuleBeanDao().insertOrReplaceInTx(replaceRuleBeans);
            refreshDataS();
        }
    }

    private static void refreshDataS() {
        replaceRuleBeansEnabled = DbHelper.getInstance().getmDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .list();
        replaceRuleBeansAll = DbHelper.getInstance().getmDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .list();
    }
}
