package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.dao.BookChapterBeanDao;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import io.reactivex.Observable;
import retrofit2.Response;

import static com.kunfei.bookshelf.constant.AppConstant.JS_PATTERN;

class BookContent {
    private String tag;
    private BookSourceBean bookSourceBean;
    private String ruleBookContent;
    private String baseUrl;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        ruleBookContent = bookSourceBean.getRuleBookContent();
        if (ruleBookContent.startsWith("$") && !ruleBookContent.startsWith("$.")) {
            ruleBookContent = ruleBookContent.substring(1);
            Matcher jsMatcher = JS_PATTERN.matcher(ruleBookContent);
            if (jsMatcher.find()) {
                ruleBookContent = ruleBookContent.replace(jsMatcher.group(), "");
            }
        }
    }

    Observable<BookContentBean> analyzeBookContent(final Response<String> response, final BaseChapterBean chapterBean, final BaseChapterBean nextChapterBean, BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        baseUrl = NetworkUtils.getUrl(response);
        return analyzeBookContent(response.body(), chapterBean, nextChapterBean, bookShelfBean, headerMap);
    }

    Observable<BookContentBean> analyzeBookContent(final String s, final BaseChapterBean chapterBean, final BaseChapterBean nextChapterBean, BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_content_error) + chapterBean.getDurChapterUrl()));
                return;
            }
            if (TextUtils.isEmpty(baseUrl)) {
                baseUrl = NetworkUtils.getAbsoluteURL(bookShelfBean.getBookInfoBean().getChapterUrl(), chapterBean.getDurChapterUrl());
            }
            if (StringUtils.isJsonType(s) && !MApplication.getInstance().getDonateHb()) {
                e.onError(new VipThrowable());
                e.onComplete();
                return;
            }
            Debug.printLog(tag, "┌成功获取正文页");
            Debug.printLog(tag, "└" + baseUrl);
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterIndex(chapterBean.getDurChapterIndex());
            bookContentBean.setDurChapterUrl(chapterBean.getDurChapterUrl());
            bookContentBean.setTag(tag);
            AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean);
            WebContentBean webContentBean = analyzeBookContent(analyzer, s, chapterBean.getDurChapterUrl(), baseUrl);
            bookContentBean.setDurChapterContent(webContentBean.content);

            /*
             * 处理分页
             */
            if (!TextUtils.isEmpty(webContentBean.nextUrl)) {
                List<String> usedUrlList = new ArrayList<>();
                usedUrlList.add(chapterBean.getDurChapterUrl());
                BaseChapterBean nextChapter;
                if (nextChapterBean != null) {
                    nextChapter = nextChapterBean;
                } else {
                    nextChapter = DbHelper.getDaoSession().getBookChapterBeanDao().queryBuilder()
                            .where(BookChapterBeanDao.Properties.NoteUrl.eq(chapterBean.getNoteUrl()),
                                    BookChapterBeanDao.Properties.DurChapterIndex.eq(chapterBean.getDurChapterIndex() + 1))
                            .build().unique();
                }

                while (!TextUtils.isEmpty(webContentBean.nextUrl) && !usedUrlList.contains(webContentBean.nextUrl)) {
                    usedUrlList.add(webContentBean.nextUrl);
                    if (nextChapter != null && NetworkUtils.getAbsoluteURL(baseUrl, webContentBean.nextUrl).equals(NetworkUtils.getAbsoluteURL(baseUrl, nextChapter.getDurChapterUrl()))) {
                        break;
                    }
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(webContentBean.nextUrl, headerMap, tag);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl).blockingFirst();
                        body = response.body();
                        webContentBean = analyzeBookContent(analyzer, body, webContentBean.nextUrl, baseUrl);
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

    private WebContentBean analyzeBookContent(AnalyzeRule analyzer, final String s, final String chapterUrl, String baseUrl) throws Exception {
        WebContentBean webContentBean = new WebContentBean();

        analyzer.setContent(s, NetworkUtils.getAbsoluteURL(baseUrl, chapterUrl));
        Debug.printLog(tag, 1, "┌解析正文内容");
        webContentBean.content = StringUtils.formatHtml(analyzer.getString(ruleBookContent));
        Debug.printLog(tag, 1, "└" + webContentBean.content);
        String nextUrlRule = bookSourceBean.getRuleContentUrlNext();
        if (!TextUtils.isEmpty(nextUrlRule)) {
            Debug.printLog(tag, 1, "┌解析下一页url");
            webContentBean.nextUrl = analyzer.getString(nextUrlRule, true);
            Debug.printLog(tag, 1, "└" + webContentBean.nextUrl);
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
