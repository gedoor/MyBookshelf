//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter.impl;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;

public interface IBookDetailPresenter extends IPresenter{

    int getOpenFrom();

    SearchBookBean getSearchBook();

    BookShelfBean getBookShelf();

    Boolean getInBookShelf();

    void initBookFormSearch(SearchBookBean searchBookBean);

    void getBookShelfInfo();

    void addToBookShelf();

    void removeFromBookShelf();

    void changeBookSource(SearchBookBean searchBookBean);
}
