package com.kunfei.bookshelf.widget.number;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.R;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;

public class AddAndSubButton extends FrameLayout implements View.OnClickListener {
    public static final int INT = 0;
    private Float number;

    private OnChangedListener onChangedListener;
    private DecimalFormat decimalFormat = new DecimalFormat("#");
    private int numberType = INT;
    private float minNumber = 0;
    private float maxNumber = 10;
    private float stepNumber = 1;
    private TextView addButton,subButton;

    public AddAndSubButton(Context context) {
        this(context, null);
    }

    public AddAndSubButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {


        LayoutInflater.from(context).inflate(R.layout.view_and_sub_buttom, this);

        addButton = findViewById(R.id.button_add);
        addButton.setOnClickListener(this);
        subButton = findViewById(R.id.button_sub);
        subButton.setOnClickListener(this);

    }


    public void setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
    }

    public float getNumber() {
        try {
            return number;
        } catch (NumberFormatException e) {
            number = minNumber;
            return minNumber;
        }
    }

    public AddAndSubButton setFormat(String pattern) {
        decimalFormat = new DecimalFormat(pattern);
        return this;
    }

    public AddAndSubButton setNumber(float number) {
        this.number = number;
        return this;
    }

    public AddAndSubButton setAddText(String addText) {
        addButton.setText(addText);
        return this;
    }

    public AddAndSubButton setSubText(String subText) {
        subButton.setText(subText);
        return this;
    }

    public AddAndSubButton setMinNumber(float minNumber) {
        this.minNumber = minNumber;
        return this;
    }

    public AddAndSubButton setMaxNumber(float maxNumber) {
        this.maxNumber = maxNumber;
        return this;
    }

    public AddAndSubButton setStepNumber(float stepNumber) {
        this.stepNumber = stepNumber;
        return this;
    }

    public AddAndSubButton setNumberType(int numberType) {
        this.numberType = numberType;
        return this;
    }

    @Override
    public void onClick(View view) {
        float count = getNumber();
        switch (view.getId()) {
            case R.id.button_add:
                if (count < maxNumber) {
                    changeNumber(count + stepNumber);
                }
                break;
            case R.id.button_sub:
                if (count > minNumber) {
                    changeNumber(count - stepNumber);
                }
                break;
        }
    }

    private void changeNumber(float number) {
        this.number = number;
        if (onChangedListener != null) {
            onChangedListener.numberChange(number);
        }
    }

    public interface OnChangedListener {
        void numberChange(float number);
    }

}
