package com.monke.monkeybook.presenter;

import android.app.Activity;
import android.content.Intent;

import com.monke.basemvplib.IPresenter;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.presenter.impl.ReadBookPresenterImpl;
import com.monke.monkeybook.widget.contentswitchview.BookContentView;

public interface IBookReadPresenter extends IPresenter{

    public int getOpen_from();

    public BookShelfBean getBookShelf();

    public void initContent();

    public void loadContent(BookContentView bookContentView,long bookTag, final int chapterIndex, final int page);

    public void updateProgress(int chapterIndex, int pageIndex);

    public void saveProgress();

    public String getChapterTitle(int chapterIndex);

    public void setPageLineCount(int pageLineCount);

    public void addToShelf(final ReadBookPresenterImpl.OnAddListner addListner);

    public Boolean getAdd();

    public void initData(Activity activity);

    public void openBookFromOther(Activity activity);
}
