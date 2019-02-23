//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.widget.check_box.SmoothCheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadAdjustPop extends FrameLayout {
    @BindView(R.id.hpb_light)
    SeekBar hpbLight;
    @BindView(R.id.scb_follow_sys)
    SmoothCheckBox scbFollowSys;
    @BindView(R.id.ll_follow_sys)
    LinearLayout llFollowSys;
    @BindView(R.id.ll_click)
    LinearLayout llClick;
    @BindView(R.id.hpb_click)
    SeekBar hpbClick;
    @BindView(R.id.ll_tts_SpeechRate)
    LinearLayout llTtsSpeechRate;
    @BindView(R.id.hpb_tts_SpeechRate)
    SeekBar hpbTtsSpeechRate;
    @BindView(R.id.scb_tts_follow_sys)
    SmoothCheckBox scbTtsFollowSys;
    @BindView(R.id.tv_auto_page)
    TextView tvAutoPage;

    private Activity context;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnAdjustListener adjustListener;

    public ReadAdjustPop(Context context) {
        super(context);
        init(context);
    }

    public ReadAdjustPop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadAdjustPop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.pop_read_adjust, null);
        addView(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);
    }

    public void setListener(Activity activity, OnAdjustListener adjustListener) {
        this.context = activity;
        this.adjustListener = adjustListener;
        initData();
        bindEvent();
        initLight();
    }

    public void show() {
        initLight();
    }

    private void initData() {
        scbTtsFollowSys.setChecked(readBookControl.isSpeechRateFollowSys());
        if (readBookControl.isSpeechRateFollowSys()) {
            hpbTtsSpeechRate.setEnabled(false);
        } else {
            hpbTtsSpeechRate.setEnabled(true);
        }
        hpbClick.setMax(180);
        hpbClick.setProgress(readBookControl.getClickSensitivity());
        tvAutoPage.setText(String.format("%sS", readBookControl.getClickSensitivity()));
        hpbTtsSpeechRate.setProgress(readBookControl.getSpeechRate() - 5);
    }

    private void bindEvent() {
        //亮度调节
        llFollowSys.setOnClickListener(v -> {
            if (scbFollowSys.isChecked()) {
                scbFollowSys.setChecked(false, true);
            } else {
                scbFollowSys.setChecked(true, true);
            }
        });
        scbFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            readBookControl.setLightFollowSys(isChecked);
            if (isChecked) {
                //跟随系统
                hpbLight.setEnabled(false);
                setScreenBrightness();
            } else {
                //不跟随系统
                hpbLight.setEnabled(true);
                setScreenBrightness(readBookControl.getLight());
            }
        });
        hpbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!readBookControl.getLightFollowSys()) {
                    readBookControl.setLight(i);
                    setScreenBrightness(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //自动翻页间隔
        hpbClick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvAutoPage.setText(String.format("%sS", i));
                readBookControl.setClickSensitivity(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //朗读语速调节
        llTtsSpeechRate.setOnClickListener(v -> {
            if (scbTtsFollowSys.isChecked()) {
                scbTtsFollowSys.setChecked(false, true);
            } else {
                scbTtsFollowSys.setChecked(true, true);
            }
        });
        scbTtsFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked) {
                //跟随系统
                hpbTtsSpeechRate.setEnabled(false);
                readBookControl.setSpeechRateFollowSys(true);
                if (adjustListener != null) {
                    adjustListener.speechRateFollowSys();
                }
            } else {
                //不跟随系统
                hpbTtsSpeechRate.setEnabled(true);
                readBookControl.setSpeechRateFollowSys(false);
                if (adjustListener != null) {
                    adjustListener.changeSpeechRate(readBookControl.getSpeechRate());
                }
            }
        });
        hpbTtsSpeechRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                readBookControl.setSpeechRate(seekBar.getProgress() + 5);
                if (adjustListener != null) {
                    adjustListener.changeSpeechRate(readBookControl.getSpeechRate());
                }
            }
        });
    }

    public void setScreenBrightness() {
        WindowManager.LayoutParams params = (context).getWindow().getAttributes();
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        (context).getWindow().setAttributes(params);
    }

    public void setScreenBrightness(int value) {
        if (value < 1) value = 1;
        WindowManager.LayoutParams params = (context).getWindow().getAttributes();
        params.screenBrightness = value * 1.0f / 255f;
        (context).getWindow().setAttributes(params);
    }

    public void initLight() {
        hpbLight.setProgress(readBookControl.getLight());
        scbFollowSys.setChecked(readBookControl.getLightFollowSys());
        if (!readBookControl.getLightFollowSys()) {
            setScreenBrightness(readBookControl.getLight());
        }
    }

    public interface OnAdjustListener {
        void changeSpeechRate(int speechRate);

        void speechRateFollowSys();
    }
}
