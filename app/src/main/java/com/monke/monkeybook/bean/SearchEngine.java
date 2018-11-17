package com.monke.monkeybook.bean;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class SearchEngine {
    private String tag;
    private boolean hasMore;
    private int page;

    public SearchEngine(String tag) {
        this.tag = tag;
        hasMore = true;
        page = 1;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public synchronized void pageAdd(){
        this.page += 1;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof SearchEngine){
            return TextUtils.equals(((SearchEngine) obj).tag, this.tag);
        }
        return super.equals(obj);
    }
}