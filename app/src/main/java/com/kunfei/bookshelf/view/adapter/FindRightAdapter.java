package com.kunfei.bookshelf.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.view.activity.ChoiceBookActivity;
import com.kunfei.bookshelf.widget.recycler.expandable.OnRecyclerViewListener;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;
import com.kunfei.bookshelf.widget.recycler.sectioned.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class FindRightAdapter extends SectionedRecyclerViewAdapter<FindRightAdapter.HeaderHolder, FindRightAdapter.DescHolder, RecyclerView.ViewHolder> {
    private List<RecyclerViewData> data = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private OnRecyclerViewListener.OnItemLongClickListener onItemLongClickListener;

    public FindRightAdapter(Context context, OnRecyclerViewListener.OnItemLongClickListener onItemLongClickListener) {
        this.context = context;
        this.onItemLongClickListener = onItemLongClickListener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<RecyclerViewData> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    protected int getSectionCount() {
        return data.size();
    }

    @Override
    protected int getItemCountForSection(int section) {
        return data.get(section).getChildList().size();
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected HeaderHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        return new HeaderHolder(inflater.inflate(R.layout.item_find2_header_view, parent, false));
    }

    @Override
    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected DescHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new DescHolder(inflater.inflate(R.layout.item_find2_childer_view, parent, false));
    }

    @Override
    protected void onBindSectionHeaderViewHolder(HeaderHolder holder, int section) {
        RecyclerViewData recyclerViewData = data.get(section);
        holder.tv_source_name.setText(((FindKindGroupBean) recyclerViewData.getGroupData()).getGroupName());
        holder.tv_source_name.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onGroupItemLongClick(section, section, holder.tv_source_name);
            }
            return true;
        });
    }

    @Override
    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder holder, int section) {

    }

    @Override
    protected void onBindItemViewHolder(DescHolder holder, int section, int position) {
        try {
            FindKindBean kindBean = (FindKindBean) data.get(section).getChild(position);
            holder.tv_item.setHorizontallyScrolling(false);
            holder.tv_item.setText(kindBean.getKindName());
            holder.tv_item.setOnClickListener(view -> {
                Intent intent = new Intent(context, ChoiceBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("url", kindBean.getKindUrl());
                intent.putExtra("title", kindBean.getKindName());
                intent.putExtra("tag", kindBean.getTag());
                context.startActivity(intent);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RecyclerViewData> getData() {
        return data;
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tv_source_name;

        HeaderHolder(View itemView) {
            super(itemView);
            tv_source_name = itemView.findViewById(R.id.tv_source_name);
        }
    }

    class DescHolder extends RecyclerView.ViewHolder {
        TextView tv_item;

        DescHolder(View view) {
            super(view);
            tv_item = view.findViewById(R.id.tv_item);
        }
    }
}

