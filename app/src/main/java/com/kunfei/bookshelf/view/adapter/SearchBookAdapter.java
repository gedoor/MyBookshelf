//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookKindBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.base.BaseListAdapter;
import com.kunfei.bookshelf.widget.CoverImageView;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.kunfei.bookshelf.utils.StringUtils.isTrimEmpty;


public class SearchBookAdapter extends RefreshRecyclerViewAdapter {
    private WeakReference<Activity> activityRef;
    private List<SearchBookBean> searchBooks;
    private BaseListAdapter.OnItemClickListener itemClickListener;

    public SearchBookAdapter(Activity activity) {
        super(true);
        this.activityRef = new WeakReference<>(activity);
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_book, parent, false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindIViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Activity activity = activityRef.get();
        holder.itemView.setBackgroundColor(ThemeStore.backgroundColor(activity));
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onItemClick(v, position);
        });
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(searchBooks.get(position).getCoverUrl())
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .placeholder(R.drawable.img_cover_default)
                    .into(myViewHolder.ivCover);
        }
        myViewHolder.tvName.setText(String.format("%s (%s)", searchBooks.get(position).getName(), searchBooks.get(position).getAuthor()));
        BookKindBean bookKindBean = new BookKindBean(searchBooks.get(position).getKind());
        if (isTrimEmpty(bookKindBean.getKind())) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(bookKindBean.getKind());
        }
        if (isTrimEmpty(bookKindBean.getWordsS())) {
            myViewHolder.tvWords.setVisibility(View.GONE);
        } else {
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(bookKindBean.getWordsS());
        }
        if (isTrimEmpty(bookKindBean.getState())) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(bookKindBean.getState());
        }
        //来源
        if (isTrimEmpty(searchBooks.get(position).getOrigin())) {
            myViewHolder.tvOrigin.setVisibility(View.GONE);
        } else {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(activity.getString(R.string.origin_format, searchBooks.get(position).getOrigin()));
        }
        //最新章节
        if (isTrimEmpty(searchBooks.get(position).getLastChapter())) {
            myViewHolder.tvLasted.setVisibility(View.GONE);
        } else {
            myViewHolder.tvLasted.setText(searchBooks.get(position).getLastChapter());
            myViewHolder.tvLasted.setVisibility(View.VISIBLE);
        }
        //简介
        if (isTrimEmpty(searchBooks.get(position).getIntroduce())) {
            myViewHolder.tvIntroduce.setVisibility(View.GONE);
        } else {
            myViewHolder.tvIntroduce.setText(StringUtils.formatHtml(searchBooks.get(position).getIntroduce()));
            myViewHolder.tvIntroduce.setVisibility(View.VISIBLE);
        }
        myViewHolder.tvOriginNum.setText(String.format("共%d个源", searchBooks.get(position).getOriginNum()));
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    public void setItemClickListener(BaseListAdapter.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public synchronized void upData(DataAction action, List<SearchBookBean> newDataS) {
        switch (action) {
            case ADD:
                searchBooks = newDataS;
                notifyDataSetChanged();
                break;
            case CLEAR:
                if (!searchBooks.isEmpty()) {
                    try {
                        Glide.with(activityRef.get()).onDestroy();
                    } catch (Exception ignored) {
                    }
                    searchBooks.clear();
                    notifyDataSetChanged();
                }
                break;
        }
    }

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = new ArrayList<>(searchBooks);
        if (newDataS != null && newDataS.size() > 0) {
            saveData(newDataS);
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (copyDataS.size() == 0) {
                copyDataS.addAll(newDataS);
            } else {
                //存在
                for (SearchBookBean temp : newDataS) {
                    boolean hasSame = false;
                    for (int i = 0, size = copyDataS.size(); i < size; i++) {
                        SearchBookBean searchBook = copyDataS.get(i);
                        if (TextUtils.equals(temp.getName(), searchBook.getName())
                                && TextUtils.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            hasSame = true;
                            searchBook.addOriginUrl(temp.getTag());
                            break;
                        }
                    }

                    if (!hasSame) {
                        searchBookBeansAdd.add(temp);
                    }
                }
                //添加
                for (SearchBookBean temp : searchBookBeansAdd) {
                    if (TextUtils.equals(keyWord, temp.getName())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else if (TextUtils.equals(keyWord, temp.getAuthor())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else {
                        copyDataS.add(temp);
                    }
                }
            }
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(() -> upData(DataAction.ADD, copyDataS));
            }
        }
    }

    private void saveData(List<SearchBookBean> data) {
        AsyncTask.execute(() -> DbHelper.getDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(data));
    }

    public SearchBookBean getItemData(int pos) {
        return searchBooks.get(pos);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ViewGroup flContent;
        CoverImageView ivCover;
        TextView tvName;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvOrigin;
        TextView tvOriginNum;
        TextView tvIntroduce;

        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLasted = itemView.findViewById(R.id.tv_lasted);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            tvOriginNum = itemView.findViewById(R.id.tv_origin_num);
            tvIntroduce = itemView.findViewById(R.id.tv_introduce);
        }
    }

    public enum DataAction {
        ADD, CLEAR
    }

}