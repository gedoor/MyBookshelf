package com.kunfei.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.databinding.PopMediaPlayerBinding;
import com.kunfei.bookshelf.help.BlurTransformation;
import com.kunfei.bookshelf.help.ImageLoader;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.TimeUtils;
import com.kunfei.bookshelf.utils.theme.MaterialValueHelper;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MediaPlayerPop extends FrameLayout {
    @SuppressLint("ConstantLocale")
    private final DateFormat timeFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private PopMediaPlayerBinding binding = PopMediaPlayerBinding.inflate(LayoutInflater.from(getContext()), this, true);
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
        binding.getRoot().setBackgroundColor(ThemeStore.primaryColor(context));
        binding.vwBg.setOnClickListener(null);
        primaryTextColor = MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(ThemeStore.primaryColor(context)));
        setColor(binding.ivSkipPrevious.getDrawable());
        setColor(binding.ivSkipNext.getDrawable());
        setColor(binding.ivChapter.getDrawable());
        setColor(binding.ivTimer.getDrawable());
        binding.seekBar.setEnabled(false);
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        binding.seekBar.setEnabled(enable);
    }

    public void upAudioSize(int audioSize) {
        binding.seekBar.setMax(audioSize);
        binding.tvAllTime.setText(TimeUtils.millis2String(audioSize, timeFormat));
    }

    public void upAudioDur(int audioDur) {
        binding.seekBar.setProgress(audioDur);
        binding.tvDurTime.setText(TimeUtils.millis2String(audioDur, timeFormat));
    }

    public void setIvCoverBgClickListener(OnClickListener onClickListener) {
        binding.ivCoverBg.setOnClickListener(onClickListener);
    }

    public void setPlayClickListener(OnClickListener onClickListener) {
        binding.fabPlayStop.setOnClickListener(onClickListener);
    }

    public void setPrevClickListener(OnClickListener onClickListener) {
        binding.ivSkipPrevious.setOnClickListener(onClickListener);
    }

    public void setNextClickListener(OnClickListener onClickListener) {
        binding.ivSkipNext.setOnClickListener(onClickListener);
    }

    public void setIvTimerClickListener(OnClickListener onClickListener) {
        binding.ivTimer.setOnClickListener(onClickListener);
    }

    public void setIvChapterClickListener(OnClickListener onClickListener) {
        binding.ivChapter.setOnClickListener(onClickListener);
    }

    public void setFabReadAloudImage(int id) {
        binding.fabPlayStop.setImageResource(id);
    }

    public void setCover(String coverPath) {
        ImageLoader.INSTANCE.load(getContext(), coverPath)
                .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                        .placeholder(R.drawable.image_cover_default))
                .into(binding.ivCover);
        ImageLoader.INSTANCE.load(getContext(), coverPath)
                .transition(DrawableTransitionOptions.withCrossFade(1500))
                .thumbnail(defaultCover())
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(getContext(), 25)))
                .into(binding.ivCoverBg);
    }

    private RequestBuilder<Drawable> defaultCover() {
        return ImageLoader.INSTANCE.load(getContext(), R.drawable.image_cover_default)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(getContext(), 25)));
    }

    public interface Callback {

        void onStopTrackingTouch(int dur);
    }

}
