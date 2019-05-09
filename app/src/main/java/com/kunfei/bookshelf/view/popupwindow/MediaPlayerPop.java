package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.theme.MaterialValueHelper;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.widget.views.ATESeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaPlayerPop extends FrameLayout {

    @BindView(R.id.vw_bg)
    View vwBg;
    @BindView(R.id.tv_dur_time)
    TextView tvDurTime;
    @BindView(R.id.player_progress)
    ATESeekBar playerProgress;
    @BindView(R.id.tv_all_time)
    TextView tvAllTime;
    @BindView(R.id.iv_skip_previous)
    ImageView ivSkipPrevious;
    @BindView(R.id.fab_play_stop)
    FloatingActionButton fabPlayStop;
    @BindView(R.id.iv_skip_next)
    ImageView ivSkipNext;

    private int primaryTextColor;

    public MediaPlayerPop(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MediaPlayerPop(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaPlayerPop(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaPlayerPop(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.pop_media_player, this);
        ButterKnife.bind(this, view);
        view.setBackgroundColor(ThemeStore.primaryColor(context));
        vwBg.setOnClickListener(null);
        primaryTextColor = MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(ThemeStore.primaryColor(context)));
        setColor(ivSkipPrevious.getDrawable());
        setColor(ivSkipNext.getDrawable());
    }

    private void setColor(Drawable drawable) {
        drawable.mutate();
        drawable.setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_ATOP);
    }

    public void upAudioSize(int audioSize) {
        playerProgress.setMax(audioSize);
    }

    public void upAudioDur(int audioDur) {
        playerProgress.setProgress(audioDur);
    }
}
