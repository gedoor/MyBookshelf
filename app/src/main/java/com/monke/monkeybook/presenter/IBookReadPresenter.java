//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.app.Activity;
import android.content.Intent;

import com.monke.basemvplib.IPresenter;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.presenter.impl.ReadBookPresenterImpl;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;

public interface IBookReadPresenter extends IPresenter{

    int getOpen_from();

    BookShelfBean getBookShelf();

    void initContent();

    void loadContent(BookContentView bookContentView,long bookTag, final int chapterIndex, final int page, boolean isReload);

    void updateProgress(int chapterIndex, int pageIndex);

    void saveProgress();

    String getChapterTitle(int chapterIndex);

    void setPageLineCount(int pageLineCount);

    void addToShelf(final ReadBookPresenterImpl.OnAddListner addListner);

    Boolean getAdd();

    void initData(Activity activity);

    void openBookFromOther(Activity activity);
}
