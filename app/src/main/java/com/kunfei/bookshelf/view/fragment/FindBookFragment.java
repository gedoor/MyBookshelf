package com.kunfei.bookshelf.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.FindBookPresenter;
import com.kunfei.bookshelf.presenter.contract.FindBookContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.activity.ChoiceBookActivity;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;
import com.kunfei.bookshelf.view.adapter.FindKindAdapter;
import com.kunfei.bookshelf.view.adapter.FindLeftAdapter;
import com.kunfei.bookshelf.view.adapter.FindRightAdapter;
import com.kunfei.bookshelf.widget.recycler.expandable.OnRecyclerViewListener;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;
import com.kunfei.bookshelf.widget.recycler.sectioned.GridSpacingItemDecoration;
import com.kunfei.bookshelf.widget.recycler.sectioned.SectionedSpanSizeLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class FindBookFragment extends MBaseFragment<FindBookContract.Presenter> implements FindBookContract.View, OnRecyclerViewListener.OnItemClickListener, OnRecyclerViewListener.OnItemLongClickListener {
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.rl_empty_view)
    RelativeLayout rlEmptyView;
    @BindView(R.id.rv_find_left)
    RecyclerView rvFindLeft;
    @BindView(R.id.rv_find_right)
    RecyclerView rvFindRight;
    @BindView(R.id.vw_divider)
    View vwDivider;

    private Unbinder unbinder;
    private FindLeftAdapter findLeftAdapter;
    private FindRightAdapter findRightAdapter;
    private FindKindAdapter findKindAdapter;
    private LinearLayoutManager leftLayoutManager;
    private RecyclerView.LayoutManager rightLayoutManager;
    private List<RecyclerViewData> data = new ArrayList<>();

    @Override
    public int createLayoutId() {
        return R.layout.fragment_book_find;
    }

    @Override
    protected FindBookContract.Presenter initInjector() {
        return new FindBookPresenter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void bindView() {
        super.bindView();
        unbinder = ButterKnife.bind(this, view);
        rvFindRight.addItemDecoration(new GridSpacingItemDecoration(10));
        refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
        refreshLayout.setOnRefreshListener(() -> {
            refreshData();
            refreshLayout.setRefreshing(false);
        });
        leftLayoutManager = new LinearLayoutManager(getContext());
        initRecyclerView();
    }

    /**
     * 首次逻辑操作
     */
    @Override
    protected void firstRequest() {
        super.firstRequest();
        refreshData();
    }

    public void refreshData() {
        if (mPresenter != null) {
            mPresenter.initData();
        }
    }

    @Override
    public void upData(List<RecyclerViewData> group) {
        this.data = group;
        upStyle();
        upUI();
    }

    public void upStyle() {
        if (rlEmptyView == null) return;
        initRecyclerView();
        if (isFlexBox()) {
            findRightAdapter.setData(data);
            findLeftAdapter.setData(data);
        } else {
            findKindAdapter.setAllDatas(data);
        }
        upUI();
    }

    public void upUI() {
        if (rlEmptyView == null) return;
        if (data.size() == 0) {
            tvEmpty.setText(R.string.no_find);
            rlEmptyView.setVisibility(View.VISIBLE);
        } else {
            rlEmptyView.setVisibility(View.GONE);
        }
        if (isFlexBox()) {
            rlEmptyView.setVisibility(View.GONE);
            if (data.size() <= 1 | !showLeftView()) {
                rvFindLeft.setVisibility(View.GONE);
                vwDivider.setVisibility(View.GONE);
            } else {
                rvFindLeft.setVisibility(View.VISIBLE);
                vwDivider.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isFlexBox() {
        return preferences.getBoolean("findTypeIsFlexBox", true);
    }

    private boolean showLeftView() {
        return preferences.getBoolean("showFindLeftView", true);
    }

    private void initRecyclerView() {
        if (rvFindRight == null) return;
        if (isFlexBox()) {
            findKindAdapter = null;
            findLeftAdapter = new FindLeftAdapter(getActivity(), pos -> {
                int counts = 0;
                for (int i = 0; i < pos; i++) {
                    //position 为点击的position
                    counts += findRightAdapter.getData().get(i).getChildList().size();
                }
                ((ScrollLinearLayoutManger) rightLayoutManager).scrollToPositionWithOffset(counts + pos, 0);
            });
            rvFindLeft.setLayoutManager(leftLayoutManager);
            rvFindLeft.setAdapter(findLeftAdapter);

            findRightAdapter = new FindRightAdapter(Objects.requireNonNull(getActivity()), this);
            //设置header
            rightLayoutManager = new ScrollLinearLayoutManger(getActivity(), 3);
            ((ScrollLinearLayoutManger) rightLayoutManager).setSpanSizeLookup(new SectionedSpanSizeLookup(findRightAdapter, (ScrollLinearLayoutManger) rightLayoutManager));
            rvFindRight.setLayoutManager(rightLayoutManager);
            rvFindRight.setLayoutManager(rightLayoutManager);
            rvFindRight.setItemViewCacheSize(10);
            rvFindRight.setItemAnimator(null);
            rvFindRight.setHasFixedSize(true);
            rvFindRight.setAdapter(findRightAdapter);
        } else {
            rightLayoutManager = new LinearLayoutManager(getContext());
            rvFindLeft.setVisibility(View.GONE);
            vwDivider.setVisibility(View.GONE);
            findLeftAdapter = null;
            findRightAdapter = null;
            findKindAdapter = new FindKindAdapter(getContext(), new ArrayList<>());
            findKindAdapter.setOnItemClickListener(this);
            findKindAdapter.setOnItemLongClickListener(this);
            findKindAdapter.setCanExpandAll(false);
            rvFindRight.setLayoutManager(rightLayoutManager);
            rvFindRight.setAdapter(findKindAdapter);
        }
    }

    @Override
    public void onGroupItemClick(int position, int groupPosition, View view) {

    }

    @Override
    public void onChildItemClick(int position, int groupPosition, int childPosition, View view) {
        FindKindBean kindBean = (FindKindBean) findKindAdapter.getAllDatas().get(groupPosition).getChild(childPosition);

        Intent intent = new Intent(getContext(), ChoiceBookActivity.class);
        intent.putExtra("url", kindBean.getKindUrl());
        intent.putExtra("title", kindBean.getKindName());
        intent.putExtra("tag", kindBean.getTag());
        startActivityByAnim(intent, view, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onGroupItemLongClick(int position, int groupPosition, View view) {
        if (getActivity() == null) return;
        FindKindGroupBean groupBean;
        if (isFlexBox()) {
            groupBean = (FindKindGroupBean) findRightAdapter.getData().get(groupPosition).getGroupData();
        } else {
            groupBean = (FindKindGroupBean) findKindAdapter.getAllDatas().get(groupPosition).getGroupData();
        }
        BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(groupBean.getGroupTag());
        if (sourceBean == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_edit, Menu.NONE, R.string.edit);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_top, Menu.NONE, R.string.to_top);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_del, Menu.NONE, R.string.delete);
        popupMenu.getMenu().add(Menu.NONE, R.id.menu_clear, Menu.NONE, R.string.clear_cache);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_edit:
                    SourceEditActivity.startThis(this, sourceBean);
                    break;
                case R.id.menu_top:
                    BookSourceManager.toTop(sourceBean)
                            .subscribe(new MySingleObserver<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    refreshData();
                                }
                            });
                    break;
                case R.id.menu_del:
                    BookSourceManager.removeBookSource(sourceBean);
                    refreshData();
                    break;
                case R.id.menu_clear:
                    ACache.get(getActivity(), "findCache").remove(sourceBean.getBookSourceUrl());
                    break;
            }
            return true;
        });
        popupMenu.show();

    }

    @Override
    public void onChildItemLongClick(int position, int groupPosition, int childPosition, View view) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SourceEditActivity.EDIT_SOURCE) {
                refreshData();
            }
        }
    }

    @SuppressWarnings("unused")
    public class ScrollLinearLayoutManger extends GridLayoutManager {

        public ScrollLinearLayoutManger(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public ScrollLinearLayoutManger(Context context, int spanCount) {
            super(context, spanCount);
        }

        public ScrollLinearLayoutManger(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class CenterSmoothScroller extends LinearSmoothScroller {

            CenterSmoothScroller(Context context) {
                super(context);
            }

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ScrollLinearLayoutManger.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }

            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 0.2f;
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        }

    }
}
