package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.analyzeRule.AnalyzeElement;

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

    BookList(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            List<SearchBookBean> books = new ArrayList<>();
            try {
                String baseURI;
                okhttp3.Response networkResponse = response.raw().networkResponse();
                if (networkResponse != null) {
                    baseURI = networkResponse.request().url().toString();
                } else {
                    baseURI = response.raw().request().url().toString();
                }
                assert response.body() != null;
                Document doc = Jsoup.parse(response.body());
                String bookUrlPattern = bookSourceBean.getRuleBookUrlPattern();
                if (!isEmpty(bookUrlPattern) && !bookUrlPattern.endsWith(".*")) {
                    bookUrlPattern += ".*";
                }
                if (!isEmpty(bookUrlPattern) && baseURI.matches(bookUrlPattern)
                        && !isEmpty(bookSourceBean.getRuleBookName()) && !isEmpty(bookSourceBean.getRuleBookLastChapter())) {
                    AnalyzeElement analyzeElement = new AnalyzeElement(doc, baseURI);
                    SearchBookBean item = new SearchBookBean();
                    item.setNoteUrl(baseURI);
                    item.setTag(tag);
                    item.setOrigin(name);
                    item.setCoverUrl(analyzeElement.getResultUrl(bookSourceBean.getRuleCoverUrl()));
                    item.setName(analyzeElement.getResult(bookSourceBean.getRuleBookName()));
                    item.setAuthor(FormatWebText.getAuthor(analyzeElement.getResult(bookSourceBean.getRuleBookAuthor())));
                    item.setKind(FormatWebText.getContent(analyzeElement.getResult(bookSourceBean.getRuleBookKind())));
                    item.setLastChapter(analyzeElement.getResult(bookSourceBean.getRuleBookLastChapter()));
                    books.add(item);
                } else {
                    Elements booksE = AnalyzeElement.getElements(doc, bookSourceBean.getRuleSearchList());
                    if (null != booksE && booksE.size() > 0) {
                        for (int i = 0; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(tag);
                            item.setOrigin(name);
                            AnalyzeElement analyzeElement = new AnalyzeElement(booksE.get(i), baseURI);
                            item.setAuthor(FormatWebText.getAuthor(analyzeElement.getResult(bookSourceBean.getRuleSearchAuthor())));
                            item.setKind(analyzeElement.getResult(bookSourceBean.getRuleSearchKind()));
                            item.setLastChapter(analyzeElement.getResult(bookSourceBean.getRuleSearchLastChapter()));
                            item.setName(analyzeElement.getResult(bookSourceBean.getRuleSearchName()));
                            String resultUrl = analyzeElement.getResultUrl(bookSourceBean.getRuleSearchNoteUrl());
                            item.setNoteUrl(isEmpty(resultUrl) ? baseURI : resultUrl);
                            item.setCoverUrl(analyzeElement.getResultUrl(bookSourceBean.getRuleSearchCoverUrl()));
                            if (!isEmpty(item.getName())) {
                                books.add(item);
                            }
                        }
                    }
                }
            e.onNext(books);
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(new ArrayList<>());
            }
            e.onComplete();
        });
    }
}
