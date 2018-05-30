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
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import me.grantland.widget.AutofitTextView;

public class BookShelfListAdapter extends RefreshRecyclerViewAdapter {
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_EMPTY = 0;

    private final long DUR_ANIM_ITEM = 30;   //item动画启动间隔

    private Activity activity;
    private List<BookShelfBean> books;
    private Boolean needAnim = true;
    private OnItemClickListener itemClickListener;
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

    public BookShelfListAdapter(Activity activity) {
        super(false);
        this.activity = activity;
        books = new ArrayList<>();
    }

    @Override
    public int getItemcount() {
        //如果mData.size()为0的话，只引入一个布局，就是emptyView
        //那么，这个recyclerView的itemCount为1
        if (books.size() == 0) {
            return 1;
        }
        //如果不为0，按正常的流程跑
        return books.size();
    }

    @Override
    public int getItemViewtype(int position) {
        //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        if (books.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }
        //如果有数据，则使用ITEM的布局
        return VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_empty, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }
        return new OtherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_list, parent, false));
    }

    @Override
    public void onBindViewholder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OtherViewHolder) {
            bindOtherViewHolder((OtherViewHolder) holder, position);
        }
    }

    private void bindOtherViewHolder(final OtherViewHolder holder, final int index) {
        if (needAnim) {
            final Animation animation = AnimationUtils.loadAnimation(holder.flContent.getContext(), R.anim.anim_bookshelf_item);
            animation.setAnimationListener(new AnimatontStartListener() {
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
        holder.ibContent.setContentDescription(String.format("%s,最新章节:%s",
                books.get(index).getBookInfoBean().getName(),
                books.get(index).getLastChapterListBean().getDurChapterName()));
        if (!activity.isFinishing()) {
            Glide.with(activity).load(books.get(index).getBookInfoBean().getCoverUrl())
                    .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop().placeholder(R.drawable.img_cover_default))
                    .into(holder.ivCover);
        }
        holder.tvName.setText(books.get(index).getBookInfoBean().getName());
        if (null != books.get(index).getBookInfoBean() && null != books.get(index).getChapterList()
                && books.get(index).getChapterListSize() > books.get(index).getDurChapter()) {
            holder.tvRead.setText(String.format(holder.tvRead.getContext().getString(R.string.read_dur_progress),
                    books.get(index).getDurChapterListBean().getDurChapterName()));
            holder.tvLast.setText(String.format(holder.tvLast.getContext().getString(R.string.book_search_last),
                    books.get(index).getLastChapterListBean().getDurChapterName()));
            if (books.get(index).getHasUpdate()) {
                holder.ivHasNew.setVisibility(View.VISIBLE);
            } else {
                holder.ivHasNew.setVisibility(View.INVISIBLE);
            }
        }
        //进度条
        holder.llDurcursor.setVisibility(View.VISIBLE);
        holder.mpbDurprogress.setVisibility(View.VISIBLE);
        holder.mpbDurprogress.setMaxProgress(books.get(index).getChapterListSize());
        float speed = books.get(index).getChapterListSize() * 1.0f / 60;

        holder.mpbDurprogress.setSpeed(speed <= 0 ? 1 : speed);
        holder.mpbDurprogress.setProgressListener(new OnProgressListener() {
            @Override
            public void moveStartProgress(float dur) {

            }

            @Override
            public void durProgressChange(float dur) {
                float rate = dur / holder.mpbDurprogress.getMaxProgress();
                holder.llDurcursor.setPadding((int) (holder.mpbDurprogress.getMeasuredWidth() * rate), 0, 0, 0);
            }

            @Override
            public void moveStopProgress(float dur) {

            }

            @Override
            public void setDurProgress(float dur) {

            }
        });
        if (needAnim) {
            holder.mpbDurprogress.setDurProgressWithAnim(books.get(index).getDurChapter());
        } else {
            holder.mpbDurprogress.setDurProgress(books.get(index).getDurChapter());
        }
        holder.ibCover.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(books.get(index), index);
        });
        holder.ibCover.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(holder.ivCover, books.get(index), index);
            }
            return true;
        });
        holder.ibContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(books.get(index), index);
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

    public Boolean getNeedAnim() {
        return needAnim;
    }

    public void setNeedAnim(Boolean needAnim) {
        this.needAnim = needAnim;
    }

    public synchronized void replaceAll(List<BookShelfBean> newDataS, String bookshelfPx) {
        this.bookshelfPx = bookshelfPx;
        books.clear();
        if (null != newDataS && newDataS.size() > 0) {
            books.addAll(newDataS);
            BookshelfHelp.order(books, bookshelfPx);
        }
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
        AutofitTextView tvRead;
        AutofitTextView tvLast;
        LinearLayout llDurcursor;
        MHorProgressBar mpbDurprogress;
        ImageButton ibContent;
        ImageButton ibCover;

        OtherViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivHasNew = itemView.findViewById(R.id.iv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRead = itemView.findViewById(R.id.tv_read);
            tvLast = itemView.findViewById(R.id.tv_last);
            llDurcursor = itemView.findViewById(R.id.ll_durcursor);
            mpbDurprogress = itemView.findViewById(R.id.mpb_durprogress);
            ibContent = itemView.findViewById(R.id.ib_content);
            ibCover = itemView.findViewById(R.id.ib_cover);
        }
    }

    abstract class AnimatontStartListener implements Animation.AnimationListener {

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