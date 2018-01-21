//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.impl;

import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.LibraryBean;

public interface ILibraryView extends IView{

    /**
     * 书城书籍获取成功  更新UI
     * @param library 书城
     */
    void updateUI(LibraryBean library);

    /**
     * 书城数据刷新成功 更新UI
     */
    void finishRefresh();
}
