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
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ReadBookControl;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private Context mContext;
    private View view;
    private ReadBookControl readBookControl;

    public interface OnChangeProListener {
        void keepScreenOnChange(Boolean keepScreenOn);

        void showTitle(Boolean showTitle);
    }

    private OnChangeProListener changeProListener;

    @SuppressLint("InflateParams")
    public MoreSettingPop(Context context, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        this.changeProListener = changeProListener;

        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_more_setting, null);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void bindEvent() {
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
            BookshelfHelp.clearLineContent();
            changeProListener.showTitle(isChecked);
        });
    }

    private void initData() {
        readBookControl = ReadBookControl.getInstance();

        sbKey.setCheckedImmediatelyNoEvent(readBookControl.getCanKeyTurn());
        sbClick.setCheckedImmediatelyNoEvent(readBookControl.getCanClickTurn());
        sbClickAllNext.setCheckedImmediatelyNoEvent(readBookControl.getClickAllNext());
        sbKeepScreenOn.setCheckedImmediatelyNoEvent(readBookControl.getKeepScreenOn());
        sbClickAnim.setCheckedImmediatelyNoEvent(readBookControl.getClickAnim());
        sbShowTitle.setCheckedImmediatelyNoEvent(readBookControl.getShowTitle());
    }
}
