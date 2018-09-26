package com.monke.monkeybook.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.view.activity.SettingActivity;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final int REQUEST_CODE_OPEN_DIRECTORY = 101;
    private SettingActivity settingActivity;
    private Context mContext;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("CONFIG");
        addPreferencesFromResource(R.xml.pref_settings);
        mContext = this.getActivity();
        settingActivity = (SettingActivity) this.getActivity();
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        if (sharedPreferences.getString(getString(R.string.pk_download_path), "").equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pk_download_path), FileHelp.getCachePath());
            editor.apply();
        }
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_bookshelf_px)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_download_path)));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value)-> {
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
        if (key.equals(getString(R.string.pk_ImmersionStatusBar))) {
            settingActivity.initImmersionBar();
            RxBus.get().post(RxBusTag.IMMERSION_CHANGE, true);
        } else if (key.equals(getString(R.string.pk_bookshelf_px))) {
            RxBus.get().post(RxBusTag.UPDATE_PX, true);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(getString(R.string.pk_download_path))) {
            if (!EasyPermissions.hasPermissions(mContext, MApplication.PerList)) {
                EasyPermissions.requestPermissions(this, "自定义缓存路径需要存储权限", 0, MApplication.PerList);
                return true;
            }
//            Intent intent = null;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
//            }


        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {

        }
    }
}
