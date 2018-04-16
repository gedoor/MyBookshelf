//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.luhuiguo.chinese.ChineseUtils;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.bean.ReadBookContentBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookContentBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.ImportBookModelImpl;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.impl.IReadBookPresenter;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.view.impl.IReadBookView;
import com.monke.monkeybook.widget.ContentTextView;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.greendao.rx.RxQuery;

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
    private double textHeight = 0;//行高

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
                bookShelf = BookshelfHelp.getBook(noteUrl);
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
            mView.openBookFromOther();
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
                if (bookShelf.getChapterList(chapterIndex).getBookContentBean().getLineSize() == mView.getPaint().getTextSize()//字体大小改变
                        && bookShelf.getChapterList(chapterIndex).getBookContentBean().getLineContent() != null//行内容不为空
                        && bookShelf.getChapterList(chapterIndex).getBookContentBean().getLineContent().size() > 0
                        && textHeight == bookContentView.getTextHeigth()) {//行内容Size>0
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
                                    textHeight = bookContentView.getTextHeigth();
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
                    e.onNext(new ReadBookContentBean(tempList == null ? new ArrayList<>() : tempList, finalPageIndex1));
                    e.onComplete();
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new SimpleObserver<ReadBookContentBean>() {
                            @Override
                            public void onNext(ReadBookContentBean tempList) {
                                if (tempList.getBookContentList() != null && tempList.getBookContentList().size() > 0
                                        && tempList.getBookContentList().get(0).getDurChapterContent() != null) {
                                    bookShelf.getChapterList(chapterIndex).setBookContentBean(tempList.getBookContentList().get(0));
                                    loadContent(bookContentView, bookTag, chapterIndex, tempList.getPageIndex());
                                    //加载下一章节
                                    LoadNextChapter(chapterIndex);
                                } else {
                                    final int finalPageIndex1 = tempList.getPageIndex();
                                    //网络获取正文
                                    WebBookModelImpl.getInstance().getBookContent(bookShelf.getChapterList(chapterIndex).getDurChapterUrl(), chapterIndex, bookShelf.getTag())
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

    /**
     * 预加载下一章节
     */
    private void LoadNextChapter(int durChapterIndex) {
        int nextIndex = durChapterIndex + 1;
        if (bookShelf.getChapterListSize() > nextIndex && bookShelf.getChapterList(nextIndex).getBookContentBean() == null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookContentBean bookContentBean = DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().queryBuilder()
                        .where(BookContentBeanDao.Properties.DurChapterUrl.eq(bookShelf.getChapterList(nextIndex).getDurChapterUrl()))
                        .build().unique();
                if (bookContentBean == null) {
                    e.onNext(true);
                } else {
                    e.onNext(false);
                }
                e.onComplete();
            }).observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            if (aBoolean) {
                                WebBookModelImpl.getInstance().getBookContent(bookShelf.getChapterList(nextIndex).getDurChapterUrl(), nextIndex, bookShelf.getTag())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.newThread())
                                        .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                                        .subscribe();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });

        }
    }



    @Override
    public void updateProgress(int chapterIndex, int pageIndex) {
        bookShelf.setDurChapter(chapterIndex);
        bookShelf.setDurChapterPage(pageIndex);
        saveProgress();
    }

    @Override
    public void saveProgress() {
        if (bookShelf != null && isAdd) {
            Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
                bookShelf.setFinalDate(System.currentTimeMillis());
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
            content = addTitle(content, bookShelf.getChapterList(chapterIndex).getDurChapterName());
            content = replaceContent(content);
            content = toTraditional(content);

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

    /**
     * 转繁体
     */
    private String toTraditional(String content) {
        if (MApplication.getInstance().getSharedPreferences("CONFIG", 0).getBoolean("textConvert", false)) {
            content = ChineseUtils.toTraditional(content);
        }
        return content;
    }

    /**
     * 添加标题
     */
    private String addTitle(String content, String chapterName) {
        if (MApplication.getInstance().getSharedPreferences("CONFIG", 0).getBoolean("showTitle", true)) {
            if (!content.startsWith(String.format("\u3000\u3000%s", chapterName))
                    && !content.startsWith(chapterName)) {
                content = String.format("%s\r\n%s", chapterName, content);
            }
        }
        return content;
    }

    /**
     * 替换净化
     */
    private String replaceContent(String content) {
        String allLine[] = content.split("\r\n\u3000\u3000");
        //替换
        if (ReplaceRuleManage.getEnabled() != null && ReplaceRuleManage.getEnabled().size() > 0) {
            StringBuilder contentBuilder = new StringBuilder();
            for (String line : allLine) {
                for (ReplaceRuleBean replaceRule : ReplaceRuleManage.getEnabled()) {
                    try {
                        line = line.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                if (line.length() > 0) {
                    if (contentBuilder.length() == 0) {
                        contentBuilder.append(line);
                    } else {
                        contentBuilder.append("\r\n").append("\u3000\u3000").append(line);
                    }
                }
            }
            content = contentBuilder.toString();
            for (ReplaceRuleBean replaceRule : ReplaceRuleManage.getEnabled()) {
                if (replaceRule.getRegex().contains("\\n")) {
                    try {
                        content = content.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return content;
    }

    /**
     * 设置每页行数
     */
    @Override
    public void setPageLineCount(int pageLineCount) {
        this.pageLineCount = pageLineCount;
    }

    @Override
    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    /**
     * APP外部打开
     */
    @Override
    public void openBookFromOther(Activity activity) {
        Uri uri = activity.getIntent().getData();
        mView.showLoading("文本导入中...");
        getRealFilePath(activity, uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
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
        BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBook);
        bookShelfBean.setSerialNumber(bookShelf.getSerialNumber());
        WebBookModelImpl.getInstance().getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelf.getDurChapter() > bookShelfBean.getChapterListSize() - 1) {
                            bookShelfBean.setDurChapter(bookShelfBean.getChapterListSize() - 1);
                        } else {
                            bookShelfBean.setDurChapter(bookShelf.getDurChapter());
                        }
                        bookShelfBean.setHasUpdate(false);
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

    /**
     * 保存换源后book
     */
    private void saveChangedBook(BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookshelfHelp.removeFromBookShelf(bookShelf);
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
                        mView.initChapterList();
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
                        Toast.makeText(MApplication.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                BookshelfHelp.removeFromBookShelf(bookShelf);
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

    /////////////////////////////////////////////////

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
            tags = {@Tag(RxBusTag.CHAPTER_CHANGE)})
    public void chapterChange(ChapterListBean chapterListBean) {
        if (bookShelf.getNoteUrl().equals(chapterListBean.getNoteUrl())) {
            mView.chapterChange(chapterListBean);
        }
    }
}
