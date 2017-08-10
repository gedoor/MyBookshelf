package com.monke.monkeybook.model;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.listener.OnGetChapterListListener;
import java.util.List;
import io.reactivex.Observable;

public interface IWebBookModel {
    /**
     * 网络请求并解析书籍信息
     */
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean);

    /**
     * 网络解析图书目录
     */
    public void getChapterList(final BookShelfBean bookShelfBean,OnGetChapterListListener getChapterListListener);

    /**
     * 章节缓存
     */
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex, String tag);

    /**
     * 搜索书籍
     */
    public Observable<List<SearchBookBean>> searchBook(String content, int page,int rankKind);

    /**
     * 获取分类书籍
     */
    public Observable<List<SearchBookBean>> getKindBook(String url,int page);
    /**
     * 其他站点资源整合搜索
     */
    public Observable<List<SearchBookBean>> searchOtherBook(String content,int page,String tag);
}
