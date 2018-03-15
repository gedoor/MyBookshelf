//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.util.ArrayList;
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
    private LastViewHolder lastViewHolder;
    private String bookshelfPx;

    public BookShelfGridAdapter() {
        super(false);
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
        Glide.with(holder.ivCover.getContext())
                .load(books.get(index).getBookInfoBean().getCoverUrl())
                .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover);

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
                    return true;
                } else
                    return false;
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

    public View getHeaderView(LinearLayout parent) {
        View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_bookshelf_lastest, parent, false);
        lastViewHolder = new LastViewHolder(headerView);
        return headerView;
    }

    //最近阅读
    private void bindLastViewHolder(final LastViewHolder holder, BookShelfBean bookShelfBean) {
        if (books.size() == 0) {
            holder.tvWatch.setOnClickListener(v -> {
                if (null != itemClickListener) {
                    itemClickListener.toSearch();
                }
            });
            holder.ivCover.setImageResource(R.drawable.img_cover_default);
            holder.flLastEstTip.setVisibility(View.INVISIBLE);
            holder.tvName.setText("最近阅读的书在这里");
            holder.tvDurProgress.setText("");
            holder.llDurCursor.setVisibility(View.INVISIBLE);
            holder.mpbDurProgress.setVisibility(View.INVISIBLE);
            holder.mpbDurProgress.setProgressListener(null);
            holder.tvWatch.setText("去选书");
        } else {
            Glide.with(holder.ivCover.getContext())
                    .load(bookShelfBean.getBookInfoBean().getCoverUrl())
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover);

            holder.flLastEstTip.setVisibility(View.VISIBLE);

            holder.tvName.setText(String.format(holder.tvName.getContext().getString(R.string.tv_book_name), bookShelfBean.getBookInfoBean().getName()));

            if (null != bookShelfBean.getBookInfoBean() && null != bookShelfBean.getChapterList()
                    && bookShelfBean.getChapterListSize() > bookShelfBean.getDurChapter()) {
                holder.tvDurProgress.setText(String.format(holder.tvDurProgress.getContext().getString(R.string.read_dur_progress),
                        bookShelfBean.getDurChapterListBean().getDurChapterName()));
            }
            holder.llDurCursor.setVisibility(View.VISIBLE);
            holder.mpbDurProgress.setVisibility(View.VISIBLE);
            holder.mpbDurProgress.setMaxProgress(bookShelfBean.getChapterListSize());
            float speed = bookShelfBean.getChapterListSize() * 1.0f / 100;

            holder.mpbDurProgress.setSpeed(speed <= 0 ? 1 : speed);
            holder.mpbDurProgress.setProgressListener(new OnProgressListener() {
                @Override
                public void moveStartProgress(float dur) {

                }

                @Override
                public void durProgressChange(float dur) {
                    float rate = dur / holder.mpbDurProgress.getMaxProgress();
                    holder.llDurCursor.setPadding((int) (holder.mpbDurProgress.getMeasuredWidth() * rate), 0, 0, 0);
                }

                @Override
                public void moveStopProgress(float dur) {

                }

                @Override
                public void setDurProgress(float dur) {

                }
            });
            if (needAnim) {
                holder.mpbDurProgress.setDurProgressWithAnim(bookShelfBean.getDurChapter());
            } else {
                holder.mpbDurProgress.setDurProgress(bookShelfBean.getDurChapter());
            }
            holder.tvWatch.setText("继续阅读");
            holder.tvWatch.setOnClickListener(v -> {
                if (null != itemClickListener) {
                    itemClickListener.onClick(bookShelfBean, 0);
                }
            });
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
            bindLastViewHolder(lastViewHolder, books.get(0));
        } else {
            bindLastViewHolder(lastViewHolder, null);
        }
        BookshelfHelp.order(books, bookshelfPx);
        notifyDataSetChanged();
    }

    public List<BookShelfBean> getBooks() {
        return books;
    }

    class LastViewHolder {
        ImageView ivCover;
        FrameLayout flLastEstTip;
        AutofitTextView tvName;
        AutofitTextView tvDurProgress;
        LinearLayout llDurCursor;
        MHorProgressBar mpbDurProgress;
        TextView tvWatch;

        LastViewHolder(View itemView) {
            ivCover = itemView.findViewById(R.id.iv_cover);
            flLastEstTip = itemView.findViewById(R.id.fl_lastest_tip);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDurProgress = itemView.findViewById(R.id.tv_durprogress);
            llDurCursor = itemView.findViewById(R.id.ll_durcursor);
            mpbDurProgress = itemView.findViewById(R.id.mpb_durprogress);
            tvWatch = itemView.findViewById(R.id.tv_watch);
        }
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