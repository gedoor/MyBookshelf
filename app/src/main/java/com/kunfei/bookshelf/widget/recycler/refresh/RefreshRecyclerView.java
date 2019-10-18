package com.kunfei.bookshelf.widget.recycler.refresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RefreshRecyclerView extends FrameLayout {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.rpb)
    RefreshProgressBar rpb;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    private View noDataView;
    private View refreshErrorView;
    private float durTouchX = -1000000;
    private float durTouchY = -1000000;
    private BaseRefreshListener baseRefreshListener;
    private OnLoadMoreListener loadMoreListener;
    private OnTouchListener refreshTouchListener = new OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    durTouchX = event.getX();
                    durTouchY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (durTouchX == -1000000) {
                        durTouchX = event.getX();
                    }
                    if (durTouchY == -1000000)
                        durTouchY = event.getY();

                    float dY = event.getY() - durTouchY;  //>0下拉
                    durTouchY = event.getY();
                    if (baseRefreshListener != null && ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getIsRequesting() == 0 && rpb.getSecondDurProgress() == rpb.getSecondFinalProgress()) {
                        if (rpb.getVisibility() != View.VISIBLE) {
                            rpb.setVisibility(View.VISIBLE);
                        }
                        if (recyclerView.getAdapter().getItemCount() > 0) {
                            if (0 == ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstCompletelyVisibleItemPosition()) {
                                rpb.setSecondDurProgress((int) (rpb.getSecondDurProgress() + dY));
                            }
                        } else {
                            rpb.setSecondDurProgress((int) (rpb.getSecondDurProgress() + dY));
                        }
                        return rpb.getSecondDurProgress() > 0;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (baseRefreshListener != null && rpb.getSecondMaxProgress() > 0 && rpb.getSecondDurProgress() > 0) {
                        if (rpb.getSecondDurProgress() >= rpb.getSecondMaxProgress() && ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getIsRequesting() == 0) {
                            if (baseRefreshListener instanceof OnRefreshWithProgressListener) {
                                //带有进度的
                                //执行刷新响应
                                ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsAll(false, false);
                                ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsRequesting(1, true);
                                rpb.setMaxProgress(((OnRefreshWithProgressListener) baseRefreshListener).getMaxProgress());
                                baseRefreshListener.startRefresh();
                                if (noDataView != null) {
                                    noDataView.setVisibility(GONE);
                                }
                                if (refreshErrorView != null) {
                                    refreshErrorView.setVisibility(GONE);
                                }
                            } else {
                                //不带进度的
                                ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsAll(false, false);
                                ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsRequesting(1, true);
                                baseRefreshListener.startRefresh();
                                if (noDataView != null) {
                                    noDataView.setVisibility(GONE);
                                }
                                if (refreshErrorView != null) {
                                    refreshErrorView.setVisibility(GONE);
                                }
                                rpb.setIsAutoLoading(true);
                            }
                        } else {
                            if (((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getIsRequesting() != 1)
                                rpb.setSecondDurProgressWithAnim(0);
                        }
                    }
                    durTouchX = -1000000;
                    durTouchY = -1000000;
                    break;
            }
            return false;
        }
    };

    public RefreshRecyclerView(Context context) {
        this(context, null);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.view_refresh_recycler, this, false);
        ButterKnife.bind(this, view);

        @SuppressLint("CustomViewStyleable")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshProgressBar);
        rpb.setSpeed(a.getDimensionPixelSize(R.styleable.RefreshProgressBar_speed, rpb.getSpeed()));
        rpb.setMaxProgress(a.getInt(R.styleable.RefreshProgressBar_max_progress, rpb.getMaxProgress()));
        rpb.setSecondMaxProgress(a.getDimensionPixelSize(R.styleable.RefreshProgressBar_second_max_progress, rpb.getSecondMaxProgress()));
        rpb.setBgColor(a.getColor(R.styleable.RefreshProgressBar_bg_color, rpb.getBgColor()));
        rpb.setSecondColor(a.getColor(R.styleable.RefreshProgressBar_second_color, rpb.getSecondColor()));
        rpb.setFontColor(a.getColor(R.styleable.RefreshProgressBar_font_color, rpb.getFontColor()));
        a.recycle();

        bindEvent();

        addView(view);
    }

    public void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        recyclerView.addItemDecoration(decor);
    }

    public void setBaseRefreshListener(BaseRefreshListener baseRefreshListener) {
        this.baseRefreshListener = baseRefreshListener;
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindEvent() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).canLoadMore()
                        && recyclerView.getAdapter().getItemCount() - 1 == ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findLastVisibleItemPosition()) {
                    if (!((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).getLoadMoreError()) {
                        if (null != loadMoreListener) {
                            ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsRequesting(2, false);
                            loadMoreListener.startLoadMore();
                        }
                    }
                }
            }
        });
        recyclerView.setOnTouchListener(refreshTouchListener);
    }

    public RefreshProgressBar getRpb() {
        return rpb;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void refreshError() {
        rpb.setIsAutoLoading(false);
        rpb.clean();
        ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setIsRequesting(0, true);
        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }
        if (refreshErrorView != null) {
            refreshErrorView.setVisibility(VISIBLE);
        }
    }

    public void startRefresh() {
        if (baseRefreshListener instanceof OnRefreshWithProgressListener) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setIsAll(false, false);
            ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsRequesting(1, false);
            rpb.setSecondDurProgress(rpb.getSecondMaxProgress());
            rpb.setMaxProgress(((OnRefreshWithProgressListener) baseRefreshListener).getMaxProgress());
        } else {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setIsRequesting(1, true);
            rpb.setIsAutoLoading(true);
            if (noDataView != null) {
                noDataView.setVisibility(GONE);
            }
            if (refreshErrorView != null) {
                refreshErrorView.setVisibility(GONE);
            }
        }
    }

    public void finishRefresh(Boolean needNotify) {
        finishRefresh(((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getICount() == 0, needNotify);
    }

    public void finishRefresh(Boolean isAll, Boolean needNotify) {
        rpb.setDurProgress(0);
        if (isAll) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setIsRequesting(0, false);
            rpb.setIsAutoLoading(false);
            ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsAll(true, needNotify);
        } else {
            rpb.setIsAutoLoading(false);
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setIsRequesting(0, needNotify);
        }

        if (isAll) {
            if (noDataView != null) {
                recyclerView.post(() -> {
                    if (((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).getICount() == 0) {
                        noDataView.setVisibility(VISIBLE);
                    } else {
                        noDataView.setVisibility(GONE);
                    }
                });
            }
            if (refreshErrorView != null) {
                refreshErrorView.setVisibility(GONE);
            }
        }
    }

    public void finishLoadMore(Boolean isAll, Boolean needNoti) {
        if (isAll) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setIsRequesting(0, false);
            ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsAll(true, needNoti);
        } else {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setIsRequesting(0, needNoti);
        }

        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }
        if (refreshErrorView != null) {
            refreshErrorView.setVisibility(GONE);
        }
    }

    public void setRefreshRecyclerViewAdapter(RefreshRecyclerViewAdapter refreshRecyclerViewAdapter, RecyclerView.LayoutManager layoutManager) {
        refreshRecyclerViewAdapter.setClickTryAgainListener(() -> {
            if (loadMoreListener != null)
                loadMoreListener.loadMoreErrorTryAgain();
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setRefreshRecyclerViewAdapter(View headerView, RefreshRecyclerViewAdapter refreshRecyclerViewAdapter, RecyclerView.LayoutManager layoutManager) {
        refreshRecyclerViewAdapter.setClickTryAgainListener(() -> {
            if (loadMoreListener != null)
                loadMoreListener.loadMoreErrorTryAgain();
        });
        llContent.addView(headerView, 0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setItemTouchHelperCallback(ItemTouchHelper.Callback callback) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void loadMoreError() {
        rpb.setIsAutoLoading(false);
        rpb.clean();
        ((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setLoadMoreError(true, true);
    }

    public void setNoDataAndRefreshErrorView(View noData, View refreshError) {
        if (noData != null) {
            noDataView = noData;
            noDataView.setVisibility(GONE);
            addView(noDataView, getChildCount() - 1);
        }
        if (refreshError != null) {
            refreshErrorView = refreshError;
            addView(refreshErrorView, 2);
            refreshErrorView.setVisibility(GONE);
        }
    }

}