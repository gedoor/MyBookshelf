package com.monke.monkeybook.widget.contentview;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import com.monke.monkeybook.widget.ContentView;

public class ContentScrollView extends ContentView {

    public ContentScrollView(@NonNull Context context) {
        super(context);
    }

    @Override
    public void changeTextSize() {

    }

    @Override
    public void changeLineSize() {

    }

    @Override
    public void changeBg() {

    }

    @Override
    public void setFont() {

    }

    @Override
    public void setTextBold() {

    }

    @Override
    public void bookReadInit(ContentSwitchView.OnBookReadInitListener bookReadInitListener) {

    }

    @Override
    public void setInitData(int durChapterIndex, int chapterAll, int durPageIndex) {

    }

    @Override
    public void setLoadDataListener(ContentSwitchView.LoadDataListener loadDataListener) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void readAloudStart() {

    }

    @Override
    public void readAloudNext() {

    }

    @Override
    public void readAloudStop() {

    }

    @Override
    public void speakStart(int speakIndex) {

    }

    @Override
    public void loadError(String errMsg) {

    }

    @Override
    public void upTime(String time) {

    }

    @Override
    public void upBattery(Integer battery) {

    }

    @Override
    public void startLoading() {

    }

    @Override
    public int getContentWidth() {
        return 0;
    }

    @Override
    public Paint getTextPaint() {
        return null;
    }

    @Override
    public String getContentText() {
        return null;
    }


}
