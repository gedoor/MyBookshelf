package com.monke.monkeybook.view;

import android.widget.EditText;

import com.monke.basemvplib.IView;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;

import java.util.List;

public interface ISearchView extends IView{

    public void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean);

    public void querySearchHistorySuccess(List<SearchHistoryBean> datas);

    public void refreshSearchBook(List<SearchBookBean> books);

    public void loadMoreSearchBook(List<SearchBookBean> books);

    public void refreshFinish(Boolean isAll);

    public void loadMoreFinish(Boolean isAll);

    public void searchBookError(Boolean isRefresh);

    public EditText getEdtContent();

    public void addBookShelfFailed(int code);

    public SearchBookAdapter getSearchBookAdapter();

    public void updateSearchItem(int index);

    public Boolean checkIsExist(SearchBookBean searchBookBean);
}
