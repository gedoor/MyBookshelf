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
import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.StringUtils;

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
            replaceRuleBeansEnabled = DbHelper.getDaoSession()
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
            replaceRuleBeansAll = DbHelper.getDaoSession()
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
        DbHelper.getDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
        refreshDataS();
    }

    public static void delData(ReplaceRuleBean replaceRuleBean) {
        DbHelper.getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        refreshDataS();
    }

    public static void addDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            DbHelper.getDaoSession().getReplaceRuleBeanDao().insertOrReplaceInTx(replaceRuleBeans);
            refreshDataS();
        }
    }

    public static void delDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
            DbHelper.getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        }
        refreshDataS();
    }

    private static void refreshDataS() {
        replaceRuleBeansEnabled = DbHelper.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
        replaceRuleBeansAll = DbHelper.getDaoSession()
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
        }
        if (NetworkUtil.isUrl(text)) {
            return BaseModelImpl.getInstance().getRetrofitString(StringUtils.getBaseUrl(text), "utf-8")
                    .create(IHttpGetApi.class)
                    .getWebContent(text, AnalyzeHeaders.getMap(null))
                    .flatMap(rsp -> importReplaceRuleO(rsp.body()))
                    .compose(RxUtils::toSimpleSingle);
        }
        return Observable.error(new Exception("不是Json或Url格式"));
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
