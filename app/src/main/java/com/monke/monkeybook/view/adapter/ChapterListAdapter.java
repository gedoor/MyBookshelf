//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.widget.ChapterListView;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ThisViewHolder> {
    private BookShelfBean bookShelfBean;
    private ChapterListView.OnItemClickListener itemClickListener;
    private int index = 0;
    private int tabPosition;

    public ChapterListAdapter(BookShelfBean bookShelfBean, @NonNull ChapterListView.OnItemClickListener itemClickListener) {
        this.bookShelfBean = bookShelfBean;
        this.itemClickListener = itemClickListener;
    }

    public void upChapterList(ChapterListBean chapterListBean) {
        if (bookShelfBean.getChapterListSize() > chapterListBean.getDurChapterIndex()) {
            bookShelfBean.getChapterList(chapterListBean.getDurChapterIndex()).setHasCache(chapterListBean.getHasCache());
            if (tabPosition == 0) {
                notifyItemChanged(chapterListBean.getDurChapterIndex());
            }
        }
    }

    public void tabChange(int tabPosition) {
        this.tabPosition = tabPosition;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThisViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chapterlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, final int position) {
        if (tabPosition == 0) {
            holder.tvName.setText(bookShelfBean.getChapterList(position).getDurChapterName());
            if (bookShelfBean.getChapterList(position).getHasCache()) {
                holder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                holder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
            holder.flContent.setOnClickListener(v -> {
                setIndex(position);
                itemClickListener.itemClick(position, tabPosition);
            });
            if (position == index) {
                holder.flContent.setBackgroundResource(R.color.btn_bg_press);
                holder.flContent.setClickable(false);
            } else {
                holder.flContent.setBackgroundResource(R.drawable.bg_ib_pre);
                holder.flContent.setClickable(true);
            }
        } else {
            holder.tvName.setText(bookShelfBean.getBookInfoBean().getBookmarkList().get(position).getContent());
            holder.flContent.setOnClickListener(v -> {
                itemClickListener.itemClick(bookShelfBean.getBookInfoBean().getBookmarkList().get(position).getChapterIndex(), tabPosition);
            });
            holder.flContent.setOnLongClickListener(view -> {
                itemClickListener.itemLongClick(position, tabPosition);
                return true;
            });
        }

    }

    @Override
    public int getItemCount() {
        if (bookShelfBean == null)
            return 0;
        else if (tabPosition == 0) {
            return bookShelfBean.getChapterListSize();
        } else {
            return bookShelfBean.getBookInfoBean().getBookmarkList().size();
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (tabPosition == 0) {
            notifyItemChanged(this.index);
            this.index = index;
            notifyItemChanged(this.index);
        }
    }

    class ThisViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout flContent;
        private TextView tvName;

        ThisViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}
