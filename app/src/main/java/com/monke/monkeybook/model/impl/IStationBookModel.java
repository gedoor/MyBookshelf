//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

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
     *
     * @param scheduler       　执行进程
     * @param durChapterUrl   　章节地址
     * @param durChapterIndex 　章节序号
     */
    Observable<BookContentBean> getBookContent(final Scheduler scheduler, final String durChapterUrl, final int durChapterIndex);


}
