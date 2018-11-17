package com.monke.monkeybook.presenter.contract;

import android.content.Intent;
import android.widget.EditText;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;

import java.util.List;

public interface SearchBookContract {
    interface Presenter extends IPresenter {

        void fromIntentSearch(Intent intent);

        void insertSearchHistory();

        void querySearchHistory(String query);

        void cleanSearchHistory();

        void cleanSearchHistory(SearchHistoryBean searchHistoryBean);

        void setUseMy716(boolean useMy716);

        void toSearchBooks(String key);

        void stopSearch(boolean callEvent);
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
         * 清空搜索列表
         */
        void resetSearchBook();

        /**
         * 加载更多书籍成功 更新UI
         */
        void loadMoreSearchBook(List<SearchBookBean> books);

        /**
         * 刷新成功
         */
        void refreshFinish();

        /**
         * 搜索失败
         */
        void searchBookError();

        /**
         * 获取搜索内容EditText
         */
        EditText getEdtContent();

        /**
         * @return SearchBookAdapter
         */
        SearchBookAdapter getSearchBookAdapter();

        void showBookSourceEmptyTip();

        void upMenu();
    }

}
