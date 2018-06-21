package android.support.v7.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.monke.monkeybook.R;

public class HackRecyclerView extends RecyclerView {
    public HackRecyclerView(Context context) {
        this(context, null);
    }

    public HackRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HackRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        byte defStyleRes = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerView, defStyle, defStyleRes);
        StateListDrawable verticalThumbDrawable = (StateListDrawable) a.getDrawable(R.styleable.RecyclerView_fastScrollVerticalThumbDrawable);
        Drawable verticalTrackDrawable = a.getDrawable(R.styleable.RecyclerView_fastScrollVerticalTrackDrawable);
        StateListDrawable horizontalThumbDrawable = (StateListDrawable) a.getDrawable(R.styleable.RecyclerView_fastScrollHorizontalThumbDrawable);
        Drawable horizontalTrackDrawable = a.getDrawable(R.styleable.RecyclerView_fastScrollHorizontalTrackDrawable);
        a.recycle();
        initFastScroller(verticalThumbDrawable, verticalTrackDrawable, horizontalThumbDrawable, horizontalTrackDrawable);
    }

    void initFastScroller(StateListDrawable verticalThumbDrawable, Drawable verticalTrackDrawable, StateListDrawable horizontalThumbDrawable, Drawable horizontalTrackDrawable) {
        if (verticalThumbDrawable != null && verticalTrackDrawable != null && horizontalThumbDrawable != null && horizontalTrackDrawable != null) {
            Resources resources = getContext().getResources();
            new HackFastScroller(this, verticalThumbDrawable, verticalTrackDrawable, horizontalThumbDrawable, horizontalTrackDrawable,
                    resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness), resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
                    resources.getDimensionPixelOffset(R.dimen.fastscroll_margin));
        }
    }
}
