//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.databinding.PopReadInterfaceBinding;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.help.permission.Permissions;
import com.kunfei.bookshelf.help.permission.PermissionsCompat;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.view.activity.ReadBookActivity;
import com.kunfei.bookshelf.view.activity.ReadStyleActivity;
import com.kunfei.bookshelf.widget.font.FontSelector;
import com.kunfei.bookshelf.widget.page.animation.PageAnimation;

import kotlin.Unit;

public class ReadInterfacePop extends FrameLayout {

    private PopReadInterfaceBinding binding = PopReadInterfaceBinding.inflate(LayoutInflater.from(getContext()), this, true);
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
        binding.vwBg.setOnClickListener(null);
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

        binding.nbTextSize.setText(String.format("%d", readBookControl.getTextSize()));
    }

    /**
     * 控件事件
     */
    private void bindEvent() {
        //字号减
        binding.nbTextSizeDec.setOnClickListener(v -> {
            int fontSize = readBookControl.getTextSize() - 1;
            if (fontSize < 10) fontSize = 10;
            readBookControl.setTextSize(fontSize);
            binding.nbTextSize.setText(String.format("%d", readBookControl.getTextSize()));
            callback.upTextSize();
        });
        //字号加
        binding.nbTextSizeAdd.setOnClickListener(v -> {
            int fontSize = readBookControl.getTextSize() + 1;
            if (fontSize > 40) fontSize = 40;
            readBookControl.setTextSize(fontSize);
            binding.nbTextSize.setText(String.format("%d", readBookControl.getTextSize()));
            callback.upTextSize();
        });
        //缩进
        binding.flIndent.setOnClickListener(v -> {
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
        binding.tvPageMode.setOnClickListener(view -> {
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
        binding.flTextBold.setOnClickListener(view -> {
            readBookControl.setTextBold(!readBookControl.getTextBold());
            updateBoldText(readBookControl.getTextBold());
            callback.upTextSize();
        });
        //行距单倍
        binding.tvRowDef0.setOnClickListener(v -> {
            readBookControl.setLineMultiplier(0.6f);
            readBookControl.setParagraphSize(1.5f);
            callback.upTextSize();
        });
        //行距双倍
        binding.tvRowDef1.setOnClickListener(v -> {
            readBookControl.setLineMultiplier(1.2f);
            readBookControl.setParagraphSize(1.8f);
            callback.upTextSize();
        });
        //行距三倍
        binding.tvRowDef2.setOnClickListener(v -> {
            readBookControl.setLineMultiplier(1.8f);
            readBookControl.setParagraphSize(2.0f);
            callback.upTextSize();
        });
        //行距默认
        binding.tvRowDef.setOnClickListener(v -> {
            readBookControl.setLineMultiplier(1.0f);
            readBookControl.setParagraphSize(1.8f);
            callback.upTextSize();
        });
        //自定义间距
        binding.tvOther.setOnClickListener(v -> {
            activity.readAdjustMarginIn();
        });
        //背景选择
        binding.civBgWhite.setOnClickListener(v -> {
            updateBg(0);
            callback.bgChange();
        });
        binding.civBgYellow.setOnClickListener(v -> {
            updateBg(1);
            callback.bgChange();
        });
        binding.civBgGreen.setOnClickListener(v -> {
            updateBg(2);
            callback.bgChange();
        });
        binding.civBgBlue.setOnClickListener(v -> {
            updateBg(3);
            callback.bgChange();
        });
        binding.civBgBlack.setOnClickListener(v -> {
            updateBg(4);
            callback.bgChange();
        });
        //自定义阅读样式
        binding.civBgWhite.setOnLongClickListener(view -> customReadStyle(0));
        binding.civBgYellow.setOnLongClickListener(view -> customReadStyle(1));
        binding.civBgGreen.setOnLongClickListener(view -> customReadStyle(2));
        binding.civBgBlue.setOnLongClickListener(view -> customReadStyle(3));
        binding.civBgBlack.setOnLongClickListener(view -> customReadStyle(4));

        //选择字体
        binding.flTextFont.setOnClickListener(view -> {
            new PermissionsCompat.Builder(activity)
                    .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                    .rationale(R.string.get_storage_per)
                    .onGranted((requestCode) -> {
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
                        return Unit.INSTANCE;
                    })
                    .request();
        });

        //长按清除字体
        binding.flTextFont.setOnLongClickListener(view -> {
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
        binding.tvPageMode.setText(String.format("%s", PageAnimation.Mode.getPageMode(pageMode)));
    }

    private void updateBoldText(Boolean isBold) {
        binding.flTextBold.setSelected(isBold);
    }

    public void setBg() {
        binding.tv0.setTextColor(readBookControl.getTextColor(0));
        binding.tv1.setTextColor(readBookControl.getTextColor(1));
        binding.tv2.setTextColor(readBookControl.getTextColor(2));
        binding.tv3.setTextColor(readBookControl.getTextColor(3));
        binding.tv4.setTextColor(readBookControl.getTextColor(4));
        binding.civBgWhite.setImageDrawable(readBookControl.getBgDrawable(0, activity, 100, 180));
        binding.civBgYellow.setImageDrawable(readBookControl.getBgDrawable(1, activity, 100, 180));
        binding.civBgGreen.setImageDrawable(readBookControl.getBgDrawable(2, activity, 100, 180));
        binding.civBgBlue.setImageDrawable(readBookControl.getBgDrawable(3, activity, 100, 180));
        binding.civBgBlack.setImageDrawable(readBookControl.getBgDrawable(4, activity, 100, 180));
    }

    private void updateBg(int index) {
        binding.civBgWhite.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        binding.civBgYellow.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        binding.civBgGreen.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        binding.civBgBlack.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        binding.civBgBlue.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        switch (index) {
            case 0:
                binding.civBgWhite.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 1:
                binding.civBgYellow.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 2:
                binding.civBgGreen.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 3:
                binding.civBgBlue.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 4:
                binding.civBgBlack.setBorderColor(Color.parseColor("#F3B63F"));
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