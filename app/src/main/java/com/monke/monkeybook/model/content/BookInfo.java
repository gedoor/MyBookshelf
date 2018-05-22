package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeJson;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

public class BookInfo {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;

    BookInfo(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    public Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(tag);
            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            if (bookInfoBean == null) {
                bookInfoBean = new BookInfoBean();
            }
            bookInfoBean.setNoteUrl(bookShelfBean.getNoteUrl());   //id
            bookInfoBean.setTag(tag);
            if (bookSourceBean.getRuleBookName().contains("JSON") || bookSourceBean.getRuleBookAuthor().contains("JSON") || bookSourceBean.getRuleChapterUrl().contains("JSON")) {
                JSONObject jsonObject = new JSONObject(s);
                AnalyzeJson analyzeJson = new AnalyzeJson(jsonObject);
                if (isEmpty(bookInfoBean.getCoverUrl())) {
                    bookInfoBean.setCoverUrl(analyzeJson.getResult(bookSourceBean.getRuleCoverUrl()));
                }
                if (isEmpty(bookInfoBean.getName())) {
                    bookInfoBean.setName(analyzeJson.getResult(bookSourceBean.getRuleBookName()));
                }
                if (isEmpty(bookInfoBean.getAuthor())) {
                    bookInfoBean.setAuthor(FormatWebText.getAuthor(analyzeJson.getResult(bookSourceBean.getRuleBookAuthor())));
                }
                bookInfoBean.setIntroduce(analyzeJson.getResult(bookSourceBean.getRuleIntroduce()));
                String chapterUrl = analyzeJson.getResult(bookSourceBean.getRuleChapterUrl());
                if (isEmpty(chapterUrl)) {
                    bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
                } else {
                    bookInfoBean.setChapterUrl(chapterUrl);
                }
                bookInfoBean.setOrigin(name);
                bookShelfBean.setBookInfoBean(bookInfoBean);
                e.onNext(bookShelfBean);
            } else {
                Document doc = Jsoup.parse(s);
                AnalyzeElement analyzeElement = new AnalyzeElement(doc, bookShelfBean.getNoteUrl());
                if (isEmpty(bookInfoBean.getCoverUrl())) {
                    bookInfoBean.setCoverUrl(analyzeElement.getResult(bookSourceBean.getRuleCoverUrl()));
                }
                if (isEmpty(bookInfoBean.getName())) {
                    bookInfoBean.setName(analyzeElement.getResult(bookSourceBean.getRuleBookName()));
                }
                if (isEmpty(bookInfoBean.getAuthor())) {
                    bookInfoBean.setAuthor(FormatWebText.getAuthor(analyzeElement.getResult(bookSourceBean.getRuleBookAuthor())));
                }
                bookInfoBean.setIntroduce(analyzeElement.getResult(bookSourceBean.getRuleIntroduce()));
                String chapterUrl = analyzeElement.getResult(bookSourceBean.getRuleChapterUrl());
                if (isEmpty(chapterUrl)) {
                    bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
                } else {
                    bookInfoBean.setChapterUrl(chapterUrl);
                }
                bookInfoBean.setOrigin(name);
                bookShelfBean.setBookInfoBean(bookInfoBean);
                e.onNext(bookShelfBean);
            }
            e.onComplete();
        });
    }
}
