package com.kunfei.bookshelf.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookInfoBean;

import java.util.ArrayList;
import java.util.List;

public class SearchBookshelfAdapter extends RecyclerView.Adapter<SearchBookshelfAdapter.MyViewHolder> {

    private List<BookInfoBean> beans = new ArrayList<>();
    private CallBack callBack;

    public SearchBookshelfAdapter(CallBack callBack) {
        this.callBack = callBack;
    }

    public void setItems(List<BookInfoBean> beans) {
        this.beans.clear();
        this.beans.addAll(beans);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(beans.get(position).getName());
        holder.itemView.setOnClickListener(v -> callBack.openBookInfo(beans.get(position)));
    }

    @Override
    public int getItemCount() {
        return beans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv);
        }
    }

    public interface CallBack {
        void openBookInfo(BookInfoBean bookInfoBean);
    }
}

