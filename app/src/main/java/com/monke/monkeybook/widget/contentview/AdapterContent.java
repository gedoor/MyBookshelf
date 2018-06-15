package com.monke.monkeybook.widget.contentview;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

public class AdapterContent extends RefreshRecyclerViewAdapter {

    public AdapterContent() {
        super(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewholder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewtype(int position) {
        return 0;
    }

    @Override
    public int getItemcount() {
        return 0;
    }
}
