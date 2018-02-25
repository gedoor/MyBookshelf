package com.monke.monkeybook.view.impl;

import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookSourceBean;

/**
 * Created by GKF on 2018/1/28.
 * 编辑书源
 */

public interface ISourceEditView extends IView {

    void setText(BookSourceBean bookSourceBean);

    String getBookSourceStr();

    void saveSuccess();
}
