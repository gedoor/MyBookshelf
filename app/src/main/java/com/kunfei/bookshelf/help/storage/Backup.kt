package com.kunfei.bookshelf.help.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.kunfei.bookshelf.DbHelper
import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.base.observer.MySingleObserver
import com.kunfei.bookshelf.help.BookshelfHelp
import com.kunfei.bookshelf.help.FileHelp
import com.kunfei.bookshelf.model.BookSourceManager
import com.kunfei.bookshelf.model.ReplaceRuleManager
import com.kunfei.bookshelf.model.TxtChapterRuleManager
import com.kunfei.bookshelf.utils.DocumentUtil
import com.kunfei.bookshelf.utils.FileUtils
import com.kunfei.bookshelf.utils.GSON
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit


object Backup {

    val backupPath = MApplication.getInstance().filesDir.absolutePath + File.separator + "backup"

    val defaultPath by lazy {
        FileUtils.getSdCardPath() + File.separator + "YueDu"
    }

    val backupFileNames by lazy {
        arrayOf(
                "myBookShelf.json",
                "myBookSource.json",
                "myBookSearchHistory.json",
                "myBookReplaceRule.json",
                "myTxtChapterRule.json",
                "config.xml"
        )
    }

    fun autoBack() {
        val lastBackup = MApplication.getConfigPreferences().getLong("lastBackup", 0)
        if (System.currentTimeMillis() - lastBackup < TimeUnit.DAYS.toMillis(1)) {
            return
        }
        val path = MApplication.getConfigPreferences().getString("backupPath", defaultPath)
        if (path == null) {
            backup(MApplication.getInstance(), defaultPath, null, true)
        } else {
            backup(MApplication.getInstance(), path, null, true)
        }
    }

    fun backup(context: Context, path: String, callBack: CallBack?, isAuto: Boolean = false) {
        MApplication.getConfigPreferences().edit().putLong("lastBackup", System.currentTimeMillis()).apply()
        Single.create(SingleOnSubscribe<Boolean> { e ->
            BookshelfHelp.getAllBook().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookShelf.json").writeText(json)
                }
            }
            BookSourceManager.getAllBookSource().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookSource.json").writeText(json)
                }
            }
            DbHelper.getDaoSession().searchHistoryBeanDao.queryBuilder().list().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookSearchHistory.json")
                            .writeText(json)
                }
            }
            ReplaceRuleManager.getAll().blockingGet().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookReplaceRule.json").writeText(json)
                }
            }
            TxtChapterRuleManager.getAll().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.createFileIfNotExist(backupPath + File.separator + "myTxtChapterRule.json")
                            .writeText(json)
                }
            }
            Preferences.getSharedPreferences(context, backupPath, "config")?.let { sp ->
                val edit = sp.edit()
                MApplication.getConfigPreferences().all.map {
                    when (val value = it.value) {
                        is Int -> edit.putInt(it.key, value)
                        is Boolean -> edit.putBoolean(it.key, value)
                        is Long -> edit.putLong(it.key, value)
                        is Float -> edit.putFloat(it.key, value)
                        is String -> edit.putString(it.key, value)
                        else -> Unit
                    }
                }
                edit.commit()
            }
            WebDavHelp.backUpWebDav(backupPath)
            if (path.isContentPath()) {
                copyBackup(context, Uri.parse(path), isAuto)
            } else {
                copyBackup(path, isAuto)
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        callBack?.backupSuccess()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.backupError(e.localizedMessage ?: "ERROR")
                    }
                })
    }

    @Throws(Exception::class)
    private fun copyBackup(context: Context, uri: Uri, isAuto: Boolean) {
        synchronized(this) {
            DocumentFile.fromTreeUri(context, uri)?.let { treeDoc ->
                for (fileName in backupFileNames) {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        if (isAuto) {
                            treeDoc.findFile("auto")?.findFile(fileName)?.delete()
                            var autoDoc = treeDoc.findFile("auto")
                            if (autoDoc == null) {
                                autoDoc = treeDoc.createDirectory("auto")
                            }
                            autoDoc?.createFile("", fileName)?.let {
                                DocumentUtil.writeBytes(context, file.readBytes(), it)
                            }
                        } else {
                            treeDoc.findFile(fileName)?.delete()
                            treeDoc.createFile("", fileName)?.let {
                                DocumentUtil.writeBytes(context, file.readBytes(), it)
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(java.lang.Exception::class)
    private fun copyBackup(path: String, isAuto: Boolean) {
        synchronized(this) {
            for (fileName in backupFileNames) {
                if (isAuto) {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        file.copyTo(FileHelp.createFileIfNotExist(path + File.separator + "auto" + File.separator + fileName), true)
                    }
                } else {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        file.copyTo(FileHelp.createFileIfNotExist(path + File.separator + fileName), true)
                    }
                }
            }
        }
    }

    interface CallBack {
        fun backupSuccess()
        fun backupError(msg: String)
    }
}