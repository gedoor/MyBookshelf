//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.presenter.FindBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IFindBookPresenter;
import com.monke.monkeybook.view.adapter.FindKindAdapter;
import com.monke.monkeybook.view.impl.IFindBookView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FindBookActivity extends MBaseActivity<IFindBookPresenter> implements IFindBookView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.expandable_list)
    ExpandableListView expandableList;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    private Animation animIn;
    private Animation animOut;
    private FindKindAdapter adapter;

    @Override
    protected IFindBookPresenter initInjector() {
        return new FindBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_expandable_list_vew);
    }

    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
    }

    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupActionBar();

        initExpandableList();

        mPresenter.initData();
    }

    private void initExpandableList() {
        adapter = new FindKindAdapter(this);
        expandableList.setAdapter(adapter);
        tvEmpty.setText(R.string.find_empty);
        expandableList.setEmptyView(tvEmpty);
        adapter.setOnGroupExpandedListener(this::expandOnlyOne);
        //  设置分组项的点击监听事件
        expandableList.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            // 请务必返回 false，否则分组不会展开
            return false;
        });

        //  设置子选项点击监听事件
        expandableList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            FindKindBean kindBean = adapter.getDataList().get(groupPosition).getChildren().get(childPosition);
            ChoiceBookActivity.startChoiceBookActivity(this, kindBean.getKindName(), kindBean.getKindUrl(), kindBean.getTag());
            return true;
        });

    }

    // 每次展开一个分组后，关闭其他的分组
    private boolean expandOnlyOne(int expandedPosition) {
        boolean result = true;
        int groupLength = expandableList.getExpandableListAdapter().getGroupCount();
        for (int i = 0; i < groupLength; i++) {
            if (i != expandedPosition && expandableList.isGroupExpanded(i)) {
                result &= expandableList.collapseGroup(i);
            }
        }
        return result;
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.find_on_www);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_library_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                //点击搜索
                startActivityByAnim(new Intent(this, SearchBookActivity.class),
                        toolbar, "to_search", android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void bindEvent() {

    }

    @Override
    public void updateUI(List<FindKindGroupBean> group) {
        if (group.size() > 0) {
            adapter.resetDataS(group);
            if (preferences.getBoolean(getString(R.string.pk_find_expand_group), true)) {
                expandableList.expandGroup(0);
            }
        }
    }

}