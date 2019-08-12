//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.model;

import android.annotation.SuppressLint;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.content.WebBook;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;
import static com.kunfei.bookshelf.constant.AppConstant.TIME_OUT;

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
                .timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    /**
     * 网络解析图书目录
     * return BookShelfBean
     */
    public Observable<List<BookChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        return WebBook.getInstance(bookShelfBean.getTag())
                .getChapterList(bookShelfBean)
                .flatMap((chapterList) -> upChapterList(bookShelfBean, chapterList))
                .timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    /**
     * 章节缓存
     */
    public Observable<BookContentBean> getBookContent(BookShelfBean bookShelfBean, BaseChapterBean chapterBean, BaseChapterBean nextChapterBean) {
        return WebBook.getInstance(chapterBean.getTag())
                .getBookContent(chapterBean, nextChapterBean, bookShelfBean)
                .flatMap((bookContentBean -> saveContent(bookShelfBean.getBookInfoBean(), chapterBean, bookContentBean)))
                .timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    /**
     * 搜索
     */
    public Observable<List<SearchBookBean>> searchBook(String content, int page, String tag) {
        return WebBook.getInstance(tag)
                .searchBook(content, page)
                .timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    /**
     * 发现页
     */
    public Observable<List<SearchBookBean>> findBook(String url, int page, String tag) {
        return WebBook.getInstance(tag)
                .findBook(url, page)
                .timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    /**
     * 更新目录
     */
    private Observable<List<BookChapterBean>> upChapterList(BookShelfBean bookShelfBean, List<BookChapterBean> chapterList) {
        return Observable.create(e -> {
            for (int i = 0; i < chapterList.size(); i++) {
                BookChapterBean chapter = chapterList.get(i);
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
                bookShelfBean.setDurChapterName(chapterList.get(bookShelfBean.getDurChapter()).getDurChapterName());
                bookShelfBean.setLastChapterName(chapterList.get(chapterList.size() - 1).getDurChapterName());
            }
            e.onNext(chapterList);
            e.onComplete();
        });
    }

    /**
     * 保存章节
     */
    @SuppressLint("DefaultLocale")
    private Observable<BookContentBean> saveContent(BookInfoBean infoBean, BaseChapterBean chapterBean, BookContentBean bookContentBean) {
        return Observable.create(e -> {
            bookContentBean.setNoteUrl(chapterBean.getNoteUrl());
            if (isEmpty(bookContentBean.getDurChapterContent())) {
                e.onError(new Throwable("下载章节出错"));
            } else if (infoBean.isAudio()) {
                bookContentBean.setTimeMillis(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
                DbHelper.getDaoSession().getBookContentBeanDao().insertOrReplace(bookContentBean);
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
