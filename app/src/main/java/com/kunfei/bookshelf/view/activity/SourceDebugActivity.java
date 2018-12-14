package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.utils.RxUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SourceDebugActivity extends MBaseActivity {

    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.action_bar)
    AppBarLayout actionBar;
    @BindView(R.id.tv_content)
    TextView tvContent;

    private String sourceUrl;
    private CompositeDisposable compositeDisposable;

    public static void startThis(Context context, String sourceUrl) {
        if (TextUtils.isEmpty(sourceUrl)) return;
        Intent intent = new Intent(context, SourceDebugActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("sourceUrl", sourceUrl);
        context.startActivity(intent);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_source_debug);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        sourceUrl = getIntent().getStringExtra("sourceUrl");
        initSearchView();
    }

    private void initSearchView() {
        searchView.setQueryHint(getString(R.string.search_book_key));
        searchView.onActionViewExpanded();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query))
                    return false;
                startDebug(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void startDebug(String key) {
        if (TextUtils.isEmpty(sourceUrl)) return;
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
        WebBookModel.getInstance().searchOtherBook(key, 1, sourceUrl)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        tvContent.setText("搜索列表获取成功");
                        SearchBookBean searchBookBean = searchBookBeans.get(0);
                        tvContent.setText(String.format("%s\n书名:%s", tvContent.getText(), searchBookBean.getName()));
                        tvContent.setText(String.format("%s\n作者:%s", tvContent.getText(), searchBookBean.getAuthor()));
                        tvContent.setText(String.format("%s\n分类:%s", tvContent.getText(), searchBookBean.getKind()));
                        tvContent.setText(String.format("%s\n简介:%s", tvContent.getText(), searchBookBean.getOrigin()));
                        tvContent.setText(String.format("%s\n最新章节:%s", tvContent.getText(), searchBookBean.getLastChapter()));
                        tvContent.setText(String.format("%s\n书籍地址:%s", tvContent.getText(), searchBookBean.getNoteUrl()));
                        if (!TextUtils.isEmpty(searchBookBean.getNoteUrl())) {
                            bookInfoDebug(searchBookBean.getNoteUrl());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        tvContent.setText(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void bookInfoDebug(String noteUrl) {

    }
}
