package com.kunfei.bookshelf.help;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.bean.TwoDataBean;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;

public class ChangeSourceHelp {
    private SearchBookModel searchBookModel;
    private BookShelfBean bookShelfBean;
    private ChangeSourceListener changeSourceListener;
    private boolean finish;

    public ChangeSourceHelp() {
        SearchBookModel.OnSearchListener searchListener = new SearchBookModel.OnSearchListener() {
            @Override
            public void refreshSearchBook() {

            }

            @Override
            public void refreshFinish(Boolean value) {

            }

            @Override
            public void loadMoreFinish(Boolean value) {

            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                selectBook(value);
            }

            @Override
            public void searchBookError(Throwable throwable) {
                if (!finish && changeSourceListener != null) {
                    changeSourceListener.error(throwable);
                    searchBookModel.onDestroy();
                }
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
        searchBookModel = new SearchBookModel(searchListener);
    }

    public void autoChange(BookShelfBean bookShelfBean, ChangeSourceListener changeSourceListener) {
        this.bookShelfBean = bookShelfBean;
        this.changeSourceListener = changeSourceListener;
        long searchTime = System.currentTimeMillis();
        finish = false;
        searchBookModel.setSearchTime(searchTime);
        searchBookModel.search(bookShelfBean.getBookInfoBean().getName(), searchTime, new ArrayList<>(), false);
    }

    private synchronized void selectBook(List<SearchBookBean> value) {
        if (finish) return;
        for (SearchBookBean searchBookBean : value) {
            if (Objects.equals(searchBookBean.getName(), bookShelfBean.getBookInfoBean().getName())) {
                if (Objects.equals(searchBookBean.getAuthor(), bookShelfBean.getBookInfoBean().getAuthor())) {
                    if (changeSourceListener != null) {
                        finish = true;
                        changeBookSource(searchBookBean, bookShelfBean)
                                .subscribe(new MyObserver<TwoDataBean<BookShelfBean, List<BookChapterBean>>>() {
                                    @Override
                                    public void onNext(TwoDataBean<BookShelfBean, List<BookChapterBean>> twoData) {
                                        changeSourceListener.finish(twoData.getData1(), twoData.getData2());
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        changeSourceListener.error(e);
                                    }
                                });
                    }
                    searchBookModel.onDestroy();
                    break;
                }
            } else {
                break;
            }
        }
    }

    public void stopSearch() {
        if (searchBookModel != null) {
            searchBookModel.onDestroy();
        }
    }

    public static Observable<TwoDataBean<BookShelfBean, List<BookChapterBean>>> changeBookSource(SearchBookBean searchBook, BookShelfBean oldBook) {
        BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBook);
        bookShelfBean.setSerialNumber(oldBook.getSerialNumber());
        bookShelfBean.setLastChapterName(oldBook.getLastChapterName());
        bookShelfBean.setDurChapterName(oldBook.getDurChapterName());
        bookShelfBean.setDurChapter(oldBook.getDurChapter());
        bookShelfBean.setDurChapterPage(oldBook.getDurChapterPage());
        bookShelfBean.setReplaceEnable(oldBook.getReplaceEnable());
        return WebBookModel.getInstance().getBookInfo(bookShelfBean)
                .flatMap(book -> WebBookModel.getInstance().getChapterList(book))
                .flatMap(chapterBeanList -> saveChangedBook(bookShelfBean, oldBook, chapterBeanList))
                .compose(RxUtils::toSimpleSingle);
    }

    private static Observable<TwoDataBean<BookShelfBean, List<BookChapterBean>>> saveChangedBook(BookShelfBean newBook, BookShelfBean oldBook, List<BookChapterBean> chapterBeanList) {
        return Observable.create(e -> {
            if (newBook.getChapterListSize() <= oldBook.getChapterListSize()) {
                newBook.setHasUpdate(false);
            }
            newBook.setCustomCoverPath(oldBook.getCustomCoverPath());
            newBook.setDurChapter(BookshelfHelp.getDurChapter(oldBook.getDurChapter(), oldBook.getChapterListSize(), oldBook.getDurChapterName(), chapterBeanList));
            newBook.setDurChapterName(chapterBeanList.get(newBook.getDurChapter()).getDurChapterName());
            newBook.setGroup(oldBook.getGroup());
            newBook.getBookInfoBean().setName(oldBook.getBookInfoBean().getName());
            newBook.getBookInfoBean().setAuthor(oldBook.getBookInfoBean().getAuthor());
            BookshelfHelp.removeFromBookShelf(oldBook);
            BookshelfHelp.saveBookToShelf(newBook);
            DbHelper.getDaoSession().getBookChapterBeanDao().insertOrReplaceInTx(chapterBeanList);
            e.onNext(new TwoDataBean<>(newBook, chapterBeanList));
            e.onComplete();
        });
    }

    public interface ChangeSourceListener {
        void finish(BookShelfBean bookShelfBean, List<BookChapterBean> chapterBeanList);

        void error(Throwable throwable);
    }

}
