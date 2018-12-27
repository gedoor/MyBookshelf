//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.presenter;

import android.content.Intent;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.RxBusTag;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.presenter.contract.ChoiceBookContract;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChoiceBookPresenter extends BasePresenterImpl<ChoiceBookContract.View> implements ChoiceBookContract.Presenter {
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String tag;
    private String url;
    private String title;

    private int page = 1;
    private long startThisSearchTime;
    private List<BookShelfBean> bookShelfs = new ArrayList<>();   //用来比对搜索的书籍是否已经添加进书架

    public ChoiceBookPresenter(final Intent intent) {
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        tag = intent.getStringExtra("tag");
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> temp = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().list();
            if (temp == null)
                temp = new ArrayList<>();
            e.onNext(temp);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BookShelfBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        bookShelfs.addAll(value);
                        initPage();
                        toSearchBooks(null);
                        mView.startRefreshAnim();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void initPage() {
        this.page = 1;
        this.startThisSearchTime = System.currentTimeMillis();
    }

    @Override
    public void toSearchBooks(String key) {
        final long tempTime = startThisSearchTime;
        searchBook(tempTime);
    }

    private void searchBook(final long searchTime) {
        WebBookModel.getInstance().findBook(url, page, tag)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<SearchBookBean> value) {
                        if (searchTime == startThisSearchTime) {
                            for (SearchBookBean temp : value) {
                                for (BookShelfBean bookShelfBean : bookShelfs) {
                                    if (temp.getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                                        temp.setIsCurrentSource(true);
                                        break;
                                    }
                                }
                            }
                            if (page == 1) {
                                mView.refreshSearchBook(value);
                                mView.refreshFinish(value.size() <= 0);
                            } else {
                                mView.loadMoreSearchBook(value);
                            }
                            page++;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.searchBookError();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addBookToShelf(final SearchBookBean searchBookBean) {
        final BookShelfBean bookShelfResult = new BookShelfBean();
        bookShelfResult.setNoteUrl(searchBookBean.getNoteUrl());
        bookShelfResult.setFinalDate(System.currentTimeMillis());
        bookShelfResult.setDurChapter(0);
        bookShelfResult.setDurChapterPage(0);
        bookShelfResult.setTag(searchBookBean.getTag());
        WebBookModel.getInstance().getBookInfo(bookShelfResult)
                .flatMap(bookShelfBean1 -> WebBookModel.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfResult) {
                        saveBookToShelf(bookShelfResult);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(e.getMessage());
                    }
                });
    }

    @Override
    public String getTitle() {
        return title;
    }

    private void saveBookToShelf(final BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelfBean.getChapterList());
            DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
            //网络数据获取成功  存入BookShelf表数据库
            DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean value) {
                        //成功   //发送RxBus
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(e.getMessage());
                    }
                });
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
        compositeDisposable.dispose();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void hadAddBook(BookShelfBean bookShelfBean) {
        bookShelfs.add(bookShelfBean);
        List<SearchBookBean> datas = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                datas.get(i).setIsCurrentSource(true);
                mView.updateSearchItem(i);
                break;
            }
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void hadRemoveBook(BookShelfBean bookShelfBean) {
        if (bookShelfs != null) {
            for (int i = 0; i < bookShelfs.size(); i++) {
                if (bookShelfs.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                    bookShelfs.remove(i);
                    break;
                }
            }
        }
        List<SearchBookBean> datas = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                datas.get(i).setIsCurrentSource(false);
                mView.updateSearchItem(i);
                break;
            }
        }
    }
}