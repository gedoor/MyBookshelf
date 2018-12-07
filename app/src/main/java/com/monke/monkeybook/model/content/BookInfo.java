package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.analyzeRule.AnalyzeByJSoup;
import com.monke.monkeybook.model.analyzeRule.AnalyzeByXPath;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

class BookInfo {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private AnalyzeByXPath analyzeByXPath;
    private AnalyzeByJSoup analyzeByJSoup;

    BookInfo(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("书籍信息获取失败"));
                e.onComplete();
                return;
            }
            bookShelfBean.setTag(tag);
            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            if (bookInfoBean == null) {
                bookInfoBean = new BookInfoBean();
            }
            bookInfoBean.setNoteUrl(bookShelfBean.getNoteUrl());   //id
            bookInfoBean.setTag(tag);
            Document doc = Jsoup.parse(s);
            analyzeByXPath = new AnalyzeByXPath(doc);
            analyzeByJSoup = new AnalyzeByJSoup(doc, bookShelfBean.getNoteUrl());
            if (isEmpty(bookInfoBean.getCoverUrl())) {
                bookInfoBean.setCoverUrl(analyzeToString(bookSourceBean.getRuleCoverUrl(), bookShelfBean.getNoteUrl()));
            }
            if (isEmpty(bookInfoBean.getName())) {
                bookInfoBean.setName(analyzeToString(bookSourceBean.getRuleBookName()));
            }
            if (isEmpty(bookInfoBean.getAuthor())) {
                bookInfoBean.setAuthor(FormatWebText.getAuthor(analyzeToString(bookSourceBean.getRuleBookAuthor())));
            }
            bookInfoBean.setIntroduce(analyzeToString(bookSourceBean.getRuleIntroduce()));
            bookInfoBean.setChapterUrl(analyzeToString(bookSourceBean.getRuleChapterUrl(), bookShelfBean.getNoteUrl()));
            if (isEmpty(bookInfoBean.getChapterUrl())) {
                bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
            }
            bookInfoBean.setOrigin(name);
            bookShelfBean.setBookInfoBean(bookInfoBean);
            e.onNext(bookShelfBean);
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
