package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.WebChapterBean;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeJson;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.utils.NetworkUtil;

import org.json.JSONObject;
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

    public Observable<BookShelfBean> analyzeChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(tag);
            boolean dx = false;
            String ruleChapterList = bookSourceBean.getRuleChapterList();
            if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
                dx = true;
                ruleChapterList = ruleChapterList.substring(1);
            }
            WebChapterBean<List<ChapterListBean>> webChapterBean = analyzeChapterList(s, bookShelfBean.getNoteUrl(), bookShelfBean.getBookInfoBean().getChapterUrl(), bookShelfBean.getChapterList(), ruleChapterList);
            List<ChapterListBean> chapterListBeans = webChapterBean.getData();

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
                webChapterBean = analyzeChapterList(response, bookShelfBean.getNoteUrl(), webChapterBean.getNextUrl(), bookShelfBean.getChapterList(), ruleChapterList);
                chapterListBeans.addAll(webChapterBean.getData());
            }

            if (dx) {
                Collections.reverse(chapterListBeans);
            }
            for (int i = 0; i < chapterListBeans.size(); i++) {
                chapterListBeans.get(i).setDurChapterIndex(i);
            }
            if (bookShelfBean.getChapterListSize() < chapterListBeans.size()) {
                bookShelfBean.setHasUpdate(true);
                bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
            }
            bookShelfBean.getBookInfoBean().setChapterList(chapterListBeans);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList (String s, String novelUrl, String chapterUrl, List<ChapterListBean> chapterListBeansOld, String ruleChapterList) throws Exception{
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        String nextUrl = "";
        if (bookSourceBean.getRuleChapterName().contains("JSON")) {
            JSONObject jsonObject = new JSONObject(s);
            List<JSONObject> jsonObjectList = AnalyzeJson.getJsonObjects(jsonObject, ruleChapterList);
            int x;
            for (int i = 0; i < jsonObjectList.size(); i++) {
                AnalyzeJson analyzeJson = new AnalyzeJson(jsonObjectList.get(i));
                ChapterListBean temp = new ChapterListBean();
                temp.setDurChapterUrl(NetworkUtil.getAbsoluteURL(chapterUrl, analyzeJson.getResult(bookSourceBean.getRuleContentUrl())));
                temp.setDurChapterName(analyzeJson.getResult(bookSourceBean.getRuleChapterName()));
                temp.setNoteUrl(novelUrl);
                temp.setTag(tag);
                if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                    x = chapterListBeansOld.indexOf(temp);
                    if (x != -1) {
                        temp.setHasCache(chapterListBeansOld.get(x).getHasCache());
                    }
                    temp.setDurChapterIndex(chapterBeans.size());
                    chapterBeans.add(temp);
                }
            }
        } else {
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
            int x;
            for (Element element : elements) {
                analyzeElement = new AnalyzeElement(element, chapterUrl);
                ChapterListBean temp = new ChapterListBean();
                temp.setDurChapterUrl(analyzeElement.getResult(bookSourceBean.getRuleContentUrl()));   //id
                temp.setDurChapterName(analyzeElement.getResult(bookSourceBean.getRuleChapterName()));
                temp.setNoteUrl(novelUrl);
                temp.setTag(tag);
                if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                    x = chapterListBeansOld.indexOf(temp);
                    if (x != -1) {
                        temp.setHasCache(chapterListBeansOld.get(x).getHasCache());
                    }
                    temp.setDurChapterIndex(chapterBeans.size());
                    chapterBeans.add(temp);
                }
            }
        }
        return new WebChapterBean<>(chapterBeans, nextUrl);
    }
}
