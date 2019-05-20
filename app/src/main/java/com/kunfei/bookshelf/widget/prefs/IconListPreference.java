package com.kunfei.bookshelf.widget.prefs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.kunfei.bookshelf.R;

import java.util.ArrayList;
import java.util.List;


public class IconListPreference extends ListPreference {

    private List<Drawable> mEntryDrawables = new ArrayList<>();

    public IconListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IconListPreference, 0, 0);

        CharSequence[] drawables;

        try {
            drawables = a.getTextArray(R.styleable.IconListPreference_icons);
        } finally {
            a.recycle();
        }

        for (CharSequence drawable : drawables) {
            int resId = context.getResources().getIdentifier(drawable.toString(), "mipmap", context.getPackageName());

            Drawable d = context.getResources().getDrawable(resId);

            mEntryDrawables.add(d);
        }

        setWidgetLayoutResource(R.layout.view_icon);
    }

    protected ListAdapter createListAdapter() {
        final String selectedValue = getValue();
        int selectedIndex = findIndexOfValue(selectedValue);
        return new AppArrayAdapter(getContext(), R.layout.item_icon_preference, getEntries(), mEntryDrawables, selectedIndex);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        String selectedValue = getValue();
        int selectedIndex = findIndexOfValue(selectedValue);

        Drawable drawable = mEntryDrawables.get(selectedIndex);

        ((ImageView) view.findViewById(R.id.preview)).setImageDrawable(drawable);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        builder.setAdapter(createListAdapter(), this);
        super.onPrepareDialogBuilder(builder);
    }

    public class AppArrayAdapter extends ArrayAdapter<CharSequence> {
        private List<Drawable> mImageDrawables = null;
        private int mSelectedIndex = 0;

        public AppArrayAdapter(Context context, int textViewResourceId,
                               CharSequence[] objects, List<Drawable> imageDrawables,
                               int selectedIndex) {
            super(context, textViewResourceId, objects);
            mSelectedIndex = selectedIndex;
            mImageDrawables = imageDrawables;
        }

        @Override
        @SuppressLint("ViewHolder")
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            View view = inflater.inflate(R.layout.item_icon_preference, parent, false);
            CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.label);
            textView.setText(getItem(position));
            textView.setChecked(position == mSelectedIndex);

            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            imageView.setImageDrawable(mImageDrawables.get(position));
            return view;
        }
    }
}
