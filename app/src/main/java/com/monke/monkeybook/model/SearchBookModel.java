package com.monke.monkeybook.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.model.impl.ISearchTask;
import com.monke.monkeybook.model.source.My716;
import com.monke.monkeybook.model.tsak.SearchTaskImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/16.
 * 搜索
 */

public class SearchBookModel implements ISearchTask.OnSearchingListener {
    private int startThisId;
    private int threadsNum;
    private OnSearchListener searchListener;
    private boolean useMy716;
    private boolean searchEngineChanged = false;

    private ExecutorService executor;
    private Scheduler scheduler;

    private final List<SearchEngine> searchEngineS = new ArrayList<>();
    private final List<ISearchTask> searchTasks = new ArrayList<>();


    public SearchBookModel(Context context, OnSearchListener searchListener, boolean useMy716) {
        this.searchListener = searchListener;
        this.useMy716 = useMy716;
        SharedPreferences preference = context.getSharedPreferences("CONFIG", 0);
        threadsNum = preference.getInt(context.getString(R.string.pk_threads_num), 4);
        executor = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executor);
        initSearchEngineS(BookSourceManager.getSelectedBookSource());
    }

    /**
     * 搜索引擎初始化
     */
    public void initSearchEngineS(List<BookSourceBean> bookSourceBeans) {
        searchEngineS.clear();
        if (useMy716 && Objects.equals(ACache.get(MApplication.getInstance()).getAsString("getZfbHb"), "True")) {
            searchEngineS.add(new SearchEngine(My716.TAG));
        }
        if(bookSourceBeans != null) {
            for (BookSourceBean bookSourceBean : bookSourceBeans) {
                searchEngineS.add(new SearchEngine(bookSourceBean.getBookSourceUrl()));
            }
        }
        searchEngineChanged = false;
    }

    private void resetSearchEngineS(boolean init) {
        if (init || searchEngineS.isEmpty()) {
            initSearchEngineS(BookSourceManager.getSelectedBookSource());
        } else {
            for (SearchEngine searchEngine : searchEngineS) {
                searchEngine.setPage(1);
                searchEngine.setHasMore(true);
            }
        }
    }

    private void resetSearch(boolean clear, int id) {
        if (!searchTasks.isEmpty()) {
            for (ISearchTask searchTask : searchTasks) {
                searchTask.stopSearch();
                searchTask.setId(id);
            }
            if (clear) {
                searchTasks.clear();
            }
        }
    }

    public void startSearch(int id, String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }

        startThisId = id;
        resetSearch(searchEngineChanged, startThisId);
        resetSearchEngineS(searchEngineChanged);

        if (searchEngineS.isEmpty()) {
            searchListener.searchSourceEmpty();
            return;
        }

        searchListener.resetSearchBook();

        search(id, query);
    }

    private void search(int id, String query) {
        if (searchTasks.isEmpty()) {
            int length = searchEngineS.size();
            int seek = length % threadsNum == 0 ? length / threadsNum : (length / threadsNum + 1);
            for (int i = 0, size = Math.min(length, threadsNum); i < size; i++) {
                int end = (i + 1) * seek;
                List<SearchEngine> engines = searchEngineS.subList(i * seek, end >= length ? length : end);
                ISearchTask searchTask = new SearchTaskImpl(id, new ArrayList<>(engines), this);
                searchTask.startSearch(query, scheduler);
                searchTasks.add(searchTask);
                if (end >= length) {
                    break;
                }
            }
        } else {
            for (ISearchTask searchTask : searchTasks) {
                searchTask.startSearch(query, scheduler);
            }
        }
    }

    public void stopSearch(boolean callEvent) {
        resetSearch(false, 0);
        if(callEvent) {
            searchListener.searchBookFinish();
        }
    }

    public void shutdownSearch() {
        resetSearch(true, 0);
        executor.shutdown();
    }

    public void setUseMy716(boolean useMy716) {
        this.useMy716 = useMy716;
        setSearchEngineChanged();
    }

    public void setSearchEngineChanged() {
        searchEngineChanged = true;
    }

    @Override
    public boolean checkSameTask(int id) {
        return startThisId == id;
    }

    @Override
    public boolean checkSearchEngine(SearchEngine engine) {
        return engine != null && engine.getHasMore();
    }

    @Override
    public boolean checkExists(SearchBookBean searchBook) {
        return searchListener.checkExists(searchBook);
    }

    @Override
    public int getShowingItemCount() {
        return searchListener.getItemCount();
    }

    @Override
    public void onSearchResult(List<SearchBookBean> searchBooks) {
        searchListener.loadMoreSearchBook(searchBooks);
    }

    @Override
    public void onSearchError() {
        for (ISearchTask searchTask : searchTasks) {
            if (!searchTask.isComplete()) {
                return;
            }
        }
        searchListener.searchBookError();
    }

    @Override
    public void onSearchComplete() {
        for (ISearchTask searchTask : searchTasks) {
            if (!searchTask.isComplete()) {
                return;
            }
        }
        searchListener.searchBookFinish();
    }

    public interface OnSearchListener {
        void searchSourceEmpty();

        void resetSearchBook();

        void searchBookFinish();

        boolean checkExists(SearchBookBean searchBook);

        void loadMoreSearchBook(List<SearchBookBean> searchBookBeanList);

        void searchBookError();

        int getItemCount();
    }

}
