//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.model;

import android.annotation.SuppressLint;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.content.WebBook;
import com.kunfei.bookshelf.utils.ACache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class WebBookModel {

    public static WebBookModel getInstance() {
        return new WebBookModel();
    }

    /**
     * 网络请求并解析书籍信息
     * return BookShelfBean
     */
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        return WebBook.getInstance(bookShelfBean.getTag())
                .getBookInfo(bookShelfBean)
                .timeout(60, TimeUnit.SECONDS);
    }

    /**
     * 网络解析图书目录
     * return BookShelfBean
     */
    public Observable<BookShelfBean> getChapterList(final BookShelfBean bookShelfBean) {
        return WebBook.getInstance(bookShelfBean.getTag())
                .getChapterList(bookShelfBean)
                .timeout(60, TimeUnit.SECONDS)
                .flatMap((chapterList) -> upChapterList(bookShelfBean, chapterList));
    }

    /**
     * 章节缓存
     */
    public Observable<BookContentBean> getBookContent(BaseChapterBean chapterBean) {
        return WebBook.getInstance(chapterBean.getTag())
                .getBookContent(chapterBean)
                .timeout(60, TimeUnit.SECONDS)
                .flatMap((bookContentBean -> saveContent(chapterBean, bookContentBean)));
    }

    /**
     * 搜索
     */
    public Observable<List<SearchBookBean>> searchBook(String content, int page, String tag) {
        return WebBook.getInstance(tag)
                .searchBook(content, page)
                .timeout(60, TimeUnit.SECONDS);
    }

    /**
     * 发现页
     */
    public Observable<List<SearchBookBean>> findBook(String url, int page, String tag) {
        return WebBook.getInstance(tag)
                .findBook(url, page)
                .timeout(60, TimeUnit.SECONDS);
    }

    /**
     * 更新目录
     */
    private Observable<BookShelfBean> upChapterList(BookShelfBean bookShelfBean, List<ChapterListBean> chapterList) {
        return Observable.create(e -> {
            for (int i = 0; i < chapterList.size(); i++) {
                ChapterListBean chapter = chapterList.get(i);
                chapter.setDurChapterIndex(i);
                chapter.setTag(bookShelfBean.getTag());
                chapter.setNoteUrl(bookShelfBean.getNoteUrl());
            }
            if (bookShelfBean.getChapterListSize() < chapterList.size()) {
                bookShelfBean.setHasUpdate(true);
                bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
            }
            if (!chapterList.isEmpty()) {
                bookShelfBean.setChapterListSize(chapterList.size());
                bookShelfBean.setDurChapter(Math.min(bookShelfBean.getDurChapter(), bookShelfBean.getChapterListSize() - 1));
                bookShelfBean.getBookInfoBean().setChapterList(chapterList);
                bookShelfBean.upDurChapterName();
                bookShelfBean.upLastChapterName();
                BookshelfHelp.delChapterList(bookShelfBean.getNoteUrl());
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /**
     * 保存章节
     */
    @SuppressLint("DefaultLocale")
    private Observable<BookContentBean> saveContent(BaseChapterBean chapterBean, BookContentBean bookContentBean) {
        return Observable.create(e -> {
            bookContentBean.setNoteUrl(chapterBean.getNoteUrl());
            BookInfoBean infoBean = DbHelper.getDaoSession().getBookInfoBeanDao().load(chapterBean.getNoteUrl());
            if (bookContentBean.getDurChapterContent() == null) {
                e.onError(new Throwable("下载章节出错"));
            } else if (infoBean.isAudio()) {
                ACache.get(MApplication.getInstance()).put(chapterBean.getDurChapterUrl(), bookContentBean.getDurChapterContent(), ACache.TIME_HOUR);
                e.onNext(bookContentBean);
            } else if (BookshelfHelp.saveChapterInfo(infoBean.getName() + "-" + chapterBean.getTag(), chapterBean.getDurChapterIndex(),
                    chapterBean.getDurChapterName(), bookContentBean.getDurChapterContent())) {
                RxBus.get().post(RxBusTag.CHAPTER_CHANGE, chapterBean);
                e.onNext(bookContentBean);
            } else {
                e.onError(new Throwable("保存章节出错"));
            }
            e.onComplete();
        });
    }
}
