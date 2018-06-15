package com.monke.monkeybook.widget.contentview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.widget.BatteryView;
import com.monke.monkeybook.widget.ContentView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContentScrollView extends ContentView {

    @BindView(R.id.iv_bg)
    ImageView ivBg;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tvProgress)
    TextView tvProgress;
    @BindView(R.id.vwBattery)
    BatteryView vwBattery;
    @BindView(R.id.llTop)
    LinearLayout llTop;
    @BindView(R.id.rv_book_content)
    RecyclerView rvBookContent;
    @BindView(R.id.vwLine)
    View vwLine;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvPage)
    TextView tvPage;
    @BindView(R.id.llBottom)
    LinearLayout llBottom;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.tv_error_info)
    TextView tvErrorInfo;
    @BindView(R.id.tv_load_again)
    TextView tvLoadAgain;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    public ContentScrollView(@NonNull Context context) {
        super(context);
        init();
    }

    public ContentScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ContentScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_content_recycler, this, false);
        addView(view);
        ButterKnife.bind(this, view);


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
