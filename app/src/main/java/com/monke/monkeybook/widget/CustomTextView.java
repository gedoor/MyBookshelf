package com.monke.monkeybook.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import com.monke.monkeybook.R;


/**
 * CustomTextView
 * @author Qiugang & jiaowenzheng
 */
public class CustomTextView extends android.support.v7.widget.AppCompatTextView {

    private GradientDrawable normalGD;
    private GradientDrawable pressedGD;
    private StateListDrawable selector;

    private int strokeWidth;
    private int radius;


    public CustomTextView(Context context) {
        super(context);

    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributeSet(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributeSet(context, attrs);
    }


    private void setAttributeSet(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        int strokeColor = a.getColor(R.styleable.CustomTextView_textStrokeColor, Color.TRANSPARENT);
        radius = a.getDimensionPixelSize(R.styleable.CustomTextView_textRadius, 0);
        int leftTopRadius = a.getDimensionPixelSize(R.styleable.CustomTextView_textLeftTopRadius, 0);
        int leftBottomRadius = a.getDimensionPixelSize(R.styleable.CustomTextView_textLeftBottomRadius, 0);
        int rightTopRadius = a.getDimensionPixelSize(R.styleable.CustomTextView_textRightTopRadius, 0);
        int rightBottomRadius = a.getDimensionPixelSize(R.styleable.CustomTextView_textRightBottomRadius, 0);
        strokeWidth = a.getDimensionPixelSize(R.styleable.CustomTextView_textStrokeWidth, 0);
        int normalTextColor = a.getColor(R.styleable.CustomTextView_textNormalTextColor, Color.TRANSPARENT);
        int selectedTextColor = a.getColor(R.styleable.CustomTextView_textSelectedTextColor, Color.TRANSPARENT);
        int normalSolidColor = a.getColor(R.styleable.CustomTextView_textNormalSolidColor,Color.TRANSPARENT);
        int pressedSolidColor = a.getColor(R.styleable.CustomTextView_textPressedSolidColor,Color.TRANSPARENT);
        boolean isSelected = a.getBoolean(R.styleable.CustomTextView_textIsSelected,false);
        boolean noLeftStroke = a.getBoolean(R.styleable.CustomTextView_textNoLeftStroke,false);
        boolean noRightStroke = a.getBoolean(R.styleable.CustomTextView_textNoRightStroke,false);
        boolean noTopStroke = a.getBoolean(R.styleable.CustomTextView_textNoTopStroke,false);
        boolean noBottomStroke = a.getBoolean(R.styleable.CustomTextView_textNoBottomStroke,false);
        Drawable textDrawable = a.getDrawable(R.styleable.CustomTextView_textDrawable);

        a.recycle();

        selector = new StateListDrawable();
        normalGD = new GradientDrawable();
        pressedGD = new GradientDrawable();

        //set selected state
        setPressedState(leftTopRadius,leftBottomRadius,rightBottomRadius,rightTopRadius,strokeColor,
                pressedSolidColor,noLeftStroke,noRightStroke,noTopStroke,noBottomStroke,isSelected);

        //set normal state
        setNormalState(leftTopRadius,leftBottomRadius,rightBottomRadius,rightTopRadius,strokeColor,
                normalSolidColor,noLeftStroke,noRightStroke,noTopStroke,noBottomStroke);

        //设置selector
        setBackgroundDrawable(selector);

        if (textDrawable != null) {
            BitmapDrawable bd = (BitmapDrawable) textDrawable;
            ImageSpan imageSpan = new ImageSpan(getContext(), bd.getBitmap());

            String text = "[icon]";
            SpannableString ss = new SpannableString("[icon]");

            ss.setSpan(imageSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            setText(ss);
        }


        if (normalTextColor != 0 && selectedTextColor != 0) {
            //设置state_selected状态时，和正常状态时文字的颜色
            setClickable(true);
            ColorStateList textColorSelect;

            if (isSelected) { //是否可以选中
                int[][] states = new int[2][1];
                states[0] = new int[]{android.R.attr.state_selected};
                states[1] = new int[]{};
                textColorSelect = new ColorStateList(states, new int[]{selectedTextColor, normalTextColor});
            }else{
                int[][] states = new int[3][1];
                states[0] = new int[]{android.R.attr.state_selected};
                states[1] = new int[]{android.R.attr.state_pressed};
                states[2] = new int[]{};
                textColorSelect = new ColorStateList(states, new int[]{selectedTextColor,selectedTextColor,normalTextColor});
            }

            setTextColor(textColorSelect);
        }else{
            setClickable(false);
        }
    }

    /**
     *
     * 设置正常状态下drawable
     *
     * @param leftTopRadius            左上角角度
     * @param leftBottomRadius         左下角角度
     * @param rightBottomRadius        右下角角度
     * @param rightTopRadius           右上角角度
     * @param strokeColor              描边颜色
     * @param normalSolid              正常状态下填充颜色
     * @param noLeftStroke             无左描边
     * @param noRightStroke            无右描边
     * @param noTopStroke              无上描边
     * @param noBottomStroke           无底描边
     */
    private void setNormalState(int leftTopRadius,int leftBottomRadius, int rightBottomRadius,int rightTopRadius,
                                int strokeColor,int normalSolid,boolean noLeftStroke,
                                boolean noRightStroke,boolean noTopStroke,boolean noBottomStroke){

        //设置正常状态下填充色
        normalGD.setColor(normalSolid);
        //设置描边
        normalGD.setStroke(strokeWidth, strokeColor);
        //设置圆角
        setRadius(normalGD,leftTopRadius,leftBottomRadius,rightBottomRadius,rightTopRadius);
        //normal drawable
        LayerDrawable normalLayerDrawable = new LayerDrawable(new Drawable[]{normalGD});
        //设置正常状态下描边边距
        setStrokeMargin(normalLayerDrawable,0,noLeftStroke,noRightStroke,noTopStroke,noBottomStroke);
        //设置正常状态下的drawable
        selector.addState(new int[]{}, normalLayerDrawable);
    }

    /**
     * 设置按下状态drawable
     *
     * @param leftTopRadius            左上角角度
     * @param leftBottomRadius         左下角角度
     * @param rightBottomRadius        右下角角度
     * @param rightTopRadius           右上角角度
     * @param strokeColor              描边颜色
     * @param pressedSolid             按下状态填充色
     * @param noLeftStroke             无左描边
     * @param noRightStroke            无右描边
     * @param noTopStroke              无上描边
     * @param noBottomStroke           无底描边
     * @param isSelected               是否可以选择状态
     */
    private void setPressedState(int leftTopRadius,int leftBottomRadius,int rightBottomRadius,int rightTopRadius,
                                 int strokeColor,int pressedSolid,boolean noLeftStroke,
                                 boolean noRightStroke,boolean noTopStroke,boolean noBottomStroke,boolean isSelected){

        if (pressedSolid != Color.TRANSPARENT) {
            //设置按下填充色
            pressedGD.setColor(pressedSolid);
            //设置选中状态下描边边距
            pressedGD.setStroke(strokeWidth, strokeColor);
            //设置圆角
            setRadius(pressedGD,leftTopRadius,leftBottomRadius,rightBottomRadius,rightTopRadius);

            LayerDrawable pressedLayerDrawable = new LayerDrawable(new Drawable[]{pressedGD});
            setStrokeMargin(pressedLayerDrawable,0,noLeftStroke,noRightStroke,noTopStroke,noBottomStroke);
            //设置按下状态
            if (isSelected) {
                selector.addState(new int[]{android.R.attr.state_selected}, pressedLayerDrawable);
            } else {
                selector.addState(new int[]{android.R.attr.state_pressed}, pressedLayerDrawable);
            }
        }
    }

    /**
     *
     * 设置角度
     *
     * @param drawable                 drawable
     * @param leftTopRadius            左上角角度
     * @param leftBottomRadius         左下角角度
     * @param rightBottomRadius        右下角角度
     * @param rightTopRadius           右上角角度
     *
     */
    private void setRadius(GradientDrawable drawable,int leftTopRadius,int leftBottomRadius,
                           int rightBottomRadius,int rightTopRadius){
        if (radius != 0) {
            drawable.setCornerRadius(radius);
        } else if (leftTopRadius != 0 || leftBottomRadius != 0 || rightTopRadius != 0 || rightBottomRadius != 0) {
            drawable.setCornerRadii(new float[]{leftTopRadius, leftTopRadius, rightTopRadius,
                    rightTopRadius, rightBottomRadius, rightBottomRadius, leftBottomRadius, leftBottomRadius});
        }
    }


    /**
     *
     * 设置 button 四个边距
     *
     * @param layerDrawable    LayerDrawable
     * @param index            下标
     * @param left             左边距
     * @param right            右边距
     * @param top              上边距
     * @param bottom           下边距
     */
    private void setStrokeMargin(LayerDrawable layerDrawable, int index , boolean left, boolean right, boolean top, boolean bottom){

        int leftMargin = left ? -strokeWidth : 0;
        int rightMargin = right ? -strokeWidth : 0;
        int topMargin = top ? -strokeWidth : 0;
        int bottomMargin = bottom ? -strokeWidth: 0;

        layerDrawable.setLayerInset(index,leftMargin,topMargin,rightMargin,bottomMargin);
    }

    /**
     * 设置填充图片
     *
     * @param drawableId  normalGD id
     */
    public void setTextDrawable(int drawableId) {
        if (drawableId != 0) {
            Drawable textdrwable = getResources().getDrawable(drawableId);
            BitmapDrawable bd = (BitmapDrawable) textdrwable;
            ImageSpan imageSpan = new ImageSpan(getContext(), bd.getBitmap());

            String text = "[icon]";
            SpannableString ss = new SpannableString("[icon]");

            ss.setSpan(imageSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            setText(ss);
        }
    }

    /**
     *
     * 设置填充颜色
     *
     * @param colorId   颜色id
     */
    public void setSolidColor(int colorId) {
        normalGD.setColor(colorId);
        setBackgroundDrawable(normalGD);
    }

    /**
     * 设置圆角弧度
     *
     * @param leftTopRadius         左上角弧度
     * @param leftBottomRadius      左下角弧度
     * @param rightTopRadius        右上角弧度
     * @param rightBottomRadius     右下角弧度
     */
    public void setRadius(int leftTopRadius, int leftBottomRadius, int rightTopRadius, int rightBottomRadius) {
        normalGD.setCornerRadii(new float[]{leftTopRadius, leftTopRadius, rightTopRadius, rightTopRadius, rightBottomRadius, rightBottomRadius, leftBottomRadius, leftBottomRadius});
        setBackgroundDrawable(normalGD);
    }

    /**
     * 设置边框颜色及宽度
     *
     * @param strokeWidth      边框宽度
     * @param color          边框颜色
     */
    public void setStrokeColorAndWidth(int strokeWidth,int color){
        normalGD.setStroke(strokeWidth, color);
    }



    /**
     * 设置textView选中状态颜色
     *
     * @param normalTextColor     正常状态颜色
     * @param selectedTextColor   按下状态颜色
     */
    public void setSelectedTextColor(int normalTextColor,int selectedTextColor) {

        normalTextColor = getResources().getColor(normalTextColor);
        selectedTextColor = getResources().getColor(selectedTextColor);

        if (normalTextColor != 0 && selectedTextColor != 0) {
            //设置state_selected状态时，和正常状态时文字的颜色
            setClickable(true);
            int[][] states = new int[3][1];
            states[0] = new int[]{android.R.attr.state_selected};
            states[1] = new int[]{android.R.attr.state_pressed};
            states[2] = new int[]{};
            ColorStateList textColorSelect = new ColorStateList(states, new int[]{selectedTextColor, selectedTextColor, normalTextColor});
            setTextColor(textColorSelect);
        }else{
            setClickable(false);
        }

    }

}
