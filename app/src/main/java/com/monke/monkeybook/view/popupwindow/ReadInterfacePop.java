//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.view.activity.ReadStyleActivity;
import com.monke.monkeybook.widget.font.FontSelector;
import com.monke.monkeybook.widget.number.NumberButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;

public class ReadInterfacePop extends PopupWindow {

    @BindView(R.id.fl_text_Bold)
    TextView flTextBold;
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
    @BindView(R.id.nbTextSize)
    NumberButton nbTextSize;
    @BindView(R.id.nbLineSize)
    NumberButton nbLineSize;
    @BindView(R.id.nbParagraphSize)
    NumberButton nbParagraphSize;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
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

        setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setClippingEnabled(false);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void initData() {
        setBg();
        updateBg(readBookControl.getTextDrawableIndex());
        updateBoldText(readBookControl.getTextBold());
        updatePageMode(readBookControl.getPageMode());

        nbTextSize.setTitle(activity.getString(R.string.text_size))
                .setMinNumber(10)
                .setMaxNumber(40)
                .setNumber(readBookControl.getTextSize())
                .setOnChangedListener(number -> {
                    readBookControl.setTextSize((int) number);
                    changeProListener.upTextSize();
                });

        nbLineSize.setTitle(activity.getString(R.string.line_size))
                .setNumberType(NumberButton.FLOAT)
                .setMinNumber(0.5f)
                .setMaxNumber(2f)
                .setStepNumber(0.1f)
                .setFormat("0.0")
                .setNumber(readBookControl.getLineMultiplier())
                .setOnChangedListener(number -> {
                    readBookControl.setLineMultiplier(number);
                    changeProListener.upTextSize();
                });

        nbParagraphSize.setTitle(activity.getString(R.string.paragraph_size))
                .setNumberType(NumberButton.FLOAT)
                .setMinNumber(1f)
                .setMaxNumber(3f)
                .setStepNumber(0.1f)
                .setFormat("0.0")
                .setNumber(readBookControl.getParagraphSize())
                .setOnChangedListener(number -> {
                    readBookControl.setParagraphSize(number);
                    changeProListener.upTextSize();
                });

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
        //翻页模式
        tvPageMode.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity, R.style.alertDialogTheme)
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
        fl_text_font.setOnClickListener(view -> {
            if (EasyPermissions.hasPermissions(activity, MApplication.PerList)) {
                new FontSelector(activity, readBookControl.getFontPath())
                        .setListener(new FontSelector.OnThisListener() {
                            @Override
                            public void setDefault() {
                                clearFontPath();
                            }

                            @Override
                            public void setFontPath(String fontPath) {
                                setReadFonts(fontPath);
                            }
                        })
                        .create()
                        .show();
            } else {
                EasyPermissions.requestPermissions(activity, "读取字体需要存储权限", MApplication.RESULT__PERMS, MApplication.PerList);
            }
        });

        //长按清除字体
        fl_text_font.setOnLongClickListener(view -> {
            clearFontPath();
            activity.toast(R.string.clear_font);
            return true;
        });

    }

    //自定义阅读样式
    private boolean customReadStyle(int index) {
        Intent intent = new Intent(activity, ReadStyleActivity.class);
        intent.putExtra("index", index);
        activity.startActivity(intent);
        return false;
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

    private void updatePageMode(int pageMode) {
        tvPageMode.setText(String.format(activity.getString(R.string.page_mode) + ":%s", activity.getResources().getStringArray(R.array.page_mode)[pageMode]));
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

    public interface OnChangeProListener {
        void upPageMode();

        void upTextSize();

        void upMargin();

        void bgChange();

        void refresh();
    }

}