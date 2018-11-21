package com.monke.monkeybook.widget.page;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节
 */

class TxtChapter {
    private int position;
    private List<TxtPage> txtPageList;
    private List<Integer> txtPageLengthList = new ArrayList<>();
    private Enum.PageStatus status = Enum.PageStatus.LOADING;
    private String msg;

    TxtChapter(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    void setTxtPageList(List<TxtPage> txtPageList) {
        this.txtPageList = txtPageList;
        txtPageLengthList.clear();
        for (int i = 0; i < txtPageList.size(); i++) {
            if (i == 0) {
                txtPageLengthList.add(txtPageList.get(i).getContent().length());
            } else {
                txtPageLengthList.add(txtPageLengthList.get(i - 1) + txtPageList.get(i).getContent().length());
            }
        }
    }

    List<TxtPage> getTxtPageList() {
        return txtPageList;
    }

    int getPageSize() {
        return txtPageList != null ? txtPageList.size() : 0;
    }

    TxtPage getPage(int page) {
        if (txtPageList != null) {
            return txtPageList.get(Math.max(0, Math.min(page, txtPageList.size() - 1)));
        }
        return null;
    }

    Enum.PageStatus getStatus() {
        return status;
    }

    void setStatus(Enum.PageStatus mStatus) {
        this.status = mStatus;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getPageLength(int position) {
        return txtPageLengthList.get(position);
    }
}
