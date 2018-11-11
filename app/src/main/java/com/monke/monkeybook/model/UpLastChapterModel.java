package com.monke.monkeybook.model;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.source.My716;
import com.monke.monkeybook.utils.RxUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 更新换源列表里最新章节
 */
public class UpLastChapterModel {
    private static UpLastChapterModel model;
    private CompositeDisposable compositeDisposable;
    private ExecutorService executorService;
    private Scheduler scheduler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<SearchBookBean> searchBookBeanList;

    public static UpLastChapterModel getInstance() {
        if (model == null) {
            model = new UpLastChapterModel();
        }
        return model;
    }

    private UpLastChapterModel() {
        executorService = Executors.newFixedThreadPool(5);
        scheduler = Schedulers.from(executorService);
        compositeDisposable = new CompositeDisposable();
    }

    public void startUpdate() {
        if (compositeDisposable.size() > 0) return;
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            List<BookShelfBean> bookShelfBeans = BookshelfHelp.getAllBook();
            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                if (!Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG)) {
                    e.onNext(bookShelfBean);
                }
            }
            e.onComplete();
        }).flatMap(this::findSearchBookBean)
                .flatMap(this::toBookshelf)
                .flatMap(this::getChapterList)
                .flatMap(this::saveSearchBookBean)
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .subscribe(new Observer<SearchBookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(SearchBookBean searchBookBean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        stopUp();
                    }

                    @Override
                    public void onComplete() {
                        stopUp();
                    }
                });
    }

    public synchronized void startUpdate(List<SearchBookBean> beanList) {
        this.searchBookBeanList = beanList;
    }

    private synchronized void startUpdate(SearchBookBean searchBookBean) {
        toBookshelf(searchBookBean)
                .flatMap(this::getChapterList)
                .flatMap(this::saveSearchBookBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .subscribe(new Observer<SearchBookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                        handler.postDelayed(() -> {
                            if (!d.isDisposed()) {
                                d.dispose();
                            }
                        }, 20 * 1000);
                    }

                    @Override
                    public void onNext(SearchBookBean searchBookBean) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void stopUp() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
    }

    public void onDestroy() {
        stopUp();
        executorService.shutdownNow();
    }

    private Observable<SearchBookBean> findSearchBookBean(BookShelfBean bookShelf) {
        return Observable.create(e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookShelf.getBookInfoBean().getName())).list();
            for (SearchBookBean searchBookBean : searchBookBeans) {
                long count = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                        .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(searchBookBean.getTag())).count();
                if (count == 0 && !Objects.equals(searchBookBean.getTag(), My716.TAG)) {
                    DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().delete(searchBookBean);
                } else if (System.currentTimeMillis() - searchBookBean.getAddTime() > 1000 * 60 * 60) {
                    e.onNext(searchBookBean);
                }
            }
            e.onComplete();
        });
    }

    private Observable<BookShelfBean> toBookshelf(SearchBookBean searchBookBean) {
        return Observable.create(e -> {
            BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBookBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private Observable<BookShelfBean> getChapterList(BookShelfBean bookShelfBean) {
        if (TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterUrl())) {
            return WebBookModel.getInstance().getBookInfo(bookShelfBean)
                    .flatMap(bookShelf -> WebBookModel.getInstance().getChapterList(bookShelf));
        } else {
            return WebBookModel.getInstance().getChapterList(bookShelfBean);
        }
    }

    private Observable<SearchBookBean> saveSearchBookBean(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            SearchBookBean searchBookBean = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl()))
                    .unique();
            if (searchBookBean != null) {
                searchBookBean.setLastChapter(bookShelfBean.getLastChapterName());
                searchBookBean.setAddTime(System.currentTimeMillis());
                DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);
                e.onNext(searchBookBean);
            }
            e.onComplete();
        });
    }
}
