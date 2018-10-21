//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchBookAdapter extends RefreshRecyclerViewAdapter {
    private Activity activity;
    private List<SearchBookBean> searchBooks;
    private OnItemClickListener itemClickListener;

    public SearchBookAdapter(Activity activity) {
        super(true);
        this.activity = activity;
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
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(searchBooks.get(position).getCoverUrl())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                            .dontAnimate().placeholder(R.drawable.img_cover_default))
                    .into(myViewHolder.ivCover);
        }
        myViewHolder.tvName.setText(String.format("%s (%s)", searchBooks.get(position).getName(), searchBooks.get(position).getAuthor()));
        String state = searchBooks.get(position).getState();
        if (state == null || state.length() == 0) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(state);
        }
        long words = searchBooks.get(position).getWords();
        if (words <= 0) {
            myViewHolder.tvWords.setVisibility(View.GONE);
        } else {
            String wordsS = Long.toString(words) + "字";
            if (words > 10000) {
                DecimalFormat df = new DecimalFormat("#.#");
                wordsS = df.format(words * 1.0f / 10000f) + "万字";
            }
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(wordsS);
        }
        String kind = searchBooks.get(position).getKind();
        if (kind == null || kind.length() <= 0) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(kind);
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
        if (searchBooks.get(position).getIsAdd()) {
            myViewHolder.tvAddShelf.setText("已添加");
            myViewHolder.tvAddShelf.setEnabled(false);
        } else {
            myViewHolder.tvAddShelf.setText("+添加");
            myViewHolder.tvAddShelf.setEnabled(true);
        }

        myViewHolder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(myViewHolder.ivCover, position, searchBooks.get(position));
        });
        myViewHolder.tvAddShelf.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickAddShelf(myViewHolder.tvAddShelf, position, searchBooks.get(position));
        });
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = new ArrayList<>(searchBooks);
        if (newDataS != null && newDataS.size() > 0) {
            saveSearchToDb(newDataS);
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (copyDataS.size() == 0) {
                copyDataS.addAll(newDataS);
            } else {
                //已有
                for (SearchBookBean temp : newDataS) {
                    Boolean hasSame = false;
                    for (int i = 0; i < copyDataS.size(); i++) {
                        SearchBookBean searchBook = copyDataS.get(i);
                        if (Objects.equals(temp.getName(), searchBook.getName()) && Objects.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            if (temp.getIsAdd()) {
                                searchBook.setIsAdd(true);
                            }
                            hasSame = true;
                            searchBook.originNumAdd();
                            break;
                        }
                    }
                    if (!hasSame) {
                        searchBookBeansAdd.add(temp);
                    }
                }
                //添加
                for (SearchBookBean temp : searchBookBeansAdd) {
                    if (temp.getName().equals(keyWord)) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            if (!Objects.equals(keyWord, copyDataS.get(i).getName())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else if (temp.getAuthor().contains(keyWord) || temp.getName().contains(keyWord)) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            if (!Objects.equals(keyWord, copyDataS.get(i).getName()) && !Objects.equals(keyWord, copyDataS.get(i).getAuthor())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else {
                        copyDataS.add(temp);
                    }
                }
            }
        }
        searchBooks = copyDataS;
        activity.runOnUiThread(this::notifyDataSetChanged);
    }

    public void clearAll() {
        int bookSize = searchBooks.size();
        if (bookSize > 0) {
            try {
                Glide.with(activity).onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchBooks.clear();
            notifyItemRangeRemoved(0, bookSize);
        }
    }

    public List<SearchBookBean> getSearchBooks() {
        return searchBooks;
    }

    private void saveSearchToDb(List<SearchBookBean> newDataS) {
        Observable.create(e -> {
            DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao()
                    .insertOrReplaceInTx(newDataS);
            e.onNext(true);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public interface OnItemClickListener {
        void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean);

        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        TextView tvName;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvAddShelf;
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
            tvAddShelf = itemView.findViewById(R.id.tv_add_shelf);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            tvOriginNum = itemView.findViewById(R.id.tv_origin_num);
        }
    }

}