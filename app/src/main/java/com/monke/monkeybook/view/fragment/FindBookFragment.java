package com.monke.monkeybook.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseFragment;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.presenter.FindBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.view.activity.ChoiceBookActivity;
import com.monke.monkeybook.view.adapter.FindKindAdapter;
import com.monke.monkeybook.widget.refreshview.expandablerecyclerview.bean.RecyclerViewData;
import com.monke.monkeybook.widget.refreshview.expandablerecyclerview.listener.OnRecyclerViewListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FindBookFragment extends MBaseFragment<FindBookContract.Presenter> implements FindBookContract.View, OnRecyclerViewListener.OnItemClickListener {
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.expandable_list)
    RecyclerView expandableList;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    Unbinder unbinder;

    private FindKindAdapter adapter;
    private int lastExpandedPosition = 0;

    @Override
    public int createLayoutId() {
        return R.layout.fragment_book_find;
    }

    @Override
    protected void bindView() {
        super.bindView();
        ButterKnife.bind(this, view);
        initExpandableList();
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.initData();
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    protected void initData() {
        mPresenter.initData();
    }

    @Override
    protected FindBookContract.Presenter initInjector() {
        return new FindBookPresenterImpl();
    }

    @Override
    public synchronized void updateUI(List<RecyclerViewData> group) {
        if (group.size() > 0) {
            adapter.setAllDatas(group);
            lastExpandedPosition = Math.min(lastExpandedPosition, group.size() - 1);
            if (autoExpandGroup()) {
                adapter.expandGroup(lastExpandedPosition);
            }
        } else {
            adapter.clearAll();
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
        expandableList.setAdapter(adapter);
        adapter.setCanExpandAll(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
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
}
