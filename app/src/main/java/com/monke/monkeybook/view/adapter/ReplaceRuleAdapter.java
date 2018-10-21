package com.monke.monkeybook.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.view.activity.ReplaceRuleActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ReplaceRuleAdapter extends RecyclerView.Adapter<ReplaceRuleAdapter.MyViewHolder> {
    private List<ReplaceRuleBean> dataList;
    private ReplaceRuleActivity activity;
    private MyItemTouchHelpCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new MyItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(dataList, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            notifyItemChanged(srcPosition);
            notifyItemChanged(targetPosition);
            activity.saveDataS();
            return true;
        }
    };

    public ReplaceRuleAdapter(ReplaceRuleActivity activity) {
        this.activity = activity;
        dataList = new ArrayList<>();
    }

    public MyItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public void resetDataS(List<ReplaceRuleBean> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
        activity.upDateSelectAll();
    }

    public List<ReplaceRuleBean> getDataList() {
        return dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_replace_rule, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.checkBox.setText(dataList.get(position).getReplaceSummary());
        holder.checkBox.setChecked(dataList.get(position).getEnable());
        holder.checkBox.setOnClickListener((View view) -> {
            dataList.get(position).setEnable(holder.checkBox.isChecked());
            activity.upDateSelectAll();
            activity.saveDataS();
        });
        holder.editView.setOnClickListener(view -> activity.editReplaceRule(dataList.get(position)));
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
