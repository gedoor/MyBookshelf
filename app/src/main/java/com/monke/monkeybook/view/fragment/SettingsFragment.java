package com.monke.monkeybook.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.monke.monkeybook.R;
import com.monke.monkeybook.view.activity.SettingActivity;

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingActivity settingActivity;
    private Context mContext;
    public static final String ImmersionAction = "immersion.broadcast.action";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        mContext = this.getActivity();
        settingActivity = (SettingActivity) this.getActivity();

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_screen_direction)));
        bindPreferenceSummaryToValue(findPreference(mContext.getString(R.string.pk_bookshelf_px)));
//        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_read_type)));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value)-> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        }
        else {
            // For all other preferences, set the summary to the value's
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
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

        Preference preference = findPreference(getString(R.string.pk_ImmersionStatusBar));
        preference.setOnPreferenceClickListener(preference1 -> {
            settingActivity.initImmersionBar();
            Intent intent = new Intent(ImmersionAction);
            intent.putExtra("data", "Immersion_Change");
            mContext.sendBroadcast(intent);
            return true;
        });
    }
}
