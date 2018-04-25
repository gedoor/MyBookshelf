//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.view.activity.ReadBookActivity;

import java.io.IOException;

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
    @BindView(R.id.fl_text_convert)
    TextView flTextConvert;
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

    @BindView(R.id.civ_bg_custom)
    TextView civBgCustom;
    @BindView(R.id.civ_text_color)
    TextView civTextColor;
    @BindView(R.id.tv_background_color)
    TextView tvBackgroundColor;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl;

    public static final int RESULT_CHOOSEFONT_PERMS = 106;

    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public interface OnChangeProListener {
        void textSizeChange(int index);

        void lineSizeChange(float lineMultiplier);

        void bgChange(int index);

        void setFont(String path);

        void setConvert();

        void setBold();
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
        updateBoldText(readBookControl.getTextBold());
        updateConvertText(readBookControl.getTextConvert());
        //upTextColor(readBookControl.getTextColorCustom());
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

        //繁简切换
        flTextConvert.setOnClickListener(v -> {
            readBookControl.setTextConvert(!readBookControl.getTextConvert());
            updateConvertText(readBookControl.getTextConvert());
            changeProListener.setConvert();
        });
        //加粗切换
        flTextBold.setOnClickListener(view -> {
            readBookControl.setTextBold(!readBookControl.getTextBold());
            updateBoldText(readBookControl.getTextBold());
            changeProListener.setBold();
        });
        tvBackgroundColor.setOnClickListener(view1 -> ColorPickerDialogBuilder
                .with(activity)
                .setTitle("选择背景颜色")
                .initialColor(readBookControl.getTextColorCustom())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {

                })
                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                    readBookControl.setBackgroundColorCustom(selectedColor);
                    //upTextColor(selectedColor);
                    changeProListener.bgChange(selectedColor);
                })
                .setNegativeButton("cancel", (dialog, which) -> {

                })
                .build()
                .show());

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
        civBgBlue.setOnClickListener(v -> {
            updateBg(3);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        });

        civBgBlack.setOnClickListener(v -> {
            updateBg(4);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        });
        //选择字体
        fl_text_font.setOnClickListener(view -> {
            chooseReadBookFont();
        });
        //长按清除字体
        fl_text_font.setOnLongClickListener(view -> {
            clearFontPath();
            Toast.makeText(activity, R.string.clear_font, Toast.LENGTH_SHORT).show();
            return true;
        });

        civBgCustom.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            activity.startActivityForResult(intent, activity.ResultSelectBg);
        });
        civTextColor.setOnClickListener(view -> ColorPickerDialogBuilder
                .with(activity)
                .setTitle("选择文字颜色")
                .initialColor(readBookControl.getTextColorCustom())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {

                })
                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                    readBookControl.setTextColorCustom(selectedColor);
                    //upTextColor(selectedColor);
                    changeProListener.bgChange(selectedColor);
                })
                .setNegativeButton("cancel", (dialog, which) -> {

                })
                .build()
                .show());
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

    /**
     * 自定义背景
     */
    public void setCustomBg(Uri uri) {
        ContentResolver cr = activity.getContentResolver();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
            bitmap = getSmallerBitmap(bitmap);
            ACache aCache = ACache.get(activity);
            aCache.put("customBg", bitmap);
            updateBg(-1);
            changeProListener.bgChange(readBookControl.getTextDrawableIndex());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getSmallerBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() / 360000;
        if (size <= 1) return bitmap; // 如果小于
        else {
            Matrix matrix = new Matrix();
            matrix.postScale((float) (1 / Math.sqrt(size)), (float) (1 / Math.sqrt(size)));
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
    }

    //设置字体
    public void setReadFonts(String path) {
        changeProListener.setFont(readBookControl.setReadBookFont(path));
    }

    private void clearFontPath() {
        changeProListener.setFont(readBookControl.setReadBookFont(null));
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

    @SuppressLint("DefaultLocale")
    private void updateLineNum(int lineNum) {
        if (lineNum > 3) {
            lineNum = -1;
        }
//        tvDurLineNum.setText(String.format("%d", lineNum));
        readBookControl.setLineNum(lineNum);
    }

    private void updateConvertText(Boolean convent) {
        if (convent) {
            flTextConvert.setText("简");
        } else {
            flTextConvert.setText("繁");
        }
    }

    private void updateBoldText(Boolean convent) {
        if (convent) {
            flTextBold.setText("细");
        } else {
            flTextBold.setText("粗");
        }
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
            /*default:
                civBgCustom.setBorderColor(Color.parseColor("#F3B63F"));
                break;*/
        }
        readBookControl.setTextDrawableIndex(index);
    }

}