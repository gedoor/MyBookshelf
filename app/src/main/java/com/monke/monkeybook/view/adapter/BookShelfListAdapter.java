//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.view.adapter.base.OnItemClickListenerTwo;
import com.monke.mprogressbar.MHorProgressBar;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import me.grantland.widget.AutofitTextView;

public class BookShelfListAdapter extends RecyclerView.Adapter<BookShelfListAdapter.MyViewHolder> {

    private Activity activity;
    private List<BookShelfBean> books;
    private Boolean needAnim;
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

    public MyItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public BookShelfListAdapter(Activity activity, boolean needAnim) {
        this.activity = activity;
        this.needAnim = needAnim;
        books = new ArrayList<>();
    }

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
        if (needAnim) {
            final Animation animation = AnimationUtils.loadAnimation(holder.flContent.getContext(), R.anim.anim_bookshelf_item);
            animation.setAnimationListener(new AnimationStartListener() {
                @Override
                void onAnimStart(Animation animation) {
                    needAnim = false;
                    holder.flContent.setVisibility(View.VISIBLE);
                }
            });
            long DUR_ANIM_ITEM = 30;
            new Handler().postDelayed(() -> holder.flContent.startAnimation(animation), index * DUR_ANIM_ITEM);
        } else {
            holder.flContent.setVisibility(View.VISIBLE);
        }
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
        if (!TextUtils.isEmpty(books.get(index).getBookInfoBean().getAuthor())) {
            holder.tvName.setText(String.format("%s (%s)", books.get(index).getBookInfoBean().getName(), books.get(index).getBookInfoBean().getAuthor()));
        } else {
            holder.tvName.setText(books.get(index).getBookInfoBean().getName());
        }
        holder.tvRead.setText(holder.tvRead.getContext().getString(R.string.read_dur_progress, books.get(index).getDurChapterName()));
        holder.tvLast.setText(holder.tvLast.getContext().getString(R.string.book_search_last, books.get(index).getLastChapterName()));
        if (books.get(index).getHasUpdate()) {
            holder.ivHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
        }
        //进度条
        holder.mpbDurProgress.setVisibility(View.VISIBLE);
        holder.mpbDurProgress.setMaxProgress(books.get(index).getChapterListSize());
        float speed = books.get(index).getChapterListSize() * 1.0f / 60;

        holder.mpbDurProgress.setSpeed(speed <= 0 ? 1 : speed);

        if (needAnim) {
            holder.mpbDurProgress.setDurProgressWithAnim(books.get(index).getDurChapter() + 1);
        } else {
            holder.mpbDurProgress.setDurProgress(books.get(index).getDurChapter() + 1);
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
            holder.flContent.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(v, index);
                }
                return true;
            });
        } else if (books.get(index).getSerialNumber() != index) {
            books.get(index).setSerialNumber(index);
            new Thread() {
                public void run() {
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(books.get(index));
                }
            }.start();
        }
        if (books.get(index).isLoading()) {
            holder.rotateLoading.setVisibility(View.VISIBLE);
            holder.rotateLoading.start();
        } else {
            holder.rotateLoading.setVisibility(View.INVISIBLE);
            holder.rotateLoading.stop();
        }
    }

    public void setItemClickListener(OnItemClickListenerTwo itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

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

    public List<BookShelfBean> getBooks() {
        return books;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout flContent;
        ImageView ivCover;
        ImageView ivHasNew;
        AutofitTextView tvName;
        AutofitTextView tvRead;
        AutofitTextView tvLast;
        View ibContent;
        RotateLoading rotateLoading;
        MHorProgressBar mpbDurProgress;

        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.cv_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivHasNew = itemView.findViewById(R.id.iv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRead = itemView.findViewById(R.id.tv_read);
            tvLast = itemView.findViewById(R.id.tv_last);
            ibContent = itemView.findViewById(R.id.ib_content);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            mpbDurProgress = itemView.findViewById(R.id.mpb_durProgress);
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
