package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSoup;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByXPath;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.dao.ChapterListBeanDao;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSonPath;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSoup;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByXPath;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

class BookContent {
    private String tag;
    private BookSourceBean bookSourceBean;
    private String ruleBookContent;
    private boolean isAJAX;
    private AnalyzeByXPath analyzeByXPath = new AnalyzeByXPath();
    private AnalyzeByJSoup analyzeByJSoup = new AnalyzeByJSoup();
    private AnalyzeByJSonPath analyzeByJSonPath = new AnalyzeByJSonPath();

    BookContent(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        ruleBookContent = bookSourceBean.getRuleBookContent();
        if (ruleBookContent.startsWith("$")) {
            isAJAX = true;
            ruleBookContent = ruleBookContent.substring(1);
        }
    }

    Observable<BookContentBean> analyzeBookContent(final String s, final BaseChapterBean chapterBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("内容获取失败"));
                e.onComplete();
                return;
            }
            if (StringUtils.isJSONType(s) && !MApplication.getInstance().getDonateHb()) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.donate_s)));
                e.onComplete();
                return;
            }
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterIndex(chapterBean.getDurChapterIndex());
            bookContentBean.setDurChapterUrl(chapterBean.getDurChapterUrl());
            bookContentBean.setTag(tag);

            WebContentBean webContentBean = analyzeBookContent(s, chapterBean.getDurChapterUrl());
            bookContentBean.setDurChapterContent(webContentBean.content);

            /*
             * 处理分页
             */
            if (!TextUtils.isEmpty(webContentBean.nextUrl)) {
                List<String> usedUrlList = new ArrayList<>();
                usedUrlList.add(chapterBean.getDurChapterUrl());
                ChapterListBean nextChapter = DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder()
                        .where(ChapterListBeanDao.Properties.NoteUrl.eq(chapterBean.getNoteUrl()), ChapterListBeanDao.Properties.DurChapterIndex.eq(chapterBean.getDurChapterIndex() + 1))
                        .build().unique();
                while (!TextUtils.isEmpty(webContentBean.nextUrl) && !usedUrlList.contains(webContentBean.nextUrl)) {
                    usedUrlList.add(webContentBean.nextUrl);
                    if (nextChapter != null && webContentBean.nextUrl.equals(nextChapter.getDurChapterUrl())) {
                        break;
                    }
                    Call<String> call = DefaultModel.getRetrofitString(bookSourceBean.getBookSourceUrl())
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
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

    private WebContentBean analyzeBookContent(final String s, final String chapterUrl) {
        WebContentBean webContentBean = new WebContentBean();
        if (!StringUtils.isJSONType(s)) {
            Document doc = Jsoup.parse(s);
            SourceRule sourceRuleBookContent = new SourceRule(ruleBookContent);
            SourceRule sourceRuleContentUrlNext = new SourceRule(bookSourceBean.getRuleContentUrlNext());
            if (sourceRuleBookContent.mode == SourceRule.Mode.XPath || sourceRuleContentUrlNext.mode == SourceRule.Mode.XPath) {
                analyzeByXPath.parse(doc);
            }
            analyzeByJSoup.parse(doc, chapterUrl);
            webContentBean.content = analyzeToString(sourceRuleBookContent);
            if (!TextUtils.isEmpty(sourceRuleContentUrlNext.rule)) {
                webContentBean.nextUrl = analyzeToString(sourceRuleContentUrlNext, chapterUrl);
            }
        } else {
            analyzeByJSonPath.parse(s);
            SourceRule sourceRule = new SourceRule(ruleBookContent);
            webContentBean.content = analyzeByJSonPath.read(sourceRule.rule);
        }
        return webContentBean;
    }

    private String analyzeToString(SourceRule sourceRule) {
        return analyzeToString(sourceRule, null);
    }

    private String analyzeToString(SourceRule sourceRule, String baseUrl) {
        String result;
        switch (sourceRule.mode) {
            case XPath:
                result = analyzeByXPath.getString(sourceRule.rule, baseUrl);
                break;
            default:
                if (TextUtils.isEmpty(baseUrl)) {
                    result = analyzeByJSoup.getResult(sourceRule.rule);
                } else {
                    result = analyzeByJSoup.getResultUrl(sourceRule.rule);
                }
        }
        return result;
    }

    private class WebContentBean {
        private String content;
        private String nextUrl;

        private WebContentBean() {

        }
    }
}
