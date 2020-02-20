package com.kunfei.bookshelf.help.storage

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.base.observer.MySingleObserver
import com.kunfei.bookshelf.constant.AppConstant
import com.kunfei.bookshelf.help.FileHelp
import com.kunfei.bookshelf.utils.ZipUtils
import com.kunfei.bookshelf.utils.webdav.WebDav
import com.kunfei.bookshelf.utils.webdav.http.HttpAuth
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

object WebDavHelp {
    private val zipFilePath = FileHelp.getCachePath() + "/backup" + ".zip"
    private val unzipFilesPath by lazy {
        FileHelp.getCachePath()
    }

    private fun getWebDavUrl(): String {
        var url = MApplication.getConfigPreferences().getString("web_dav_url", AppConstant.DEFAULT_WEB_DAV_URL)
        if (url.isNullOrEmpty()) {
            url = AppConstant.DEFAULT_WEB_DAV_URL
        }
        if (!url.endsWith("/")) url += "/"
        return url
    }

    private fun initWebDav(): Boolean {
        val account = MApplication.getConfigPreferences().getString("web_dav_account", "")
        val password = MApplication.getConfigPreferences().getString("web_dav_password", "")
        if (!account.isNullOrBlank() && !password.isNullOrBlank()) {
            HttpAuth.auth = HttpAuth.Auth(account, password)
            return true
        }
        return false
    }

    fun getWebDavFileNames(): ArrayList<String> {
        val url = getWebDavUrl()
        val names = arrayListOf<String>()
        try {
            if (initWebDav()) {
                var files = WebDav(url + "YueDu/").listFiles()
                files = files.reversed()
                for (index: Int in 0 until min(10, files.size)) {
                    files[index].displayName?.let {
                        names.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return names
    }

    fun showRestoreDialog(context: Context, names: ArrayList<String>, callBack: Restore.CallBack?): Boolean {
        return if (names.isNotEmpty()) {
            context.selector(title = "选择恢复文件", items = names) { _, index ->
                if (index in 0 until names.size) {
                    restoreWebDav(names[index], callBack)
                }
            }
            true
        } else {
            false
        }
    }

    private fun restoreWebDav(name: String, callBack: Restore.CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            getWebDavUrl().let {
                val file = WebDav(it + "YueDu/" + name)
                file.downloadTo(zipFilePath, true)
                @Suppress("BlockingMethodInNonBlockingContext")
                ZipUtils.unzipFile(zipFilePath, unzipFilesPath)
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        Restore.restore(unzipFilesPath, callBack)
                    }
                })
    }

    fun backUpWebDav(path: String) {
        try {
            if (initWebDav()) {
                val paths = arrayListOf(*Backup.backupFileNames)
                for (i in 0 until paths.size) {
                    paths[i] = path + File.separator + paths[i]
                }
                FileHelp.deleteFile(zipFilePath)
                if (ZipUtils.zipFiles(paths, zipFilePath)) {
                    WebDav(getWebDavUrl() + "YueDu").makeAsDir()
                    val putUrl = getWebDavUrl() + "YueDu/backup" +
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(Date(System.currentTimeMillis())) + ".zip"
                    WebDav(putUrl).upload(zipFilePath)
                }
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                MApplication.getInstance().toast("WebDav\n${e.localizedMessage}")
            }
        }
    }
}