//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private Activity activity;
    private List<SearchBookBean> searchBooks;

    public interface OnItemClickListener {
        void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean);

        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    private OnItemClickListener itemClickListener;

    public ChoiceBookAdapter(Activity activity) {
        super(true);
        this.activity = activity;
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_searchbook_item, parent, false));
    }

    @Override
    public void onBindViewholder(final RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(searchBooks.get(position).getCoverUrl())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .fitCenter().dontAnimate()
                            .placeholder(R.drawable.img_cover_default))
                    .into(myViewHolder.ivCover);
        }
        myViewHolder.tvName.setText(String.format("%s(%s)", searchBooks.get(position).getName(), searchBooks.get(position).getAuthor()));
        String state = searchBooks.get(position).getState();
        if (state == null || state.length() == 0) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(state);
        }
        long words = searchBooks.get(position).getWords();
        if (words <= 0) {
            myViewHolder.tvWords.setVisibility(View.GONE);
        } else {
            String wordsS = Long.toString(words) + "字";
            if (words > 10000) {
                DecimalFormat df = new DecimalFormat("#.#");
                wordsS = df.format(words * 1.0f / 10000f) + "万字";
            }
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(wordsS);
        }
        String kind = searchBooks.get(position).getKind();
        if (kind == null || kind.length() <= 0) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(kind);
        }
        if (searchBooks.get(position).getLastChapter() != null && searchBooks.get(position).getLastChapter().length() > 0)
            myViewHolder.tvLasted.setText(searchBooks.get(position).getLastChapter());
        else if (searchBooks.get(position).getDesc() != null && searchBooks.get(position).getDesc().length() > 0) {
            myViewHolder.tvLasted.setText(searchBooks.get(position).getDesc());
        } else
            myViewHolder.tvLasted.setText("");
        if (searchBooks.get(position).getOrigin() != null && searchBooks.get(position).getOrigin().length() > 0) {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(String.format("来源:%s", searchBooks.get(position).getOrigin()));
        } else {
            myViewHolder.tvOrigin.setVisibility(View.GONE);
        }

        myViewHolder.tvAddShelf.setText("搜索");
        myViewHolder.tvAddShelf.setEnabled(true);

        myViewHolder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(myViewHolder.ivCover, position, searchBooks.get(position));
        });
        myViewHolder.tvAddShelf.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickAddShelf(myViewHolder.tvAddShelf, position, searchBooks.get(position));
        });
    }

    @Override
    public int getItemViewtype(int position) {
        return 0;
    }

    @Override
    public int getItemcount() {
        return searchBooks.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        TextView tvName;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvAddShelf;
        TextView tvOrigin;

        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLasted = itemView.findViewById(R.id.tv_lasted);
            tvAddShelf = itemView.findViewById(R.id.tv_add_shelf);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addAll(List<SearchBookBean> newData) {
        if (newData != null && newData.size() > 0) {
            int position = getItemcount();
            if (newData.size() > 0) {
                searchBooks.addAll(newData);
            }
            notifyItemInserted(position);
            notifyItemRangeChanged(position, newData.size());
        }
    }

    public void replaceAll(List<SearchBookBean> newData) {
        searchBooks.clear();
        if (newData != null && newData.size() > 0) {
            searchBooks.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public List<SearchBookBean> getSearchBooks() {
        return searchBooks;
    }
}