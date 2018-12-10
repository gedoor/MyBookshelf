package com.kunfei.bookshelf.widget.refreshview.scroller;

public interface FastScrollStateChangeListener {

    /**
     * Called when fast scrolling begins
     */
    void onFastScrollStart(FastScroller fastScroller);

    /**
     * Called when fast scrolling ends
     */
    void onFastScrollStop(FastScroller fastScroller);
}