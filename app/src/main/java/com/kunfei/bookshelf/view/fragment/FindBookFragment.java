package com.kunfei.bookshelf.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.FindBookPresenter;
import com.kunfei.bookshelf.presenter.contract.FindBookContract;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.activity.ChoiceBookActivity;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;
import com.kunfei.bookshelf.view.adapter.FindKindAdapter;
import com.kunfei.bookshelf.view.adapter.FindLeftAdapter;
import com.kunfei.bookshelf.view.adapter.FindRightAdapter;
import com.kunfei.bookshelf.widget.recycler.expandable.OnRecyclerViewListener;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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
    private LinearLayoutManager rightLayoutManager;

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
        refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.initData();
            refreshLayout.setRefreshing(false);
        });
        leftLayoutManager = new LinearLayoutManager(getContext());
        rightLayoutManager = new LinearLayoutManager(getContext());
        initRecyclerView();
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
    public void upStyle() {
        initRecyclerView();
    }

    @Override
    public synchronized void updateUI(List<RecyclerViewData> group) {
        if (rlEmptyView == null) return;
        if (group.size() == 0) {
            tvEmpty.setText("没有发现，可以在书源里添加。");
            rlEmptyView.setVisibility(View.VISIBLE);
        } else {
            rlEmptyView.setVisibility(View.GONE);
        }
        if (isFlexBox()) {
            findLeftAdapter.setDatas(group);
            findRightAdapter.setDatas(group);
            rlEmptyView.setVisibility(View.GONE);
            rvFindLeft.setVisibility(View.VISIBLE);
            vwDivider.setVisibility(View.VISIBLE);
            if (group.size() <= 1) {
                rvFindLeft.setVisibility(View.GONE);
                vwDivider.setVisibility(View.GONE);
            }
        } else {
            findKindAdapter.setAllDatas(group);
        }
    }

    private boolean isFlexBox() {
        return preferences.getBoolean("findTypeIsFlexBox", true);
    }

    private void initRecyclerView() {
        if (isFlexBox()) {
            findKindAdapter = null;
            findLeftAdapter = new FindLeftAdapter(pos -> rightLayoutManager.scrollToPositionWithOffset(pos, 0));
            rvFindLeft.setLayoutManager(leftLayoutManager);
            rvFindLeft.setAdapter(findLeftAdapter);
            findRightAdapter = new FindRightAdapter(this);
            rvFindRight.setLayoutManager(rightLayoutManager);
            rvFindRight.setAdapter(findRightAdapter);
            rvFindRight.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int index = rightLayoutManager.findFirstVisibleItemPosition();
                    if (findLeftAdapter != null) {
                        findLeftAdapter.upShowIndex(index);
                        leftLayoutManager.scrollToPositionWithOffset(index, rvFindLeft.getHeight() / 2);
                    }
                }
            });
        } else {
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
            groupBean = (FindKindGroupBean) findRightAdapter.getDatas().get(groupPosition).getGroupData();
        } else {
            groupBean = (FindKindGroupBean) findKindAdapter.getAllDatas().get(groupPosition).getGroupData();
        }
        BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(groupBean.getGroupTag());
        if (sourceBean == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenu().add(R.string.edit);
        popupMenu.getMenu().add(R.string.to_top);
        popupMenu.getMenu().add(R.string.delete);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals(getString(R.string.edit))) {
                SourceEditActivity.startThis(getActivity(), sourceBean);
            } else if (item.getTitle().equals(getString(R.string.to_top))) {
                BookSourceManager.toTop(sourceBean);
            } else if (item.getTitle().equals(getString(R.string.delete))) {
                BookSourceManager.removeBookSource(sourceBean);
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

}
