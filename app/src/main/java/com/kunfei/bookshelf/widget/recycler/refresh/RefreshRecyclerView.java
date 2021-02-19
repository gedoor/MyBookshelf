package com.kunfei.bookshelf.widget.recycler.refresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.databinding.ViewRefreshRecyclerBinding;

import java.util.Objects;

public class RefreshRecyclerView extends FrameLayout {

    private ViewRefreshRecyclerBinding binding = ViewRefreshRecyclerBinding.inflate(LayoutInflater.from(getContext()), this, true);
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
                    if (baseRefreshListener != null
                            && ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getIsRequesting() == 0
                            && binding.rpb.getSecondDurProgress() == binding.rpb.getSecondFinalProgress()) {
                        if (binding.rpb.getVisibility() != View.VISIBLE) {
                            binding.rpb.setVisibility(View.VISIBLE);
                        }
                        if (binding.recyclerView.getAdapter().getItemCount() > 0) {
                            if (0 == ((LinearLayoutManager) Objects.requireNonNull(binding.recyclerView.getLayoutManager())).findFirstCompletelyVisibleItemPosition()) {
                                binding.rpb.setSecondDurProgress((int) (binding.rpb.getSecondDurProgress() + dY));
                            }
                        } else {
                            binding.rpb.setSecondDurProgress((int) (binding.rpb.getSecondDurProgress() + dY));
                        }
                        return binding.rpb.getSecondDurProgress() > 0;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (baseRefreshListener != null && binding.rpb.getSecondMaxProgress() > 0 && binding.rpb.getSecondDurProgress() > 0) {
                        if (binding.rpb.getSecondDurProgress() >= binding.rpb.getSecondMaxProgress() && ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getIsRequesting() == 0) {
                            if (baseRefreshListener instanceof OnRefreshWithProgressListener) {
                                //带有进度的
                                //执行刷新响应
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(false, false);
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsRequesting(1, true);
                                binding.rpb.setMaxProgress(((OnRefreshWithProgressListener) baseRefreshListener).getMaxProgress());
                                baseRefreshListener.startRefresh();
                                if (noDataView != null) {
                                    noDataView.setVisibility(GONE);
                                }
                                if (refreshErrorView != null) {
                                    refreshErrorView.setVisibility(GONE);
                                }
                            } else {
                                //不带进度的
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(false, false);
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsRequesting(1, true);
                                baseRefreshListener.startRefresh();
                                if (noDataView != null) {
                                    noDataView.setVisibility(GONE);
                                }
                                if (refreshErrorView != null) {
                                    refreshErrorView.setVisibility(GONE);
                                }
                                binding.rpb.setIsAutoLoading(true);
                            }
                        } else {
                            if (((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getIsRequesting() != 1)
                                binding.rpb.setSecondDurProgressWithAnim(0);
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

        @SuppressLint("CustomViewStyleable")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshProgressBar);
        binding.rpb.setSpeed(a.getDimensionPixelSize(R.styleable.RefreshProgressBar_speed, binding.rpb.getSpeed()));
        binding.rpb.setMaxProgress(a.getInt(R.styleable.RefreshProgressBar_max_progress, binding.rpb.getMaxProgress()));
        binding.rpb.setSecondMaxProgress(a.getDimensionPixelSize(R.styleable.RefreshProgressBar_second_max_progress, binding.rpb.getSecondMaxProgress()));
        binding.rpb.setBgColor(a.getColor(R.styleable.RefreshProgressBar_bg_color, binding.rpb.getBgColor()));
        binding.rpb.setSecondColor(a.getColor(R.styleable.RefreshProgressBar_second_color, binding.rpb.getSecondColor()));
        binding.rpb.setFontColor(a.getColor(R.styleable.RefreshProgressBar_font_color, binding.rpb.getFontColor()));
        a.recycle();

        bindEvent();
    }

    public void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        binding.recyclerView.addItemDecoration(decor);
    }

    public void setBaseRefreshListener(BaseRefreshListener baseRefreshListener) {
        this.baseRefreshListener = baseRefreshListener;
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindEvent() {
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

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
        binding.recyclerView.setOnTouchListener(refreshTouchListener);
    }

    public RefreshProgressBar getRpb() {
        return binding.rpb;
    }

    public RecyclerView getRecyclerView() {
        return binding.recyclerView;
    }

    public void refreshError() {
        binding.rpb.setIsAutoLoading(false);
        binding.rpb.clean();
        ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, true);
        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }
        if (refreshErrorView != null) {
            refreshErrorView.setVisibility(VISIBLE);
        }
    }

    public void startRefresh() {
        if (baseRefreshListener instanceof OnRefreshWithProgressListener) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsAll(false, false);
            ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsRequesting(1, false);
            binding.rpb.setSecondDurProgress(binding.rpb.getSecondMaxProgress());
            binding.rpb.setMaxProgress(((OnRefreshWithProgressListener) baseRefreshListener).getMaxProgress());
        } else {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(1, true);
            binding.rpb.setIsAutoLoading(true);
            if (noDataView != null) {
                noDataView.setVisibility(GONE);
            }
            if (refreshErrorView != null) {
                refreshErrorView.setVisibility(GONE);
            }
        }
    }

    public void finishRefresh(Boolean needNotify) {
        finishRefresh(((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getICount() == 0, needNotify);
    }

    public void finishRefresh(Boolean isAll, Boolean needNotify) {
        binding.rpb.setDurProgress(0);
        if (isAll) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, false);
            binding.rpb.setIsAutoLoading(false);
            ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(true, needNotify);
        } else {
            binding.rpb.setIsAutoLoading(false);
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, needNotify);
        }

        if (isAll) {
            if (noDataView != null) {
                binding.recyclerView.post(() -> {
                    if (((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).getICount() == 0) {
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
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, false);
            ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(true, needNoti);
        } else {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, needNoti);
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
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setRefreshRecyclerViewAdapter(View headerView, RefreshRecyclerViewAdapter refreshRecyclerViewAdapter, RecyclerView.LayoutManager layoutManager) {
        refreshRecyclerViewAdapter.setClickTryAgainListener(() -> {
            if (loadMoreListener != null)
                loadMoreListener.loadMoreErrorTryAgain();
        });
        binding.llContent.addView(headerView, 0);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setItemTouchHelperCallback(ItemTouchHelper.Callback callback) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    public void loadMoreError() {
        binding.rpb.setIsAutoLoading(false);
        binding.rpb.clean();
        ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setLoadMoreError(true, true);
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