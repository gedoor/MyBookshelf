//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ThisViewHolder> {

    private BookShelfBean bookShelfBean;
    private OnItemClickListener itemClickListener;
    private List<BookmarkBean> bookmarkBeans = new ArrayList<>();
    private boolean isSearch = false;

    public BookmarkAdapter(BookShelfBean bookShelfBean, @NonNull OnItemClickListener itemClickListener) {
        this.bookShelfBean = bookShelfBean;
        this.itemClickListener = itemClickListener;
    }

    public void search(final String key) {
        bookmarkBeans.clear();
        if (Objects.equals(key, "")) {
            isSearch = false;
            notifyDataSetChanged();
        } else {
            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                for (BookmarkBean bookmarkBean : bookShelfBean.getBookInfoBean().getBookmarkList()) {
                    if (bookmarkBean.getChapterName().contains(key)) {
                        bookmarkBeans.add(bookmarkBean);
                    } else if (bookmarkBean.getContent().contains(key)) {
                        bookmarkBeans.add(bookmarkBean);
                    }
                }
                emitter.onNext(true);
                emitter.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            isSearch = true;
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    @NonNull
    @Override
    public ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThisViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, final int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        int realPosition = holder.getLayoutPosition();
        if (realPosition == getItemCount() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }

        BookmarkBean bookmarkBean = isSearch ? bookmarkBeans.get(realPosition) : bookShelfBean.getBookmark(realPosition);
        holder.tvName.setText(StringUtils.isTrimEmpty(bookmarkBean.getContent()) ? bookmarkBean.getChapterName() : bookmarkBean.getContent());
        holder.llName.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.itemClick(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex());
            }
        });
        holder.llName.setOnLongClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.itemLongClick(bookmarkBean);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        if (bookShelfBean == null)
            return 0;
        else {
            if (isSearch) {
                return bookmarkBeans.size();
            }
            return bookShelfBean.getBookmarkListSize();
        }
    }

    static class ThisViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private View line;
        private View llName;

        ThisViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            line = itemView.findViewById(R.id.v_line);
            llName = itemView.findViewById(R.id.ll_name);
        }
    }

    public interface OnItemClickListener {
        void itemClick(int index, int page);

        void itemLongClick(BookmarkBean bookmarkBean);
    }
}
