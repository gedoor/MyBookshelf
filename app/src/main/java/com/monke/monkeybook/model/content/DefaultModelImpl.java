package com.monke.monkeybook.model.content;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.WebChapterBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.ErrorAnalyContentManager;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.model.impl.IHttpPostApi;
import com.monke.monkeybook.model.impl.IStationBookModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

/**
 * 默认检索规则
 */
public class DefaultModelImpl extends BaseModelImpl implements IStationBookModel {
    private String TAG;
    private String name;
    private BookSourceBean bookSourceBean;

    private DefaultModelImpl(String tag) {
        TAG = tag;
        try {
            URL url = new URL(tag);
            name = url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            name = tag;
        }
    }

    public static DefaultModelImpl getInstance(String tag) {
        return new DefaultModelImpl(tag);
    }

    private Boolean initBookSourceBean() {
        if (bookSourceBean == null) {
            List<BookSourceBean> bookSourceBeans = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(TAG)).build().list();
            if (bookSourceBeans != null && bookSourceBeans.size() > 0) {
                bookSourceBean = bookSourceBeans.get(0);
                name = bookSourceBean.getBookSourceName();
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        if (!initBookSourceBean()) {
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
        Boolean isPost = bookSourceBean.getRuleSearchUrl().contains("@");
        try {
            AnalyzeSearchUrl analyzeSearchUrl = new AnalyzeSearchUrl(bookSourceBean.getRuleSearchUrl(), content, page);
            if (analyzeSearchUrl.getSearchUrl() == null) {
                return Observable.create(emitter -> {
                    emitter.onNext(new ArrayList<>());
                    emitter.onComplete();
                });
            }
            if (isPost) {
                return getRetrofitString(MApplication.getInstance(), analyzeSearchUrl.getSearchUrl())
                        .create(IHttpPostApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(), analyzeSearchUrl.getQueryMap())
                        .flatMap(this::analyzeSearchBook);
            } else {
                return getRetrofitString(MApplication.getInstance(), analyzeSearchUrl.getSearchUrl())
                        .create(IHttpGetApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(), analyzeSearchUrl.getQueryMap())
                        .flatMap(this::analyzeSearchBook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
    }

    private Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            try {
                String baseURI;
                okhttp3.Response networkResponse = response.raw().networkResponse();
                if (networkResponse != null && networkResponse.request() != null) {
                    baseURI = networkResponse.request().url().toString();
                } else {
                    baseURI = response.raw().request().url().toString();
                }
                Document doc = Jsoup.parse(response.body());
                Elements booksE = AnalyzeRule.getElements(doc, bookSourceBean.getRuleSearchList());
                if (null != booksE && booksE.size() > 0) {
                    List<SearchBookBean> books = new ArrayList<>();
                    for (int i = 0; i < booksE.size(); i++) {
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(TAG);
                        item.setOrigin(name);
                        AnalyzeRule analyzeRule = new AnalyzeRule(booksE.get(i), baseURI);
                        item.setAuthor(FormatWebText.getAuthor(analyzeRule.getResult(bookSourceBean.getRuleSearchAuthor())));
                        item.setKind(analyzeRule.getResult(bookSourceBean.getRuleSearchKind()));
                        item.setLastChapter(analyzeRule.getResult(bookSourceBean.getRuleSearchLastChapter()));
                        item.setName(analyzeRule.getResult(bookSourceBean.getRuleSearchName()));
                        item.setNoteUrl(analyzeRule.getResult(bookSourceBean.getRuleSearchNoteUrl()));
                        if (isEmpty(item.getNoteUrl())) {
                            item.setNoteUrl(baseURI);
                        }
                        item.setCoverUrl(analyzeRule.getResult(bookSourceBean.getRuleSearchCoverUrl()));
                        if (!isEmpty(item.getName())) {
                            books.add(item);
                        }
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
        if (!initBookSourceBean()) {
            return Observable.error(new Throwable(String.format("无法找到源%s", TAG)));
        }
        return getRetrofitString(MApplication.getInstance(), TAG)
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getNoteUrl().replace(TAG, ""))
                .flatMap(s -> analyzeBookInfo(s, bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(TAG);
            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            if (bookInfoBean == null) {
                bookInfoBean = new BookInfoBean();
            }
            bookInfoBean.setNoteUrl(bookShelfBean.getNoteUrl());   //id
            bookInfoBean.setTag(TAG);
            Document doc = Jsoup.parse(s);
            AnalyzeRule analyzeRule = new AnalyzeRule(doc, bookShelfBean.getNoteUrl());
            if (isEmpty(bookInfoBean.getCoverUrl())) {
                bookInfoBean.setCoverUrl(analyzeRule.getResult(bookSourceBean.getRuleCoverUrl()));
            }
            if (isEmpty(bookInfoBean.getName())) {
                bookInfoBean.setName(analyzeRule.getResult(bookSourceBean.getRuleBookName()));
            }
            if (isEmpty(bookInfoBean.getAuthor())) {
                bookInfoBean.setAuthor(analyzeRule.getResult(bookSourceBean.getRuleBookAuthor()));
            }
            bookInfoBean.setIntroduce(analyzeRule.getResult(bookSourceBean.getRuleIntroduce()));
            String chapterUrl = analyzeRule.getResult(bookSourceBean.getRuleChapterUrl());
            if (isEmpty(chapterUrl)) {
                bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());
            } else {
                bookInfoBean.setChapterUrl(chapterUrl);
            }
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
        if (!initBookSourceBean()) {
            return Observable.create(emitter -> {
                bookShelfBean.setErrorMsg(String.format("%s没有找到书源配置", bookShelfBean.getBookInfoBean().getName()));
                emitter.onNext(bookShelfBean);
                emitter.onComplete();
            });
        }
        return getRetrofitString(MApplication.getInstance(), TAG)
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getBookInfoBean().getChapterUrl().replace(TAG, ""))
                .flatMap(s -> analyzeChapterList(s, bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setTag(TAG);
            int chapterSize = bookShelfBean.getChapterListSize();
            WebChapterBean<List<ChapterListBean>> temp = analyzeChapterList(s, bookShelfBean.getNoteUrl(), bookShelfBean.getBookInfoBean().getChapterUrl());
            bookShelfBean.getBookInfoBean().setChapterList(temp.getData());
            if (chapterSize < bookShelfBean.getChapterListSize()) {
                bookShelfBean.setHasUpdate(true);
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String novelUrl, String chapterUrl) {
        Document doc = Jsoup.parse(s);
        Elements chapterList = AnalyzeRule.getElements(doc, bookSourceBean.getRuleChapterList());
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        for (int i = 0; i < chapterList.size(); i++) {
            AnalyzeRule analyzeRule = new AnalyzeRule(chapterList.get(i), chapterUrl);
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterIndex(i);
            temp.setDurChapterUrl(analyzeRule.getResult(bookSourceBean.getRuleContentUrl()));   //id
            temp.setDurChapterName(analyzeRule.getResult(bookSourceBean.getRuleChapterName()));
            temp.setNoteUrl(novelUrl);
            temp.setTag(TAG);
            if (!isEmpty(temp.getDurChapterUrl()) && !isEmpty(temp.getDurChapterName())) {
                chapterBeans.add(temp);
            }
        }
        return new WebChapterBean<>(chapterBeans, false);
    }

    /**
     * 获取正文
     */
    @Override
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex) {
        if (!initBookSourceBean()) {
            return Observable.create(emitter -> {
                emitter.onNext(new BookContentBean());
                emitter.onComplete();
            });
        }
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
                AnalyzeRule analyzeRule = new AnalyzeRule(doc, durChapterUrl);
                bookContentBean.setDurChapterContent(analyzeRule.getResult(bookSourceBean.getRuleBookContent()));
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
}
