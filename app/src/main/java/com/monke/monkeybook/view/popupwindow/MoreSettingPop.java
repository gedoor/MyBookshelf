//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.activity.ReadBookActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.monke.monkeybook.view.fragment.SettingsFragment.ImmersionAction;

public class MoreSettingPop extends PopupWindow {

    @BindView(R.id.sb_click_all_next)
    SwitchButton sbClickAllNext;
    @BindView(R.id.sb_key)
    SwitchButton sbKey;
    @BindView(R.id.sb_click)
    SwitchButton sbClick;
    @BindView(R.id.sb_show_title)
    SwitchButton sbShowTitle;
    @BindView(R.id.sb_showTimeBattery)
    SwitchButton sbShowTimeBattery;
    @BindView(R.id.sb_hideStatusBar)
    SwitchButton sbHideStatusBar;
    @BindView(R.id.ll_hideStatusBar)
    LinearLayout llHideStatusBar;
    @BindView(R.id.ll_showTimeBattery)
    LinearLayout llShowTimeBattery;
    @BindView(R.id.sb_hideNavigationBar)
    SwitchButton sbHideNavigationBar;
    @BindView(R.id.ll_hideNavigationBar)
    LinearLayout llHideNavigationBar;
    @BindView(R.id.sbImmersionBar)
    SwitchButton sbImmersionBar;
    @BindView(R.id.llImmersionBar)
    LinearLayout llImmersionBar;
    @BindView(R.id.llScreenTimeOut)
    LinearLayout llScreenTimeOut;
    @BindView(R.id.tv_screen_time_out)
    TextView tvScreenTimeOut;
    @BindView(R.id.tvJFConvert)
    TextView tvJFConvert;
    @BindView(R.id.llJFConvert)
    LinearLayout llJFConvert;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    public interface OnChangeProListener {
        void keepScreenOnChange(int keepScreenOn);

        void reLoad();
    }

    private OnChangeProListener changeProListener;

    @SuppressLint("InflateParams")
    public MoreSettingPop(ReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;

        View view = LayoutInflater.from(activity).inflate(R.layout.pop_more_setting, null);
        ImmersionBar.navigationBarPadding(activity, view);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setClippingEnabled(false);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void bindEvent() {
        sbHideStatusBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setHideStatusBar(isChecked);
            initData();
            changeProListener.reLoad();
        });
        sbHideNavigationBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setHideNavigationBar(isChecked);
            initData();
            changeProListener.reLoad();
        });
        sbKey.setOnCheckedChangeListener((buttonView, isChecked) -> readBookControl.setCanKeyTurn(isChecked));
        sbClick.setOnCheckedChangeListener((buttonView, isChecked) -> readBookControl.setCanClickTurn(isChecked));
        sbClickAllNext.setOnCheckedChangeListener((buttonView, isChecked) -> readBookControl.setClickAllNext(isChecked));
        sbShowTitle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setShowTitle(isChecked);
            readBookControl.setLineChange(System.currentTimeMillis());
            changeProListener.reLoad();
        });
        sbShowTimeBattery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setShowTimeBattery(isChecked);
            readBookControl.setLineChange(System.currentTimeMillis());
            changeProListener.reLoad();
        });
        sbImmersionBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setImmersionStatusBar(isChecked);
            readBookControl.setLineChange(System.currentTimeMillis());
            Intent intent = new Intent(ImmersionAction);
            intent.putExtra("data", "Immersion_Change");
            activity.sendBroadcast(intent);
            changeProListener.reLoad();
        });
        llScreenTimeOut.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.keep_light))
                    .setSingleChoiceItems(activity.getResources().getStringArray(R.array.screen_time_out), readBookControl.getScreenTimeOut(), (dialogInterface, i) -> {
                        readBookControl.setScreenTimeOut(i);
                        upScreenTimeOut(i);
                        changeProListener.keepScreenOnChange(i);
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
        });
        llJFConvert.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.jf_convert))
                    .setSingleChoiceItems(activity.getResources().getStringArray(R.array.convert_s), readBookControl.getTextConvert(), (dialogInterface, i) -> {
                        readBookControl.setTextConvert(i);
                        upFConvert(i);
                        changeProListener.reLoad();
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
        });
    }

    private void initData() {
        upScreenTimeOut(readBookControl.getScreenTimeOut());
        upFConvert(readBookControl.getTextConvert());
        sbHideStatusBar.setCheckedImmediatelyNoEvent(readBookControl.getHideStatusBar());
        sbHideNavigationBar.setCheckedImmediatelyNoEvent(readBookControl.getHideNavigationBar());
        sbKey.setCheckedImmediatelyNoEvent(readBookControl.getCanKeyTurn());
        sbClick.setCheckedImmediatelyNoEvent(readBookControl.getCanClickTurn());
        sbClickAllNext.setCheckedImmediatelyNoEvent(readBookControl.getClickAllNext());
        sbShowTitle.setCheckedImmediatelyNoEvent(readBookControl.getShowTitle());
        sbShowTimeBattery.setCheckedImmediatelyNoEvent(readBookControl.getShowTimeBattery());
        sbImmersionBar.setCheckedImmediatelyNoEvent(readBookControl.getImmersionStatusBar());
        if (readBookControl.getHideStatusBar()) {
            llShowTimeBattery.setVisibility(View.VISIBLE);
        } else {
            llShowTimeBattery.setVisibility(View.GONE);
        }

    }

    private void upScreenTimeOut(int screenTimeOut) {
        tvScreenTimeOut.setText(activity.getResources().getStringArray(R.array.screen_time_out)[screenTimeOut]);
    }

    private void upFConvert(int fConvert){
        tvJFConvert.setText(activity.getResources().getStringArray(R.array.convert_s)[fConvert]);
    }

}
