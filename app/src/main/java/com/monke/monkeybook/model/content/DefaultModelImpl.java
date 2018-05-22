package com.monke.monkeybook.model.content;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeSearchUrl;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.model.impl.IHttpPostApi;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

/**
 * 默认检索规则
 */
public class DefaultModelImpl extends BaseModelImpl implements IStationBookModel {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private Map<String, String> headerMap = AnalyzeHeaders.getMap(null);

    private DefaultModelImpl(String tag) {
        this.tag = tag;
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
                    .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(tag)).build().list();
            if (bookSourceBeans != null && bookSourceBeans.size() > 0) {
                bookSourceBean = bookSourceBeans.get(0);
                name = bookSourceBean.getBookSourceName();
                headerMap = AnalyzeHeaders.getMap(bookSourceBean.getHttpUserAgent());
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 发现
     */
    @Override
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        if (!initBookSourceBean() || isEmpty(bookSourceBean.getRuleSearchUrl())) {
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
        BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeSearchUrl analyzeSearchUrl = new AnalyzeSearchUrl(url, "", page);
            if (analyzeSearchUrl.getSearchUrl() == null) {
                return Observable.create(emitter -> {
                    emitter.onNext(new ArrayList<>());
                    emitter.onComplete();
                });
            }
            if (url.contains("@")) {
                return getRetrofitString(analyzeSearchUrl.getSearchUrl())
                        .create(IHttpPostApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else if (url.contains("?")) {
                return getRetrofitString(analyzeSearchUrl.getSearchUrl())
                        .create(IHttpGetApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else {
                return getRetrofitString(analyzeSearchUrl.getSearchUrl())
                        .create(IHttpGetApi.class)
                        .getWebContent(analyzeSearchUrl.getSearchPath(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
    }

    /**
     * 搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        if (!initBookSourceBean() || isEmpty(bookSourceBean.getRuleSearchUrl())) {
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
        BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeSearchUrl analyzeSearchUrl = new AnalyzeSearchUrl(bookSourceBean.getRuleSearchUrl(), content, page);
            if (analyzeSearchUrl.getSearchUrl() == null) {
                return Observable.create(emitter -> {
                    emitter.onNext(new ArrayList<>());
                    emitter.onComplete();
                });
            }
            if (bookSourceBean.getRuleSearchUrl().contains("@")) {
                return getRetrofitString(analyzeSearchUrl.getSearchUrl())
                        .create(IHttpPostApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else if (bookSourceBean.getRuleSearchUrl().contains("?")) {
                return getRetrofitString(analyzeSearchUrl.getSearchUrl())
                        .create(IHttpGetApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else {
                return getRetrofitString(analyzeSearchUrl.getSearchUrl())
                        .create(IHttpGetApi.class)
                        .getWebContent(analyzeSearchUrl.getSearchPath(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
    }

    /**
     * 获取书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        if (!initBookSourceBean()) {
            return Observable.error(new Throwable(String.format("无法找到源%s", tag)));
        }
        BookInfo bookInfo = new BookInfo(tag, name, bookSourceBean);
        return getRetrofitString(tag)
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getNoteUrl(), headerMap)
                .flatMap(response -> bookInfo.analyzeBookInfo(response.body(), bookShelfBean));
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
        BookChapter bookChapter = new BookChapter(tag, name, bookSourceBean);
        return getRetrofitString(tag)
                .create(IHttpGetApi.class)
                .getWebContent(bookShelfBean.getBookInfoBean().getChapterUrl(), headerMap)
                .flatMap(response -> bookChapter.analyzeChapterList(response.body(), bookShelfBean));
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
        BookContent bookContent = new BookContent(tag, name, bookSourceBean);
        return getRetrofitString(tag)
                .create(IHttpGetApi.class)
                .getWebContent(durChapterUrl, headerMap)
                .flatMap(response -> bookContent.analyzeBookContent(response.body(), durChapterUrl, durChapterIndex))
                .flatMap(bookContent::upChapterList);
    }

}
