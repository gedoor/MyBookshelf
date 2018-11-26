package com.monke.monkeybook.model.content;

import android.text.TextUtils;
import android.util.SparseArray;

import com.monke.monkeybook.bean.BaseChapterBean;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

class BookContent {
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

    Observable<BookContentBean> analyzeBookContent(final String s, final BaseChapterBean chapterBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("内容获取失败"));
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

    private String getLib99Content(Document doc) {
        if (doc == null) return "";
        StringBuilder builder = new StringBuilder();
        SparseArray textArray = new SparseArray();
        String[] clients = StringUtils.base64Decode(doc.select("meta[name=client]").attr("content")).split("[A-Z]+%");
        Element content = doc.getElementById("content");
        content.select("abbr,bdi,command,details,figure,footer,keygen,mark,acronym,bdo,big,cite,code,dfn,kbd,q,s,samp,strike,tt,u,var,site").remove();
        content.select("br").after("\n").remove();
        int star = 0;
        Elements childNotes = content.children();
        for (int i = 0, size = childNotes.size(); i < size; i++) {
            if (childNotes.get(i).tagName().equals("h2")) {
                star = i + 1;
            }
            if (childNotes.get(i).tagName().equals("div") && !childNotes.get(i).className().equals("chapter")) {
                break;
            }
        }
        int j = 0;
        for (int i = 0, size = clients.length; i < size; i++) {
            int item = Integer.parseInt(clients[i]);
            if (item < 3) {
                textArray.append(item, childNotes.get(i + star).wholeText());
                j++;
            } else {
                textArray.append(item - j, childNotes.get(i + star).wholeText());
                j += 2;
            }
        }
        for (int i = 0, size = textArray.size(); i < size; i++) {
            builder.append(textArray.valueAt(i)).append("\n");
        }
        return builder.toString();
    }

    private WebContentBean analyzeBookContent(final String s, final String chapterUrl) {
        WebContentBean webContentBean = new WebContentBean();
        Document doc = Jsoup.parse(s);
        if (chapterUrl.matches("^https?://(www\\.)?99lib\\.net/.*")) {
            webContentBean.content = getLib99Content(doc);
            webContentBean.nextUrl = "";
        } else {
            AnalyzeElement analyzeElement = new AnalyzeElement(doc, chapterUrl);
            webContentBean.content = analyzeElement.getResult(ruleBookContent);
            if (!TextUtils.isEmpty(bookSourceBean.getRuleContentUrlNext())) {
                webContentBean.nextUrl = analyzeElement.getResultUrl(bookSourceBean.getRuleContentUrlNext());
            }
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
