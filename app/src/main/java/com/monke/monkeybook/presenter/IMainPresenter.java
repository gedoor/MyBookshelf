package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;

public interface IMainPresenter extends IPresenter{
    public void queryBookShelf(Boolean needRefresh);
}
