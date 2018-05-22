package com.monke.monkeybook.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchHistoryBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.impl.ISearchBookPresenter;
import com.monke.monkeybook.view.impl.ISearchBookView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchBookPresenterImpl extends BasePresenterImpl<ISearchBookView> implements ISearchBookPresenter {
    private static final int BOOK = 2;

    private Boolean hasSearch = false;   //判断是否搜索过

    private long startThisSearchTime;
    private String durSearchKey;

    private List<BookShelfBean> bookShelfS = new ArrayList<>();   //用来比对搜索的书籍是否已经添加进书架

    private Boolean isInput = false;
    private SearchBookModel searchBookModel;

    public SearchBookPresenterImpl(Context context) {
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> temp = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().list();
            if (temp == null)
                temp = new ArrayList<>();
            e.onNext(temp);
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
                return mView.checkIsExist(value);
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                mView.loadMoreSearchBook(value);
            }

            @Override
            public void searchBookError(Boolean value) {
                mView.searchBookError(value);
            }

            @Override
            public int getItemCount() {
                return mView.getSearchBookAdapter().getItemcount();
            }
        };

        //搜索引擎初始化
        searchBookModel = new SearchBookModel((BaseActivity) context, onSearchListener);
    }

    @Override
    public Boolean getHasSearch() {
        return hasSearch;
    }

    @Override
    public void setHasSearch(Boolean hasSearch) {
        this.hasSearch = hasSearch;
    }

    @Override
    public void insertSearchHistory() {
        final int type = SearchBookPresenterImpl.BOOK;
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
                    new String[]{String.valueOf(SearchBookPresenterImpl.BOOK), "%" + content + "%"});
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
    public void querySearchHistory(String content) {
        Observable.create((ObservableOnSubscribe<List<SearchHistoryBean>>) e -> {
            List<SearchHistoryBean> data = DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(SearchBookPresenterImpl.BOOK), SearchHistoryBeanDao.Properties.Content.like("%" + content + "%"))
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
    //添加书集
    @Override
    public void addBookToShelf(final SearchBookBean searchBookBean) {
        final BookShelfBean bookShelfResult = new BookShelfBean();
        bookShelfResult.setNoteUrl(searchBookBean.getNoteUrl());
        bookShelfResult.setFinalDate(System.currentTimeMillis());
        bookShelfResult.setDurChapter(0);
        bookShelfResult.setDurChapterPage(0);
        bookShelfResult.setTag(searchBookBean.getTag());
        BookInfoBean bookInfoBean = new BookInfoBean();
        bookInfoBean.setNoteUrl(searchBookBean.getNoteUrl());
        bookInfoBean.setAuthor(searchBookBean.getAuthor());
        bookInfoBean.setCoverUrl(searchBookBean.getCoverUrl());
        bookInfoBean.setName(searchBookBean.getName());
        bookInfoBean.setTag(searchBookBean.getTag());
        bookShelfResult.setBookInfoBean(bookInfoBean);
        WebBookModelImpl.getInstance().getBookInfo(bookShelfResult)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        saveBookToShelf(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(String.format("添加书籍失败%s", e.getMessage()));
                    }
                });
    }

    private void saveBookToShelf(final BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        //成功   //发送RxBus
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                        saveSearchBookToDb(value.getBookInfoBean().getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(String.format("保存书籍失败%s", e.getMessage()));
                    }
                });
    }

    private void saveSearchBookToDb(String bookName) {
        Observable.create((ObservableOnSubscribe<Boolean>) e->{
            for (SearchBookBean searchBookBean : mView.getSearchBookAdapter().getSearchBooks()) {
                if (Objects.equals(searchBookBean.getName(), bookName)) {
                    DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);
                }
            }
        }).subscribe();
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
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void hadAddBook(BookShelfBean bookShelfBean) {
        bookShelfS.add(bookShelfBean);
        saveSearchBookToDb(bookShelfBean.getBookInfoBean().getName());
        List<SearchBookBean> data = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                data.get(i).setIsAdd(true);
                mView.updateSearchItem(i);
                break;
            }
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void hadRemoveBook(BookShelfBean bookShelfBean) {
        if (bookShelfS != null) {
            for (int i = 0; i < bookShelfS.size(); i++) {
                if (bookShelfS.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                    bookShelfS.remove(i);
                    break;
                }
            }
        }
        List<SearchBookBean> data = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                data.get(i).setIsAdd(false);
                mView.updateSearchItem(i);
                break;
            }
        }
    }

    @Override
    public Boolean getInput() {
        return isInput;
    }

    @Override
    public void upSearchEngineS() {
        searchBookModel.refreshSearchEngineS();
    }


}
