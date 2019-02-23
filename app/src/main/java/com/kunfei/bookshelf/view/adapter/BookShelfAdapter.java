package com.kunfei.bookshelf.view.adapter;

import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.ItemTouchHelpCallback;
import com.kunfei.bookshelf.view.adapter.base.OnItemClickListenerTwo;

import java.util.List;

public interface BookShelfAdapter {
    ItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener();

    List<BookShelfBean> getBooks();

    void replaceAll(List<BookShelfBean> newDataS, String bookshelfPx);

    void refreshBook(String noteUrl);

    void setItemClickListener(OnItemClickListenerTwo itemClickListener);
}
