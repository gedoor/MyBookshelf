package com.kunfei.bookshelf.widget.recycler.scroller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.kunfei.bookshelf.R;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public class FastScrollRecyclerView extends RecyclerView {


    private FastScroller mFastScroller;


    public FastScrollRecyclerView(Context context) {

        super(context);

        layout(context, null);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    }


    public FastScrollRecyclerView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);

    }


    public FastScrollRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        layout(context, attrs);

    }


    @Override

    public void setAdapter(Adapter adapter) {

        super.setAdapter(adapter);


        if (adapter instanceof FastScroller.SectionIndexer) {

            setSectionIndexer((FastScroller.SectionIndexer) adapter);

        } else if (adapter == null) {

            setSectionIndexer(null);

        }

    }


    @Override

    public void setVisibility(int visibility) {

        super.setVisibility(visibility);

        mFastScroller.setVisibility(visibility);

    }


    /**
     * Set the {@link FastScroller.SectionIndexer} for the {@link FastScroller}.
     *
     * @param sectionIndexer The SectionIndexer that provides section text for the FastScroller
     */

    public void setSectionIndexer(FastScroller.SectionIndexer sectionIndexer) {

        mFastScroller.setSectionIndexer(sectionIndexer);

    }


    /**
     * Set the enabled state of fast scrolling.
     *
     * @param enabled True to enable fast scrolling, false otherwise
     */

    public void setFastScrollEnabled(boolean enabled) {

        mFastScroller.setEnabled(enabled);

    }


    /**
     * Hide the scrollbar when not scrolling.
     *
     * @param hideScrollbar True to hide the scrollbar, false to show
     */

    public void setHideScrollbar(boolean hideScrollbar) {

        mFastScroller.setFadeScrollbar(hideScrollbar);

    }


    /**
     * Display a scroll track while scrolling.
     *
     * @param visible True to show scroll track, false to hide
     */

    public void setTrackVisible(boolean visible) {

        mFastScroller.setTrackVisible(visible);

    }


    /**
     * Set the color of the scroll track.
     *
     * @param color The color for the scroll track
     */

    public void setTrackColor(@ColorInt int color) {

        mFastScroller.setTrackColor(color);

    }


    /**
     * Set the color for the scroll handle.
     *
     * @param color The color for the scroll handle
     */

    public void setHandleColor(@ColorInt int color) {

        mFastScroller.setHandleColor(color);

    }


    /**
     * Show the section bubble while scrolling.
     *
     * @param visible True to show the bubble, false to hide
     */

    public void setBubbleVisible(boolean visible) {

        mFastScroller.setBubbleVisible(visible);

    }


    /**
     * Set the background color of the index bubble.
     *
     * @param color The background color for the index bubble
     */

    public void setBubbleColor(@ColorInt int color) {

        mFastScroller.setBubbleColor(color);

    }


    /**
     * Set the text color of the index bubble.
     *
     * @param color The text color for the index bubble
     */

    public void setBubbleTextColor(@ColorInt int color) {

        mFastScroller.setBubbleTextColor(color);

    }


    /**
     * Set the fast scroll state change listener.
     *
     * @param fastScrollStateChangeListener The interface that will listen to fastscroll state change events
     */

    public void setFastScrollStateChangeListener(FastScrollStateChangeListener fastScrollStateChangeListener) {

        mFastScroller.setFastScrollStateChangeListener(fastScrollStateChangeListener);

    }


    @Override

    protected void onAttachedToWindow() {

        super.onAttachedToWindow();

        mFastScroller.attachRecyclerView(this);


        ViewParent parent = getParent();


        if (parent instanceof ViewGroup) {

            ViewGroup viewGroup = (ViewGroup) parent;

            viewGroup.addView(mFastScroller);

            mFastScroller.setLayoutParams(viewGroup);

        }

    }


    @Override

    protected void onDetachedFromWindow() {

        mFastScroller.detachRecyclerView();

        super.onDetachedFromWindow();

    }


    private void layout(Context context, AttributeSet attrs) {

        mFastScroller = new FastScroller(context, attrs);

        mFastScroller.setId(R.id.fastscroller);

    }

}