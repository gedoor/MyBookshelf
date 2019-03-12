package com.kunfei.bookshelf.widget.recycler.scroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


public class FastScroller extends LinearLayout {
    private static final int sBubbleAnimDuration = 100;
    private static final int sScrollbarAnimDuration = 300;
    private static final int sScrollbarHideDelay = 1000;
    private static final int sTrackSnapRange = 5;
    @ColorInt
    private int mBubbleColor;
    @ColorInt
    private int mHandleColor;
    private int mBubbleHeight;
    private int mHandleHeight;
    private int mViewHeight;
    private boolean mFadeScrollbar;
    private boolean mShowBubble;
    private SectionIndexer mSectionIndexer;
    private ViewPropertyAnimator mScrollbarAnimator;
    private ViewPropertyAnimator mBubbleAnimator;
    private RecyclerView mRecyclerView;
    private TextView mBubbleView;
    private ImageView mHandleView;
    private ImageView mTrackView;
    private View mScrollbar;
    private Drawable mBubbleImage;
    private Drawable mHandleImage;
    private Drawable mTrackImage;
    private FastScrollStateChangeListener mFastScrollStateChangeListener;
    private Runnable mScrollbarHider = this::hideScrollbar;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!mHandleView.isSelected() && isEnabled()) {
                setViewPositions(getScrollProportion(recyclerView));
            }
        }
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (isEnabled()) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        getHandler().removeCallbacks(mScrollbarHider);
                        cancelAnimation(mScrollbarAnimator);
                        if (!isViewVisible(mScrollbar)) {
                            showScrollbar();
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (mFadeScrollbar && !mHandleView.isSelected()) {
                            getHandler().postDelayed(mScrollbarHider, sScrollbarHideDelay);
                        }
                        break;
                }
            }
        }
    };

    public FastScroller(Context context) {
        super(context);
        layout(context, null);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
    }

    public FastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        layout(context, attrs);
        setLayoutParams(generateLayoutParams(attrs));
    }

    @Override
    public void setLayoutParams(@NonNull ViewGroup.LayoutParams params) {
        params.width = LayoutParams.WRAP_CONTENT;
        super.setLayoutParams(params);
    }

    public void setLayoutParams(@NonNull ViewGroup viewGroup) {
        @IdRes int recyclerViewId = mRecyclerView != null ? mRecyclerView.getId() : NO_ID;
        int marginTop = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_margin_top);
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_margin_bottom);
        if (recyclerViewId == NO_ID) {
            throw new IllegalArgumentException("RecyclerView must have a view ID");
        }
        if (viewGroup instanceof ConstraintLayout) {
            ConstraintSet constraintSet = new ConstraintSet();
            @IdRes int layoutId = getId();
            constraintSet.clone((ConstraintLayout) viewGroup);
            constraintSet.connect(layoutId, ConstraintSet.TOP, recyclerViewId, ConstraintSet.TOP);
            constraintSet.connect(layoutId, ConstraintSet.BOTTOM, recyclerViewId, ConstraintSet.BOTTOM);
            constraintSet.connect(layoutId, ConstraintSet.END, recyclerViewId, ConstraintSet.END);
            constraintSet.applyTo((ConstraintLayout) viewGroup);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) getLayoutParams();
            layoutParams.setMargins(0, marginTop, 0, marginBottom);
            setLayoutParams(layoutParams);
        } else if (viewGroup instanceof CoordinatorLayout) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
            layoutParams.setAnchorId(recyclerViewId);
            layoutParams.anchorGravity = GravityCompat.END;
            layoutParams.setMargins(0, marginTop, 0, marginBottom);
            setLayoutParams(layoutParams);
        } else if (viewGroup instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
            layoutParams.gravity = GravityCompat.END;
            layoutParams.setMargins(0, marginTop, 0, marginBottom);
            setLayoutParams(layoutParams);
        } else if (viewGroup instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
            int endRule = RelativeLayout.ALIGN_END;
            layoutParams.addRule(RelativeLayout.ALIGN_TOP, recyclerViewId);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, recyclerViewId);
            layoutParams.addRule(endRule, recyclerViewId);
            layoutParams.setMargins(0, marginTop, 0, marginBottom);
            setLayoutParams(layoutParams);
        } else {
            throw new IllegalArgumentException("Parent ViewGroup must be a ConstraintLayout, CoordinatorLayout, FrameLayout, or RelativeLayout");
        }
        updateViewHeights();
    }

    public void setSectionIndexer(SectionIndexer sectionIndexer) {
        mSectionIndexer = sectionIndexer;
    }

    public void attachRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            mRecyclerView.addOnScrollListener(mScrollListener);
            post(() -> {
                // set initial positions for bubble and handle
                setViewPositions(getScrollProportion(mRecyclerView));
            });
        }
    }

    public void detachRecyclerView() {
        if (mRecyclerView != null) {
            mRecyclerView.removeOnScrollListener(mScrollListener);
            mRecyclerView = null;
        }
    }

    /**
     * Hide the scrollbar when not scrolling.
     *
     * @param fadeScrollbar True to hide the scrollbar, false to show
     */
    public void setFadeScrollbar(boolean fadeScrollbar) {
        mFadeScrollbar = fadeScrollbar;
        mScrollbar.setVisibility(fadeScrollbar ? GONE : VISIBLE);
    }

    /**
     * Show the section bubble while scrolling.
     *
     * @param visible True to show the bubble, false to hide
     */
    public void setBubbleVisible(boolean visible) {
        mShowBubble = visible;
    }

    /**
     * Display a scroll track while scrolling.
     *
     * @param visible True to show scroll track, false to hide
     */
    public void setTrackVisible(boolean visible) {
        mTrackView.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * Set the color of the scroll track.
     *
     * @param color The color for the scroll track
     */
    public void setTrackColor(@ColorInt int color) {
        @ColorInt int trackColor = color;
        if (mTrackImage == null) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_track);
            if (drawable != null) {
                mTrackImage = DrawableCompat.wrap(drawable);
            }
        }
        DrawableCompat.setTint(mTrackImage, trackColor);
        mTrackView.setImageDrawable(mTrackImage);
    }

    /**
     * Set the color for the scroll handle.
     *
     * @param color The color for the scroll handle
     */
    public void setHandleColor(@ColorInt int color) {
        mHandleColor = color;
        if (mHandleImage == null) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_handle);
            if (drawable != null) {
                mHandleImage = DrawableCompat.wrap(drawable);
            }
        }
        DrawableCompat.setTint(mHandleImage, mHandleColor);
        mHandleView.setImageDrawable(mHandleImage);
    }

    /**
     * Set the background color of the index bubble.
     *
     * @param color The background color for the index bubble
     */
    public void setBubbleColor(@ColorInt int color) {
        mBubbleColor = color;
        if (mBubbleImage == null) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_bubble);
            if (drawable != null) {
                mBubbleImage = DrawableCompat.wrap(drawable);
            }
        }
        DrawableCompat.setTint(mBubbleImage, mBubbleColor);
        mBubbleView.setBackground(mBubbleImage);
    }

    /**
     * Set the text color of the index bubble.
     *
     * @param color The text color for the index bubble
     */
    public void setBubbleTextColor(@ColorInt int color) {
        mBubbleView.setTextColor(color);
    }

    /**
     * Set the fast scroll state change listener.
     *
     * @param fastScrollStateChangeListener The interface that will listen to fastscroll state change events
     */
    public void setFastScrollStateChangeListener(FastScrollStateChangeListener fastScrollStateChangeListener) {
        mFastScrollStateChangeListener = fastScrollStateChangeListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setVisibility(enabled ? VISIBLE : GONE);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < mHandleView.getX() - ViewCompat.getPaddingStart(mHandleView)) {
                    return false;
                }
                requestDisallowInterceptTouchEvent(true);
                setHandleSelected(true);
                getHandler().removeCallbacks(mScrollbarHider);
                cancelAnimation(mScrollbarAnimator);
                cancelAnimation(mBubbleAnimator);
                if (!isViewVisible(mScrollbar)) {
                    showScrollbar();
                }
                if (mShowBubble && mSectionIndexer != null) {
                    showBubble();
                }
                if (mFastScrollStateChangeListener != null) {
                    mFastScrollStateChangeListener.onFastScrollStart(this);
                }
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                setViewPositions(y);
                setRecyclerViewPosition(y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                requestDisallowInterceptTouchEvent(false);
                setHandleSelected(false);
                if (mFadeScrollbar) {
                    getHandler().postDelayed(mScrollbarHider, sScrollbarHideDelay);
                }
                hideBubble();
                if (mFastScrollStateChangeListener != null) {
                    mFastScrollStateChangeListener.onFastScrollStop(this);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewHeight = h;
    }

    private void setRecyclerViewPosition(float y) {
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            int itemCount = mRecyclerView.getAdapter().getItemCount();
            float proportion;
            if (mHandleView.getY() == 0) {
                proportion = 0f;
            } else if (mHandleView.getY() + mHandleHeight >= mViewHeight - sTrackSnapRange) {
                proportion = 1f;
            } else {
                proportion = y / (float) mViewHeight;
            }
            int scrolledItemCount = Math.round(proportion * itemCount);
            if (isLayoutReversed(mRecyclerView.getLayoutManager())) {
                scrolledItemCount = itemCount - scrolledItemCount;
            }
            int targetPos = getValueInRange(0, itemCount - 1, scrolledItemCount);
            mRecyclerView.getLayoutManager().scrollToPosition(targetPos);
            if (mShowBubble && mSectionIndexer != null) {
                mBubbleView.setText(mSectionIndexer.getSectionText(targetPos));
            }
        }
    }

    private float getScrollProportion(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return 0;
        }
        final int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
        final int verticalScrollRange = recyclerView.computeVerticalScrollRange();
        final float rangeDiff = verticalScrollRange - mViewHeight;
        float proportion = (float) verticalScrollOffset / (rangeDiff > 0 ? rangeDiff : 1f);
        return mViewHeight * proportion;
    }

    @SuppressWarnings("SameParameterValue")
    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void setViewPositions(float y) {
        mBubbleHeight = mBubbleView.getHeight();
        mHandleHeight = mHandleView.getHeight();
        int bubbleY = getValueInRange(0, mViewHeight - mBubbleHeight - mHandleHeight / 2, (int) (y - mBubbleHeight));
        int handleY = getValueInRange(0, mViewHeight - mHandleHeight, (int) (y - mHandleHeight / 2));
        if (mShowBubble) {
            mBubbleView.setY(bubbleY);
        }
        mHandleView.setY(handleY);
    }

    private void updateViewHeights() {
        int measureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        mBubbleView.measure(measureSpec, measureSpec);
        mBubbleHeight = mBubbleView.getMeasuredHeight();
        mHandleView.measure(measureSpec, measureSpec);
        mHandleHeight = mHandleView.getMeasuredHeight();
    }

    private boolean isLayoutReversed(@NonNull final RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getReverseLayout();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getReverseLayout();
        }
        return false;
    }

    private boolean isViewVisible(View view) {
        return view != null && view.getVisibility() == VISIBLE;
    }

    private void cancelAnimation(ViewPropertyAnimator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }

    private void showBubble() {
        if (!isViewVisible(mBubbleView)) {
            mBubbleView.setVisibility(VISIBLE);
            mBubbleAnimator = mBubbleView.animate().alpha(1f)
                    .setDuration(sBubbleAnimDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        // adapter required for new alpha value to stick
                    });
        }
    }

    private void hideBubble() {
        if (isViewVisible(mBubbleView)) {
            mBubbleAnimator = mBubbleView.animate().alpha(0f)
                    .setDuration(sBubbleAnimDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mBubbleView.setVisibility(GONE);
                            mBubbleAnimator = null;
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            mBubbleView.setVisibility(GONE);
                            mBubbleAnimator = null;
                        }
                    });
        }
    }

    private void showScrollbar() {
        if (mRecyclerView.computeVerticalScrollRange() - mViewHeight > 0) {
            float transX = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding_end);
            mScrollbar.setTranslationX(transX);
            mScrollbar.setVisibility(VISIBLE);
            mScrollbarAnimator = mScrollbar.animate().translationX(0f).alpha(1f)
                    .setDuration(sScrollbarAnimDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        // adapter required for new alpha value to stick
                    });
        }
    }

    private void hideScrollbar() {
        float transX = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding_end);
        mScrollbarAnimator = mScrollbar.animate().translationX(transX).alpha(0f)
                .setDuration(sScrollbarAnimDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mScrollbar.setVisibility(GONE);
                        mScrollbarAnimator = null;
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mScrollbar.setVisibility(GONE);
                        mScrollbarAnimator = null;
                    }
                });
    }

    private void setHandleSelected(boolean selected) {
        mHandleView.setSelected(selected);
        DrawableCompat.setTint(mHandleImage, selected ? mBubbleColor : mHandleColor);
    }

    @SuppressWarnings("ConstantConditions")
    private void layout(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_fastscroller, this);
        setClipChildren(false);
        setOrientation(HORIZONTAL);
        mBubbleView = findViewById(R.id.fastscroll_bubble);
        mHandleView = findViewById(R.id.fastscroll_handle);
        mTrackView = findViewById(R.id.fastscroll_track);
        mScrollbar = findViewById(R.id.fastscroll_scrollbar);
        @ColorInt int bubbleColor = ColorUtil.adjustAlpha(ThemeStore.accentColor(context), 0.8f);
        @ColorInt int handleColor = ThemeStore.accentColor(context);
        @ColorInt int trackColor = context.getResources().getColor(R.color.transparent30);
        @ColorInt int textColor = ColorUtil.isColorLight(bubbleColor) ? Color.BLACK : Color.WHITE;
        boolean fadeScrollbar = true;
        boolean showBubble = false;
        boolean showTrack = true;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FastScroller, 0, 0);
            if (typedArray != null) {
                try {
                    bubbleColor = typedArray.getColor(R.styleable.FastScroller_bubbleColor, bubbleColor);
                    handleColor = typedArray.getColor(R.styleable.FastScroller_handleColor, handleColor);
                    trackColor = typedArray.getColor(R.styleable.FastScroller_trackColor, trackColor);
                    textColor = typedArray.getColor(R.styleable.FastScroller_bubbleTextColor, textColor);
                    fadeScrollbar = typedArray.getBoolean(R.styleable.FastScroller_fadeScrollbar, fadeScrollbar);
                    showBubble = typedArray.getBoolean(R.styleable.FastScroller_showBubble, showBubble);
                    showTrack = typedArray.getBoolean(R.styleable.FastScroller_showTrack, showTrack);
                } finally {
                    typedArray.recycle();
                }
            }
        }
        setTrackColor(trackColor);
        setHandleColor(handleColor);
        setBubbleColor(bubbleColor);
        setBubbleTextColor(textColor);
        setFadeScrollbar(fadeScrollbar);
        setBubbleVisible(showBubble);
        setTrackVisible(showTrack);
    }

    public interface SectionIndexer {
        String getSectionText(int position);
    }

}
