package com.monke.monkeybook.view.adapter;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.view.activity.BookSourceActivity;
import com.monke.monkeybook.view.activity.SourceEditActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class BookSourceAdapter extends RecyclerView.Adapter<BookSourceAdapter.MyViewHolder> {
    private List<BookSourceBean> dataList;
    private BookSourceActivity activity;

    public BookSourceAdapter(BookSourceActivity activity) {
        this.activity = activity;
        dataList = new ArrayList<>();
    }

    public void addDataS(List<BookSourceBean> bookSourceBeanList) {
        this.dataList = bookSourceBeanList;
        notifyDataSetChanged();
    }

    public void resetDataS(List<BookSourceBean> bookSourceBeanList) {
        this.dataList = bookSourceBeanList;
        notifyDataSetChanged();
    }

    public List<BookSourceBean> getBookSourceBeanList() {
        return dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_book_source_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.cbView.setText(dataList.get(position).getBookSourceName());
        holder.cbView.setChecked(dataList.get(position).getEnable());
        holder.cbView.setOnClickListener((View view) -> dataList.get(position).setEnable(holder.cbView.isChecked()));
        holder.editView.getDrawable().mutate();
        holder.editView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        holder.editView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, SourceEditActivity.class);
            String key = String.valueOf(System.currentTimeMillis());
            intent.putExtra("data_key", key);
            try {
                BitIntentDataManager.getInstance().putData(key, dataList.get(position).clone());
            } catch (CloneNotSupportedException e) {
                BitIntentDataManager.getInstance().putData(key, dataList.get(position));
                e.printStackTrace();
            }
            activity.startActivityForResult(intent, BookSourceActivity.EDIT_SOURCE);
        });
        holder.delView.getDrawable().mutate();
        holder.delView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        holder.delView.setOnClickListener(view -> {
            activity.delBookSource(dataList.get(position));
            dataList.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbView;
        ImageView editView;
        ImageView delView;

        MyViewHolder(View itemView) {
            super(itemView);
            cbView = itemView.findViewById(R.id.cb_book_source);
            editView = itemView.findViewById(R.id.iv_edit_source);
            delView = itemView.findViewById(R.id.iv_del_source);
        }
    }
}
