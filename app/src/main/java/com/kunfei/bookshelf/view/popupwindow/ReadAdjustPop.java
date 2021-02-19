//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.kunfei.bookshelf.databinding.PopReadAdjustBinding;
import com.kunfei.bookshelf.help.ReadBookControl;

public class ReadAdjustPop extends FrameLayout {

    private PopReadAdjustBinding binding = PopReadAdjustBinding.inflate(LayoutInflater.from(getContext()), this, true);
    private Activity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private Callback callback;

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
        binding.vwBg.setOnClickListener(null);
    }

    public void setListener(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        initData();
        bindEvent();
        initLight();
    }

    public void show() {
        initLight();
    }

    private void initData() {
        binding.scbTtsFollowSys.setChecked(readBookControl.isSpeechRateFollowSys());
        binding.hpbTtsSpeechRate.setEnabled(!readBookControl.isSpeechRateFollowSys());
        //CPM范围设置 每分钟阅读200字到2000字 默认500字/分钟
        binding.hpbClick.setMax(readBookControl.maxCPM - readBookControl.minCPM);
        binding.hpbClick.setProgress(readBookControl.getCPM());
        binding.tvAutoPage.setText(String.format("%sCPM", readBookControl.getCPM()));
        binding.hpbTtsSpeechRate.setProgress(readBookControl.getSpeechRate() - 5);
    }

    private void bindEvent() {
        //亮度调节
        binding.llFollowSys.setOnClickListener(v -> {
            binding.scbFollowSys.setChecked(!binding.scbFollowSys.isChecked(), true);
        });
        binding.scbFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            readBookControl.setLightFollowSys(isChecked);
            if (isChecked) {
                //跟随系统
                binding.hpbLight.setEnabled(false);
                setScreenBrightness();
            } else {
                //不跟随系统
                binding.hpbLight.setEnabled(true);
                setScreenBrightness(readBookControl.getLight());
            }
        });
        binding.hpbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        //自动翻页阅读速度(CPM)
        binding.hpbClick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.tvAutoPage.setText(String.format("%sCPM", i + readBookControl.minCPM));
                readBookControl.setCPM(i + readBookControl.minCPM);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //朗读语速调节
        binding.llTtsSpeechRate.setOnClickListener(v -> {
            binding.scbTtsFollowSys.setChecked(!binding.scbTtsFollowSys.isChecked(), true);
        });
        binding.scbTtsFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked) {
                //跟随系统
                binding.hpbTtsSpeechRate.setEnabled(false);
                readBookControl.setSpeechRateFollowSys(true);
                if (callback != null) {
                    callback.speechRateFollowSys();
                }
            } else {
                //不跟随系统
                binding.hpbTtsSpeechRate.setEnabled(true);
                readBookControl.setSpeechRateFollowSys(false);
                if (callback != null) {
                    callback.changeSpeechRate(readBookControl.getSpeechRate());
                }
            }
        });
        binding.hpbTtsSpeechRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                readBookControl.setSpeechRate(seekBar.getProgress() + 5);
                if (callback != null) {
                    callback.changeSpeechRate(readBookControl.getSpeechRate());
                }
            }
        });
    }

    public void setScreenBrightness() {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        activity.getWindow().setAttributes(params);
    }

    public void setScreenBrightness(int value) {
        if (value < 1) value = 1;
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.screenBrightness = value * 1.0f / 255f;
        activity.getWindow().setAttributes(params);
    }

    public void initLight() {
        binding.hpbLight.setProgress(readBookControl.getLight());
        binding.scbFollowSys.setChecked(readBookControl.getLightFollowSys());
        if (!readBookControl.getLightFollowSys()) {
            setScreenBrightness(readBookControl.getLight());
        }
    }

    public interface Callback {
        void changeSpeechRate(int speechRate);

        void speechRateFollowSys();
    }
}
