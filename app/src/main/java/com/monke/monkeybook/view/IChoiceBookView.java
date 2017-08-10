package com.monke.monkeybook.view;

import com.monke.basemvplib.IView;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.view.adapter.ChoiceBookAdapter;
import java.util.List;

public interface IChoiceBookView extends IView{

    public void refreshSearchBook(List<SearchBookBean> books);

    public void loadMoreSearchBook(List<SearchBookBean> books);

    public void refreshFinish(Boolean isAll);

    public void loadMoreFinish(Boolean isAll);

    public void searchBookError();

    public void addBookShelfSuccess(List<SearchBookBean> searchBooks);

    public void addBookShelfFailed(int code);

    public ChoiceBookAdapter getSearchBookAdapter();

    public void updateSearchItem(int index);

    public void startRefreshAnim();
}
