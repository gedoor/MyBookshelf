//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.presenter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.BitIntentDataManager;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.DownloadBookBean;
import com.kunfei.bookshelf.bean.LocBookShelfBean;
import com.kunfei.bookshelf.bean.OpenChapterBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.ImportBookModel;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.presenter.contract.ReadBookContract;
import com.kunfei.bookshelf.service.DownloadService;
import com.kunfei.bookshelf.service.ReadAloudService;
import com.kunfei.bookshelf.widget.modialog.ChangeSourceView;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ReadBookPresenter extends BasePresenterImpl<ReadBookContract.View> implements ReadBookContract.Presenter {
    public final static int OPEN_FROM_OTHER = 0;
    public final static int OPEN_FROM_APP = 1;

    private int open_from;
    private BookShelfBean bookShelf;
    private BookSourceBean bookSourceBean;

    @Override
    public void initData(Activity activity) {
        Intent intent = activity.getIntent();
        open_from = intent.getData() != null ? OPEN_FROM_OTHER : OPEN_FROM_APP;
        open_from = intent.getIntExtra("openFrom", open_from);
        if (open_from == OPEN_FROM_APP) {
            loadBook(intent);
        } else {
            mView.openBookFromOther();
            mView.upMenu();
        }
    }

    private void loadBook(Intent intent) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            if (bookShelf == null) {
                String key = intent.getStringExtra("data_key");
                bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
                BitIntentDataManager.getInstance().cleanData(key);
            }
            if (bookShelf == null && !TextUtils.isEmpty(mView.getNoteUrl())) {
                bookShelf = BookshelfHelp.getBook(mView.getNoteUrl());
            }
            if (bookShelf == null) {
                List<BookShelfBean> beans = BookshelfHelp.getAllBook();
                if (beans != null && beans.size() > 0) {
                    bookShelf = beans.get(0);
                }
            }
            if (bookShelf != null) {
                bookShelf.getBookInfoBean().setChapterList(BookshelfHelp.getChapterList(bookShelf.getNoteUrl()));
                bookShelf.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName()));
                mView.setAdd(BookshelfHelp.isInBookShelf(bookShelf.getNoteUrl()));
                if (!bookShelf.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                    bookSourceBean = BookSourceManager.getBookSourceByUrl(bookShelf.getTag());
                }
            }
            e.onNext(bookShelf);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelf == null || TextUtils.isEmpty(bookShelf.getBookInfoBean().getName())) {
                            mView.finish();
                        } else {
                            mView.startLoadingBook();
                            mView.upMenu();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.finish();
                    }
                });
    }

    /**
     * 禁用当前书源
     */
    public void disableDurBookSource() {
        try {
            if (bookSourceBean != null) {
                bookSourceBean.addGroup("禁用");
                DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
                BookSourceManager.refreshBookSource();
                mView.toast("已禁用" + bookSourceBean.getBookSourceName());
            }
        } catch (Exception e) {
            Log.e("MonkBook", e.getLocalizedMessage() + "\n" + e.getMessage());
        }
    }

    @Override
    public BookSourceBean getBookSource() {
        return bookSourceBean;
    }

    @Override
    public void saveProgress() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
                bookShelf.setFinalDate(System.currentTimeMillis());
                bookShelf.upDurChapterName();
                bookShelf.setHasUpdate(false);
                BookshelfHelp.saveBookToShelf(bookShelf);
                e.onNext(bookShelf);
                e.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .subscribe(new SimpleObserver<BookShelfBean>() {
                        @Override
                        public void onNext(BookShelfBean value) {
                            RxBus.get().post(RxBusTag.UPDATE_BOOK_PROGRESS, bookShelf);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    /**
     * APP外部打开
     */
    @Override
    public void openBookFromOther(Activity activity) {
        Uri uri = activity.getIntent().getData();
        getRealFilePath(activity, uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String value) {
                        ImportBookModel.getInstance().importBook(new File(value))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(new SimpleObserver<LocBookShelfBean>() {
                                    @Override
                                    public void onNext(LocBookShelfBean value) {
                                        if (value.getNew())
                                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                                        bookShelf = value.getBookShelfBean();
                                        mView.setAdd(BookshelfHelp.isInBookShelf(bookShelf.getNoteUrl()));
                                        mView.startLoadingBook();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        mView.toast("文本打开失败！");
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.toast("文本打开失败！");
                    }
                });
    }

    /**
     * 下载
     */
    @Override
    public void addDownload(int start, int end) {
        addToShelf(() -> {
            DownloadBookBean downloadBook = new DownloadBookBean();
            downloadBook.setName(bookShelf.getBookInfoBean().getName());
            downloadBook.setNoteUrl(bookShelf.getNoteUrl());
            downloadBook.setCoverUrl(bookShelf.getBookInfoBean().getCoverUrl());
            downloadBook.setStart(start);
            downloadBook.setEnd(end);
            downloadBook.setFinalDate(System.currentTimeMillis());
            DownloadService.addDownload(mView.getContext(), downloadBook);
        });
    }

    /**
     * 换源
     */
    @Override
    public void changeBookSource(SearchBookBean searchBook) {
        BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBook);
        bookShelfBean.setSerialNumber(bookShelf.getSerialNumber());
        bookShelfBean.setLastChapterName(bookShelf.getLastChapterName());
        bookShelfBean.setDurChapterName(bookShelf.getDurChapterName());
        bookShelfBean.setDurChapter(bookShelf.getDurChapter());
        bookShelfBean.setDurChapterPage(bookShelf.getDurChapterPage());
        WebBookModel.getInstance().getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModel.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        saveChangedBook(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("换源失败！" + e.getMessage());
                        mView.changeSourceFinish(null);
                    }
                });
    }

    @Override
    public void saveBookmark(BookmarkBean bookmarkBean) {
        Observable.create((ObservableOnSubscribe<BookmarkBean>) e -> {
            BookshelfHelp.saveBookmark(bookmarkBean);
            bookShelf.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(bookmarkBean.getBookName()));
            e.onNext(bookmarkBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delBookmark(BookmarkBean bookmarkBean) {
        Observable.create((ObservableOnSubscribe<BookmarkBean>) e -> {
            BookshelfHelp.delBookmark(bookmarkBean);
            bookShelf.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(bookmarkBean.getBookName()));
            e.onNext(bookmarkBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * 保存换源后book
     */
    private void saveChangedBook(BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            bookShelfBean.setHasUpdate(false);
            bookShelfBean.setCustomCoverPath(bookShelf.getCustomCoverPath());
            bookShelfBean.setDurChapter(BookshelfHelp.getDurChapter(bookShelf, bookShelfBean));
            bookShelfBean.setDurChapterName(bookShelfBean.getChapter(bookShelfBean.getDurChapter()).getDurChapterName());
            bookShelfBean.setGroup(bookShelf.getGroup());
            BookshelfHelp.removeFromBookShelf(bookShelf, true);
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                        bookShelf = value;
                        mView.changeSourceFinish(bookShelf);
                        String tag = bookShelf.getTag();
                        try {
                            long currentTime = System.currentTimeMillis();
                            String bookName = bookShelf.getBookInfoBean().getName();
                            BookSourceBean bookSourceBean = BookshelfHelp.getBookSourceByTag(tag);
                            if (ChangeSourceView.savedSource.getBookSource() != null && currentTime - ChangeSourceView.savedSource.getSaveTime() < 60000 && ChangeSourceView.savedSource.getBookName().equals(bookName))
                                ChangeSourceView.savedSource.getBookSource().increaseWeight(-450);
                            BookshelfHelp.saveBookSource(ChangeSourceView.savedSource.getBookSource());
                            ChangeSourceView.savedSource.setBookName(bookName);
                            ChangeSourceView.savedSource.setSaveTime(currentTime);
                            ChangeSourceView.savedSource.setBookSource(bookSourceBean);
                            bookSourceBean.increaseWeightBySelection();
                            BookshelfHelp.saveBookSource(bookSourceBean);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.toast(e.getMessage());
                        mView.changeSourceFinish(null);
                    }
                });
    }

    @Override
    public int getOpen_from() {
        return open_from;
    }

    @Override
    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void addToShelf(final OnAddListener addListener) {
        if (bookShelf != null) {
            AsyncTask.execute(() -> {
                BookshelfHelp.saveBookToShelf(bookShelf);
                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                mView.setAdd(true);
                if (addListener != null) {
                    addListener.addSuccess();
                }
            });
        }
    }

    @Override
    public void removeFromShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.removeFromBookShelf(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                            mView.setAdd(true);
                            mView.finish();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    private Observable<String> getRealFilePath(final Context context, final Uri uri) {
        return Observable.create(e -> {
            String data = "";
            if (null != uri) {
                final String scheme = uri.getScheme();
                if (scheme == null)
                    data = uri.getPath();
                else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                    data = uri.getPath();
                } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                            if (index > -1) {
                                data = cursor.getString(index);
                            }
                        }
                        cursor.close();
                    }

                    if ((data == null || data.length() <= 0) && uri.getPath() != null && uri.getPath().contains("/storage/emulated/")) {
                        data = uri.getPath().substring(uri.getPath().indexOf("/storage/emulated/"));
                    }
                }
            }
            e.onNext(data == null ? "" : data);
            e.onComplete();
        });
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    /////////////////////////////////////////////////

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    /////////////////////RxBus////////////////////////

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.MEDIA_BUTTON)})
    public void onMediaButton(String command) {
        if (bookShelf != null) {
            mView.onMediaButton();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UPDATE_READ)})
    public void updateRead(Boolean recreate) {
        mView.refresh(recreate);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.ALOUD_STATE)})
    public void upAloudState(ReadAloudService.Status state) {
        mView.upAloudState(state);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.ALOUD_TIMER)})
    public void upAloudTimer(String timer) {
        mView.upAloudTimer(timer);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SKIP_TO_CHAPTER)})
    public void skipToChapter(OpenChapterBean openChapterBean) {
        mView.skipToChapter(openChapterBean.getChapterIndex(), openChapterBean.getPageIndex());
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.OPEN_BOOK_MARK)})
    public void openBookmark(BookmarkBean bookmarkBean) {
        mView.showBookmark(bookmarkBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.READ_ALOUD_START)})
    public void readAloudStart(Integer start) {
        mView.readAloudStart(start);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.READ_ALOUD_NUMBER)})
    public void readAloudLength(Integer start) {
        mView.readAloudLength(start);
    }

    public interface OnAddListener {
        void addSuccess();
    }

}
