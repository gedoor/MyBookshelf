package com.kunfei.bookshelf.presenter.contract;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.view.adapter.ChoiceBookAdapter;

import java.util.List;

public interface ChoiceBookContract {
    interface Presenter extends IPresenter {

        int getPage();

        void initPage();

        void toSearchBooks(String key);

        void addBookToShelf(final SearchBookBean searchBookBean);

        String getTitle();
    }

    interface View extends IView {

        void refreshSearchBook(List<SearchBookBean> books);

        void loadMoreSearchBook(List<SearchBookBean> books);

        void refreshFinish(Boolean isAll);

        void loadMoreFinish(Boolean isAll);

        void searchBookError();

        void addBookShelfSuccess(List<SearchBookBean> searchBooks);

        void addBookShelfFailed(String massage);

        ChoiceBookAdapter getSearchBookAdapter();

        void updateSearchItem(int index);

        void startRefreshAnim();
    }


}
