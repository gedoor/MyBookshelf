package com.kunfei.bookshelf.view.popupwindow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.DensityUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadLongPressPop extends FrameLayout {


    //翻页相关
    @BindView(R.id.fl_replace)
    FrameLayout flReplace;
    @BindView(R.id.fl_cp)
    FrameLayout flCp;


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
        View view = LayoutInflater.from(context).inflate(R.layout.pop_read_long_press, null);
        addView(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);
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

        //翻页1
        flCp.setOnClickListener(v -> clickListener.copySelect());

        //翻页2
        flReplace.setOnClickListener(v -> clickListener.replaceSelect());
    }

    public interface OnBtnClickListener {
        void copySelect();

        void replaceSelect();

    }
}