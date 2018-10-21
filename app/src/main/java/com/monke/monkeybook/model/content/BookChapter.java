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
import java.util.Objects;

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
            List<ChapterListBean> chapterList = webChapterBean.getData();

            while (!TextUtils.isEmpty(webChapterBean.getNextUrl())) {
                Call<String> call = DefaultModelImpl.getRetrofitString(bookSourceBean.getBookSourceUrl())
                        .create(IHttpGetApi.class).getWebContentCall(webChapterBean.getNextUrl(), AnalyzeHeaders.getMap(bookSourceBean.getHttpUserAgent()));
                String response = "";
                try {
                    response = call.execute().body();
                } catch (Exception exception) {
                    if (!e.isDisposed()) {
                        e.onError(exception);
                    }
                }
                webChapterBean = analyzeChapterList(response, webChapterBean.getNextUrl(), ruleChapterList);
                chapterList.addAll(webChapterBean.getData());
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
        String nextUrl = "";
        Document doc = Jsoup.parse(s);
        AnalyzeElement analyzeElement;
        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
            analyzeElement = new AnalyzeElement(doc, chapterUrl);
            nextUrl = analyzeElement.getResult(bookSourceBean.getRuleChapterUrlNext());
            if (Objects.equals(nextUrl, chapterUrl)) {
                nextUrl = "";
            }
        }
        Elements elements = AnalyzeElement.getElements(doc, ruleChapterList);
        for (Element element : elements) {
            analyzeElement = new AnalyzeElement(element, chapterUrl);
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(analyzeElement.getResult(bookSourceBean.getRuleContentUrl()));   //id
            temp.setDurChapterName(analyzeElement.getResult(bookSourceBean.getRuleChapterName()));
            temp.setTag(tag);
            if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                temp.setDurChapterIndex(chapterBeans.size());
                chapterBeans.add(temp);
            }
        }
        return new WebChapterBean<>(chapterBeans, nextUrl);
    }

    private class WebChapterBean<T> {
        private T data;

        private String nextUrl;

        private WebChapterBean(T data, String nextUrl) {
            this.data = data;
            this.nextUrl = nextUrl;
        }

        private T getData() {
            return data;
        }

        private String getNextUrl() {
            return nextUrl;
        }

    }
}
