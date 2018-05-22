package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.WebChapterBean;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeJson;
import com.monke.monkeybook.utils.NetworkUtil;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

public class BookChapter {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;

    BookChapter(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    public Observable<BookShelfBean> analyzeChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(tag);
            WebChapterBean<List<ChapterListBean>> chapterList = analyzeChapterList(s, bookShelfBean.getNoteUrl(), bookShelfBean.getBookInfoBean().getChapterUrl(), bookShelfBean.getChapterList());
            if (bookShelfBean.getChapterListSize() < chapterList.getData().size()) {
                bookShelfBean.setHasUpdate(true);
                bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
            }
            bookShelfBean.getBookInfoBean().setChapterList(chapterList.getData());
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList (String s, String novelUrl, String chapterUrl, List<ChapterListBean> chapterListBeansOld) throws Exception{
        boolean dx = false;
        String ruleChapterList = bookSourceBean.getRuleChapterList();
        if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
            dx = true;
            ruleChapterList = ruleChapterList.substring(1);
        }
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        if (bookSourceBean.getRuleChapterName().contains("JSON")) {
            JSONObject jsonObject = new JSONObject(s);
            List<JSONObject> jsonObjectList = AnalyzeJson.getJsonObjects(jsonObject, ruleChapterList);
            int x;
            for (int i = 0; i < jsonObjectList.size(); i++) {
                AnalyzeJson analyzeJson = new AnalyzeJson(jsonObjectList.get(i));
                ChapterListBean temp = new ChapterListBean();
                temp.setDurChapterIndex(i);
                temp.setDurChapterUrl(NetworkUtil.getAbsoluteURL(chapterUrl, analyzeJson.getResult(bookSourceBean.getRuleContentUrl())));
                temp.setDurChapterName(analyzeJson.getResult(bookSourceBean.getRuleChapterName()));
                temp.setNoteUrl(novelUrl);
                temp.setTag(tag);
                if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                    x = chapterListBeansOld.indexOf(temp);
                    if (x != -1) {
                        temp.setHasCache(chapterListBeansOld.get(x).getHasCache());
                    }
                    chapterBeans.add(temp);
                }
            }
        } else {
            Document doc = Jsoup.parse(s);
            Elements chapterList = AnalyzeElement.getElements(doc, ruleChapterList);
            int x;
            for (int i = 0; i < chapterList.size(); i++) {
                AnalyzeElement analyzeElement = new AnalyzeElement(chapterList.get(i), chapterUrl);
                ChapterListBean temp = new ChapterListBean();
                temp.setDurChapterIndex(i);
                temp.setDurChapterUrl(analyzeElement.getResult(bookSourceBean.getRuleContentUrl()));   //id
                temp.setDurChapterName(analyzeElement.getResult(bookSourceBean.getRuleChapterName()));
                temp.setNoteUrl(novelUrl);
                temp.setTag(tag);
                if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                    x = chapterListBeansOld.indexOf(temp);
                    if (x != -1) {
                        temp.setHasCache(chapterListBeansOld.get(x).getHasCache());
                    }
                    chapterBeans.add(temp);
                }
            }
        }

        if (dx) {
            Collections.reverse(chapterBeans);
        }
        return new WebChapterBean<>(chapterBeans, false);
    }
}
