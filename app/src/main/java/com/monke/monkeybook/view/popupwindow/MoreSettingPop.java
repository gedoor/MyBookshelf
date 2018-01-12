//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import com.kyleduo.switchbutton.SwitchButton;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;

public class MoreSettingPop extends PopupWindow{
    private Context mContext;
    private View view;

    private SwitchButton sbKey;
    private SwitchButton sbClick;
    private SwitchButton sbKeepScreenOn;

    private ReadBookControl readBookControl;

    public interface OnChangeProListener{
        void keepScreenOnChange(Boolean keepScreenOn);
    }
    private MoreSettingPop.OnChangeProListener changeProListener;

    @SuppressLint("InflateParams")
    public MoreSettingPop(Context context, @NonNull MoreSettingPop.OnChangeProListener changeProListener){
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        this.changeProListener = changeProListener;

        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_more_setting,null);
        this.setContentView(view);
        initData();
        bindView();
        bindEvent();

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void bindEvent() {
        sbKey.setOnCheckedChangeListener((buttonView, isChecked) -> readBookControl.setCanKeyTurn(isChecked));
        sbClick.setOnCheckedChangeListener((buttonView, isChecked) -> readBookControl.setCanClickTurn(isChecked));
        sbKeepScreenOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setKeepScreenOn(isChecked);
            changeProListener.keepScreenOnChange(isChecked);
        });
    }

    private void bindView() {
        sbKey = view.findViewById(R.id.sb_key);
        sbClick = view.findViewById(R.id.sb_click);
        sbKeepScreenOn = view.findViewById(R.id.sb_keep_screen_on);

        sbKey.setCheckedImmediatelyNoEvent(readBookControl.getCanKeyTurn());
        sbClick.setCheckedImmediatelyNoEvent(readBookControl.getCanClickTurn());
        sbKeepScreenOn.setCheckedImmediatelyNoEvent(readBookControl.getKeepScreenOn());
    }

    private void initData() {
        readBookControl = ReadBookControl.getInstance();
    }
}
