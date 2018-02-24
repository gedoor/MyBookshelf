package com.monke.monkeybook.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookShelf;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.impl.IBookDetailPresenter;
import com.monke.monkeybook.view.impl.IBookDetailView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookDetailPresenterImpl extends BasePresenterImpl<IBookDetailView> implements IBookDetailPresenter {
    public final static int FROM_BOOKSHELF = 1;
    public final static int FROM_SEARCH = 2;

    private int openFrom;
    private SearchBookBean searchBook;
    private BookShelfBean bookShelf;
    private Boolean inBookShelf = false;

    private List<BookShelfBean> bookShelfS = Collections.synchronizedList(new ArrayList<BookShelfBean>());   //用来比对搜索的书籍是否已经添加进书架

    public BookDetailPresenterImpl(Intent intent) {
        openFrom = intent.getIntExtra("from", FROM_BOOKSHELF);
        if (openFrom == FROM_BOOKSHELF) {
            String key = intent.getStringExtra("data_key");
            bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
            BitIntentDataManager.getInstance().cleanData(key);
            inBookShelf = true;
            searchBook = new SearchBookBean();
            searchBook.setNoteUrl(bookShelf.getNoteUrl());
            searchBook.setTag(bookShelf.getTag());
        } else {
            searchBook = intent.getParcelableExtra("data");
            inBookShelf = searchBook.getIsAdd();
            bookShelf = new BookShelfBean();
            bookShelf.setNoteUrl(searchBook.getNoteUrl());
            bookShelf.setFinalDate(System.currentTimeMillis());
            bookShelf.setDurChapter(0);
            bookShelf.setDurChapterPage(0);
            bookShelf.setTag(searchBook.getTag());
            BookInfoBean bookInfo = new BookInfoBean();
            bookInfo.setNoteUrl(searchBook.getNoteUrl());
            bookInfo.setAuthor(searchBook.getAuthor());
            bookInfo.setCoverUrl(searchBook.getCoverUrl());
            bookInfo.setName(searchBook.getName());
            bookInfo.setTag(searchBook.getTag());
            bookInfo.setOrigin(searchBook.getOrigin());
            bookShelf.setBookInfoBean(bookInfo);
        }
    }

    public Boolean getInBookShelf() {
        return inBookShelf;
    }

    public int getOpenFrom() {
        return openFrom;
    }

    public SearchBookBean getSearchBook() {
        return searchBook;
    }

    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void getBookShelfInfo() {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            bookShelfS = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().list();
            if (bookShelfS == null) {
                bookShelfS = new ArrayList<>();
            }
            for (int i = 0; i < bookShelfS.size(); i++) {
                if (bookShelfS.get(i).getNoteUrl().equals(bookShelf.getNoteUrl())) {
                    if (openFrom == FROM_BOOKSHELF) {
                        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelf.getBookInfoBean());
                    }
                    inBookShelf = true;
                    break;
                }
            }
            e.onNext(bookShelf);
            e.onComplete();
        })
                .flatMap(bookShelfBean -> WebBookModelImpl.getInstance().getBookInfo(bookShelfBean))
                .flatMap(bookShelfBean -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean))
                .subscribeOn(Schedulers.io())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfResult) {
                        if (openFrom == FROM_BOOKSHELF && bookShelf != null) {
                            int durChapter = bookShelf.getDurChapter();
                            bookShelf = bookShelfResult;
                            bookShelf.setDurChapter(durChapter);
                        } else {
                            bookShelf = bookShelfResult;
                        }
                        mView.updateView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        mView.getBookShelfError();
                    }
                });
    }

    @Override
    public void addToBookShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelf.getChapterList());
                DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelf.getBookInfoBean());
                //网络数据获取成功  存入BookShelf表数据库
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
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
                BookShelf.removeFromBookShelf(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
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

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void hadAddBook(BookShelfBean value) {
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl())) || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setIsAdd(true);
            }
            mView.updateView();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void hadRemoveBook(BookShelfBean value) {
        if (bookShelfS != null) {
            for (int i = 0; i < bookShelfS.size(); i++) {
                if (bookShelfS.get(i).getNoteUrl().equals(value.getNoteUrl())) {
                    bookShelfS.remove(i);
                    break;
                }
            }
        }
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl()))
                || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = false;
            if (null != searchBook) {
                searchBook.setIsAdd(false);
            }
            mView.updateView();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK),})
    public void hadBook(BookShelfBean value) {
        bookShelfS.add(value);
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl())) || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setIsAdd(true);
            }
            mView.updateView();
        }
    }
}
