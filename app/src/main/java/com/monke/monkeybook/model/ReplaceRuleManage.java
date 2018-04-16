package com.monke.monkeybook.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.ReplaceRuleBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.content.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import java.net.URL;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/2/12.
 * 替换规则管理
 */

public class ReplaceRuleManage extends BaseModelImpl {
    private static List<ReplaceRuleBean> replaceRuleBeansEnabled;
    private static List<ReplaceRuleBean> replaceRuleBeansAll;

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
        BookshelfHelp.clearLineContent();
    }

    public static Observable<Boolean> importReplaceRuleFromWww(URL url) {
        return getRetrofitString(String.format("%s://%s", url.getProtocol(), url.getHost()))
                .create(IHttpGetApi.class)
                .getWebContent(url.getPath(), AnalyzeHeaders.getMap(null))
                .flatMap(rsp -> importReplaceRuleO(rsp.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
