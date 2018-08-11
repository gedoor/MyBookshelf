package com.monke.monkeybook.view.adapter;

import android.app.Activity;
import android.content.SharedPreferences;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.view.adapter.base.BaseListAdapter;
import com.monke.monkeybook.view.adapter.base.IViewHolder;
import com.monke.monkeybook.view.adapter.view.BookShelfHolder;
import com.monke.monkeybook.view.adapter.view.BookShelfHolderGrid;
import com.monke.monkeybook.view.adapter.view.BookShelfHolderList;

public class BookShelfAdapter extends BaseListAdapter<BookShelfBean> {

    private BookShelfHolder bookShelfHolder;
    private Activity activity;
    private SharedPreferences preferences;

    BookShelfAdapter(Activity activity, SharedPreferences preferences) {
        this.activity = activity;
        this.preferences = preferences;
    }

    @Override
    protected IViewHolder<BookShelfBean> createViewHolder(int viewType) {
        if (preferences.getBoolean("bookshelfIsList", true)) {
            bookShelfHolder = new BookShelfHolderList(activity, preferences.getBoolean(activity.getString(R.string.pk_bookshelf_anim), false));
        } else {
            bookShelfHolder = new BookShelfHolderGrid(activity, preferences.getBoolean(activity.getString(R.string.pk_bookshelf_anim), false));
        }
        return bookShelfHolder;
    }








}
