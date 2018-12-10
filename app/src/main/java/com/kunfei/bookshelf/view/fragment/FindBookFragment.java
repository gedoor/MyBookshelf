package com.kunfei.bookshelf.view.fragment;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.FindBookPresenter;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;
import com.kunfei.bookshelf.view.adapter.FindKindAdapter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.FindBookPresenter;
import com.kunfei.bookshelf.presenter.contract.FindBookContract;
import com.kunfei.bookshelf.view.activity.ChoiceBookActivity;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;
import com.kunfei.bookshelf.view.adapter.FindKindAdapter;
import com.kunfei.bookshelf.widget.refreshview.expandablerecyclerview.bean.RecyclerViewData;
import com.kunfei.bookshelf.widget.refreshview.expandablerecyclerview.listener.OnRecyclerViewListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FindBookFragment extends MBaseFragment<FindBookContract.Presenter> implements FindBookContract.View, OnRecyclerViewListener.OnItemClickListener, OnRecyclerViewListener.OnItemLongClickListener {
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.expandable_list)
    RecyclerView expandableList;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.rl_empty_view)
    RelativeLayout rlEmptyView;

    private Unbinder unbinder;
    private FindKindAdapter adapter;
    private int lastExpandedPosition = 0;

    @Override
    public int createLayoutId() {
        return R.layout.fragment_book_find;
    }

    @Override
    protected FindBookContract.Presenter initInjector() {
        return new FindBookPresenter();
    }

    @Override
    protected void bindView() {
        super.bindView();
        unbinder = ButterKnife.bind(this, view);
        initExpandableList();
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.initData();
            refreshLayout.setRefreshing(false);
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
    public synchronized void updateUI(List<RecyclerViewData> group) {
        if (rlEmptyView == null) return;
        if (group.size() > 0) {
            adapter.setAllDatas(group);
            lastExpandedPosition = Math.min(lastExpandedPosition, group.size() - 1);
            if (autoExpandGroup()) {
                adapter.expandGroup(lastExpandedPosition);
            }
            rlEmptyView.setVisibility(View.GONE);
        } else {
            adapter.clearAll();
            tvEmpty.setText("没有发现，可以在书源里添加。");
            rlEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private boolean autoExpandGroup() {
        if (isAdded()) {
            return preferences.getBoolean(getString(R.string.pk_find_expand_group), false);
        } else {
            return false;
        }
    }

    private void initExpandableList() {
        expandableList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FindKindAdapter(getActivity(), new ArrayList<>());
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        expandableList.setAdapter(adapter);
        adapter.setCanExpandAll(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * position 当前在列表中的position
     */
    @Override
    public void onGroupItemClick(int position, int groupPosition, View view) {
        lastExpandedPosition = groupPosition;
    }

    @Override
    public void onChildItemClick(int position, int groupPosition, int childPosition, View view) {
        FindKindBean kindBean = (FindKindBean) adapter.getAllDatas().get(groupPosition).getChild(childPosition);

        Intent intent = new Intent(getContext(), ChoiceBookActivity.class);
        intent.putExtra("url", kindBean.getKindUrl());
        intent.putExtra("title", kindBean.getKindName());
        intent.putExtra("tag", kindBean.getTag());
        startActivityByAnim(intent, view, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onGroupItemLongClick(int position, int groupPosition, View view) {
        if (getActivity() == null) return;
        FindKindBean kindBean = (FindKindBean) adapter.getAllDatas().get(groupPosition).getChild(0);
        BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(kindBean.getTag());
        if (sourceBean != null) {
            SourceEditActivity.startThis(getActivity(), sourceBean);
        }
    }

    @Override
    public void onChildItemLongClick(int position, int groupPosition, int childPosition, View view) {

    }
}
