package com.kunfei.bookshelf.widget.number;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.theme.ATH;

import androidx.appcompat.app.AlertDialog;

public class NumberPickerDialog {
    private AlertDialog.Builder builder;
    private NumberPicker numberPicker;

    NumberPickerDialog(Context context) {
        builder = new AlertDialog.Builder(context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null);
        numberPicker = view.findViewById(R.id.number_picker);
        builder.setView(view);
    }

    public NumberPickerDialog setTitle(String title) {
        builder.setTitle(title);
        return this;
    }

    public NumberPickerDialog setMaxValue(int value) {
        numberPicker.setMaxValue(value);
        return this;
    }

    public NumberPickerDialog setMinValue(int value) {
        numberPicker.setMinValue(value);
        return this;
    }

    public NumberPickerDialog setValue(int value) {
        numberPicker.setValue(value);
        return this;
    }

    public NumberPickerDialog create() {
        builder.create();
        return this;
    }

    public NumberPickerDialog setListener(OnClickListener onClickListener) {
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            numberPicker.clearFocus();
            SoftInputUtil.hideIMM(numberPicker);
            if (onClickListener != null) {
                onClickListener.setNumber(numberPicker.getValue());
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        return this;
    }

    public void show() {
        AlertDialog dialog = builder.show();
        ATH.setAlertDialogTint(dialog);
    }

    public interface OnClickListener {
        void setNumber(int i);
    }

}
