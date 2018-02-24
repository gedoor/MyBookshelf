//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.monke.monkeybook.R;

public class ReadBookMenuMorePop extends PopupWindow{
    private Context mContext;
    private View view;

    private LinearLayout llDownload;
    private LinearLayout llChangeSource;

    @SuppressLint("InflateParams")
    public ReadBookMenuMorePop(Context context){
        super(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mContext = context;
        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_menu_more,null);
        this.setContentView(view);

        initView();

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowmenumore);
    }

    private void initView() {
        llDownload = view.findViewById(R.id.ll_download);
        llChangeSource = view.findViewById(R.id.ll_exchange_source);
        ImageView ivChangeSource = view.findViewById(R.id.iv_change_source);
        ImageView ivDownload = view.findViewById(R.id.iv_download_offline);
        ivChangeSource.getDrawable().mutate();
        ivChangeSource.getDrawable().setColorFilter(mContext.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivDownload.getDrawable().mutate();
        ivDownload.getDrawable().setColorFilter(mContext.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
    }

    public void setOnClickDownload(View.OnClickListener clickDownload){
        llDownload.setOnClickListener(clickDownload);
    }

    public void setOnClickChangeSource(View.OnClickListener clickChangeSource) {
        llChangeSource.setOnClickListener(clickChangeSource);
    }
}
