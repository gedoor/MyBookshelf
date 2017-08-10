package com.monke.monkeybook.view;

import android.graphics.Paint;
import com.monke.basemvplib.IView;

public interface IBookReadView extends IView{

    public Paint getPaint();

    public int getContentWidth();

    public void initContentSuccess(int durChapterIndex, int chapterAll, int durPageIndex);

    public void startLoadingBook();

    public void setHpbReadProgressMax(int count);

    public void initPop();

    public void showLoadBook();

    public void dimissLoadBook();

    public void loadLocationBookError();

    public void showDownloadMenu();
}
