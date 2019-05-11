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
import com.kunfei.bookshelf.utils.NetworkUtil;

import org.jsoup.nodes.Element;
import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

class BookChapter {
    private String tag;
    private BookSourceBean bookSourceBean;
    private AnalyzeRule analyzer;

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

            List<String> chapterUrlS = new ArrayList<>(webChapterBean.nextUrlList);
            if (chapterUrlS.size() > 1) {
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
            } else if (chapterUrlS.size() == 1) {
                List<String> usedUrl = new ArrayList<>();
                usedUrl.add(bookShelfBean.getBookInfoBean().getChapterUrl());
                while (webChapterBean.nextUrlList.size() > 0 && !usedUrl.contains(chapterUrlS.get(0))) {
                    usedUrl.add(chapterUrlS.get(0));
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrlS.get(0), headerMap, tag);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                                .blockingFirst();
                        body = response.body();
                        webChapterBean = analyzeChapterList(body, chapterUrlS.get(0), ruleChapterList, false);
                        chapterList.addAll(webChapterBean.data);
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
            }
            finish(dx, chapterList, e);
        });
    }

    private void finish(boolean dx, List<ChapterListBean> chapterList, Emitter<List<ChapterListBean>> emitter) {
        //去除重复,保留后面的,先倒序,从后面往前判断
        if (!dx) {
            Collections.reverse(chapterList);
        }
        LinkedHashSet<ChapterListBean> lh = new LinkedHashSet<>(chapterList);
        chapterList = new ArrayList<>(lh);
        Collections.reverse(chapterList);
        Debug.printLog(tag, "-目录解析完成");
        emitter.onNext(chapterList);
        emitter.onComplete();
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String chapterUrl, String ruleChapterList, boolean printLog) throws Exception {
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        List<String> nextUrlList = new ArrayList<>();

        analyzer.setContent(s, chapterUrl);
        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
            Debug.printLog(tag, "┌获取目录下一页网址", printLog);
            nextUrlList = analyzer.getStringList(bookSourceBean.getRuleChapterUrlNext(), true);
            int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
            if (thisUrlIndex != -1) {
                nextUrlList.remove(thisUrlIndex);
            }
            Debug.printLog(tag, "└" + nextUrlList.toString(), printLog);
        }
        boolean allInOne = false;
        if (ruleChapterList.startsWith("+")){
            allInOne = true;
            ruleChapterList = ruleChapterList.substring(1);
        }
        Debug.printLog(tag, "┌解析目录列表", printLog);
        List<Object> collections = analyzer.getElements(ruleChapterList);
        Debug.printLog(tag, "└找到 " + collections.size() + " 个章节", printLog);
        if (collections.isEmpty()) {
            return new WebChapterBean<>(chapterBeans, new LinkedHashSet<>(nextUrlList));
        }
        String name = "";
        String url = "";
        if (allInOne) {
            String nameRule = bookSourceBean.getRuleChapterName();
            String urlRule = bookSourceBean.getRuleContentUrl();
            Object object0 = collections.get(0);
            Debug.printLog(tag, "┌获取章节名称");
            if(object0 instanceof NativeObject){
                name = String.valueOf(((NativeObject)object0).get(nameRule));
            } else if(object0 instanceof Element){
                name = ((Element)object0).text();
            }
            Debug.printLog(tag, "└" + name);
            Debug.printLog(tag, "┌获取章节网址");
            if(object0 instanceof NativeObject){
                url = String.valueOf(((NativeObject)object0).get(urlRule));
            } else if(object0 instanceof Element){
                url = ((Element)object0).attr(urlRule);
            }
            Debug.printLog(tag, "└" + url);

            for (Object object: collections) {
                if(object instanceof NativeObject){
                    name = String.valueOf(((NativeObject)object).get(nameRule));
                    url = String.valueOf(((NativeObject)object).get(urlRule));
                } else if(object instanceof Element){
                    name = ((Element)object).text();
                    url = ((Element)object).attr(urlRule);
                }
                if (!isEmpty(name) && !isEmpty(url)) {
                    ChapterListBean temp = new ChapterListBean();
                    temp.setTag(tag);
                    temp.setDurChapterName(name);
                    temp.setDurChapterUrl(NetworkUtil.getAbsoluteURL(chapterUrl, url));
                    chapterBeans.add(temp);
                }
            }
            return new WebChapterBean<>(chapterBeans, new LinkedHashSet<>(nextUrlList));
        }

        List<AnalyzeRule.SourceRule> nameRule = analyzer.splitSourceRule(bookSourceBean.getRuleChapterName());
        List<AnalyzeRule.SourceRule> urlRule = analyzer.splitSourceRule(bookSourceBean.getRuleContentUrl());
        for (int i = 0; i < collections.size(); i++) {
            Object object = collections.get(i);
            analyzer.setContent(object, chapterUrl);
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
        return new WebChapterBean<>(chapterBeans, new LinkedHashSet<>(nextUrlList));
    }

    private class WebChapterBean<T> {
        private T data;

        private LinkedHashSet<String> nextUrlList;

        private WebChapterBean(T data, LinkedHashSet<String> nextUrlList) {
            this.data = data;
            this.nextUrlList = nextUrlList;
        }
    }
}