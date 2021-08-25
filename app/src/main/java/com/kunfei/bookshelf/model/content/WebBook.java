package com.kunfei.bookshelf.model.content;

import static android.text.TextUtils.isEmpty;
import static com.kunfei.bookshelf.constant.AppConstant.JS_PATTERN;
import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;

import android.text.TextUtils;

import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.help.JsExtensions;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import javax.script.SimpleBindings;

import io.reactivex.Observable;
import retrofit2.Response;

/**
 * 默认检索规则
 */
public class WebBook extends BaseModelImpl implements JsExtensions {
    private final String tag;
    private String name;
    private final BookSourceBean bookSourceBean;
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
            headerMap = bookSourceBean.getHeaderMap(true);
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
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(
                    url, tag, bookSourceBean, null, page,
                    bookSourceBean.getHeaderMap(true)
            );
            return getResponseO(analyzeUrl)
                    .flatMap(response -> checkLogin(response, url, tag))
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
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(
                    bookSourceBean.getRuleSearchUrl(),
                    tag, bookSourceBean, content, page,
                    bookSourceBean.getHeaderMap(true)
            );
            return getResponseO(analyzeUrl)
                    .flatMap(response -> checkLogin(response, bookSourceBean.getRuleSearchUrl(), tag))
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
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(
                    bookShelfBean.getNoteUrl(), tag, bookSourceBean,
                    bookSourceBean.getHeaderMap(true)
            );
            return getResponseO(analyzeUrl)
                    .flatMap(response -> setCookie(response, tag))
                    .flatMap(response -> checkLogin(response, bookShelfBean.getNoteUrl(), tag))
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
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(
                    bookShelfBean.getBookInfoBean().getChapterUrl(),
                    bookShelfBean.getNoteUrl(), bookSourceBean,
                    bookSourceBean.getHeaderMap(true)
            );
            return getResponseO(analyzeUrl)
                    .flatMap(response -> setCookie(response, tag))
                    .flatMap(stringResponse -> checkLogin(stringResponse, bookShelfBean.getBookInfoBean().getChapterUrl(), bookShelfBean.getNoteUrl()))
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
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(
                    chapterBean.getDurChapterUrl(),
                    bookShelfBean.getBookInfoBean().getChapterUrl(),
                    bookSourceBean,
                    bookSourceBean.getHeaderMap(true));
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
                        .flatMap(stringResponse -> checkLogin(stringResponse, chapterBean.getDurChapterUrl(), bookShelfBean.getBookInfoBean().getChapterUrl()))
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapterBean, nextChapterBean, bookShelfBean, headerMap));
            }
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", e.getLocalizedMessage())));
        }
    }

    Observable<Response<String>> checkLogin(final Response<String> stringResponse, String url, String baseUrl) {
        return Observable.create(emitter -> {
            String checkJs = bookSourceBean.getLoginCheckJs();
            if (!TextUtils.isEmpty(checkJs)) {
                SimpleBindings bindings = new SimpleBindings();
                bindings.put("source", bookSourceBean);
                bindings.put("url", url);
                bindings.put("java", this);
                bindings.put("result", stringResponse);
                bindings.put("baseUrl", baseUrl);
                @SuppressWarnings("unchecked")
                Response<String> res = (Response<String>) SCRIPT_ENGINE.eval(checkJs, bindings);
                emitter.onNext(res);
                return;
            }
            emitter.onNext(stringResponse);
        });
    }

    public class NoSourceThrowable extends Throwable {
        NoSourceThrowable(String tag) {
            super(String.format("%s没有找到书源配置", tag));
        }
    }

}
