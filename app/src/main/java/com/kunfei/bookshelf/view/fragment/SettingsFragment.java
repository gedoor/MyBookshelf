package com.kunfei.bookshelf.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import androidx.appcompat.app.AlertDialog;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.help.ProcessTextHelp;
import com.kunfei.bookshelf.help.permission.Permissions;
import com.kunfei.bookshelf.help.permission.PermissionsCompat;
import com.kunfei.bookshelf.service.WebService;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.view.activity.SettingActivity;
import com.kunfei.bookshelf.widget.filepicker.picker.FilePicker;

import java.util.Objects;

import kotlin.Unit;

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingActivity settingActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("CONFIG");
        settingActivity = (SettingActivity) this.getActivity();
        settingActivity.setupActionBar(getString(R.string.setting));
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean processTextEnabled = ProcessTextHelp.isProcessTextEnabled();
        editor.putBoolean("process_text", processTextEnabled);
        if (Objects.equals(sharedPreferences.getString(getString(R.string.pk_download_path), ""), "")) {
            editor.putString(getString(R.string.pk_download_path), FileHelp.getCachePath());
        }
        editor.apply();
        addPreferencesFromResource(R.xml.pref_settings);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_bookshelf_px)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pk_download_path)));
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
        if (key.equals(getString(R.string.pk_bookshelf_px)) || key.equals("behaviorMain")) {
            RxBus.get().post(RxBusTag.RECREATE, true);
        } else if (key.equals("process_text")) {
            ProcessTextHelp.setProcessTextEnable(sharedPreferences.getBoolean("process_text", true));
        } else if (key.equals("webPort")) {
            WebService.upHttpServer(settingActivity);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(getString(R.string.pk_download_path))) {
            selectDownloadPath(preference);
        } else if (preference.getKey().equals("webDavSetting")) {
            WebDavSettingsFragment webDavSettingsFragment = new WebDavSettingsFragment();
            getFragmentManager().beginTransaction().replace(R.id.settingsFrameLayout, webDavSettingsFragment, "webDavSettings").commit();
        } else if (preference.getKey().equals("clearCache")) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.clear_cache)
                    .setMessage(getString(R.string.sure_del_download_book))
                    .setPositiveButton(R.string.yes, (dialog, which) -> BookshelfHelp.clearCaches(true))
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> BookshelfHelp.clearCaches(false))
                    .show();
            ATH.setAlertDialogTint(alertDialog);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void selectDownloadPath(Preference preference) {
        new PermissionsCompat.Builder(settingActivity)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.set_download_per)
                .onGranted((requestCode) -> {
                    FilePicker picker = new FilePicker(getActivity(), FilePicker.DIRECTORY);
                    picker.setBackgroundColor(getResources().getColor(R.color.background));
                    picker.setTopBackgroundColor(getResources().getColor(R.color.background));
                    picker.setRootPath(preference.getSummary().toString());
                    picker.setItemHeight(30);
                    picker.setOnFilePickListener(currentPath -> {
                        if (!currentPath.contains(FileUtils.getSdCardPath())) {
                            MApplication.getInstance().setDownloadPath(null);
                        } else {
                            MApplication.getInstance().setDownloadPath(currentPath);
                        }
                        preference.setSummary(MApplication.downloadPath);
                    });
                    picker.show();
                    picker.getCancelButton().setText(R.string.restore_default);
                    picker.getCancelButton().setOnClickListener(view -> {
                        picker.dismiss();
                        MApplication.getInstance().setDownloadPath(null);
                        preference.setSummary(MApplication.downloadPath);
                    });
                    return Unit.INSTANCE;
                })
                .request();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
