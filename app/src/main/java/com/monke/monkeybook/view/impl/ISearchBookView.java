//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.impl;

import android.widget.EditText;

import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;

import java.util.List;

public interface ISearchBookView extends IView{

    /**
     * 成功 新增查询记录
     * @param searchHistoryBean
     */
    void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean);

    /**
     * 成功搜索 搜索记录
     * @param datas
     */
    void querySearchHistorySuccess(List<SearchHistoryBean> datas);

    /**
     * 首次查询成功 更新UI
     */
    void refreshSearchBook();

    /**
     * 加载更多书籍成功 更新UI
     * @param books
     */
    void loadMoreSearchBook(List<SearchBookBean> books);

    /**
     * 刷新成功
     * @param isAll
     */
    void refreshFinish(Boolean isAll);

    /**
     * 加载成功
     * @param isAll
     */
    void loadMoreFinish(Boolean isAll);

    /**
     * 搜索失败
     * @param isRefresh
     */
    void searchBookError(Boolean isRefresh);

    /**
     * 获取搜索内容EditText
     */
    EditText getEdtContent();

    /**
     * 添加书籍失败
     */
    void addBookShelfFailed(String massage);

    /**
     * @return SearchBookAdapter
     */
    SearchBookAdapter getSearchBookAdapter();

    /**
     * @param index
     */
    void updateSearchItem(int index);

    /**
     * 判断书籍是否已经在书架上
     */
    Boolean checkIsExist(SearchBookBean searchBookBean);
}
