package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;

import java.util.List;

import io.reactivex.Scheduler;

public interface ISearchTask {

    int getId();

    void setId(int id);

    SearchEngine getNextSearchEngine();

    void startSearch(String query, Scheduler scheduler);

    void stopSearch();

    boolean isComplete();

    interface OnSearchingListener {
        boolean checkSameTask(int id);

        boolean checkSearchEngine(SearchEngine engine);

        boolean checkExists(SearchBookBean searchBook);

        int getShowingItemCount();

        void onSearchResult(List<SearchBookBean> searchBooks);

        void onSearchError();

        void onSearchComplete();
    }

}
