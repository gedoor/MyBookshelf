//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.widget.flowlayout.FlowLayout;
import com.monke.monkeybook.widget.flowlayout.TagAdapter;
import java.util.ArrayList;

public class SearchHistoryAdapter extends TagAdapter<SearchHistoryBean> {
    public SearchHistoryAdapter() {
        super(new ArrayList<SearchHistoryBean>());
    }

    public interface OnItemClickListener{
        void itemClick(SearchHistoryBean searchHistoryBean);
    }
    private SearchHistoryAdapter.OnItemClickListener onItemClickListener;

    public OnItemClickListener getListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public View getView(FlowLayout parent, int position, final SearchHistoryBean searchHistoryBean) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_searchhistory_item,
                parent, false);
        tv.setText(searchHistoryBean.getContent());
        tv.setOnClickListener(v -> {
            if(null != onItemClickListener){
                onItemClickListener.itemClick(searchHistoryBean);
            }
        });
        return tv;
    }

    public SearchHistoryBean getItemData(int position){
        return mTagDatas.get(position);
    }

    public int getDataSize(){
        return mTagDatas.size();
    }
}
