package com.monke.monkeybook.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.monke.monkeybook.widget.contentview.ContentSwitchView;

public abstract class BaseContentView extends FrameLayout {

    public BaseContentView(@NonNull Context context) {
        this(context, null);
    }

    public BaseContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseContentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void changeContentView();

    public abstract void changeBg();

    public abstract void setTextBold();

    public abstract void bookReadInit(ContentSwitchView.OnBookReadInitListener bookReadInitListener);

    public abstract void setInitData(int durChapterIndex, int chapterAll, int durPageIndex);

    public abstract void setLoadDataListener(ContentSwitchView.LoadDataListener loadDataListener);

    public abstract void showLoading();

    public abstract void readAloudStart();

    public abstract void readAloudNext();

    public abstract void readAloudStop();

    public abstract void speakStart(int speakIndex);

    public abstract void loadError(String errMsg);

    public abstract void upTime(String time);

    public abstract void upBattery(Integer battery);

    public abstract void startLoading();

    public abstract int getContentWidth();

    public abstract Paint getTextPaint();

    public abstract String getContentText();


}
