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

public class BookShelfListAdapter extends RefreshRecyclerViewAdapter {
    private final int TYPE_LASTEST = 1;
    private final int TYPE_OTHER = 2;

    private final long DURANIMITEM = 30;   //item动画启动间隔

    private List<BookShelfBean> books;

    private Boolean needAnim = true;

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void toSearch();

        void onClick(BookShelfBean bookShelfBean, int index);

        void onLongClick(View view, BookShelfBean bookShelfBean, int index);
    }

    public BookShelfListAdapter() {
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
            return TYPE_OTHER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LASTEST) {
            return new LastestViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_lastest, parent, false));
        } else {
            return new OtherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf, parent, false));
        }
    }

    @Override
    public void onBindViewholder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_LASTEST) {
            bindLastestViewHolder((LastestViewHolder) holder, position);
        } else {
            bindOtherViewHolder((OtherViewHolder) holder, position - 1);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != holder)
                        holder.flContent.startAnimation(animation);
                }
            }, index * DURANIMITEM);
        } else {
            holder.flContent.setVisibility(View.VISIBLE);
        }
        Glide.with(holder.ivCover.getContext()).load(books.get(index).getBookInfoBean().getCoverUrl()).dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover);
        holder.tvName.setText(books.get(index).getBookInfoBean().getName());

        holder.ibContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null)
                    itemClickListener.onClick(books.get(index), index);
            }
        });
        holder.ibContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(holder.ivCover, books.get(index), index);
                    return true;
                } else
                    return false;
            }
        });

    }

    private void bindLastestViewHolder(final LastestViewHolder holder, final int index) {
        if (books.size() == 0) {
            holder.tvWatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != itemClickListener) {
                        itemClickListener.toSearch();
                    }
                }
            });
            holder.ivCover.setImageResource(R.drawable.img_cover_default);
            holder.flLastestTip.setVisibility(View.INVISIBLE);
            holder.tvName.setText("最近阅读的书在这里");
            holder.tvDurprogress.setText("");
            holder.llDurcursor.setVisibility(View.INVISIBLE);
            holder.mpbDurprogress.setVisibility(View.INVISIBLE);
            holder.mpbDurprogress.setProgressListener(null);
            holder.tvWatch.setText("去选书");
        } else {
            Glide.with(holder.ivCover.getContext()).load(books.get(index).getBookInfoBean().getCoverUrl()).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover);

            holder.flLastestTip.setVisibility(View.VISIBLE);

            holder.tvName.setText(String.format(holder.tvName.getContext().getString(R.string.tv_book_name), books.get(index).getBookInfoBean().getName()));

            if (null != books.get(index).getBookInfoBean() && null != books.get(index).getBookInfoBean().getChapterlist() && books.get(index).getBookInfoBean().getChapterlist().size() > books.get(index).getDurChapter()) {
                holder.tvDurprogress.setText(String.format(holder.tvDurprogress.getContext().getString(R.string.tv_read_durprogress), books.get(index).getBookInfoBean().getChapterlist().get(books.get(index).getDurChapter()).getDurChapterName()));
            }
            holder.llDurcursor.setVisibility(View.VISIBLE);
            holder.mpbDurprogress.setVisibility(View.VISIBLE);
            holder.mpbDurprogress.setMaxProgress(books.get(index).getBookInfoBean().getChapterlist().size());
            float speed = books.get(index).getBookInfoBean().getChapterlist().size()*1.0f/100;

            holder.mpbDurprogress.setSpeed(speed<=0?1:speed);
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
            holder.tvWatch.setText("继续阅读");
            holder.tvWatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != itemClickListener) {
                        itemClickListener.onClick(books.get(index), index);
                    }
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

    class LastestViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        FrameLayout flLastestTip;
        AutofitTextView tvName;
        AutofitTextView tvDurprogress;
        LinearLayout llDurcursor;
        MHorProgressBar mpbDurprogress;
        TextView tvWatch;

        public LastestViewHolder(View itemView) {
            super(itemView);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            flLastestTip = (FrameLayout) itemView.findViewById(R.id.fl_lastest_tip);
            tvName = (AutofitTextView) itemView.findViewById(R.id.tv_name);
            tvDurprogress = (AutofitTextView) itemView.findViewById(R.id.tv_durprogress);
            llDurcursor = (LinearLayout) itemView.findViewById(R.id.ll_durcursor);
            mpbDurprogress = (MHorProgressBar) itemView.findViewById(R.id.mpb_durprogress);
            tvWatch = (TextView) itemView.findViewById(R.id.tv_watch);
        }
    }

    class OtherViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        AutofitTextView tvName;
        ImageButton ibContent;

        public OtherViewHolder(View itemView) {
            super(itemView);
            flContent = (FrameLayout) itemView.findViewById(R.id.fl_content_1);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover_1);
            tvName = (AutofitTextView) itemView.findViewById(R.id.tv_name_1);
            ibContent = (ImageButton) itemView.findViewById(R.id.ib_content_1);

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
}