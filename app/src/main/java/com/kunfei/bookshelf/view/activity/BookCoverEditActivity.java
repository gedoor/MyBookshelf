package com.kunfei.bookshelf.view.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.dao.SearchBookBeanDao;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

public class BookCoverEditActivity extends MBaseActivity {

    @BindView(R.id.rf_rv_change_cover)
    RecyclerView changeCover;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private SearchBookModel searchBookModel;
    private String name;
    private String author;
    private Boolean isLoading = true;
    private List<String> urls = new ArrayList<>();
    private List<String> origins = new ArrayList<>();

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_book_cover_edit);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.cover_change_source);
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
    protected void initData() {
        name = getIntent().getStringExtra("name");
        author = getIntent().getStringExtra("author");
        ChangeCoverAdapter changeCoverAdapter = new ChangeCoverAdapter();
        changeCover.setLayoutManager(new GridLayoutManager(this, 3));
        changeCover.setAdapter(changeCoverAdapter);
        SearchBookModel.OnSearchListener searchListener = new SearchBookModel.OnSearchListener() {
            @Override
            public void refreshSearchBook() {
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void refreshFinish(Boolean value) {
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                if (value) {
                    isLoading = false;
                }
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                if (!value.isEmpty()) {
                    SearchBookBean bookBean = value.get(0);
                    if (bookBean.getName().equals(name)
                            && bookBean.getCoverUrl() != null
                            && !urls.contains(bookBean.getCoverUrl())) {
                        urls.add(bookBean.getCoverUrl());
                        origins.add(bookBean.getOrigin());
                        changeCoverAdapter.notifyItemChanged(urls.size() - 1);
                    }
                }
            }

            @Override
            public void searchBookError(Throwable throwable) {
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
        searchBookModel = new SearchBookModel(searchListener);
        swipeRefreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isLoading) {
                isLoading = true;
                long time = System.currentTimeMillis();
                searchBookModel.setSearchTime(time);
                searchBookModel.search(name, time, new ArrayList<>(), false);
            }
        });
        Single.create((SingleOnSubscribe<Boolean>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(name), SearchBookBeanDao.Properties.Author.eq(author), SearchBookBeanDao.Properties.CoverUrl.isNotNull())
                    .build().list();
            for (SearchBookBean searchBook : searchBookBeans) {
                BookSourceBean bean = BookSourceManager.getBookSourceByUrl(searchBook.getTag());
                if (bean != null) {
                    String url = searchBook.getCoverUrl();
                    if (url != null && !urls.contains(url)) {
                        urls.add(url);
                        origins.add(searchBook.getOrigin());
                    }
                }
            }
            e.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (urls.isEmpty()) {
                            swipeRefreshLayout.setRefreshing(true);
                            long time = System.currentTimeMillis();
                            searchBookModel.setSearchTime(time);
                            searchBookModel.search(name, time, new ArrayList<>(), false);
                        } else {
                            changeCoverAdapter.notifyDataSetChanged();
                            isLoading = false;
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchBookModel.onDestroy();
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    public class ChangeCoverAdapter extends RefreshRecyclerViewAdapter {

        ChangeCoverAdapter() {
            super(false);
        }

        @Override
        public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_cover, parent, false));
        }

        @Override
        public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.bind(urls.get(position), origins.get(position), holder);
        }

        @Override
        public int getIViewType(int position) {
            return 0;
        }

        @Override
        public int getICount() {
            return urls.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivCover;
            TextView tvSourceName;

            MyViewHolder(View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvSourceName = itemView.findViewById(R.id.tv_source_name);
            }

            public void bind(String url, String origin, RecyclerView.ViewHolder holder) {
                tvSourceName.setText(origin);
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .error(R.drawable.img_cover_default)
                        .into(ivCover);
                ivCover.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
        }
    }
}
