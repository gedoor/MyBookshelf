package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.TimeUtils;
import com.kunfei.bookshelf.utils.theme.MaterialValueHelper;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.widget.views.ATESeekBar;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaPlayerPop extends FrameLayout {
    @SuppressLint("ConstantLocale")
    private final DateFormat timeFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    @BindView(R.id.vw_bg)
    View vwBg;
    @BindView(R.id.iv_cover)
    ImageView ivCover;
    @BindView(R.id.tv_dur_time)
    TextView tvDurTime;
    @BindView(R.id.player_progress)
    ATESeekBar seekBar;
    @BindView(R.id.tv_all_time)
    TextView tvAllTime;
    @BindView(R.id.iv_skip_previous)
    ImageView ivSkipPrevious;
    @BindView(R.id.fab_play_stop)
    FloatingActionButton fabPlayStop;
    @BindView(R.id.iv_skip_next)
    ImageView ivSkipNext;

    private int primaryTextColor;
    private Callback callback;

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
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (callback != null) {
                    callback.onStopTrackingTouch(seekBar.getProgress());
                }
            }
        });
    }

    private void setColor(Drawable drawable) {
        drawable.mutate();
        drawable.setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_ATOP);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setSeekBarEnable(boolean enable) {
        seekBar.setEnabled(enable);
    }

    public void upAudioSize(int audioSize) {
        seekBar.setMax(audioSize);
        tvAllTime.setText(TimeUtils.millis2String(audioSize, timeFormat));
    }

    public void upAudioDur(int audioDur) {
        seekBar.setProgress(audioDur);
        tvDurTime.setText(TimeUtils.millis2String(audioDur, timeFormat));
    }

    public void setIvCoverClickListener(View.OnClickListener onClickListener) {
        ivCover.setOnClickListener(onClickListener);
    }

    public void setPlayClickListener(View.OnClickListener onClickListener) {
        fabPlayStop.setOnClickListener(onClickListener);
    }

    public void setPrevClickListener(View.OnClickListener onClickListener) {
        ivSkipPrevious.setOnClickListener(onClickListener);
    }

    public void setNextClickListener(View.OnClickListener onClickListener) {
        ivSkipNext.setOnClickListener(onClickListener);
    }

    public void setFabReadAloudImage(int id) {
        fabPlayStop.setImageResource(id);
    }

    public void setCover(String coverPath) {
        if (TextUtils.isEmpty(coverPath)) return;
        if (coverPath.startsWith("http")) {
            Glide.with(this).load(coverPath)
                    .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                            .placeholder(R.drawable.img_cover_default))
                    .into(ivCover);
        } else {
            File file = new File(coverPath);
            Glide.with(this).load(file)
                    .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                            .placeholder(R.drawable.img_cover_default))
                    .into(ivCover);
        }
    }

    public interface Callback {

        void onStopTrackingTouch(int dur);
    }

}
