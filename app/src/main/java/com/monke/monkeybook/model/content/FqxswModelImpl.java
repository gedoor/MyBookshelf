//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.content;

import com.monke.basemvplib.impl.BaseModelImpl;
import com.monke.monkeybook.ErrorAnalyContentManager;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.WebChapterBean;
import com.monke.monkeybook.common.api.ILingdiankanshuApi;
import com.monke.monkeybook.listener.OnGetChapterListListener;
import com.monke.monkeybook.model.IStationBookModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FqxswModelImpl extends BaseModelImpl implements IStationBookModel {
    public static final String TAG = "http://www.fqxsw.com/";
    public static final String Name = "番茄小说";

    public static FqxswModelImpl getInstance() {
        return new FqxswModelImpl();
    }

    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject("http://zhannei.baidu.com").
                create(ILingdiankanshuApi.class)
                .searchBook(content, page - 1, "1681641916749657229")
                .flatMap(s -> analySearchBook(s));
    }

    public Observable<List<SearchBookBean>> analySearchBook(final String s) {
        return Observable.create(e -> {
            try {
                Document doc = Jsoup.parse(s);
                Elements booksE = doc.getElementsByClass("result-list").get(0).getElementsByClass("result-item result-game-item");
                if (null != booksE && booksE.size() > 1) {
                    List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                    for (int i = 0; i < booksE.size(); i++) {
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(TAG);
                        item.setAuthor(booksE.get(i).getElementsByClass("result-game-item-info").get(0)
                                .getElementsByClass("result-game-item-info-tag").get(0)
                                .getElementsByTag("span").get(1).text());
                        item.setKind(booksE.get(i).getElementsByClass("result-game-item-info").get(0)
                                .getElementsByClass("result-game-item-info-tag").get(1)
                                .getElementsByTag("span").get(1).text());
                        item.setLastChapter(booksE.get(i).getElementsByClass("result-game-item-info").get(0)
                                .getElementsByClass("result-game-item-info-tag").get(3)
                                .getElementsByTag("a").get(0).text());
                        item.setOrigin("fqxsw.com");
                        item.setName(booksE.get(i).getElementsByClass("result-item-title result-game-item-title").get(0)
                                .getElementsByTag("a").get(0).text());
                        item.setNoteUrl(booksE.get(i).getElementsByClass("result-item-title result-game-item-title").get(0)
                                .getElementsByTag("a").get(0).attr("href"));
                        item.setCoverUrl(booksE.get(i).getElementsByTag("img").get(0).attr("src"));
                        books.add(item);
                    }
                    e.onNext(books);
                } else {
                    e.onNext(new ArrayList<SearchBookBean>());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(new ArrayList<SearchBookBean>());
            }
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getBookInfo(bookShelfBean.getNoteUrl().replace(TAG, "")).flatMap(s -> analyBookInfo(s, bookShelfBean));
    }

    private Observable<BookShelfBean> analyBookInfo(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(TAG);
            bookShelfBean.setBookInfoBean(analyBookinfo(s, bookShelfBean.getNoteUrl()));
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private BookInfoBean analyBookinfo(String s, String novelUrl) {
        BookInfoBean bookInfoBean = new BookInfoBean();
        bookInfoBean.setNoteUrl(novelUrl);   //id
        bookInfoBean.setTag(TAG);
        Document doc = Jsoup.parse(s);
        Element resultE = doc.getElementsByClass("box_con").get(0);
        bookInfoBean.setCoverUrl(resultE.getElementById("fmimg").getElementsByTag("img").get(0).attr("src"));
        bookInfoBean.setName(resultE.getElementById("info").getElementsByTag("h1").get(0).text());
        String author = resultE.getElementById("info").getElementsByTag("p").get(0).text().toString().trim();
        author = author.replace(" ", "").replace("  ", "").replace("作者：", "");
        bookInfoBean.setAuthor(author);

        List<TextNode> contentEs = resultE.getElementById("intro").textNodes();
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < contentEs.size(); i++) {
            String temp = contentEs.get(i).text().trim();
            temp = temp.replaceAll(" ", "").replaceAll(" ", "")
                    .replaceAll("\r","").replaceAll("\n", "").replaceAll("\t", "");
            if (temp.length() > 0) {
                if (i != 0) {
                    content.append("\r\n");
                }
                content.append("\u3000\u3000" + temp);
            }
        }

        bookInfoBean.setIntroduce(content.toString());
        bookInfoBean.setChapterUrl(novelUrl);
        bookInfoBean.setOrigin("fqxsw.com");
        return bookInfoBean;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, final OnGetChapterListListener getChapterListListener) {
        getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getChapterList(bookShelfBean.getBookInfoBean().getChapterUrl().replace(TAG, "")).flatMap(s -> analyChapterList(s, bookShelfBean))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<WebChapterBean<BookShelfBean>>() {
                    @Override
                    public void onNext(WebChapterBean<BookShelfBean> value) {
                        if (getChapterListListener != null) {
                            getChapterListListener.success(value.getData());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (getChapterListListener != null) {
                            getChapterListListener.error();
                        }
                    }
                });
    }

    private Observable<WebChapterBean<BookShelfBean>> analyChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(TAG);
            WebChapterBean<List<ChapterListBean>> temp = analyChapterlist(s, bookShelfBean.getNoteUrl());
            bookShelfBean.getBookInfoBean().setChapterlist(temp.getData());
            e.onNext(new WebChapterBean<BookShelfBean>(bookShelfBean, temp.getNext()));
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyChapterlist(String s, String novelUrl) {
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementById("list").getElementsByTag("dd");
        List<ChapterListBean> chapterBeans = new ArrayList<ChapterListBean>();
        for (int i = 0; i < chapterlist.size(); i++) {
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(TAG + chapterlist.get(i).getElementsByTag("a").get(0).attr("href"));   //id
            temp.setDurChapterIndex(i);
            temp.setDurChapterName(chapterlist.get(i).getElementsByTag("a").get(0).text());
            temp.setNoteUrl(novelUrl);
            temp.setTag(TAG);

            chapterBeans.add(temp);
        }
        Boolean next = false;
        return new WebChapterBean<List<ChapterListBean>>(chapterBeans, next);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex) {
        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getBookContent(durChapterUrl.replace(TAG, "")).flatMap(s -> analyBookContent(s, durChapterUrl, durChapterIndex));
    }

    private Observable<BookContentBean> analyBookContent(final String s, final String durChapterUrl, final int durChapterIndex) {
        return Observable.create(e -> {
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterIndex(durChapterIndex);
            bookContentBean.setDurChapterUrl(durChapterUrl);
            bookContentBean.setTag(TAG);
            try {
                Document doc = Jsoup.parse(s);
                List<TextNode> contentEs = doc.getElementById("content").textNodes();
                StringBuilder content = new StringBuilder();
                for (int i = 0; i < contentEs.size(); i++) {
                    String temp = contentEs.get(i).text().trim();
                    temp = temp.replaceAll(" ", "").replaceAll(" ", "");
                    if (temp.length() > 0) {
                        content.append("\u3000\u3000" + temp);
                        if (i < contentEs.size() - 1) {
                            content.append("\r\n");
                        }
                    }
                }
                bookContentBean.setDurCapterContent(content.toString());
                bookContentBean.setRight(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                ErrorAnalyContentManager.getInstance().writeNewErrorUrl(durChapterUrl);
                bookContentBean.setDurCapterContent(durChapterUrl.substring(0, durChapterUrl.indexOf('/', 8)) + "站点暂时不支持解析，请反馈给Monke QQ:1105075896,半小时内解决，超级效率的程序员");
                bookContentBean.setRight(false);
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
