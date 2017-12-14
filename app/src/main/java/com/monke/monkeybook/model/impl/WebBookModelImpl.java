//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.listener.OnGetChapterListListener;
import com.monke.monkeybook.model.IWebBookModel;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;

public class WebBookModelImpl implements IWebBookModel {

    public static WebBookModelImpl getInstance() {
        return new WebBookModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络请求并解析书籍信息
     * return BookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        if(bookShelfBean.getTag().equals(GxwztvBookModelImpl.TAG)){
            return GxwztvBookModelImpl.getInstance().getBookInfo(bookShelfBean);
        }
        else if(bookShelfBean.getTag().equals(LingdiankanshuModelImpl.TAG)){
            return LingdiankanshuModelImpl.getInstance().getBookInfo(bookShelfBean);
        }
        else if(bookShelfBean.getTag().equals(ContentXBQGModelImpl.TAG)){
            return ContentXBQGModelImpl.getInstance().getBookInfo(bookShelfBean);
        }
        else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络解析图书目录
     * return BookShelfBean
     */
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener) {
        if(bookShelfBean.getTag().equals(GxwztvBookModelImpl.TAG)){
            GxwztvBookModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        }
        else if(bookShelfBean.getTag().equals(LingdiankanshuModelImpl.TAG)){
            LingdiankanshuModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        }
        else if(bookShelfBean.getTag().equals(ContentXBQGModelImpl.TAG)){
            ContentXBQGModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        }
        else{
            if(getChapterListListener!=null)
                getChapterListListener.success(bookShelfBean);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 章节缓存
     */
    @Override
    public Observable<BookContentBean> getBookContent(String durChapterUrl, int durChapterIndex, String tag) {
        if(tag.equals(GxwztvBookModelImpl.TAG)){
            return GxwztvBookModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        }
        else if(tag.equals(LingdiankanshuModelImpl.TAG)){
            return LingdiankanshuModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        }
        else if(tag.equals(ContentXBQGModelImpl.TAG)){
            return ContentXBQGModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        }
        else
            return Observable.create(e -> {
                e.onNext(new BookContentBean());
                e.onComplete();
            });
    }

    /**
     * 其他站点集合搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchOtherBook(String content,int page,String tag){
        if(tag.equals(GxwztvBookModelImpl.TAG)){
            return GxwztvBookModelImpl.getInstance().searchBook(content, page);
        }
        else if(tag.equals(LingdiankanshuModelImpl.TAG)){
            return LingdiankanshuModelImpl.getInstance().searchBook(content, page);
        }
        else if(tag.equals(ContentXBQGModelImpl.TAG)){
            return ContentXBQGModelImpl.getInstance().searchBook(content, page);
        }
        else{
            return Observable.create(e -> {
                e.onNext(new ArrayList<SearchBookBean>());
                e.onComplete();
            });
        }
    }
    /**
     * 获取分类书籍
     */
    @Override
    public Observable<List<SearchBookBean>> getKindBook(String url,int page) {
        return GxwztvBookModelImpl.getInstance().getKindBook(url,page);
    }
}
