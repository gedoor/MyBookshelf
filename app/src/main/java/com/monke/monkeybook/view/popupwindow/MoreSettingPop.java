//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

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
    @BindView(R.id.sb_click_anim)
    SwitchButton sbClickAnim;
    @BindView(R.id.sb_key)
    SwitchButton sbKey;
    @BindView(R.id.sb_click)
    SwitchButton sbClick;
    @BindView(R.id.sb_keep_screen_on)
    SwitchButton sbKeepScreenOn;
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
    @BindView(R.id.sb_showLine)
    SwitchButton sbShowLine;
    @BindView(R.id.sbImmersionBar)
    SwitchButton sbImmersionBar;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    public interface OnChangeProListener {
        void keepScreenOnChange(Boolean keepScreenOn);

        void reLoad();
    }

    private OnChangeProListener changeProListener;

    @SuppressLint("InflateParams")
    public MoreSettingPop(ReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;

        View view = LayoutInflater.from(activity).inflate(R.layout.view_pop_more_setting, null);
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
        sbKeepScreenOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setKeepScreenOn(isChecked);
            changeProListener.keepScreenOnChange(isChecked);
        });
        sbClickAnim.setOnCheckedChangeListener(((compoundButton, b) -> readBookControl.setClickAnim(b)));
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
        sbShowLine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setShowLine(isChecked);
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
    }

    private void initData() {
        sbHideStatusBar.setCheckedImmediatelyNoEvent(readBookControl.getHideStatusBar());
        sbHideNavigationBar.setCheckedImmediatelyNoEvent(readBookControl.getHideNavigationBar());
        sbKey.setCheckedImmediatelyNoEvent(readBookControl.getCanKeyTurn());
        sbClick.setCheckedImmediatelyNoEvent(readBookControl.getCanClickTurn());
        sbClickAllNext.setCheckedImmediatelyNoEvent(readBookControl.getClickAllNext());
        sbKeepScreenOn.setCheckedImmediatelyNoEvent(readBookControl.getKeepScreenOn());
        sbClickAnim.setCheckedImmediatelyNoEvent(readBookControl.getClickAnim());
        sbShowTitle.setCheckedImmediatelyNoEvent(readBookControl.getShowTitle());
        sbShowTimeBattery.setCheckedImmediatelyNoEvent(readBookControl.getShowTimeBattery());
        sbShowLine.setCheckedImmediatelyNoEvent(readBookControl.getShowLine());
        sbImmersionBar.setCheckedImmediatelyNoEvent(readBookControl.getImmersionStatusBar());
        if (readBookControl.getHideStatusBar()) {
            llShowTimeBattery.setVisibility(View.VISIBLE);
        } else {
            llShowTimeBattery.setVisibility(View.GONE);
        }

    }
}
