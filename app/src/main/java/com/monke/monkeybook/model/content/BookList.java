package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.analyzeRule.AnalyzeByJSoup;
import com.monke.monkeybook.model.analyzeRule.AnalyzeByXPath;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

class BookList {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private AnalyzeByXPath analyzeByXPath;
    private AnalyzeByJSoup analyzeByJSoup;

    BookList(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            List<SearchBookBean> books = new ArrayList<>();
            String baseUrl;
            okhttp3.Response networkResponse = response.raw().networkResponse();
            if (networkResponse != null) {
                baseUrl = networkResponse.request().url().toString();
            } else {
                baseUrl = response.raw().request().url().toString();
            }
            assert response.body() != null;
            Document doc = Jsoup.parse(response.body());
            analyzeByXPath = new AnalyzeByXPath(doc);
            String bookUrlPattern = bookSourceBean.getRuleBookUrlPattern();
            if (!isEmpty(bookUrlPattern) && !bookUrlPattern.endsWith(".*")) {
                bookUrlPattern += ".*";
            }
            if (!isEmpty(bookUrlPattern) && baseUrl.matches(bookUrlPattern)
                    && !isEmpty(bookSourceBean.getRuleBookName()) && !isEmpty(bookSourceBean.getRuleBookLastChapter())) {
                analyzeByJSoup = new AnalyzeByJSoup(doc, baseUrl);
                SearchBookBean item = new SearchBookBean();
                item.setNoteUrl(baseUrl);
                item.setTag(tag);
                item.setOrigin(name);
                item.setName(analyzeToString(bookSourceBean.getRuleBookName()));
                item.setCoverUrl(analyzeToString(bookSourceBean.getRuleCoverUrl(), baseUrl));
                item.setAuthor(analyzeToString(bookSourceBean.getRuleBookAuthor()));
                item.setKind(analyzeToString(bookSourceBean.getRuleBookKind()));
                item.setLastChapter(analyzeToString(bookSourceBean.getRuleBookLastChapter()));
                if (!TextUtils.isEmpty(item.getName())) {
                    books.add(item);
                }
            } else {
                Elements booksE = AnalyzeByJSoup.getElements(doc, bookSourceBean.getRuleSearchList());
                if (null != booksE && booksE.size() > 0) {
                    for (int i = 0; i < booksE.size(); i++) {
                        analyzeByJSoup = new AnalyzeByJSoup(booksE.get(i), baseUrl);
                        analyzeByXPath = new AnalyzeByXPath(booksE.get(i).children());
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(tag);
                        item.setOrigin(name);
                        item.setAuthor(FormatWebText.getAuthor(analyzeToString(bookSourceBean.getRuleSearchAuthor())));
                        item.setKind(analyzeToString(bookSourceBean.getRuleSearchKind()));
                        item.setLastChapter(analyzeToString(bookSourceBean.getRuleSearchLastChapter()));
                        item.setName(analyzeToString(bookSourceBean.getRuleSearchName()));
                        String resultUrl = analyzeToString(bookSourceBean.getRuleSearchNoteUrl(), baseUrl);
                        item.setNoteUrl(isEmpty(resultUrl) ? baseUrl : resultUrl);
                        item.setCoverUrl(analyzeToString(bookSourceBean.getRuleSearchCoverUrl(), baseUrl));
                        if (!TextUtils.isEmpty(item.getName())) {
                            books.add(item);
                        }
                    }
                }
            }
            e.onNext(books);
            e.onComplete();
        });
    }

    private String analyzeToString(String rule) {
        return analyzeToString(rule, null);
    }

    private String analyzeToString(String rule, String baseUrl) {
        SourceRule sourceRule = new SourceRule(rule);
        String result;
        switch (sourceRule.mode) {
            case XPath:
                result = analyzeByXPath.getString(sourceRule.rule, baseUrl);
                break;
            default:
                if (TextUtils.isEmpty(baseUrl)) {
                    result = analyzeByJSoup.getResult(sourceRule.rule);
                } else {
                    result = analyzeByJSoup.getResultUrl(sourceRule.rule);
                }
        }
        return result;
    }

}
