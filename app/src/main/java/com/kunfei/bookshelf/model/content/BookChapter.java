package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeCollection;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

import static android.text.TextUtils.isEmpty;

class BookChapter {
    private String tag;
    private BookSourceBean bookSourceBean;

    BookChapter(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<List<ChapterListBean>> analyzeChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("目录获取失败"));
                e.onComplete();
                return;
            }

            bookShelfBean.setTag(tag);
            boolean dx = false;
            String ruleChapterList = bookSourceBean.getRuleChapterList();
            if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
                dx = true;
                ruleChapterList = ruleChapterList.substring(1);
            }
            WebChapterBean<LinkedHashSet<ChapterListBean>> webChapterBean = analyzeChapterList(s, bookShelfBean.getBookInfoBean().getChapterUrl(), ruleChapterList);
            LinkedHashSet<ChapterListBean> chapterList = webChapterBean.data;

            if (webChapterBean.nextUrlList.size() > 1) {
                List<String> chapterUrlS = new ArrayList<>(webChapterBean.nextUrlList);
                for (int i = 0; i < chapterUrlS.size(); i++) {
                    Call<String> call = DefaultModel.getRetrofitString(bookSourceBean.getBookSourceUrl())
                            .create(IHttpGetApi.class).getWebContentCall(chapterUrlS.get(i), AnalyzeHeaders.getMap(bookSourceBean.getHttpUserAgent()));
                    String response = "";
                    try {
                        response = call.execute().body();
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                    webChapterBean = analyzeChapterList(response, chapterUrlS.get(i), ruleChapterList);
                    chapterList.addAll(webChapterBean.data);
                }
            } else if (webChapterBean.nextUrlList.size() == 1) {
                List<String> usedUrl = new ArrayList<>();
                usedUrl.add(bookShelfBean.getBookInfoBean().getChapterUrl());
                while (webChapterBean.nextUrlList.size() > 0 && !usedUrl.contains(webChapterBean.nextUrlList.get(0))) {
                    usedUrl.add(webChapterBean.nextUrlList.get(0));
                    Call<String> call = DefaultModel.getRetrofitString(bookSourceBean.getBookSourceUrl())
                            .create(IHttpGetApi.class).getWebContentCall(webChapterBean.nextUrlList.get(0), AnalyzeHeaders.getMap(bookSourceBean.getHttpUserAgent()));
                    String response = "";
                    try {
                        response = call.execute().body();
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                    webChapterBean = analyzeChapterList(response, webChapterBean.nextUrlList.get(0), ruleChapterList);
                    chapterList.addAll(webChapterBean.data);
                }
            }
            List<ChapterListBean> cList = new ArrayList<>(chapterList);
            if (dx) {
                Collections.reverse(cList);
            }
            e.onNext(cList);
            e.onComplete();
        });
    }

    private WebChapterBean<LinkedHashSet<ChapterListBean>> analyzeChapterList(String s, String chapterUrl, String ruleChapterList) {
        LinkedHashSet<ChapterListBean> chapterBeans = new LinkedHashSet<>();
        List<String> nextUrlList = new ArrayList<>();

        AnalyzeRule analyzer = new AnalyzeRule();
        analyzer.setContent(s);
        analyzer.setBaseUrl(chapterUrl);

        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
            nextUrlList = analyzer.getStringList(bookSourceBean.getRuleChapterUrlNext());

            int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
            if (thisUrlIndex != -1) {
                nextUrlList.remove(thisUrlIndex);
            }
        }

        AnalyzeCollection collections = analyzer.getElements(ruleChapterList);
        while (collections.hasNext()) {
            AnalyzeRule anaer = collections.next();
            String name = anaer.getString(bookSourceBean.getRuleChapterName());
            String url = anaer.getString(bookSourceBean.getRuleContentUrl());
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