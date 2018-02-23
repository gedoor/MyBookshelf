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
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.view.activity.BookSourceActivity;
import com.monke.monkeybook.view.activity.ReplaceRuleActivity;
import com.monke.monkeybook.view.activity.SourceEditActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ReplaceRuleAdapter extends RecyclerView.Adapter<ReplaceRuleAdapter.MyViewHolder> {
    private List<ReplaceRuleBean> dataList;
    private ReplaceRuleActivity activity;

    public ReplaceRuleAdapter(ReplaceRuleActivity activity) {
        this.activity = activity;
        dataList = new ArrayList<>();
    }

    public void addDataS(List<ReplaceRuleBean> dataList) {
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void resetDataS(List<ReplaceRuleBean> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public List<ReplaceRuleBean> getDataList() {
        return dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_replace_rule_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.checkBox.setText(dataList.get(position).getReplaceSummary());
        holder.checkBox.setChecked(dataList.get(position).getEnable());
        holder.checkBox.setOnClickListener((View view) -> dataList.get(position).setEnable(holder.checkBox.isChecked()));
        holder.editView.getDrawable().mutate();
        holder.editView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        holder.editView.setOnClickListener(view -> activity.editReplaceRule(dataList.get(position)));
        holder.delView.getDrawable().mutate();
        holder.delView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        holder.delView.setOnClickListener(view -> {
            activity.delData(dataList.get(position));
            dataList.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView editView;
        ImageView delView;

        MyViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cb_replace_rule);
            editView = itemView.findViewById(R.id.iv_edit);
            delView = itemView.findViewById(R.id.iv_del);
        }
    }
}
