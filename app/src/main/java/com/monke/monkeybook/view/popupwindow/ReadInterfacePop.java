//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.view.activity.ReadBookActivity;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReadInterfacePop extends PopupWindow {

    @BindView(R.id.fl_line_smaller)
    FrameLayout flLineSmaller;
    @BindView(R.id.tv_dur_line_size)
    TextView tvDurLineSize;
    @BindView(R.id.fl_line_bigger)
    FrameLayout flLineBigger;
    @BindView(R.id.tv_dur_line_num)
    TextView tvDurLineNum;
    @BindView(R.id.fl_line_num)
    FrameLayout flLineNum;
    @BindView(R.id.fl_text_smaller)
    FrameLayout flTextSmaller;
    @BindView(R.id.tv_dur_text_size)
    TextView tvDurTextSize;
    @BindView(R.id.fl_text_bigger)
    FrameLayout flTextBigger;
    @BindView(R.id.civ_bg_white)
    CircleImageView civBgWhite;
    @BindView(R.id.civ_bg_yellow)
    CircleImageView civBgYellow;
    @BindView(R.id.civ_bg_green)
    CircleImageView civBgGreen;
    @BindView(R.id.civ_bg_black)
    CircleImageView civBgBlack;
    @BindView(R.id.civ_bg_custom)
    CircleImageView civBgCustom;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl;

    public interface OnChangeProListener {
        void textSizeChange(int index);

        void lineSizeChange(float lineMultiplier);

        void bgChange(int index);
    }

    private OnChangeProListener changeProListener;

    public ReadInterfacePop(ReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;

        View view = LayoutInflater.from(readBookActivity).inflate(R.layout.view_pop_read_interface, null);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(readBookActivity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void initData() {
        readBookControl = ReadBookControl.getInstance();
        updateText(readBookControl.getTextKindIndex());
        updateBg(readBookControl.getTextDrawableIndex());
        updateLineSize(readBookControl.getLineMultiplier());
        updateLineNum(readBookControl.getLineNum());
    }

    private void bindEvent() {
        flTextSmaller.setOnClickListener(v -> {
            updateText(readBookControl.getTextKindIndex() - 1);
            changeProListener.textSizeChange(readBookControl.getTextKindIndex());
        });
        flTextBigger.setOnClickListener(v -> {
            updateText(readBookControl.getTextKindIndex() + 1);
            changeProListener.textSizeChange(readBookControl.getTextKindIndex());
        });
        flLineSmaller.setOnClickListener(v -> {
            updateLineSize((float) (readBookControl.getLineMultiplier() - 0.1));
            changeProListener.lineSizeChange(readBookControl.getLineMultiplier());
        });
        flLineBigger.setOnClickListener(v -> {
            updateLineSize((float) (readBookControl.getLineMultiplier() + 0.1));
            changeProListener.lineSizeChange(readBookControl.getLineMultiplier());
        });
        flLineNum.setOnClickListener(view1 -> {
            updateLineNum(readBookControl.getLineNum() + 1);
            changeProListener.lineSizeChange(readBookControl.getLineMultiplier());
        });


        civBgWhite.setOnClickListener(v -> {
            updateBg(0);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        });
        civBgYellow.setOnClickListener(v -> {
            updateBg(1);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        });
        civBgGreen.setOnClickListener(v -> {
            updateBg(2);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        });
        civBgBlack.setOnClickListener(v -> {
            updateBg(3);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        });
        civBgCustom.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            activity.startActivityForResult(intent, activity.ResultSelectBg);
        });
    }

    public void setCustomBg(Uri uri) {
        ContentResolver cr = activity.getContentResolver();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
            ACache aCache = ACache.get(activity);
            aCache.put("customBg", bitmap);
            updateBg(4);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateText(int textKindIndex) {
        if (textKindIndex == 0) {
            flTextSmaller.setEnabled(false);
            flTextBigger.setEnabled(true);
        } else if (textKindIndex == readBookControl.getTextKind().size() - 1) {
            flTextSmaller.setEnabled(true);
            flTextBigger.setEnabled(false);
        } else {
            flTextSmaller.setEnabled(true);
            flTextBigger.setEnabled(true);
        }

        tvDurTextSize.setText(String.valueOf(readBookControl.getTextKind().get(textKindIndex).get("textSize")));
        readBookControl.setTextKindIndex(textKindIndex);
    }

    private void updateLineSize(float lineSize) {
        if (lineSize > 2) {
            lineSize = 2;
        }
        if (lineSize < 0.5) {
            lineSize = 0.5f;
        }
        tvDurLineSize.setText(String.format("%.1f", lineSize));
        readBookControl.setLineMultiplier(lineSize);
    }

    private void updateLineNum(int linenum) {
        if (linenum > 2) {
            linenum = -1;
        }
        tvDurLineNum.setText(String.format("%d", linenum));
        readBookControl.setLineNum(linenum);
    }

    private void updateBg(int index) {
        civBgWhite.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgYellow.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgGreen.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgBlack.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgCustom.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        switch (index) {
            case 0:
                civBgWhite.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 1:
                civBgYellow.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 2:
                civBgGreen.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 3:
                civBgBlack.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            default:
                civBgCustom.setBorderColor(Color.parseColor("#F3B63F"));
                break;
        }
        readBookControl.setTextDrawableIndex(index);
    }

}