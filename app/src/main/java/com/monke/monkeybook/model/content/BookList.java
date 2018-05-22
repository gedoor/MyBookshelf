package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeJson;
import com.monke.monkeybook.utils.NetworkUtil;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

public class BookList {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;

    BookList(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    public Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            try {
                String baseURI;
                okhttp3.Response networkResponse = response.raw().networkResponse();
                if (networkResponse != null && networkResponse.request() != null) {
                    baseURI = networkResponse.request().url().toString();
                } else {
                    baseURI = response.raw().request().url().toString();
                }
                if (bookSourceBean.getRuleSearchList().contains("JSON")) {
                    JSONObject jsonObject = new JSONObject(response.body());
                    List<JSONObject> jsonObjects = AnalyzeJson.getJsonObjects(jsonObject, bookSourceBean.getRuleSearchList());
                    if (jsonObjects != null && jsonObjects.size() > 0) {
                        List<SearchBookBean> books = new ArrayList<>();
                        for (int i = 0; i < jsonObjects.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(tag);
                            item.setOrigin(name);
                            AnalyzeJson analyzeJson = new AnalyzeJson(jsonObjects.get(i));
                            item.setAuthor(FormatWebText.getAuthor(analyzeJson.getResult(bookSourceBean.getRuleSearchAuthor())));
                            item.setKind(analyzeJson.getResult(bookSourceBean.getRuleSearchKind()));
                            item.setLastChapter(analyzeJson.getResult(bookSourceBean.getRuleSearchLastChapter()));
                            item.setName(analyzeJson.getResult(bookSourceBean.getRuleSearchName()));
                            item.setNoteUrl(NetworkUtil.getAbsoluteURL(baseURI, analyzeJson.getResult(bookSourceBean.getRuleSearchNoteUrl())));
                            if (isEmpty(item.getNoteUrl())) {
                                item.setNoteUrl(baseURI);
                            }
                            item.setCoverUrl(analyzeJson.getResult(bookSourceBean.getRuleSearchCoverUrl()));
                            if (!isEmpty(item.getName())) {
                                books.add(item);
                            }
                        }
                    } else {
                        e.onNext(new ArrayList<>());
                    }
                } else {
                    Document doc = Jsoup.parse(response.body());
                    Elements booksE = AnalyzeElement.getElements(doc, bookSourceBean.getRuleSearchList());
                    if (null != booksE && booksE.size() > 0) {
                        List<SearchBookBean> books = new ArrayList<>();
                        for (int i = 0; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(tag);
                            item.setOrigin(name);
                            AnalyzeElement analyzeElement = new AnalyzeElement(booksE.get(i), baseURI);
                            item.setAuthor(FormatWebText.getAuthor(analyzeElement.getResult(bookSourceBean.getRuleSearchAuthor())));
                            item.setKind(analyzeElement.getResult(bookSourceBean.getRuleSearchKind()));
                            item.setLastChapter(analyzeElement.getResult(bookSourceBean.getRuleSearchLastChapter()));
                            item.setName(analyzeElement.getResult(bookSourceBean.getRuleSearchName()));
                            item.setNoteUrl(analyzeElement.getResult(bookSourceBean.getRuleSearchNoteUrl()));
                            if (isEmpty(item.getNoteUrl())) {
                                item.setNoteUrl(baseURI);
                            }
                            item.setCoverUrl(analyzeElement.getResult(bookSourceBean.getRuleSearchCoverUrl()));
                            if (!isEmpty(item.getName())) {
                                books.add(item);
                            }
                        }
                        e.onNext(books);
                    } else {
                        e.onNext(new ArrayList<>());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(new ArrayList<>());
            }
            e.onComplete();
        });
    }
}
