//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;

public interface IMainPresenter extends IPresenter{
    void queryBookShelf(Boolean needRefresh);

    void backupBookShelf();

    void restoreBookShelf();
}
