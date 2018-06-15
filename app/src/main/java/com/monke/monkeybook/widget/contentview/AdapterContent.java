package com.monke.monkeybook.widget.contentview;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterContent extends RefreshRecyclerViewAdapter {
    private List<ChapterListBean> chapterListBeans;

    AdapterContent() {
        super(false);
        chapterListBeans = new ArrayList<>();
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
        return chapterListBeans.size();
    }



}
