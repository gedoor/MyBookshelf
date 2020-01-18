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
import com.kunfei.bookshelf.help.ReadBookControl
import com.kunfei.bookshelf.utils.theme.ATH
import com.kunfei.bookshelf.widget.modialog.PageKeyDialog
import kotlinx.android.synthetic.main.pop_more_setting.view.*
import org.jetbrains.anko.sdk27.listeners.onClick

class MoreSettingPop : FrameLayout {

    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null

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
        LayoutInflater.from(context).inflate(R.layout.pop_more_setting, this)
        vw_bg.setOnClickListener(null)
    }

    fun setListener(callback: Callback) {
        this.callback = callback
        initData()
        bindEvent()
    }

    private fun bindEvent() {
        setOnClickListener { this.visibility = View.GONE }
        sbImmersionStatusBar.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.immersionStatusBar = b
                callback?.upBar()
                RxBus.get().post(RxBusTag.RECREATE, true)
            }
        }
        sb_hideStatusBar.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.hideStatusBar = isChecked
                callback?.recreate()
            }
        }
        sb_to_lh.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.toLh = isChecked
                callback?.recreate()
            }
        }
        sb_hideNavigationBar.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.hideNavigationBar = isChecked
                initData()
                callback?.recreate()
            }
        }
        sw_volume_next_page.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.canKeyTurn = b
                upView()
            }
        }
        sw_read_aloud_key.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.aloudCanKeyTurn = b
            }
        }
        sb_click.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.canClickTurn = isChecked
                upView()
            }
        }
        sb_click_all_next.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.clickAllNext = isChecked
            }
        }
        sb_show_title.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.showTitle = isChecked
                callback?.refreshPage()
            }
        }
        sb_showTimeBattery.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.showTimeBattery = isChecked
                callback?.refreshPage()
            }
        }
        sb_showLine.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.showLine = isChecked
                callback?.refreshPage()
            }
        }
        llScreenTimeOut.setOnClickListener {
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
        llJFConvert.setOnClickListener {
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
        ll_screen_direction.setOnClickListener {
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
        llNavigationBarColor!!.setOnClickListener {
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
        sb_select_text.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.isCanSelectText = isChecked
            }
        }
        ll_click_key_code.onClick {
            PageKeyDialog(context).show()
        }
    }

    private fun initData() {
        upScreenDirection(readBookControl.screenDirection)
        upScreenTimeOut(readBookControl.screenTimeOut)
        upFConvert(readBookControl.textConvert)
        upNavBarColor(readBookControl.navBarColor)
        sbImmersionStatusBar!!.isChecked = readBookControl.immersionStatusBar
        sw_volume_next_page.isChecked = readBookControl.canKeyTurn
        sw_read_aloud_key.isChecked = readBookControl.aloudCanKeyTurn
        sb_hideStatusBar.isChecked = readBookControl.hideStatusBar
        sb_to_lh.isChecked = readBookControl.toLh
        sb_hideNavigationBar.isChecked = readBookControl.hideNavigationBar
        sb_click.isChecked = readBookControl.canClickTurn
        sb_click_all_next.isChecked = readBookControl.clickAllNext
        sb_show_title.isChecked = readBookControl.showTitle
        sb_showTimeBattery.isChecked = readBookControl.showTimeBattery
        sb_showLine.isChecked = readBookControl.showLine
        sb_select_text.isChecked = readBookControl.isCanSelectText
        upView()
    }

    private fun upView() {
        if (readBookControl.hideStatusBar) {
            sb_showTimeBattery.isEnabled = true
            sb_to_lh.isEnabled = true
        } else {
            sb_showTimeBattery.isEnabled = false
            sb_to_lh.isEnabled = false
        }
        sw_read_aloud_key.isEnabled = readBookControl.canKeyTurn
        sb_click_all_next.isEnabled = readBookControl.canClickTurn
        if (readBookControl.hideNavigationBar) {
            llNavigationBarColor.isEnabled = false
            reNavBarColor_val.isEnabled = false
        } else {
            llNavigationBarColor!!.isEnabled = true
            reNavBarColor_val.isEnabled = true
        }
    }

    private fun upScreenTimeOut(screenTimeOut: Int) {
        tv_screen_time_out.text = context.resources.getStringArray(R.array.screen_time_out)[screenTimeOut]
    }

    private fun upFConvert(fConvert: Int) {
        tvJFConvert.text = context.resources.getStringArray(R.array.convert_s)[fConvert]
    }

    private fun upScreenDirection(screenDirection: Int) {
        val screenDirectionListTitle = context.resources.getStringArray(R.array.screen_direction_list_title)
        if (screenDirection >= screenDirectionListTitle.size) {
            tv_screen_direction.text = screenDirectionListTitle[0]
        } else {
            tv_screen_direction.text = screenDirectionListTitle[screenDirection]
        }
    }

    private fun upNavBarColor(nColor: Int) {
        reNavBarColor_val.text = context.resources.getStringArray(R.array.NavBarColors)[nColor]
    }

    interface Callback {
        fun upBar()
        fun keepScreenOnChange(keepScreenOn: Int)
        fun recreate()
        fun refreshPage()
    }
}