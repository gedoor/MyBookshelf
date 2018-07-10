//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter.impl;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;

public interface ISearchBookPresenter extends IPresenter{

    Boolean getHasSearch();

    void setHasSearch(Boolean hasSearch);

    void insertSearchHistory();

    void querySearchHistory(String content);

    void cleanSearchHistory();

    void cleanSearchHistory(SearchHistoryBean searchHistoryBean);

    int getPage();

    void initPage();

    void toSearchBooks(String key,Boolean fromError);

    void addBookToShelf(final SearchBookBean searchBookBean);

    void upSearchEngineS();
}
