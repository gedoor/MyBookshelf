//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter.impl;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.monke.basemvplib.impl.IPresenter;

public interface IMainPresenter extends IPresenter{
    void queryBookShelf(Boolean needRefresh, int group);

    void backupData();

    void restoreData();

    void downloadAll();

    void addBookUrl(String bookUrl);

    void bookshelfSync(GoogleSignInAccount account);

    void clearBookshelf();
}
