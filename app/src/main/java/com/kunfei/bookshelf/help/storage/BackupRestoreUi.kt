package com.kunfei.bookshelf.help.storage

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import com.hwangjr.rxbus.RxBus
import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.base.observer.MySingleObserver
import com.kunfei.bookshelf.constant.RxBusTag
import com.kunfei.bookshelf.help.permission.Permissions
import com.kunfei.bookshelf.help.permission.PermissionsCompat
import com.kunfei.bookshelf.help.storage.WebDavHelp.getWebDavFileNames
import com.kunfei.bookshelf.help.storage.WebDavHelp.showRestoreDialog
import com.kunfei.bookshelf.widget.filepicker.picker.FilePicker
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import java.util.*

object BackupRestoreUi : Backup.CallBack, Restore.CallBack {

    private const val backupSelectRequestCode = 22
    private const val restoreSelectRequestCode = 33

    private fun getBackupPath(): String? {
        return MApplication.getConfigPreferences().getString("backupPath", null)
    }

    private fun setBackupPath(path: String?) {
        if (path.isNullOrEmpty()) {
            MApplication.getConfigPreferences().edit().remove("backupPath").apply()
        } else {
            MApplication.getConfigPreferences().edit().putString("backupPath", path).apply()
        }
    }

    override fun backupSuccess() {
        MApplication.getInstance().toast(R.string.backup_success)
    }

    override fun backupError(msg: String) {
        MApplication.getInstance().toast(msg)
    }

    override fun restoreSuccess() {
        MApplication.getInstance().toast(R.string.restore_success)
        RxBus.get().post(RxBusTag.RECREATE, true)
    }

    override fun restoreError(msg: String) {
        MApplication.getInstance().toast(msg)
    }

    fun backup(activity: Activity) {
        val backupPath = getBackupPath()
        if (backupPath.isNullOrEmpty()) {
            selectBackupFolder(activity)
        } else {
            if (backupPath.isContentPath()) {
                val uri = Uri.parse(backupPath)
                val doc = DocumentFile.fromTreeUri(activity, uri)
                if (doc?.canWrite() == true) {
                    Backup.backup(activity, backupPath, this)
                } else {
                    selectBackupFolder(activity)
                }
            } else {
                backupUsePermission(activity)
            }
        }
    }

    private fun backupUsePermission(activity: Activity, path: String = Backup.defaultPath) {
        PermissionsCompat.Builder(activity)
                .addPermissions(*Permissions.Group.STORAGE)
                .rationale(R.string.get_storage_per)
                .onGranted {
                    setBackupPath(path)
                    Backup.backup(activity, path, this)
                }
                .request()
    }

    fun selectBackupFolder(activity: Activity) {
        activity.alert {
            titleResource = R.string.select_folder
            items(activity.resources.getStringArray(R.array.select_folder).toList()) { _, index ->
                when (index) {
                    0 -> {
                        setBackupPath(Backup.defaultPath)
                        backupUsePermission(activity)
                    }
                    1 -> {
                        try {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivityForResult(intent, backupSelectRequestCode)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            activity.toast(e.localizedMessage ?: "ERROR")
                        }
                    }
                    2 -> {
                        PermissionsCompat.Builder(activity)
                                .addPermissions(*Permissions.Group.STORAGE)
                                .rationale(R.string.get_storage_per)
                                .onGranted {
                                    selectBackupFolderApp(activity, false)
                                }
                                .request()
                    }
                }
            }
        }.show()
    }

    private fun selectBackupFolderApp(activity: Activity, isRestore: Boolean) {
        val picker = FilePicker(activity, FilePicker.DIRECTORY)
        picker.setBackgroundColor(activity.resources.getColor(R.color.background))
        picker.setTopBackgroundColor(activity.resources.getColor(R.color.background))
        picker.setItemHeight(30)
        picker.setOnFilePickListener { currentPath: String ->
            setBackupPath(currentPath)
            if (isRestore) {
                Restore.restore(currentPath, this)
            } else {
                Backup.backup(activity, currentPath, this)
            }
        }
        picker.show()
    }

    fun restore(activity: Activity) {
        Single.create { emitter: SingleEmitter<ArrayList<String>?> ->
            emitter.onSuccess(getWebDavFileNames())
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<ArrayList<String>?>() {
                    override fun onSuccess(strings: ArrayList<String>) {
                        if (!showRestoreDialog(activity, strings, this@BackupRestoreUi)) {
                            val path = getBackupPath()
                            if (TextUtils.isEmpty(path)) {
                                selectRestoreFolder(activity)
                            } else {
                                if (path.isContentPath()) {
                                    val uri = Uri.parse(path)
                                    val doc = DocumentFile.fromTreeUri(activity, uri)
                                    if (doc?.canWrite() == true) {
                                        Restore.restore(activity, Uri.parse(path), this@BackupRestoreUi)
                                    } else {
                                        selectRestoreFolder(activity)
                                    }
                                } else {
                                    restoreUsePermission(activity)
                                }
                            }
                        }
                    }
                })
    }

    private fun restoreUsePermission(activity: Activity, path: String = Backup.defaultPath) {
        PermissionsCompat.Builder(activity)
                .addPermissions(*Permissions.Group.STORAGE)
                .rationale(R.string.get_storage_per)
                .onGranted {
                    setBackupPath(path)
                    Restore.restore(path, this)
                }
                .request()
    }

    private fun selectRestoreFolder(activity: Activity) {
        activity.alert {
            titleResource = R.string.select_folder
            items(activity.resources.getStringArray(R.array.select_folder).toList()) { _, index ->
                when (index) {
                    0 -> restoreUsePermission(activity)
                    1 -> {
                        try {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivityForResult(intent, restoreSelectRequestCode)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            activity.toast(e.localizedMessage ?: "ERROR")
                        }
                    }
                    2 -> {
                        PermissionsCompat.Builder(activity)
                                .addPermissions(*Permissions.Group.STORAGE)
                                .rationale(R.string.get_storage_per)
                                .onGranted {
                                    selectBackupFolderApp(activity, true)
                                }
                                .request()
                    }
                }
            }
        }.show()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            backupSelectRequestCode -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    MApplication.getInstance().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    setBackupPath(uri.toString())
                    Backup.backup(MApplication.getInstance(), uri.toString(), this)
                }
            }
            restoreSelectRequestCode -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    MApplication.getInstance().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    setBackupPath(uri.toString())
                    Restore.restore(MApplication.getInstance(), uri, this)
                }
            }
        }
    }

}

fun String?.isContentPath(): Boolean = this?.startsWith("content://") == true