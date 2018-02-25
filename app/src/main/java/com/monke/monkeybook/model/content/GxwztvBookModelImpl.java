//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.content;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.LibraryBean;
import com.monke.monkeybook.bean.LibraryKindBookListBean;
import com.monke.monkeybook.bean.LibraryNewBookBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.WebChapterBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.ErrorAnalyContentManager;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.model.impl.IGxwztvBookModel;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.presenter.LibraryPresenterImpl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

public class GxwztvBookModelImpl extends BaseModelImpl implements IGxwztvBookModel {
    public static final String TAG = "http://www.gxwztv.com";
    public static final String name = "梧州中文台";

    public static GxwztvBookModelImpl getInstance() {
        return new GxwztvBookModelImpl();
    }

    /**
     * 获取主页信息
     */
    @Override
    public Observable<LibraryBean> getLibraryData(final ACache aCache) {
        return getRetrofitString(MApplication.getInstance(), TAG)
                .create(IHttpGetApi.class)
                .getWebContent("")
                .flatMap(s -> {
            if (s != null && s.length() > 0 && aCache != null) {
                aCache.put(LibraryPresenterImpl.LIBRARY_CACHE_KEY, s);
            }
            return analyzeLibraryData(s);
        });
    }

    /**
     * 解析主页数据
     */
    @Override
    public Observable<LibraryBean> analyzeLibraryData(final String data) {
        return Observable.create(e -> {
            LibraryBean result = new LibraryBean();
            Document doc = Jsoup.parse(data);
            Element contentE = doc.getElementsByClass("container").get(0);
            //解析最新书籍
            Elements newBookEs = contentE.getElementsByClass("list-group-item text-nowrap modal-open");
            List<LibraryNewBookBean> libraryNewBooks = new ArrayList<>();
            for (int i = 0; i < newBookEs.size(); i++) {
                Element itemE = newBookEs.get(i).getElementsByTag("a").get(0);
                LibraryNewBookBean item = new LibraryNewBookBean(itemE.text(), TAG + itemE.attr("href"), TAG, "gxwztv.com");
                libraryNewBooks.add(item);
            }
            result.setLibraryNewBooks(libraryNewBooks);
            //////////////////////////////////////////////////////////////////////
            List<LibraryKindBookListBean> kindBooks = new ArrayList<>();
            //解析男频女频
            Elements hotEs = contentE.getElementsByClass("col-xs-12");
            for (int i = 1; i < hotEs.size(); i++) {
                LibraryKindBookListBean kindItem = new LibraryKindBookListBean();
                kindItem.setKindName(hotEs.get(i).getElementsByClass("panel-title").get(0).text());
                Elements bookEs = hotEs.get(i).getElementsByClass("panel-body").get(0).getElementsByTag("li");

                List<SearchBookBean> books = new ArrayList<>();
                for (int j = 0; j < bookEs.size(); j++) {
                    SearchBookBean searchBookBean = new SearchBookBean();
                    searchBookBean.setOrigin(name);
                    searchBookBean.setTag(TAG);
                    searchBookBean.setName(bookEs.get(j).getElementsByTag("span").get(0).text());
                    searchBookBean.setNoteUrl(TAG + bookEs.get(j).getElementsByTag("a").get(0).attr("href"));
                    searchBookBean.setCoverUrl(bookEs.get(j).getElementsByTag("img").get(0).attr("src"));
                    books.add(searchBookBean);
                }
                kindItem.setBooks(books);
                kindBooks.add(kindItem);
            }
            //解析部分分类推荐
            Elements kindEs = contentE.getElementsByClass("panel panel-info index-category-qk");
            for (int i = 0; i < kindEs.size(); i++) {
                LibraryKindBookListBean kindItem = new LibraryKindBookListBean();
                kindItem.setKindName(kindEs.get(i).getElementsByClass("panel-title").get(0).text());
                kindItem.setKindUrl(TAG + kindEs.get(i).getElementsByClass("listMore").get(0).getElementsByTag("a").get(0).attr("href"));

                List<SearchBookBean> books = new ArrayList<>();
                Element firstBookE = kindEs.get(i).getElementsByTag("dl").get(0);
                SearchBookBean firstBook = new SearchBookBean();
                firstBook.setTag(TAG);
                firstBook.setOrigin(name);
                firstBook.setName(firstBookE.getElementsByTag("a").get(1).text());
                firstBook.setNoteUrl(TAG + firstBookE.getElementsByTag("a").get(0).attr("href"));
                firstBook.setCoverUrl(firstBookE.getElementsByTag("a").get(0).getElementsByTag("img").get(0).attr("src"));
                firstBook.setKind(kindItem.getKindName());
                books.add(firstBook);

                Elements otherBookEs = kindEs.get(i).getElementsByClass("book_textList").get(0).getElementsByTag("li");
                for (int j = 0; j < otherBookEs.size(); j++) {
                    SearchBookBean item = new SearchBookBean();
                    item.setTag(TAG);
                    item.setOrigin(name);
                    item.setKind(kindItem.getKindName());
                    item.setNoteUrl(TAG + otherBookEs.get(j).getElementsByTag("a").get(0).attr("href"));
                    item.setName(otherBookEs.get(j).getElementsByTag("a").get(0).text());
                    books.add(item);
                }
                kindItem.setBooks(books);
                kindBooks.add(kindItem);
            }
            //////////////
            result.setKindBooks(kindBooks);
            e.onNext(result);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //搜索
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("keyword", content);
        queryMap.put("pn", String.valueOf(page - 1));
        return getRetrofitString(MApplication.getInstance(), TAG)
                .create(IHttpGetApi.class)
                .searchBook("/search.htm", queryMap)
                .flatMap(response -> analyzeSearchBook(response.body()));
    }

    private Observable<List<SearchBookBean>> analyzeSearchBook(final String s) {
        return Observable.create(e -> {
            try {
                Document doc = Jsoup.parse(s);
                Elements booksE = doc.getElementById("novel-list").getElementsByClass("list-group-item clearfix");
                if (null != booksE && booksE.size() >= 2) {
                    List<SearchBookBean> books = new ArrayList<>();
                    for (int i = 1; i < booksE.size(); i++) {
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(TAG);
                        item.setAuthor(booksE.get(i).getElementsByClass("col-xs-2").get(0).text());
                        item.setKind(booksE.get(i).getElementsByClass("col-xs-1").get(0).text());
                        item.setLastChapter(booksE.get(i).getElementsByClass("col-xs-4").get(0).getElementsByTag("a").get(0).text());
                        item.setOrigin(name);
                        item.setName(booksE.get(i).getElementsByClass("col-xs-3").get(0).getElementsByTag("a").get(0).text());
                        item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("col-xs-3").get(0).getElementsByTag("a").get(0).attr("href"));
                        item.setCoverUrl("noimage");
                        books.add(item);
                    }
                    e.onNext(books);
                } else {
                    e.onNext(new ArrayList<>());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(new ArrayList<>());
            }
            e.onComplete();
        });
    }

    /**
     * 获取书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        return getRetrofitString(MApplication.getInstance(), TAG)
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getNoteUrl().replace(TAG, ""))
                .flatMap(s -> analyzeBookInfo(s, bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeBookInfo(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(TAG);
            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            if (bookInfoBean == null) {
                bookInfoBean = new BookInfoBean();
            }
            bookInfoBean.setNoteUrl(bookInfoBean.getNoteUrl());   //id
            bookInfoBean.setTag(TAG);
            Document doc = Jsoup.parse(s);
            Element resultE = doc.getElementsByClass("panel panel-warning").get(0);
            bookInfoBean.setCoverUrl(resultE.getElementsByClass("panel-body").get(0).getElementsByClass("img-thumbnail").get(0).attr("src"));
            bookInfoBean.setName(resultE.getElementsByClass("active").get(0).text());
            String author = resultE.getElementsByClass("col-xs-12 list-group-item no-border").get(0).getElementsByTag("small").get(0).text();
            bookInfoBean.setAuthor(FormatWebText.getAuthor(author));
            Element introduceE = resultE.getElementsByClass("panel panel-default mt20").get(0);
            String introduce;
            if (introduceE.getElementById("all") != null) {
                introduce = introduceE.getElementById("all").text().replace("[收起]", "");
            } else {
                introduce = introduceE.getElementById("shot").text();
            }
            bookInfoBean.setIntroduce("\u3000\u3000" + introduce);
            bookInfoBean.setChapterUrl(TAG + resultE.getElementsByClass("list-group-item tac").get(0).getElementsByTag("a").get(0).attr("href"));
            bookInfoBean.setOrigin(name);
            bookShelfBean.setBookInfoBean(bookInfoBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /**
     * 获取目录
     */
    @Override
    public Observable<BookShelfBean> getChapterList(final BookShelfBean bookShelfBean) {
        return getRetrofitString(MApplication.getInstance(), TAG)
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getBookInfoBean().getChapterUrl().replace(TAG, ""))
                .flatMap(s -> analyzeChapterList(s, bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(TAG);
            int chapterSize = bookShelfBean.getChapterListSize();
            WebChapterBean<List<ChapterListBean>> temp = analyzeChapterList(s, bookShelfBean.getNoteUrl());
            bookShelfBean.getBookInfoBean().setChapterList(temp.getData());
            if (chapterSize < bookShelfBean.getChapterListSize()) {
                bookShelfBean.setHasUpdate(true);
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String novelUrl) {
        Document doc = Jsoup.parse(s);
        Elements chapterS = doc.getElementById("chapters-list").getElementsByTag("a");
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        for (int i = 0; i < chapterS.size(); i++) {
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(TAG + chapterS.get(i).attr("href"));   //id
            temp.setDurChapterIndex(i);
            temp.setDurChapterName(chapterS.get(i).text());
            temp.setNoteUrl(novelUrl);
            temp.setTag(TAG);

            chapterBeans.add(temp);
        }
        return new WebChapterBean<>(chapterBeans, false);
    }

    /**
     * 获取正文
     */
    @Override
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex) {
        return getRetrofitString(MApplication.getInstance(), TAG)
                .create(IHttpGetApi.class)
                .getWebContent(durChapterUrl.replace(TAG, ""))
                .flatMap(s -> analyzeBookContent(s, durChapterUrl, durChapterIndex));
    }

    private Observable<BookContentBean> analyzeBookContent(final String s, final String durChapterUrl, final int durChapterIndex) {
        return Observable.create(e -> {
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterIndex(durChapterIndex);
            bookContentBean.setDurChapterUrl(durChapterUrl);
            bookContentBean.setTag(TAG);
            try {
                Document doc = Jsoup.parse(s);
                List<TextNode> contentEs = doc.getElementById("txtContent").textNodes();
                StringBuilder content = new StringBuilder();
                for (int i = 0; i < contentEs.size(); i++) {
                    String temp = contentEs.get(i).text().trim();
                    for (ReplaceRuleBean replaceRule : ReplaceRuleManage.getEnabled()) {
                        try {
                            temp = temp.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    temp = FormatWebText.getContent(temp);
                    if (temp.length() > 0) {
                        if (content.length() > 0) {
                            content.append("\r\n");
                        }
                        content.append("\u3000\u3000").append(temp);
                    }
                }
                bookContentBean.setDurChapterContent(content.toString());
                bookContentBean.setRight(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                ErrorAnalyContentManager.getInstance().writeNewErrorUrl(durChapterUrl);
                bookContentBean.setDurChapterContent(durChapterUrl.substring(0, durChapterUrl.indexOf('/', 8)) + MApplication.getInstance().getString(R.string.analyze_error));
                bookContentBean.setRight(false);
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取分类书籍
     */
    @Override
    public Observable<List<SearchBookBean>> getKindBook(String url, int page) {
        url = url + page + ".htm";
        return getRetrofitString(MApplication.getInstance(), GxwztvBookModelImpl.TAG)
                .create(IHttpGetApi.class)
                .getWebContent(url.replace(GxwztvBookModelImpl.TAG, ""))
                .flatMap(this::analyzeSearchBook);
    }
}
