package com.kunfei.bookshelf.utils;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;

public class ShapeSelector {
    @IntDef({GradientDrawable.RECTANGLE, GradientDrawable.OVAL,
            GradientDrawable.LINE, GradientDrawable.RING})
    private @interface Shape {
    }

    private int mShape;               //the shape of background
    private int mDefaultBgColor;      //default background color
    private int mDisabledBgColor;     //state_enabled = false
    private int mPressedBgColor;      //state_pressed = true
    private int mSelectedBgColor;     //state_selected = true
    private int mFocusedBgColor;      //state_focused = true
    private int mStrokeWidth;         //stroke width in pixel
    private int mDefaultStrokeColor;  //default stroke color
    private int mDisabledStrokeColor; //state_enabled = false
    private int mPressedStrokeColor;  //state_pressed = true
    private int mSelectedStrokeColor; //state_selected = true
    private int mFocusedStrokeColor;  //state_focused = true
    private int mCornerRadius;        //corner radius

    private boolean hasSetDisabledBgColor = false;
    private boolean hasSetPressedBgColor = false;
    private boolean hasSetSelectedBgColor = false;
    private boolean hasSetFocusedBgColor = false;

    private boolean hasSetDisabledStrokeColor = false;
    private boolean hasSetPressedStrokeColor = false;
    private boolean hasSetSelectedStrokeColor = false;
    private boolean hasSetFocusedStrokeColor = false;

    public static ShapeSelector newShapeSelector() {
        return new ShapeSelector();
    }

    public ShapeSelector() {
        //initialize default values
        mShape = GradientDrawable.RECTANGLE;
        mDefaultBgColor = Color.TRANSPARENT;
        mDisabledBgColor = Color.TRANSPARENT;
        mPressedBgColor = Color.TRANSPARENT;
        mSelectedBgColor = Color.TRANSPARENT;
        mFocusedBgColor = Color.TRANSPARENT;
        mStrokeWidth = 0;
        mDefaultStrokeColor = Color.TRANSPARENT;
        mDisabledStrokeColor = Color.TRANSPARENT;
        mPressedStrokeColor = Color.TRANSPARENT;
        mSelectedStrokeColor = Color.TRANSPARENT;
        mFocusedStrokeColor = Color.TRANSPARENT;
        mCornerRadius = 0;
    }

    public ShapeSelector setShape(@Shape int shape) {
        mShape = shape;
        return this;
    }

    public ShapeSelector setDefaultBgColor(@ColorInt int color) {
        mDefaultBgColor = color;
        if (!hasSetDisabledBgColor)
            mDisabledBgColor = color;
        if (!hasSetPressedBgColor)
            mPressedBgColor = color;
        if (!hasSetSelectedBgColor)
            mSelectedBgColor = color;
        if (!hasSetFocusedBgColor)
            mFocusedBgColor = color;
        return this;
    }

    public ShapeSelector setDisabledBgColor(@ColorInt int color) {
        mDisabledBgColor = color;
        hasSetDisabledBgColor = true;
        return this;
    }

    public ShapeSelector setPressedBgColor(@ColorInt int color) {
        mPressedBgColor = color;
        hasSetPressedBgColor = true;
        return this;
    }

    public ShapeSelector setSelectedBgColor(@ColorInt int color) {
        mSelectedBgColor = color;
        hasSetSelectedBgColor = true;
        return this;
    }

    public ShapeSelector setFocusedBgColor(@ColorInt int color) {
        mFocusedBgColor = color;
        hasSetPressedBgColor = true;
        return this;
    }

    public ShapeSelector setStrokeWidth(@Dimension int width) {
        mStrokeWidth = width;
        return this;
    }

    public ShapeSelector setDefaultStrokeColor(@ColorInt int color) {
        mDefaultStrokeColor = color;
        if (!hasSetDisabledStrokeColor)
            mDisabledStrokeColor = color;
        if (!hasSetPressedStrokeColor)
            mPressedStrokeColor = color;
        if (!hasSetSelectedStrokeColor)
            mSelectedStrokeColor = color;
        if (!hasSetFocusedStrokeColor)
            mFocusedStrokeColor = color;
        return this;
    }

    public ShapeSelector setDisabledStrokeColor(@ColorInt int color) {
        mDisabledStrokeColor = color;
        hasSetDisabledStrokeColor = true;
        return this;
    }

    public ShapeSelector setPressedStrokeColor(@ColorInt int color) {
        mPressedStrokeColor = color;
        hasSetPressedStrokeColor = true;
        return this;
    }

    public ShapeSelector setSelectedStrokeColor(@ColorInt int color) {
        mSelectedStrokeColor = color;
        hasSetSelectedStrokeColor = true;
        return this;
    }

    public ShapeSelector setFocusedStrokeColor(@ColorInt int color) {
        mFocusedStrokeColor = color;
        hasSetFocusedStrokeColor = true;
        return this;
    }

    public ShapeSelector setCornerRadius(@Dimension int radius) {
        mCornerRadius = radius;
        return this;
    }

    public StateListDrawable create() {
        StateListDrawable selector = new StateListDrawable();

        //enabled = false
        if (hasSetDisabledBgColor || hasSetDisabledStrokeColor) {
            GradientDrawable disabledShape = getItemShape(mShape, mCornerRadius,
                    mDisabledBgColor, mStrokeWidth, mDisabledStrokeColor);
            selector.addState(new int[]{-android.R.attr.state_enabled}, disabledShape);
        }

        //pressed = true
        if (hasSetPressedBgColor || hasSetPressedStrokeColor) {
            GradientDrawable pressedShape = getItemShape(mShape, mCornerRadius,
                    mPressedBgColor, mStrokeWidth, mPressedStrokeColor);
            selector.addState(new int[]{android.R.attr.state_pressed}, pressedShape);
        }

        //selected = true
        if (hasSetSelectedBgColor || hasSetSelectedStrokeColor) {
            GradientDrawable selectedShape = getItemShape(mShape, mCornerRadius,
                    mSelectedBgColor, mStrokeWidth, mSelectedStrokeColor);
            selector.addState(new int[]{android.R.attr.state_selected}, selectedShape);
        }

        //focused = true
        if (hasSetFocusedBgColor || hasSetFocusedStrokeColor) {
            GradientDrawable focusedShape = getItemShape(mShape, mCornerRadius,
                    mFocusedBgColor, mStrokeWidth, mFocusedStrokeColor);
            selector.addState(new int[]{android.R.attr.state_focused}, focusedShape);
        }

        //default
        GradientDrawable defaultShape = getItemShape(mShape, mCornerRadius,
                mDefaultBgColor, mStrokeWidth, mDefaultStrokeColor);
        selector.addState(new int[]{}, defaultShape);

        return selector;
    }

    private GradientDrawable getItemShape(int shape, int cornerRadius,
                                          int solidColor, int strokeWidth, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(shape);
        drawable.setStroke(strokeWidth, strokeColor);
        drawable.setCornerRadius(cornerRadius);
        drawable.setColor(solidColor);
        return drawable;
    }
}
