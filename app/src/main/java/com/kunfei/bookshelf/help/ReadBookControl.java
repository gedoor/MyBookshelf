//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.help;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.DisplayMetrics;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.utils.BitmapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kunfei.bookshelf.widget.page.PageLoader.DEFAULT_MARGIN_WIDTH;

public class ReadBookControl {
    private static final int DEFAULT_BG = 1;
    private int textDrawableIndex = DEFAULT_BG;
    private List<Map<String, Integer>> textDrawable;
    private Bitmap bgBitmap;
    private int screenDirection;
    private int speechRate;
    private boolean speechRateFollowSys;
    private int textSize;
    private int textColor;
    private boolean bgIsColor;
    private int bgColor;
    private float lineMultiplier;
    private float paragraphSize;
    private int pageMode;
    private Boolean hideStatusBar;
    private Boolean hideNavigationBar;
    private String fontPath;
    private int textConvert;
    private int navBarColor;
    private Boolean textBold;
    private Boolean canClickTurn;
    private Boolean canKeyTurn;
    private Boolean readAloudCanKeyTurn;
    private int clickSensitivity;
    private Boolean clickAllNext;
    private Boolean showTitle;
    private Boolean showTimeBattery;
    private Boolean showLine;
    private Boolean darkStatusIcon;
    private int screenTimeOut;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    private Boolean tipMarginChange;

    private SharedPreferences preferences;

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
        preferences = MApplication.getConfigPreferences();
        initTextDrawable();
        updateReaderSettings();
    }

    public void updateReaderSettings() {
        this.hideStatusBar = preferences.getBoolean("hide_status_bar", false);
        this.hideNavigationBar = preferences.getBoolean("hide_navigation_bar", false);
        this.textSize = preferences.getInt("textSize", 20);
        this.canClickTurn = preferences.getBoolean("canClickTurn", true);
        this.canKeyTurn = preferences.getBoolean("canKeyTurn", true);
        this.readAloudCanKeyTurn = preferences.getBoolean("readAloudCanKeyTurn", false);
        this.lineMultiplier = preferences.getFloat("lineMultiplier", 1);
        this.paragraphSize = preferences.getFloat("paragraphSize", 1);
        this.clickSensitivity = preferences.getInt("clickSensitivity", 50) > 100
                ? 50 : preferences.getInt("clickSensitivity", 50);
        this.clickAllNext = preferences.getBoolean("clickAllNext", false);
        this.fontPath = preferences.getString("fontPath", null);
        this.textConvert = preferences.getInt("textConvertInt", 0);
        this.textBold = preferences.getBoolean("textBold", false);
        this.speechRate = preferences.getInt("speechRate", 10);
        this.speechRateFollowSys = preferences.getBoolean("speechRateFollowSys", true);
        this.showTitle = preferences.getBoolean("showTitle", true);
        this.showTimeBattery = preferences.getBoolean("showTimeBattery", true);
        this.showLine = preferences.getBoolean("showLine", true);
        this.screenTimeOut = preferences.getInt("screenTimeOut", 0);
        this.paddingLeft = preferences.getInt("paddingLeft", DEFAULT_MARGIN_WIDTH);
        this.paddingTop = preferences.getInt("paddingTop", 0);
        this.paddingRight = preferences.getInt("paddingRight", DEFAULT_MARGIN_WIDTH);
        this.paddingBottom = preferences.getInt("paddingBottom", 0);
        this.pageMode = preferences.getInt("pageMode", 0);
        this.screenDirection = preferences.getInt("screenDirection", 0);
        this.tipMarginChange = preferences.getBoolean("tipMarginChange", false);
        this.navBarColor = preferences.getInt("navBarColorInt", 0);

        initTextDrawableIndex();
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
            temp2.put("textBackground", Color.parseColor("#C6BAA1"));
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

    public void initTextDrawableIndex() {
        if (getIsNightTheme()) {
            textDrawableIndex = preferences.getInt("textDrawableIndexNight", 4);
        } else {
            textDrawableIndex = preferences.getInt("textDrawableIndex", DEFAULT_BG);
        }
        if (textDrawableIndex == -1) {
            textDrawableIndex = DEFAULT_BG;
        }
        initPageStyle();
        setTextDrawable();
    }

    @SuppressWarnings("ConstantConditions")
    private void initPageStyle() {
        if (getBgCustom(textDrawableIndex) == 2 && getBgPath(textDrawableIndex) != null) {
            bgIsColor = false;
            String bgPath = getBgPath(textDrawableIndex);
            Resources resources = MApplication.getInstance().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            bgBitmap = BitmapUtil.getFitSampleBitmap(bgPath, width, height);
            if (bgBitmap != null) {
                return;
            }
        } else if (getBgCustom(textDrawableIndex) == 1) {
            bgIsColor = true;
            bgColor = getBgColor(textDrawableIndex);
            return;
        }
        bgIsColor = true;
        bgColor = textDrawable.get(textDrawableIndex).get("textBackground");
    }

    private void setTextDrawable() {
        darkStatusIcon = getDarkStatusIcon(textDrawableIndex);
        textColor = getTextColor(textDrawableIndex);
    }

    public int getTextColor(int textDrawableIndex) {
        if (preferences.getInt("textColor" + textDrawableIndex, 0) != 0) {
            return preferences.getInt("textColor" + textDrawableIndex, 0);
        } else {
            return getDefaultTextColor(textDrawableIndex);
        }
    }

    public void setTextColor(int textDrawableIndex, int textColor) {
        preferences.edit()
                .putInt("textColor" + textDrawableIndex, textColor)
                .apply();
    }

    @SuppressWarnings("ConstantConditions")
    public Drawable getBgDrawable(int textDrawableIndex, Context context, int width, int height) {
        int color;
        try {
            switch (getBgCustom(textDrawableIndex)) {
                case 2:
                    Bitmap bitmap = BitmapUtil.getFitSampleBitmap(getBgPath(textDrawableIndex), width, height);
                    if (bitmap != null) {
                        return new BitmapDrawable(context.getResources(), bitmap);
                    }
                    break;
                case 1:
                    color = getBgColor(textDrawableIndex);
                    return new ColorDrawable(color);
            }
            if (textDrawable.get(textDrawableIndex).get("bgIsColor") != 0) {
                color = textDrawable.get(textDrawableIndex).get("textBackground");
                return new ColorDrawable(color);
            } else {
                return getDefaultBgDrawable(textDrawableIndex, context);
            }
        } catch (Exception e) {
            if (textDrawable.get(textDrawableIndex).get("bgIsColor") != 0) {
                color = textDrawable.get(textDrawableIndex).get("textBackground");
                return new ColorDrawable(color);
            } else {
                return getDefaultBgDrawable(textDrawableIndex, context);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public Drawable getDefaultBgDrawable(int textDrawableIndex, Context context) {
        if (textDrawable.get(textDrawableIndex).get("bgIsColor") != 0) {
            return new ColorDrawable(textDrawable.get(textDrawableIndex).get("textBackground"));
        } else {
            return context.getResources().getDrawable(getDefaultBg(textDrawableIndex));
        }
    }

    public int getBgCustom(int textDrawableIndex) {
        return preferences.getInt("bgCustom" + textDrawableIndex, 0);
    }

    public void setBgCustom(int textDrawableIndex, int bgCustom) {
        preferences.edit()
                .putInt("bgCustom" + textDrawableIndex, bgCustom)
                .apply();
    }

    public String getBgPath(int textDrawableIndex) {
        return preferences.getString("bgPath" + textDrawableIndex, null);
    }

    public void setBgPath(int textDrawableIndex, String bgUri) {
        preferences.edit()
                .putString("bgPath" + textDrawableIndex, bgUri)
                .apply();
    }

    @SuppressWarnings("ConstantConditions")
    public int getDefaultTextColor(int textDrawableIndex) {
        return textDrawable.get(textDrawableIndex).get("textColor");
    }

    @SuppressWarnings("ConstantConditions")
    private int getDefaultBg(int textDrawableIndex) {
        return textDrawable.get(textDrawableIndex).get("textBackground");
    }

    public int getBgColor(int index) {
        return preferences.getInt("bgColor" + index, Color.parseColor("#1e1e1e"));
    }

    public void setBgColor(int index, int bgColor) {
        preferences.edit()
                .putInt("bgColor" + index, bgColor)
                .apply();
    }

    private boolean getIsNightTheme() {
        return preferences.getBoolean("nightTheme", false);
    }

    public boolean getImmersionStatusBar() {
        return preferences.getBoolean("immersionStatusBar", false);
    }

    public void setImmersionStatusBar(boolean immersionStatusBar) {
        preferences.edit()
                .putBoolean("immersionStatusBar", immersionStatusBar)
                .apply();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        preferences.edit()
                .putInt("textSize", textSize)
                .apply();
    }

    public int getTextColor() {
        return textColor;
    }

    public boolean bgIsColor() {
        return bgIsColor;
    }

    public Drawable getTextBackground(Context context) {
        if (bgIsColor) {
            return new ColorDrawable(bgColor);
        }
        return new BitmapDrawable(context.getResources(), bgBitmap);
    }

    public int getBgColor() {
        return bgColor;
    }

    public boolean bgBitmapIsNull() {
        return bgBitmap == null || bgBitmap.isRecycled();
    }

    public Bitmap getBgBitmap() {
        return bgBitmap.copy(Bitmap.Config.RGB_565, true);
    }

    public int getTextDrawableIndex() {
        return textDrawableIndex;
    }

    public void setTextDrawableIndex(int textDrawableIndex) {
        this.textDrawableIndex = textDrawableIndex;
        if (getIsNightTheme()) {
            preferences.edit()
                    .putInt("textDrawableIndexNight", textDrawableIndex)
                    .apply();
        } else {
            preferences.edit()
                    .putInt("textDrawableIndex", textDrawableIndex)
                    .apply();
        }
        setTextDrawable();
    }

    public void setTextConvert(int textConvert) {
        this.textConvert = textConvert;
        preferences.edit()
                .putInt("textConvertInt", textConvert)
                .apply();
    }

    public void setNavbarColor(int navBarColor) {
        this.navBarColor = navBarColor;
        preferences.edit()
                .putInt("navBarColorInt", navBarColor)
                .apply();
    }

    public int getNavbarColor() {
        return navBarColor;
    }


    public void setTextBold(boolean textBold) {
        this.textBold = textBold;
        preferences.edit()
                .putBoolean("textBold", textBold)
                .apply();
    }

    public void setReadBookFont(String fontPath) {
        this.fontPath = fontPath;
        preferences.edit()
                .putString("fontPath", fontPath)
                .apply();
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

    public Boolean getCanKeyTurn(Boolean isPlay) {
        if (!canKeyTurn) {
            return false;
        } else if (readAloudCanKeyTurn) {
            return true;
        } else {
            return !isPlay;
        }
    }

    public Boolean getCanKeyTurn() {
        return canKeyTurn;
    }

    public void setCanKeyTurn(Boolean canKeyTurn) {
        this.canKeyTurn = canKeyTurn;
        preferences.edit()
                .putBoolean("canKeyTurn", canKeyTurn)
                .apply();
    }

    public Boolean getAloudCanKeyTurn() {
        return readAloudCanKeyTurn;
    }

    public void setAloudCanKeyTurn(Boolean canAloudKeyTurn) {
        this.readAloudCanKeyTurn = canAloudKeyTurn;
        preferences.edit()
                .putBoolean("readAloudCanKeyTurn", canAloudKeyTurn)
                .apply();
    }

    public Boolean getCanClickTurn() {
        return canClickTurn;
    }

    public void setCanClickTurn(Boolean canClickTurn) {
        this.canClickTurn = canClickTurn;
        preferences.edit()
                .putBoolean("canClickTurn", canClickTurn)
                .apply();
    }

    public float getLineMultiplier() {
        return lineMultiplier;
    }

    public void setLineMultiplier(float lineMultiplier) {
        this.lineMultiplier = lineMultiplier;
        preferences.edit()
                .putFloat("lineMultiplier", lineMultiplier)
                .apply();
    }

    public float getParagraphSize() {
        return paragraphSize;
    }

    public void setParagraphSize(float paragraphSize) {
        this.paragraphSize = paragraphSize;
        preferences.edit()
                .putFloat("paragraphSize", paragraphSize)
                .apply();
    }

    public int getClickSensitivity() {
        return clickSensitivity;
    }

    public void setClickSensitivity(int clickSensitivity) {
        this.clickSensitivity = clickSensitivity;
        preferences.edit()
                .putInt("clickSensitivity", clickSensitivity)
                .apply();
    }

    public Boolean getClickAllNext() {
        return clickAllNext;
    }

    public void setClickAllNext(Boolean clickAllNext) {
        this.clickAllNext = clickAllNext;
        preferences.edit()
                .putBoolean("clickAllNext", clickAllNext)
                .apply();
    }

    public int getSpeechRate() {
        return speechRate;
    }

    public void setSpeechRate(int speechRate) {
        this.speechRate = speechRate;
        preferences.edit()
                .putInt("speechRate", speechRate)
                .apply();
    }

    public boolean isSpeechRateFollowSys() {
        return speechRateFollowSys;
    }

    public void setSpeechRateFollowSys(boolean speechRateFollowSys) {
        this.speechRateFollowSys = speechRateFollowSys;
        preferences.edit()
                .putBoolean("speechRateFollowSys", speechRateFollowSys)
                .apply();
    }

    public Boolean getShowTitle() {
        return showTitle;
    }

    public void setShowTitle(Boolean showTitle) {
        this.showTitle = showTitle;
        preferences.edit()
                .putBoolean("showTitle", showTitle)
                .apply();
    }

    public Boolean getShowTimeBattery() {
        return showTimeBattery;
    }

    public void setShowTimeBattery(Boolean showTimeBattery) {
        this.showTimeBattery = showTimeBattery;
        preferences.edit()
                .putBoolean("showTimeBattery", showTimeBattery)
                .apply();
    }

    public Boolean getHideStatusBar() {
        return hideStatusBar;
    }

    public void setHideStatusBar(Boolean hideStatusBar) {
        this.hideStatusBar = hideStatusBar;
        preferences.edit()
                .putBoolean("hide_status_bar", hideStatusBar)
                .apply();
    }

    public Boolean getHideNavigationBar() {
        return hideNavigationBar;
    }

    public void setHideNavigationBar(Boolean hideNavigationBar) {
        this.hideNavigationBar = hideNavigationBar;
        preferences.edit()
                .putBoolean("hide_navigation_bar", hideNavigationBar)
                .apply();
    }

    public Boolean getShowLine() {
        return showLine;
    }

    public void setShowLine(Boolean showLine) {
        this.showLine = showLine;
        preferences.edit()
                .putBoolean("showLine", showLine)
                .apply();
    }

    public boolean getDarkStatusIcon() {
        return darkStatusIcon;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean getDarkStatusIcon(int textDrawableIndex) {
        return preferences.getBoolean("darkStatusIcon" + textDrawableIndex, textDrawable.get(textDrawableIndex).get("darkStatusIcon") != 0);
    }

    public void setDarkStatusIcon(int textDrawableIndex, Boolean darkStatusIcon) {
        preferences.edit()
                .putBoolean("darkStatusIcon" + textDrawableIndex, darkStatusIcon)
                .apply();
    }

    public int getScreenTimeOut() {
        return screenTimeOut;
    }

    public void setScreenTimeOut(int screenTimeOut) {
        this.screenTimeOut = screenTimeOut;
        preferences.edit()
                .putInt("screenTimeOut", screenTimeOut)
                .apply();
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
        preferences.edit()
                .putInt("paddingLeft", paddingLeft)
                .apply();
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
        preferences.edit()
                .putInt("paddingTop", paddingTop)
                .apply();
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
        preferences.edit()
                .putInt("paddingRight", paddingRight)
                .apply();
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
        preferences.edit()
                .putInt("paddingBottom", paddingBottom)
                .apply();
    }

    public int getPageMode() {
        return pageMode;
    }

    public void setPageMode(int pageMode) {
        this.pageMode = pageMode;
        preferences.edit()
                .putInt("pageMode", pageMode)
                .apply();
    }

    public int getScreenDirection() {
        return screenDirection;
    }

    public void setScreenDirection(int screenDirection) {
        this.screenDirection = screenDirection;
        preferences.edit()
                .putInt("screenDirection", screenDirection)
                .apply();
    }

    public Boolean getTipMarginChange() {
        return tipMarginChange;
    }

    public void setTipMarginChange(Boolean tipMarginChange) {
        this.tipMarginChange = tipMarginChange;
        preferences.edit()
                .putBoolean("tipMarginChange", tipMarginChange)
                .apply();
    }

    public int getLight() {
        return preferences.getInt("light", getScreenBrightness());
    }

    public void setLight(int light) {
        preferences.edit()
                .putInt("light", light)
                .apply();
    }

    public Boolean getLightFollowSys() {
        return preferences.getBoolean("isfollowsys", true);
    }

    public void setLightFollowSys(boolean isFollowSys) {
        preferences.edit()
                .putBoolean("isfollowsys", isFollowSys)
                .apply();
    }

    private int getScreenBrightness() {
        int value = 0;
        ContentResolver cr = MApplication.getInstance().getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }
}
