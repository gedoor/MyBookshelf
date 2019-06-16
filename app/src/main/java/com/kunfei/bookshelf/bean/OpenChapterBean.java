package com.kunfei.bookshelf.bean;

public class OpenChapterBean {
    private int chapterIndex;
    private int pageIndex;

    public OpenChapterBean(int chapterIndex, int pageIndex) {
        this.chapterIndex = chapterIndex;
        this.pageIndex = pageIndex;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }
}
