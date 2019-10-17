package com.kunfei.bookshelf.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ChangeCoverAdapter extends RefreshRecyclerViewAdapter {
    private List<SearchBookBean> allBookBeans;
    private CallBack callBack;

    public ChangeCoverAdapter(Boolean needLoadMore) {
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

    public void removeData(SearchBookBean searchBookBean) {
        DbHelper.getDaoSession().getSearchBookBeanDao().delete(searchBookBean);
        allBookBeans.remove(searchBookBean);
        notifyDataSetChanged();
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public List<SearchBookBean> getSearchBookBeans() {
        return allBookBeans;
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_cover, parent, false));
    }

    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.bind(allBookBeans.get(position), callBack, holder);
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

        ImageView ivCover;
        TextView tvSourceName;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvSourceName = itemView.findViewById(R.id.tv_source_name);
        }

        public void bind(SearchBookBean searchBookBean, CallBack callBack, RecyclerView.ViewHolder holder) {
            Glide.with(holder.itemView.getContext()).load(searchBookBean.getCoverUrl()).into(ivCover);
            tvSourceName.setText(searchBookBean.getOrigin());
            ivCover.setOnClickListener(view -> {
                if (callBack != null) {
                    callBack.changeTo(searchBookBean);
                }
            });
        }
    }

    public interface CallBack {
        void changeTo(SearchBookBean searchBookBean);

        void showMenu(View view, SearchBookBean searchBookBean);
    }

}
