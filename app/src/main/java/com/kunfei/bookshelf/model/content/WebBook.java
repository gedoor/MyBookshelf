package com.kunfei.bookshelf.model.content;

import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

/**
 * 默认检索规则
 */
public class WebBook extends BaseModelImpl {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private Map<String, String> headerMap = AnalyzeHeaders.getMap(null);

    public static WebBook getInstance(String tag) {
        return new WebBook(tag);
    }

    private WebBook(String tag) {
        this.tag = tag;
        try {
            URL url = new URL(tag);
            name = url.getHost();
        } catch (MalformedURLException e) {
            name = tag;
        }
        bookSourceBean = BookSourceManager.getBookSourceByUrl(tag);
        if (bookSourceBean != null) {
            name = bookSourceBean.getBookSourceName();
            headerMap = AnalyzeHeaders.getMap(bookSourceBean);
        }
    }

    /**
     * 发现
     */
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        if (bookSourceBean == null) {
            return Observable.error(new NoSourceThrowable(tag));
        }
        BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(url, null, page, headerMap, tag);
            return getResponseO(analyzeUrl)
                    .flatMap(bookList::analyzeSearchBook);
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("%s错误:%s", url, e.getLocalizedMessage())));
        }
    }

    /**
     * 搜索
     */
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        if (bookSourceBean == null || isEmpty(bookSourceBean.getRuleSearchUrl())) {
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
        BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookSourceBean.getRuleSearchUrl(), content, page, headerMap, tag);
            return getResponseO(analyzeUrl)
                    .flatMap(bookList::analyzeSearchBook);
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    /**
     * 获取书籍信息
     */
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        if (bookSourceBean == null) {
            return Observable.error(new NoSourceThrowable(tag));
        }
        BookInfo bookInfo = new BookInfo(tag, name, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getNoteUrl(), headerMap, tag);
            return getResponseO(analyzeUrl)
                    .flatMap(response -> setCookie(response, tag))
                    .flatMap(response -> bookInfo.analyzeBookInfo(response.body(), bookShelfBean));
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", bookShelfBean.getNoteUrl())));
        }
    }

    /**
     * 获取目录
     */
    public Observable<List<ChapterListBean>> getChapterList(final BookShelfBean bookShelfBean) {
        if (bookSourceBean == null) {
            return Observable.error(new NoSourceThrowable(bookShelfBean.getBookInfoBean().getName()));
        }
        BookChapter bookChapter = new BookChapter(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getBookInfoBean().getChapterUrl(), headerMap, tag);
            return getResponseO(analyzeUrl)
                    .flatMap(response -> setCookie(response, tag))
                    .flatMap(response -> bookChapter.analyzeChapterList(response.body(), bookShelfBean, headerMap));
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", bookShelfBean.getBookInfoBean().getChapterUrl())));
        }
    }

    /**
     * 获取正文
     */
    public Observable<BookContentBean> getBookContent(final BaseChapterBean chapterBean) {
        if (bookSourceBean == null) {
            return Observable.error(new NoSourceThrowable(chapterBean.getTag()));
        }
        BookContent bookContent = new BookContent(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterBean.getDurChapterUrl(), headerMap, tag);
            if (bookSourceBean.getRuleBookContent().startsWith("$") && !bookSourceBean.getRuleBookContent().startsWith("$.")) {
                return getAjaxString(analyzeUrl, tag)
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapterBean, headerMap));
            } else {
                return getResponseO(analyzeUrl)
                        .flatMap(response -> setCookie(response, tag))
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapterBean, headerMap));
            }
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", chapterBean.getDurChapterUrl())));
        }
    }

    public class NoSourceThrowable extends Throwable {

        NoSourceThrowable(String tag) {
            super(String.format("%s没有找到书源配置", tag));
        }
    }

}
