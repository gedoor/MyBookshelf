//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.bean.ReadBookContentBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookContentBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookShelf;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.ImportBookModelImpl;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.impl.IReadBookPresenter;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.utils.PremissionCheck;
import com.monke.monkeybook.view.impl.IReadBookView;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ReadBookPresenterImpl extends BasePresenterImpl<IReadBookView> implements IReadBookPresenter {
    public final static int OPEN_FROM_OTHER = 0;
    public final static int OPEN_FROM_APP = 1;

    private Boolean isAdd = false; //判断是否已经添加进书架
    private int open_from;
    private BookShelfBean bookShelf;

    private int pageLineCount = 5;   //假设5行一页
    private int pageWidth;

    private int numberOfRetries = 0;

    public ReadBookPresenterImpl() {

    }

    @Override
    public void initData(Activity activity) {
        Intent intent = activity.getIntent();
        open_from = intent.getIntExtra("from", OPEN_FROM_OTHER);
        if (open_from == OPEN_FROM_APP) {
            String noteUrl = mView.getNoteUrl();
            if (noteUrl != null && !noteUrl.isEmpty()) {
                bookShelf = BookShelf.getBook(noteUrl);
            }
            if (bookShelf == null) {
                String key = intent.getStringExtra("data_key");
                bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
                BitIntentDataManager.getInstance().cleanData(key);
            }
            if (bookShelf == null) {
                mView.finish();
                return;
            }
            if (!bookShelf.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                mView.showOnLineView();
            }
            checkInShelf();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PremissionCheck.checkPremission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //申请权限
                activity.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x11);
            } else {
                openBookFromOther(activity);
            }
        }
    }

    @Override
    public void initContent() {
        mView.initContentSuccess(bookShelf.getDurChapter(), bookShelf.getChapterListSize(), bookShelf.getDurChapterPage());
    }

    @Override
    public void loadContent(final BookContentView bookContentView, final long bookTag, final int chapterIndex, int pageIndex) {
        //载入正文
        if (null != bookShelf && bookShelf.getChapterListSize() > 0) {
            if (null != bookShelf.getChapterList(chapterIndex).getBookContentBean()
                    && null != bookShelf.getChapterList(chapterIndex).getBookContentBean().getDurChapterContent()) {
                if (bookShelf.getChapterList(chapterIndex).getBookContentBean().getLineSize() == mView.getPaint().getTextSize()
                        && bookShelf.getChapterList(chapterIndex).getBookContentBean().getLineContent().size() > 0) {
                    //已有数据
                    int tempCount = (int) Math.ceil(bookShelf.getChapterList(chapterIndex)
                            .getBookContentBean().getLineContent().size() * 1.0 / pageLineCount) - 1;

                    if (pageIndex == BookContentView.DurPageIndexBegin) {
                        pageIndex = 0;
                    } else if (pageIndex == BookContentView.DurPageIndexEnd) {
                        pageIndex = tempCount;
                    } else {
                        if (pageIndex >= tempCount) {
                            pageIndex = tempCount;
                        }
                    }
                    int start = pageIndex * pageLineCount;
                    int end = pageIndex == tempCount ? bookShelf.getChapterList(chapterIndex).getBookContentBean().getLineContent().size() : start + pageLineCount;
                    if (bookContentView != null && bookTag == bookContentView.getQTag()) {
                        bookContentView.updateData(bookTag,
                                bookShelf.getChapterList(chapterIndex).getDurChapterName(),
                                bookShelf.getChapterList(chapterIndex).getBookContentBean().getLineContent().subList(start, end),
                                chapterIndex,
                                bookShelf.getChapterListSize(),
                                pageIndex,
                                tempCount + 1);
                    }
                } else {
                    //有元数据  重新分行
                    bookShelf.getChapterList(chapterIndex).getBookContentBean()
                            .setLineSize(mView.getPaint().getTextSize());
                    final int finalPageIndex = pageIndex;
                    SeparateParagraphToLines(bookShelf.getChapterList(chapterIndex).getBookContentBean().getDurChapterContent(), chapterIndex)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                            .subscribe(new SimpleObserver<List<String>>() {
                                @Override
                                public void onNext(List<String> value) {
                                    bookShelf.getChapterList(chapterIndex).getBookContentBean().setLineContent(value);
                                    loadContent(bookContentView, bookTag, chapterIndex, finalPageIndex);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (bookContentView != null && bookTag == bookContentView.getQTag())
                                        bookContentView.loadError();
                                }
                            });
                }
            } else {
                final int finalPageIndex1 = pageIndex;
                Observable.create((ObservableOnSubscribe<ReadBookContentBean>) e -> {
                    List<BookContentBean> tempList = DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().queryBuilder()
                            .where(BookContentBeanDao.Properties.DurChapterUrl.eq(bookShelf.getChapterList(chapterIndex).getDurChapterUrl())).build().list();
                    //加载下一章节
                    LoadNextChapter(chapterIndex);
                    e.onNext(new ReadBookContentBean(tempList == null ? new ArrayList<>() : tempList, finalPageIndex1));
                    e.onComplete();
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.newThread())
                        .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new SimpleObserver<ReadBookContentBean>() {
                            @Override
                            public void onNext(ReadBookContentBean tempList) {
                                if (tempList.getBookContentList() != null && tempList.getBookContentList().size() > 0
                                        && tempList.getBookContentList().get(0).getDurChapterContent() != null) {
                                    bookShelf.getChapterList(chapterIndex).setBookContentBean(tempList.getBookContentList().get(0));
                                    loadContent(bookContentView, bookTag, chapterIndex, tempList.getPageIndex());
                                } else {
                                    final int finalPageIndex1 = tempList.getPageIndex();
                                    //网络获取正文
                                    WebBookModelImpl.getInstance().getBookContent(bookShelf.getChapterList(chapterIndex).
                                            getDurChapterUrl(), chapterIndex, bookShelf.getTag()).map(bookContentBean -> {
                                        if (bookContentBean.getRight()) {
                                            bookContentBean.setNoteUrl(bookShelf.getNoteUrl());
                                            DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().insertOrReplace(bookContentBean);
                                            bookShelf.getChapterList(chapterIndex).setHasCache(true);
                                            DbHelper.getInstance().getmDaoSession().getChapterListBeanDao()
                                                    .update(bookShelf.getChapterList(chapterIndex));
                                        }
                                        return bookContentBean;
                                    })
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.newThread())
                                            .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                                            .subscribe(new SimpleObserver<BookContentBean>() {
                                                @Override
                                                public void onNext(BookContentBean value) {
                                                    numberOfRetries = 0;
                                                    if (value.getDurChapterUrl() != null && value.getDurChapterUrl().length() > 0) {
                                                        bookShelf.getChapterList(chapterIndex).setBookContentBean(value);
                                                        if (bookTag == bookContentView.getQTag())
                                                            loadContent(bookContentView, bookTag, chapterIndex, finalPageIndex1);
                                                    } else {
                                                        if (bookContentView != null && bookTag == bookContentView.getQTag())
                                                            bookContentView.loadError();
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                    if (bookContentView != null && bookTag == bookContentView.getQTag())
                                                        //重试3次
                                                        if (numberOfRetries < 3) {
                                                            numberOfRetries = numberOfRetries + 1;
                                                            loadContent(bookContentView, bookTag, chapterIndex, finalPageIndex1);
                                                        } else {
                                                            numberOfRetries = 0;
                                                            bookContentView.loadError();
                                                        }
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            }
        } else {
            if (bookContentView != null && bookTag == bookContentView.getQTag())
                bookContentView.loadError();
        }
    }

    private void LoadNextChapter(int durChapterIndex) {
        new Thread(() -> {
            int nextIndex = durChapterIndex + 1;
            if (bookShelf.getChapterListSize() > nextIndex) {
                if (bookShelf.getChapterList(nextIndex).getBookContentBean() == null) {
                    List<BookContentBean> tempList = DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().queryBuilder()
                            .where(BookContentBeanDao.Properties.DurChapterUrl.eq(bookShelf.getChapterList(nextIndex).getDurChapterUrl()))
                            .build().list();
                    if (tempList == null) {
                        WebBookModelImpl.getInstance().getBookContent(bookShelf.getChapterList(nextIndex).
                                getDurChapterUrl(), nextIndex, bookShelf.getTag()).map(bookContentBean -> {
                            if (bookContentBean.getRight()) {
                                bookContentBean.setNoteUrl(bookShelf.getNoteUrl());
                                DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().insertOrReplace(bookContentBean);
                                bookShelf.getChapterList(nextIndex).setHasCache(true);
                                DbHelper.getInstance().getmDaoSession().getChapterListBeanDao()
                                        .update(bookShelf.getChapterList(nextIndex));
                            }
                            return bookContentBean;
                        })
                                .subscribe();
                    }
                }
            }
        }).start();
    }

    @Override
    public void updateProgress(int chapterIndex, int pageIndex) {
        bookShelf.setDurChapter(chapterIndex);
        bookShelf.setDurChapterPage(pageIndex);
    }

    @Override
    public void saveProgress() {
        if (bookShelf != null && isAdd) {
            Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
                bookShelf.setFinalDate(System.currentTimeMillis());
                BookShelf.saveBookToShelf(bookShelf);
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

    @Override
    public String getChapterTitle(int chapterIndex) {
        if (bookShelf.getChapterListSize() == 0) {
            return mView.getContext().getString(R.string.no_chapter);
        } else
            return bookShelf.getChapterList(chapterIndex).getDurChapterName();
    }

    /**
     * @param paragraphStr 内容
     * @return 分行内容
     */
    private Observable<List<String>> SeparateParagraphToLines(String paragraphStr, int chapterIndex) {
        return Observable.create(e -> {
            String content = paragraphStr;
            if (!content.startsWith(bookShelf.getChapterList(chapterIndex).getDurChapterName())) {
                content = String.format("%s\r\n%s", bookShelf.getChapterList(chapterIndex).getDurChapterName(), paragraphStr);
            }
            TextPaint mPaint = (TextPaint) mView.getPaint();
            mPaint.setSubpixelText(true);
            Layout tempLayout = new StaticLayout(content, mPaint, pageWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
            List<String> linesData = new ArrayList<>();
            for (int i = 0; i < tempLayout.getLineCount(); i++) {
                linesData.add(content.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
            }
            e.onNext(linesData);
            e.onComplete();
        });
    }

    @Override
    public void setPageLineCount(int pageLineCount) {
        this.pageLineCount = pageLineCount;
    }

    @Override
    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    @Override
    public void openBookFromOther(Activity activity) {
        //APP外部打开
        Uri uri = activity.getIntent().getData();
        mView.showLoading("文本导入中...");
        getRealFilePath(activity, uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String value) {
                        ImportBookModelImpl.getInstance().importBook(new File(value))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(new SimpleObserver<LocBookShelfBean>() {
                                    @Override
                                    public void onNext(LocBookShelfBean value) {
                                        if (value.getNew())
                                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                                        bookShelf = value.getBookShelfBean();
                                        mView.dismissLoading();
                                        checkInShelf();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        mView.dismissLoading();
                                        mView.loadLocationBookError();
                                        Toast.makeText(MApplication.getInstance(), "文本打开失败！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.dismissLoading();
                        mView.loadLocationBookError();
                        Toast.makeText(MApplication.getInstance(), "文本打开失败！", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void addDownload(int start, int end) {
        addToShelf(() -> {
            Intent intent = new Intent(mView.getContext(), DownloadService.class);
            intent.setAction("addDownload");
            intent.putExtra("noteUrl", bookShelf.getNoteUrl());
            intent.putExtra("start", start);
            intent.putExtra("end", end);
            mView.getContext().startService(intent);
        });
    }

    /**
     * 换源
     */
    @Override
    public void changeBookSource(SearchBookBean searchBook) {
        BookShelfBean bookShelfBean = new BookShelfBean();
        bookShelfBean.setTag(searchBook.getTag());
        bookShelfBean.setNoteUrl(searchBook.getNoteUrl());
        bookShelfBean.setFinalDate(System.currentTimeMillis());
        bookShelfBean.setDurChapter(0);
        bookShelfBean.setDurChapterPage(0);
        BookInfoBean bookInfo = new BookInfoBean();
        bookInfo.setNoteUrl(searchBook.getNoteUrl());
        bookInfo.setAuthor(searchBook.getAuthor());
        bookInfo.setCoverUrl(searchBook.getCoverUrl());
        bookInfo.setName(searchBook.getName());
        bookInfo.setTag(searchBook.getTag());
        bookInfo.setOrigin(searchBook.getOrigin());
        bookShelfBean.setBookInfoBean(bookInfo);
        WebBookModelImpl.getInstance().getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelf.getDurChapter() > bookShelfBean.getChapterListSize() - 1) {
                            bookShelfBean.setDurChapter(bookShelfBean.getChapterListSize() - 1);
                        } else {
                            bookShelfBean.setDurChapter(bookShelf.getDurChapter());
                        }
                        saveChangedBook(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.getCsvBook().setInitData(bookShelf.getDurChapter(),
                                bookShelf.getChapterListSize(),
                                bookShelf.getDurChapterPage());
                        Toast.makeText(MApplication.getInstance(), "换源失败！" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveChangedBook(BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookShelf.removeFromBookShelf(bookShelf);
            BookShelf.saveBookToShelf(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        bookShelf = value;
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                        mView.getCsvBook().setInitData(bookShelf.getDurChapter(),
                                bookShelf.getChapterListSize(),
                                BookContentView.DurPageIndexBegin);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.getCsvBook().setInitData(bookShelf.getDurChapter(),
                                bookShelf.getChapterListSize(),
                                bookShelf.getDurChapterPage());
                        Toast.makeText(MApplication.getInstance(), "换源失败！", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void detachView() {
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
    public void setBookshelf(BookShelfBean bookshelf) {
        this.bookShelf = bookshelf;
    }

    private void checkInShelf() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            List<BookShelfBean> temp = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().where(BookShelfBeanDao.Properties.NoteUrl.eq(bookShelf.getNoteUrl())).build().list();
            isAdd = !(temp == null || temp.size() == 0);
            e.onNext(isAdd);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        mView.initPop();
                        mView.setHpbReadProgressMax(0);
                        mView.startLoadingBook();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void addToShelf(final OnAddListener addListener) {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelf.getChapterList());
                DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelf.getBookInfoBean());
                //网络数据获取成功  存入BookShelf表数据库
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                isAdd = true;
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Object>() {
                        @Override
                        public void onNext(Object value) {
                            if (addListener != null)
                                addListener.addSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    @Override
    public void removeFromShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookShelf.removeFromBookShelf(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                            mView.finish();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    public Boolean getAdd() {
        return isAdd;
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

    public interface OnAddListener {
        void addSuccess();
    }
}
