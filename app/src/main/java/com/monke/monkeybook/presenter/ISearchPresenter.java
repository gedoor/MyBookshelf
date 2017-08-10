package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;
import com.monke.monkeybook.bean.SearchBookBean;

public interface ISearchPresenter extends IPresenter{

    public Boolean getHasSearch();

    public void setHasSearch(Boolean hasSearch);

    public void insertSearchHistory();

    public void querySearchHistory();

    public void cleanSearchHistory();

    public int getPage();

    public void initPage();

    public void toSearchBooks(String key);

    public void addBookToShelf(final SearchBookBean searchBookBean);

    public Boolean getInput();

    public void setInput(Boolean input);
}
