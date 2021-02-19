package com.kunfei.bookshelf.view.popupwindow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.kunfei.bookshelf.databinding.PopReadLongPressBinding;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.DensityUtil;

public class ReadLongPressPop extends FrameLayout {

    private PopReadLongPressBinding binding = PopReadLongPressBinding.inflate(LayoutInflater.from(getContext()), this, true);
    //private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnBtnClickListener clickListener;

    public ReadLongPressPop(Context context) {
        super(context);
        init(context);
    }

    public ReadLongPressPop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadLongPressPop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Path path = new Path();
        path.addRoundRect(new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight()), DensityUtil.dp2px(getContext(), 4), DensityUtil.dp2px(getContext(), 4), Path.Direction.CW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvas.clipPath(path);
        } else {
            canvas.clipPath(path, Region.Op.REPLACE);
        }

        super.dispatchDraw(canvas);
    }

    private void init(Context context) {
        binding.getRoot().setOnClickListener(null);
    }

    public void setListener(@NonNull OnBtnClickListener clickListener) {
        //this.activity = readBookActivity;
        this.clickListener = clickListener;
        initData();
        bindEvent();
    }

    private void initData() {

    }

    private void bindEvent() {

        //复制
        binding.flCp.setOnClickListener(v -> clickListener.copySelect());

        //替换
        binding.flReplace.setOnClickListener(v -> clickListener.replaceSelect());

        //标记广告
        binding.flReplaceAd.setOnClickListener(v -> clickListener.replaceSelectAd());
    }

    public interface OnBtnClickListener {
        void copySelect();

        void replaceSelect();

        void replaceSelectAd();

    }
}