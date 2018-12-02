package com.monke.monkeybook.widget.page;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节
 */

class TxtChapter {
    private int position;
    private List<TxtPage> txtPageList = new ArrayList<>();
    private List<Integer> txtPageLengthList = new ArrayList<>();
    private List<Integer> paragraphLengthList = new ArrayList<>();
    private Enum.PageStatus status = Enum.PageStatus.LOADING;
    private String msg;

    TxtChapter(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    List<TxtPage> getTxtPageList() {
        return txtPageList;
    }

    void addPage(TxtPage txtPage) {
        txtPageList.add(txtPage);
    }

    int getPageSize() {
        return txtPageList.size();
    }

    TxtPage getPage(int page) {
        if (!txtPageList.isEmpty()) {
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

    int getPageLength(int position) {
        if (txtPageLengthList == null || position >= txtPageLengthList.size()) {
            return -1;
        }
        return txtPageLengthList.get(position);
    }

    void addTxtPageLength(int length) {
        txtPageLengthList.add(length);
    }

    List<Integer> getTxtPageLengthList() {
        return txtPageLengthList;
    }

    List<Integer> getParagraphLengthList() {
        return paragraphLengthList;
    }

    void addParagraphLength(int length) {
        paragraphLengthList.add(length);
    }

    int getParagraphIndex(int length) {
        for (int i = 0; i < paragraphLengthList.size(); i++) {
            if ((i == 0 || paragraphLengthList.get(i - 1) < length) && length <= paragraphLengthList.get(i)) {
                return i;
            }
        }
        return -1;
    }
}
