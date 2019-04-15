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
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_book_info_error) + bookShelfBean.getNoteUrl()));
                return;
            } else {
                Debug.printLog(tag, "书籍详情页获取成功");
                Debug.printLog(tag, "网址:" + bookShelfBean.getNoteUrl());
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
            analyzer.setContent(s, bookShelfBean.getNoteUrl());

            Debug.printLog(tag, "开始获取书名");
            if (isEmpty(bookInfoBean.getName())) {
                result = analyzer.getString(bookSourceBean.getRuleBookName());
                bookInfoBean.setName(result);
            }
            Debug.printLog(tag, "书名:" + bookInfoBean.getName());
            Debug.printLog(tag, "开始获取作者");
            if (isEmpty(bookInfoBean.getAuthor())) {
                result = analyzer.getString(bookSourceBean.getRuleBookAuthor());
                bookInfoBean.setAuthor(FormatWebText.getAuthor(result));
            }
            Debug.printLog(tag, "作者:" + bookInfoBean.getAuthor());
            Debug.printLog(tag, "开始获取封面");
            result = analyzer.getString(bookSourceBean.getRuleCoverUrl(), true);
            if (!isEmpty(result)) {
                bookInfoBean.setCoverUrl(result);
            }
            Debug.printLog(tag, "封面:" + bookInfoBean.getCoverUrl());
            Debug.printLog(tag, "开始获取简介");
            result = analyzer.getString(bookSourceBean.getRuleIntroduce());
            if (!isEmpty(result)) {
                bookInfoBean.setIntroduce(result);
            }
            Debug.printLog(tag, "简介:" + bookInfoBean.getIntroduce());
            Debug.printLog(tag, "开始获取最新章节");
            result = analyzer.getString(bookSourceBean.getRuleBookLastChapter());
            if (!isEmpty(result)) {
                bookShelfBean.setLastChapterName(result);
            }
            Debug.printLog(tag, "最新章节" + bookShelfBean.getLastChapterName());
            Debug.printLog(tag, "开始获取目录网址");
            result = analyzer.getString(bookSourceBean.getRuleChapterUrl(), true);
            if (!isEmpty(result)) {
                bookInfoBean.setChapterUrl(result);
            }else{
                bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
            }
            Debug.printLog(tag, "目录网址" + bookInfoBean.getChapterUrl());
            bookInfoBean.setOrigin(name);
            bookShelfBean.setBookInfoBean(bookInfoBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }
}
