//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.widget.ChapterListView;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ThisViewHolder> {
    private BookShelfBean bookShelfBean;
    private ChapterListView.OnItemClickListener itemClickListener;
    private int index = 0;
    private Boolean isAsc = true;

    public ChapterListAdapter(BookShelfBean bookShelfBean, @NonNull ChapterListView.OnItemClickListener itemClickListener) {
        this.bookShelfBean = bookShelfBean;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ThisViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThisViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_adapter_chapterlist, parent, false));
    }

    @Override
    public void onBindViewHolder(ThisViewHolder holder, final int posiTion) {
        if (posiTion == getItemCount() - 1) {
            holder.vLine.setVisibility(View.INVISIBLE);
        } else
            holder.vLine.setVisibility(View.VISIBLE);

        final int position;
        if (isAsc) {
            position = posiTion;
        } else {
            position = getItemCount() - 1 - posiTion;
        }
        holder.tvName.setText(bookShelfBean.getChapterList(position).getDurChapterName());
        holder.flContent.setOnClickListener(v -> {
            setIndex(position);
            itemClickListener.itemClick(position);
        });
        if (position == index) {
            holder.flContent.setBackgroundResource(R.color.tv_text_addshelf_deep_pre);
            holder.flContent.setClickable(false);
        } else {
            holder.flContent.setBackgroundResource(R.drawable.bg_ib_pre2);
            holder.flContent.setClickable(true);
        }
    }

    @Override
    public int getItemCount() {
        if (bookShelfBean == null)
            return 0;
        else
            return bookShelfBean.getChapterListSize();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        notifyItemChanged(this.index);
        this.index = index;
        notifyItemChanged(this.index);
    }

    class ThisViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout flContent;
        private TextView tvName;
        private View vLine;

        ThisViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            tvName = itemView.findViewById(R.id.tv_name);
            vLine = itemView.findViewById(R.id.v_line);
        }
    }
}
