package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;
import static com.kunfei.bookshelf.constant.AppConstant.JS_PATTERN;

/**
 * 默认检索规则
 */
public class WebBook extends BaseModelImpl {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private Map<String, String> headerMap;

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
        BookList bookList = new BookList(tag, name, bookSourceBean, true);
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
        BookList bookList = new BookList(tag, name, bookSourceBean, false);
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
        if (!TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getBookInfoHtml())) {
            return bookInfo.analyzeBookInfo(bookShelfBean.getBookInfoBean().getBookInfoHtml(), bookShelfBean);
        }
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
    public Observable<List<BookChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        if (bookSourceBean == null) {
            return Observable.error(new NoSourceThrowable(bookShelfBean.getBookInfoBean().getName()));
        }
        BookChapterList bookChapterList = new BookChapterList(tag, bookSourceBean, true);
        if (!TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookChapterList.analyzeChapterList(bookShelfBean.getBookInfoBean().getChapterListHtml(), bookShelfBean, headerMap);
        }
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getBookInfoBean().getChapterUrl(), headerMap, bookShelfBean.getNoteUrl());
            return getResponseO(analyzeUrl)
                    .flatMap(response -> setCookie(response, tag))
                    .flatMap(response -> bookChapterList.analyzeChapterList(response.body(), bookShelfBean, headerMap));
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", bookShelfBean.getBookInfoBean().getChapterUrl())));
        }
    }

    /**
     * 获取正文
     */
    public Observable<BookContentBean> getBookContent(final BaseChapterBean chapterBean, final BaseChapterBean nextChapterBean, final BookShelfBean bookShelfBean) {
        if (bookSourceBean == null) {
            return Observable.error(new NoSourceThrowable(chapterBean.getTag()));
        }
        if (isEmpty(bookSourceBean.getRuleBookContent())) {
            return Observable.create(emitter -> {
                BookContentBean bookContentBean = new BookContentBean();
                bookContentBean.setDurChapterContent(chapterBean.getDurChapterUrl());
                bookContentBean.setDurChapterIndex(chapterBean.getDurChapterIndex());
                bookContentBean.setTag(bookShelfBean.getTag());
                bookContentBean.setDurChapterUrl(chapterBean.getDurChapterUrl());
                emitter.onNext(bookContentBean);
                emitter.onComplete();
            });
        }
        BookContent bookContent = new BookContent(tag, bookSourceBean);
        if (Objects.equals(chapterBean.getDurChapterUrl(), bookShelfBean.getBookInfoBean().getChapterUrl())
                && !TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookContent.analyzeBookContent(bookShelfBean.getBookInfoBean().getChapterListHtml(), chapterBean, nextChapterBean, bookShelfBean, headerMap);
        }
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterBean.getDurChapterUrl(), headerMap, bookShelfBean.getBookInfoBean().getChapterUrl());
            String contentRule = bookSourceBean.getRuleBookContent();
            if (contentRule.startsWith("$") && !contentRule.startsWith("$.")) {
                //动态网页第一个js放到webView里执行
                contentRule = contentRule.substring(1);
                String js = null;
                Matcher jsMatcher = JS_PATTERN.matcher(contentRule);
                if (jsMatcher.find()) {
                    js = jsMatcher.group();
                    if (js.startsWith("<js>")) {
                        js = js.substring(4, js.lastIndexOf("<"));
                    } else {
                        js = js.substring(4);
                    }
                }
                return getAjaxString(analyzeUrl, tag, js)
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapterBean, nextChapterBean, bookShelfBean, headerMap));
            } else {
                return getResponseO(analyzeUrl)
                        .flatMap(response -> setCookie(response, tag))
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapterBean, nextChapterBean, bookShelfBean, headerMap));
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
