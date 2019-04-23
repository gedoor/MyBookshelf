package com.kunfei.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

public class FindLeftAdapter extends RecyclerView.Adapter<FindLeftAdapter.MyViewHolder> {
    private Context context;
    private int showIndex = 0;
    private List<RecyclerViewData> data = new ArrayList<>();
    private OnClickListener onClickListener;

    public FindLeftAdapter(Context context, OnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
    }

    public void setData(List<RecyclerViewData> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void upShowIndex(int showIndex) {
        if (showIndex != this.showIndex) {
            int oldIndex = this.showIndex;
            this.showIndex = showIndex;
            notifyItemChanged(oldIndex);
            notifyItemChanged(this.showIndex);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_find_left, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") int i) {
        FindKindGroupBean groupBean = (FindKindGroupBean) data.get(i).getGroupData();
        myViewHolder.tvSourceName.setText(groupBean.getGroupName());
        if (i == showIndex) {
            myViewHolder.tvSourceName.setBackgroundColor(context.getResources().getColor(R.color.transparent30));
        } else {
            myViewHolder.tvSourceName.setBackgroundColor(Color.TRANSPARENT);
        }
        myViewHolder.tvSourceName.setOnClickListener(v -> {
            if (onClickListener != null) {
                int oldIndex = showIndex;
                showIndex = i;
                notifyItemChanged(oldIndex);
                notifyItemChanged(showIndex);
                onClickListener.click(showIndex);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSourceName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSourceName = itemView.findViewById(R.id.tv_source_name);
        }
    }

    public interface OnClickListener {
        void click(int pos);
    }

}
