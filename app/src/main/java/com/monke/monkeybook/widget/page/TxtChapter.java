package com.monke.monkeybook.widget.page;

import java.util.List;

/**
 * 章节
 */

class TxtChapter {
    private int position;
    private List<TxtPage> txtPageList;
    private int mStatus;

    TxtChapter(int position, List<TxtPage> txtPageList) {
        this.position = position;
        this.txtPageList = txtPageList;
    }

    public int getPosition() {
        return position;
    }

    public List<TxtPage> getTxtPageList() {
        return txtPageList;
    }

    public int size() {
        return txtPageList != null ? txtPageList.size() : 0;
    }

    public TxtPage get(int page) {
        return txtPageList.get(page);
    }

    public int getmStatus() {
        return mStatus;
    }

    public void setmStatus(int mStatus) {
        this.mStatus = mStatus;
    }
}
