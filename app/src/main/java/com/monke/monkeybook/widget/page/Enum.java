package com.monke.monkeybook.widget.page;

public class Enum {
    /**
     * 作用：翻页动画的模式
     */
    public enum PageMode {
        COVER, SIMULATION, NONE, SCROLL, SLIDE
    }

    public enum PageStatus {
        LOADING, FINISH, ERROR, EMPTY, CATEGORY_EMPTY, CHANGE_SOURCE,
    }
}
