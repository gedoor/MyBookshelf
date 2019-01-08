package com.kunfei.bookshelf.widget.recycler.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kunfei.bookshelf.R;

/**
 * Created by newbiechen on 17-4-22.
 * 功能:
 * 1. 加载动画
 * 2. 加载错误点击重新加载
 */

public class RefreshLayout extends FrameLayout {

    protected static final int STATUS_LOADING = 0;
    protected static final int STATUS_FINISH = 1;
    protected static final int STATUS_ERROR = 2;
    protected static final int STATUS_EMPTY = 3;
    private static final String TAG = "RefreshLayout";
    private Context mContext;

    private int mEmptyViewId;
    private int mErrorViewId;
    private int mLoadingViewId;

    private View mEmptyView;
    private View mErrorView;
    private View mLoadingView;
    private View mContentView;

    private OnReloadingListener mListener;
    private int mStatus = 0;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttrs(attrs);
        initView();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.RefreshLayout);
        mEmptyViewId = typedArray.getResourceId(R.styleable.RefreshLayout_layout_refresh_empty, R.layout.view_empty);
        mErrorViewId = typedArray.getResourceId(R.styleable.RefreshLayout_layout_refresh_error, R.layout.view_net_error);
        mLoadingViewId = typedArray.getResourceId(R.styleable.RefreshLayout_layout_refresh_loading, R.layout.view_loading);

        typedArray.recycle();
    }

    private void initView() {

        //添加在empty、error、loading 情况下的布局
        mEmptyView = inflateView(mEmptyViewId);
        mErrorView = inflateView(mErrorViewId);
        mLoadingView = inflateView(mLoadingViewId);

        addView(mEmptyView);
        addView(mErrorView);
        addView(mLoadingView);

        //设置监听器
        mErrorView.setOnClickListener(
                (view) -> {
                    if (mListener != null) {
                        toggleStatus(STATUS_LOADING);
                        mListener.onReload();
                    }
                }
        );
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        toggleStatus(STATUS_LOADING);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (getChildCount() == 4) {
            mContentView = child;
        }
    }

    //除了自带的数据，保证子类只能够添加一个子View
    @Override
    public void addView(View child) {
        if (getChildCount() > 4) {
            throw new IllegalStateException("RefreshLayout can host only one direct child");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 4) {
            throw new IllegalStateException("RefreshLayout can host only one direct child");
        }

        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 4) {
            throw new IllegalStateException("RefreshLayout can host only one direct child");
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 4) {
            throw new IllegalStateException("RefreshLayout can host only one direct child");
        }

        super.addView(child, index, params);
    }

    public void showLoading() {
        if (mStatus != STATUS_LOADING) {
            toggleStatus(STATUS_LOADING);
        }
    }

    public void showFinish() {
        if (mStatus == STATUS_LOADING) {
            toggleStatus(STATUS_FINISH);
        }
    }

    public void showError() {
        if (mStatus != STATUS_ERROR) {
            toggleStatus(STATUS_ERROR);
        }
    }

    public void showEmpty() {
        if (mStatus != STATUS_EMPTY) {
            toggleStatus(STATUS_EMPTY);
        }
    }

    //视图根据状态切换
    private void toggleStatus(int status) {
        switch (status) {
            case STATUS_LOADING:
                mLoadingView.setVisibility(VISIBLE);
                mEmptyView.setVisibility(GONE);
                mErrorView.setVisibility(GONE);
                if (mContentView != null) {
                    mContentView.setVisibility(GONE);
                }
                break;
            case STATUS_FINISH:
                if (mContentView != null) {
                    mContentView.setVisibility(VISIBLE);
                }
                mLoadingView.setVisibility(GONE);
                mEmptyView.setVisibility(GONE);
                mErrorView.setVisibility(GONE);
                break;
            case STATUS_ERROR:
                mErrorView.setVisibility(VISIBLE);
                mLoadingView.setVisibility(GONE);
                mEmptyView.setVisibility(GONE);
                if (mContentView != null) {
                    mContentView.setVisibility(GONE);
                }
                break;
            case STATUS_EMPTY:
                mEmptyView.setVisibility(VISIBLE);
                mErrorView.setVisibility(GONE);
                mLoadingView.setVisibility(GONE);
                if (mContentView != null) {
                    mContentView.setVisibility(GONE);
                }
                break;
        }
        mStatus = status;
    }

    protected int getStatus() {
        return mStatus;
    }

    public void setOnReloadingListener(OnReloadingListener listener) {
        mListener = listener;
    }

    private View inflateView(int id) {
        return LayoutInflater.from(mContext)
                .inflate(id, this, false);
    }

    //数据存储
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superParcel = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superParcel);
        savedState.status = mStatus;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        //刷新状态
        toggleStatus(savedState.status);
    }

    //添加错误重新加载的监听
    public interface OnReloadingListener {
        void onReload();
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int status;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            status = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(status);
        }
    }
}
