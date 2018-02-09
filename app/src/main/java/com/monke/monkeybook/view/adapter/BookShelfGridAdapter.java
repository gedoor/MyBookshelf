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
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

public class BookShelfGridAdapter extends RefreshRecyclerViewAdapter {
    private final int TYPE_LAST = 1;
    private final int TYPE_OTHER = 2;

    private final long DUR_ANIM_ITEM = 30;   //item动画启动间隔

    private List<BookShelfBean> books;

    private Boolean needAnim = true;

    private OnItemClickListener itemClickListener;

    public BookShelfGridAdapter() {
        super(false);
        books = new ArrayList<>();
    }

    @Override
    public int getItemcount() {
        if (books.size() == 0) {
            return 1;
        } else {
            if (books.size() % 3 == 0) {
                return 1 + books.size() / 3;
            } else {
                return 1 + (books.size() / 3 + 1);
            }
        }
    }

    public int getRealItemCount() {
        return books.size();
    }

    @Override
    public int getItemViewtype(int position) {
        if (position == 0) {
            return TYPE_LAST;
        } else {
            return TYPE_OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LAST) {
            return new LastViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_grid_lastest, parent, false));
        } else {
            return new OtherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_grid_other, parent, false));
        }
    }

    @Override
    public void onBindViewholder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_LAST) {
            bindLastViewHolder((LastViewHolder) holder, position);
        } else {
            bindOtherViewHolder((OtherViewHolder) holder, position - 1);
        }
    }

    private void bindOtherViewHolder(final OtherViewHolder holder, int index) {
        //第一列
        final int index_1 = index * 3;
        if (needAnim) {
            final Animation animation = AnimationUtils.loadAnimation(holder.flContent_1.getContext(), R.anim.anim_bookshelf_item);
            animation.setAnimationListener(new AnimationStartListener() {
                @Override
                void onAnimStart(Animation animation) {
                    needAnim = false;
                    holder.flContent_1.setVisibility(View.VISIBLE);
                }
            });
            new Handler().postDelayed(() -> {
                holder.flContent_1.startAnimation(animation);
            }, index_1 * DUR_ANIM_ITEM);
        } else {
            holder.flContent_1.setVisibility(View.VISIBLE);
        }
        Glide.with(holder.ivCover_1.getContext())
                .load(books.get(index_1).getBookInfoBean().getCoverUrl())
                .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover_1);
        holder.tvName_1.setText(books.get(index_1).getBookInfoBean().getName());
        holder.ibContent_1.setContentDescription(books.get(index_1).getBookInfoBean().getName());
        if (books.get(index_1).getHasUpdate()) {
            holder.ivHasNew_1.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasNew_1.setVisibility(View.INVISIBLE);
        }

        holder.ibContent_1.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(books.get(index_1), index_1);
        });
        holder.ibContent_1.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(holder.ivCover_1, books.get(index_1), index_1);
                return true;
            } else
                return false;
        });
        //第二列
        final int index_2 = index_1 + 1;
        if (index_2 < books.size()) {
            if (needAnim) {
                final Animation animation = AnimationUtils.loadAnimation(holder.flContent_2.getContext(), R.anim.anim_bookshelf_item);
                animation.setAnimationListener(new AnimationStartListener() {
                    @Override
                    void onAnimStart(Animation animation) {
                        needAnim = false;
                        holder.flContent_2.setVisibility(View.VISIBLE);
                    }
                });
                new Handler().postDelayed(() -> {
                    if (null != holder)
                        holder.flContent_2.startAnimation(animation);
                }, index_2 * DUR_ANIM_ITEM);
            } else {
                holder.flContent_2.setVisibility(View.VISIBLE);
            }
            Glide.with(holder.ivCover_2.getContext())
                    .load(books.get(index_2).getBookInfoBean().getCoverUrl())
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover_2);
            holder.tvName_2.setText(books.get(index_2).getBookInfoBean().getName());
            holder.ibContent_2.setContentDescription(books.get(index_2).getBookInfoBean().getName());
            if (books.get(index_2).getHasUpdate()) {
                holder.ivHasNew_2.setVisibility(View.VISIBLE);
            } else {
                holder.ivHasNew_2.setVisibility(View.INVISIBLE);
            }
            holder.ibContent_2.setOnClickListener(v -> {
                if (itemClickListener != null)
                    itemClickListener.onClick(books.get(index_2), index_2);
            });
            holder.ibContent_2.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(holder.ivCover_2, books.get(index_2), index_2);
                    return true;
                } else
                    return false;
            });
            //第三列
            final int index_3 = index_2 + 1;
            if (index_3 < books.size()) {
                if (needAnim) {
                    final Animation animation = AnimationUtils.loadAnimation(holder.flContent_3.getContext(), R.anim.anim_bookshelf_item);
                    animation.setAnimationListener(new AnimationStartListener() {
                        @Override
                        void onAnimStart(Animation animation) {
                            needAnim = false;
                            holder.flContent_3.setVisibility(View.VISIBLE);
                        }
                    });
                    new Handler().postDelayed(() -> {
                        holder.flContent_3.startAnimation(animation);
                    }, index_3 * DUR_ANIM_ITEM);
                } else {
                    holder.flContent_3.setVisibility(View.VISIBLE);
                }
                Glide.with(holder.ivCover_3.getContext())
                        .load(books.get(index_3).getBookInfoBean().getCoverUrl())
                        .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover_3);
                holder.tvName_3.setText(books.get(index_3).getBookInfoBean().getName());
                holder.ibContent_3.setContentDescription(books.get(index_3).getBookInfoBean().getName());
                if (books.get(index_3).getHasUpdate()) {
                    holder.ivHasNew_3.setVisibility(View.VISIBLE);
                } else {
                    holder.ivHasNew_3.setVisibility(View.INVISIBLE);
                }
                holder.ibContent_3.setOnClickListener(v -> {
                    if (itemClickListener != null)
                        itemClickListener.onClick(books.get(index_3), index_3);
                });
                holder.ibContent_3.setOnLongClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onLongClick(holder.ivCover_3, books.get(index_3), index_3);
                        return true;
                    } else
                        return false;
                });
            }else{
                holder.flContent_3.setVisibility(View.INVISIBLE);
            }
        }else{
            holder.flContent_2.setVisibility(View.INVISIBLE);
            holder.flContent_3.setVisibility(View.INVISIBLE);
        }
    }
    //最近阅读
    private void bindLastViewHolder(final LastViewHolder holder, final int index) {
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
                    .load(books.get(index).getBookInfoBean().getCoverUrl())
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover);

            holder.flLastEstTip.setVisibility(View.VISIBLE);

            holder.tvName.setText(String.format(holder.tvName.getContext().getString(R.string.tv_book_name), books.get(index).getBookInfoBean().getName()));

            if (null != books.get(index).getBookInfoBean() && null != books.get(index).getChapterList()
                    && books.get(index).getChapterListSize() > books.get(index).getDurChapter()) {
                holder.tvDurProgress.setText(String.format(holder.tvDurProgress.getContext().getString(R.string.read_dur_progress),
                        books.get(index).getDurChapterListBean().getDurChapterName()));
            }
            holder.llDurCursor.setVisibility(View.VISIBLE);
            holder.mpbDurProgress.setVisibility(View.VISIBLE);
            holder.mpbDurProgress.setMaxProgress(books.get(index).getChapterListSize());
            float speed = books.get(index).getChapterListSize() * 1.0f / 100;

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
                holder.mpbDurProgress.setDurProgressWithAnim(books.get(index).getDurChapter());
            } else {
                holder.mpbDurProgress.setDurProgress(books.get(index).getDurChapter());
            }
            holder.tvWatch.setText("继续阅读");
            holder.tvWatch.setOnClickListener(v -> {
                if (null != itemClickListener) {
                    itemClickListener.onClick(books.get(index), index);
                }
            });
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

    public synchronized void replaceAll(List<BookShelfBean> newDatas) {
        books.clear();
        if (null != newDatas && newDatas.size() > 0) {
            books.addAll(newDatas);
        }
        order();

        notifyDataSetChanged();
    }

    private void order() {
        if (books != null && books.size() > 0) {
            for (int i = 0; i < books.size(); i++) {
                int temp = i;
                for (int j = i + 1; j < books.size(); j++) {
                    if (books.get(temp).getFinalDate() < books.get(j).getFinalDate()) {
                        temp = j;
                    }
                }
                BookShelfBean tempBookShelfBean = books.get(i);
                books.set(i, books.get(temp));
                books.set(temp, tempBookShelfBean);
            }
        }
    }

    public List<BookShelfBean> getBooks() {
        return books;
    }

    class LastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        FrameLayout flLastEstTip;
        AutofitTextView tvName;
        AutofitTextView tvDurProgress;
        LinearLayout llDurCursor;
        MHorProgressBar mpbDurProgress;
        TextView tvWatch;

        LastViewHolder(View itemView) {
            super(itemView);
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
        FrameLayout flContent_1;
        ImageView ivCover_1;
        ImageView ivHasNew_1;
        AutofitTextView tvName_1;
        ImageButton ibContent_1;

        FrameLayout flContent_2;
        ImageView ivCover_2;
        ImageView ivHasNew_2;
        AutofitTextView tvName_2;
        ImageButton ibContent_2;

        FrameLayout flContent_3;
        ImageView ivCover_3;
        ImageView ivHasNew_3;
        AutofitTextView tvName_3;
        ImageButton ibContent_3;

        OtherViewHolder(View itemView) {
            super(itemView);
            flContent_1 = itemView.findViewById(R.id.fl_content_1);
            ivCover_1 = itemView.findViewById(R.id.iv_cover_1);
            ivHasNew_1 = itemView.findViewById(R.id.iv_has_new_1);
            tvName_1 = itemView.findViewById(R.id.tv_name_1);
            ibContent_1 = itemView.findViewById(R.id.ib_content_1);

            flContent_2 = itemView.findViewById(R.id.fl_content_2);
            ivCover_2 = itemView.findViewById(R.id.iv_cover_2);
            ivHasNew_2 = itemView.findViewById(R.id.iv_has_new_2);
            tvName_2 = itemView.findViewById(R.id.tv_name_2);
            ibContent_2 = itemView.findViewById(R.id.ib_content_2);

            flContent_3 = itemView.findViewById(R.id.fl_content_3);
            ivCover_3 = itemView.findViewById(R.id.iv_cover_3);
            ivHasNew_3 = itemView.findViewById(R.id.iv_has_new_3);
            tvName_3 = itemView.findViewById(R.id.tv_name_3);
            ibContent_3 = itemView.findViewById(R.id.ib_content_3);
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