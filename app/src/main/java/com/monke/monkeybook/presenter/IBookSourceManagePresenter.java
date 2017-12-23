package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;
import com.monke.monkeybook.bean.BookSourceBean;

import java.util.List;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public interface IBookSourceManagePresenter extends IPresenter {

    public void saveDate(List<BookSourceBean> bookSourceBeans);

}
