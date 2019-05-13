package com.kunfei.bookshelf.model.task;

import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.bean.WebChapterBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.kunfei.bookshelf.model.content.BookChapter;

import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AnalyzeNextUrlTask {
    private WebChapterBean webChapterBean;
    private Callback callback;
    private BookShelfBean bookShelfBean;
    private Map<String, String> headerMap;
    private BookChapter bookChapter;

    public AnalyzeNextUrlTask(BookChapter bookChapter, WebChapterBean webChapterBean, BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        this.bookChapter = bookChapter;
        this.webChapterBean = webChapterBean;
        this.bookShelfBean = bookShelfBean;
        this.headerMap = headerMap;
    }

    public AnalyzeNextUrlTask setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void analyzeUrl(AnalyzeUrl analyzeUrl) {
        BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                .flatMap(stringResponse ->
                        bookChapter.analyzeChapterList(stringResponse.body(), bookShelfBean, headerMap))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<List<ChapterListBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        callback.addDisposable(d);
                    }

                    @Override
                    public void onNext(List<ChapterListBean> chapterListBeans) {
                        callback.analyzeFinish(webChapterBean, chapterListBeans);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
    }

    public interface Callback {
        void addDisposable(Disposable disposable);

        void analyzeFinish(WebChapterBean bean, List<ChapterListBean> chapterListBeans);

        void onError(Throwable throwable);
    }
}
