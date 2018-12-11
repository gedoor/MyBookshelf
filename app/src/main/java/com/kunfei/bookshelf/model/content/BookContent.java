package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.dao.ChapterListBeanDao;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

class BookContent {
    private String tag;
    private BookSourceBean bookSourceBean;
    private String ruleBookContent;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        ruleBookContent = bookSourceBean.getRuleBookContent();
        if (ruleBookContent.startsWith("$")) {
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

        AnalyzeRule analyzer = new AnalyzeRule();
        analyzer.setContent(s);

        webContentBean.content = analyzer.getString(ruleBookContent);

        String nextUrlRule = bookSourceBean.getRuleContentUrlNext();
        if (!TextUtils.isEmpty(nextUrlRule)) {
            webContentBean.nextUrl = analyzer.getString(nextUrlRule, chapterUrl);
        }

        return webContentBean;
    }

    private class WebContentBean {
        private String content;
        private String nextUrl;

        private WebContentBean() {

        }
    }
}
