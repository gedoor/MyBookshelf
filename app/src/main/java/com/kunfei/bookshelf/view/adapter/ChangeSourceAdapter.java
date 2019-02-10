package com.kunfei.bookshelf.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.view.adapter.base.BaseListAdapter;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ChangeSourceAdapter extends RefreshRecyclerViewAdapter {
    private List<SearchBookBean> allBookBeans;
    private BaseListAdapter.OnItemClickListener onItemClickListener;
    private BaseListAdapter.OnItemLongClickListener onItemLongClickListener;

    public ChangeSourceAdapter(Boolean needLoadMore) {
        super(needLoadMore);
        allBookBeans = new ArrayList<>();
    }

    public void addSourceAdapter(SearchBookBean value) {
        allBookBeans.add(value);
        notifyDataSetChanged();
    }

    public void addAllSourceAdapter(List<SearchBookBean> value) {
        allBookBeans.addAll(value);
        notifyDataSetChanged();
    }

    public void reSetSourceAdapter() {
        allBookBeans.clear();
            notifyDataSetChanged();
    }

    public void removeData(int pos) {
        DbHelper.getDaoSession().getSearchBookBeanDao().delete(allBookBeans.get(pos));
        getSearchBookBeans().remove(pos);
        notifyItemRemoved(pos);
    }

    public void setOnItemClickListener(BaseListAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(BaseListAdapter.OnItemLongClickListener itemLongClickListener) {
        this.onItemLongClickListener = itemLongClickListener;
    }

    public List<SearchBookBean> getSearchBookBeans() {
        return allBookBeans;
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_source, parent, false));
    }

    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.tvBookSource.setText(allBookBeans.get(position).getOrigin());
        if (isEmpty(allBookBeans.get(position).getLastChapter())) {
            myViewHolder.tvLastChapter.setText(R.string.no_last_chapter);
        } else {
            myViewHolder.tvLastChapter.setText(allBookBeans.get(position).getLastChapter());
        }
        if (allBookBeans.get(position).getIsCurrentSource()) {
            myViewHolder.ivChecked.setVisibility(View.VISIBLE);
        } else {
            myViewHolder.ivChecked.setVisibility(View.INVISIBLE);
        }
        myViewHolder.llContent.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, position);
            }
        });
        myViewHolder.llContent.setOnLongClickListener(view -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(view, position);
            }
            return true;
        });
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return allBookBeans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llContent;
        TextView tvBookSource;
        TextView tvLastChapter;
        ImageView ivChecked;

        MyViewHolder(View itemView) {
            super(itemView);
            llContent = itemView.findViewById(R.id.ll_content);
            tvBookSource = itemView.findViewById(R.id.tv_source_name);
            tvLastChapter = itemView.findViewById(R.id.tv_lastChapter);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }
    }
}
