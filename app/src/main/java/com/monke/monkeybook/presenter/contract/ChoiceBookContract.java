package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.view.adapter.ChoiceBookAdapter;

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
