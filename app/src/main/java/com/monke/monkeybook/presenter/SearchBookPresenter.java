package com.monke.monkeybook.presenter;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchHistoryBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.presenter.contract.SearchBookContract;
import com.monke.monkeybook.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchBookPresenter extends BasePresenterImpl<SearchBookContract.View> implements SearchBookContract.Presenter {
    private static final int BOOK = 2;

    private String searchKey;
    private SearchBookModel searchBookModel;

    public SearchBookPresenter(Context context, boolean useMy716) {

        //搜索监听
        SearchBookModel.OnSearchListener onSearchListener = new SearchBookModel.OnSearchListener() {

            @Override
            public void searchSourceEmpty() {
                mView.refreshFinish();
                mView.showBookSourceEmptyTip();
            }

            @Override
            public void resetSearchBook() {
                mView.resetSearchBook();
            }

            @Override
            public void searchBookFinish() {
                mView.refreshFinish();
            }

            @Override
            public boolean checkExists(SearchBookBean searchBook) {
                return false;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                mView.loadMoreSearchBook(value);
            }

            @Override
            public void searchBookError() {
                mView.searchBookError();
            }

            @Override
            public int getItemCount() {
                return mView.getSearchBookAdapter().getICount();
            }
        };
        //搜索引擎初始化
        searchBookModel = new SearchBookModel(context, onSearchListener, useMy716);
    }

    @Override
    public void fromIntentSearch(Intent intent) {
        String keyWord = null;
        if (intent != null) {
            keyWord = intent.getStringExtra("searchKey");
            if (keyWord == null && intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
                ClipData.Item item = intent.getClipData().getItemAt(0);
                keyWord = item.getText().toString();
            }
        }
        if (keyWord != null) {
            int start = keyWord.indexOf("《");
            int end = keyWord.indexOf("》");
            if (start >= 0 && end > 1) {
                keyWord = keyWord.substring(start + 1, end);
            } else if (keyWord.length() > 12) {
                keyWord = keyWord.substring(0, 12);
            }
        }
        mView.searchBook(keyWord);
    }

    public void insertSearchHistory() {
        final int type = SearchBookPresenter.BOOK;
        final String content = mView.getEdtContent().getText().toString().trim();
        Observable.create((ObservableOnSubscribe<SearchHistoryBean>) e -> {
            List<SearchHistoryBean> data = DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(type), SearchHistoryBeanDao.Properties.Content.eq(content))
                    .limit(1)
                    .build().list();
            SearchHistoryBean searchHistoryBean;
            if (null != data && data.size() > 0) {
                searchHistoryBean = data.get(0);
                searchHistoryBean.setDate(System.currentTimeMillis());
                DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao().update(searchHistoryBean);
            } else {
                searchHistoryBean = new SearchHistoryBean(type, content, System.currentTimeMillis());
                DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao().insert(searchHistoryBean);
            }
            e.onNext(searchHistoryBean);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<SearchHistoryBean>() {
                    @Override
                    public void onNext(SearchHistoryBean value) {
                        mView.insertSearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void cleanSearchHistory() {
        final String content = mView.getEdtContent().getText().toString().trim();
        Observable.create((ObservableOnSubscribe<Integer>) e -> {
            int a = DbHelper.getInstance().getDb().delete(SearchHistoryBeanDao.TABLENAME,
                    SearchHistoryBeanDao.Properties.Type.columnName + "=? and " + SearchHistoryBeanDao.Properties.Content.columnName + " like ?",
                    new String[]{String.valueOf(SearchBookPresenter.BOOK), "%" + content + "%"});
            e.onNext(a);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer>() {
                    @Override
                    public void onNext(Integer value) {
                        if (value > 0) {
                            mView.querySearchHistorySuccess(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void cleanSearchHistory(SearchHistoryBean searchHistoryBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao().delete(searchHistoryBean);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            querySearchHistory(mView.getEdtContent().getText().toString().trim());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void querySearchHistory(String content) {
        Observable.create((ObservableOnSubscribe<List<SearchHistoryBean>>) e -> {
            List<SearchHistoryBean> data = DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(SearchBookPresenter.BOOK), SearchHistoryBeanDao.Properties.Content.like("%" + content + "%"))
                    .orderDesc(SearchHistoryBeanDao.Properties.Date)
                    .limit(20)
                    .build().list();
            e.onNext(data);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchHistoryBean>>() {
                    @Override
                    public void onNext(List<SearchHistoryBean> value) {
                        if (null != value)
                            mView.querySearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void setUseMy716(boolean useMy716) {
        searchBookModel.setUseMy716(useMy716);
    }

    @Override
    public void toSearchBooks(String key) {
        if (key != null) {
            searchKey = key;
        }
        if (!NetworkUtil.isNetWorkAvailable()) {
            mView.searchBookError();
            return;
        }

        int id = (int) System.currentTimeMillis();
        if (key == null) {
            searchBookModel.startSearch(id, searchKey);
        } else {
            searchBookModel.startSearch(id, key);
        }
    }

    @Override
    public void stopSearch(boolean callEvent) {
        searchBookModel.stopSearch(callEvent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
        if (searchBookModel != null) {
            searchBookModel.stopSearch(true);
            searchBookModel.shutdownSearch();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SEARCH_BOOK)})
    public void searchBook(String searchKey) {
        mView.searchBook(searchKey);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SOURCE_LIST_CHANGE)})
    public void sourceListChange(Boolean change) {
        searchBookModel.setSearchEngineChanged();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.GET_ZFB_Hb)})
    public void getZfbHB(Boolean getZfbHB) {
        searchBookModel.setUseMy716(getZfbHB);
        mView.upMenu();
    }
}
