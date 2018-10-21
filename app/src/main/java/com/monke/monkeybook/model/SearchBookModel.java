package com.monke.monkeybook.model;

import android.content.SharedPreferences;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.model.source.My716;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/16.
 * 搜索
 */

public class SearchBookModel {
    private MBaseActivity activity;
    private long startThisSearchTime;
    private List<SearchEngine> searchEngineS = new ArrayList<>();
    private int threadsNum;
    private int page = 0;
    private int searchEngineIndex;
    private int searchSuccessNum;
    private CompositeDisposable compositeDisposable;
    private OnSearchListener searchListener;
    private boolean useMy716;

    public SearchBookModel(MBaseActivity activity, OnSearchListener searchListener, boolean useMy716) {
        this.activity = activity;
        this.searchListener = searchListener;
        this.useMy716 = useMy716;
        SharedPreferences preference = MApplication.getInstance().getConfigPreferences();
        threadsNum = preference.getInt(activity.getString(R.string.pk_threads_num), 6);
        compositeDisposable = new CompositeDisposable();
        initSearchEngineS();
    }

    /**
     * 搜索引擎初始化
     */
    public void initSearchEngineS() {
        searchEngineS.clear();
        if (Objects.equals(ACache.get(activity).getAsString("getZfbHb"), "True") && useMy716) {
            SearchEngine my716 = new SearchEngine();
            my716.setTag(My716.TAG);
            my716.setHasMore(true);
            searchEngineS.add(my716);
        }
        for (BookSourceBean bookSourceBean : BookSourceManage.getSelectedBookSource()) {
            SearchEngine se = new SearchEngine();
            se.setTag(bookSourceBean.getBookSourceUrl());
            se.setHasMore(true);
            searchEngineS.add(se);
        }
    }

    public void searchReNew() {
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        page = 0;
        for (SearchEngine searchEngine : searchEngineS) {
            searchEngine.setHasMore(true);
        }
    }

    public void stopSearch() {
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        searchListener.refreshFinish(true);
        searchListener.loadMoreFinish(true);
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
        if (searchEngineS.size() == 0) {
            activity.toast("没有选中任何书源");
            searchListener.refreshFinish(true);
            searchListener.loadMoreFinish(true);
            return;
        }
        searchSuccessNum = 0;
        searchEngineIndex = -1;
        for (int i = 1; i <= threadsNum; i++) {
            searchOnEngine(content, bookShelfS, searchTime);
        }
    }

    private synchronized void searchOnEngine(final String content, List<BookShelfBean> bookShelfS, final long searchTime) {
        if (searchTime != startThisSearchTime) {
            return;
        }
        searchEngineIndex++;
        long startTime = System.currentTimeMillis();
        if (searchEngineIndex < searchEngineS.size()) {
            final SearchEngine searchEngine = searchEngineS.get(searchEngineIndex);
            if (searchEngine.getHasMore()) {
                WebBookModelImpl.getInstance()
                        .searchOtherBook(content, page, searchEngine.getTag())
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.newThread())
                        .compose(activity.bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Observer<List<SearchBookBean>>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(List<SearchBookBean> searchBookBeans) {
                                if (searchTime == startThisSearchTime) {
                                    searchSuccessNum++;
                                    if (searchBookBeans.size() > 0) {
                                        for (SearchBookBean temp : searchBookBeans) {
                                            int searchTime = (int) (System.currentTimeMillis() - startTime) / 1000;
                                            temp.setSearchTime(searchTime);
                                            for (BookShelfBean bookShelfBean : bookShelfS) {
                                                if (temp.getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                                                    temp.setIsAdd(true);
                                                    break;
                                                }
                                            }
                                        }
                                        if (!searchListener.checkIsExist(searchBookBeans.get(0))) {
                                            searchListener.loadMoreSearchBook(searchBookBeans);
                                        } else {
                                            searchEngine.setHasMore(false);
                                        }
                                    } else {
                                        searchEngine.setHasMore(false);
                                    }
                                    searchOnEngine(content, bookShelfS, searchTime);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                searchEngine.setHasMore(false);
                                searchOnEngine(content, bookShelfS, searchTime);
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else {
                searchOnEngine(content, bookShelfS, searchTime);
            }
        } else {
            activity.runOnUiThread(() -> {
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
                        }
                        for (SearchEngine engine : searchEngineS) {
                            if (engine.hasMore) {
                                searchListener.loadMoreFinish(false);
                                return;
                            }
                        }
                        searchListener.loadMoreFinish(true);
                    }
                }
            });
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setUseMy716(boolean useMy716) {
        this.useMy716 = useMy716;
        initSearchEngineS();
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
