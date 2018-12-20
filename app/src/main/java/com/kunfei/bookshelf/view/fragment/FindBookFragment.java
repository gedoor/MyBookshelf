package com.kunfei.bookshelf.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.presenter.FindBookPresenter;
import com.kunfei.bookshelf.presenter.contract.FindBookContract;
import com.kunfei.bookshelf.view.adapter.FindLeftAdapter;
import com.kunfei.bookshelf.view.adapter.FindRightAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FindBookFragment extends MBaseFragment<FindBookContract.Presenter> implements FindBookContract.View {
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

    private Unbinder unbinder;
    private FindLeftAdapter findLeftAdapter;
    private FindRightAdapter findRightAdapter;

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
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.initData();
            refreshLayout.setRefreshing(false);
        });
        findLeftAdapter = new FindLeftAdapter(pos -> rvFindRight.scrollToPosition(pos));
        rvFindLeft.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFindLeft.setAdapter(findLeftAdapter);
        findRightAdapter = new FindRightAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvFindRight.setLayoutManager(layoutManager);
        rvFindRight.setAdapter(findRightAdapter);
        rvFindRight.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int index = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                findLeftAdapter.upShowIndex(index);
                rvFindLeft.scrollToPosition(index);
            }
        });
    }

    /**
     * 首次逻辑操作
     */
    @Override
    protected void firstRequest() {
        super.firstRequest();
        mPresenter.initData();
    }

    @Override
    public synchronized void updateUI(List<FindKindGroupBean> group) {
        findLeftAdapter.setDatas(group);
        findRightAdapter.setDatas(group);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
