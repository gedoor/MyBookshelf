//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter.impl;

import android.app.Activity;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;

public interface IReadBookPresenter extends IPresenter {

    int getOpen_from();

    BookShelfBean getBookShelf();

    void initContent();

    void loadContent(BookContentView bookContentView,long bookTag, final int chapterIndex, final int page);

    void updateProgress(int chapterIndex, int pageIndex);

    void saveProgress();

    String getChapterTitle(int chapterIndex);

    void setPageLineCount(int pageLineCount);

    void setPageWidth(int pageWidth);

    void addToShelf(final ReadBookPresenterImpl.OnAddListener addListner);

    void removeFromShelf();

    void initData(Activity activity);

    void openBookFromOther(Activity activity);

    void addDownload(int start, int end);

    void changeBookSource(SearchBookBean searchBookBean);
}
