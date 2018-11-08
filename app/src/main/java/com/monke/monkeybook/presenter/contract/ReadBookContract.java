package com.monke.monkeybook.presenter.contract;

import android.app.Activity;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.presenter.ReadBookPresenter;

public interface ReadBookContract {
    interface View extends IView {

        /**
         * @return Book标志
         */
        String getNoteUrl();

        Boolean getAdd();

        void setAdd(Boolean isAdd);

        void changeSourceFinish(BookShelfBean book);

        /**
         * 开始加载
         */
        void startLoadingBook();

        void setHpbReadProgressMax(int count);

        void initChapterList();

        void upMenu();

        void openBookFromOther();

        void chapterChange(ChapterListBean chapterListBean);

        void onMediaButton();

        /**
         * 更新朗读状态
         */
        void upAloudState(int state);

        void upAloudTimer(String timer);

        void speakIndex(int index);

        void refresh(boolean recreate);

        void finish();
    }

    interface Presenter extends IPresenter {

        int getOpen_from();

        BookShelfBean getBookShelf();

        void saveProgress();

        String getChapterTitle(int chapterIndex);

        void addToShelf(final ReadBookPresenter.OnAddListener Listener);

        void removeFromShelf();

        void initData(Activity activity);

        void openBookFromOther(Activity activity);

        void addDownload(int start, int end);

        void changeBookSource(SearchBookBean searchBookBean);

        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void disableDurBookSource();
    }
}
