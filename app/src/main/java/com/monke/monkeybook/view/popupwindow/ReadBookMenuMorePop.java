package com.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import com.monke.monkeybook.R;

public class ReadBookMenuMorePop extends PopupWindow{
    private Context mContext;
    private View view;

    private LinearLayout llDownload;

    public ReadBookMenuMorePop(Context context){
        super(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mContext = context;
        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_menumore,null);
        this.setContentView(view);

        initView();

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowmenumore);
    }

    private void initView() {
        llDownload = (LinearLayout) view.findViewById(R.id.ll_download);
    }

    public void setOnClickDownload(View.OnClickListener clickDownload){
        llDownload.setOnClickListener(clickDownload);
    }
}
