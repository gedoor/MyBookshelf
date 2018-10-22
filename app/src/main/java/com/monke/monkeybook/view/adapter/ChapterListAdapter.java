//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.widget.ChapterListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ThisViewHolder> {
    private BookShelfBean bookShelfBean;
    private ChapterListView.OnItemClickListener itemClickListener;
    private List<ChapterListBean> chapterListBeans = new ArrayList<>();
    private List<BookmarkBean> bookmarkBeans = new ArrayList<>();
    private int index = 0;
    private int tabPosition;
    private Boolean isSearch = false;

    public ChapterListAdapter(BookShelfBean bookShelfBean, @NonNull ChapterListView.OnItemClickListener itemClickListener) {
        this.bookShelfBean = bookShelfBean;
        this.itemClickListener = itemClickListener;
    }

    public void upChapterList(ChapterListBean chapterListBean) {
        if (bookShelfBean.getChapterListSize() > chapterListBean.getDurChapterIndex()) {
            if (tabPosition == 0 && !isSearch) {
                notifyItemChanged(chapterListBean.getDurChapterIndex());
            }
        }
    }

    public void tabChange(int tabPosition) {
        this.tabPosition = tabPosition;
        notifyDataSetChanged();
    }

    public void search(final String key) {
        chapterListBeans.clear();
        bookmarkBeans.clear();
        if (Objects.equals(key, "")) {
            isSearch = false;
            notifyDataSetChanged();
        } else {
            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                if (tabPosition == 0) {
                    for (ChapterListBean chapterListBean : bookShelfBean.getChapterList()) {
                        if (chapterListBean.getDurChapterName().contains(key)) {
                            chapterListBeans.add(chapterListBean);
                        }
                    }
                } else {
                    for (BookmarkBean bookmarkBean : bookShelfBean.getBookInfoBean().getBookmarkList()) {
                        if (bookmarkBean.getChapterName().contains(key)) {
                            bookmarkBeans.add(bookmarkBean);
                        } else if (bookmarkBean.getContent().contains(key)) {
                            bookmarkBeans.add(bookmarkBean);
                        }
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
        if (tabPosition == 0) {
            ChapterListBean chapterListBean = isSearch ? chapterListBeans.get(position) : bookShelfBean.getChapterList(position);
            holder.tvName.setText(chapterListBean.getDurChapterName());
            if (Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG) || BookshelfHelp.isChapterCached(bookShelfBean.getBookInfoBean(), chapterListBean)) {
                holder.tvName.setSelected(true);
                holder.tvName.getPaint().setFakeBoldText(true);
            } else {
                holder.tvName.setSelected(false);
                holder.tvName.getPaint().setFakeBoldText(false);
            }
            holder.flContent.setOnClickListener(v -> {
                setIndex(position);
                itemClickListener.itemClick(chapterListBean.getDurChapterIndex(), 0, tabPosition);
            });
            if (chapterListBean.getDurChapterIndex() == index) {
                holder.flContent.setBackgroundResource(R.color.btn_bg_press);
                holder.flContent.setClickable(false);
            } else {
                holder.flContent.setBackgroundResource(R.drawable.bg_ib_pre);
                holder.flContent.setClickable(true);
            }
        } else {
            BookmarkBean bookmarkBean = isSearch ? bookmarkBeans.get(position) : bookShelfBean.getBookInfoBean().getBookmarkList().get(position);
            holder.tvName.setText(bookmarkBean.getContent());
            holder.flContent.setOnClickListener(v -> {
                itemClickListener.itemClick(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex(), tabPosition);
            });
            holder.flContent.setOnLongClickListener(view -> {
                itemClickListener.itemLongClick(bookmarkBean, tabPosition);
                return true;
            });
        }

    }

    @Override
    public int getItemCount() {
        if (bookShelfBean == null)
            return 0;
        else if (tabPosition == 0) {
            if (isSearch) {
                return chapterListBeans.size();
            }
            return bookShelfBean.getChapterListSize();
        } else {
            if (isSearch) {
                return bookmarkBeans.size();
            }
            return bookShelfBean.getBookInfoBean().getBookmarkList().size();
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (tabPosition == 0) {
            notifyItemChanged(this.index);
            this.index = index;
            notifyItemChanged(this.index);
        }
    }

    class ThisViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout flContent;
        private TextView tvName;

        ThisViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}
