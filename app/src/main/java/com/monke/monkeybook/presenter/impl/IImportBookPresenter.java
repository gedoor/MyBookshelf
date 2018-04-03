//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter.impl;

import com.monke.basemvplib.impl.IPresenter;

import java.io.File;
import java.util.List;

public interface IImportBookPresenter extends IPresenter{
    void searchLocationBook(File file);

    void importBooks(List<File> books);

}
