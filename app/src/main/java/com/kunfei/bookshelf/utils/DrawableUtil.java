package com.kunfei.bookshelf.utils;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;

import androidx.annotation.ColorInt;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class DrawableUtil {

    public static TransitionDrawable createTransitionDrawable(@ColorInt int startColor, @ColorInt int endColor) {
        return createTransitionDrawable(new ColorDrawable(startColor), new ColorDrawable(endColor));
    }

    public static TransitionDrawable createTransitionDrawable(Drawable start, Drawable end) {
        final Drawable[] drawables = new Drawable[2];

        drawables[0] = start;
        drawables[1] = end;

        return new TransitionDrawable(drawables);
    }

    /**
     * 获取Selector
     */
    public static StateListDrawable getSelector(Drawable normalDraw, Drawable pressedDraw) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDraw);
        stateListDrawable.addState(new int[]{}, normalDraw);
        return stateListDrawable;
    }

    /**
     * 设置shape(设置单独圆角)
     */
    public static GradientDrawable getDrawable(float topLeftCA, float topRigthCA, float buttomLeftCA,
                                               float buttomRightCA, int bgColor, int storkeWidth, int strokeColor) {
        //把边框值设置成dp对应的px
        storkeWidth = ScreenUtils.dpToPx(storkeWidth);

        float[] circleAngleArr = {topLeftCA, topLeftCA, topRigthCA, topRigthCA,
                buttomLeftCA, buttomLeftCA, buttomRightCA, buttomRightCA};
        //把圆角设置成dp对应的px
        for (int i = 0; i < circleAngleArr.length; i++) {
            circleAngleArr[i] = ScreenUtils.dpToPx((int) circleAngleArr[i]);
        }

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadii(circleAngleArr);//圆角
        gradientDrawable.setColor(bgColor); //背景色
        gradientDrawable.setStroke(storkeWidth, strokeColor); //边框宽度，边框颜色

        return gradientDrawable;
    }

    /**
     * 设置shape(圆角)
     */
    public static GradientDrawable getDrawable(int bgCircleAngle, int bgColor, int width, int strokeColor) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(bgCircleAngle);
        gradientDrawable.setColor(bgColor);
        gradientDrawable.setStroke(width, strokeColor);
        return gradientDrawable;
    }

    private DrawableUtil() {
    }
}
