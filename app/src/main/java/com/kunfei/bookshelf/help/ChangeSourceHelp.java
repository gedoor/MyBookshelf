package com.kunfei.bookshelf.help;

import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;

public class ChangeSourceHelp {
    private SearchBookModel searchBookModel;

    public void autoChange(BookShelfBean bookShelfBean, ChangeSourceListener changeSourceListener) {
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
            public Boolean checkIsExist(SearchBookBean searchBookBean) {
                return true;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                for (SearchBookBean searchBookBean : value) {
                    if (Objects.equals(searchBookBean.getName(), bookShelfBean.getBookInfoBean().getName())) {
                        if (Objects.equals(searchBookBean.getAuthor(), bookShelfBean.getBookInfoBean().getAuthor())) {
                            if (changeSourceListener != null) {
                                changeSourceListener.finish(changeBookSource(searchBookBean, bookShelfBean));
                            }
                            searchBookModel.onDestroy();
                        }
                    } else {
                        break;
                    }
                }
            }

            @Override
            public void searchBookError(Boolean value) {
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
        searchBookModel = new SearchBookModel(searchListener);
        searchBookModel.search(bookShelfBean.getBookInfoBean().getName(), System.currentTimeMillis(), new ArrayList<>(), false);
    }

    public static Observable<BookShelfBean> changeBookSource(SearchBookBean searchBook, BookShelfBean oldBook) {
        BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBook);
        bookShelfBean.setSerialNumber(oldBook.getSerialNumber());
        bookShelfBean.setLastChapterName(oldBook.getLastChapterName());
        bookShelfBean.setDurChapterName(oldBook.getDurChapterName());
        bookShelfBean.setDurChapter(oldBook.getDurChapter());
        bookShelfBean.setDurChapterPage(oldBook.getDurChapterPage());
        return WebBookModel.getInstance().getBookInfo(bookShelfBean)
                .flatMap(book -> WebBookModel.getInstance().getChapterList(book))
                .flatMap(book -> saveChangedBook(book, oldBook))
                .compose(RxUtils::toSimpleSingle);
    }

    private static Observable<BookShelfBean> saveChangedBook(BookShelfBean newBook, BookShelfBean oldBook) {
        return Observable.create(e -> {
            if (newBook.getChapterListSize() <= oldBook.getChapterListSize()) {
                newBook.setHasUpdate(false);
            }
            newBook.setCustomCoverPath(oldBook.getCustomCoverPath());
            newBook.setDurChapter(BookshelfHelp.getDurChapter(oldBook, newBook));
            newBook.setDurChapterName(newBook.getChapter(newBook.getDurChapter()).getDurChapterName());
            newBook.setGroup(oldBook.getGroup());
            BookshelfHelp.removeFromBookShelf(oldBook);
            BookshelfHelp.saveBookToShelf(newBook);
            e.onNext(newBook);
            e.onComplete();
        });
    }

    public interface ChangeSourceListener {
        void finish(Observable<BookShelfBean> bookShelfBeanO);
    }
}
