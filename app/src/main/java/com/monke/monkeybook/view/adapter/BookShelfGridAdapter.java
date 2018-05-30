//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import me.grantland.widget.AutofitTextView;

public class BookShelfGridAdapter extends RefreshRecyclerViewAdapter {
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_EMPTY = 0;

    private final long DUR_ANIM_ITEM = 30;   //item动画启动间隔

    private List<BookShelfBean> books;

    private Boolean needAnim = true;
    private OnItemClickListener itemClickListener;
    private String bookshelfPx;
    private Activity activity;

    private MyItemTouchHelpCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new MyItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(books, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            int start = srcPosition;
            int end = targetPosition;
            if (start > end) {
                start = targetPosition;
                end = srcPosition;
            }
            notifyItemRangeChanged(start, end - start + 1);
            return true;
        }
    };

    public MyItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public BookShelfGridAdapter(Activity activity) {
        super(false);
        this.activity = activity;
        books = new ArrayList<>();
    }

    @Override
    public int getItemcount() {
        //如果不为0，按正常的流程跑
        return books.size();
    }

    @Override
    public int getItemViewtype(int position) {
        //如果有数据，则使用ITEM的布局
        return VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        return new OtherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_grid, parent, false));
    }

    @Override
    public void onBindViewholder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OtherViewHolder) {
            bindOtherViewHolder((OtherViewHolder) holder, position);
        }
    }

    private void bindOtherViewHolder(final OtherViewHolder holder, int index) {
        if (needAnim) {
            final Animation animation = AnimationUtils.loadAnimation(holder.flContent.getContext(), R.anim.anim_bookshelf_item);
            animation.setAnimationListener(new AnimationStartListener() {
                @Override
                void onAnimStart(Animation animation) {
                    needAnim = false;
                    holder.flContent.setVisibility(View.VISIBLE);
                }
            });
            new Handler().postDelayed(() -> holder.flContent.startAnimation(animation), index * DUR_ANIM_ITEM);
        } else {
            holder.flContent.setVisibility(View.VISIBLE);
        }
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(books.get(index).getBookInfoBean().getCoverUrl())
                    .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop().placeholder(R.drawable.img_cover_default))
                    .into(holder.ivCover);
        }
        holder.tvName.setText(books.get(index).getBookInfoBean().getName());
        holder.ibContent.setContentDescription(books.get(index).getBookInfoBean().getName());
        if (books.get(index).getHasUpdate()) {
            holder.ivHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
        }

        holder.ibContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(books.get(index), index);
        });
        holder.tvName.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(holder.ivCover, books.get(index), index);
            }
        });
        if (!Objects.equals(bookshelfPx, "2")) {
            holder.ibContent.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(holder.ivCover, books.get(index), index);
                }
                return true;
            });
        } else if (books.get(index).getSerialNumber() != index){
            books.get(index).setSerialNumber(index);
            new Thread(){
                public void run() {
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(books.get(index));
                }
            }.start();
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public synchronized void replaceAll(List<BookShelfBean> newDataS, String bookshelfPx) {
        this.bookshelfPx = bookshelfPx;
        books.clear();
        if (null != newDataS && newDataS.size() > 0) {
            books.addAll(newDataS);
        }
        BookshelfHelp.order(books, bookshelfPx);
        notifyDataSetChanged();
    }

    public List<BookShelfBean> getBooks() {
        return books;
    }

    class OtherViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        ImageView ivHasNew;
        AutofitTextView tvName;
        ImageButton ibContent;

        OtherViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivHasNew = itemView.findViewById(R.id.iv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            ibContent = itemView.findViewById(R.id.ib_content);

        }
    }

    abstract class AnimationStartListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            onAnimStart(animation);
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        abstract void onAnimStart(Animation animation);
    }
}