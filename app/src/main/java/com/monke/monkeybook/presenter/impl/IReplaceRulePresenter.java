package com.monke.monkeybook.presenter.impl;

import android.net.Uri;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;

import java.util.List;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public interface IReplaceRulePresenter extends IPresenter {

    void saveData(List<ReplaceRuleBean> replaceRuleBeans);

    void delData(ReplaceRuleBean replaceRuleBean);

    void delData(List<ReplaceRuleBean> replaceRuleBeans);

    void importDataS(Uri uri);

    void importDataS(String url);
}
