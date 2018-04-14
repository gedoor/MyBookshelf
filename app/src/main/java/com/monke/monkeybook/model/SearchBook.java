package com.monke.monkeybook.model;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/16.
 * 搜索
 */

public class SearchBook {
    private long startThisSearchTime;
    private List<SearchEngine> searchEngineS;
    private int threadsNum;
    private int page = 0;
    private int searchEngineIndex;
    private int searchSuccessNum;

    private OnSearchListener searchListener;

    public SearchBook(OnSearchListener searchListener) {
        this.searchListener = searchListener;
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        threadsNum = preference.getInt(MApplication.getInstance().getString(R.string.pk_threads_num), 6);

        //搜索引擎初始化
        searchEngineS = new ArrayList<>();
        for (BookSourceBean bookSourceBean : BookSourceManage.getSelectedBookSource()) {
            SearchEngine se = new SearchEngine();
            se.setTag(bookSourceBean.getBookSourceUrl());
            se.setHasMore(true);
            searchEngineS.add(se);
        }
    }

    public void refreshSearchEngineS() {
        searchEngineS.clear();
        for (BookSourceBean bookSourceBean : BookSourceManage.getSelectedBookSource()) {
            SearchEngine se = new SearchEngine();
            se.setTag(bookSourceBean.getBookSourceUrl());
            se.setHasMore(true);
            searchEngineS.add(se);
        }
    }

    public void searchReNew() {
        page = 0;
        for (SearchEngine searchEngine : searchEngineS) {
            searchEngine.setHasMore(true);
        }
    }

    public void setSearchTime(long searchTime) {
        this.startThisSearchTime = searchTime;
    }

    public void search(final String content, final long searchTime, List<BookShelfBean> bookShelfS, Boolean fromError) {
        if (searchTime != startThisSearchTime) {
            return;
        }
        if (!fromError) {
            page++;
        }
        if (page == 0) {
            page = 1;
        }
        if (page == 1) {
            searchListener.refreshSearchBook();
        }
        searchSuccessNum = 0;
        searchEngineIndex = -1;
        for (int i = 1; i <= threadsNum; i++) {
            searchOnEngine(content, bookShelfS);
        }
    }

    private void searchOnEngine(final String content, List<BookShelfBean> bookShelfS) {
        searchEngineIndex++;
        if (searchEngineIndex < searchEngineS.size()) {
            SearchEngine searchEngine = searchEngineS.get(searchEngineIndex);
            if (searchEngine.getHasMore()) {
                WebBookModelImpl.getInstance()
                        .searchOtherBook(content, page, searchEngine.getTag())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                            @Override
                            public void onNext(List<SearchBookBean> value) {
                                searchSuccessNum++;
                                if (value.size() > 0) {
                                    for (SearchBookBean temp : value) {
                                        for (BookShelfBean bookShelfBean : bookShelfS) {
                                            if (temp.getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                                                temp.setIsAdd(true);
                                                break;
                                            }
                                        }
                                    }
                                    if (!searchListener.checkIsExist(value.get(0))) {
                                        searchListener.loadMoreSearchBook(value);
                                    }
                                } else {
                                    searchEngine.setHasMore(false);
                                }
                                searchOnEngine(content, bookShelfS);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                searchOnEngine(content, bookShelfS);
                            }
                        });
            } else {
                searchOnEngine(content, bookShelfS);
            }
        } else {
            if (searchEngineIndex >= searchEngineS.size() + threadsNum - 1) {
                if (searchSuccessNum == 0 && searchListener.getItemCount() == 0) {
                    if (page == 1) {
                        searchListener.searchBookError(true);
                    } else {
                        searchListener.searchBookError(false);
                    }
                } else {
                    if (page == 1) {
                        searchListener.refreshFinish(false);
                    } else if (page < 3) {
                        searchListener.loadMoreFinish(false);
                    } else {
                        searchListener.loadMoreFinish(true);
                    }
                }
            }
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public interface OnSearchListener {
        void refreshSearchBook();

        void refreshFinish(Boolean isAll);

        void loadMoreFinish(Boolean isAll);

        Boolean checkIsExist(SearchBookBean searchBookBean);

        void loadMoreSearchBook(List<SearchBookBean> searchBookBeanList);

        void searchBookError(Boolean isRefresh);

        int getItemCount();
    }

    private class SearchEngine {
        private String tag;
        private Boolean hasMore;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        Boolean getHasMore() {
            return hasMore;
        }

        void setHasMore(Boolean hasMore) {
            this.hasMore = hasMore;
        }

    }
}
