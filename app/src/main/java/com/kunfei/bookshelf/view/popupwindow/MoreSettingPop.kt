//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.hwangjr.rxbus.RxBus
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.constant.RxBusTag
import com.kunfei.bookshelf.databinding.PopMoreSettingBinding
import com.kunfei.bookshelf.help.ReadBookControl
import com.kunfei.bookshelf.utils.theme.ATH
import com.kunfei.bookshelf.widget.modialog.PageKeyDialog
import org.jetbrains.anko.sdk27.listeners.onClick

class MoreSettingPop : FrameLayout {

    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null
    private val binding = PopMoreSettingBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        binding.vwBg.setOnClickListener(null)
    }

    fun setListener(callback: Callback) {
        this.callback = callback
        initData()
        bindEvent()
    }

    private fun bindEvent() {
        setOnClickListener { this.visibility = View.GONE }
        binding.sbImmersionStatusBar.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.immersionStatusBar = b
                callback?.upBar()
                RxBus.get().post(RxBusTag.RECREATE, true)
            }
        }
        binding.sbLightNovelParagraph.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.lightNovelParagraph = isChecked
                callback?.recreate()
            }
        }
        binding.sbHideStatusBar.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.hideStatusBar = isChecked
                callback?.recreate()
            }
        }
        binding.sbToLh.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.toLh = isChecked
                callback?.recreate()
            }
        }
        binding.sbHideNavigationBar.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.hideNavigationBar = isChecked
                initData()
                callback?.recreate()
            }
        }
        binding.swVolumeNextPage.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.canKeyTurn = b
                upView()
            }
        }
        binding.swReadAloudKey.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.aloudCanKeyTurn = b
            }
        }
        binding.sbClick.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.canClickTurn = isChecked
                upView()
            }
        }
        binding.sbClickAllNext.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.clickAllNext = isChecked
            }
        }
        binding.sbShowTitle.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.showTitle = isChecked
                callback?.refreshPage()
            }
        }
        binding.sbShowTimeBattery.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.showTimeBattery = isChecked
                callback?.refreshPage()
            }
        }
        binding.sbShowLine.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.showLine = isChecked
                callback?.refreshPage()
            }
        }
        binding.llScreenTimeOut.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.keep_light))
                    .setSingleChoiceItems(
                            context.resources.getStringArray(R.array.screen_time_out),
                            readBookControl.screenTimeOut
                    ) { dialogInterface: DialogInterface, i: Int ->
                        readBookControl.screenTimeOut = i
                        upScreenTimeOut(i)
                        callback?.keepScreenOnChange(i)
                        dialogInterface.dismiss()
                    }
                    .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        binding.llJFConvert.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.jf_convert))
                    .setSingleChoiceItems(context.resources.getStringArray(R.array.convert_s), readBookControl.textConvert) { dialogInterface: DialogInterface, i: Int ->
                        readBookControl.textConvert = i
                        upFConvert(i)
                        dialogInterface.dismiss()
                        callback?.refreshPage()
                    }
                    .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        binding.llScreenDirection.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.screen_direction))
                    .setSingleChoiceItems(context.resources.getStringArray(R.array.screen_direction_list_title), readBookControl.screenDirection) { dialogInterface: DialogInterface, i: Int ->
                        readBookControl.screenDirection = i
                        upScreenDirection(i)
                        dialogInterface.dismiss()
                        callback?.recreate()
                    }
                    .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        binding.llNavigationBarColor.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.re_navigation_bar_color))
                    .setSingleChoiceItems(context.resources.getStringArray(R.array.NavBarColors), readBookControl.navBarColor) { dialogInterface: DialogInterface, i: Int ->
                        readBookControl.navBarColor = i
                        upNavBarColor(i)
                        dialogInterface.dismiss()
                        callback?.recreate()
                    }
                    .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        binding.sbSelectText.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.isCanSelectText = isChecked
            }
        }
        binding.llClickKeyCode.onClick {
            PageKeyDialog(context).show()
        }
    }

    private fun initData() {
        upScreenDirection(readBookControl.screenDirection)
        upScreenTimeOut(readBookControl.screenTimeOut)
        upFConvert(readBookControl.textConvert)
        upNavBarColor(readBookControl.navBarColor)
        binding.sbImmersionStatusBar.isChecked = readBookControl.immersionStatusBar
        binding.swVolumeNextPage.isChecked = readBookControl.canKeyTurn
        binding.swReadAloudKey.isChecked = readBookControl.aloudCanKeyTurn
        binding.sbLightNovelParagraph.isChecked = readBookControl.lightNovelParagraph;
        binding.sbHideStatusBar.isChecked = readBookControl.hideStatusBar
        binding.sbToLh.isChecked = readBookControl.toLh
        binding.sbHideNavigationBar.isChecked = readBookControl.hideNavigationBar
        binding.sbClick.isChecked = readBookControl.canClickTurn
        binding.sbClickAllNext.isChecked = readBookControl.clickAllNext
        binding.sbShowTitle.isChecked = readBookControl.showTitle
        binding.sbShowTimeBattery.isChecked = readBookControl.showTimeBattery
        binding.sbShowLine.isChecked = readBookControl.showLine
        binding.sbSelectText.isChecked = readBookControl.isCanSelectText
        upView()
    }

    private fun upView() {
        if (readBookControl.hideStatusBar) {
            binding.sbShowTimeBattery.isEnabled = true
            binding.sbToLh.isEnabled = true
        } else {
            binding.sbShowTimeBattery.isEnabled = false
            binding.sbToLh.isEnabled = false
        }
        binding.swReadAloudKey.isEnabled = readBookControl.canKeyTurn
        binding.sbClickAllNext.isEnabled = readBookControl.canClickTurn
        if (readBookControl.hideNavigationBar) {
            binding.llNavigationBarColor.isEnabled = false
            binding.reNavBarColorVal.isEnabled = false
        } else {
            binding.llNavigationBarColor.isEnabled = true
            binding.reNavBarColorVal.isEnabled = true
        }
    }

    private fun upScreenTimeOut(screenTimeOut: Int) {
        binding.tvScreenTimeOut.text = context.resources.getStringArray(R.array.screen_time_out)[screenTimeOut]
    }

    private fun upFConvert(fConvert: Int) {
        binding.tvJFConvert.text = context.resources.getStringArray(R.array.convert_s)[fConvert]
    }

    private fun upScreenDirection(screenDirection: Int) {
        val screenDirectionListTitle = context.resources.getStringArray(R.array.screen_direction_list_title)
        if (screenDirection >= screenDirectionListTitle.size) {
            binding.tvScreenDirection.text = screenDirectionListTitle[0]
        } else {
            binding.tvScreenDirection.text = screenDirectionListTitle[screenDirection]
        }
    }

    private fun upNavBarColor(nColor: Int) {
        binding.reNavBarColorVal.text = context.resources.getStringArray(R.array.NavBarColors)[nColor]
    }

    interface Callback {
        fun upBar()
        fun keepScreenOnChange(keepScreenOn: Int)
        fun recreate()
        fun refreshPage()
    }
}