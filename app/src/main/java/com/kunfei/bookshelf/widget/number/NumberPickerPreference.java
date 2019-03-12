package com.kunfei.bookshelf.widget.number;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.SoftInputUtil;

import androidx.annotation.NonNull;

/**
 * Displaying a NumberPicker in a DialogPreference
 */
public class NumberPickerPreference extends DialogPreference {
    private static final String TAG = NumberPickerPreference.class.getSimpleName();

    /**
     * this variables will be initialised in 'init()'
     */
    //by default min value is 0
    private int minValue = 0;
    //by default max value is 10
    private int maxValue = 10;
    //get summary - hardcode if no summary is set
    private String summaryPattern = "number picked: %s";

    private NumberPicker numPicker;

    private int numValue = minValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // initializing attributes
        init(attrs);
    }

    /**
     * setting attributes from xml
     * attr attributeset
     */
    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.NumberPickerPreference);

        maxValue = a.getInt(R.styleable.NumberPickerPreference_MaxValue, maxValue);
        minValue = a.getInt(R.styleable.NumberPickerPreference_MinValue, minValue);
        summaryPattern = a.getString(R.styleable.NumberPickerPreference_android_summary);

        a.recycle();
    }

    /**
     * @return dialog view with picker inside
     */
    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        numPicker = new NumberPicker(getContext());
        numPicker.setLayoutParams(layoutParams);
        numPicker.setMinValue(minValue);
        numPicker.setMaxValue(maxValue);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(numPicker);

        return dialogView;
    }


    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        numPicker.setValue(getValue());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        numPicker.clearFocus();
        SoftInputUtil.hideIMM(numPicker);
        super.onClick(dialog, which);
    }

    /**
     * update summary when dialog is closed
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int pickerValue = numPicker.getValue();
            updateSummary(pickerValue);
            setValue(pickerValue);
            Log.d(TAG, "number picked = " + pickerValue);
        }
    }

    /**
     * if no default value is set - then set min value
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, minValue);
    }

    /**
     * SetInitialValue
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedInt(minValue));
        } else {
            setValue((Integer) defaultValue);
            if (((Integer) defaultValue > maxValue)) {
                Log.w(TAG, "default value is bigger than maxValue!");
            } else if (((Integer) defaultValue < minValue)) {
                Log.w(TAG, "default value is smaller than minValue!");
            }
        }
        updateSummary(getValue());
    }

    /**
     * @return current value
     */
    public int getValue() {
        return this.numValue;
    }

    /**
     * @param value which will be stored in SharedPreferences
     */
    private void setValue(int value) {
        this.numValue = value;
        persistInt(this.numValue);
    }

    /**
     * @return get summary pattern from xml file
     */
    private String getSummaryPattern() {
        return this.summaryPattern;
    }


    /**
     * value insert into summaryPattern
     */
    private void updateSummary(int val) {
        setSummary(String.format(getSummaryPattern(), Integer.toString(val)));
    }
}
