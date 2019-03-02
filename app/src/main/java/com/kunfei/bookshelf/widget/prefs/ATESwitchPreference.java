package com.kunfei.bookshelf.widget.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import java.util.LinkedList;

public class ATESwitchPreference extends SwitchPreference {
    @SuppressLint("NewApi")
    public ATESwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ATESwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ATESwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ATESwitchPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            LinkedList<ViewGroup> queue = new LinkedList<>();
            queue.add(viewGroup);
            while (!queue.isEmpty()) {
                ViewGroup current = queue.removeFirst();
                for (int i = 0; i < current.getChildCount(); i++) {
                    if (current.getChildAt(i) instanceof Switch) {
                        ATH.setTint(current.getChildAt(i), ThemeStore.accentColor(view.getContext()));
                        return;
                    } else if (current.getChildAt(i) instanceof ViewGroup) {
                        queue.addLast((ViewGroup) current.getChildAt(i));
                    }
                }
            }
        }

    }

}
