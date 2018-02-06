package com.monke.monkeybook.view.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.monke.basemvplib.BaseActivity;
import com.monke.monkeybook.BitIntentDataManager;
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
    private BookSourceActivity activity;

    public BookSourceAdapter(BookSourceActivity activity) {
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

    public List<BookSourceBean> getBookSourceBeanList() {
        return bookSourceBeanList;
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
        holder.bookSource.setOnClickListener((View view) -> {
            bookSourceBeanList.get(position).setEnable(holder.bookSource.isChecked());
        });
        holder.editSource.setOnClickListener(view -> {
            Intent intent = new Intent(activity, SourceEditActivity.class);
            String key = String.valueOf(System.currentTimeMillis());
            intent.putExtra("data_key", key);
            try {
                BitIntentDataManager.getInstance().putData(key, bookSourceBeanList.get(position).clone());
            } catch (CloneNotSupportedException e) {
                BitIntentDataManager.getInstance().putData(key, bookSourceBeanList.get(position));
                e.printStackTrace();
            }
            activity.startActivityForResult(intent, BookSourceActivity.EDIT_SOURCE);
        });
        holder.delSource.setOnClickListener(view -> {
            activity.delBookSource(bookSourceBeanList.get(position));
            bookSourceBeanList.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return bookSourceBeanList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox bookSource;
        ImageView editSource;
        ImageView delSource;

        MyViewHolder(View itemView) {
            super(itemView);
            bookSource = itemView.findViewById(R.id.cb_book_source);
            editSource = itemView.findViewById(R.id.iv_edit_source);
            delSource = itemView.findViewById(R.id.iv_del_source);
        }
    }
}
