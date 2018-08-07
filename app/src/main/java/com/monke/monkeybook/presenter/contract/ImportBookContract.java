package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;

import java.io.File;
import java.util.List;

public interface ImportBookContract {

    interface Presenter extends IPresenter {
        void searchLocationBook(File file);

        void importBooks(List<File> books);

    }

    interface View extends IView {

        /**
         * @param newFile 新增书籍
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
        void addError(String msg);
    }
}
