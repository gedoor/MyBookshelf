package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;
import com.monke.monkeybook.bean.SearchBookBean;

public interface IChoiceBookPresenter extends IPresenter{

    public int getPage();

    public void initPage();

    public void toSearchBooks(String key);

    public void addBookToShelf(final SearchBookBean searchBookBean);

    public String getTitle();
}