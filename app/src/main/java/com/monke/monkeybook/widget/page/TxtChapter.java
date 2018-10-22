package com.monke.monkeybook.widget.page;

import java.util.List;

/**
 * 章节
 */

class TxtChapter {
    private int position;
    private List<TxtPage> txtPageList;
    private int status;

    TxtChapter(int position) {
        this.position = position;
    }

    TxtChapter(int position, List<TxtPage> txtPageList) {
        this.position = position;
        this.txtPageList = txtPageList;
    }

    public int getPosition() {
        return position;
    }

    public void setTxtPageList(List<TxtPage> txtPageList) {
        this.txtPageList = txtPageList;
    }

    public List<TxtPage> getTxtPageList() {
        return txtPageList;
    }

    public int size() {
        return txtPageList != null ? txtPageList.size() : 0;
    }

    public TxtPage getPage(int page) {
        if (txtPageList != null && page >= 0 && page < txtPageList.size()) {
            return txtPageList.get(page);
        }
        return null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int mStatus) {
        this.status = mStatus;
    }
}
