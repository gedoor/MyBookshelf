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

    fun backup(context: Context, uri: Uri?, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            BookshelfHelp.getAllBook().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.getFile(backupPath + File.separator + "myBookShelf.json").writeText(json)
                }
            }
            BookSourceManager.getAllBookSource().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.getFile(backupPath + File.separator + "myBookSource.json").writeText(json)
                }
            }
            DbHelper.getDaoSession().searchHistoryBeanDao.queryBuilder().list().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.getFile(backupPath + File.separator + "myBookSearchHistory.json")
                            .writeText(json)
                }
            }
            ReplaceRuleManager.getAll().blockingGet().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.getFile(backupPath + File.separator + "myBookReplaceRule.json").writeText(json)
                }
            }
            TxtChapterRuleManager.getAll().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileHelp.getFile(backupPath + File.separator + "myTxtChapterRule.json")
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
            if (uri != null) {
                copyBackup(context, uri)
            } else {
                copyBackup()
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        callBack?.backupSuccess()
                    }
                })
    }

    private fun copyBackup(context: Context, uri: Uri) {
        try {
            DocumentFile.fromTreeUri(context, uri)?.let { treeDoc ->
                for (fileName in backupFileNames) {
                    val doc = treeDoc.findFile(fileName) ?: treeDoc.createFile("", fileName)
                    doc?.let {
                        DocumentUtil.writeBytes(context, FileHelp.getFile(backupPath + File.separator + fileName).readBytes(), doc)
                    }
                }
            }
        } catch (e: Exception) {
            copyBackup()
        }
    }

    private fun copyBackup() {
        try {
            for (fileName in backupFileNames) {
                FileHelp.getFile(backupPath + File.separator + "bookshelf.json")
                        .copyTo(FileHelp.getFile(defaultPath + File.separator + "bookshelf.json"), true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface CallBack {
        fun backupSuccess()
    }
}