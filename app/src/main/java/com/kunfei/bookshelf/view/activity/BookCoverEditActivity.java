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

import com.bumptech.glide.Glide;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.dao.SearchBookBeanDao;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerView;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookCoverEditActivity extends MBaseActivity {

    @BindView(R.id.rf_rv_change_cover)
    RefreshRecyclerView changeCover;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

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
            actionBar.setTitle("封面换源");
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
        String name = getIntent().getStringExtra("name");
        String author = getIntent().getStringExtra("author");
        List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                .where(SearchBookBeanDao.Properties.Name.eq(name), SearchBookBeanDao.Properties.Author.eq(author), SearchBookBeanDao.Properties.CoverUrl.isNotNull())
                .build().list();
        List<String> urls = new ArrayList<>();
        List<String> origins = new ArrayList<>();
        for (SearchBookBean searchBook : searchBookBeans){
            String url = searchBook.getCoverUrl();
            if(url!= null && !urls.contains(url)){
                urls.add(url);
                origins.add(searchBook.getOrigin());
            }
        }
        ChangeCoverAdapter changeCoverAdapter = new ChangeCoverAdapter(urls, origins);
        changeCover.setRefreshRecyclerViewAdapter(changeCoverAdapter, new GridLayoutManager(this,3));
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    public class ChangeCoverAdapter extends RefreshRecyclerViewAdapter {
        private List<String> urls;
        private List<String> origins;

        public ChangeCoverAdapter(List<String> urls,List<String> origins) {
            super(false);
            this.urls = urls;
            this.origins = origins;
        }

        @Override
        public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_cover, parent, false));
        }

        @Override
        public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.bind(urls.get(position),origins.get(position), holder);
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

            public void bind(String url,String origin, RecyclerView.ViewHolder holder) {
                tvSourceName.setText(origin);
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.img_cover_default)
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
