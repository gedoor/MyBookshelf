package com.kunfei.bookshelf.model;

import com.kunfei.bookshelf.bean.BookSourceBean;

public class SavedSource {
    public static SavedSource Instance = new SavedSource();

    private String bookName;
    private long saveTime;
    private String sourceUrl;

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
        if (sourceUrl == null) {
            return null;
        }
        return BookSourceManager.getBookSourceByUrl(sourceUrl);
    }

    public void setBookSource(BookSourceBean bookSource) {
        if (bookSource != null) {
            this.sourceUrl = bookSource.getBookSourceUrl();
        }
    }
}
