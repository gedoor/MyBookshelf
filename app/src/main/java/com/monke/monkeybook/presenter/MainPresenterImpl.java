//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookShelf;
import com.monke.monkeybook.help.DataBackup;
import com.monke.monkeybook.help.DataRestore;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.impl.IMainPresenter;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.view.impl.IMainView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<IMainView> implements IMainPresenter {
    private int threadsNum = 6;

    private List<BookShelfBean> getAllBookShelf() {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            List<BookInfoBean> temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).limit(1).build().list();
            if (temp != null && temp.size() > 0) {
                BookInfoBean bookInfoBean = temp.get(0);
                bookInfoBean.setChapterList(DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder()
                        .where(ChapterListBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).orderAsc(ChapterListBeanDao.Properties.DurChapterIndex).build().list());
                bookShelfList.get(i).setBookInfoBean(bookInfoBean);
            } else {
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().delete(bookShelfList.get(i));
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    @Override
    public void queryBookShelf(final Boolean needRefresh) {
        if (needRefresh) {
            mView.activityRefreshView();
        }
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfList = getAllBookShelf();
            e.onNext(bookShelfList == null ? new ArrayList<>() : bookShelfList);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        if (null != value) {
                            mView.refreshBookShelf(value);
                            if (needRefresh) {
                                startRefreshBook(value);
                            } else {
                                mView.refreshFinish();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                    }
                });
    }

    @Override
    public void backupData() {
        DataBackup.getInstance().run();
    }

    @Override
    public void restoreData() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (DataRestore.getInstance().run()) {
                e.onNext(true);
            } else {
                e.onNext(false);
            }
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            queryBookShelf(true);
                            mView.onRestore();
                        } else {
                            Toast.makeText(mView.getContext(), R.string.restore_fail, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(mView.getContext(), R.string.restore_fail, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void clearAllContent() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().deleteAll();
            e.onNext(true);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {

                    @Override
                    public void onNext(Boolean aBoolean) {
                        Toast.makeText(mView.getContext(), "删除成功", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "删除失败", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void addBookUrl(String bookUrl) {
        if (TextUtils.isEmpty(bookUrl.trim())) return;
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            URL url = new URL(bookUrl);
            List<BookInfoBean> temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookUrl)).limit(1).build().list();
            if (temp != null && temp.size() > 0) {
                e.onNext(null);
            } else {
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setTag(String.format("%s://%s", url.getProtocol(), url.getHost()));
                bookShelfBean.setNoteUrl(url.toString());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                bookShelfBean.setFinalDate(System.currentTimeMillis());
                e.onNext(bookShelfBean);
            }
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelfBean != null) {
                            getBook(bookShelfBean);
                        } else {
                            Toast.makeText(mView.getContext(), "已在书架中", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "网址格式不对", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getBook(BookShelfBean bookShelfBean) {
        WebBookModelImpl.getInstance()
                .getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .flatMap(this::saveBookToShelfO)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        if (value.getBookInfoBean().getChapterUrl() == null) {
                            Toast.makeText(mView.getContext(), "添加书籍失败", Toast.LENGTH_SHORT).show();
                        } else {
                            //成功   //发送RxBus
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                            Toast.makeText(mView.getContext(), "添加书籍成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "添加书籍失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int refreshIndex;

    private void startRefreshBook(List<BookShelfBean> value) {
        if (value != null && value.size() > 0) {
            mView.setRecyclerMaxProgress(value.size());
            refreshIndex = -1;
            for (int i = 1; i <= threadsNum; i++) {
                refreshBookshelf(value);
            }
        } else {
            mView.refreshFinish();
        }
    }

    private void refreshBookshelf(final List<BookShelfBean> bookShelfBeans) {
        refreshIndex++;
        if (refreshIndex < bookShelfBeans.size()) {
            BookShelfBean bookShelfBean = bookShelfBeans.get(refreshIndex);
            if (!Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG)) {
                WebBookModelImpl.getInstance().getChapterList(bookShelfBean)
                        .flatMap(this::saveBookToShelfO)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<BookShelfBean>() {
                            @Override
                            public void onNext(BookShelfBean bookShelfBean) {
                                if (bookShelfBean.getErrorMsg() != null) {
                                    bookShelfBean.setErrorMsg(null);
                                    Toast.makeText(mView.getContext(), bookShelfBean.getErrorMsg(), Toast.LENGTH_SHORT).show();
                                }
                                mView.refreshRecyclerViewItemAdd();
                                refreshBookshelf(bookShelfBeans);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                mView.refreshRecyclerViewItemAdd();
                                refreshBookshelf(bookShelfBeans);
                            }
                        });
            } else {
                mView.refreshRecyclerViewItemAdd();
                refreshBookshelf(bookShelfBeans);
            }
        } else {
            if (refreshIndex >= bookShelfBeans.size() + threadsNum - 1) {
                mView.refreshFinish();
                queryBookShelf(false);
            }
        }
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            BookShelf.saveBookToShelf(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK), @Tag(RxBusTag.HAD_REMOVE_BOOK), @Tag(RxBusTag.UPDATE_BOOK_PROGRESS)})
    public void hadAddOrRemoveBook(BookShelfBean bookShelfBean) {
        queryBookShelf(false);
    }
}
