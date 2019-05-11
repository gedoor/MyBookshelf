package com.kunfei.bookshelf.utils.theme;

import android.annotation.SuppressLint;
import android.content.Context;

import com.kunfei.bookshelf.R;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class MaterialValueHelper {

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getPrimaryTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.primary_text_default_material_light);
        }
        return ContextCompat.getColor(context, R.color.primary_text_default_material_dark);
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getSecondaryTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.secondary_text_default_material_light);
        }
        return ContextCompat.getColor(context, R.color.secondary_text_default_material_dark);
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getPrimaryDisabledTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.primary_text_disabled_material_light);
        }
        return ContextCompat.getColor(context, R.color.primary_text_disabled_material_dark);
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getSecondaryDisabledTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.secondary_text_disabled_material_light);
        }
        return ContextCompat.getColor(context, R.color.secondary_text_disabled_material_dark);
    }

    private MaterialValueHelper() {
    }
}
