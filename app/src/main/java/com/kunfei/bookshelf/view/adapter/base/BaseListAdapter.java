package com.kunfei.bookshelf.view.adapter.base;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by newbiechen on 17-3-21.
 */

public abstract class BaseListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "BaseListAdapter";
    /*common statement*/
    protected final List<T> mList = new ArrayList<>();
    protected OnItemClickListener mClickListener;
    protected OnItemLongClickListener mLongClickListener;

    /************************abstract area************************/
    protected abstract IViewHolder<T> createViewHolder(int viewType);

    /*************************rewrite logic area***************************************/
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        IViewHolder<T> viewHolder = createViewHolder(viewType);

        View view = viewHolder.createItemView(parent);
        //初始化
        RecyclerView.ViewHolder holder = new BaseViewHolder(view, viewHolder);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //防止别人直接使用RecyclerView.ViewHolder调用该方法
        if (!(holder instanceof BaseViewHolder))
            throw new IllegalArgumentException("The ViewHolder item must extend BaseViewHolder");

        IViewHolder<T> iHolder = ((BaseViewHolder) holder).holder;
        iHolder.onBind(getItem(position), position);

        //设置点击事件
        holder.itemView.setOnClickListener((v) -> {
            if (mClickListener != null) {
                mClickListener.onItemClick(v, position);
            }
            //adapter监听点击事件
            iHolder.onClick();
            onItemClick(v, position);
        });
        //设置长点击事件
        holder.itemView.setOnLongClickListener(
                (v) -> {
                    boolean isClicked = false;
                    if (mLongClickListener != null) {
                        isClicked = mLongClickListener.onItemLongClick(v, position);
                    }
                    //Adapter监听长点击事件
                    onItemLongClick(v, position);
                    return isClicked;
                }
        );
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    protected void onItemClick(View v, int pos) {
    }

    protected void onItemLongClick(View v, int pos) {
    }

    /******************************public area***********************************/

    public void setOnItemClickListener(OnItemClickListener mListener) {
        this.mClickListener = mListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mListener) {
        this.mLongClickListener = mListener;
    }

    public void addItem(T value) {
        mList.add(value);
        notifyDataSetChanged();
    }

    public void addItem(int index, T value) {
        mList.add(index, value);
        notifyDataSetChanged();
    }

    public void addItems(List<T> values) {
        mList.addAll(values);
        notifyDataSetChanged();
    }

    public void removeItem(T value) {
        mList.remove(value);
        notifyDataSetChanged();
    }

    public void removeItems(List<T> value) {
        mList.removeAll(value);
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return mList.get(position);
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(mList);
    }

    public int getItemSize() {
        return mList.size();
    }

    public void refreshItems(List<T> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
    }

    /***************************inner class area***********************************/
    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int pos);
    }
}
