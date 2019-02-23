package com.kunfei.bookshelf.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.view.activity.ThemeSettingActivity;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;

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
        addPreferencesFromResource(R.xml.pref_settings_theme);
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
        AlertDialog alertDialog;
        switch (key) {
            case "immersionStatusBar":
            case "navigationBarColorChange":
                settingActivity.initImmersionBar();
                RxBus.get().post(RxBusTag.IMMERSION_CHANGE, true);
                break;
            case "colorPrimary":
            case "colorAccent":
            case "colorBackground":
                if (!ColorUtil.isColorLight(sharedPreferences.getInt("colorBackground", settingActivity.getResources().getColor(R.color.md_grey_100)))) {
                    alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("白天背景太暗")
                            .setMessage("将会恢复默认背景？")
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                settingActivity.preferences.edit().putInt("colorBackground", settingActivity.getResources().getColor(R.color.md_grey_100)).apply();
                                upTheme(false);
                            })
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> upTheme(false))
                            .show();
                    ATH.setAlertDialogTint(alertDialog);
                } else {
                    upTheme(false);
                }
                break;
            case "colorPrimaryNight":
            case "colorAccentNight":
            case "colorBackgroundNight":
                if (ColorUtil.isColorLight(sharedPreferences.getInt("colorBackgroundNight", settingActivity.getResources().getColor(R.color.md_grey_800)))) {
                    alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("夜间背景太亮")
                            .setMessage("将会恢复默认背景？")
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                settingActivity.preferences.edit().putInt("colorBackgroundNight", settingActivity.getResources().getColor(R.color.md_grey_800)).apply();
                                upTheme(true);
                            })
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> upTheme(true))
                            .show();
                    ATH.setAlertDialogTint(alertDialog);

                } else {
                    upTheme(true);
                }
                break;
        }
    }

    private void upTheme(boolean isNightTheme) {
        if (settingActivity.isNightTheme() == isNightTheme) {
            MApplication.getInstance().upThemeStore();
            RxBus.get().post(RxBusTag.RECREATE, true);
            new Handler().postDelayed(() -> getActivity().recreate(), 200);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Objects.equals(preference.getKey(), "defaultTheme")) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("恢复默认主题")
                    .setMessage("是否确认恢复？")
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        settingActivity.preferences.edit()
                                .putInt("colorPrimary", settingActivity.getResources().getColor(R.color.md_grey_100))
                                .putInt("colorAccent", settingActivity.getResources().getColor(R.color.md_pink_600))
                                .putInt("colorBackground", settingActivity.getResources().getColor(R.color.md_grey_100))
                                .putInt("colorPrimaryNight", settingActivity.getResources().getColor(R.color.md_grey_800))
                                .putInt("colorAccentNight", settingActivity.getResources().getColor(R.color.md_pink_800))
                                .putInt("colorBackgroundNight", settingActivity.getResources().getColor(R.color.md_grey_800))
                                .apply();
                        MApplication.getInstance().upThemeStore();
                        RxBus.get().post(RxBusTag.RECREATE, true);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {})
                    .show();
            ATH.setAlertDialogTint(alertDialog);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
