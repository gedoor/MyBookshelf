package com.monke.monkeybook.model;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LibraryBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.cache.ACache;
import com.monke.monkeybook.listener.OnGetChapterListListener;

import java.util.List;

import io.reactivex.Observable;

public interface IEasouBookModel {
    /**
     * 搜索书籍
     */
    public Observable<List<SearchBookBean>> searchBook(String content, int page, int rankKind);

    /**
     * 网络请求并解析书籍信息
     */
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean);

    /**
     * 网络解析图书目录
     */
    public void getChapterList(final BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener);

    /**
     * 章节缓存
     */
    Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex);
}
