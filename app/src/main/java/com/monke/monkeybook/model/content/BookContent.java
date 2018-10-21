package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.model.ErrorAnalyContentManager;
import com.monke.monkeybook.model.analyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.reactivex.Observable;
import retrofit2.Call;

public class BookContent {
    private String tag;
    private BookSourceBean bookSourceBean;
    private String ruleBookContent;
    private boolean isAJAX;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        ruleBookContent = bookSourceBean.getRuleBookContent();
        if (ruleBookContent.startsWith("$")) {
            isAJAX = true;
            ruleBookContent = ruleBookContent.substring(1);
        }
    }

    public Observable<BookContentBean> analyzeBookContent(final String s, final String durChapterUrl, final int durChapterIndex) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("内容获取失败"));
                e.onComplete();
                return;
            }
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterIndex(durChapterIndex);
            bookContentBean.setDurChapterUrl(durChapterUrl);
            bookContentBean.setTag(tag);

            WebContentBean webContentBean = analyzeBookContent(s, durChapterUrl);
            bookContentBean.setDurChapterContent(webContentBean.content);
            bookContentBean.setRight(webContentBean.isRight);

            while (!TextUtils.isEmpty(webContentBean.nextUrl)) {
                Call<String> call = DefaultModelImpl.getRetrofitString(bookSourceBean.getBookSourceUrl())
                        .create(IHttpGetApi.class).getWebContentCall(webContentBean.nextUrl, AnalyzeHeaders.getMap(bookSourceBean.getHttpUserAgent()));
                String response = "";
                try {
                    response = call.execute().body();
                } catch (Exception exception) {
                    if (!e.isDisposed()) {
                        e.onError(exception);
                    }
                }
                webContentBean = analyzeBookContent(response, webContentBean.nextUrl);
                if (!TextUtils.isEmpty(webContentBean.content)) {
                    bookContentBean.setDurChapterContent(bookContentBean.getDurChapterContent() + "\n" + webContentBean.content);
                }
            }

            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

    private WebContentBean analyzeBookContent(final String s, final String chapterUrl) {
        WebContentBean webContentBean = new WebContentBean();
        try {
            Document doc = Jsoup.parse(s);
            AnalyzeElement analyzeElement = new AnalyzeElement(doc, chapterUrl);
            webContentBean.content = analyzeElement.getResult(ruleBookContent);
            webContentBean.isRight = true;
            if (!TextUtils.isEmpty(bookSourceBean.getRuleContentUrlNext())) {
                webContentBean.nextUrl = analyzeElement.getResult(bookSourceBean.getRuleContentUrlNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorAnalyContentManager.getInstance().writeNewErrorUrl(chapterUrl);
            webContentBean.content = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + ex.getMessage();
            webContentBean.isRight = false;
        }
        return webContentBean;
    }

    private class WebContentBean {
        private String content;
        private boolean isRight;
        private String nextUrl;

        private WebContentBean() {

        }
    }
}
