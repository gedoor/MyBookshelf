package com.kunfei.bookshelf.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.dao.ReplaceRuleBeanDao;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.StringUtils;

import java.net.URL;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by GKF on 2018/2/12.
 * 替换规则管理
 */

public class ReplaceRuleManager {
    private static List<ReplaceRuleBean> replaceRuleBeansEnabled;
    private static List<ReplaceRuleBean> replaceRuleBeansAll;
    private static long lastUpTime = System.currentTimeMillis();

    public static List<ReplaceRuleBean> getEnabled() {
        if (replaceRuleBeansEnabled == null) {
            replaceRuleBeansEnabled = DbHelper.getInstance().getmDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                    .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                    .list();
        }
        return replaceRuleBeansEnabled;
    }

    public static long getLastUpTime() {
        return lastUpTime;
    }

    public static List<ReplaceRuleBean> getAll() {
        if (replaceRuleBeansAll == null) {
            replaceRuleBeansAll = DbHelper.getInstance().getmDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                    .list();
        }
        return replaceRuleBeansAll;
    }

    public static void saveData(ReplaceRuleBean replaceRuleBean) {
        if (replaceRuleBean.getSerialNumber() == 0) {
            replaceRuleBean.setSerialNumber(replaceRuleBeansAll.size() + 1);
        }
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

    public static void delDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
            DbHelper.getInstance().getmDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        }
        refreshDataS();
    }

    private static void refreshDataS() {
        replaceRuleBeansEnabled = DbHelper.getInstance().getmDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
        replaceRuleBeansAll = DbHelper.getInstance().getmDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
        lastUpTime = System.currentTimeMillis();
    }

    public static Observable<Boolean> importReplaceRule(String text) {
        if (TextUtils.isEmpty(text)) return null;
        text = text.trim();
        if (text.length() == 0) return null;
        if (StringUtils.isJsonType(text)) {
            return importReplaceRuleO(text)
                    .compose(RxUtils::toSimpleSingle);
        } else {
            try {
                URL url = new URL(text);
                return BaseModelImpl.getInstance().getRetrofitString(String.format("%s://%s", url.getProtocol(), url.getHost()), "utf-8")
                        .create(IHttpGetApi.class)
                        .getWebContent(url.getPath(), AnalyzeHeaders.getMap(null))
                        .flatMap(rsp -> importReplaceRuleO(rsp.body()))
                        .compose(RxUtils::toSimpleSingle);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Observable<Boolean> importReplaceRuleO(String json) {
        return Observable.create(e -> {
            try {
                List<ReplaceRuleBean> replaceRuleBeans = new Gson().fromJson(json, new TypeToken<List<ReplaceRuleBean>>() {
                }.getType());
                addDataS(replaceRuleBeans);
                e.onNext(true);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(false);
            }
            e.onComplete();
        });
    }
}
