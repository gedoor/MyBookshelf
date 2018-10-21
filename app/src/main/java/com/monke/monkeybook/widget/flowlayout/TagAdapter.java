package com.monke.monkeybook.widget.flowlayout;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TagAdapter<T> {
    protected List<T> mTagDataList;
    protected HashSet<Integer> mCheckedPosList = new HashSet<Integer>();
    private OnDataChangedListener mOnDataChangedListener;

    public TagAdapter() {

    }

    public TagAdapter(List<T> dataList) {
        mTagDataList = dataList;
    }

    public TagAdapter(T[] dataList) {
        mTagDataList = new ArrayList<T>(Arrays.asList(dataList));
    }

    void setOnDataChangedListener(OnDataChangedListener listener) {
        mOnDataChangedListener = listener;
    }

    public void setSelectedList(int... poses) {
        Set<Integer> set = new HashSet<>();
        for (int pos : poses) {
            set.add(pos);
        }
        setSelectedList(set);
    }

    public void setSelectedList(Set<Integer> set) {
        mCheckedPosList.clear();
        if (set != null)
            mCheckedPosList.addAll(set);
        notifyDataChanged();
    }

    public synchronized void replaceAll(List<T> newDatas) {
        mTagDataList.clear();
        if (newDatas != null)
            mTagDataList.addAll(newDatas);
        notifyDataChanged();
    }

    HashSet<Integer> getPreCheckedList() {
        return mCheckedPosList;
    }

    public int getCount() {
        return mTagDataList == null ? 0 : mTagDataList.size();
    }

    public void notifyDataChanged() {
        mOnDataChangedListener.onChanged();
    }

    public T getItem(int position) {
        return mTagDataList.get(position);
    }

    public abstract View getView(FlowLayout parent, int position, T t);

    public boolean setSelected(int position, T t) {
        return false;
    }

    interface OnDataChangedListener {
        void onChanged();
    }


}