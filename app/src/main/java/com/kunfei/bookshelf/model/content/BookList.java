package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.help.FormatWebText;
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
            } else {
                Debug.printLog(tag, "搜索网站获取成功");
                Debug.printLog(tag, baseUrl);
            }
            List<SearchBookBean> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule(null);
            analyzer.setContent(response.body(), baseUrl);

            String bookUrlPattern = bookSourceBean.getRuleBookUrlPattern();
            if (!isEmpty(bookUrlPattern) && !bookUrlPattern.endsWith(".*")) {
                bookUrlPattern += ".*";
            }
            //如果符合详情页url规则
            if (!isEmpty(bookUrlPattern) && baseUrl.matches(bookUrlPattern)
                    && !isEmpty(bookSourceBean.getRuleBookName()) && !isEmpty(bookSourceBean.getRuleBookLastChapter())) {
                Debug.printLog(tag, "搜索结果为详情页");
                SearchBookBean item = getItem(analyzer, baseUrl);
                if (item != null) {
                    books.add(item);
                }
            } else {
                List<Object> collections;
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
                Debug.printLog(tag, "开始解析搜索列表");
                collections = analyzer.getElements(ruleSearchList);
                if (collections.size() == 0) {
                    Debug.printLog(tag, "搜索列表为空,当做详情页处理");
                    SearchBookBean item = getItem(analyzer, baseUrl);
                    if (item != null) {
                        books.add(item);
                    }
                } else {
                    Debug.printLog(tag, "搜索列表数量为" + collections.size());
                    for (int i = 0; i < collections.size(); i++) {
                        Object object = collections.get(i);
                        analyzer.setContent(object, baseUrl);
                        SearchBookBean item;
                        if (i == 0) {
                            item = getItemInList0(analyzer, baseUrl);
                        } else {
                            item = getItemInList(analyzer, baseUrl);
                        }
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
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.no_book_name)));
                return;
            }
            e.onNext(books);
            e.onComplete();
        });
    }

    private SearchBookBean getItem(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        Debug.printLog(tag, "书籍网址:" + baseUrl);
        Debug.printLog(tag, "开始获取书名");
        String bookName = analyzer.getString(bookSourceBean.getRuleBookName());
        Debug.printLog(tag, "书名:" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(name);
            item.setNoteUrl(baseUrl);
            item.setName(bookName);
            Debug.printLog(tag, "开始获取作者");
            item.setAuthor(FormatWebText.getAuthor(analyzer.getString(bookSourceBean.getRuleBookAuthor())));
            Debug.printLog(tag, "作者:" + item.getAuthor());
            Debug.printLog(tag, "开始获取封面");
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl(), true));
            Debug.printLog(tag, "封面:" + item.getCoverUrl());
            Debug.printLog(tag, "开始获取分类");
            item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleBookKind())));
            Debug.printLog(tag, "分类:" + item.getKind());
            Debug.printLog(tag, "开始获取最新章节");
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
            Debug.printLog(tag, "最新章节" + item.getLastChapter());
            Debug.printLog(tag, "开始获取简介");
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
            Debug.printLog(tag, "简介:" + item.getIntroduce());
            return item;
        }
        return null;
    }

    private SearchBookBean getItemInList0(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        Debug.printLog(tag, "开始获取书名");
        String bookName = analyzer.getString(bookSourceBean.getRuleSearchName());
        Debug.printLog(tag, "书名:" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(name);
            item.setName(bookName);
            Debug.printLog(tag, "开始获取作者");
            item.setAuthor(FormatWebText.getAuthor(analyzer.getString(bookSourceBean.getRuleSearchAuthor())));
            Debug.printLog(tag, "作者:" + item.getAuthor());
            Debug.printLog(tag, "开始获取分类");
            item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleSearchKind())));
            Debug.printLog(tag, "分类:" + item.getKind());
            Debug.printLog(tag, "开始获取最新章节");
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleSearchLastChapter()));
            Debug.printLog(tag, "最新章节" + item.getLastChapter());
            Debug.printLog(tag, "开始获取封面");
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleSearchCoverUrl(), true));
            Debug.printLog(tag, "封面:" + item.getCoverUrl());
            Debug.printLog(tag, "开始获取简介");
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
            Debug.printLog(tag, "简介:" + item.getIntroduce());
            Debug.printLog(tag, "开始获取书箱网址");
            String resultUrl = analyzer.getString(bookSourceBean.getRuleSearchNoteUrl(), true);
            item.setNoteUrl(isEmpty(resultUrl) ? baseUrl : resultUrl);
            Debug.printLog(tag, "书箱网址:" + item.getNoteUrl());
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
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleSearchCoverUrl(), true));
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
            String resultUrl = analyzer.getString(bookSourceBean.getRuleSearchNoteUrl(), true);
            item.setNoteUrl(isEmpty(resultUrl) ? baseUrl : resultUrl);
            return item;
        }
        return null;
    }
}