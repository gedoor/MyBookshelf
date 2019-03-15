package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.help.FormatWebText;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;

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
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_book_info_error)));
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

            AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean);
            analyzer.setContent(s);

            if (isEmpty(bookInfoBean.getName())) {
                result = analyzer.getString(bookSourceBean.getRuleBookName());
                bookInfoBean.setName(result);
            }

            if (isEmpty(bookInfoBean.getAuthor())) {
                result = analyzer.getString(bookSourceBean.getRuleBookAuthor());
                bookInfoBean.setAuthor(FormatWebText.getAuthor(result));
            }

            result = analyzer.getString(bookSourceBean.getRuleCoverUrl(), bookShelfBean.getNoteUrl());
            if (!isEmpty(result)) {
                bookInfoBean.setCoverUrl(result);
            }

            result = analyzer.getString(bookSourceBean.getRuleIntroduce());
            if (!isEmpty(result)) {
                bookInfoBean.setIntroduce(result);
            }

            result = analyzer.getString(bookSourceBean.getRuleBookLastChapter());
            if (!isEmpty(result)) {
                bookShelfBean.setLastChapterName(result);
            }

            result = analyzer.getString(bookSourceBean.getRuleChapterUrl(), bookShelfBean.getNoteUrl());
            if (!isEmpty(result)) {
                bookInfoBean.setChapterUrl(result);
            }else{
                bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
            }

            bookInfoBean.setOrigin(name);
            bookShelfBean.setBookInfoBean(bookInfoBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }
}
