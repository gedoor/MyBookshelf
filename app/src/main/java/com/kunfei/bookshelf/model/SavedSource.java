package com.kunfei.bookshelf.model;

import com.kunfei.bookshelf.bean.BookSourceBean;

public class SavedSource {
    public static SavedSource Instance = new SavedSource();

    private String bookName;
    private long saveTime;
    private BookSourceBean bookSource;

    private SavedSource() {
        this.bookName = "";
        saveTime = 0;
    }

    public String getBookName() {
        return this.bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public BookSourceBean getBookSource() {
        return bookSource;
    }

    public void setBookSource(BookSourceBean bookSource) {
        this.bookSource = bookSource;
    }
}
