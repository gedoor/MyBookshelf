//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.presenter.BookDetailPresenter;
import com.monke.monkeybook.presenter.ChoiceBookPresenter;
import com.monke.monkeybook.presenter.contract.ChoiceBookContract;
import com.monke.monkeybook.view.adapter.ChoiceBookAdapter;
import com.monke.monkeybook.widget.refreshview.OnLoadMoreListener;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChoiceBookActivity extends MBaseActivity<ChoiceBookContract.Presenter> implements ChoiceBookContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rfRv_search_books)
    RefreshRecyclerView rfRvSearchBooks;

    private ChoiceBookAdapter searchBookAdapter;

    @Override
    protected ChoiceBookContract.Presenter initInjector() {
        return new ChoiceBookPresenter(getIntent());
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_choice);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void initData() {
        searchBookAdapter = new ChoiceBookAdapter(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();

        rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));

        View viewRefreshError = LayoutInflater.from(this).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            searchBookAdapter.replaceAll(null);
            //刷新失败 ，重试
            mPresenter.initPage();
            mPresenter.toSearchBooks(null);
            startRefreshAnim();
        });
        rfRvSearchBooks.setNoDataAndrRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mPresenter.getTitle());
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void bindEvent() {
        searchBookAdapter.setItemClickListener(new ChoiceBookAdapter.OnItemClickListener() {
            @Override
            public void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean) {
                SearchBookActivity.startByKey(ChoiceBookActivity.this, searchBookBean.getName());
            }

            @Override
            public void clickItem(View animView, int position, SearchBookBean searchBookBean) {
                Intent intent = new Intent(ChoiceBookActivity.this, BookDetailActivity.class);
                intent.putExtra("openFrom", BookDetailPresenter.FROM_SEARCH);
                intent.putExtra("data", searchBookBean);
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        rfRvSearchBooks.setBaseRefreshListener(() -> {
            mPresenter.initPage();
            mPresenter.toSearchBooks(null);
            startRefreshAnim();
        });
        rfRvSearchBooks.setLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadMore() {
                mPresenter.toSearchBooks(null);
            }

            @Override
            public void loadMoreErrorTryAgain() {
                mPresenter.toSearchBooks(null);
            }
        });
    }

    @Override
    public void refreshSearchBook(List<SearchBookBean> books) {
        searchBookAdapter.replaceAll(books);
    }

    @Override
    public void refreshFinish(Boolean isAll) {
        rfRvSearchBooks.finishRefresh(isAll, true);
    }

    @Override
    public void loadMoreFinish(Boolean isAll) {
        rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        if (books.size() <= 0) {
            loadMoreFinish(true);
            return;
        }
        for (SearchBookBean searchBook : searchBookAdapter.getSearchBooks()) {
            if (Objects.equals(books.get(0).getName(), searchBook.getName()) && Objects.equals(books.get(0).getAuthor(), searchBook.getAuthor())) {
                loadMoreFinish(true);
                return;
            }
        }
        searchBookAdapter.addAll(books);
        loadMoreFinish(false);
    }

    @Override
    public void searchBookError() {
        if (mPresenter.getPage() > 1) {
            rfRvSearchBooks.loadMoreError();
        } else {
            //刷新失败
            rfRvSearchBooks.refreshError();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void addBookShelfSuccess(List<SearchBookBean> datas) {
        searchBookAdapter.notifyDataSetChanged();
    }

    @Override
    public void addBookShelfFailed(String massage) {
        toast(massage, ERROR);
    }

    @Override
    public ChoiceBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    public void updateSearchItem(int index) {
        if (index < searchBookAdapter.getICount()) {
            try {
                int startIndex = ((LinearLayoutManager) Objects.requireNonNull(rfRvSearchBooks.getRecyclerView().getLayoutManager())).findFirstVisibleItemPosition();
                TextView tvAddShelf = rfRvSearchBooks.getRecyclerView().getChildAt(index - startIndex).findViewById(R.id.tv_add_shelf);
                if (tvAddShelf != null) {
                    if (searchBookAdapter.getSearchBooks().get(index).getIsAdd()) {
                        tvAddShelf.setText("已添加");
                        tvAddShelf.setEnabled(false);
                    } else {
                        tvAddShelf.setText("+添加");
                        tvAddShelf.setEnabled(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startRefreshAnim() {
        rfRvSearchBooks.startRefresh();
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
    }

}