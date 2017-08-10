package com.monke.monkeybook.view;

import com.monke.basemvplib.IView;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.view.adapter.BookShelfAdapter;

import java.util.List;

public interface IMainView extends IView{

    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList);

    public void activityRefreshView();

    public void refreshFinish();

    public void refreshError(String error);

    public List<BookShelfBean> getBookShelfAdapterDatas();

    public void refreshRecyclerViewItemAdd();

    public void setRecyclerMaxProgress(int x);
}
