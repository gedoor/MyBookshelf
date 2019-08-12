package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByRegex;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.utils.StringUtils;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

class BookInfo {
    private String tag;
    private String sourceName;
    private BookSourceBean bookSourceBean;

    BookInfo(String tag, String sourceName, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            String baseUrl = bookShelfBean.getNoteUrl();

            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_book_info_error) + baseUrl));
                return;
            } else {
                Debug.printLog(tag, "┌成功获取详情页");
                Debug.printLog(tag, "└" + baseUrl);
            }
            bookShelfBean.setTag(tag);

            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            bookInfoBean.setNoteUrl(baseUrl);   //id
            bookInfoBean.setTag(tag);
            bookInfoBean.setOrigin(sourceName);
            bookInfoBean.setBookSourceType(bookSourceBean.getBookSourceType()); // 是否为有声读物

            AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean);
            analyzer.setContent(s, baseUrl);

            // 获取详情页预处理规则
            String ruleInfoInit = bookSourceBean.getRuleBookInfoInit();
            boolean isRegex = false;
            if (!isEmpty(ruleInfoInit)) {
                // 仅使用java正则表达式提取书籍详情
                if (ruleInfoInit.startsWith(":")) {
                    isRegex = true;
                    ruleInfoInit = ruleInfoInit.substring(1);
                    Debug.printLog(tag, "┌详情信息预处理");
                    AnalyzeByRegex.getInfoOfRegex(s, ruleInfoInit.split("&&"), 0, bookShelfBean, analyzer, bookSourceBean, tag);
                } else {
                    Object object = analyzer.getElement(ruleInfoInit);
                    if (object != null) {
                        analyzer.setContent(object);
                    }
                }
            }
            if (!isRegex) {
                Debug.printLog(tag, "┌详情信息预处理");
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) analyzer.setContent(object);
                Debug.printLog(tag, "└详情预处理完成");

                Debug.printLog(tag, "┌获取书名");
                String bookName = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookName()));
                if (!isEmpty(bookName)) bookInfoBean.setName(bookName);
                Debug.printLog(tag, "└" + bookName);

                Debug.printLog(tag, "┌获取作者");
                String bookAuthor = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookAuthor()));
                if (!isEmpty(bookAuthor)) bookInfoBean.setAuthor(bookAuthor);
                Debug.printLog(tag, "└" + bookAuthor);

                Debug.printLog(tag, "┌获取分类");
                String bookKind = analyzer.getString(bookSourceBean.getRuleBookKind());
                Debug.printLog(tag, 111, "└" + bookKind);

                Debug.printLog(tag, "┌获取最新章节");
                String bookLastChapter = analyzer.getString(bookSourceBean.getRuleBookLastChapter());
                if (!isEmpty(bookLastChapter)) bookShelfBean.setLastChapterName(bookLastChapter);
                Debug.printLog(tag, "└" + bookLastChapter);

                Debug.printLog(tag, "┌获取简介");
                String bookIntroduce = analyzer.getString(bookSourceBean.getRuleIntroduce());
                if (!isEmpty(bookIntroduce)) bookInfoBean.setIntroduce(bookIntroduce);
                Debug.printLog(tag, 1, "└" + bookIntroduce, true, true);

                Debug.printLog(tag, "┌获取封面");
                String bookCoverUrl = analyzer.getString(bookSourceBean.getRuleCoverUrl(), true);
                if (!isEmpty(bookCoverUrl)) bookInfoBean.setCoverUrl(bookCoverUrl);
                Debug.printLog(tag, "└" + bookCoverUrl);

                Debug.printLog(tag, "┌获取目录网址");
                String bookCatalogUrl = analyzer.getString(bookSourceBean.getRuleChapterUrl(), true);
                if (isEmpty(bookCatalogUrl)) bookCatalogUrl = baseUrl;
                bookInfoBean.setChapterUrl(bookCatalogUrl);
                //如果目录页和详情页相同,暂存页面内容供获取目录用
                if (bookCatalogUrl.equals(baseUrl)) bookInfoBean.setChapterListHtml(s);
                Debug.printLog(tag, "└" + bookInfoBean.getChapterUrl());
                bookShelfBean.setBookInfoBean(bookInfoBean);
                Debug.printLog(tag, "-详情页解析完成");
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

}
