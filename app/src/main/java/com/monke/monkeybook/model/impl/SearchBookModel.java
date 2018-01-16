package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/16.
 */

public class SearchBookModel {
    private static final String TAG_KEY = "tag";
    private static final String HAS_MORE_KEY = "hasMore";
    private static final String HAS_LOAD_KEY = "hasLoad";
    private static final String DUR_REQUEST_TIME = "durRequestTime";    //当前搜索引擎失败次数  成功一次会重新开始计数
    private static final String MAX_REQUEST_TIME = "maxRequestTime";   //最大连续请求失败次数

    private long startThisSearchTime;
    private List<Map> searchEngine;
    private int page = 1;

    private List<BookShelfBean> bookShelfS = new ArrayList<>();
    private OnSearchListener searchListener;

    public SearchBookModel(OnSearchListener searchListener) {
        this.searchListener = searchListener;
        //搜索引擎初始化
        searchEngine = new ArrayList<>();
        for (BookSourceBean bookSourceBean: AllBookSource.getSelectedBookSource()) {
            Map se = new HashMap();
            se.put(TAG_KEY, bookSourceBean.getBookSourceUrl());
            se.put(HAS_MORE_KEY, true);
            se.put(HAS_LOAD_KEY, false);
            se.put(DUR_REQUEST_TIME, 1);
            se.put(MAX_REQUEST_TIME, 3);
            searchEngine.add(se);
        }
    }

    public interface OnSearchListener {
        void refreshSearchBook(List<SearchBookBean> value);

        void refreshFinish(Boolean value);

        void loadMoreFinish(Boolean value);

        Boolean checkIsExist(SearchBookBean value);

        void loadMoreSearchBook(List<SearchBookBean> value);

        void searchBookError(Boolean value);

        int getItemCount();
    }

    //搜索书集
    private void searchBook(final String content, final long searchTime, Boolean fromError) {
        if (searchTime == startThisSearchTime) {
            Boolean canLoad = false;
            for (Map temp : searchEngine) {
                if ((Boolean) temp.get(HAS_MORE_KEY) && (int) temp.get(DUR_REQUEST_TIME) <= (int) temp.get(MAX_REQUEST_TIME)) {
                    canLoad = true;
                    break;
                }
            }
            if (canLoad) {
                int searchEngineIndex = -1;
                for (int i = 0; i < searchEngine.size(); i++) {
                    if (!(Boolean) searchEngine.get(i).get(HAS_LOAD_KEY) && (int) searchEngine.get(i).get(DUR_REQUEST_TIME) <= (int) searchEngine.get(i).get(MAX_REQUEST_TIME)) {
                        searchEngineIndex = i;
                        break;
                    }
                }
                if (searchEngineIndex == -1) {
                    this.page++;
                    for (Map item : searchEngine) {
                        item.put(HAS_LOAD_KEY, false);
                    }
                    if (!fromError) {
                        if (page - 1 == 1) {
                            searchListener.refreshFinish(false);
                        } else {
                            searchListener.loadMoreFinish(false);
                        }
                    } else {
                        searchBook(content, searchTime, false);
                    }
                } else {
                    final int finalSearchEngineIndex = searchEngineIndex;
                    WebBookModelImpl.getInstance().searchOtherBook(content, page, (String) searchEngine.get(searchEngineIndex).get(TAG_KEY))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                                @Override
                                public void onNext(List<SearchBookBean> value) {
                                    if (searchTime == startThisSearchTime) {
                                        searchEngine.get(finalSearchEngineIndex).put(HAS_LOAD_KEY, true);
                                        searchEngine.get(finalSearchEngineIndex).put(DUR_REQUEST_TIME, 1);
                                        if (value.size() == 0) {
                                            searchEngine.get(finalSearchEngineIndex).put(HAS_MORE_KEY, false);
                                        } else {
                                            for (SearchBookBean temp : value) {
                                                for (BookShelfBean bookShelfBean : bookShelfS) {
                                                    if (temp.getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                                                        temp.setAdd(true);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (page == 1 && finalSearchEngineIndex == 0) {
                                            searchListener.refreshSearchBook(value);
                                        } else {
                                            if (value.size() > 0 && !searchListener.checkIsExist(value.get(0)))
                                                searchListener.loadMoreSearchBook(value);
                                            else {
                                                searchEngine.get(finalSearchEngineIndex).put(HAS_MORE_KEY, false);
                                            }
                                        }
                                        searchBook(content, searchTime, false);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                    if (searchTime == startThisSearchTime) {
                                        searchEngine.get(finalSearchEngineIndex).put(HAS_LOAD_KEY, false);
                                        searchEngine.get(finalSearchEngineIndex).put(DUR_REQUEST_TIME, ((int) searchEngine.get(finalSearchEngineIndex).get(DUR_REQUEST_TIME)) + 1);
                                        searchListener.searchBookError(page == 1 && (finalSearchEngineIndex == 0 || (finalSearchEngineIndex > 0 && searchListener.getItemCount() == 0)));
                                    }
                                }
                            });
                }
            } else {
                if (page == 1) {
                    searchListener.refreshFinish(true);
                } else {
                    searchListener.loadMoreFinish(true);
                }
                this.page++;
                for (Map item : searchEngine) {
                    item.put(HAS_LOAD_KEY, false);
                }
            }
        }
    }
}
