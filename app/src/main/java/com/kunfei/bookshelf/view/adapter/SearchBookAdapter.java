//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookKindBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.view.adapter.base.BaseListAdapter;
import com.kunfei.bookshelf.widget.CoverImageView;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

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
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onItemClick(v, position);
        });
        Activity activity = activityRef.get();
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(searchBooks.get(position).getCoverUrl())
                    .apply(new RequestOptions()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop()
                            .placeholder(R.drawable.img_cover_default))
                    .into(myViewHolder.ivCover);
        }
        myViewHolder.tvName.setText(String.format("%s (%s)", searchBooks.get(position).getName(), searchBooks.get(position).getAuthor()));
        BookKindBean bookKindBean = new BookKindBean(searchBooks.get(position).getKind());
        if (TextUtils.isEmpty(bookKindBean.getKind())) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(bookKindBean.getKind());
        }
        if (TextUtils.isEmpty(bookKindBean.getWordsS())) {
            myViewHolder.tvWords.setVisibility(View.GONE);
        } else {
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(bookKindBean.getWordsS());
        }
        if (TextUtils.isEmpty(bookKindBean.getState())) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(bookKindBean.getState());
        }
        if (searchBooks.get(position).getLastChapter() != null && searchBooks.get(position).getLastChapter().length() > 0)
            myViewHolder.tvLasted.setText(searchBooks.get(position).getLastChapter());
        else if (searchBooks.get(position).getDesc() != null && searchBooks.get(position).getDesc().length() > 0) {
            myViewHolder.tvLasted.setText(searchBooks.get(position).getDesc());
        } else
            myViewHolder.tvLasted.setText("");
        if (searchBooks.get(position).getOrigin() != null && searchBooks.get(position).getOrigin().length() > 0) {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(activity.getString(R.string.origin_format, searchBooks.get(position).getOrigin()));
        } else {
            myViewHolder.tvOrigin.setVisibility(View.GONE);
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

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = new ArrayList<>(searchBooks);
        if (newDataS != null && newDataS.size() > 0) {
            saveData(newDataS);
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (copyDataS.size() == 0) {
                copyDataS.addAll(newDataS);
                sortSearchBooks(copyDataS, keyWord);
            } else {
                //存在
                for (SearchBookBean temp : newDataS) {
                    Boolean hasSame = false;
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
                    } else if (temp.getName().contains(keyWord) || temp.getAuthor().contains(keyWord)) {
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
            if(activity != null) {
                searchBooks = copyDataS;
                activity.runOnUiThread(this::notifyDataSetChanged);
            }
        }
    }

    public void clearAll() {
        int bookSize = searchBooks.size();
        if (bookSize > 0) {
            try {
                Glide.with(activityRef.get()).onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchBooks.clear();
            notifyItemRangeRemoved(0, bookSize);
        }
    }

    private void saveData(List<SearchBookBean> data) {
        AsyncTask.execute(() -> DbHelper.getDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(data));
    }

    private void sortSearchBooks(List<SearchBookBean> searchBookBeans, String keyWord) {
        try {
            Collections.sort(searchBookBeans, (o1, o2) -> {
                if (TextUtils.equals(keyWord, o1.getName())
                        || TextUtils.equals(keyWord, o1.getAuthor())) {
                    return -1;
                } else if (TextUtils.equals(keyWord, o2.getName())
                        || TextUtils.equals(keyWord, o2.getAuthor())) {
                    return 1;
                } else if (o1.getName().contains(keyWord) || o1.getAuthor().contains(keyWord)) {
                    return -1;
                } else if (o2.getName().contains(keyWord) || o2.getAuthor().contains(keyWord)) {
                    return 1;
                } else {
                    return 0;
                }
            });
        } catch (Exception ignored) {
        }
    }

    public SearchBookBean getItemData(int pos) {
        return searchBooks.get(pos);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        CoverImageView ivCover;
        TextView tvName;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvOrigin;
        TextView tvOriginNum;

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
        }
    }

}