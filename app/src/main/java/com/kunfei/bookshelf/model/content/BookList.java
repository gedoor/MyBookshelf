package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.help.FormatWebText;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeCollection;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;

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
            List<SearchBookBean> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule();

            analyzer.setContent(response.body());

            String baseUrl;
            okhttp3.Response networkResponse = response.raw().networkResponse();
            if (networkResponse != null) {
                baseUrl = networkResponse.request().url().toString();
            } else {
                baseUrl = response.raw().request().url().toString();
            }

            String bookUrlPattern = bookSourceBean.getRuleBookUrlPattern();
            if (!isEmpty(bookUrlPattern) && !bookUrlPattern.endsWith(".*")) {
                bookUrlPattern += ".*";
            }
            if (!isEmpty(bookUrlPattern) && baseUrl.matches(bookUrlPattern)
                    && !isEmpty(bookSourceBean.getRuleBookName()) && !isEmpty(bookSourceBean.getRuleBookLastChapter())) {
                //如果是详情页面, 解析详情页面
                String bookName = analyzer.getString(bookSourceBean.getRuleBookName());
                if (!TextUtils.isEmpty(bookName)) {
                    SearchBookBean item = new SearchBookBean();
                    item.setNoteUrl(baseUrl);
                    item.setTag(tag);
                    item.setOrigin(name);
                    item.setName(bookName);
                    item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl(), baseUrl));
                    item.setAuthor(FormatWebText.getAuthor(analyzer.getString(bookSourceBean.getRuleBookAuthor())));
                    item.setKind(analyzer.getString(bookSourceBean.getRuleBookKind()));
                    item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
                    books.add(item);
                }
            } else {
                AnalyzeCollection collections = analyzer.getElements(bookSourceBean.getRuleSearchList());
                if (collections.size() == 0) {
                    e.onError(new Throwable("搜索列表为空"));
                    return;
                }
                while (collections.hasNext()){
                    AnalyzeRule anaer = collections.next();
                    String bookName = anaer.getString(bookSourceBean.getRuleSearchName());
                    if (!TextUtils.isEmpty(bookName)) {
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(tag);
                        item.setOrigin(name);
                        item.setName(bookName);
                        item.setAuthor(FormatWebText.getAuthor(anaer.getString(bookSourceBean.getRuleSearchAuthor())));
                        item.setKind(anaer.getString(bookSourceBean.getRuleSearchKind()));
                        item.setLastChapter(anaer.getString(bookSourceBean.getRuleSearchLastChapter()));
                        item.setCoverUrl(anaer.getString(bookSourceBean.getRuleSearchCoverUrl(), baseUrl));
                        item.setIntroduce(anaer.getString(bookSourceBean.getRuleIntroduce()));
                        String resultUrl = anaer.getString(bookSourceBean.getRuleSearchNoteUrl(), baseUrl);
                        item.setNoteUrl(isEmpty(resultUrl) ? baseUrl : resultUrl);
                        books.add(item);
                    }
                }
            }

            e.onNext(books);
            e.onComplete();
        });
    }
}