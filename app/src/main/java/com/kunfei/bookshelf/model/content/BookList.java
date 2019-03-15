package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.help.FormatWebText;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeCollection;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
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
            String baseUrl;
            okhttp3.Response networkResponse = response.raw().networkResponse();
            if (networkResponse != null) {
                baseUrl = networkResponse.request().url().toString();
            } else {
                baseUrl = response.raw().request().url().toString();
            }
            if (TextUtils.isEmpty(response.body())) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_web_content_error, baseUrl)));
                return;
            }
            List<SearchBookBean> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule(null);
            analyzer.setContent(response.body());

            String bookUrlPattern = bookSourceBean.getRuleBookUrlPattern();
            if (!isEmpty(bookUrlPattern) && !bookUrlPattern.endsWith(".*")) {
                bookUrlPattern += ".*";
            }
            //如果符合详情页url规则
            if (!isEmpty(bookUrlPattern) && baseUrl.matches(bookUrlPattern)
                    && !isEmpty(bookSourceBean.getRuleBookName()) && !isEmpty(bookSourceBean.getRuleBookLastChapter())) {
                SearchBookBean item = getItem(analyzer, baseUrl);
                if (item != null) {
                    books.add(item);
                }
            } else {
                AnalyzeCollection collections;
                boolean reverse;
                String ruleSearchList;
                if (bookSourceBean.getRuleSearchList().startsWith("-")) {
                    reverse = true;
                    ruleSearchList = bookSourceBean.getRuleSearchList().substring(1);
                } else {
                    reverse = false;
                    ruleSearchList = bookSourceBean.getRuleSearchList();
                }
                //获取列表
                collections = analyzer.getElements(ruleSearchList);
                if (collections.size() == 0) {
                    // 搜索列表为空时,当做详情页处理
                    SearchBookBean item = getItem(analyzer, baseUrl);
                    if (item != null) {
                        books.add(item);
                    }
                } else {
                    while (collections.hasNext()) {
                        collections.next(analyzer);
                        SearchBookBean item = getItemInList(analyzer, baseUrl);
                        if (item != null) {
                            books.add(item);
                        }
                    }
                    if (books.size() > 1 && reverse) {
                        Collections.reverse(books);
                    }
                }
            }
            if (books.isEmpty()) {
                if (!e.isDisposed()) {
                    e.onError(new Throwable(MApplication.getInstance().getString(R.string.no_book_name)));
                }
                return;
            }
            e.onNext(books);
            e.onComplete();
        });
    }

    private SearchBookBean getItem(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        String bookName = analyzer.getString(bookSourceBean.getRuleBookName());
        if (!TextUtils.isEmpty(bookName)) {
            item.setNoteUrl(baseUrl);
            item.setTag(tag);
            item.setOrigin(name);
            item.setName(bookName);
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl(), baseUrl));
            item.setAuthor(FormatWebText.getAuthor(analyzer.getString(bookSourceBean.getRuleBookAuthor())));
            item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleBookKind())));
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
            return item;
        }
        return null;
    }

    private SearchBookBean getItemInList(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        String bookName = analyzer.getString(bookSourceBean.getRuleSearchName());
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(name);
            item.setName(bookName);
            item.setAuthor(FormatWebText.getAuthor(analyzer.getString(bookSourceBean.getRuleSearchAuthor())));
            item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleSearchKind())));
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleSearchLastChapter()));
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleSearchCoverUrl(), baseUrl));
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
            String resultUrl = analyzer.getString(bookSourceBean.getRuleSearchNoteUrl(), baseUrl);
            item.setNoteUrl(isEmpty(resultUrl) ? baseUrl : resultUrl);
            return item;
        }
        return null;
    }
}