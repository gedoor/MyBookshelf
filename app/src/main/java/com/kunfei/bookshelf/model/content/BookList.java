package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.help.FormatWebText;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeCollection;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

class BookList {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;

    BookList(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            String baseUrl;
            okhttp3.Response networkResponse = response.raw().networkResponse();
            if (networkResponse != null) {
                baseUrl = networkResponse.request().url().toString();
            } else {
                baseUrl = response.raw().request().url().toString();
            }
            if (TextUtils.isEmpty(response.body())) {
                e.onError(new Throwable("访问网站失败:" + baseUrl));
                return;
            }
            List<SearchBookBean> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule(null);
            analyzer.setContent(response.body());

            String bookUrlPattern = bookSourceBean.getRuleBookUrlPattern();
            if (!isEmpty(bookUrlPattern) && !bookUrlPattern.endsWith(".*")) {
                bookUrlPattern += ".*";
            }
            //如果是详情页面, 解析详情页面
            if (!isEmpty(bookUrlPattern) && baseUrl.matches(bookUrlPattern)
                    && !isEmpty(bookSourceBean.getRuleBookName()) && !isEmpty(bookSourceBean.getRuleBookLastChapter())) {
                SearchBookBean item = new SearchBookBean();
                analyzer.setBook(item);
                String bookName = analyzer.getString(bookSourceBean.getRuleBookName());
                if (!TextUtils.isEmpty(bookName)) {
                    item.setNoteUrl(baseUrl);
                    item.setTag(tag);
                    item.setOrigin(name);
                    item.setName(bookName);
                    item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl(), baseUrl));
                    item.setAuthor(FormatWebText.getAuthor(analyzer.getString(bookSourceBean.getRuleBookAuthor())));
                    item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleBookKind())));
                    item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
                    books.add(item);
                } else if (!e.isDisposed()) {
                    e.onError(new Throwable("未获取到书名"));
                    return;
                }
            } else {
                AnalyzeCollection collections = analyzer.getElements(bookSourceBean.getRuleSearchList());
                if (collections.size() == 0 && !e.isDisposed()) {
                    e.onError(new Throwable("搜索列表为空"));
                    return;
                }
                while (collections.hasNext()){
                    SearchBookBean item = new SearchBookBean();
                    analyzer.setBook(item);
                    collections.next(analyzer);
                    String bookName = analyzer.getString(bookSourceBean.getRuleSearchName());
                    if (!TextUtils.isEmpty(bookName)) {
                        item.setTag(tag);
                        item.setOrigin(name);
                        item.setName(bookName);
                        item.setAuthor(FormatWebText.getAuthor(analyzer.getString(bookSourceBean.getRuleSearchAuthor())));
                        item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleSearchKind())));
                        item.setLastChapter(analyzer.getString(bookSourceBean.getRuleSearchLastChapter()));
                        item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleSearchCoverUrl(), baseUrl));
                        item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
                        String resultUrl = analyzer.getString(bookSourceBean.getRuleSearchNoteUrl(), baseUrl);
                        item.setNoteUrl(isEmpty(resultUrl) ? baseUrl : resultUrl);
                        books.add(item);
                    }
                }
            }
            if (books.isEmpty() && !e.isDisposed()) {
                e.onError(new Throwable("未获取到书名"));
                return;
            }
            e.onNext(books);
            e.onComplete();
        });
    }
}