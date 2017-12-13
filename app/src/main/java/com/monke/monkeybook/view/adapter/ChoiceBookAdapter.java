//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private List<SearchBookBean> searchBooks;

    public interface OnItemClickListener {
        void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean);

        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    private OnItemClickListener itemClickListener;

    public ChoiceBookAdapter() {
        super(true);
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_searchbook_item, parent, false));
    }

    @Override
    public void onBindViewholder(final RecyclerView.ViewHolder holder, final int position) {
        final int realposition = position;
        Glide.with(((Viewholder) holder).ivCover.getContext())
                .load(searchBooks.get(realposition).getCoverUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .fitCenter()
                .dontAnimate()
                .placeholder(R.drawable.img_cover_default)
                .into(((Viewholder) holder).ivCover);
        ((Viewholder) holder).tvName.setText(searchBooks.get(realposition).getName());
        ((Viewholder) holder).tvAuthor.setText(searchBooks.get(realposition).getAuthor());
        String state = searchBooks.get(position).getState();
        if (state == null || state.length() == 0) {
            ((Viewholder) holder).tvState.setVisibility(View.GONE);
        } else {
            ((Viewholder) holder).tvState.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvState.setText(state);
        }
        long words = searchBooks.get(realposition).getWords();
        if (words <= 0) {
            ((Viewholder) holder).tvWords.setVisibility(View.GONE);
        } else {
            String wordsS = Long.toString(words) + "字";
            if (words > 10000) {
                DecimalFormat df = new DecimalFormat("#.#");
                wordsS = df.format(words * 1.0f / 10000f) + "万字";
            }
            ((Viewholder) holder).tvWords.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvWords.setText(wordsS);
        }
        String kind = searchBooks.get(realposition).getKind();
        if (kind == null || kind.length() <= 0) {
            ((Viewholder) holder).tvKind.setVisibility(View.GONE);
        } else {
            ((Viewholder) holder).tvKind.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvKind.setText(kind);
        }
        if (searchBooks.get(realposition).getLastChapter() != null && searchBooks.get(realposition).getLastChapter().length() > 0)
            ((Viewholder) holder).tvLastest.setText(searchBooks.get(realposition).getLastChapter());
        else if (searchBooks.get(realposition).getDesc() != null && searchBooks.get(realposition).getDesc().length() > 0) {
            ((Viewholder) holder).tvLastest.setText(searchBooks.get(realposition).getDesc());
        } else
            ((Viewholder) holder).tvLastest.setText("");
        if (searchBooks.get(realposition).getOrigin() != null && searchBooks.get(realposition).getOrigin().length() > 0) {
            ((Viewholder) holder).tvOrigin.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvOrigin.setText("来源:" + searchBooks.get(realposition).getOrigin());
        } else {
            ((Viewholder) holder).tvOrigin.setVisibility(View.GONE);
        }
        if (searchBooks.get(realposition).getAdd()) {
            ((Viewholder) holder).tvAddShelf.setText("已添加");
            ((Viewholder) holder).tvAddShelf.setEnabled(false);
        } else {
            ((Viewholder) holder).tvAddShelf.setText("+添加");
            ((Viewholder) holder).tvAddShelf.setEnabled(true);
        }

        ((Viewholder) holder).flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(((Viewholder) holder).ivCover, realposition, searchBooks.get(realposition));
        });
        ((Viewholder) holder).tvAddShelf.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickAddShelf(((Viewholder) holder).tvAddShelf, realposition, searchBooks.get(realposition));
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

    class Viewholder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        TextView tvName;
        TextView tvAuthor;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLastest;
        TextView tvAddShelf;
        TextView tvOrigin;

        public Viewholder(View itemView) {
            super(itemView);
            flContent = (FrameLayout) itemView.findViewById(R.id.fl_content);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            tvState = (TextView) itemView.findViewById(R.id.tv_state);
            tvWords = (TextView) itemView.findViewById(R.id.tv_words);
            tvLastest = (TextView) itemView.findViewById(R.id.tv_lastest);
            tvAddShelf = (TextView) itemView.findViewById(R.id.tv_addshelf);
            tvKind = (TextView) itemView.findViewById(R.id.tv_kind);
            tvOrigin = (TextView) itemView.findViewById(R.id.tv_origin);
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addAll(List<SearchBookBean> newData) {
        if (newData != null && newData.size() > 0) {
            int position = getItemcount();
            if (newData != null && newData.size() > 0) {
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