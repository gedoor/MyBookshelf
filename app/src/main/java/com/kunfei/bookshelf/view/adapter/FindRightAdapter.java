package com.kunfei.bookshelf.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.view.activity.ChoiceBookActivity;
import com.kunfei.bookshelf.widget.recycler.expandable.OnRecyclerViewListener;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FindRightAdapter extends RecyclerView.Adapter<FindRightAdapter.MyViewHolder> {
    private List<RecyclerViewData> datas = new ArrayList<>();
    private Context context;
    private OnRecyclerViewListener.OnItemLongClickListener onItemLongClickListener;

    public FindRightAdapter(OnRecyclerViewListener.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setDatas(List<RecyclerViewData> datas) {
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_find_right, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int pos) {
        FindKindGroupBean groupBean = (FindKindGroupBean) datas.get(pos).getGroupData();
        myViewHolder.sourceName.setText(groupBean.getGroupName());
        myViewHolder.flexboxLayout.removeAllViews();
        TextView tagView;
        for (Object object : datas.get(pos).getChildList()) {
            FindKindBean kindBean = (FindKindBean) object;
            tagView = (TextView) LayoutInflater.from(context).inflate(R.layout.item_search_history, myViewHolder.flexboxLayout, false);
            tagView.setText(kindBean.getKindName());
            tagView.setOnClickListener(view -> {
                Intent intent = new Intent(context, ChoiceBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("url", kindBean.getKindUrl());
                intent.putExtra("title", kindBean.getKindName());
                intent.putExtra("tag", kindBean.getTag());
                context.startActivity(intent);
            });
            myViewHolder.flexboxLayout.addView(tagView);
        }
        myViewHolder.sourceName.setOnLongClickListener(v -> {
            BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(groupBean.getGroupTag());
            if (sourceBean != null && onItemLongClickListener != null) {
                onItemLongClickListener.onGroupItemLongClick(pos, pos, v);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public List<RecyclerViewData> getDatas() {
        return datas;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        View findRight;
        TextView sourceName;
        FlexboxLayout flexboxLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            findRight = itemView.findViewById(R.id.find_right);
            sourceName = itemView.findViewById(R.id.tv_source_name);
            flexboxLayout = itemView.findViewById(R.id.tfl_find_kind);
        }
    }
}

