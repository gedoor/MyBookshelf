package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.analyzeRule.AnalyzeByJSoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

class BookInfo {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;

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
            AnalyzeByJSoup analyzeElement = new AnalyzeByJSoup(doc, bookShelfBean.getNoteUrl());
            if (isEmpty(bookInfoBean.getCoverUrl())) {
                bookInfoBean.setCoverUrl(analyzeElement.getResultUrl(bookSourceBean.getRuleCoverUrl()));
            }
            if (isEmpty(bookInfoBean.getName())) {
                bookInfoBean.setName(analyzeElement.getResult(bookSourceBean.getRuleBookName()));
            }
            if (isEmpty(bookInfoBean.getAuthor())) {
                bookInfoBean.setAuthor(FormatWebText.getAuthor(analyzeElement.getResult(bookSourceBean.getRuleBookAuthor())));
            }
            bookInfoBean.setIntroduce(analyzeElement.getResult(bookSourceBean.getRuleIntroduce()));
            String chapterUrl = analyzeElement.getResultUrl(bookSourceBean.getRuleChapterUrl());
            if (isEmpty(chapterUrl)) {
                bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
            } else {
                bookInfoBean.setChapterUrl(chapterUrl);
            }
            bookInfoBean.setOrigin(name);
            bookShelfBean.setBookInfoBean(bookInfoBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }
}
