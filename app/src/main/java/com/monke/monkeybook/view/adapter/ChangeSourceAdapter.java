package com.monke.monkeybook.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ChangeSourceAdapter extends RefreshRecyclerViewAdapter implements View.OnClickListener {
    private List<SearchBookBean> searchBookBeans;
    private OnItemClickListener mOnItemClickListener;

    public ChangeSourceAdapter(Boolean needLoadMore) {
        super(needLoadMore);
        searchBookBeans = new ArrayList<>();
    }

    public void addSourceAdapter(SearchBookBean value) {
        searchBookBeans.add(value);
        notifyDataSetChanged();
    }

    public void addAllSourceAdapter(List<SearchBookBean> value) {
        searchBookBeans.addAll(value);
        notifyDataSetChanged();
    }

    public void reSetSourceAdapter() {
        searchBookBeans.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(view, (int) view.getTag());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int index);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public List<SearchBookBean> getSearchBookBeans() {
        return searchBookBeans;
    }

    class thisViewHolder extends RecyclerView.ViewHolder{
        TextView tvBookSource;
        ImageView ivChecked;

        thisViewHolder(View itemView) {
            super(itemView);
            tvBookSource = itemView.findViewById(R.id.tv_source_name);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        return new thisViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_change_source_item, parent, false));
    }

    @Override
    public void onBindViewholder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        ((thisViewHolder) holder).tvBookSource.setText(searchBookBeans.get(position).getOrigin());
        if (searchBookBeans.get(position).getIsAdd()) {
            ((thisViewHolder) holder).ivChecked.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewtype(int position) {
        return 0;
    }

    @Override
    public int getItemcount() {
        return searchBookBeans.size();
    }
}
