package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSoup;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.help.FormatWebText;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSonPath;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSoup;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByXPath;
import com.kunfei.bookshelf.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

class BookInfo {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private AnalyzeByXPath analyzeByXPath = new AnalyzeByXPath();
    private AnalyzeByJSoup analyzeByJSoup = new AnalyzeByJSoup();
    private AnalyzeByJSonPath analyzeByJSonPath = new AnalyzeByJSonPath();

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
            String result;
            bookInfoBean.setNoteUrl(bookShelfBean.getNoteUrl());   //id
            bookInfoBean.setTag(tag);
            if (!StringUtils.isJSONType(s)) {
                Document doc = Jsoup.parse(s);
                analyzeByXPath.parse(doc);
                analyzeByJSoup.parse(doc, bookShelfBean.getNoteUrl());
                if (isEmpty(bookInfoBean.getName())) {
                    bookInfoBean.setName(analyzeToString(bookSourceBean.getRuleBookName()));
                }
                result = analyzeToString(bookSourceBean.getRuleCoverUrl(), bookShelfBean.getNoteUrl());
                if (!isEmpty(result)) {
                    bookInfoBean.setCoverUrl(result);
                }
                result = FormatWebText.getAuthor(analyzeToString(bookSourceBean.getRuleBookAuthor()));
                if (!isEmpty(result)) {
                    bookInfoBean.setAuthor(result);
                }
                result = analyzeToString(bookSourceBean.getRuleIntroduce());
                if (!isEmpty(result)) {
                    bookInfoBean.setIntroduce(result);
                }
                bookInfoBean.setChapterUrl(analyzeToString(bookSourceBean.getRuleChapterUrl(), bookShelfBean.getNoteUrl()));
                if (isEmpty(bookInfoBean.getChapterUrl())) {
                    bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
                }
            } else {
                analyzeByJSonPath.parse(s);
                SourceRule sourceRule;
                if (isEmpty(bookInfoBean.getName())) {
                    sourceRule = new SourceRule(bookSourceBean.getRuleBookName());
                    result = analyzeByJSonPath.read(sourceRule.rule);
                    bookInfoBean.setName(result);
                }
                sourceRule = new SourceRule(bookSourceBean.getRuleCoverUrl());
                result = analyzeByJSonPath.read(sourceRule.rule);
                if (!isEmpty(result)) {
                    bookInfoBean.setCoverUrl(result);
                }
                sourceRule = new SourceRule(bookSourceBean.getRuleBookAuthor());
                result = analyzeByJSonPath.read(sourceRule.rule);
                if (!isEmpty(result)) {
                    bookInfoBean.setAuthor(result);
                }
                sourceRule = new SourceRule(bookSourceBean.getRuleIntroduce());
                result = analyzeByJSonPath.read(sourceRule.rule);
                if (!isEmpty(result)) {
                    bookInfoBean.setIntroduce(result);
                }
                sourceRule = new SourceRule(bookSourceBean.getRuleChapterUrl());
                bookInfoBean.setChapterUrl(analyzeByJSonPath.read(sourceRule.rule));
                if (isEmpty(bookInfoBean.getChapterUrl())) {
                    bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
                }
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
