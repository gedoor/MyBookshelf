//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.monke.monkeybook.widget.NumberButton;

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
    @BindView(R.id.nbPaddingTop)
    NumberButton nbPaddingTop;
    @BindView(R.id.nbPaddingBottom)
    NumberButton nbPaddingBottom;
    @BindView(R.id.nbPaddingLeft)
    NumberButton nbPaddingLeft;
    @BindView(R.id.nbPaddingRight)
    NumberButton nbPaddingRight;
    @BindView(R.id.tvPageMode)
    TextView tvPageMode;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    public static final int RESULT_CHOOSEFONT_PERMS = 106;

    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public interface OnChangeProListener {
        void upPageMode();
        void upTextSize();
        void upMargin();
        void bgChange();
        void refresh();
    }

    private OnChangeProListener changeProListener;

    public ReadInterfacePop(ReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;

        View view = LayoutInflater.from(readBookActivity).inflate(R.layout.pop_read_interface, null);
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
        updateText(readBookControl.getTextSize(), false);
        updateLineSize(readBookControl.getLineMultiplier(), false);
        updateBg(readBookControl.getTextDrawableIndex());
        updateBoldText(readBookControl.getTextBold());
        updatePageMode(readBookControl.getPageMode());

        nbPaddingTop.setTitle(activity.getString(R.string.padding_top))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingTop())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingTop((int) number);
                    changeProListener.upMargin();
                });

        nbPaddingBottom.setTitle(activity.getString(R.string.padding_bottom))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingBottom())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingBottom((int) number);
                    changeProListener.upMargin();
                });

        nbPaddingLeft.setTitle(activity.getString(R.string.padding_left))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingLeft())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingLeft((int) number);
                    changeProListener.upMargin();
                });

        nbPaddingRight.setTitle(activity.getString(R.string.padding_right))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingRight())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingRight((int) number);
                    changeProListener.upMargin();
                });
    }

    private void bindEvent() {
        flTextSmaller.setOnClickListener(v -> {
            updateText(readBookControl.getTextSize() - 1, true);
        });
        flTextBigger.setOnClickListener(v -> {
            updateText(readBookControl.getTextSize() + 1, true);
        });
        flLineSmaller.setOnClickListener(v -> {
            updateLineSize((float) (readBookControl.getLineMultiplier() - 0.1), true);
        });
        flLineBigger.setOnClickListener(v -> {
            updateLineSize((float) (readBookControl.getLineMultiplier() + 0.1), true);
        });

        //翻页模式
        tvPageMode.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.page_mode))
                    .setSingleChoiceItems(activity.getResources().getStringArray(R.array.page_mode), readBookControl.getPageMode(), (dialogInterface, i) -> {
                        readBookControl.setPageMode(i);
                        updatePageMode(i);
                        changeProListener.upPageMode();
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
        });
        //加粗切换
        flTextBold.setOnClickListener(view -> {
            readBookControl.setTextBold(!readBookControl.getTextBold());
            updateBoldText(readBookControl.getTextBold());
            changeProListener.refresh();
        });
        //背景选择
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
        //自定义阅读样式
        civBgWhite.setOnLongClickListener(view -> customReadStyle(0));
        civBgYellow.setOnLongClickListener(view -> customReadStyle(1));
        civBgGreen.setOnLongClickListener(view -> customReadStyle(2));
        civBgBlue.setOnLongClickListener(view -> customReadStyle(3));
        civBgBlack.setOnLongClickListener(view -> customReadStyle(4));

        //选择字体
        fl_text_font.setOnClickListener(view -> chooseReadBookFont());
        //长按清除字体
        fl_text_font.setOnLongClickListener(view -> {
            clearFontPath();
            Toast.makeText(activity, R.string.clear_font, Toast.LENGTH_SHORT).show();
            return true;
        });

    }

    //自定义阅读样式
    private boolean customReadStyle(int index) {
        Intent intent = new Intent(activity, ReadStyleActivity.class);
        intent.putExtra("index", index);
        activity.startActivityForResult(intent, activity.ResultStyleSet);
        return false;
    }

    //选择字体
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
        changeProListener.refresh();
    }

    //清除字体
    private void clearFontPath() {
        readBookControl.setReadBookFont(null);
        changeProListener.refresh();
    }

    private void updateText(int textSize, boolean refresh) {
        if (textSize < 10) {
            flTextSmaller.setEnabled(false);
            flTextBigger.setEnabled(true);
        } else if (textSize > 40) {
            flTextSmaller.setEnabled(true);
            flTextBigger.setEnabled(false);
        } else {
            flTextSmaller.setEnabled(true);
            flTextBigger.setEnabled(true);
        }
        tvDurTextSize.setText(String.valueOf(textSize));
        if (refresh) {
            readBookControl.setTextSize(textSize);
            changeProListener.upTextSize();
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateLineSize(float lineSize, boolean refresh) {
        if (lineSize > 2) {
            lineSize = 2;
        }
        if (lineSize < 0.5) {
            lineSize = 0.5f;
        }
        tvDurLineSize.setText(String.format("%.1f", lineSize));
        if (refresh) {
            readBookControl.setLineMultiplier(lineSize);
            changeProListener.upTextSize();
        }
    }

    private void updatePageMode(int pageMode) {
        tvPageMode.setText(String.format(activity.getString(R.string.page_mode)+":%s", activity.getResources().getStringArray(R.array.page_mode)[pageMode]));
    }

    private void updateBoldText(Boolean isBold) {
        flTextBold.setSelected(isBold);
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