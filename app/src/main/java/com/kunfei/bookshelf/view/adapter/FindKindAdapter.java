package com.kunfei.bookshelf.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.widget.recycler.expandable.BaseExpandAbleViewHolder;
import com.kunfei.bookshelf.widget.recycler.expandable.BaseExpandableRecyclerAdapter;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.GroupItem;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class FindKindAdapter extends BaseExpandableRecyclerAdapter<FindKindGroupBean, FindKindBean, FindKindAdapter.MyViewHolder> {

    public FindKindAdapter(Context ctx, List<RecyclerViewData> datas) {
        super(ctx, datas);
    }

    /**
     * return groupView
     */
    @Override
    public View getGroupView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_group, parent, false);
    }

    /**
     * return childView
     */
    @Override
    public View getChildView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_kind, parent, false);
    }

    /**
     * return <VH extends BaseViewHolder> instance
     */
    @Override
    public MyViewHolder createRealViewHolder(Context ctx, View view, int viewType) {
        return new MyViewHolder(ctx, view, viewType);
    }

    /**
     * onBind groupData to groupView
     */
    @Override
    public void onBindGroupHolder(MyViewHolder holder, int groupPos, int position, FindKindGroupBean groupData) {
        holder.textView.setText(groupData.getGroupName());
        GroupItem item = getAllDatas().get(groupPos).getGroupItem();
        if (item.isExpand()) {
            holder.imageView.setImageResource(R.drawable.ic_expand_less_24dp);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_expand_more_24dp);
        }
    }

    /**
     * onBind childData to childView
     */
    @Override
    public void onBindChildpHolder(MyViewHolder holder, int groupPos, int childPos, int position, FindKindBean childData) {
        holder.textView.setText(childData.getKindName());
    }

    public class MyViewHolder extends BaseExpandAbleViewHolder {
        TextView textView;
        AppCompatImageView imageView;

        public MyViewHolder(Context ctx, View itemView, int viewType) {
            super(ctx, itemView, viewType);
            textView = itemView.findViewById(R.id.tv_kind_name);
            if (viewType == VIEW_TYPE_PARENT) {
                imageView = itemView.findViewById(R.id.iv_group);
            }
        }

        /**
         * return ChildView root layout id
         */
        @Override
        public int getChildViewResId() {
            return R.id.ll_content;
        }

        /**
         * return GroupView root layout id
         */
        @Override
        public int getGroupViewResId() {
            return R.id.ll_content;
        }
    }
}
