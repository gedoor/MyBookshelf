//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.impl;

import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.ChapterListBean;

public interface IReadBookView extends IView {

    /**
     * @return Book标志
     */
    String getNoteUrl();

    Boolean getAdd();

    void setAdd(Boolean isAdd);

    void finishContent();

    void error(String msg);

    /**
     * 开始加载
     */
    void startLoadingBook();

    void setHpbReadProgressMax(int count);

    void initChapterList();

    void showOnLineView();

    void finish();

    void changeSourceFinish();

    void openBookFromOther();

    void chapterChange(ChapterListBean chapterListBean);

    void onMediaButton();

    void toast(String msg);

    /**
     * 更新朗读状态
     */
    void upAloudState(int state);

    void upAloudTimer(String timer);

    void speakIndex(int index);
}
