package com.monke.monkeybook.presenter.contract;

import android.content.SharedPreferences;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.List;

public interface BookListContract {

    interface View extends IView {

        /**
         * 刷新书架书籍小说信息 更新UI
         *
         * @param bookShelfBeanList 书架
         */
        void refreshBookShelf(List<BookShelfBean> bookShelfBeanList);

        void refreshBook(String noteUrl);

        /**
         * 刷新错误
         *
         * @param error 错误
         */
        void refreshError(String error);

        SharedPreferences getPreferences();

        void recreate();

        /**
         * 更新Group
         */
        void updateGroup(Integer group);

        void toast(String msg);
    }

    interface Presenter extends IPresenter {
        void queryBookShelf(Boolean needRefresh, int group);
    }

}
