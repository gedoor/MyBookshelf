//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import com.kunfei.bookshelf.databinding.PopReadAdjustMarginBinding
import com.kunfei.bookshelf.help.ReadBookControl
import org.jetbrains.anko.sdk27.listeners.onClick

class ReadAdjustMarginPop : FrameLayout {

    val binding = PopReadAdjustMarginBinding.inflate(LayoutInflater.from(context), this, true)
    private var activity: Activity? = null
    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        binding.vwBg.setOnClickListener(null)
    }

    fun setListener(activity: Activity, callback: Callback) {
        this.activity = activity
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
            setSeekBarView(binding.hpbMrF, binding.tvHpbMrF, -0.5f, 0.5f, readBookControl.textLetterSpacing, 100)
            // 行距
            setSeekBarView(binding.hpbMrRm, binding.tvHpbMrRm, 0.5f, 3.0f, readBookControl.lineMultiplier, 10)
            // 段距
            setSeekBarView(binding.hpbMrDm, binding.tvHpbMrDm, 1.0f, 5.0f, readBookControl.paragraphSize, 10)
        }
        if (flag == 0 || flag == 1) {
            // 正文边距
            setSeekBarView(binding.hpbMrZT, binding.tvHpbMrZT, 0, 100, readBookControl.paddingTop)
            setSeekBarView(binding.hpbMrZL, binding.tvHpbMrZL, 0, 100, readBookControl.paddingLeft)
            setSeekBarView(binding.hpbMrZR, binding.tvHpbMrZR, 0, 100, readBookControl.paddingRight)
            setSeekBarView(binding.hpbMrZB, binding.tvHpbMrZB, 0, 100, readBookControl.paddingBottom)
        }
        if (flag == 0 || flag == 2) {
            // Tip边距
            setSeekBarView(binding.hpbMrTT, binding.tvHpbMrTT, 0, 100, readBookControl.tipPaddingTop)
            setSeekBarView(binding.hpbMrTL, binding.tvHpbMrTL, 0, 100, readBookControl.tipPaddingLeft)
            setSeekBarView(binding.hpbMrTR, binding.tvHpbMrTR, 0, 100, readBookControl.tipPaddingRight)
            setSeekBarView(binding.hpbMrTB, binding.tvHpbMrTB, 0, 100, readBookControl.tipPaddingBottom)
        }
    }

    private fun bindEvent() = with(binding) {
        //字距调节
        hpbMrF.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                readBookControl.textLetterSpacing = i / 100.0f - 0.5f
                tvHpbMrF.text = String.format("%.2f", readBookControl.textLetterSpacing)
                callback?.upTextSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        ivMrFAdd.onClick { hpbMrF.progress = hpbMrF.progress + 1 }
        ivMrFRemove.onClick { hpbMrF.progress = hpbMrF.progress - 1 }

        //行距调节
        hpbMrRm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                readBookControl.lineMultiplier = i / 10.0f + 0.5f
                tvHpbMrRm.text = String.format("%.1f", readBookControl.lineMultiplier)
                callback?.upTextSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        ivMrRmAdd.onClick { hpbMrRm.progress = hpbMrRm.progress + 1 }
        ivMrRmRemove.onClick { hpbMrRm.progress = hpbMrRm.progress - 1 }

        //段距调节
        hpbMrDm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                readBookControl.paragraphSize = i / 10.0f + 1.0f
                tvHpbMrDm.text = String.format("%.1f", readBookControl.paragraphSize)
                callback?.upTextSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        ivMrDmAdd.onClick { hpbMrDm.progress = hpbMrDm.progress + 1 }
        ivMrDmRemove.onClick { hpbMrDm.progress = hpbMrDm.progress - 1 }

        //段距调节
        val pdChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var flag = 1
                when {
                    seekBar === hpbMrZT -> readBookControl.paddingTop = i
                    seekBar === hpbMrZL -> readBookControl.paddingLeft = i
                    seekBar === hpbMrZR -> readBookControl.paddingRight = i
                    seekBar === hpbMrZB -> readBookControl.paddingBottom = i
                    else -> {
                        flag = 2
                        when {
                            seekBar === hpbMrTT -> readBookControl.tipPaddingTop = i
                            seekBar === hpbMrTL -> readBookControl.tipPaddingLeft = i
                            seekBar === hpbMrTR -> readBookControl.tipPaddingRight = i
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
        hpbMrZT.setOnSeekBarChangeListener(pdChange)
        hpbMrZL.setOnSeekBarChangeListener(pdChange)
        hpbMrZR.setOnSeekBarChangeListener(pdChange)
        hpbMrZB.setOnSeekBarChangeListener(pdChange)
        hpbMrTT.setOnSeekBarChangeListener(pdChange)
        hpbMrTL.setOnSeekBarChangeListener(pdChange)
        hpbMrTR.setOnSeekBarChangeListener(pdChange)
        hpbMrTB.setOnSeekBarChangeListener(pdChange)
        ivMrZTAdd.onClick { hpbMrZT.progress = hpbMrZT.progress + 1 }
        ivMrZTRemove.onClick { hpbMrZT.progress = hpbMrZT.progress - 1 }
        ivMrZLAdd.onClick { hpbMrZL.progress = hpbMrZL.progress + 1 }
        ivMrZLRemove.onClick { hpbMrZL.progress = hpbMrZL.progress - 1 }
        ivMrZRAdd.onClick { hpbMrZR.progress = hpbMrZR.progress + 1 }
        ivMrZRRemove.onClick { hpbMrZR.progress = hpbMrZR.progress - 1 }
        ivMrZBAdd.onClick { hpbMrZB.progress = hpbMrZB.progress + 1 }
        ivMrZBRemove.onClick { hpbMrZB.progress = hpbMrZB.progress - 1 }
        ivMrTTAdd.onClick { hpbMrTT.progress = hpbMrTT.progress + 1 }
        ivMrTTRemove.onClick { hpbMrTT.progress = hpbMrTT.progress - 1 }
        ivMrTLAdd.onClick { hpbMrTL.progress = hpbMrTL.progress + 1 }
        ivMrTLRemove.onClick { hpbMrTL.progress = hpbMrTL.progress - 1 }
        ivMrTRAdd.onClick { hpbMrTR.progress = hpbMrTR.progress + 1 }
        ivMrTRRemove.onClick { hpbMrTR.progress = hpbMrTR.progress - 1 }
        ivMrTBAdd.onClick { hpbMrTB.progress = hpbMrTB.progress + 1 }
        ivMrTBRemove.onClick { hpbMrTB.progress = hpbMrTB.progress - 1 }
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
