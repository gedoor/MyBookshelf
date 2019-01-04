package com.kunfei.bookshelf.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.RxBusTag;
import com.kunfei.bookshelf.utils.Theme.ThemeStore;
import com.kunfei.bookshelf.view.activity.ThemeSettingActivity;

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */
public class ThemeSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ThemeSettingActivity settingActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("CONFIG");
        addPreferencesFromResource(R.xml.theme_settings);
        settingActivity = (ThemeSettingActivity) this.getActivity();
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            // For all other preferences, set the summary to the value's
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                preference.getContext().getSharedPreferences("CONFIG", Context.MODE_PRIVATE).getString(preference.getKey(), ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pk_ImmersionStatusBar)) || key.equals(getString(R.string.pk_navigationBarColorChange))) {
            settingActivity.initImmersionBar();
            RxBus.get().post(RxBusTag.IMMERSION_CHANGE, true);
        } else if (key.equals("colorPrimary")) {
            ThemeStore.editTheme(getActivity())
                    .primaryColor(sharedPreferences.getInt(key, getActivity().getResources().getColor(R.color.colorPrimary)))
                    .commit();
        } else if (key.equals("colorAccent")) {
            ThemeStore.editTheme(getActivity())
                    .accentColor(sharedPreferences.getInt(key, getActivity().getResources().getColor(R.color.colorAccent)))
                    .commit();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
