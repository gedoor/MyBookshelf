package com.monke.monkeybook.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.view.activity.ChoiceBookActivity;
import com.monke.monkeybook.view.activity.LibraryActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class FindKindAdapter extends RecyclerView.Adapter<FindKindAdapter.MyViewHolder> {
    private List<FindKindBean> dataList;
    private LibraryActivity activity;

    public FindKindAdapter(LibraryActivity activity) {
        this.activity = activity;
        dataList = new ArrayList<>();
    }

    public void addDataS(List<FindKindBean> dataS) {
        this.dataList = dataS;
        notifyDataSetChanged();
    }

    public void resetDataS(List<FindKindBean> dataS) {
        this.dataList = dataS;
        notifyDataSetChanged();
    }

    public List<FindKindBean> getDataList() {
        return dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_find_kind, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvKindName.setText(dataList.get(position).getKindName());
        holder.tvKindName.setOnClickListener(view -> ChoiceBookActivity
                .startChoiceBookActivity(activity.getContext(),
                        dataList.get(position).getKindName(),
                        dataList.get(position).getKindUrl(),
                        dataList.get(position).getTag()));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvKindName;

        MyViewHolder(View itemView) {
            super(itemView);
            tvKindName = itemView.findViewById(R.id.tv_kind_name);

        }
    }

}
