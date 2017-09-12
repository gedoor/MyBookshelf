//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;
import com.monke.monkeybook.bean.SearchBookBean;

public interface IChoiceBookPresenter extends IPresenter{

    int getPage();

    void initPage();

    void toSearchBooks(String key);

    void addBookToShelf(final SearchBookBean searchBookBean);

    String getTitle();
}