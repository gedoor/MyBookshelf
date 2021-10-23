//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.databinding.PopReadInterfaceBinding
import com.kunfei.bookshelf.help.ReadBookControl
import com.kunfei.bookshelf.help.permission.Permissions
import com.kunfei.bookshelf.help.permission.PermissionsCompat
import com.kunfei.bookshelf.utils.*
import com.kunfei.bookshelf.utils.theme.ATH
import com.kunfei.bookshelf.view.activity.ReadBookActivity
import com.kunfei.bookshelf.view.activity.ReadStyleActivity
import com.kunfei.bookshelf.widget.font.FontSelector
import com.kunfei.bookshelf.widget.font.FontSelector.OnThisListener
import com.kunfei.bookshelf.widget.page.animation.PageAnimation
import timber.log.Timber

class ReadInterfacePop : FrameLayout {
    private val binding = PopReadInterfaceBinding.inflate(
        LayoutInflater.from(
            context
        ), this, true
    )
    private var activity: ReadBookActivity? = null
    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        binding.vwBg.setOnClickListener(null)
    }

    fun setListener(readBookActivity: ReadBookActivity, callback: Callback) {
        activity = readBookActivity
        this.callback = callback
        initData()
        bindEvent()
    }

    @SuppressLint("DefaultLocale")
    private fun initData() {
        setBg()
        updateBg(readBookControl.textDrawableIndex)
        updateBoldText(readBookControl.textBold)
        updatePageMode(readBookControl.pageMode)
        binding.nbTextSize.text = String.format("%d", readBookControl.textSize)
    }

    /**
     * 控件事件
     */
    @SuppressLint("DefaultLocale")
    private fun bindEvent() {
        //字号减
        binding.nbTextSizeDec.setOnClickListener {
            var fontSize = readBookControl.textSize - 1
            if (fontSize < 10) fontSize = 10
            readBookControl.textSize = fontSize
            binding.nbTextSize.text = String.format("%d", readBookControl.textSize)
            callback!!.upTextSize()
        }
        //字号加
        binding.nbTextSizeAdd.setOnClickListener {
            var fontSize = readBookControl.textSize + 1
            if (fontSize > 40) fontSize = 40
            readBookControl.textSize = fontSize
            binding.nbTextSize.text = String.format("%d", readBookControl.textSize)
            callback!!.upTextSize()
        }
        //缩进
        binding.flIndent.setOnClickListener {
            val dialog = AlertDialog.Builder(
                activity!!, R.style.alertDialogTheme
            )
                .setTitle(activity!!.getString(R.string.indent))
                .setSingleChoiceItems(
                    activity!!.resources.getStringArray(R.array.indent),
                    readBookControl.indent
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.indent = i
                    callback!!.refresh()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        //翻页模式
        binding.tvPageMode.setOnClickListener {
            val dialog = AlertDialog.Builder(
                activity!!, R.style.alertDialogTheme
            )
                .setTitle(activity!!.getString(R.string.page_mode))
                .setSingleChoiceItems(
                    PageAnimation.Mode.getAllPageMode(),
                    readBookControl.pageMode
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.pageMode = i
                    updatePageMode(i)
                    callback!!.upPageMode()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        //加粗切换
        binding.flTextBold.setOnClickListener {
            readBookControl.textBold = !readBookControl.textBold
            updateBoldText(readBookControl.textBold)
            callback!!.upTextSize()
        }
        //行距单倍
        binding.tvRowDef0.setOnClickListener {
            readBookControl.lineMultiplier = 0.6f
            readBookControl.paragraphSize = 1.5f
            callback!!.upTextSize()
        }
        //行距双倍
        binding.tvRowDef1.setOnClickListener {
            readBookControl.lineMultiplier = 1.2f
            readBookControl.paragraphSize = 1.8f
            callback!!.upTextSize()
        }
        //行距三倍
        binding.tvRowDef2.setOnClickListener {
            readBookControl.lineMultiplier = 1.8f
            readBookControl.paragraphSize = 2.0f
            callback!!.upTextSize()
        }
        //行距默认
        binding.tvRowDef.setOnClickListener {
            readBookControl.lineMultiplier = 1.0f
            readBookControl.paragraphSize = 1.8f
            callback!!.upTextSize()
        }
        //自定义间距
        binding.tvOther.setOnClickListener { activity!!.readAdjustMarginIn() }
        //背景选择
        binding.civBgWhite.setOnClickListener {
            updateBg(0)
            callback!!.bgChange()
        }
        binding.civBgYellow.setOnClickListener {
            updateBg(1)
            callback!!.bgChange()
        }
        binding.civBgGreen.setOnClickListener {
            updateBg(2)
            callback!!.bgChange()
        }
        binding.civBgBlue.setOnClickListener {
            updateBg(3)
            callback!!.bgChange()
        }
        binding.civBgBlack.setOnClickListener {
            updateBg(4)
            callback!!.bgChange()
        }
        //自定义阅读样式
        binding.civBgWhite.setOnLongClickListener { customReadStyle(0) }
        binding.civBgYellow.setOnLongClickListener { customReadStyle(1) }
        binding.civBgGreen.setOnLongClickListener { customReadStyle(2) }
        binding.civBgBlue.setOnLongClickListener { customReadStyle(3) }
        binding.civBgBlack.setOnLongClickListener { customReadStyle(4) }

        //选择字体
        binding.flTextFont.setOnClickListener {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                activity!!.selectFontDir()
            } else {
                PermissionsCompat.Builder(activity!!)
                    .addPermissions(
                        Permissions.READ_EXTERNAL_STORAGE,
                        Permissions.WRITE_EXTERNAL_STORAGE
                    )
                    .rationale(R.string.get_storage_per)
                    .onGranted {
                        kotlin.runCatching {
                            selectFont(
                                DocumentUtils.listFiles(FileUtils.getSdCardPath() + "/Fonts") {
                                    it.name.matches(FontSelector.fontRegex)
                                }
                            )
                        }.onFailure {
                            context.toastOnUi("获取文件出错\n${it.localizedMessage}")
                        }
                    }
                    .request()
            }
        }

        //长按清除字体
        binding.flTextFont.setOnLongClickListener {
            clearFontPath()
            activity!!.toast(R.string.clear_font)
            true
        }
    }

    fun showFontSelector(uri: Uri) {
        kotlin.runCatching {
            val doc = DocumentFile.fromTreeUri(context, uri)
            DocumentUtils.listFiles(doc!!.uri) {
                it.name.matches(FontSelector.fontRegex)
            }.let {
                selectFont(it)
            }
        }.onFailure {
            context.toastOnUi("获取文件列表出错\n${it.localizedMessage}")
            Timber.e(it)
        }
    }

    private fun selectFont(docItems: List<FileDoc?>?) {
        FontSelector(context, readBookControl.fontPath)
            .setListener(object : OnThisListener {
                override fun setDefault() {
                    clearFontPath()
                }

                override fun setFontPath(fileDoc: FileDoc) {
                    setReadFonts(fileDoc)
                }
            })
            .create(docItems)
            .show()
    }

    //自定义阅读样式
    private fun customReadStyle(index: Int): Boolean {
        val intent = Intent(activity, ReadStyleActivity::class.java)
        intent.putExtra("index", index)
        activity!!.startActivity(intent)
        return false
    }

    //设置字体
    fun setReadFonts(fileDoc: FileDoc) {
        if (fileDoc.isContentScheme) {
            val file = FileUtils.createFileIfNotExist(context.externalFiles, "Fonts", fileDoc.name)
            file.writeBytes(fileDoc.uri.readBytes(context))
            readBookControl.setReadBookFont(file.absolutePath)
        } else {
            readBookControl.setReadBookFont(fileDoc.uri.toString())
        }
        callback!!.refresh()
    }

    //清除字体
    fun clearFontPath() {
        readBookControl.setReadBookFont(null)
        callback!!.refresh()
    }

    private fun updatePageMode(pageMode: Int) {
        binding.tvPageMode.text = String.format("%s", PageAnimation.Mode.getPageMode(pageMode))
    }

    private fun updateBoldText(isBold: Boolean) {
        binding.flTextBold.isSelected = isBold
    }

    fun setBg() {
        binding.tv0.setTextColor(readBookControl.getTextColor(0))
        binding.tv1.setTextColor(readBookControl.getTextColor(1))
        binding.tv2.setTextColor(readBookControl.getTextColor(2))
        binding.tv3.setTextColor(readBookControl.getTextColor(3))
        binding.tv4.setTextColor(readBookControl.getTextColor(4))
        binding.civBgWhite.setImageDrawable(readBookControl.getBgDrawable(0, activity, 100, 180))
        binding.civBgYellow.setImageDrawable(readBookControl.getBgDrawable(1, activity, 100, 180))
        binding.civBgGreen.setImageDrawable(readBookControl.getBgDrawable(2, activity, 100, 180))
        binding.civBgBlue.setImageDrawable(readBookControl.getBgDrawable(3, activity, 100, 180))
        binding.civBgBlack.setImageDrawable(readBookControl.getBgDrawable(4, activity, 100, 180))
    }

    private fun updateBg(index: Int) {
        binding.civBgWhite.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgYellow.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgGreen.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgBlack.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgBlue.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        when (index) {
            0 -> binding.civBgWhite.borderColor = Color.parseColor("#F3B63F")
            1 -> binding.civBgYellow.borderColor = Color.parseColor("#F3B63F")
            2 -> binding.civBgGreen.borderColor = Color.parseColor("#F3B63F")
            3 -> binding.civBgBlue.borderColor = Color.parseColor("#F3B63F")
            4 -> binding.civBgBlack.borderColor = Color.parseColor("#F3B63F")
        }
        readBookControl.textDrawableIndex = index
    }

    interface Callback {
        fun upPageMode()
        fun upTextSize()
        fun upMargin()
        fun bgChange()
        fun refresh()
    }
}