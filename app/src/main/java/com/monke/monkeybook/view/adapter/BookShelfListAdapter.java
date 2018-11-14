//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.view.adapter.base.OnItemClickListenerTwo;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BookShelfListAdapter extends RecyclerView.Adapter<BookShelfListAdapter.MyViewHolder> implements BookShelfAdapter {

    private Activity activity;
    private List<BookShelfBean> books;
    private OnItemClickListenerTwo itemClickListener;
    private String bookshelfPx;

    private MyItemTouchHelpCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new MyItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(books, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            notifyItemChanged(srcPosition);
            notifyItemChanged(targetPosition);
            return true;
        }
    };

    @Override
    public MyItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public BookShelfListAdapter(Activity activity) {
        this.activity = activity;
        books = new ArrayList<>();
    }

    @Override
    public void refreshBook(String noteUrl) {
        for (int i = 0; i < books.size(); i++) {
            if (Objects.equals(books.get(i).getNoteUrl(), noteUrl)) {
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public int getItemCount() {
        //如果不为0，按正常的流程跑
        return books.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookshelf_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int index) {
        if (!activity.isFinishing()) {
            if (TextUtils.isEmpty(books.get(index).getCustomCoverPath())) {
                Glide.with(activity).load(books.get(index).getBookInfoBean().getCoverUrl())
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .centerCrop().placeholder(R.drawable.img_cover_default))
                        .into(holder.ivCover);
            } else if (books.get(index).getCustomCoverPath().startsWith("http")) {
                Glide.with(activity).load(books.get(index).getCustomCoverPath())
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .centerCrop().placeholder(R.drawable.img_cover_default))
                        .into(holder.ivCover);
            } else {
                holder.ivCover.setImageBitmap(BitmapFactory.decodeFile(books.get(index).getCustomCoverPath()));
            }
        }
        holder.tvName.setText(books.get(index).getBookInfoBean().getName());
        holder.tvAuthor.setText(books.get(index).getBookInfoBean().getAuthor());
        holder.tvRead.setText(String.format("%s - %s", books.get(index).getDurChapterName(), BookshelfHelp.getReadProgress(books.get(index))));
        holder.tvLast.setText(books.get(index).getLastChapterName());
        if (books.get(index).getHasUpdate()) {
            holder.ivHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
        }
        holder.ivCover.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        holder.ivCover.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(v, index);
            }
            return true;
        });
        holder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        if (!Objects.equals(bookshelfPx, "2")) {
            holder.flContent.setOnLongClickListener(view -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(view, index);
                }
                return true;
            });
        } else {
            holder.ivCover.setOnClickListener(view -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(view, index);
                }
            });
        }
        if (Objects.equals(bookshelfPx, "2") && books.get(index).getSerialNumber() != index) {
            books.get(index).setSerialNumber(index);
            AsyncTask.execute(() -> DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(books.get(index)));
        }
        if (books.get(index).isLoading()) {
            holder.rotateLoading.setVisibility(View.VISIBLE);
            holder.rotateLoading.start();
        } else {
            holder.rotateLoading.setVisibility(View.INVISIBLE);
            holder.rotateLoading.stop();
        }
    }

    @Override
    public void setItemClickListener(OnItemClickListenerTwo itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public synchronized void replaceAll(List<BookShelfBean> newDataS, String bookshelfPx) {
        this.bookshelfPx = bookshelfPx;
        if (null != newDataS && newDataS.size() > 0) {
            BookshelfHelp.order(newDataS, bookshelfPx);
            books = newDataS;
        } else {
            books.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public List<BookShelfBean> getBooks() {
        return books;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        ImageView ivHasNew;
        TextView tvName;
        TextView tvAuthor;
        TextView tvRead;
        TextView tvLast;
        View ibContent;
        RotateLoading rotateLoading;

        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.cv_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivHasNew = itemView.findViewById(R.id.iv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRead = itemView.findViewById(R.id.tv_read);
            tvLast = itemView.findViewById(R.id.tv_last);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            ibContent = itemView.findViewById(R.id.ib_content);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
        }
    }

}
