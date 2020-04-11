package com.kunfei.bookshelf.view.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import com.hwangjr.rxbus.RxBus
import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.constant.RxBusTag
import com.kunfei.bookshelf.help.BookshelfHelp
import com.kunfei.bookshelf.help.FileHelp
import com.kunfei.bookshelf.help.ProcessTextHelp
import com.kunfei.bookshelf.help.permission.Permissions
import com.kunfei.bookshelf.help.permission.PermissionsCompat
import com.kunfei.bookshelf.help.storage.BackupRestoreUi.selectBackupFolder
import com.kunfei.bookshelf.service.WebService
import com.kunfei.bookshelf.utils.FileUtils
import com.kunfei.bookshelf.view.activity.SettingActivity
import com.kunfei.bookshelf.widget.filepicker.picker.FilePicker
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */
@Suppress("DEPRECATION")
class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {
    private var settingActivity: SettingActivity? = null
    private lateinit var bookshelfPxKey: String
    private lateinit var downloadPathKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = "CONFIG"
        settingActivity = activity as? SettingActivity
        settingActivity?.setupActionBar(getString(R.string.setting))
        addPreferencesFromResource(R.xml.pref_settings)
        val sharedPreferences = preferenceManager.sharedPreferences
        val editor = sharedPreferences.edit()
        val processTextEnabled = ProcessTextHelp.isProcessTextEnabled()
        editor.putBoolean("process_text", processTextEnabled)
        if (sharedPreferences.getString(getString(R.string.pk_download_path), "") == "") {
            editor.putString(getString(R.string.pk_download_path), FileHelp.getCachePath())
        }
        editor.apply()
        bookshelfPxKey = getString(R.string.pk_bookshelf_px)
        downloadPathKey = getString(R.string.pk_download_path)
        upPreferenceSummary(bookshelfPxKey, sharedPreferences.getString(bookshelfPxKey, "0"))
        upPreferenceSummary(downloadPathKey, MApplication.downloadPath)
        upPreferenceSummary("backupPath", sharedPreferences.getString("backupPath", null))
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            bookshelfPxKey -> {
                upPreferenceSummary(key, sharedPreferences.getString(key, "0"))
                RxBus.get().post(RxBusTag.RECREATE, true)
            }
            "behaviorMain" -> RxBus.get().post(RxBusTag.RECREATE, true)
            "process_text" -> ProcessTextHelp.setProcessTextEnable(sharedPreferences.getBoolean("process_text", true))
            "webPort" -> WebService.upHttpServer(activity)
            "backupPath" -> upPreferenceSummary(key, sharedPreferences.getString(key, null))
            downloadPathKey -> upPreferenceSummary(downloadPathKey, MApplication.downloadPath)
        }

        if (key == bookshelfPxKey || key == "behaviorMain") {
            RxBus.get().post(RxBusTag.RECREATE, true)
        } else if (key == "process_text") {
            ProcessTextHelp.setProcessTextEnable(sharedPreferences.getBoolean("process_text", true))
        } else if (key == "webPort") {
            WebService.upHttpServer(activity)
        }
    }

    private fun upPreferenceSummary(preferenceKey: String, value: String?) {
        val preference = findPreference(preferenceKey) ?: return
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(value)
            // Set the summary to reflect the new value.
            preference.summary = if (index >= 0) preference.entries[index] else null
        } else {
            preference.summary = value
        }
    }

    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pk_download_path) -> {
                selectDownloadPath(preference)
            }
            "backupPath" -> {
                selectBackupFolder(activity)
            }
            "webDavSetting" -> {
                val webDavSettingsFragment = WebDavSettingsFragment()
                fragmentManager.beginTransaction().replace(R.id.settingsFrameLayout, webDavSettingsFragment, "webDavSettings").commit()
            }
            "clearCache" -> {
                alert {
                    titleResource = R.string.clear_cache
                    messageResource = R.string.sure_del_download_book
                    okButton {
                        BookshelfHelp.clearCaches(true)
                    }
                    noButton {
                        BookshelfHelp.clearCaches(false)
                    }
                }.show()
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    private fun selectDownloadPath(preference: Preference) {
        PermissionsCompat.Builder(activity)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.set_download_per)
                .onGranted {
                    val picker = FilePicker(activity, FilePicker.DIRECTORY)
                    picker.setBackgroundColor(resources.getColor(R.color.background))
                    picker.setTopBackgroundColor(resources.getColor(R.color.background))
                    picker.setRootPath(preference.summary.toString())
                    picker.setItemHeight(30)
                    picker.setOnFilePickListener { currentPath: String ->
                        if (!currentPath.contains(FileUtils.getSdCardPath())) {
                            MApplication.getInstance().setDownloadPath(null)
                        } else {
                            MApplication.getInstance().setDownloadPath(currentPath)
                        }
                        preference.summary = MApplication.downloadPath
                    }
                    picker.show()
                    picker.cancelButton.setText(R.string.restore_default)
                    picker.cancelButton.setOnClickListener {
                        picker.dismiss()
                        MApplication.getInstance().setDownloadPath(null)
                        preference.summary = MApplication.downloadPath
                    }
                }
                .request()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
    }

}