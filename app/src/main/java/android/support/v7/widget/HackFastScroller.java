package android.support.v7.widget;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.monke.monkeybook.R;

public class HackFastScroller extends FastScroller {
    private final int mMinVerticalThumbHeight;

    public HackFastScroller(RecyclerView recyclerView, StateListDrawable verticalThumbDrawable, Drawable verticalTrackDrawable, StateListDrawable horizontalThumbDrawable, Drawable horizontalTrackDrawable, int defaultWidth, int scrollbarMinimumRange, int margin) {
        super(recyclerView, verticalThumbDrawable, verticalTrackDrawable, horizontalThumbDrawable, horizontalTrackDrawable, defaultWidth, scrollbarMinimumRange, margin);
        mMinVerticalThumbHeight = recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.fast_scroll_min_thumb_height);
    }

    @Override
    void updateScrollPosition(int offsetX, int offsetY) {
        super.updateScrollPosition(offsetX, offsetY);
        if (mVerticalThumbHeight < mMinVerticalThumbHeight) {
            mVerticalThumbHeight = mMinVerticalThumbHeight;
        }
    }
}
