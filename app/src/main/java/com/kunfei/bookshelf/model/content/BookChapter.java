package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;

import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

class BookChapter {
    private String tag;
    private BookSourceBean bookSourceBean;
    private AnalyzeRule analyzer;
    private List<AnalyzeRule.SourceRule> nameRule;
    private List<AnalyzeRule.SourceRule> urlRule;

    BookChapter(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<List<ChapterListBean>> analyzeChapterList(final String s, final BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_chapter_list_error) + bookShelfBean.getBookInfoBean().getChapterUrl()));
                return;
            } else {
                Debug.printLog(tag, "┌成功获取目录页");
                Debug.printLog(tag, "└" + bookShelfBean.getBookInfoBean().getChapterUrl());
            }
            bookShelfBean.setTag(tag);
            analyzer = new AnalyzeRule(bookShelfBean);
            boolean dx = false;
            String ruleChapterList = bookSourceBean.getRuleChapterList();
            if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
                dx = true;
                ruleChapterList = ruleChapterList.substring(1);
            }
            WebChapterBean<List<ChapterListBean>> webChapterBean = analyzeChapterList(s, bookShelfBean.getBookInfoBean().getChapterUrl(), ruleChapterList, true);
            List<ChapterListBean> chapterList = webChapterBean.data;

            if (webChapterBean.nextUrlList.size() > 1) {
                List<String> chapterUrlS = new ArrayList<>(webChapterBean.nextUrlList);
                for (int i = 0; i < chapterUrlS.size(); i++) {
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrlS.get(i), headerMap, tag);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                                .blockingFirst();
                        body = response.body();
                        webChapterBean = analyzeChapterList(body, chapterUrlS.get(i), ruleChapterList, false);
                        chapterList.addAll(webChapterBean.data);
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
            } else if (webChapterBean.nextUrlList.size() == 1) {
                List<String> usedUrl = new ArrayList<>();
                usedUrl.add(bookShelfBean.getBookInfoBean().getChapterUrl());
                while (webChapterBean.nextUrlList.size() > 0 && !usedUrl.contains(webChapterBean.nextUrlList.get(0))) {
                    usedUrl.add(webChapterBean.nextUrlList.get(0));
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(webChapterBean.nextUrlList.get(0), headerMap, tag);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                                .blockingFirst();
                        body = response.body();
                        webChapterBean = analyzeChapterList(body, webChapterBean.nextUrlList.get(0), ruleChapterList, false);
                        chapterList.addAll(webChapterBean.data);
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
            }
            //去除重复,保留后面的,先倒序,从后面往前判断
            if (!dx) {
                Collections.reverse(chapterList);
            }
            LinkedHashSet<ChapterListBean> lh = new LinkedHashSet<>(chapterList);
            chapterList = new ArrayList<>(lh);
            Collections.reverse(chapterList);
            e.onNext(chapterList);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String chapterUrl, String ruleChapterList, boolean printLog) throws Exception {
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        List<String> nextUrlList = new ArrayList<>();

        analyzer.setContent(s, chapterUrl);
        nameRule = analyzer.splitSourceRule(bookSourceBean.getRuleChapterName());
        urlRule = analyzer.splitSourceRule(bookSourceBean.getRuleContentUrl());
        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
            Debug.printLog(tag, "┌获取目录下一页网址", printLog);
            nextUrlList = analyzer.getStringList(bookSourceBean.getRuleChapterUrlNext(), true);
            int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
            if (thisUrlIndex != -1) {
                nextUrlList.remove(thisUrlIndex);
            }
            Debug.printLog(tag, "└" + nextUrlList.toString(), printLog);
        }

        if(ruleChapterList.startsWith("AllInOne")) {
            Debug.printLog(tag, "┌解析目录列表", printLog);
            List<Object> collections = analyzer.getElements(ruleChapterList.substring(8));
            Debug.printLog(tag, "└找到 " + collections.size() + " 个章节", printLog);
            NativeObject nativeObject = (NativeObject)collections.get(0);
            Debug.printLog(tag, "┌获取章节名称");
            String nameRule = bookSourceBean.getRuleChapterName();
            if(isEmpty(nameRule)){
                nameRule = "name";
            }
            String name = String.valueOf(nativeObject.get(nameRule));
            Debug.printLog(tag, "└" + name);
            Debug.printLog(tag, "┌获取章节网址");
            String urlRule = bookSourceBean.getRuleContentUrl();
            if(isEmpty(urlRule)){
                urlRule = "url";
            }
            String url = String.valueOf(nativeObject.get(urlRule));
            Debug.printLog(tag, "└" + url);

            for (Object object: collections) {
                nativeObject = (NativeObject)object;
                name = String.valueOf(nativeObject.get(nameRule));
                url = String.valueOf(nativeObject.get(urlRule));
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

        Debug.printLog(tag, "┌解析目录列表", printLog);
        List<Object> collections = analyzer.getElements(ruleChapterList);
        Debug.printLog(tag, "└找到 " + collections.size() + " 个章节", printLog);
        for (int i = 0; i < collections.size(); i++) {
            Object object = collections.get(i);
            analyzer.setContent(object, chapterUrl);
            String name;
            String url;
            printLog = printLog && i == 0;
            Debug.printLog(tag, "┌获取章节名称", printLog);
            name = analyzer.getString(nameRule, false);
            Debug.printLog(tag, "└" + name, printLog);
            Debug.printLog(tag, "┌获取章节网址", printLog);
            url = analyzer.getString(urlRule, true);
            Debug.printLog(tag, "└" + url, printLog);

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