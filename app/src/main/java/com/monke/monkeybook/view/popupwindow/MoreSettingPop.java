//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import com.kyleduo.switchbutton.SwitchButton;
import com.monke.monkeybook.R;
import com.monke.monkeybook.ReadBookControl;

public class MoreSettingPop extends PopupWindow{
    private Context mContext;
    private View view;

    private SwitchButton sbKey;
    private SwitchButton sbClick;
    private SwitchButton sbHideStatusBar;

    private ReadBookControl readBookControl;

    public interface OnChangeProListener{
        public void statusBarChange(Boolean hideStatusBar);
    }
    private MoreSettingPop.OnChangeProListener changeProListener;

    public MoreSettingPop(Context context, @NonNull MoreSettingPop.OnChangeProListener changeProListener){
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        this.changeProListener = changeProListener;

        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_moresetting,null);
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
        sbHideStatusBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readBookControl.setHideStatusBar(isChecked);
            changeProListener.statusBarChange(isChecked);
        });
    }

    private void bindView() {
        sbKey = view.findViewById(R.id.sb_key);
        sbClick = view.findViewById(R.id.sb_click);
        sbHideStatusBar = view.findViewById(R.id.sb_hide_status_bar);

        sbKey.setCheckedImmediatelyNoEvent(readBookControl.getCanKeyTurn());
        sbClick.setCheckedImmediatelyNoEvent(readBookControl.getCanClickTurn());
        sbHideStatusBar.setCheckedImmediatelyNoEvent(readBookControl.getHideStatusBar());
    }

    private void initData() {
        readBookControl = ReadBookControl.getInstance();
    }
}
