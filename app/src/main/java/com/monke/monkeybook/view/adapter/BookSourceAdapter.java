package com.monke.monkeybook.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;

import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class BookSourceAdapter extends RecyclerView.Adapter<BookSourceAdapter.MyViewHolder> {
    private List<BookSourceBean> bookSourceBeanList;

    public BookSourceAdapter(List<BookSourceBean> bookSourceBeanList) {
        this.bookSourceBeanList = bookSourceBeanList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox bookSource;

        MyViewHolder(View itemView) {
            super(itemView);
            bookSource = itemView.findViewById(R.id.book_source);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_book_source_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bookSource.setText(bookSourceBeanList.get(position).getBookSourceName());
        holder.bookSource.setChecked(bookSourceBeanList.get(position).getEnable());
    }

    @Override
    public int getItemCount() {
        return bookSourceBeanList.size();
    }
}
