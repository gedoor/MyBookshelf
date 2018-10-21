package com.monke.monkeybook.presenter.contract;

import android.widget.EditText;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;

import java.util.List;

public interface SearchBookContract {
    interface Presenter extends IPresenter {

        Boolean getHasSearch();

        void setHasSearch(Boolean hasSearch);

        void insertSearchHistory();

        void querySearchHistory(String content);

        void cleanSearchHistory();

        void cleanSearchHistory(SearchHistoryBean searchHistoryBean);

        int getPage();

        void initPage();

        void setUseMy716(boolean useMy716);

        void toSearchBooks(String key, Boolean fromError);

        void addBookToShelf(final SearchBookBean searchBookBean);

        void stopSearch();
    }

    interface View extends IView {

        void searchBook(String searchKey);

        /**
         * 成功 新增查询记录
         */
        void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean);

        /**
         * 成功搜索 搜索记录
         */
        void querySearchHistorySuccess(List<SearchHistoryBean> datas);

        /**
         * 首次查询成功 更新UI
         */
        void refreshSearchBook();

        /**
         * 加载更多书籍成功 更新UI
         */
        void loadMoreSearchBook(List<SearchBookBean> books);

        /**
         * 刷新成功
         */
        void refreshFinish(Boolean isAll);

        /**
         * 加载成功
         */
        void loadMoreFinish(Boolean isAll);

        /**
         * 搜索失败
         */
        void searchBookError(Boolean isRefresh);

        /**
         * 获取搜索内容EditText
         */
        EditText getEdtContent();

        /**
         * 添加书籍失败
         */
        void addBookShelfFailed(String massage);

        /**
         * @return SearchBookAdapter
         */
        SearchBookAdapter getSearchBookAdapter();

        /**
         * @param index
         */
        void updateSearchItem(int index);

        /**
         * 判断书籍是否已经在书架上
         */
        Boolean checkIsExist(SearchBookBean searchBookBean);

        void upMenu();
    }

}
