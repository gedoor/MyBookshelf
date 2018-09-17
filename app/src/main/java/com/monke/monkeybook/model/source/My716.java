package com.monke.monkeybook.model.source;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.util.List;

import io.reactivex.Observable;

public class My716 extends BaseModelImpl implements IStationBookModel {
    private final String name = "My716";

    /**
     * 发现书籍
     *
     * @param url
     * @param page
     */
    @Override
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        return null;
    }

    /**
     * 搜索书籍
     *
     * @param content
     * @param page
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return null;
    }

    /**
     * 网络请求并解析书籍信息
     *
     * @param bookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        return null;
    }

    /**
     * 网络解析图书目录
     *
     * @param bookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getChapterList(BookShelfBean bookShelfBean) {
        return null;
    }

    /**
     * 章节缓存
     *
     * @param durChapterUrl
     * @param durChapterIndex
     */
    @Override
    public Observable<BookContentBean> getBookContent(String durChapterUrl, int durChapterIndex) {
        return null;
    }
}
