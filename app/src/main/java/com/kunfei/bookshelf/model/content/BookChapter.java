package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeCollection;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

class BookChapter {
    private String tag;
    private BookSourceBean bookSourceBean;
    private AnalyzeRule analyzer;

    BookChapter(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<List<ChapterListBean>> analyzeChapterList(final String s, final BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_chapter_list_error)));
                return;
            }
            analyzer = new AnalyzeRule(bookShelfBean);
            bookShelfBean.setTag(tag);
            boolean dx = false;
            String ruleChapterList = bookSourceBean.getRuleChapterList();
            if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
                dx = true;
                ruleChapterList = ruleChapterList.substring(1);
            }
            WebChapterBean<List<ChapterListBean>> webChapterBean = analyzeChapterList(s, bookShelfBean.getBookInfoBean().getChapterUrl(), ruleChapterList);
            List<ChapterListBean> chapterList = webChapterBean.data;

            if (webChapterBean.nextUrlList.size() > 1) {
                List<String> chapterUrlS = new ArrayList<>(webChapterBean.nextUrlList);
                for (int i = 0; i < chapterUrlS.size(); i++) {
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrlS.get(i), null, null, headerMap);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                                .blockingFirst();
                        body = response.body();
                        webChapterBean = analyzeChapterList(body, chapterUrlS.get(i), ruleChapterList);
                        chapterList.addAll(webChapterBean.data);
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
            } else if (webChapterBean.nextUrlList.size() == 1) {
                List<String> usedUrl = new ArrayList<>();
                usedUrl.add(bookShelfBean.getBookInfoBean().getChapterUrl());
                while (webChapterBean.nextUrlList.size() > 0 && !usedUrl.contains(webChapterBean.nextUrlList.get(0))) {
                    usedUrl.add(webChapterBean.nextUrlList.get(0));
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(webChapterBean.nextUrlList.get(0), null, null, headerMap);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                                .blockingFirst();
                        body = response.body();
                        webChapterBean = analyzeChapterList(body, webChapterBean.nextUrlList.get(0), ruleChapterList);
                        chapterList.addAll(webChapterBean.data);
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
            }
            //去除重复,保留后面的,先倒序,从后面往前判断
            if (!dx) {
                Collections.reverse(chapterList);
            }
            LinkedHashSet<ChapterListBean> lh = new LinkedHashSet<>(chapterList);
            chapterList = new ArrayList<>(lh);
            Collections.reverse(chapterList);
            e.onNext(chapterList);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String chapterUrl, String ruleChapterList) throws Exception {
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        List<String> nextUrlList = new ArrayList<>();

        analyzer.setContent(s);

        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
            nextUrlList = analyzer.getStringList(bookSourceBean.getRuleChapterUrlNext(), chapterUrl);

            int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
            if (thisUrlIndex != -1) {
                nextUrlList.remove(thisUrlIndex);
            }
        }

        AnalyzeCollection collections = analyzer.getElements(ruleChapterList);
        while (collections.hasNext()) {
            collections.next(analyzer);
            String name = analyzer.getString(bookSourceBean.getRuleChapterName());
            String url = analyzer.getString(bookSourceBean.getRuleContentUrl(), chapterUrl);
            if (!isEmpty(name) && !isEmpty(url)) {
                ChapterListBean temp = new ChapterListBean();
                temp.setTag(tag);
                temp.setDurChapterName(name);
                temp.setDurChapterUrl(url);
                chapterBeans.add(temp);
            }
        }

        return new WebChapterBean<>(chapterBeans, nextUrlList);
    }

    private class WebChapterBean<T> {
        private T data;

        private List<String> nextUrlList;

        private WebChapterBean(T data, List<String> nextUrlList) {
            this.data = data;
            this.nextUrlList = nextUrlList;
        }
    }
}