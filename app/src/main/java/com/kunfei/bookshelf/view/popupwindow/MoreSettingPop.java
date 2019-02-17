//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.widget.views.ATESwitch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MoreSettingPop extends FrameLayout {

    @BindView(R.id.sb_click_all_next)
    Switch sbClickAllNext;
    @BindView(R.id.sb_click)
    Switch sbClick;
    @BindView(R.id.sb_show_title)
    Switch sbShowTitle;
    @BindView(R.id.sb_showTimeBattery)
    Switch sbShowTimeBattery;
    @BindView(R.id.sb_hideStatusBar)
    Switch sbHideStatusBar;
    @BindView(R.id.ll_hideStatusBar)
    LinearLayout llHideStatusBar;
    @BindView(R.id.ll_showTimeBattery)
    LinearLayout llShowTimeBattery;
    @BindView(R.id.sb_hideNavigationBar)
    Switch sbHideNavigationBar;
    @BindView(R.id.ll_hideNavigationBar)
    LinearLayout llHideNavigationBar;
    @BindView(R.id.sb_showLine)
    Switch sbShowLine;
    @BindView(R.id.llScreenTimeOut)
    LinearLayout llScreenTimeOut;
    @BindView(R.id.tv_screen_time_out)
    TextView tvScreenTimeOut;
    @BindView(R.id.tvJFConvert)
    TextView tvJFConvert;
    @BindView(R.id.llJFConvert)
    LinearLayout llJFConvert;
    @BindView(R.id.tv_screen_direction)
    TextView tvScreenDirection;
    @BindView(R.id.ll_screen_direction)
    LinearLayout llScreenDirection;
    @BindView(R.id.sw_volume_next_page)
    Switch swVolumeNextPage;
    @BindView(R.id.sw_read_aloud_key)
    Switch swReadAloudKey;
    @BindView(R.id.ll_read_aloud_key)
    LinearLayout llReadAloudKey;
    @BindView(R.id.sb_tip_margin_change)
    Switch sbTipMarginChange;
    @BindView(R.id.ll_click_all_next)
    LinearLayout llClickAllNext;
    @BindView(R.id.reNavbarcolor)
    TextView reNavbarcolor;
    @BindView(R.id.reNavbarcolor_val)
    TextView reNavbarcolorVal;
    @BindView(R.id.llNavigationBarColor)
    LinearLayout llNavigationBarColor;
    @BindView(R.id.sbImmersionStatusBar)
    ATESwitch sbImmersionStatusBar;
    @BindView(R.id.llImmersionStatusBar)
    LinearLayout llImmersionStatusBar;

    private Context context;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnChangeProListener changeProListener;

    public MoreSettingPop(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public MoreSettingPop(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public MoreSettingPop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.pop_more_setting, null);
        addView(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);
    }

    public void setListener(@NonNull OnChangeProListener changeProListener) {
        this.changeProListener = changeProListener;
        initData();
        bindEvent();
    }

    private void bindEvent() {
        this.setOnClickListener(view -> this.setVisibility(GONE));
        sbImmersionStatusBar.setOnCheckedChangeListener(((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                readBookControl.setImmersionStatusBar(b);
                changeProListener.upBar();
                RxBus.get().post(RxBusTag.RECREATE, true);
            }
        }));
        sbHideStatusBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setHideStatusBar(isChecked);
                changeProListener.recreate();
            }
        });
        sbHideNavigationBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setHideNavigationBar(isChecked);
                initData();
                changeProListener.recreate();
            }
        });
        swVolumeNextPage.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                readBookControl.setCanKeyTurn(b);
                upView();
            }
        });
        swReadAloudKey.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                readBookControl.setAloudCanKeyTurn(b);
            }
        });
        sbClick.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setCanClickTurn(isChecked);
                upView();
            }
        });
        sbClickAllNext.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setClickAllNext(isChecked);
            }
        });

        sbShowTitle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setShowTitle(isChecked);
                changeProListener.refreshPage();
            }
        });
        sbShowTimeBattery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setShowTimeBattery(isChecked);
                changeProListener.refreshPage();
            }
        });
        sbShowLine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setShowLine(isChecked);
                changeProListener.refreshPage();
            }
        });
        sbTipMarginChange.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                readBookControl.setTipMarginChange(b);
                changeProListener.refreshPage();
            }
        });
        llScreenTimeOut.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.keep_light))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.screen_time_out), readBookControl.getScreenTimeOut(), (dialogInterface, i) -> {
                        readBookControl.setScreenTimeOut(i);
                        upScreenTimeOut(i);
                        changeProListener.keepScreenOnChange(i);
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
        llJFConvert.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.jf_convert))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.convert_s), readBookControl.getTextConvert(), (dialogInterface, i) -> {
                        readBookControl.setTextConvert(i);
                        upFConvert(i);
                        dialogInterface.dismiss();
                        changeProListener.refreshPage();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
        llScreenDirection.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.screen_direction))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.screen_direction_list_title), readBookControl.getScreenDirection(), (dialogInterface, i) -> {
                        readBookControl.setScreenDirection(i);
                        upScreenDirection(i);
                        dialogInterface.dismiss();
                        changeProListener.recreate();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
        llNavigationBarColor.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.re_navigation_bar_color))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.NavbarColors), readBookControl.getNavbarColor(), (dialogInterface, i) -> {
                        readBookControl.setNavbarColor(i);
                        upNavbarColor(i);
                        dialogInterface.dismiss();
                        changeProListener.recreate();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
    }

    private void initData() {
        upScreenDirection(readBookControl.getScreenDirection());
        upScreenTimeOut(readBookControl.getScreenTimeOut());
        upFConvert(readBookControl.getTextConvert());
        upNavbarColor(readBookControl.getNavbarColor());
        sbImmersionStatusBar.setChecked(readBookControl.getImmersionStatusBar());
        swVolumeNextPage.setChecked(readBookControl.getCanKeyTurn());
        swReadAloudKey.setChecked(readBookControl.getAloudCanKeyTurn());
        sbHideStatusBar.setChecked(readBookControl.getHideStatusBar());
        sbHideNavigationBar.setChecked(readBookControl.getHideNavigationBar());
        sbClick.setChecked(readBookControl.getCanClickTurn());
        sbClickAllNext.setChecked(readBookControl.getClickAllNext());
        sbShowTitle.setChecked(readBookControl.getShowTitle());
        sbShowTimeBattery.setChecked(readBookControl.getShowTimeBattery());
        sbShowLine.setChecked(readBookControl.getShowLine());
        sbTipMarginChange.setChecked(readBookControl.getTipMarginChange());
        upView();
    }

    private void upView() {
        if (readBookControl.getHideStatusBar()) {
            sbShowTimeBattery.setEnabled(true);
        } else {
            sbShowTimeBattery.setEnabled(false);
        }
        if (readBookControl.getCanKeyTurn()) {
            swReadAloudKey.setEnabled(true);
        } else {
            swReadAloudKey.setEnabled(false);
        }
        if (readBookControl.getCanClickTurn()) {
            sbClickAllNext.setEnabled(true);
        } else {
            sbClickAllNext.setEnabled(false);
        }
        if (readBookControl.getHideNavigationBar()) {
            llNavigationBarColor.setEnabled(false);
            reNavbarcolorVal.setEnabled(false);
        } else {
            llNavigationBarColor.setEnabled(true);
            reNavbarcolorVal.setEnabled(true);
        }
    }

    private void upScreenTimeOut(int screenTimeOut) {
        tvScreenTimeOut.setText(context.getResources().getStringArray(R.array.screen_time_out)[screenTimeOut]);
    }

    private void upFConvert(int fConvert) {
        tvJFConvert.setText(context.getResources().getStringArray(R.array.convert_s)[fConvert]);
    }

    private void upScreenDirection(int screenDirection) {
        String[] screenDirectionListTitle = context.getResources().getStringArray(R.array.screen_direction_list_title);
        if (screenDirection >= screenDirectionListTitle.length) {
            tvScreenDirection.setText(screenDirectionListTitle[0]);
        } else {
            tvScreenDirection.setText(screenDirectionListTitle[screenDirection]);
        }
    }

    private void upNavbarColor(int nColor) {
        reNavbarcolorVal.setText(context.getResources().getStringArray(R.array.NavbarColors)[nColor]);
    }

    public interface OnChangeProListener {
        void upBar();

        void keepScreenOnChange(int keepScreenOn);

        void recreate();

        void refreshPage();
    }

}
