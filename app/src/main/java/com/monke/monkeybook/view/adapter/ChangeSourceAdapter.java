package com.monke.monkeybook.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ChangeSourceAdapter extends RecyclerView.Adapter<ChangeSourceAdapter.MyViewHolder> {
    private List<SearchBookBean> searchBookBeans = new ArrayList<>();

    public void addSourceAdapter(List<SearchBookBean> value, String bookName) {
        if (value.get(0).getName().equals(bookName)) {
            searchBookBeans.add(value.get(0));
            notifyDataSetChanged();
        }
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_change_source_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bookSource.setText(searchBookBeans.get(position).getTag());
        holder.bookSource.setChecked(searchBookBeans.get(position).getAdd());
    }

    @Override
    public int getItemCount() {
        return searchBookBeans.size();
    }
}
