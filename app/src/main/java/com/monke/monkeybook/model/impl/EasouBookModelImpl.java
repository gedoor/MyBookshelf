package com.monke.monkeybook.model.impl;

import com.monke.basemvplib.impl.BaseModelImpl;
import com.monke.monkeybook.ErrorAnalyContentManager;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.WebChapterBean;
import com.monke.monkeybook.common.api.IEasouApi;
import com.monke.monkeybook.listener.OnGetChapterListListener;
import com.monke.monkeybook.model.IEasouBookModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EasouBookModelImpl extends BaseModelImpl implements IEasouBookModel {
    public static final String TAG = "http://book.easou.com";

    public static EasouBookModelImpl getInstance() {
        return new EasouBookModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 搜索书籍
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page, int rankKind) {
        String temp = "/w/searchNovel/" + content + "_" + rankKind + "_" + page + ".html";
        return getRetrofitObject(TAG).create(IEasouApi.class).searchBook(temp).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
            @Override
            public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                return analySearchBook(s);
            }
        });
    }

    public Observable<List<SearchBookBean>> analySearchBook(final String s) {
        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                try {
                    Document doc = Jsoup.parse(s);
                    Elements booksE = doc.getElementsByClass("resultContent").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (Element bookItem : booksE) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(bookItem.getElementsByClass("attr").get(0).getElementsByTag("a").get(0).text());
                            item.setKind(bookItem.getElementsByClass("attr").get(0).getElementsByTag("a").get(1).text());
                            item.setState(bookItem.getElementsByTag("span").get(0).text());
                            item.setLastChapter(bookItem.getElementsByClass("lastchapter").get(0).text());
                            item.setOrigin(bookItem.getElementsByClass("source").get(0).text().replaceAll("来源:", ""));
                            item.setName(bookItem.getElementsByClass("name").get(0).getElementsByClass("common").get(0).text());
                            item.setNoteUrl(TAG + bookItem.getElementsByClass("name").get(0).getElementsByClass("common").get(0).attr("href"));
                            item.setCoverUrl(bookItem.getElementsByTag("img").get(0).attr("src"));
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
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络请求并解析书籍信息
     * return BookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        return getRetrofitObject(TAG).create(IEasouApi.class).getBookInfo(bookShelfBean.getNoteUrl().replace(TAG, "")).flatMap(new Function<String, ObservableSource<BookShelfBean>>() {
            @Override
            public ObservableSource<BookShelfBean> apply(String s) throws Exception {
                return analyBookInfo(s, bookShelfBean);
            }
        });
    }

    private Observable<BookShelfBean> analyBookInfo(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(new ObservableOnSubscribe<BookShelfBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookShelfBean> e) throws Exception {
                bookShelfBean.setTag(TAG);
                bookShelfBean.setBookInfoBean(analyBookinfo(s, bookShelfBean.getNoteUrl()));
                e.onNext(bookShelfBean);
                e.onComplete();
            }
        });
    }

    private BookInfoBean analyBookinfo(String s, String novelUrl) {
        BookInfoBean bookInfoBean = new BookInfoBean();
        bookInfoBean.setNoteUrl(novelUrl);   //id
        bookInfoBean.setTag(TAG);
        Document doc = Jsoup.parse(s);
        Element resultE = doc.getElementsByClass("content").get(0);
        String coverUrl = resultE.getElementsByClass("imgShow").get(0).getElementsByTag("img").get(0).attr("src");
        bookInfoBean.setCoverUrl(coverUrl.startsWith("http") ? coverUrl : (TAG + coverUrl));
        bookInfoBean.setName(resultE.getElementsByClass("tit").get(1).getElementsByTag("h1").get(0).text());
        bookInfoBean.setAuthor(resultE.getElementsByClass("author").get(0).getElementsByClass("common").get(0).text());
        bookInfoBean.setIntroduce("\u3000\u3000" + resultE.getElementsByClass("desc").get(0).text().trim());
        bookInfoBean.setChapterUrl(TAG + resultE.getElementsByClass("category").get(0).getElementsByTag("a").get(0).attr("href"));
        bookInfoBean.setOrigin(resultE.getElementsByClass("attribute").get(0).getElementsByClass("source").get(0).getElementsByClass("t").get(0).text());
        return bookInfoBean;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络解析图书目录
     * return BookShelfBean
     */
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener) {
        getChapterListPage(bookShelfBean, getChapterListListener, 1);
    }

    private void getChapterListPage(final BookShelfBean bookShelfBean, final OnGetChapterListListener getChapterListListener, final int page) {
        getRetrofitObject(TAG).create(IEasouApi.class).getChapterList(bookShelfBean.getBookInfoBean().getChapterUrl().replace(TAG, "").replace("1_", page + "_")).flatMap(new Function<String, ObservableSource<WebChapterBean<BookShelfBean>>>() {
            @Override
            public ObservableSource<WebChapterBean<BookShelfBean>> apply(String s) throws Exception {
                return analyChapterList(s, bookShelfBean, page == 1 ? true : false);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<WebChapterBean<BookShelfBean>>() {
                    @Override
                    public void onNext(WebChapterBean<BookShelfBean> value) {
                        if (value.getNext()) {
                            getChapterListPage(value.getData(), getChapterListListener, page + 1);
                        } else {
                            if (value.getData().getBookInfoBean().getChapterlist() != null && value.getData().getBookInfoBean().getChapterlist().size() > 0) {
                                for (int i = 0; i < value.getData().getBookInfoBean().getChapterlist().size(); i++) {
                                    int temp = i;
                                    for (int j = i; j < value.getData().getBookInfoBean().getChapterlist().size(); j++) {
                                        if (value.getData().getBookInfoBean().getChapterlist().get(temp).getDurChapterIndex() > value.getData().getBookInfoBean().getChapterlist().get(j).getDurChapterIndex()) {
                                            temp = j;
                                        }
                                    }
                                    ChapterListBean tempBean = value.getData().getBookInfoBean().getChapterlist().get(i);
                                    value.getData().getBookInfoBean().getChapterlist().set(i, value.getData().getBookInfoBean().getChapterlist().get(temp));
                                    value.getData().getBookInfoBean().getChapterlist().set(temp, tempBean);
                                }
                                for (int i = 0; i < value.getData().getBookInfoBean().getChapterlist().size(); i++) {
                                    value.getData().getBookInfoBean().getChapterlist().get(i).setDurChapterIndex(i);
                                }
                            }
                            if (getChapterListListener != null) {
                                getChapterListListener.success(value.getData());
                            }
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

    private Observable<WebChapterBean<BookShelfBean>> analyChapterList(final String s, final BookShelfBean bookShelfBean, final Boolean isFirstPage) {
        return Observable.create(new ObservableOnSubscribe<WebChapterBean<BookShelfBean>>() {
            @Override
            public void subscribe(ObservableEmitter<WebChapterBean<BookShelfBean>> e) throws Exception {
                bookShelfBean.setTag(TAG);
                WebChapterBean<List<ChapterListBean>> temp = analyChapterlist(s, bookShelfBean.getNoteUrl(), bookShelfBean.getTag());
                if (isFirstPage) {
                    bookShelfBean.getBookInfoBean().setChapterlist(temp.getData());
                } else {
                    bookShelfBean.getBookInfoBean().addChapterlist(temp.getData());
                }
                e.onNext(new WebChapterBean<BookShelfBean>(bookShelfBean, temp.getNext()));
                e.onComplete();
            }
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyChapterlist(String s, String novelUrl, String chapterUrl) {
        Document doc = Jsoup.parse(s);
        Element element = doc.getElementsByClass("content").get(0);
        Elements chapterlist = element.getElementsByClass("category").get(0).getElementsByTag("li");
        List<ChapterListBean> chapterBeans = new ArrayList<ChapterListBean>();
        for (int i = 0; i < chapterlist.size(); i++) {
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(chapterUrl + chapterlist.get(i).getElementsByTag("a").get(0).attr("href"));   //id
            String name = chapterlist.get(i).getElementsByTag("a").get(0).text();
            setDurChapterData(i, name, temp);
            temp.setNoteUrl(novelUrl);
            temp.setTag(TAG);

            chapterBeans.add(temp);
        }

        Elements nextE = doc.getElementsByClass("pager").get(0).getElementsByClass("next unable");
        Boolean next = (nextE == null || nextE.size() <= 0) ? true : false;
        return new WebChapterBean<List<ChapterListBean>>(chapterBeans, next);
    }

    private void setDurChapterData(int i, String name, ChapterListBean temp) {
        try {
            String regex = "第.{1,7}章";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(name);
            if (m.find()) {
                String indexTemp = m.group();
                indexTemp = indexTemp.replace("第", "").replace("章", "").trim();
                String regex3 = "[0-9]{1,6}";
                Pattern p3 = Pattern.compile(regex3);
                Matcher m3 = p3.matcher(indexTemp);
                int resultIndex = i;
                if (m3.matches()) {
                    resultIndex = Integer.parseInt(indexTemp);
                } else {
                    resultIndex = parse(indexTemp);
                }
                temp.setDurChapterIndex(resultIndex);
            } else {
                String regex2 = "[0-9]{1,6}\\.";
                Pattern p2 = Pattern.compile(regex2);
                Matcher m2 = p2.matcher(name);
                if (m2.find()) {
                    String indexTemp = m2.group();
                    indexTemp = indexTemp.replaceAll("\\.", "").trim();
                    int resultIndex = Integer.parseInt(indexTemp);
                    temp.setDurChapterIndex(resultIndex);
                } else {
                    temp.setDurChapterIndex(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            temp.setDurChapterIndex(i);
        }finally {
            temp.setDurChapterName(name);
        }
    }

    public static int parse(String money) {
        int result = 0;
        char c = 0;

        boolean flag = Pattern.matches("^.*亿.*万.*$", money);

        for (int i = 0; i < money.length(); i++) {
            switch (money.charAt(i)) {
                case '零':
                    break;
                case '一':
                    c = 1;
                    break;
                case '二':
                    c = 2;
                    break;
                case '三':
                    c = 3;
                    break;
                case '四':
                    c = 4;
                    break;
                case '五':
                    c = 5;
                    break;
                case '六':
                    c = 6;
                    break;
                case '七':
                    c = 7;
                    break;
                case '八':
                    c = 8;
                    break;
                case '九':
                    c = 9;
                    break;
                case '十':
                    result += (c == 0 ? 10 : c * 10);
                    c = 0;
                    break;
                case '百':
                    result += c * 100;
                    c = 0;
                    break;
                case '千':
                    result += c * 1000;
                    c = 0;
                    break;
                case '万':
                    result = (result + c) * 10000;
                    c = 0;
                    break;
                case '亿':
                    if (flag) {
                        result = (result + c) * 10000;
                    } else {
                        result = (result + c) * 100000000;
                    }
                    c = 0;
                    break;
                default:
                    c = 0;
            }

        }
        if (c != 0)
            result += c;
        return result;

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 章节缓存
     */
    @Override
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex) {
        return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
            @Override
            public void subscribe(final ObservableEmitter<BookContentBean> e) throws Exception {
                OkHttpClient client = clientBuilder.build();
                Request.Builder requestBuilder = new Request.Builder().url(durChapterUrl).method("GET", null);
                requestBuilder.addHeader("Accept", "text/html,application/xhtml+xml,application/xml");
                requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
                requestBuilder.addHeader("Accept-Charset", "UTF-8");
                requestBuilder.addHeader("Keep-Alive", "300");
                requestBuilder.addHeader("Cache-Control", "no-cache");
                requestBuilder.addHeader("Connection", "Keep-Alive");
                Call mcall = client.newCall(requestBuilder.build());
                mcall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException ex) {
                        ex.printStackTrace();
                        if (!e.isDisposed()) {
                            e.onError(ex);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String url = response.request().url().toString();
                        BookContentBean bookContentBean = new BookContentBean();
                        try {
                            bookContentBean = choiceAnaly(response, url, durChapterUrl, durChapterIndex);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ErrorAnalyContentManager.getInstance().writeMayByNetError(url);
                            bookContentBean = new BookContentBean();
                            bookContentBean.setDurChapterIndex(durChapterIndex);
                            bookContentBean.setDurChapterUrl(durChapterUrl);
                            bookContentBean.setDurCapterContent(url.substring(0, url.indexOf('/', 8)) + "站点服务器异常");
                            bookContentBean.setTag(TAG);
                            bookContentBean.setRight(false);
                        }
                        if (!e.isDisposed()) {
                            e.onNext(bookContentBean);
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }

    private BookContentBean choiceAnaly(Response response, String url, String durChapterUrl, int durChapterIndex) throws Exception {
        BookContentBean bookContentBean = new BookContentBean();
        bookContentBean.setDurChapterIndex(durChapterIndex);
        bookContentBean.setDurChapterUrl(durChapterUrl);
        bookContentBean.setTag(TAG);
        if (url.contains(ContentEasouModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentEasouModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentShulouModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentShulouModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentPbtxtModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentPbtxtModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentXqingdouModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentXqingdouModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentSnwx8ModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentSnwx8ModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(Content17kModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(Content17kModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(Content92zwModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(Content92zwModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentSuimengModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentSuimengModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentKewaishuModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentKewaishuModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentWxguanModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentWxguanModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentBaishukuModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentBaishukuModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentAszwModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentAszwModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentXqingdouCCModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentXqingdouCCModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentFuheishuModelImpl.TAG)) {
            String xml = response.body().string();
            //站点有问题  未完全解析
            bookContentBean.setDurCapterContent(ContentFuheishuModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentSyzwwModelImpl.TAG)) {
            String xml = response.body().string();
            //站点有问题  未完全解析
            bookContentBean.setDurCapterContent(ContentSyzwwModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(Content630bookCCModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(Content630bookCCModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentBxwx9ModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentBxwx9ModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(Content44pqModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(Content44pqModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentQzreadModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentQzreadModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentLeduwoModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentLeduwoModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(Content17duxsModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(Content17duxsModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentDhzwModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentDhzwModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentYb3ModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentYb3ModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentZhulangModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentZhulangModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentRanwenaModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentRanwenaModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentPpxsModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GBK"));
            bookContentBean.setDurCapterContent(ContentPpxsModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentVodtwModelImpl.TAG)) {
            String xml = response.body().source().readString(Charset.forName("GB2312"));
            bookContentBean.setDurCapterContent(ContentVodtwModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentQulaModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentQulaModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(ContentLewen8ModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(ContentLewen8ModelImpl.getInstance().analyBookcontent(xml, url));
        } else if (url.contains(Content3dllcModelImpl.TAG)) {
            String xml = response.body().string();
            bookContentBean.setDurCapterContent(Content3dllcModelImpl.getInstance().analyBookcontent(xml, url));
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        else {
            byte[] xmlData = response.body().source().readByteArray();
            String xml = new String(xmlData, "UTF-8");
            String charset = "UTF-8";
            try {
                Document doc = Jsoup.parse(xml);
                String data = doc.getElementsByTag("head").get(0).getElementsByTag("meta").get(0).attr("content").toUpperCase().trim();
                if (data.contains("CHARSET")) {
                    data = data.substring(data.indexOf("CHARSET=") + 8);
                    if (data.contains(";")) {
                        data = data.substring(0, data.indexOf(";"));
                    }
                    charset = data;
                    if (!charset.equals("UTF-8")) {
                        xml = new String(xmlData, charset);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            bookContentBean = ContentCommendModelImpl.getInstance().analyBookcontent(bookContentBean, xml, url);
        }
        return bookContentBean;
    }

    /*
    测试各个网站目录解析
     */
    public static void main(String[] args) {
        EasouBookModelImpl.getInstance().getBookContent("http://www.yb3.cc/5200/3186/7426551.html", 0)
                .subscribe(new SimpleObserver<BookContentBean>() {
                    @Override
                    public void onNext(BookContentBean value) {
                        value.getRight();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
//        EasouBookModelImpl.getInstance().setDurChapterData(1,"第42章学习",new ChapterListBean());
//        EasouBookModelImpl.getInstance().getLibraryData(null)
//                .subscribe(new SimpleObserver<LibraryBean>() {
//                    @Override
//                    public void onNext(LibraryBean value) {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//                });
    }
}