//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view;

import com.monke.basemvplib.IView;
import java.io.File;

public interface IImportBookView extends IView{

    /**
     * 新增书籍
     * @param newFile
     */
    void addNewBook(File newFile);

    /**
     * 书籍搜索完成
     */
    void searchFinish();

    /**
     * 添加成功
     */
    void addSuccess();

    /**
     * 添加失败
     */
    void addError();
}