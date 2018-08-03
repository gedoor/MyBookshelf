//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.help;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.widget.page.PageMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadBookControl {
    private static final int DEFAULT_TEXT = 3;
    private static final int DEFAULT_BG = 1;

    private List<Map<String, Integer>> textDrawable;

    private int speechRate;
    private boolean speechRateFollowSys;
    private int textSize;
    private float textExtra;
    private int textColor;
    private Drawable textBackground;
    private boolean bgIsColor;
    private int bgColor;
    private Bitmap bgBitmap;
    private float lineMultiplier;
    private int pageMode;

    private int textDrawableIndex = DEFAULT_BG;

    private Boolean hideStatusBar;
    private Boolean hideNavigationBar;
    private String fontPath;
    private int textConvert;
    private Boolean textBold;
    private Boolean canClickTurn;
    private Boolean canKeyTurn;
    private int clickSensitivity;
    private Boolean clickAllNext;
    private Boolean clickAnim;
    private Boolean showTitle;
    private Boolean showTimeBattery;
    private Boolean showLine;
    private long lineChange;
    private String lastNoteUrl;
    private Boolean darkStatusIcon;
    private int screenTimeOut;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;

    private SharedPreferences readPreference;
    private SharedPreferences defaultPreference;

    private static ReadBookControl readBookControl;

    public static ReadBookControl getInstance() {
        if (readBookControl == null) {
            synchronized (ReadBookControl.class) {
                if (readBookControl == null) {
                    readBookControl = new ReadBookControl();
                }
            }
        }
        return readBookControl;
    }

    private ReadBookControl() {
        initTextDrawable();
        readPreference = MApplication.getInstance().getSharedPreferences("CONFIG", 0);
        defaultPreference = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        this.hideStatusBar = defaultPreference.getBoolean("hide_status_bar", false);
        this.hideNavigationBar = defaultPreference.getBoolean("hide_navigation_bar", false);
        this.textSize = readPreference.getInt("textSize", 50);
        this.textExtra = readPreference.getFloat("textExtra", 1.0f);
        this.canClickTurn = readPreference.getBoolean("canClickTurn", true);
        this.canKeyTurn = readPreference.getBoolean("canKeyTurn", true);
        this.lineMultiplier = readPreference.getFloat("lineMultiplier", 1);
        this.clickSensitivity = readPreference.getInt("clickSensitivity", 50) > 100
                ? 50 : readPreference.getInt("clickSensitivity", 50);
        this.clickAllNext = readPreference.getBoolean("clickAllNext", false);
        this.clickAnim = readPreference.getBoolean("clickAnim", true);
        this.fontPath = readPreference.getString("fontPath", null);
        this.textConvert = readPreference.getInt("textConvertInt", 0);
        this.textBold = readPreference.getBoolean("textBold", false);
        this.speechRate = readPreference.getInt("speechRate", 10);
        this.speechRateFollowSys = readPreference.getBoolean("speechRateFollowSys", true);
        this.showTitle = readPreference.getBoolean("showTitle", true);
        this.showTimeBattery = readPreference.getBoolean("showTimeBattery", true);
        this.showLine = readPreference.getBoolean("showLine", true);
        this.lineChange = readPreference.getLong("lineChange", System.currentTimeMillis());
        this.lastNoteUrl = readPreference.getString("lastNoteUrl", "");
        this.screenTimeOut = readPreference.getInt("screenTimeOut", 0);
        this.paddingLeft = readPreference.getInt("paddingLeft", 0);
        this.paddingTop = readPreference.getInt("paddingTop", 0);
        this.paddingRight = readPreference.getInt("paddingRight", 0);
        this.paddingBottom = readPreference.getInt("paddingBottom", 0);
        this.pageMode = readPreference.getInt("pageMode", 0);

        initTextDrawableIndex();
    }

    public void initTextDrawableIndex() {
        if (getIsNightTheme()) {
            textDrawableIndex = readPreference.getInt("textDrawableIndexNight", 4);
        } else {
            textDrawableIndex = readPreference.getInt("textDrawableIndex", DEFAULT_BG);
        }
        if (textDrawableIndex == -1) {
            textDrawableIndex = DEFAULT_BG;
        }
        initPageStyle(MApplication.getInstance());
        setTextDrawable(MApplication.getInstance());
    }

    private void initPageStyle(Context context) {
        ACache aCache = ACache.get(context);
        bgColor = textDrawable.get(textDrawableIndex).get("textBackground");
        switch (getBgCustom(textDrawableIndex)) {
            case 2:
                Bitmap bitmap = aCache.getAsBitmap("customBg" + textDrawableIndex);
                if (bitmap != null) {
                    bgIsColor = false;
                    bgBitmap = bitmap;
                    return;
                }
                break;
            case 1:
                bgIsColor = true;
                bgColor = getBgColor(textDrawableIndex);
                return;
        }
        if (textDrawable.get(textDrawableIndex).get("bgIsColor") != 0) {
            bgIsColor = true;
            bgColor = textDrawable.get(textDrawableIndex).get("textBackground");
        } else {
            bgIsColor = false;
            bgBitmap = BitmapFactory.decodeResource(context.getResources(), getDefaultBg(textDrawableIndex));
        }
    }

    private void setTextDrawable(Context context) {
        darkStatusIcon = getDarkStatusIcon(textDrawableIndex);
        textColor = getTextColor(textDrawableIndex);
        textBackground = getBgDrawable(textDrawableIndex, context);
    }

    public int getTextColor(int textDrawableIndex) {
        if (readPreference.getInt("textColor" + textDrawableIndex, 0) != 0) {
            return readPreference.getInt("textColor" + textDrawableIndex, 0);
        } else {
            return getDefaultTextColor(textDrawableIndex);
        }
    }

    public void setTextColor(int textDrawableIndex, int textColor) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textColor" + textDrawableIndex, textColor);
        editor.apply();
    }

    public Drawable getBgDrawable(int textDrawableIndex, Context context) {
        ACache aCache = ACache.get(context);
        switch (getBgCustom(textDrawableIndex)) {
            case 2:
                Bitmap bitmap = aCache.getAsBitmap("customBg" + textDrawableIndex);
                if (bitmap != null) {
                    return new BitmapDrawable(context.getResources(), bitmap);
                }
                break;
            case 1:
                bgColor = getBgColor(textDrawableIndex);
                return new ColorDrawable(bgColor);
        }
        if (textDrawable.get(textDrawableIndex).get("bgIsColor") != 0) {
            bgColor = textDrawable.get(textDrawableIndex).get("textBackground");
            return new ColorDrawable(bgColor);
        } else {
            return getDefaultBgDrawable(textDrawableIndex, context);
        }
    }

    public Drawable getDefaultBgDrawable(int textDrawableIndex, Context context) {
        if (textDrawable.get(textDrawableIndex).get("bgIsColor") != 0) {
            return new ColorDrawable(textDrawable.get(textDrawableIndex).get("textBackground"));
        } else {
            return context.getResources().getDrawable(getDefaultBg(textDrawableIndex));
        }
    }

    public int getBgCustom(int textDrawableIndex) {
        return readPreference.getInt("bgCustom" + textDrawableIndex, 0);
    }

    public void setBgCustom(int textDrawableIndex, int bgCustom) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("bgCustom" + textDrawableIndex, bgCustom);
        editor.apply();
    }

    public int getDefaultTextColor(int textDrawableIndex) {
        return textDrawable.get(textDrawableIndex).get("textColor");
    }

    private int getDefaultBg(int textDrawableIndex) {
        return textDrawable.get(textDrawableIndex).get("textBackground");
    }

    public int getBgColor(int index) {
        return readPreference.getInt("bgColor" + index, Color.parseColor("#1e1e1e"));
    }

    public void setBgColor(int index, int bgColor) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("bgColor" + index, bgColor);
        editor.apply();
    }

    public boolean getIsNightTheme() {
        return defaultPreference.getBoolean("nightTheme", false);
    }

    public boolean getImmersionStatusBar() {
        return defaultPreference.getBoolean("immersionStatusBar", false);
    }

    public void setImmersionStatusBar(boolean immersionStatusBar) {
        SharedPreferences.Editor editor = defaultPreference.edit();
        editor.putBoolean("immersionStatusBar", immersionStatusBar);
        editor.apply();
    }

    public String getLastNoteUrl() {
        return lastNoteUrl;
    }

    public void setLastNoteUrl(String lastNoteUrl) {
        this.lastNoteUrl = lastNoteUrl;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putString("lastNoteUrl", lastNoteUrl);
        editor.apply();
    }

    //阅读背景
    private void initTextDrawable() {
        if (null == textDrawable) {
            textDrawable = new ArrayList<>();
            Map<String, Integer> temp1 = new HashMap<>();
            temp1.put("textColor", Color.parseColor("#3E3D3B"));
            temp1.put("bgIsColor", 1);
            temp1.put("textBackground", Color.parseColor("#F3F3F3"));
            temp1.put("darkStatusIcon", 1);
            textDrawable.add(temp1);

            Map<String, Integer> temp2 = new HashMap<>();
            temp2.put("textColor", Color.parseColor("#5E432E"));
            temp2.put("bgIsColor", 1);
            temp2.put("textBackground", Color.parseColor("#FFF2E2"));
            temp2.put("darkStatusIcon", 1);
            textDrawable.add(temp2);

            Map<String, Integer> temp3 = new HashMap<>();
            temp3.put("textColor", Color.parseColor("#22482C"));
            temp3.put("bgIsColor", 1);
            temp3.put("textBackground", Color.parseColor("#E1F1DA"));
            temp3.put("darkStatusIcon", 1);
            textDrawable.add(temp3);

            Map<String, Integer> temp4 = new HashMap<>();
            temp4.put("textColor", Color.parseColor("#FFFFFF"));
            temp4.put("bgIsColor", 1);
            temp4.put("textBackground", Color.parseColor("#015A86"));
            temp4.put("darkStatusIcon", 0);
            textDrawable.add(temp4);

            Map<String, Integer> temp5 = new HashMap<>();
            temp5.put("textColor", Color.parseColor("#808080"));
            temp5.put("bgIsColor", 1);
            temp5.put("textBackground", Color.parseColor("#000000"));
            temp5.put("darkStatusIcon", 0);
            textDrawable.add(temp5);
        }
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textSize", textSize);
        editor.apply();
    }

    public float getTextExtra() {
        return textExtra;
    }

    public int getTextColor() {
        return textColor;
    }

    public boolean bgIsColor() {
        return bgIsColor;
    }

    public Drawable getTextBackground() {
        return textBackground;
    }

    public int getBgColor() {
        return bgColor;
    }

    public Bitmap getBgBitmap() {
        return bgBitmap;
    }

    public int getTextDrawableIndex() {
        return textDrawableIndex;
    }

    public void setTextDrawableIndex(int textDrawableIndex) {
        this.textDrawableIndex = textDrawableIndex;
        SharedPreferences.Editor editor = readPreference.edit();
        if (getIsNightTheme()) {
            editor.putInt("textDrawableIndexNight", textDrawableIndex);
        } else {
            editor.putInt("textDrawableIndex", textDrawableIndex);
        }
        editor.apply();
        setTextDrawable(MApplication.getInstance());
    }

    public void setTextConvert(int textConvert) {
        this.textConvert = textConvert;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textConvertInt", textConvert);
        editor.apply();
    }

    public void setTextBold(boolean textBold) {
        this.textBold = textBold;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("textBold", textBold);
        editor.apply();
    }

    public void setReadBookFont(String fontPath) {
        this.fontPath = fontPath;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putString("fontPath", fontPath);
        editor.apply();
    }

    public String getFontPath() {
        return fontPath;
    }

    public int getTextConvert() {
        return textConvert == -1 ? 2 : textConvert;
    }

    public Boolean getTextBold() {
        return textBold;
    }

    public List<Map<String, Integer>> getTextDrawable() {
        return textDrawable;
    }

    public Boolean getCanKeyTurn() {
        return canKeyTurn;
    }

    public void setCanKeyTurn(Boolean canKeyTurn) {
        this.canKeyTurn = canKeyTurn;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("canKeyTurn", canKeyTurn);
        editor.apply();
    }

    public Boolean getCanClickTurn() {
        return canClickTurn;
    }

    public void setCanClickTurn(Boolean canClickTurn) {
        this.canClickTurn = canClickTurn;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("canClickTurn", canClickTurn);
        editor.apply();
    }

    public float getLineMultiplier() {
        return lineMultiplier;
    }

    public void setLineMultiplier(float lineMultiplier) {
        this.lineMultiplier = lineMultiplier;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putFloat("lineMultiplier", lineMultiplier);
        editor.apply();
    }

    public int getClickSensitivity() {
        return clickSensitivity;
    }

    public void setClickSensitivity(int clickSensitivity) {
        this.clickSensitivity = clickSensitivity;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("clickSensitivity", clickSensitivity);
        editor.apply();
    }

    public Boolean getClickAllNext() {
        return clickAllNext;
    }

    public void setClickAllNext(Boolean clickAllNext) {
        this.clickAllNext = clickAllNext;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("clickAllNext", clickAllNext);
        editor.apply();
    }

    public Boolean getClickAnim() {
        return clickAnim;
    }

    public void setClickAnim(Boolean clickAnim) {
        this.clickAnim = clickAnim;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("clickAnim", clickAnim);
        editor.apply();
    }

    public int getSpeechRate() {
        return speechRate;
    }

    public void setSpeechRate(int speechRate) {
        this.speechRate = speechRate;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("speechRate", speechRate);
        editor.apply();
    }

    public boolean isSpeechRateFollowSys() {
        return speechRateFollowSys;
    }

    public void setSpeechRateFollowSys(boolean speechRateFollowSys) {
        this.speechRateFollowSys = speechRateFollowSys;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("speechRateFollowSys", speechRateFollowSys);
        editor.apply();
    }

    public Boolean getShowTitle() {
        return showTitle;
    }

    public void setShowTitle(Boolean showTitle) {
        this.showTitle = showTitle;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("showTitle", showTitle);
        editor.apply();
    }

    public Boolean getShowTimeBattery() {
        return showTimeBattery;
    }

    public void setShowTimeBattery(Boolean showTimeBattery) {
        this.showTimeBattery = showTimeBattery;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("showTimeBattery", showTimeBattery);
        editor.apply();
    }

    public Boolean getHideStatusBar() {
        return hideStatusBar;
    }

    public void setHideStatusBar(Boolean hideStatusBar) {
        this.hideStatusBar = hideStatusBar;
        SharedPreferences.Editor editor = defaultPreference.edit();
        editor.putBoolean("hide_status_bar", hideStatusBar);
        editor.apply();
    }

    public Boolean getHideNavigationBar() {
        return hideNavigationBar;
    }

    public void setHideNavigationBar(Boolean hideNavigationBar) {
        this.hideNavigationBar = hideNavigationBar;
        SharedPreferences.Editor editor = defaultPreference.edit();
        editor.putBoolean("hide_navigation_bar", hideNavigationBar);
        editor.apply();
    }

    public Boolean getShowLine() {
        return showLine;
    }

    public void setShowLine(Boolean showLine) {
        this.showLine = showLine;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("showLine", showLine);
        editor.apply();
    }

    public long getLineChange() {
        return lineChange;
    }

    public void setLineChange(long lineChange) {
        this.lineChange = lineChange;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putLong("lineChange", lineChange);
        editor.apply();
    }

    public boolean getDarkStatusIcon() {
        return darkStatusIcon;
    }

    public boolean getDarkStatusIcon(int textDrawableIndex) {
        return defaultPreference.getBoolean("darkStatusIcon" + textDrawableIndex,  textDrawable.get(textDrawableIndex).get("darkStatusIcon") != 0 );
    }

    public void setDarkStatusIcon(int textDrawableIndex, Boolean darkStatusIcon) {
        SharedPreferences.Editor editor = defaultPreference.edit();
        editor.putBoolean("darkStatusIcon" + textDrawableIndex, darkStatusIcon);
        editor.apply();
    }

    public int getScreenTimeOut() {
        return screenTimeOut;
    }

    public void setScreenTimeOut(int screenTimeOut) {
        this.screenTimeOut = screenTimeOut;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("screenTimeOut", screenTimeOut);
        editor.apply();
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingLeft", paddingLeft);
        editor.apply();
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingTop", paddingTop);
        editor.apply();
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingRight", paddingRight);
        editor.apply();
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingBottom", paddingBottom);
        editor.apply();
    }

    public int getPageMode() {
        return pageMode;
    }

    public PageMode getPageMode(int pageMode) {
        switch (pageMode) {
            case 0:
                return PageMode.COVER;
            case 1:
                return PageMode.SIMULATION;
            case 2:
                return PageMode.SCROLL;
            case 3:
                return PageMode.NONE;
            default:
                return PageMode.COVER;
        }
    }

    public void setPageMode(int pageMode) {
        this.pageMode = pageMode;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("pageMode", pageMode);
        editor.apply();
    }
}
