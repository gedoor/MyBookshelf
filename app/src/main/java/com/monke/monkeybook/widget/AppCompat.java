package com.monke.monkeybook.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ScreenUtils;

import java.lang.reflect.Field;

public class AppCompat {

    public static void useCustomIconForSearchView(SearchView searchView, String hint, boolean showSearchIcon, boolean showBg) {
        AppCompatImageView close = searchView.findViewById(R.id.search_close_btn);
        close.setImageResource(R.drawable.ic_close_black_24dp);
        setTint(close, searchView.getResources().getColor(R.color.menu_color_default));
        close.setPadding(0, ScreenUtils.dpToPx(2), 0, 0);

        AppCompatImageView search = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        search.setImageResource(R.drawable.ic_search_black_24dp);
        setTint(search, searchView.getResources().getColor(R.color.menu_color_default));

        SearchView.SearchAutoComplete searchText = searchView.findViewById(R.id.search_src_text);

        LinearLayout plate = searchView.findViewById(R.id.search_plate);
        if (showBg) {
            Drawable bag = searchView.getResources().getDrawable(R.drawable.bg_textfield_search);
            setTintList(bag, createSearchPlateBagState(searchView.getResources().getColor(R.color.colorAccent),
                    searchText.getCurrentHintTextColor()));
            android.support.v4.view.ViewCompat.setBackground(plate, bag);
        } else {
            android.support.v4.view.ViewCompat.setBackground(plate, null);
        }

        setQueryHintForSearchText(searchText, hint, showSearchIcon);
    }

    public static void useCustomIconForSearchView(SearchView searchView, String hint) {
        useCustomIconForSearchView(searchView, hint, true, true);
    }

    private static ColorStateList createSearchPlateBagState(int activeColor, int normalColor) {
        int[] colors = new int[]{activeColor, activeColor, activeColor, normalColor, normalColor};
        int[][] states = new int[5][];
        states[0] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_activated};
        states[2] = new int[]{android.R.attr.state_focused};
        states[3] = new int[]{android.R.attr.state_window_focused};
        states[4] = new int[]{};
        return new ColorStateList(states, colors);
    }

    public static void setQueryHintForSearchText(SearchView.SearchAutoComplete textView, String hintText) {
        setQueryHintForSearchText(textView, hintText, true);
    }


    public static void setQueryHintForSearchText(SearchView.SearchAutoComplete textView, String hintText, boolean showIcon) {
        textView.setTextColor(textView.getResources().getColor(R.color.tv_text_default));
        if (showIcon) {
            final int textSize = (int) (textView.getTextSize() * 1.25);
            Drawable mSearchHintIcon = textView.getResources().getDrawable(R.drawable.ic_search_black_24dp);
            mSearchHintIcon.setBounds(0, 0, textSize, textSize);
            setTint(mSearchHintIcon, textView.getCurrentTextColor());
            final SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
            ssb.setSpan(new ImageSpan(mSearchHintIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(hintText);
            textView.setHint(ssb);
        } else {
            textView.setHint(hintText);
        }
    }

    public static void setNavigationViewLineStyle(NavigationView navigationView, @ColorInt final int color, final int height) {
        try {
            Field fieldByPressenter = navigationView.getClass().getDeclaredField("presenter");
            fieldByPressenter.setAccessible(true);
            NavigationMenuPresenter menuPresenter = (NavigationMenuPresenter) fieldByPressenter.get(navigationView);
            Field fieldByMenuView = menuPresenter.getClass().getDeclaredField("menuView");
            fieldByMenuView.setAccessible(true);
            final NavigationMenuView mMenuView = (NavigationMenuView) fieldByMenuView.get(menuPresenter);
            mMenuView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(View view) {
                    RecyclerView.ViewHolder viewHolder = mMenuView.getChildViewHolder(view);
                    if (viewHolder != null && "SeparatorViewHolder".equals(viewHolder.getClass().getSimpleName())) {
                        if (viewHolder.itemView instanceof FrameLayout) {
                            FrameLayout frameLayout = (FrameLayout) viewHolder.itemView;
                            View line = frameLayout.getChildAt(0);
                            line.setBackgroundColor(color);
                            line.getLayoutParams().height = height;
                            line.setLayoutParams(line.getLayoutParams());
                        }
                    }
                }

                @Override
                public void onChildViewDetachedFromWindow(View view) {

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setTintList(Drawable drawable, ColorStateList tint, @NonNull PorterDuff.Mode tintMode) {
        if (drawable == null) return;
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, tintMode);
    }

    public static void setTintList(Drawable drawable, ColorStateList tint) {
        setTintList(drawable, tint, PorterDuff.Mode.SRC_ATOP);
    }

    public static void setTintList(View view, ColorStateList tint) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            setTintList(drawable, tint);
        } else if (view instanceof TextView) {
            Drawable[] drawables = ((TextView) view).getCompoundDrawables();
            for (Drawable drawable : drawables) {
                setTintList(drawable, tint);
            }
        }
    }

    public static void setTint(Drawable drawable, @ColorInt int tint, @NonNull PorterDuff.Mode tintMode) {
        if (drawable == null) return;
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, tintMode);
    }

    public static void setTint(Drawable drawable, @ColorInt int tint) {
        setTint(drawable, tint, PorterDuff.Mode.SRC_ATOP);
    }

    public static void setTint(View view, int color) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            setTint(drawable, color);
        } else if (view instanceof TextView) {
            Drawable[] drawables = ((TextView) view).getCompoundDrawables();
            for (Drawable drawable : drawables) {
                setTint(drawable, color);
            }
        }
    }

    public static void setTint(MenuItem item, int color) {
        if (item != null && item.getIcon() != null) {
            setTint(item.getIcon(), color);
        }
    }

    public static void setToolbarNavIconTint(Toolbar toolbar, int color) {
        if (toolbar != null && toolbar.getNavigationIcon() != null) {
            setTint(toolbar.getNavigationIcon(), color);
        }
    }
}
