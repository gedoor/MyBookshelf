package com.kunfei.bookshelf.presenter.contract;

import android.content.Intent;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.SearchBookBean;

public interface BookDetailContract {
    interface Presenter extends IPresenter {
        void initData(Intent intent);

        int getOpenFrom();

        SearchBookBean getSearchBook();

        BookShelfBean getBookShelf();

        Boolean getInBookShelf();

        void initBookFormSearch(SearchBookBean searchBookBean);

        void getBookShelfInfo();

        void addToBookShelf();

        void removeFromBookShelf();

        void changeBookSource(SearchBookBean searchBookBean);
    }

    interface View extends IView {
        /**
         * 更新书籍详情UI
         */
        void updateView();

        /**
         * 数据获取失败
         */
        void getBookShelfError();

        void finish();

        void toast(String msg);
    }
}
