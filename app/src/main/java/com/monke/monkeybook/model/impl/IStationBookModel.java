//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

import io.reactivex.Observable;

public interface IStationBookModel {

    /**
     * 发现书籍
     */
     Observable<List<SearchBookBean>> findBook(String url, int page);

    /**
     * 搜索书籍
     */
    Observable<List<SearchBookBean>> searchBook(String content, int page);

    /**
     * 网络请求并解析书籍信息
     */
    Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean);

    /**
     * 网络解析图书目录
     */
    Observable<BookShelfBean> getChapterList(final BookShelfBean bookShelfBean);

    /**
     * 章节缓存
     */
    Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex);


}
