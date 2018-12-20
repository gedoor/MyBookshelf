package com.kunfei.bookshelf.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.FindKindGroupBean;

import java.util.ArrayList;
import java.util.List;

public class FindLeftAdapter extends RecyclerView.Adapter<FindLeftAdapter.MyViewHolder> {

    private List<FindKindGroupBean> datas = new ArrayList<>();

    public void setDatas(List<FindKindGroupBean> datas) {
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_find_left, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.tvSourceName.setText(datas.get(i).getGroupName());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        View findLeft;
        TextView tvSourceName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            findLeft = itemView.findViewById(R.id.find_left);
            tvSourceName = itemView.findViewById(R.id.tv_source_name);
        }
    }
}
