package com.monke.monkeybook.view.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.monke.basemvplib.BaseActivity;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.view.activity.BookSourceActivity;
import com.monke.monkeybook.view.activity.SourceEditActivity;

import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class BookSourceAdapter extends RecyclerView.Adapter<BookSourceAdapter.MyViewHolder> {
    private List<BookSourceBean> bookSourceBeanList;
    private BaseActivity activity;

    public BookSourceAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    public void addBookSource(List<BookSourceBean> bookSourceBeanList) {
        this.bookSourceBeanList = bookSourceBeanList;
        notifyDataSetChanged();
    }

    public void resetBookSource(List<BookSourceBean> bookSourceBeanList) {
        this.bookSourceBeanList = bookSourceBeanList;
        notifyDataSetChanged();
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
        holder.editSource.setOnClickListener(view -> {
            Gson gson = new Gson();
            String bs = gson.toJson(bookSourceBeanList.get(position));
            Intent intent = new Intent(activity, SourceEditActivity.class);
            intent.putExtra("bookSource", bs);
            activity.startActivityForResult(intent, BookSourceActivity.EDIT_SOURCE);
        });
    }

    @Override
    public int getItemCount() {
        return bookSourceBeanList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox bookSource;
        ImageView editSource;

        MyViewHolder(View itemView) {
            super(itemView);
            bookSource = itemView.findViewById(R.id.cb_book_source);
            editSource = itemView.findViewById(R.id.iv_edit_source);
        }
    }
}
