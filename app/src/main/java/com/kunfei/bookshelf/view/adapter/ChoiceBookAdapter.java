//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookKindBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.widget.CoverImageView;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private Activity activity;
    private List<SearchBookBean> searchBooks;
    private OnItemClickListener itemClickListener;

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
                    .apply(new RequestOptions()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop()
                            .placeholder(R.drawable.img_cover_default))
                    .into(myViewHolder.ivCover);
        }
        String title = searchBooks.get(position).getName();
        String author = searchBooks.get(position).getAuthor();
        if (author != null && author.trim().length() > 0)
            title = String.format("%s (%s)", title, author);
        myViewHolder.tvName.setText(title);
        BookKindBean bookKindBean = new BookKindBean(searchBooks.get(position).getKind());
        if (TextUtils.isEmpty(bookKindBean.getKind())) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(bookKindBean.getKind());
        }
        if (TextUtils.isEmpty(bookKindBean.getWordsS())) {
            myViewHolder.tvWords.setVisibility(View.GONE);
        } else {
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(bookKindBean.getWordsS());
        }
        if (TextUtils.isEmpty(bookKindBean.getState())) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(bookKindBean.getState());
        }

        if (searchBooks.get(position).getLastChapter() != null && searchBooks.get(position).getLastChapter().length() > 0)
            myViewHolder.tvLasted.setText(searchBooks.get(position).getLastChapter());
        else if (searchBooks.get(position).getDesc() != null && searchBooks.get(position).getDesc().length() > 0) {
            myViewHolder.tvLasted.setText(searchBooks.get(position).getDesc());
        } else
            myViewHolder.tvLasted.setText("");
        if (searchBooks.get(position).getOrigin() != null && searchBooks.get(position).getOrigin().length() > 0) {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(activity.getString(R.string.origin_format, searchBooks.get(position).getOrigin()));
        } else {
            myViewHolder.tvOrigin.setVisibility(View.GONE);
        }

        myViewHolder.tvAddShelf.setText("搜索");
        myViewHolder.tvAddShelf.setVisibility(View.VISIBLE);
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
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
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

    public List<SearchBookBean> getSearchBooks() {
        return searchBooks;
    }

    public interface OnItemClickListener {
        void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean);

        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        CoverImageView ivCover;
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
}
