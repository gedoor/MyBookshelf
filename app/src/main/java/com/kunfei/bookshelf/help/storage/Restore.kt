package com.kunfei.bookshelf.help.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.kunfei.bookshelf.DbHelper
import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.base.observer.MySingleObserver
import com.kunfei.bookshelf.bean.*
import com.kunfei.bookshelf.help.FileHelp
import com.kunfei.bookshelf.help.LauncherIcon
import com.kunfei.bookshelf.help.ReadBookControl
import com.kunfei.bookshelf.model.BookSourceManager
import com.kunfei.bookshelf.model.ReplaceRuleManager
import com.kunfei.bookshelf.model.TxtChapterRuleManager
import com.kunfei.bookshelf.utils.DocumentUtil
import com.kunfei.bookshelf.utils.GSON
import com.kunfei.bookshelf.utils.fromJsonArray
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

object Restore {

    fun restore(context: Context, uri: Uri, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            DocumentFile.fromTreeUri(context, uri)?.listFiles()?.forEach { doc ->
                for (fileName in Backup.backupFileNames) {
                    if (doc.name == fileName) {
                        DocumentUtil.readBytes(context, doc.uri)?.let {
                            FileHelp.createFileIfNotExist(Backup.backupPath + File.separator + fileName)
                                    .writeBytes(it)
                        }
                    }
                }
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        restore(Backup.backupPath, callBack)
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }

    fun restore(path: String, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookShelf.json")
                val json = file.readText()
                GSON.fromJsonArray<BookShelfBean>(json)?.forEach { bookshelf ->
                    if (bookshelf.noteUrl != null) {
                        DbHelper.getDaoSession().bookShelfBeanDao.insertOrReplace(bookshelf)
                    }
                    if (bookshelf.bookInfoBean.noteUrl != null) {
                        DbHelper.getDaoSession().bookInfoBeanDao.insertOrReplace(bookshelf.bookInfoBean)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookSource.json")
                val json = file.readText()
                GSON.fromJsonArray<BookSourceBean>(json)?.let {
                    BookSourceManager.addBookSource(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookSearchHistory.json")
                val json = file.readText()
                GSON.fromJsonArray<SearchHistoryBean>(json)?.let {
                    DbHelper.getDaoSession().searchHistoryBeanDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookReplaceRule.json")
                val json = file.readText()
                GSON.fromJsonArray<ReplaceRuleBean>(json)?.let {
                    ReplaceRuleManager.addDataS(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myTxtChapterRule.json")
                val json = file.readText()
                GSON.fromJsonArray<TxtChapterRuleBean>(json)?.let {
                    TxtChapterRuleManager.save(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var donateHb = MApplication.getConfigPreferences().getLong("DonateHb", 0)
            donateHb = if (donateHb > System.currentTimeMillis()) 0 else donateHb
            Preferences.getSharedPreferences(MApplication.getInstance(), path, "config")?.all?.map {
                val edit = MApplication.getConfigPreferences().edit()
                when (val value = it.value) {
                    is Int -> edit.putInt(it.key, value)
                    is Boolean -> edit.putBoolean(it.key, value)
                    is Long -> edit.putLong(it.key, value)
                    is Float -> edit.putFloat(it.key, value)
                    is String -> edit.putString(it.key, value)
                    else -> Unit
                }
                edit.putLong("DonateHb", donateHb)
                edit.putInt("versionCode", MApplication.getVersionCode())
                edit.apply()
                LauncherIcon.ChangeIcon(MApplication.getConfigPreferences().getString("launcher_icon", MApplication.getInstance().getString(R.string.icon_main)))
                ReadBookControl.getInstance().updateReaderSettings()
                MApplication.getInstance().upThemeStore()
                MApplication.getInstance().initNightTheme()
                edit.commit()
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        callBack?.restoreSuccess()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }


    interface CallBack {
        fun restoreSuccess()
        fun restoreError(msg: String)
    }

}