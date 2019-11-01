//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.help.ReadBookControl
import kotlinx.android.synthetic.main.pop_read_adjust_margin.view.*
import org.jetbrains.anko.sdk27.listeners.onClick

class ReadAdjustMarginPop : FrameLayout {

    private var context: Activity? = null
    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        inflate(context, R.layout.pop_read_adjust_margin, this)
        vw_bg.setOnClickListener(null)
    }

    fun setListener(activity: Activity, callback: Callback) {
        this.context = activity
        this.callback = callback
        initData(0)
        bindEvent()
    }

    fun show() {
        initData(0)
    }

    private fun initData(flag: Int) {
        if (flag == 0) {
            // 字距
            setSeekBarView(hpb_mr_f, tv_hpb_mr_f, -0.5f, 0.5f, readBookControl.textLetterSpacing, 100)
            // 行距
            setSeekBarView(hpb_mr_rm, tv_hpb_mr_rm, 0.5f, 3.0f, readBookControl.lineMultiplier, 10)
            // 段距
            setSeekBarView(hpb_mr_dm, tv_hpb_mr_dm, 1.0f, 5.0f, readBookControl.paragraphSize, 10)
        }
        if (flag == 0 || flag == 1) {
            // 正文边距
            setSeekBarView(hpb_mr_z_t, tv_hpb_mr_z_t, 0, 100, readBookControl.paddingTop)
            setSeekBarView(hpb_mr_z_l, tv_hpb_mr_z_l, 0, 100, readBookControl.paddingLeft)
            setSeekBarView(hpb_mr_z_r, tv_hpb_mr_z_r, 0, 100, readBookControl.paddingRight)
            setSeekBarView(hpb_mr_z_b, tv_hpb_mr_z_b, 0, 100, readBookControl.paddingBottom)
        }
        if (flag == 0 || flag == 2) {
            // Tip边距
            setSeekBarView(hpb_mr_t_t, tv_hpb_mr_t_t, 0, 100, readBookControl.tipPaddingTop)
            setSeekBarView(hpb_mr_t_l, tv_hpb_mr_t_l, 0, 100, readBookControl.tipPaddingLeft)
            setSeekBarView(hpb_mr_t_r, tv_hpb_mr_t_r, 0, 100, readBookControl.tipPaddingRight)
            setSeekBarView(hpb_mr_t_b, tv_hpb_mr_t_b, 0, 100, readBookControl.tipPaddingBottom)
        }
    }

    private fun bindEvent() {
        //字距调节
        hpb_mr_f.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                readBookControl.textLetterSpacing = i / 100.0f - 0.5f
                tv_hpb_mr_f.text = String.format("%.2f", readBookControl.textLetterSpacing)
                callback?.upTextSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        iv_mr_f_add.onClick { hpb_mr_f.progress = hpb_mr_f.progress + 1 }
        iv_mr_f_remove.onClick { hpb_mr_f.progress = hpb_mr_f.progress - 1 }

        //行距调节
        hpb_mr_rm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                readBookControl.lineMultiplier = i / 10.0f + 0.5f
                tv_hpb_mr_rm.text = String.format("%.1f", readBookControl.lineMultiplier)
                callback?.upTextSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        iv_mr_rm_add.onClick { hpb_mr_rm.progress = hpb_mr_rm.progress + 1 }
        iv_mr_rm_remove.onClick { hpb_mr_rm.progress = hpb_mr_rm.progress - 1 }

        //段距调节
        hpb_mr_dm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                readBookControl.paragraphSize = i / 10.0f + 1.0f
                tv_hpb_mr_dm.text = String.format("%.1f", readBookControl.paragraphSize)
                callback?.upTextSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        iv_mr_dm_add.onClick { hpb_mr_dm.progress = hpb_mr_dm.progress + 1 }
        iv_mr_dm_remove.onClick { hpb_mr_dm.progress = hpb_mr_dm.progress - 1 }

        //段距调节
        val pdChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var flag = 1
                when {
                    seekBar === hpb_mr_z_t -> readBookControl.paddingTop = i
                    seekBar === hpb_mr_z_l -> readBookControl.paddingLeft = i
                    seekBar === hpb_mr_z_r -> readBookControl.paddingRight = i
                    seekBar === hpb_mr_z_b -> readBookControl.paddingBottom = i
                    else -> {
                        flag = 2
                        when {
                            seekBar === hpb_mr_t_t -> readBookControl.tipPaddingTop = i
                            seekBar === hpb_mr_t_l -> readBookControl.tipPaddingLeft = i
                            seekBar === hpb_mr_t_r -> readBookControl.tipPaddingRight = i
                            else -> readBookControl.tipPaddingBottom = i
                        }
                    }
                }
                initData(flag)
                callback?.upMargin()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }
        hpb_mr_z_t.setOnSeekBarChangeListener(pdChange)
        hpb_mr_z_l.setOnSeekBarChangeListener(pdChange)
        hpb_mr_z_r.setOnSeekBarChangeListener(pdChange)
        hpb_mr_z_b.setOnSeekBarChangeListener(pdChange)
        hpb_mr_t_t.setOnSeekBarChangeListener(pdChange)
        hpb_mr_t_l.setOnSeekBarChangeListener(pdChange)
        hpb_mr_t_r.setOnSeekBarChangeListener(pdChange)
        hpb_mr_t_b.setOnSeekBarChangeListener(pdChange)
        iv_mr_z_t_add.onClick { hpb_mr_z_t.progress = hpb_mr_z_t.progress + 1 }
        iv_mr_z_t_remove.onClick { hpb_mr_z_t.progress = hpb_mr_z_t.progress - 1 }
        iv_mr_z_l_add.onClick { hpb_mr_z_l.progress = hpb_mr_z_l.progress + 1 }
        iv_mr_z_l_remove.onClick { hpb_mr_z_l.progress = hpb_mr_z_l.progress - 1 }
        iv_mr_z_r_add.onClick { hpb_mr_z_r.progress = hpb_mr_z_r.progress + 1 }
        iv_mr_z_r_remove.onClick { hpb_mr_z_r.progress = hpb_mr_z_r.progress - 1 }
        iv_mr_z_b_add.onClick { hpb_mr_z_b.progress = hpb_mr_z_b.progress + 1 }
        iv_mr_z_b_remove.onClick { hpb_mr_z_b.progress = hpb_mr_z_b.progress - 1 }
        iv_mr_t_t_add.onClick { hpb_mr_t_t.progress = hpb_mr_t_t.progress + 1 }
        iv_mr_t_t_remove.onClick { hpb_mr_t_t.progress = hpb_mr_t_t.progress - 1 }
        iv_mr_t_l_add.onClick { hpb_mr_t_l.progress = hpb_mr_t_l.progress + 1 }
        iv_mr_t_l_remove.onClick { hpb_mr_t_l.progress = hpb_mr_t_l.progress - 1 }
        iv_mr_t_r_add.onClick { hpb_mr_t_r.progress = hpb_mr_t_r.progress + 1 }
        iv_mr_t_r_remove.onClick { hpb_mr_t_r.progress = hpb_mr_t_r.progress - 1 }
        iv_mr_t_b_add.onClick { hpb_mr_t_b.progress = hpb_mr_t_b.progress + 1 }
        iv_mr_t_b_remove.onClick { hpb_mr_t_b.progress = hpb_mr_t_b.progress - 1 }
    }

    private fun setSeekBarView(hpb: SeekBar, tv: TextView?, min: Float, max: Float, value: Float, p: Int) {
        val a = (min * p).toInt()
        val b = (max * p).toInt() - a
        hpb.max = b
        hpb.progress = (value * p).toInt() - a
        when {
            p >= 100 -> tv?.text = String.format("%.2f", value)
            p >= 10 -> tv?.text = String.format("%.1f", value)
            else -> tv?.text = String.format("%.0f", value)
        }
    }

    private fun setSeekBarView(hpb: SeekBar, tv: TextView, min: Int, max: Int, value: Int) {
        hpb.max = max - min
        hpb.progress = value - min
        tv.text = String.format("%d", value)
    }

    interface Callback {
        fun upTextSize()
        fun upMargin()
        fun refresh()
    }
}
