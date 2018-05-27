//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.view.activity.ReadStyleActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;

public class ReadInterfacePop extends PopupWindow {

    @BindView(R.id.fl_line_smaller)
    TextView flLineSmaller;//行间距小
    @BindView(R.id.tv_dur_line_size)
    TextView tvDurLineSize;//行间距数字
    @BindView(R.id.fl_line_bigger)
    TextView flLineBigger;//行间距大
    @BindView(R.id.tv_convert_j)
    TextView tvConvertJ;
    @BindView(R.id.tv_convert_o)
    TextView tvConvertO;
    @BindView(R.id.tv_convert_f)
    TextView tvConvertF;
    @BindView(R.id.fl_text_Bold)
    TextView flTextBold;
    @BindView(R.id.fl_text_smaller)
    TextView flTextSmaller;//字号小
    @BindView(R.id.tv_dur_text_size)
    TextView tvDurTextSize;//字号数字
    @BindView(R.id.fl_text_bigger)
    TextView flTextBigger;//字号大
    @BindView(R.id.fl_text_font)
    TextView fl_text_font;

    @BindView(R.id.civ_bg_white)
    CircleImageView civBgWhite;
    @BindView(R.id.civ_bg_yellow)
    CircleImageView civBgYellow;
    @BindView(R.id.civ_bg_green)
    CircleImageView civBgGreen;
    @BindView(R.id.civ_bg_black)
    CircleImageView civBgBlack;
    @BindView(R.id.civ_bg_blue)
    CircleImageView civBgBlue;

    @BindView(R.id.tv0)
    TextView tv0;
    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.tv2)
    TextView tv2;
    @BindView(R.id.tv3)
    TextView tv3;
    @BindView(R.id.tv4)
    TextView tv4;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    public static final int RESULT_CHOOSEFONT_PERMS = 106;

    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public interface OnChangeProListener {
        void textSizeChange();

        void lineSizeChange();

        void bgChange();

        void setFont();

        void setConvert();

        void setBold();
    }

    private OnChangeProListener changeProListener;

    public ReadInterfacePop(ReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;

        View view = LayoutInflater.from(readBookActivity).inflate(R.layout.view_pop_read_interface, null);
        ImmersionBar.navigationBarPadding(activity, view);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(readBookActivity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setClippingEnabled(false);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void initData() {
        setBg();
        updateText(readBookControl.getTextKindIndex());
        updateBg(readBookControl.getTextDrawableIndex());
        updateLineSize(readBookControl.getLineMultiplier());
        updateBoldText(readBookControl.getTextBold());
        updateConvertText(readBookControl.getTextConvert());
        //upTextColor(readBookControl.getTextColorCustom());

    }

    private void bindEvent() {
        flTextSmaller.setOnClickListener(v -> {
            updateText(readBookControl.getTextKindIndex() - 1);
            changeProListener.textSizeChange();
        });
        flTextBigger.setOnClickListener(v -> {
            updateText(readBookControl.getTextKindIndex() + 1);
            changeProListener.textSizeChange();
        });
        flLineSmaller.setOnClickListener(v -> {
            updateLineSize((float) (readBookControl.getLineMultiplier() - 0.1));
            changeProListener.lineSizeChange();
        });
        flLineBigger.setOnClickListener(v -> {
            updateLineSize((float) (readBookControl.getLineMultiplier() + 0.1));
            changeProListener.lineSizeChange();
        });

        //繁简切换
        tvConvertF.setOnClickListener(view -> {
            readBookControl.setTextConvert(-1);
            updateConvertText(readBookControl.getTextConvert());
            changeProListener.setConvert();
        });
        tvConvertO.setOnClickListener(view -> {
            readBookControl.setTextConvert(0);
            updateConvertText(readBookControl.getTextConvert());
            changeProListener.setConvert();
        });
        tvConvertJ.setOnClickListener(view -> {
            readBookControl.setTextConvert(1);
            updateConvertText(readBookControl.getTextConvert());
            changeProListener.setConvert();
        });
        //加粗切换
        flTextBold.setOnClickListener(view -> {
            readBookControl.setTextBold(!readBookControl.getTextBold());
            updateBoldText(readBookControl.getTextBold());
            changeProListener.setBold();
        });

        civBgWhite.setOnClickListener(v -> {
            updateBg(0);
            changeProListener.bgChange();
        });
        civBgYellow.setOnClickListener(v -> {
            updateBg(1);
            changeProListener.bgChange();
        });
        civBgGreen.setOnClickListener(v -> {
            updateBg(2);
            changeProListener.bgChange();
        });
        civBgBlue.setOnClickListener(v -> {
            updateBg(3);
            changeProListener.bgChange();
        });

        civBgBlack.setOnClickListener(v -> {
            updateBg(4);
            changeProListener.bgChange();
        });
        civBgWhite.setOnLongClickListener(view -> {
            Intent intent = new Intent(activity, ReadStyleActivity.class);
            intent.putExtra("index", 0);
            activity.startActivityForResult(intent, activity.ResultStyleSet);
            return false;
        });
        civBgYellow.setOnLongClickListener(view -> {
            Intent intent = new Intent(activity, ReadStyleActivity.class);
            intent.putExtra("index", 1);
            activity.startActivityForResult(intent, activity.ResultStyleSet);
            return false;
        });
        civBgGreen.setOnLongClickListener(view -> {
            Intent intent = new Intent(activity, ReadStyleActivity.class);
            intent.putExtra("index", 2);
            activity.startActivityForResult(intent, activity.ResultStyleSet);
            return false;
        });
        civBgBlue.setOnLongClickListener(view -> {
            Intent intent = new Intent(activity, ReadStyleActivity.class);
            intent.putExtra("index", 3);
            activity.startActivityForResult(intent, activity.ResultStyleSet);
            return false;
        });
        civBgBlack.setOnLongClickListener(view -> {
            Intent intent = new Intent(activity, ReadStyleActivity.class);
            intent.putExtra("index", 4);
            activity.startActivityForResult(intent, activity.ResultStyleSet);
            return false;
        });

        //选择字体
        fl_text_font.setOnClickListener(view -> chooseReadBookFont());
        //长按清除字体
        fl_text_font.setOnLongClickListener(view -> {
            clearFontPath();
            Toast.makeText(activity, R.string.clear_font, Toast.LENGTH_SHORT).show();
            return true;
        });

    }

    private void chooseReadBookFont() {
        if (EasyPermissions.hasPermissions(activity, perms)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            activity.startActivityForResult(intent, activity.ResultSelectFont);
        } else {
            EasyPermissions.requestPermissions(activity, "选择字体",
                    RESULT_CHOOSEFONT_PERMS, perms);
        }
    }

    //设置字体
    public void setReadFonts(String path) {
        readBookControl.setReadBookFont(path);
        changeProListener.setFont();
    }

    private void clearFontPath() {
        readBookControl.setReadBookFont(null);
        changeProListener.setFont();
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

    @SuppressLint("DefaultLocale")
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

    private void updateConvertText(int convent) {

    }

    private void updateBoldText(Boolean convent) {
        if (convent) {
            flTextBold.setText("细");
        } else {
            flTextBold.setText("粗");
        }
    }

    public void setBg() {
        tv0.setTextColor(readBookControl.getTextColor(0));
        tv1.setTextColor(readBookControl.getTextColor(1));
        tv2.setTextColor(readBookControl.getTextColor(2));
        tv3.setTextColor(readBookControl.getTextColor(3));
        tv4.setTextColor(readBookControl.getTextColor(4));
        civBgWhite.setImageDrawable(readBookControl.getBgDrawable(0, activity));
        civBgYellow.setImageDrawable(readBookControl.getBgDrawable(1, activity));
        civBgGreen.setImageDrawable(readBookControl.getBgDrawable(2, activity));
        civBgBlue.setImageDrawable(readBookControl.getBgDrawable(3, activity));
        civBgBlack.setImageDrawable(readBookControl.getBgDrawable(4, activity));
    }

    private void updateBg(int index) {
        civBgWhite.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgYellow.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgGreen.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgBlack.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgBlue.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
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
                civBgBlue.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 4:
                civBgBlack.setBorderColor(Color.parseColor("#F3B63F"));
                break;
        }
        readBookControl.setTextDrawableIndex(index);
    }

}