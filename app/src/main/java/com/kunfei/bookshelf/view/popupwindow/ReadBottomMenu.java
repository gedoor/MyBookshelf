package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.service.ReadAloudService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadBottomMenu extends FrameLayout {


    @BindView(R.id.fab_read_aloud_timer)
    FloatingActionButton fabReadAloudTimer;
    @BindView(R.id.tv_read_aloud_timer)
    TextView tvReadAloudTimer;
    @BindView(R.id.ll_read_aloud_timer)
    LinearLayout llReadAloudTimer;
    @BindView(R.id.fabReadAloud)
    FloatingActionButton fabReadAloud;
    @BindView(R.id.fabAutoPage)
    FloatingActionButton fabAutoPage;
    @BindView(R.id.fabReplaceRule)
    FloatingActionButton fabReplaceRule;
    @BindView(R.id.fabNightTheme)
    FloatingActionButton fabNightTheme;
    @BindView(R.id.tv_pre)
    TextView tvPre;
    @BindView(R.id.hpb_read_progress)
    SeekBar hpbReadProgress;
    @BindView(R.id.tv_next)
    TextView tvNext;
    @BindView(R.id.ll_catalog)
    LinearLayout llCatalog;
    @BindView(R.id.ll_adjust)
    LinearLayout llAdjust;
    @BindView(R.id.ll_font)
    LinearLayout llFont;
    @BindView(R.id.ll_setting)
    LinearLayout llSetting;
    @BindView(R.id.llNavigationBar)
    LinearLayout llNavigationBar;
    @BindView(R.id.ll_floating_button)
    LinearLayout llFloatingButton;

    private OnMenuListener menuListener;

    public ReadBottomMenu(Context context) {
        super(context);
        init(context);
    }

    public ReadBottomMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadBottomMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.pop_read_menu, null);
        addView(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);
    }

    public void setListener(OnMenuListener menuListener) {
        this.menuListener = menuListener;
        bindEvent();
    }

    private void bindEvent() {
        llReadAloudTimer.setOnClickListener(view -> {
            menuListener.dismiss();
        });
        llFloatingButton.setOnClickListener(view -> {
            menuListener.dismiss();
        });

        //阅读进度
        hpbReadProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                menuListener.skipToPage(seekBar.getProgress());
            }
        });

        //朗读定时
        fabReadAloudTimer.setOnClickListener(view -> ReadAloudService.setTimer(getContext(), 10));

        //朗读
        fabReadAloud.setOnClickListener(view -> menuListener.onMediaButton());
        //长按停止朗读
        fabReadAloud.setOnLongClickListener(view -> {
            if (ReadAloudService.running) {
                menuListener.toast(R.string.aloud_stop);
                ReadAloudService.stop(getContext());
            } else {
                menuListener.toast(R.string.read_aloud);
            }
            return true;
        });

        //自动翻页
        fabAutoPage.setOnClickListener(view -> menuListener.autoPage());
        fabAutoPage.setOnLongClickListener(view -> {
            menuListener.toast(R.string.auto_next_page);
            return true;
        });

        //替换
        fabReplaceRule.setOnClickListener(view -> menuListener.openReplaceRule());
        fabReplaceRule.setOnLongClickListener(view -> {
            menuListener.toast(R.string.replace_rule_title);
            return true;
        });

        //夜间模式
        fabNightTheme.setOnClickListener(view -> menuListener.setNightTheme());
        fabNightTheme.setOnLongClickListener(view -> {
            menuListener.toast(R.string.night_theme);
            return true;
        });

        //上一章
        tvPre.setOnClickListener(view -> menuListener.skipPreChapter());

        //下一章
        tvNext.setOnClickListener(view -> menuListener.skipNextChapter());

        //目录
        llCatalog.setOnClickListener(view -> menuListener.openChapterList());

        //调节
        llAdjust.setOnClickListener(view -> menuListener.openAdjust());

        //界面
        llFont.setOnClickListener(view -> menuListener.openReadInterface());

        //设置
        llSetting.setOnClickListener(view -> menuListener.openMoreSetting());

        tvReadAloudTimer.setOnClickListener(null);
    }

    public void setFabReadAloudImage(int id) {
        fabReadAloud.setImageResource(id);
    }

    public void setReadAloudTimer(boolean visibility) {
        if (visibility) {
            llReadAloudTimer.setVisibility(VISIBLE);
        } else {
            llReadAloudTimer.setVisibility(GONE);
        }
    }

    public void setReadAloudTimer(String text) {
        tvReadAloudTimer.setText(text);
    }

    public void setFabReadAloudText(String text) {
        fabReadAloud.setContentDescription(text);
    }

    public SeekBar getReadProgress() {
        return hpbReadProgress;
    }

    public void setTvPre(boolean enable) {
        tvPre.setEnabled(enable);
    }

    public void setTvNext(boolean enable) {
        tvNext.setEnabled(enable);
    }

    public void setNavigationBar(int nbHeight) {
        llNavigationBar.setPadding(0, 0, 0, nbHeight);
    }

    public void setAutoPage(boolean autoPage) {
        if (autoPage) {
            fabAutoPage.setImageResource(R.drawable.ic_auto_page_stop);
            fabAutoPage.setContentDescription(getContext().getString(R.string.auto_next_page_stop));
        } else {
            fabAutoPage.setImageResource(R.drawable.ic_auto_page);
            fabAutoPage.setContentDescription(getContext().getString(R.string.auto_next_page));
        }
    }

    public void setFabNightTheme(boolean isNightTheme) {
        if (isNightTheme) {
            fabNightTheme.setImageResource(R.drawable.ic_daytime_24dp);
        } else {
            fabNightTheme.setImageResource(R.drawable.ic_brightness);
        }
    }

    public interface OnMenuListener {
        void skipToPage(int page);

        void onMediaButton();

        void autoPage();

        void setNightTheme();

        void skipPreChapter();

        void skipNextChapter();

        void openReplaceRule();

        void openChapterList();

        void openAdjust();

        void openReadInterface();

        void openMoreSetting();

        void toast(int id);

        void dismiss();
    }

}
