//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.SearchBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.ISearchBookPresenter;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;
import com.monke.monkeybook.view.adapter.SearchHistoryAdapter;
import com.monke.monkeybook.view.impl.ISearchBookView;
import com.monke.monkeybook.widget.flowlayout.TagFlowLayout;
import com.monke.monkeybook.widget.refreshview.OnLoadMoreListener;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tyrantgit.explosionfield.ExplosionField;

public class SearchBookActivity extends MBaseActivity<ISearchBookPresenter> implements ISearchBookView {
    public final static int BookSource = 1;

    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_search_history)
    LinearLayout llSearchHistory;
    @BindView(R.id.tv_search_history_clean)
    TextView tvSearchHistoryClean;
    @BindView(R.id.tfl_search_history)
    TagFlowLayout tflSearchHistory;
    @BindView(R.id.rfRv_search_books)
    RefreshRecyclerView rfRvSearchBooks;

    private SearchHistoryAdapter searchHistoryAdapter;
    private ExplosionField explosionField;

    private SearchBookAdapter searchBookAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean showHishtory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected ISearchBookPresenter initInjector() {
        return new SearchBookPresenterImpl(this);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_search_book);

    }

    @Override
    protected void initData() {
        explosionField = ExplosionField.attach2Window(this);
        searchHistoryAdapter = new SearchHistoryAdapter();
        searchBookAdapter = new SearchBookAdapter(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initSearchView();
        llSearchHistory.setOnClickListener(null);

        tflSearchHistory.setAdapter(searchHistoryAdapter);

        rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));

        View viewRefreshError = LayoutInflater.from(this).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            mPresenter.initPage();
            mPresenter.toSearchBooks(null, true);
            rfRvSearchBooks.startRefresh();
        });
        rfRvSearchBooks.setNoDataAndrRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);

        searchBookAdapter.setItemClickListener(new SearchBookAdapter.OnItemClickListener() {
            @Override
            public void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean) {
                mPresenter.addBookToShelf(searchBookBean);
            }

            @Override
            public void clickItem(View animView, int position, SearchBookBean searchBookBean) {
                Intent intent = new Intent(SearchBookActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH);
                intent.putExtra("data", searchBookBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.action_search);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_book_source_manage:
                startActivityForResult(new Intent(this, BookSourceActivity.class), BookSource);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSearchView() {
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchView.setQueryHint("搜索书名、作者");
        searchView.onActionViewExpanded();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                toSearch();
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mPresenter.querySearchHistory(newText);
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((view, b) -> {
            showHishtory = b;
            if (!b && searchView.getQuery().toString().trim().equals("")) {
                finish();
            }
            openOrCloseHistory(showHishtory);
        });
    }

    @Override
    protected void bindEvent() {
        tvSearchHistoryClean.setOnClickListener(v -> {
            for (int i = 0; i < tflSearchHistory.getChildCount(); i++) {
                explosionField.explode(tflSearchHistory.getChildAt(i));
            }
            mPresenter.cleanSearchHistory();
        });

        searchHistoryAdapter.setOnItemClickListener(searchHistoryBean -> {
            searchView.setQuery(searchHistoryBean.getContent(), true);
            searchView.clearFocus();
        });

        rfRvSearchBooks.setLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadMore() {
                mPresenter.toSearchBooks(null, false);
            }

            @Override
            public void loadMoreErrorTryAgain() {
                mPresenter.toSearchBooks(null, true);
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        Intent intent = this.getIntent();
        String searchKey = intent.getStringExtra("searchKey");
        if (!TextUtils.isEmpty(searchKey)) {
            mSearchAutoComplete.setText(searchKey);
            searchView.clearFocus();
            toSearch();
            showHishtory = false;
        } else {
            showHishtory = true;
            mPresenter.querySearchHistory("");
        }
        openOrCloseHistory(showHishtory);
    }

    @Override
    public void onPause() {
        super.onPause();
        showHishtory = llSearchHistory.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        openOrCloseHistory(showHishtory);
    }

    /**
     * 开始搜索
     */
    private void toSearch() {
        if (searchView.getQuery().toString().trim().length() > 0) {
            final String key = searchView.getQuery().toString().trim();
            mPresenter.setHasSearch(true);
            mPresenter.insertSearchHistory();
            //执行搜索请求
            new Handler().postDelayed(() -> {
                mPresenter.initPage();
                mPresenter.toSearchBooks(key, false);
                rfRvSearchBooks.startRefresh();
            }, 300);
        }
    }

    private void openOrCloseHistory(Boolean open) {
        if (open) {
            if (llSearchHistory.getVisibility() != View.VISIBLE) {
                llSearchHistory.setVisibility(View.VISIBLE);
            }
        } else {
            if (llSearchHistory.getVisibility() == View.VISIBLE) {
                llSearchHistory.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean) {
        //搜索历史插入或者修改成功
        mPresenter.querySearchHistory(searchView.getQuery().toString().trim());
    }

    @Override
    public void querySearchHistorySuccess(List<SearchHistoryBean> datas) {
        searchHistoryAdapter.replaceAll(datas);
        if (searchHistoryAdapter.getDataSize() > 0) {
            tvSearchHistoryClean.setVisibility(View.VISIBLE);
        } else {
            tvSearchHistoryClean.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void refreshSearchBook() {
        searchBookAdapter.clearAll();
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
    public void searchBookError(Boolean isRefresh) {
        if (isRefresh) {
            rfRvSearchBooks.refreshError();
        } else {
            rfRvSearchBooks.loadMoreError();
        }
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        searchBookAdapter.addAll(books, mSearchAutoComplete.getText().toString().trim());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        explosionField.clear();
    }

    @Override
    public EditText getEdtContent() {
        return mSearchAutoComplete;
    }

    @Override
    public void addBookShelfFailed(String massage) {
        Toast.makeText(this, massage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public SearchBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    public void updateSearchItem(int index) {
        if (index < searchBookAdapter.getItemcount()) {
            int startIndex = ((LinearLayoutManager) rfRvSearchBooks.getRecyclerView().getLayoutManager()).findFirstVisibleItemPosition();
            try {
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
    public Boolean checkIsExist(SearchBookBean searchBookBean) {
        Boolean result = false;
        for (int i = 0; i < searchBookAdapter.getItemcount(); i++) {
            if (searchBookAdapter.getSearchBooks().get(i).getNoteUrl().equals(searchBookBean.getNoteUrl()) && searchBookAdapter.getSearchBooks().get(i).getTag().equals(searchBookBean.getTag())) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BookSource) {
            mPresenter.upSearchEngineS();
        }
    }
}
