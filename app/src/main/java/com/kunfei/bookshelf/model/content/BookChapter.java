package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSoup;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByXPath;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSonPath;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByJSoup;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByXPath;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.utils.StringUtils;

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

class BookChapter {
    private String tag;
    private BookSourceBean bookSourceBean;
    private AnalyzeByXPath analyzeByXPath = new AnalyzeByXPath();
    private AnalyzeByJSoup analyzeByJSoup = new AnalyzeByJSoup();
    private AnalyzeByJSonPath analyzeByJSonPath = new AnalyzeByJSonPath();

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
            analyzeByJSoup = new AnalyzeByJSoup();
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
            //去除重复,保留后面的,先倒序,从后面往前判断
            if (!dx) {
                Collections.reverse(chapterList);
            }
            List<ChapterListBean> chapterListQc = new ArrayList<>();
            for (ChapterListBean chapterListBean : chapterList) {
                if (!chapterListQc.contains(chapterListBean)) {
                    chapterListQc.add(chapterListBean);
                }
            }
            Collections.reverse(chapterListQc);
            e.onNext(chapterListQc);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String chapterUrl, String ruleChapterList) {
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        List<String> nextUrlList = new ArrayList<>();
        SourceRule sourceRule;
        if (!StringUtils.isJSONType(s)) {
            Document doc = Jsoup.parse(s);
            analyzeByXPath.parse(doc);
            analyzeByJSoup.parse(doc, chapterUrl);
            if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
                sourceRule = new SourceRule(bookSourceBean.getRuleChapterUrlNext());
                switch (sourceRule.mode) {
                    case XPath:
                        nextUrlList = analyzeByXPath.getStringList(sourceRule.rule, chapterUrl);
                        break;
                    default:
                        nextUrlList = analyzeByJSoup.getAllResultList(sourceRule.rule);
                }
            }
            int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
            if (thisUrlIndex != -1) {
                nextUrlList.remove(thisUrlIndex);
            }
            Elements elements;
            sourceRule = new SourceRule(ruleChapterList);
            switch (sourceRule.mode) {
                case XPath:
                    elements = analyzeByXPath.getElements(sourceRule.rule);
                    break;
                default:
                    elements = analyzeByJSoup.getElements(doc, sourceRule.rule);
            }
            SourceRule ruleChapterName = new SourceRule(bookSourceBean.getRuleChapterName());
            SourceRule ruleContentUrl = new SourceRule(bookSourceBean.getRuleContentUrl());
            for (Element element : elements) {
                if (ruleChapterName.mode == SourceRule.Mode.XPath || ruleContentUrl.mode == SourceRule.Mode.XPath) {
                    analyzeByXPath.parse(element.children());
                }
                analyzeByJSoup.parse(element, chapterUrl);
                ChapterListBean temp = new ChapterListBean();
                temp.setTag(tag);
                temp.setDurChapterName(analyzeToString(bookSourceBean.getRuleChapterName()));
                temp.setDurChapterUrl(analyzeToString(bookSourceBean.getRuleContentUrl(), chapterUrl));
                if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                    chapterBeans.add(temp);
                }
            }
        } else {
            analyzeByJSonPath.parse(s);
            sourceRule = new SourceRule(ruleChapterList);
            List<Object> objects = analyzeByJSonPath.readList(sourceRule.rule);
            for (Object object : objects) {
                analyzeByJSonPath.parse(object);
                ChapterListBean temp = new ChapterListBean();
                temp.setTag(tag);
                sourceRule = new SourceRule(bookSourceBean.getRuleChapterName());
                temp.setDurChapterName(analyzeByJSonPath.read(sourceRule.rule));
                sourceRule = new SourceRule(bookSourceBean.getRuleContentUrl());
                temp.setDurChapterUrl(analyzeByJSonPath.read(sourceRule.rule));
                chapterBeans.add(temp);
            }
        }
        return new WebChapterBean<>(chapterBeans, nextUrlList);
    }

    private String analyzeToString(String rule) {
        return analyzeToString(rule, null);
    }

    private String analyzeToString(String rule, String baseUrl) {
        SourceRule sourceRule = new SourceRule(rule);
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

    private class WebChapterBean<T> {
        private T data;

        private List<String> nextUrlList;

        private WebChapterBean(T data, List<String> nextUrlList) {
            this.data = data;
            this.nextUrlList = nextUrlList;
        }
    }
}
