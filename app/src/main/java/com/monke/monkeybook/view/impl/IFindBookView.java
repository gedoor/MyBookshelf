//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.impl;

import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;

import java.util.LinkedHashMap;
import java.util.List;

public interface IFindBookView extends IView{

    /**
     * 更新UI
     */
    void updateUI(List<FindKindGroupBean> group);

}
