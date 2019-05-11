package com.kunfei.bookshelf.model.content;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.TimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class Debug {
    public static String SOURCE_DEBUG_TAG;
    @SuppressLint("ConstantLocale")
    private static final DateFormat DEBUG_TIME_FORMAT = new SimpleDateFormat("[mm:ss.SSS]", Locale.getDefault());
    private static long startTime;

    private static String getDoTime() {
        return TimeUtils.millis2String(System.currentTimeMillis() - startTime, DEBUG_TIME_FORMAT);
    }

    static void printLog(String tag, String msg) {
        printLog(tag, msg, true);
    }

    static void printLog(String tag, String msg, boolean print) {
        if (print && Objects.equals(SOURCE_DEBUG_TAG, tag)) {
            msg = String.format("%s %s", getDoTime(), msg);
            RxBus.get().post(RxBusTag.PRINT_DEBUG_LOG, msg);
        }
    }

    public static void newDebug(String tag, String key, @NonNull CompositeDisposable compositeDisposable, @NonNull Callback callback) {
        if (TextUtils.isEmpty(tag)) {
            callback.printError("书源url不能为空");
            return;
        }
        key = StringUtils.trim(key);
        if (TextUtils.isEmpty(key)) {
            callback.printError("关键字不能为空");
            return;
        }
        new Debug(tag, key, compositeDisposable, callback);
    }

    private Callback callback;
    private CompositeDisposable compositeDisposable;

    private Debug(String tag, String key, CompositeDisposable compositeDisposable, Callback callback) {
        UpLastChapterModel.destroy();
        startTime = System.currentTimeMillis();
        SOURCE_DEBUG_TAG = tag;
        this.callback = callback;
        this.compositeDisposable = compositeDisposable;
        if (NetworkUtil.isUrl(key)) {
            printLog(String.format("%s %s", getDoTime(), "≡关键字为Url"));
            BookShelfBean bookShelfBean = new BookShelfBean();
            bookShelfBean.setTag(Debug.SOURCE_DEBUG_TAG);
            bookShelfBean.setNoteUrl(key);
            bookShelfBean.setDurChapter(0);
            bookShelfBean.setGroup(0);
            bookShelfBean.setDurChapterPage(0);
            bookShelfBean.setFinalDate(System.currentTimeMillis());
            bookInfoDebug(bookShelfBean);
        } else {
            searchDebug(key);
        }
    }

    private void searchDebug(String key) {
        printLog(String.format("%s %s", getDoTime(), "≡开始搜索指定关键字"));
        WebBookModel.getInstance().searchBook(key, 1, Debug.SOURCE_DEBUG_TAG)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        SearchBookBean searchBookBean = searchBookBeans.get(0);
                        if (!TextUtils.isEmpty(searchBookBean.getNoteUrl())) {
                            bookInfoDebug(BookshelfHelp.getBookFromSearchBook(searchBookBean));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookInfoDebug(BookShelfBean bookShelfBean) {
        printLog(String.format("\n%s ≡开始获取详情页", getDoTime()));
        WebBookModel.getInstance().getBookInfo(bookShelfBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        bookChapterListDebug(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookChapterListDebug(BookShelfBean bookShelfBean) {
        printLog(String.format("\n%s ≡开始获取目录页", getDoTime()));
        WebBookModel.getInstance().getChapterList(bookShelfBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelfBean.getChapterList().size() > 0) {
                            ChapterListBean chapterListBean = bookShelfBean.getChapter(0);
                            bookContentDebug(chapterListBean, bookShelfBean.getBookInfoBean().getName());
                        } else {
                            printError("获取到的目录为空");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookContentDebug(ChapterListBean chapterListBean, String bookName) {
        printLog(String.format("\n%s ≡开始获取正文页", getDoTime()));
        WebBookModel.getInstance().getBookContent(chapterListBean, bookName)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<BookContentBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookContentBean bookContentBean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        finish();
                    }
                });
    }

    private void printLog(String log) {
        if (callback != null) {
            callback.printLog(log);
        }
    }

    private void printError(String msg) {
        if (callback != null) {
            callback.printError(String.format("%s └%s", getDoTime(), msg));
        }
    }

    private void finish() {
        if (callback != null) {
            callback.finish();
        }
    }

    public interface Callback {
        void printLog(String msg);

        void printError(String msg);

        void finish();
    }
}