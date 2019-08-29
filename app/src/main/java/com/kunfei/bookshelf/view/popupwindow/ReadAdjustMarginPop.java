//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.widget.check_box.SmoothCheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadAdjustMarginPop extends FrameLayout {
    @BindView(R.id.vw_bg)
    View vwBg;

    @BindView(R.id.hpb_mr_rm)
    SeekBar hpb_mr_rm;
    @BindView(R.id.tv_hpb_mr_rm)
    TextView tv_hpb_mr_rm;
    @BindView(R.id.hpb_mr_dm)
    SeekBar hpb_mr_dm;
    @BindView(R.id.tv_hpb_mr_dm)
    TextView tv_hpb_mr_dm;
    @BindView(R.id.hpb_mr_f)
    SeekBar hpb_mr_f;
    @BindView(R.id.tv_hpb_mr_f)
    TextView tv_hpb_mr_f;

    @BindView(R.id.hpb_mr_z_t)
    SeekBar hpb_mr_z_t;
    @BindView(R.id.tv_hpb_mr_z_t)
    TextView tv_hpb_mr_z_t;
    @BindView(R.id.hpb_mr_z_l)
    SeekBar hpb_mr_z_l;
    @BindView(R.id.tv_hpb_mr_z_l)
    TextView tv_hpb_mr_z_l;
    @BindView(R.id.hpb_mr_z_r)
    SeekBar hpb_mr_z_r;
    @BindView(R.id.tv_hpb_mr_z_r)
    TextView tv_hpb_mr_z_r;
    @BindView(R.id.hpb_mr_z_b)
    SeekBar hpb_mr_z_b;
    @BindView(R.id.tv_hpb_mr_z_b)
    TextView tv_hpb_mr_z_b;

    @BindView(R.id.hpb_mr_t_t)
    SeekBar hpb_mr_t_t;
    @BindView(R.id.tv_hpb_mr_t_t)
    TextView tv_hpb_mr_t_t;
    @BindView(R.id.hpb_mr_t_l)
    SeekBar hpb_mr_t_l;
    @BindView(R.id.tv_hpb_mr_t_l)
    TextView tv_hpb_mr_t_l;
    @BindView(R.id.hpb_mr_t_r)
    SeekBar hpb_mr_t_r;
    @BindView(R.id.tv_hpb_mr_t_r)
    TextView tv_hpb_mr_t_r;
    @BindView(R.id.hpb_mr_t_b)
    SeekBar hpb_mr_t_b;
    @BindView(R.id.tv_hpb_mr_t_b)
    TextView tv_hpb_mr_t_b;

    private Activity context;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private Callback callback;

    public ReadAdjustMarginPop(Context context) {
        super(context);
        init(context);
    }

    public ReadAdjustMarginPop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadAdjustMarginPop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.pop_read_adjust_margin, this);
        ButterKnife.bind(this, view);
        vwBg.setOnClickListener(null);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initData(0);
        bindEvent();
    }

    public void show() {
        initData(0);
    }

    private void initData(int flag) {
        if (flag == 0) {
            // 字距
            setSeekBarView(hpb_mr_f, tv_hpb_mr_f, -0.5f, 0.5f, readBookControl.getTextLetterSpacing(), 100);
            // 行距
            setSeekBarView(hpb_mr_rm, tv_hpb_mr_rm, 0.5f, 3.0f, readBookControl.getLineMultiplier(), 10);
            // 段距
            setSeekBarView(hpb_mr_dm, tv_hpb_mr_dm, 1.0f, 5.0f, readBookControl.getParagraphSize(), 10);
        }
        if (flag == 0 || flag == 1) {
            // 正文边距
            setSeekBarView(hpb_mr_z_t, tv_hpb_mr_z_t, 0, 100, readBookControl.getPaddingTop());
            setSeekBarView(hpb_mr_z_l, tv_hpb_mr_z_l, 0, 100, readBookControl.getPaddingLeft());
            setSeekBarView(hpb_mr_z_r, tv_hpb_mr_z_r, 0, 100, readBookControl.getPaddingRight());
            setSeekBarView(hpb_mr_z_b, tv_hpb_mr_z_b, 0, 100, readBookControl.getPaddingBottom());
        }
        if (flag == 0 || flag == 2) {
            // Tip边距
            setSeekBarView(hpb_mr_t_t, tv_hpb_mr_t_t, 0, 100, readBookControl.getTipPaddingTop());
            setSeekBarView(hpb_mr_t_l, tv_hpb_mr_t_l, 0, 100, readBookControl.getTipPaddingLeft());
            setSeekBarView(hpb_mr_t_r, tv_hpb_mr_t_r, 0, 100, readBookControl.getTipPaddingRight());
            setSeekBarView(hpb_mr_t_b, tv_hpb_mr_t_b, 0, 100, readBookControl.getTipPaddingBottom());
        }
    }

    private void bindEvent() {
        //字距调节
        hpb_mr_f.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                readBookControl.setTextLetterSpacing(i / 100.0f - 0.5f);
                tv_hpb_mr_f.setText(String.format("%.2f", readBookControl.getTextLetterSpacing()));
                callback.upTextSize();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //行距调节
        hpb_mr_rm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                readBookControl.setLineMultiplier(i / 10.0f + 0.5f);
                tv_hpb_mr_rm.setText(String.format("%.1f", readBookControl.getLineMultiplier()));
                callback.upTextSize();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //段距调节
        hpb_mr_dm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                readBookControl.setParagraphSize(i / 10.0f + 1.0f);
                tv_hpb_mr_dm.setText(String.format("%.1f", readBookControl.getParagraphSize()));
                callback.upTextSize();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //段距调节
        SeekBar.OnSeekBarChangeListener pdChange = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int flag = 1;
                if (seekBar == hpb_mr_z_t)
                    readBookControl.setPaddingTop(i);
                else if (seekBar == hpb_mr_z_l)
                    readBookControl.setPaddingLeft(i);
                else if (seekBar == hpb_mr_z_r)
                    readBookControl.setPaddingRight(i);
                else if (seekBar == hpb_mr_z_b)
                    readBookControl.setPaddingBottom(i);
                else {
                    flag = 2;
                    if (seekBar == hpb_mr_t_t)
                        readBookControl.setTipPaddingTop(i);
                    else if (seekBar == hpb_mr_t_l)
                        readBookControl.setTipPaddingLeft(i);
                    else if (seekBar == hpb_mr_t_r)
                        readBookControl.setTipPaddingRight(i);
                    else
                        readBookControl.setTipPaddingBottom(i);
                }
                initData(flag);
                callback.upMargin();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        hpb_mr_z_t.setOnSeekBarChangeListener(pdChange);
        hpb_mr_z_l.setOnSeekBarChangeListener(pdChange);
        hpb_mr_z_r.setOnSeekBarChangeListener(pdChange);
        hpb_mr_z_b.setOnSeekBarChangeListener(pdChange);
        hpb_mr_t_t.setOnSeekBarChangeListener(pdChange);
        hpb_mr_t_l.setOnSeekBarChangeListener(pdChange);
        hpb_mr_t_r.setOnSeekBarChangeListener(pdChange);
        hpb_mr_t_b.setOnSeekBarChangeListener(pdChange);
    }

    private void setSeekBarView(SeekBar hpb, TextView tv, float min, float max, float value, int p) {
        int a = (int) (min * p);
        int b = (int) (max * p) - a;
        hpb.setMax(b);
        hpb.setProgress((int) (value * p) - a);
        if (p >= 100)
            tv.setText(String.format("%.2f", value));
        else if (p >= 10)
            tv.setText(String.format("%.1f", value));
        else
            tv.setText(String.format("%.0f", value));
    }

    private void setSeekBarView(SeekBar hpb, TextView tv, int min, int max, int value) {
        hpb.setMax((int) (max) - min);
        hpb.setProgress((int) (value) - min);
        tv.setText(String.format("%d", value));
    }

    public interface Callback {
        void upTextSize();
        void upMargin();
        void refresh();
    }
}
