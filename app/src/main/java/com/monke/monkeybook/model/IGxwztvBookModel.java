package com.monke.monkeybook.model;

import com.monke.monkeybook.bean.LibraryBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.cache.ACache;

import java.util.List;

import io.reactivex.Observable;

public interface IGxwztvBookModel extends IStationBookModel {

    public Observable<List<SearchBookBean>> getKindBook(String url, int page);

    /**
     * 获取主页信息
     */
    public Observable<LibraryBean> getLibraryData(ACache aCache);

    /**
     * 解析主页数据
     */
    public Observable<LibraryBean> analyLibraryData(String data);
}
