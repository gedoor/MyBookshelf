//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter.impl;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.IView;
import com.monke.basemvplib.impl.BaseActivity;
import com.monke.basemvplib.impl.BasePresenterImpl;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.common.RxBusTag;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.listener.OnGetChapterListListener;
import com.monke.monkeybook.model.impl.WebBookModelImpl;
import com.monke.monkeybook.presenter.IBookDetailPresenter;
import com.monke.monkeybook.view.IBookDetailView;
import com.trello.rxlifecycle2.android.ActivityEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BookDetailPresenterImpl extends BasePresenterImpl<IBookDetailView> implements IBookDetailPresenter {
    public final static int FROM_BOOKSHELF = 1;
    public final static int FROM_SEARCH = 2;

    private int openfrom;
    private SearchBookBean searchBook;
    private BookShelfBean bookShelf;
    private Boolean inBookShelf = false;

    private List<BookShelfBean> bookShelfs = Collections.synchronizedList(new ArrayList<BookShelfBean>());   //用来比对搜索的书籍是否已经添加进书架

    public BookDetailPresenterImpl(Intent intent) {
        openfrom = intent.getIntExtra("from", FROM_BOOKSHELF);
        if (openfrom == FROM_BOOKSHELF) {
            String key = intent.getStringExtra("data_key");
            bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
            BitIntentDataManager.getInstance().cleanData(key);
            inBookShelf = true;
            searchBook = new SearchBookBean();
            searchBook.setNoteUrl(bookShelf.getNoteUrl());
            searchBook.setTag(bookShelf.getTag());
        } else {
            searchBook = intent.getParcelableExtra("data");
            inBookShelf = searchBook.getAdd();
        }
    }

    public Boolean getInBookShelf() {
        return inBookShelf;
    }

    public void setInBookShelf(Boolean inBookShelf) {
        this.inBookShelf = inBookShelf;
    }

    public int getOpenfrom() {
        return openfrom;
    }

    public SearchBookBean getSearchBook() {
        return searchBook;
    }

    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void getBookShelfInfo() {
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> temp = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().list();
            if (temp == null)
                temp = new ArrayList<BookShelfBean>();
            e.onNext(temp);
            e.onComplete();
        }).flatMap(bookShelfBeen -> {
            bookShelfs.addAll(bookShelfBeen);

            final BookShelfBean bookShelfResult = new BookShelfBean();
            bookShelfResult.setNoteUrl(searchBook.getNoteUrl());
            bookShelfResult.setFinalDate(System.currentTimeMillis());
            bookShelfResult.setDurChapter(0);
            bookShelfResult.setDurChapterPage(0);
            bookShelfResult.setTag(searchBook.getTag());
            return WebBookModelImpl.getInstance().getBookInfo(bookShelfResult);
        }).map(bookShelfBean -> {
            for(int i=0;i<bookShelfs.size();i++){
                if(bookShelfs.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())){
                    if (openfrom == FROM_BOOKSHELF) {
                        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
                    }
                    inBookShelf = true;
                    break;
                }
            }
            return bookShelfBean;
        }).subscribeOn(Schedulers.newThread())
                .compose(((BaseActivity)mView.getContext()).<BookShelfBean>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        WebBookModelImpl.getInstance().getChapterList(value, new OnGetChapterListListener() {
                            @Override
                            public void success(BookShelfBean bookShelfBean) {
                                if (openfrom == FROM_BOOKSHELF) {
                                    int durChapter = bookShelf.getDurChapter();
                                    bookShelf = bookShelfBean;
                                    bookShelf.setDurChapter(durChapter);
                                } else {
                                    bookShelf = bookShelfBean;
                                }
                                mView.updateView();
                            }

                            @Override
                            public void error() {
                                bookShelf = null;
                                mView.getBookShelfError();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        bookShelf = null;
                        mView.getBookShelfError();
                    }
                });
    }

    @Override
    public void addToBookShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelf.getBookInfoBean().getChapterlist());
                DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelf.getBookInfoBean());
                //网络数据获取成功  存入BookShelf表数据库
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity)mView.getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                            } else {
                                Toast.makeText(MApplication.getInstance(), "放入书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(MApplication.getInstance(), "放入书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void removeFromBookShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().deleteByKey(bookShelf.getNoteUrl());
                DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().deleteByKey(bookShelf.getBookInfoBean().getNoteUrl());
                List<String> keys = new ArrayList<String>();
                if(bookShelf.getBookInfoBean().getChapterlist().size()>0){
                    for(int i=0;i<bookShelf.getBookInfoBean().getChapterlist().size();i++){
                        keys.add(bookShelf.getBookInfoBean().getChapterlist().get(i).getDurChapterUrl());
                    }
                }
                DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().deleteByKeyInTx(keys);
                DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteInTx(bookShelf.getBookInfoBean().getChapterlist());
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity)mView.getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                            } else {
                                Toast.makeText(MApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(MApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

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
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK)
            }
    )
    public void hadAddBook(BookShelfBean value) {
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl())) || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(inBookShelf);
            }
            mView.updateView();
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_REMOVE_BOOK)
            }
    )
    public void hadRemoveBook(BookShelfBean value) {
        if(bookShelfs!=null){
            for(int i=0;i<bookShelfs.size();i++){
                if(bookShelfs.get(i).getNoteUrl().equals(value.getNoteUrl())){
                    bookShelfs.remove(i);
                    break;
                }
            }
        }
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl()))
                || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = false;
            if (null != searchBook) {
                searchBook.setAdd(false);
            }
            mView.updateView();
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK),
            }
    )
    public void hadBook(BookShelfBean value) {
        bookShelfs.add(value);
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl())) || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(true);
            }
            mView.updateView();
        }
    }
}
