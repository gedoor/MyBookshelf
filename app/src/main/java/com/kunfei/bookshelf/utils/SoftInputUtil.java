package com.kunfei.bookshelf.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.kunfei.bookshelf.MApplication;

public class SoftInputUtil {

    //隐藏输入法
    public static void hideIMM(View view) {
        InputMethodManager imm = (InputMethodManager) MApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void resetBoxPosition(Activity activity, View prentView, int viewId) {

        final View decorView = (activity).getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            try {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = getScreenHeight(activity);
                int heightDifference = screenHeight - rect.bottom;  //计算软键盘占有的高度  = 屏幕高度 - 视图可见高度
                View view = prentView.findViewById(viewId);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                layoutParams.bottomMargin = heightDifference;   //设置rlContent的marginBottom的值为软键盘占有的高度即可
                view.setLayoutParams(layoutParams);
                view.requestLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static int getScreenHeight(Activity activity) {
        WindowManager manager = (activity).getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static int getScreenWidth(Activity activity) {
        WindowManager manager = (activity).getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
}
