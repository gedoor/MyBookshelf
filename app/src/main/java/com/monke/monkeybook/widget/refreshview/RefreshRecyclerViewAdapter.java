package com.monke.monkeybook.widget.refreshview;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;

public abstract class RefreshRecyclerViewAdapter extends RecyclerView.Adapter {
    private final int LOADMORETYPE = 2001;

    private Handler handler;
    private int isRequesting = 0;   //0是未执行网络请求  1是正在下拉刷新  2是正在加载更多
    private Boolean needLoadMore = false;
    private Boolean isAll = false;  //判断是否还有更多
    private Boolean loadMoreError = false;

    private OnClickTryAgainListener clickTryAgainListener;

    public interface OnClickTryAgainListener {
        public void loadMoreErrorTryAgain();
    }

    public interface OnItemClickListener {
        void toSearch();

        void onClick(BookShelfBean bookShelfBean, int index);

        void onLongClick(View view, BookShelfBean bookShelfBean, int index);
    }

    public RefreshRecyclerViewAdapter(Boolean needLoadMore) {
        this.needLoadMore = needLoadMore;
        handler = new Handler();
    }

    public int getIsRequesting() {
        return isRequesting;
    }

    public void setIsRequesting(int isRequesting, Boolean needNoti) {
        this.isRequesting = isRequesting;
        if (this.isRequesting == 1) {
            isAll = false;
        }
        if (needNoti) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                notifyItemRangeChanged(getItemCount(), getItemCount() - getItemcount());
            } else {
                handler.post(() -> notifyDataSetChanged());
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOADMORETYPE) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_refresh_loadmore, parent, false));
        } else
            return onCreateViewholder(parent, viewType);
    }

    public abstract RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == LOADMORETYPE) {
            if (!loadMoreError) {
                ((LoadMoreViewHolder) holder).tvLoadMore.setText("正在加载...");
            } else {
                ((LoadMoreViewHolder) holder).tvLoadMore.setText("加载失败,点击重试");
            }
            ((LoadMoreViewHolder) holder).tvLoadMore.setOnClickListener(v -> {
                if (null != clickTryAgainListener && loadMoreError) {
                    clickTryAgainListener.loadMoreErrorTryAgain();
                    loadMoreError = false;
                    ((LoadMoreViewHolder) holder).tvLoadMore.setText("正在加载...");
                }
            });
        } else
            onBindViewholder(holder, position);
    }

    public abstract void onBindViewholder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemViewType(int position) {
        if (needLoadMore && isRequesting != 1 && !isAll && position == getItemCount() - 1 && getItemcount() > 0) {
            return LOADMORETYPE;
        } else {
            return getItemViewtype(position);
        }
    }

    public abstract int getItemViewtype(int position);

    @Override
    public int getItemCount() {
        if (needLoadMore && isRequesting != 1 && !isAll && getItemcount() > 0) {
            return getItemcount() + 1;
        } else
            return getItemcount();
    }

    public abstract int getItemcount();

    public void setIsAll(Boolean isAll, Boolean needNoti) {
        this.isAll = isAll;
        if (needNoti) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
//                notifyItemRangeChanged(getItemCount(),getItemCount()-getItemcount());
                if (getItemCount() > getItemcount()) {
                    notifyItemRangeChanged(getItemCount(), getItemCount() - getItemcount());
                } else
                    notifyItemRemoved(getItemCount() + 1);
            } else {
                handler.post(() -> notifyDataSetChanged());
            }
        }
    }

    class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        FrameLayout llLoadMore;
        TextView tvLoadMore;

        public LoadMoreViewHolder(View itemView) {
            super(itemView);
            llLoadMore = (FrameLayout) itemView.findViewById(R.id.ll_loadmore);
            tvLoadMore = (TextView) itemView.findViewById(R.id.tv_loadmore);
        }
    }

    public Boolean canLoadMore() {
        return needLoadMore && isRequesting == 0 && !isAll && getItemcount() > 0;
    }

    public OnClickTryAgainListener getClickTryAgainListener() {
        return clickTryAgainListener;
    }

    public void setClickTryAgainListener(OnClickTryAgainListener clickTryAgainListener) {
        this.clickTryAgainListener = clickTryAgainListener;

    }

    public Boolean getLoadMoreError() {
        return loadMoreError;
    }

    public void setLoadMoreError(Boolean loadMoreError, Boolean needNoti) {
        this.isRequesting = 0;
        this.loadMoreError = loadMoreError;
        if (needNoti) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                notifyDataSetChanged();
            } else {
                handler.post(() -> notifyDataSetChanged());
            }
        }
    }
}
