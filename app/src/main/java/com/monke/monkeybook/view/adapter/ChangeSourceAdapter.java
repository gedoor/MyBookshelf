package com.monke.monkeybook.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ChangeSourceAdapter extends RecyclerView.Adapter<ChangeSourceAdapter.MyViewHolder> implements View.OnClickListener{
    private List<SearchBookBean> searchBookBeans = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public void addSourceAdapter(List<SearchBookBean> value, String bookName) {
        if (value.get(0).getName().equals(bookName)) {
            searchBookBeans.add(value.get(0));
            notifyDataSetChanged();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookSource;
        ImageView ivChecked;

        MyViewHolder(View itemView) {
            super(itemView);
            tvBookSource = itemView.findViewById(R.id.tv_source_name);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(view,(int)view.getTag());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int index);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public List<SearchBookBean> getSearchBookBeans() {
        return searchBookBeans;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_change_source_item, parent, false);
        view.setOnClickListener(this);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.tvBookSource.setText(searchBookBeans.get(position).getOrigin());
        if (searchBookBeans.get(position).getAdd()) {
            holder.ivChecked.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return searchBookBeans.size();
    }
}
