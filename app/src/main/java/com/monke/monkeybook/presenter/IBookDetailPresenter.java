package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;

public interface IBookDetailPresenter extends IPresenter{

    public int getOpenfrom();

    public SearchBookBean getSearchBook();

    public BookShelfBean getBookShelf();

    public Boolean getInBookShelf();

    public void getBookShelfInfo();

    public void addToBookShelf();

    public void removeFromBookShelf();
}
