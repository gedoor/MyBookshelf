package com.kunfei.bookshelf.presenter;

import android.content.Context;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.dao.SearchHistoryBeanDao;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.presenter.contract.SearchBookContract;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchBookPresenter extends BasePresenterImpl<SearchBookContract.View> implements SearchBookContract.Presenter {
    private static final int BOOK = 2;

    private long startThisSearchTime;
    private String durSearchKey;

    private List<BookShelfBean> bookShelfS = new ArrayList<>();   //用来比对搜索的书籍是否已经添加进书架

    private SearchBookModel searchBookModel;

    public SearchBookPresenter(Context context) {
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> booAll = BookshelfHelp.getAllBook();
            e.onNext(booAll == null ? new ArrayList<>() : booAll);
            e.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        bookShelfS.addAll(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });

        //搜索监听
        SearchBookModel.OnSearchListener onSearchListener = new SearchBookModel.OnSearchListener() {
            @Override
            public void refreshSearchBook() {
                mView.refreshSearchBook();
            }

            @Override
            public void refreshFinish(Boolean value) {
                mView.refreshFinish(value);
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                mView.loadMoreFinish(value);
            }

            @Override
            public Boolean checkIsExist(SearchBookBean value) {
                return false;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                mView.loadMoreSearchBook(value);
            }

            @Override
            public void searchBookError(Boolean value) {
                mView.searchBookError(value);
                searchBookModel.stopSearch();
            }

            @Override
            public int getItemCount() {
                return mView.getSearchBookAdapter().getICount();
            }
        };
        //搜索引擎初始化
        searchBookModel = new SearchBookModel(context, onSearchListener);
    }

    public void insertSearchHistory() {
        final int type = SearchBookPresenter.BOOK;
        final String content = mView.getEdtContent().getText().toString().trim();
        Observable.create((ObservableOnSubscribe<SearchHistoryBean>) e -> {
            List<SearchHistoryBean> data = DbHelper.getDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(type), SearchHistoryBeanDao.Properties.Content.eq(content))
                    .limit(1)
                    .build().list();
            SearchHistoryBean searchHistoryBean;
            if (null != data && data.size() > 0) {
                searchHistoryBean = data.get(0);
                searchHistoryBean.setDate(System.currentTimeMillis());
                DbHelper.getDaoSession().getSearchHistoryBeanDao().update(searchHistoryBean);
            } else {
                searchHistoryBean = new SearchHistoryBean(type, content, System.currentTimeMillis());
                DbHelper.getDaoSession().getSearchHistoryBeanDao().insert(searchHistoryBean);
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
            DbHelper.getDaoSession().getSearchHistoryBeanDao().delete(searchHistoryBean);
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
            List<SearchHistoryBean> data = DbHelper.getDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(SearchBookPresenter.BOOK), SearchHistoryBeanDao.Properties.Content.like("%" + content + "%"))
                    .orderDesc(SearchHistoryBeanDao.Properties.Date)
                    .limit(50)
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
    public int getPage() {
        return searchBookModel.getPage();
    }

    @Override
    public void initPage() {
        searchBookModel.setPage(0);
    }

    @Override
    public void toSearchBooks(String key, Boolean fromError) {
        if (key != null) {
            durSearchKey = key;
            startThisSearchTime = System.currentTimeMillis();
            searchBookModel.setSearchTime(startThisSearchTime);
            searchBookModel.searchReNew();
        }
        searchBookModel.search(durSearchKey, startThisSearchTime, bookShelfS, fromError);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void stopSearch() {
        searchBookModel.stopSearch();
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
        searchBookModel.onDestroy();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SEARCH_BOOK)})
    public void searchBook(String searchKey) {
        mView.searchBook(searchKey);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SOURCE_LIST_CHANGE)})
    public void sourceListChange(Boolean change) {

        searchBookModel.initSearchEngineS(BookSourceManager.getSelectedBookSource());
    }

}
