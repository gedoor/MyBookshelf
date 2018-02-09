//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.LocBookShelfBean;

import java.io.File;

import io.reactivex.Observable;

public interface IImportBookModel {

    Observable<LocBookShelfBean> importBook(File book);
}
