package com.kunfei.bookshelf.view.adapter.base;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by newbiechen on 17-5-17.
 */

public interface IViewHolder<T> {
    View createItemView(ViewGroup parent);

    void initView();

    void onBind(T data, int pos);

    void onClick();
}
