package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.impl.ISearchTask;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SearchTaskImpl implements ISearchTask {

    private int id;
    private CompositeDisposable disposables;

    private boolean isComplete;

    private OnSearchingListener listener;

    private List<SearchEngine> searchEngines;


    public SearchTaskImpl(int id, List<SearchEngine> searchEngines, OnSearchingListener listener) {
        this.id = id;
        this.listener = listener;
        this.searchEngines = searchEngines;

        disposables = new CompositeDisposable();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void startSearch(String query, Scheduler scheduler) {
        if (searchEngines == null || TextUtils.isEmpty(query) || !listener.checkSameTask(getId())) {
            return;
        }

        isComplete = false;

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        toSearch(query, scheduler);
    }

    @Override
    public void stopSearch() {
        if (!isComplete) {
            isComplete = true;
        }

        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    @Override
    public SearchEngine getNextSearchEngine() {
        if (!isComplete && searchEngines != null) {
            for (SearchEngine engine : searchEngines) {
                if (listener.checkSearchEngine(engine)) {
                    return engine;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    private synchronized void toSearch(String query, Scheduler scheduler) {
        SearchEngine searchEngine = getNextSearchEngine();
        if (listener.checkSearchEngine(searchEngine)) {
            long start = System.currentTimeMillis();
            WebBookModel.getInstance()
                    .searchOtherBook(query, searchEngine.getPage(), searchEngine.getTag())
                    .subscribeOn(scheduler)
                    .flatMap(this::dispatchResult)
                    .doOnComplete(() -> incrementBookSourceWeight(searchEngine.getTag(), start))
                    .doOnError(throwable -> decrementBookSourceWeight(searchEngine.getTag()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposables.add(d);
                        }

                        @Override
                        public void onNext(Boolean bool) {
                            whenNext(searchEngine, bool, query, scheduler);
                        }

                        @Override
                        public void onError(Throwable e) {
                            whenError(searchEngine, query, scheduler);
                        }
                    });
        }
    }

    private void whenNext(SearchEngine searchEngine, boolean hasMore, String query, Scheduler scheduler) {
        if (isComplete) {
            return;
        }

        searchEngine.pageAdd();
        searchEngine.setHasMore(hasMore);
        if (!listener.checkSearchEngine(getNextSearchEngine())) {
            stopSearch();
            listener.onSearchComplete();
        } else {
            toSearch(query, scheduler);
        }
    }

    private void whenError(SearchEngine searchEngine, String query, Scheduler scheduler) {
        if (isComplete) {
            return;
        }

        searchEngine.pageAdd();
        searchEngine.setHasMore(false);
        if (!listener.checkSearchEngine(getNextSearchEngine())) {
            stopSearch();
            if (listener.getShowingItemCount() == 0) {
                listener.onSearchError();
            } else {
                listener.onSearchComplete();
            }
        } else {
            toSearch(query, scheduler);
        }
    }

    private Observable<Boolean> dispatchResult(final List<SearchBookBean> searchBookBeans) {
        return Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            boolean hasMore = true;
            if (!isComplete && listener.checkSameTask(getId())) {
                if (searchBookBeans.size() > 0) {
                    if (!listener.checkExists(searchBookBeans.get(0))) {
                        listener.onSearchResult(searchBookBeans);

                        saveData(searchBookBeans);
                    }
                } else {
                    hasMore = false;
                }
            }
            emitter.onNext(hasMore);
        }).onErrorReturnItem(false);
    }

    private void incrementBookSourceWeight(String tag, long startTime) {
        int searchTime = (int) (System.currentTimeMillis() - startTime);
        BookSourceBean bookSourceBean = BookshelfHelp.getBookSourceByTag(tag);
        if (bookSourceBean != null && searchTime < 10000) {
            bookSourceBean.increaseWeight(10000 / (1000 + searchTime));
            BookshelfHelp.saveBookSource(bookSourceBean);
        }
    }

    private void decrementBookSourceWeight(String tag) {
        BookSourceBean sourceBean = BookshelfHelp.getBookSourceByTag(tag);
        if (sourceBean != null) {
            sourceBean.increaseWeight(-100);
            BookshelfHelp.saveBookSource(sourceBean);
        }
    }

    private static void saveData(List<SearchBookBean> searchBookBeans) {
        if (searchBookBeans != null) {
            DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(searchBookBeans);
        }
    }
}
