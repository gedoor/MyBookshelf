package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.dao.ChapterListBeanDao;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;

class BookContent {
    private String tag;
    private BookSourceBean bookSourceBean;
    private String ruleBookContent;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        ruleBookContent = bookSourceBean.getRuleBookContent();
        if (ruleBookContent.startsWith("$") && !ruleBookContent.startsWith("$.")) {
            ruleBookContent = ruleBookContent.substring(1);
        }
    }

    Observable<BookContentBean> analyzeBookContent(final Response<String> response, final BaseChapterBean chapterBean, Map<String, String> headerMap) {
        return analyzeBookContent(response.body(), chapterBean, headerMap);
    }

    Observable<BookContentBean> analyzeBookContent(final String s, final BaseChapterBean chapterBean, Map<String, String> headerMap) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_content_error)));
                return;
            }

            if (StringUtils.isJsonType(s) && !MApplication.getInstance().getDonateHb()) {
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
                ChapterListBean nextChapter = DbHelper.getDaoSession().getChapterListBeanDao().queryBuilder()
                        .where(ChapterListBeanDao.Properties.NoteUrl.eq(chapterBean.getNoteUrl()), ChapterListBeanDao.Properties.DurChapterIndex.eq(chapterBean.getDurChapterIndex() + 1))
                        .build().unique();
                while (!TextUtils.isEmpty(webContentBean.nextUrl) && !usedUrlList.contains(webContentBean.nextUrl)) {
                    usedUrlList.add(webContentBean.nextUrl);
                    if (nextChapter != null && webContentBean.nextUrl.equals(nextChapter.getDurChapterUrl())) {
                        break;
                    }
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(webContentBean.nextUrl, null, null, headerMap);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl).blockingFirst();
                        body = response.body();
                        webContentBean = analyzeBookContent(body, webContentBean.nextUrl);
                        if (!TextUtils.isEmpty(webContentBean.content)) {
                            bookContentBean.setDurChapterContent(bookContentBean.getDurChapterContent() + "\n" + webContentBean.content);
                        }
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

    private WebContentBean analyzeBookContent(final String s, final String chapterUrl) throws Exception {
        WebContentBean webContentBean = new WebContentBean();

        AnalyzeRule analyzer = new AnalyzeRule(null);
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
