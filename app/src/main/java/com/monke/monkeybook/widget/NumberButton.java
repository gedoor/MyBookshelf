package com.monke.monkeybook.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;

public class NumberButton extends LinearLayout  implements View.OnClickListener {
    private OnChangedListener onChangedListener;
    private TextView tvNumber;

    private float minNumber = 0;
    private float maxNumber = 10;
    private float stepNumber = 1;

    public NumberButton(Context context) {
        this(context, null);
    }

    public NumberButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_number_buttom, this);

        TextView addButton = findViewById(R.id.button_add);
        addButton.setOnClickListener(this);
        TextView subButton = findViewById(R.id.button_sub);
        subButton.setOnClickListener(this);

        tvNumber = findViewById(R.id.tv_number);

    }

    public NumberButton setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
        return this;
    }

    public float getNumber() {
        try {
            return Float.parseFloat(tvNumber.getText().toString());
        } catch (NumberFormatException e) {
            tvNumber.setText(Float.toString(minNumber));
            return minNumber;
        }
    }

    public void setNumber(float number) {
        tvNumber.setText(Float.toString(number));
    }

    public NumberButton setMinNumber(float minNumber) {
        this.minNumber = minNumber;
        return this;
    }

    public NumberButton setMaxNumber(float maxNumber) {
        this.maxNumber = maxNumber;
        return this;
    }

    public NumberButton setStepNumber(float stepNumber) {
        this.stepNumber = stepNumber;
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

    private void changeNumber(float f) {
        tvNumber.setText(Float.toString(f));
        if (onChangedListener != null) {
            onChangedListener.numberChange(f);
        }
    }

    public interface OnChangedListener {
        void numberChange(float number);
    }

}
