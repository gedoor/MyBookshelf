//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.databinding.ActivityBookChoiceBinding;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.ChoiceBookPresenter;
import com.kunfei.bookshelf.presenter.contract.ChoiceBookContract;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.ChoiceBookAdapter;
import com.kunfei.bookshelf.widget.recycler.refresh.OnLoadMoreListener;

import java.util.List;

public class ChoiceBookActivity extends MBaseActivity<ChoiceBookContract.Presenter> implements ChoiceBookContract.View {

    private ActivityBookChoiceBinding binding;
    private ChoiceBookAdapter searchBookAdapter;
    private View viewRefreshError;

    @Override
    protected ChoiceBookContract.Presenter initInjector() {
        return new ChoiceBookPresenter(getIntent());
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityBookChoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();

        binding.rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));

        viewRefreshError = LayoutInflater.from(this).inflate(R.layout.view_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            searchBookAdapter.replaceAll(null);
            //刷新失败 ，重试
            mPresenter.initPage();
            mPresenter.toSearchBooks(null);
            startRefreshAnim();
        });
        binding.rfRvSearchBooks.setNoDataAndRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_refresh_no_data, null),
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
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void bindEvent() {
        searchBookAdapter.setCallback((animView, position, searchBookBean) -> {
            String dataKey = String.valueOf(System.currentTimeMillis());
            Intent intent = new Intent(ChoiceBookActivity.this, BookDetailActivity.class);
            intent.putExtra("openFrom", BookDetailPresenter.FROM_SEARCH);
            intent.putExtra("data_key", dataKey);
            BitIntentDataManager.getInstance().putData(dataKey, searchBookBean);
            startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
        });

        binding.rfRvSearchBooks.setBaseRefreshListener(() -> {
            mPresenter.initPage();
            mPresenter.toSearchBooks(null);
            startRefreshAnim();
        });
        binding.rfRvSearchBooks.setLoadMoreListener(new OnLoadMoreListener() {
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
        binding.rfRvSearchBooks.finishRefresh(isAll, true);
    }

    @Override
    public void loadMoreFinish(Boolean isAll) {
        binding.rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        if (books.size() <= 0) {
            loadMoreFinish(true);
            return;
        }
        searchBookAdapter.addAll(books);
        loadMoreFinish(false);
    }

    @Override
    public void searchBookError(String msg) {
        if (mPresenter.getPage() > 1) {
            binding.rfRvSearchBooks.finishLoadMore(true, true);
        } else {
            //刷新失败
            binding.rfRvSearchBooks.refreshError();
            if (msg != null) {
                ((TextView) viewRefreshError.findViewById(R.id.tv_error_msg)).setText(msg);
            } else {
                ((TextView) viewRefreshError.findViewById(R.id.tv_error_msg)).setText(R.string.get_data_error);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void addBookShelfFailed(String massage) {
        toast(massage, ERROR);
    }

    @Override
    public void startRefreshAnim() {
        binding.rfRvSearchBooks.startRefresh();
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
    }

}