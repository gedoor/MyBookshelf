//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.view.activity.ReadBookActivity;
import com.kunfei.bookshelf.view.activity.ReadStyleActivity;
import com.kunfei.bookshelf.widget.font.FontSelector;
import com.kunfei.bookshelf.widget.number.NumberButton;
import com.kunfei.bookshelf.widget.page.animation.PageAnimation;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReadInterfacePop extends FrameLayout {
    @BindView(R.id.vw_bg)
    View vwBg;
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
    @BindView(R.id.fl_indent)
    TextView tvIndent;
    @BindView(R.id.nbTipPaddingTop)
    NumberButton nbTipPaddingTop;
    @BindView(R.id.nbTipPaddingBottom)
    NumberButton nbTipPaddingBottom;
    @BindView(R.id.nbTipPaddingLeft)
    NumberButton nbTipPaddingLeft;
    @BindView(R.id.nbTipPaddingRight)
    NumberButton nbTipPaddingRight;
    @BindView(R.id.nbLetterSpacing)
    NumberButton nbLetterSpacing;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private Callback callback;

    public ReadInterfacePop(Context context) {
        super(context);
        init(context);
    }

    public ReadInterfacePop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadInterfacePop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.pop_read_interface, this);
        ButterKnife.bind(this, view);
        vwBg.setOnClickListener(null);
    }

    public void setListener(ReadBookActivity readBookActivity, @NonNull Callback callback) {
        this.activity = readBookActivity;
        this.callback = callback;
        initData();
        bindEvent();
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
                    callback.upTextSize();
                });

        nbLetterSpacing.setTitle(activity.getContext().getString(R.string.text_letter_spacing))
                .setNumberType(NumberButton.FLOAT)
                .setMinNumber(-0.5f)
                .setMaxNumber(0.5f)
                .setStepNumber(0.01f)
                .setFormat("0.00")
                .setNumber(readBookControl.getTextLetterSpacing())
                .setOnChangedListener(number -> {
                    readBookControl.setTextLetterSpacing(number);
                    callback.upTextSize();
                });

        nbLineSize.setTitle(activity.getString(R.string.line_size))
                .setNumberType(NumberButton.FLOAT)
                .setMinNumber(0.5f)
                .setMaxNumber(3f)
                .setStepNumber(0.1f)
                .setFormat("0.0")
                .setNumber(readBookControl.getLineMultiplier())
                .setOnChangedListener(number -> {
                    readBookControl.setLineMultiplier(number);
                    callback.upTextSize();
                });

        nbParagraphSize.setTitle(activity.getString(R.string.paragraph_size))
                .setNumberType(NumberButton.FLOAT)
                .setMinNumber(1f)
                .setMaxNumber(5f)
                .setStepNumber(0.1f)
                .setFormat("0.0")
                .setNumber(readBookControl.getParagraphSize())
                .setOnChangedListener(number -> {
                    readBookControl.setParagraphSize(number);
                    callback.upTextSize();
                });

        nbPaddingTop.setTitle(activity.getString(R.string.padding_top))
                .setMinNumber(0)
                .setMaxNumber(100)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingTop())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingTop((int) number);
                    callback.upMargin();
                });

        nbPaddingBottom.setTitle(activity.getString(R.string.padding_bottom))
                .setMinNumber(0)
                .setMaxNumber(100)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingBottom())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingBottom((int) number);
                    callback.upMargin();
                });

        nbPaddingLeft.setTitle(activity.getString(R.string.padding_left))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingLeft())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingLeft((int) number);
                    callback.upMargin();
                });

        nbPaddingRight.setTitle(activity.getString(R.string.padding_right))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getPaddingRight())
                .setOnChangedListener(number -> {
                    readBookControl.setPaddingRight((int) number);
                    callback.upMargin();
                });
        nbTipPaddingTop.setTitle(activity.getString(R.string.padding_top))
                .setMinNumber(0)
                .setMaxNumber(100)
                .setStepNumber(1)
                .setNumber(readBookControl.getTipPaddingTop())
                .setOnChangedListener(number -> {
                    readBookControl.setTipPaddingTop((int) number);
                    callback.upMargin();
                });

        nbTipPaddingBottom.setTitle(activity.getString(R.string.padding_bottom))
                .setMinNumber(0)
                .setMaxNumber(100)
                .setStepNumber(1)
                .setNumber(readBookControl.getTipPaddingBottom())
                .setOnChangedListener(number -> {
                    readBookControl.setTipPaddingBottom((int) number);
                    callback.upMargin();
                });

        nbTipPaddingLeft.setTitle(activity.getString(R.string.padding_left))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getTipPaddingLeft())
                .setOnChangedListener(number -> {
                    readBookControl.setTipPaddingLeft((int) number);
                    callback.upMargin();
                });

        nbTipPaddingRight.setTitle(activity.getString(R.string.padding_right))
                .setMinNumber(0)
                .setMaxNumber(50)
                .setStepNumber(1)
                .setNumber(readBookControl.getTipPaddingRight())
                .setOnChangedListener(number -> {
                    readBookControl.setTipPaddingRight((int) number);
                    callback.upMargin();
                });
    }

    /**
     * 控件事件
     */
    private void bindEvent() {
        //缩进
        tvIndent.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(activity, R.style.alertDialogTheme)
                    .setTitle(activity.getString(R.string.indent))
                    .setSingleChoiceItems(activity.getResources().getStringArray(R.array.indent),
                            readBookControl.getIndent(),
                            (dialogInterface, i) -> {
                                readBookControl.setIndent(i);
                                callback.refresh();
                                dialogInterface.dismiss();
                            })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
        //翻页模式
        tvPageMode.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity, R.style.alertDialogTheme)
                    .setTitle(activity.getString(R.string.page_mode))
                    .setSingleChoiceItems(PageAnimation.Mode.getAllPageMode(), readBookControl.getPageMode(), (dialogInterface, i) -> {
                        readBookControl.setPageMode(i);
                        updatePageMode(i);
                        callback.upPageMode();
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
        //加粗切换
        flTextBold.setOnClickListener(view -> {
            readBookControl.setTextBold(!readBookControl.getTextBold());
            updateBoldText(readBookControl.getTextBold());
            callback.refresh();
        });
        //背景选择
        civBgWhite.setOnClickListener(v -> {
            updateBg(0);
            callback.bgChange();
        });
        civBgYellow.setOnClickListener(v -> {
            updateBg(1);
            callback.bgChange();
        });
        civBgGreen.setOnClickListener(v -> {
            updateBg(2);
            callback.bgChange();
        });
        civBgBlue.setOnClickListener(v -> {
            updateBg(3);
            callback.bgChange();
        });
        civBgBlack.setOnClickListener(v -> {
            updateBg(4);
            callback.bgChange();
        });
        //自定义阅读样式
        civBgWhite.setOnLongClickListener(view -> customReadStyle(0));
        civBgYellow.setOnLongClickListener(view -> customReadStyle(1));
        civBgGreen.setOnLongClickListener(view -> customReadStyle(2));
        civBgBlue.setOnLongClickListener(view -> customReadStyle(3));
        civBgBlack.setOnLongClickListener(view -> customReadStyle(4));

        //选择字体
        fl_text_font.setOnClickListener(view -> {
            List<String> per = PermissionUtils.checkMorePermissions(activity, MApplication.PerList);
            if (per.isEmpty()) {
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
                Toast.makeText(activity, "本软件需要存储权限来存储备份书籍信息", Toast.LENGTH_SHORT).show();
                PermissionUtils.requestMorePermissions(activity, per, MApplication.RESULT__PERMS);
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
        callback.refresh();
    }

    //清除字体
    private void clearFontPath() {
        readBookControl.setReadBookFont(null);
        callback.refresh();
    }

    private void updatePageMode(int pageMode) {
        tvPageMode.setText(String.format(activity.getString(R.string.page_mode) + ":%s", PageAnimation.Mode.getPageMode(pageMode)));
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
        civBgWhite.setImageDrawable(readBookControl.getBgDrawable(0, activity, 100, 180));
        civBgYellow.setImageDrawable(readBookControl.getBgDrawable(1, activity, 100, 180));
        civBgGreen.setImageDrawable(readBookControl.getBgDrawable(2, activity, 100, 180));
        civBgBlue.setImageDrawable(readBookControl.getBgDrawable(3, activity, 100, 180));
        civBgBlack.setImageDrawable(readBookControl.getBgDrawable(4, activity, 100, 180));
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

    public interface Callback {
        void upPageMode();

        void upTextSize();

        void upMargin();

        void bgChange();

        void refresh();
    }

}