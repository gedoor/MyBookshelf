//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.model.impl;

import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.bean.SearchBookBean;

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
    Observable<List<ChapterListBean>> getChapterList(final BookShelfBean bookShelfBean);


    /**
     * 获取章节
     */
    Observable<BookContentBean> getBookContent(final BaseChapterBean chapterBean);


}
