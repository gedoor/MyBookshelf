package com.monke.monkeybook.model.source;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.content.DefaultModelImpl;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class My716 extends BaseModelImpl implements IStationBookModel {
    public static final String TAG = "My716";

    public static My716 getInstance() {
        return new My716();
    }

    /**
     * 发现书籍
     */
    @Override
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        return null;
    }

    /**
     * 搜索书籍
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("query", content);
        return getRetrofitString("http://api.zhuishushenqi.com")
                .create(IHttpGetApi.class)
                .searchBook("http://api.zhuishushenqi.com/book/fuzzy-search",
                        queryMap,
                        AnalyzeHeaders.getMap(null))
                .flatMap(this::analyzeSearchBook);
    }

    private Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            List<SearchBookBean> searchBookList = new ArrayList<>();
            JsonObject root = new JsonParser().parse(Objects.requireNonNull(response.body())).getAsJsonObject();
            if (root.get("ok").getAsBoolean()) {
                JsonArray bookArray = root.get("books").getAsJsonArray();
                for (int i = 0; i < bookArray.size(); i++) {
                    JsonObject book = bookArray.get(i).getAsJsonObject();

                    SearchBookBean searchBookBean = new SearchBookBean();
                    searchBookBean.setTag(TAG);
                    searchBookBean.setWeight(Integer.MAX_VALUE);
                    searchBookBean.setOrigin(TAG);
                    searchBookBean.setKind(book.get("cat").getAsString());
                    searchBookBean.setName(book.get("title").getAsString());
                    searchBookBean.setAuthor(book.get("author").getAsString());
                    searchBookBean.setNoteUrl("http://api.zhuishushenqi.com/atoc?view=summary&book=" + book.get("_id").getAsString());
                    searchBookBean.setLastChapter(book.get("lastChapter").getAsString().replaceAll("^\\s*正文[卷：\\s]+", ""));
                    searchBookBean.setCoverUrl("http://statics.zhuishushenqi.com" + book.get("cover").getAsString());
                    searchBookBean.setIntroduce(book.get("shortIntro").getAsString());

                    Call<String> call = DefaultModelImpl.getRetrofitString("http://api.zhuishushenqi.com")
                            .create(IHttpGetApi.class).getWebContentCall(searchBookBean.getNoteUrl(), AnalyzeHeaders.getMap(null));
                    String s = "";
                    try {
                        s = call.execute().body();
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                    if (!TextUtils.isEmpty(s)) {
                        JsonArray sourceArray = new JsonParser().parse(s).getAsJsonArray();
                        for (int j = 0; j < sourceArray.size(); j++) {
                            JsonObject source = sourceArray.get(j).getAsJsonObject();
                            String name = source.get("source").getAsString();
                            if (name.equals("my176")) {
                                searchBookBean.setLastChapter(source.get("lastChapter").getAsString());
                                searchBookBean.setChapterUrl("http://api.zhuishushenqi.com/atoc/" + source.get("_id").getAsString() + "?view=chapters");
                                searchBookList.add(searchBookBean);
                                break;
                            }
                        }
                    }
                }
            }
            e.onNext(searchBookList);
            e.onComplete();
        });
    }

    /**
     * 网络请求并解析书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        return getRetrofitString("http://api.zhuishushenqi.com")
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getNoteUrl(), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeBookInfo(response.body(), bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("书籍信息获取失败"));
                e.onComplete();
                return;
            }

            JsonArray sourceArray = new JsonParser().parse(s).getAsJsonArray();
            for (int i = 0; i < sourceArray.size(); i++) {
                JsonObject source = sourceArray.get(i).getAsJsonObject();
                String name = source.get("source").getAsString();
                if (name.equals("my176")) {
                    bookShelfBean.setLastChapterName(source.get("lastChapter").getAsString());
                    bookShelfBean.getBookInfoBean().setChapterUrl("http://api.zhuishushenqi.com/atoc/" + source.get("_id").getAsString() + "?view=chapters");

                    e.onNext(bookShelfBean);
                    e.onComplete();
                    return;
                }
            }
            e.onError(new Throwable("书籍信息获取失败"));
            e.onComplete();
        });
    }

    /**
     * 网络解析图书目录
     */
    @Override
    public Observable<List<ChapterListBean>> getChapterList(BookShelfBean bookShelfBean) {
        return getRetrofitString("http://api.zhuishushenqi.com")
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getBookInfoBean().getChapterUrl(), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeChapterList(response.body(), bookShelfBean));
    }

    private Observable<List<ChapterListBean>> analyzeChapterList(String s, BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            List<ChapterListBean> chapterList = new ArrayList<>();
            JsonObject root = new JsonParser().parse(s).getAsJsonObject();
            JsonArray chapterArray = root.get("chapters").getAsJsonArray();
            for (int i = 0; i < chapterArray.size(); i++) {
                JsonObject chapter = chapterArray.get(i).getAsJsonObject();

                ChapterListBean chapterListBean = new ChapterListBean();
                chapterListBean.setDurChapterIndex(i);
                chapterListBean.setDurChapterName(chapter.get("title").getAsString());
                chapterListBean.setDurChapterUrl("http://chapterup.zhuishushenqi.com/chapter/" + URLEncoder.encode(chapter.get("link").getAsString(), "UTF-8"));

                chapterList.add(chapterListBean);
            }
            e.onNext(chapterList);
            e.onComplete();
        });
    }

    /**
     * 章节缓存
     */
    @Override
    public Observable<BookContentBean> getBookContent(final Scheduler scheduler, final String durChapterUrl, int durChapterIndex) {
        return getRetrofitString("http://chapterup.zhuishushenqi.com")
                .create(IHttpGetApi.class)
                .getWebContent(durChapterUrl, AnalyzeHeaders.getMap(null))
                .subscribeOn(Schedulers.newThread())
                .flatMap(response -> analyzeBookContent(response.body(), durChapterUrl, durChapterIndex));
    }

    private Observable<BookContentBean> analyzeBookContent(String s, String durChapterUrl, int durChapterIndex) {
        return Observable.create(e -> {
            BookContentBean bookContentBean = new BookContentBean();
            JsonObject root = new JsonParser().parse(s).getAsJsonObject();
            if (root.get("ok").getAsBoolean()) {
                JsonObject chapter = root.get("chapter").getAsJsonObject();

                bookContentBean.setRight(true);
                bookContentBean.setDurChapterUrl(durChapterUrl);
                bookContentBean.setDurChapterIndex(durChapterIndex);
                bookContentBean.setDurChapterContent(chapter.get("body").getAsString());
            } else {
                bookContentBean.setRight(false);
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

}
