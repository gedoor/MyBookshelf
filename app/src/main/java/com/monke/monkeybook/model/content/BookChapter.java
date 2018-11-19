package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

import static android.text.TextUtils.isEmpty;

public class BookChapter {
    private String tag;
    private BookSourceBean bookSourceBean;

    BookChapter(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
    }

    public Observable<List<ChapterListBean>> analyzeChapterList(final String s, final BookShelfBean bookShelfBean) {
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
            WebChapterBean<List<ChapterListBean>> webChapterBean = analyzeChapterList(s, bookShelfBean.getBookInfoBean().getChapterUrl(), ruleChapterList);
            List<ChapterListBean> chapterList = webChapterBean.data;

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
                while (webChapterBean.nextUrlList.size() > 0 && usedUrl.indexOf(webChapterBean.nextUrlList.get(0)) == -1) {
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
            if (dx) {
                Collections.reverse(chapterList);
            }
            e.onNext(chapterList);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String chapterUrl, String ruleChapterList) throws Exception {
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        List<String> nextUrlList = new ArrayList<>();
        Document doc = Jsoup.parse(s);
        AnalyzeElement analyzeElement;
        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
            analyzeElement = new AnalyzeElement(doc, chapterUrl);
            nextUrlList = analyzeElement.getAllResultList(bookSourceBean.getRuleChapterUrlNext());
        }
        int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
        if (thisUrlIndex != -1) {
            nextUrlList.remove(thisUrlIndex);
        }
        Elements elements = AnalyzeElement.getElements(doc, ruleChapterList);
        for (Element element : elements) {
            analyzeElement = new AnalyzeElement(element, chapterUrl);
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(analyzeElement.getAllResultList(bookSourceBean.getRuleContentUrl()).get(0));   //id
            temp.setDurChapterName(analyzeElement.getResult(bookSourceBean.getRuleChapterName()));
            temp.setTag(tag);
            if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                temp.setDurChapterIndex(chapterBeans.size());
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
