package com.monke.monkeybook.model;

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

    private int page = 1;

    private OnSearchListener searchListener;

    public SearchBook(OnSearchListener searchListener) {
        this.searchListener = searchListener;

        //搜索引擎初始化
        searchEngineS = new ArrayList<>();
        for (BookSourceBean bookSourceBean : BookSourceManage.getSelectedBookSource()) {
            SearchEngine se = new SearchEngine();
            se.setTag(bookSourceBean.getBookSourceUrl());
            se.setHasMore(true);
            se.setHasLoad(false);
            se.setDurRequestTime(1);
            se.setMaxRequestTime(3);
            searchEngineS.add(se);
        }
    }

    public void searchReNew() {
        for (SearchEngine searchEngine : searchEngineS) {
            searchEngine.setHasMore(true);
            searchEngine.setHasLoad(false);
            searchEngine.setDurRequestTime(1);
        }
    }

    public void setSearchTime(long searchTime) {
        this.startThisSearchTime = searchTime;
    }

    //搜索书集
    public void search(final String content, final long searchTime, List<BookShelfBean> bookShelfS, Boolean fromError) {
        if (searchTime == startThisSearchTime) {
            Boolean canLoad = false;
            for (SearchEngine temp : searchEngineS) {
                if (temp.getHasMore() && temp.getDurRequestTime() <= temp.getMaxRequestTime()) {
                    canLoad = true;
                    break;
                }
            }
            if (canLoad) {
                int searchEngineIndex = -1;
                for (int i = 0; i < searchEngineS.size(); i++) {
                    if (!searchEngineS.get(i).getHasLoad() && searchEngineS.get(i).getDurRequestTime() <= searchEngineS.get(i).getMaxRequestTime()) {
                        searchEngineIndex = i;
                        break;
                    }
                }
                if (searchEngineIndex == -1) {
                    this.page++;
                    for (SearchEngine item : searchEngineS) {
                        item.setHasLoad(false);
                    }
                    if (!fromError) {
                        if (page - 1 == 1) {
                            searchListener.refreshFinish(false);
                        } else {
                            searchListener.loadMoreFinish(false);
                        }
                    } else {
                        search(content, searchTime, bookShelfS, false);
                    }
                } else {
                    searchOnEngine(content, searchTime, bookShelfS, searchEngineIndex);
                }
            } else {
                if (page == 1) {
                    searchListener.refreshFinish(true);
                } else {
                    searchListener.loadMoreFinish(true);
                }
                this.page++;
                for (SearchEngine item : searchEngineS) {
                    item.setHasLoad(false);
                }
            }
        }
    }

    private void searchOnEngine(final String content, final long searchTime, List<BookShelfBean> bookShelfS, final int searchEngineIndex) {
        WebBookModelImpl.getInstance()
                .searchOtherBook(content, page, searchEngineS.get(searchEngineIndex).getTag())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> value) {
                        searchSuccess(content, searchTime, bookShelfS, searchEngineIndex, value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        searchError(searchEngineIndex, searchTime);
                    }
                });
    }

    private void searchSuccess(final String content, final long searchTime, List<BookShelfBean> bookShelfS, final int searchEngineIndex, List<SearchBookBean> value) {
        if (searchTime == startThisSearchTime) {
            searchEngineS.get(searchEngineIndex).setHasLoad(true);
            searchEngineS.get(searchEngineIndex).setDurRequestTime(1);
            if (value.size() == 0) {
                searchEngineS.get(searchEngineIndex).setHasMore(false);
            } else {
                for (SearchBookBean temp : value) {
                    for (BookShelfBean bookShelfBean : bookShelfS) {
                        if (temp.getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                            temp.setIsAdd(true);
                            break;
                        }
                    }
                }
            }
            if (page == 1 && searchEngineIndex == 0) {
                searchListener.refreshSearchBook(value);
            } else {
                if (value.size() > 0 && !searchListener.checkIsExist(value.get(0)))
                    searchListener.loadMoreSearchBook(value);
                else {
                    searchEngineS.get(searchEngineIndex).setHasMore(false);
                }
            }
            search(content, searchTime, bookShelfS, false);
        }
    }

    private void searchError(int finalSearchEngineIndex, long searchTime) {
        if (searchTime == startThisSearchTime) {
            searchEngineS.get(finalSearchEngineIndex).setHasLoad(false);
            searchEngineS.get(finalSearchEngineIndex)
                    .setDurRequestTime(searchEngineS.get(finalSearchEngineIndex).getDurRequestTime() + 1);
            searchListener.searchBookError(page == 1
                    && (finalSearchEngineIndex == 0
                    || (finalSearchEngineIndex > 0 && searchListener.getItemCount() == 0)));
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
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

    private class SearchEngine {
        private String tag;
        private Boolean hasMore;
        private Boolean hasLoad;
        //当前搜索引擎失败次数  成功一次会重新开始计数
        private int durRequestTime;
        //最大连续请求失败次数
        private int maxRequestTime;

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

        Boolean getHasLoad() {
            return hasLoad;
        }

        void setHasLoad(Boolean hasLoad) {
            this.hasLoad = hasLoad;
        }

        int getDurRequestTime() {
            return durRequestTime;
        }

        void setDurRequestTime(int durRequestTime) {
            this.durRequestTime = durRequestTime;
        }

        int getMaxRequestTime() {
            return maxRequestTime;
        }

        void setMaxRequestTime(int maxRequestTime) {
            this.maxRequestTime = maxRequestTime;
        }

    }
}
