package com.kunfei.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FindLeftAdapter extends RecyclerView.Adapter<FindLeftAdapter.MyViewHolder> {
    private Context context;
    private int showIndex = 0;
    private List<RecyclerViewData> datas = new ArrayList<>();
    private OnClickListener onClickListener;

    public FindLeftAdapter(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setDatas(List<RecyclerViewData> datas) {
        this.datas.clear();
        this.datas.addAll(datas);
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
        context = viewGroup.getContext();
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_find_left, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") int i) {
        FindKindGroupBean groupBean = (FindKindGroupBean) datas.get(i).getGroupData();
        myViewHolder.tvSourceName.setText(groupBean.getGroupName());
        if (i == showIndex) {
            myViewHolder.findLeft.setBackgroundColor(context.getResources().getColor(R.color.transparent30));
        } else {
            myViewHolder.findLeft.setBackgroundColor(Color.TRANSPARENT);
        }
        myViewHolder.findLeft.setOnClickListener(v -> {
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

    public interface OnClickListener {
        void click(int pos);
    }

}
