//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookKindBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.widget.CoverImageView;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.kunfei.bookshelf.utils.StringUtils.isTrimEmpty;

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private Activity activity;
    private List<SearchBookBean> searchBooks;
    private Callback callback;

    public ChoiceBookAdapter(Activity activity) {
        super(true);
        this.activity = activity;
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_book, parent, false));
    }

    @Override
    public void onBindIViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(searchBooks.get(position).getCoverUrl())
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .placeholder(R.drawable.img_cover_default)
                    .into(myViewHolder.ivCover);
        }
        String title = searchBooks.get(position).getName();
        String author = searchBooks.get(position).getAuthor();
        if (author != null && author.trim().length() > 0)
            title = String.format("%s (%s)", title, author);
        myViewHolder.tvName.setText(title);
        BookKindBean bookKindBean = new BookKindBean(searchBooks.get(position).getKind());
        if (isTrimEmpty(bookKindBean.getKind())) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(bookKindBean.getKind());
        }
        if (isTrimEmpty(bookKindBean.getWordsS())) {
            myViewHolder.tvWords.setVisibility(View.GONE);
        } else {
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(bookKindBean.getWordsS());
        }
        if (isTrimEmpty(bookKindBean.getState())) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(bookKindBean.getState());
        }
        //来源
        if (isTrimEmpty(searchBooks.get(position).getOrigin())) {
            myViewHolder.tvOrigin.setVisibility(View.GONE);
        } else {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(activity.getString(R.string.origin_format, searchBooks.get(position).getOrigin()));
        }
        //最新章节
        if (isTrimEmpty(searchBooks.get(position).getLastChapter())) {
            myViewHolder.tvLasted.setVisibility(View.GONE);
        } else {
            myViewHolder.tvLasted.setText(searchBooks.get(position).getLastChapter());
            myViewHolder.tvLasted.setVisibility(View.VISIBLE);
        }
        //简介
        if (isTrimEmpty(searchBooks.get(position).getIntroduce())) {
            myViewHolder.tvIntroduce.setVisibility(View.GONE);
        } else {
            myViewHolder.tvIntroduce.setText(StringUtils.formatHtml(searchBooks.get(position).getIntroduce()));
            myViewHolder.tvIntroduce.setVisibility(View.VISIBLE);
        }

        myViewHolder.flContent.setOnClickListener(v -> {
            if (callback != null)
                callback.clickItem(myViewHolder.ivCover, position, searchBooks.get(position));
        });
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void addAll(List<SearchBookBean> newData) {
        if (newData != null && newData.size() > 0) {
            int position = getICount();
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

    public interface Callback {
        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ViewGroup flContent;
        CoverImageView ivCover;
        TextView tvName;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvOrigin;
        TextView tvIntroduce;

        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLasted = itemView.findViewById(R.id.tv_lasted);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            tvIntroduce = itemView.findViewById(R.id.tv_introduce);
        }
    }
}
