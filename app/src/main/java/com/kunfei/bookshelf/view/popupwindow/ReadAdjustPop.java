//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.widget.checkbox.SmoothCheckBox;

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
    private Boolean isFollowSys;
    private int light;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnAdjustListener adjustListener;
    private SharedPreferences preference = MApplication.getInstance().getConfigPreferences();

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
        View view = LayoutInflater.from(context).inflate(R.layout.pop_read_adjust, null);
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

    public void dismiss() {
        saveLight();
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
            isFollowSys = isChecked;
            if (isChecked) {
                //跟随系统
                hpbLight.setEnabled(false);
                setScreenBrightness();
            } else {
                //不跟随系统
                hpbLight.setEnabled(true);
                setScreenBrightness(light);
            }
        });
        hpbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!isFollowSys) {
                    light = i;
                    setScreenBrightness(light);
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

    public int getScreenBrightness() {
        int value = 0;
        ContentResolver cr = context.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public void setScreenBrightness(int value) {
        if (value < 1) value = 1;
        WindowManager.LayoutParams params = (context).getWindow().getAttributes();
        params.screenBrightness = value * 1.0f / 255f;
        (context).getWindow().setAttributes(params);
    }

    private void saveLight() {
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt("light", light);
        editor.putBoolean("isfollowsys", isFollowSys);
        editor.apply();
    }

    private int getLight() {
        return preference.getInt("light", getScreenBrightness());
    }

    private Boolean getIsFollowSys() {
        return preference.getBoolean("isfollowsys", true);
    }

    public void initLight() {
        isFollowSys = getIsFollowSys();
        light = getLight();
        hpbLight.setProgress(light);
        scbFollowSys.setChecked(isFollowSys);
        if (!isFollowSys) {
            setScreenBrightness(light);
        }
    }

    public interface OnAdjustListener {
        void changeSpeechRate(int speechRate);

        void speechRateFollowSys();
    }
}
