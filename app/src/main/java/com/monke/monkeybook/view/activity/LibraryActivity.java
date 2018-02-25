//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.LibraryBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.LibraryPresenterImpl;
import com.monke.monkeybook.presenter.impl.ILibraryPresenter;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.view.impl.ILibraryView;
import com.monke.monkeybook.widget.libraryview.LibraryKindBookListView;
import com.monke.monkeybook.widget.libraryview.LibraryNewBooksView;
import com.monke.monkeybook.widget.refreshview.RefreshProgressBar;
import com.monke.monkeybook.widget.refreshview.RefreshScrollView;

import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LibraryActivity extends MBaseActivity<ILibraryPresenter> implements ILibraryView {

    private Animation animIn;
    private Animation animOut;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.kind_ll)
    LinearLayout kindLl;
    @BindView(R.id.lkbv_kindbooklist)
    LibraryKindBookListView lkbvKindbooklist;
    @BindView(R.id.lav_hotauthor)
    LibraryNewBooksView lavHotauthor;
    @BindView(R.id.rscv_content)
    RefreshScrollView rscvContent;
    @BindView(R.id.rpb_progress)
    RefreshProgressBar rpbProgress;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    @Override
    protected ILibraryPresenter initInjector() {
        return new LibraryPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_library);
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

        rscvContent.setRpb(rpbProgress);

        initKind();

    }

    private void initKind() {
        int columnCount = 4;
        Iterator iterator = mPresenter.getKinds().entrySet().iterator();
        int temp = 0;
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = null;
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLp.weight = 1;
        while (iterator.hasNext()) {
            final Map.Entry<String, String> resultTemp = (Map.Entry<String, String>) iterator.next();
            if (temp % columnCount == 0) {
                linearLayout = new LinearLayout(this);
                linearLayout.setLayoutParams(l);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                kindLl.addView(linearLayout);
            }
            TextView textView = new TextView(this);
            textView.setLayoutParams(tvLp);
            textView.setText(resultTemp.getKey());
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(14);
            textView.setPadding(0, DensityUtil.dp2px(this, 5), 0, DensityUtil.dp2px(this, 5));
            textView.setLines(1);
            textView.setTextColor(getResources().getColorStateList(R.color.selector_kind_tv_color));
            textView.setOnClickListener(v -> ChoiceBookActivity.startChoiceBookActivity(LibraryActivity.this, resultTemp.getKey(), resultTemp.getValue()));
            assert linearLayout != null;
            linearLayout.addView(textView);
            temp++;
        }
        int viewCount = mPresenter.getKinds().size() % columnCount == 0 ? 0 : (columnCount - mPresenter.getKinds().size() % columnCount);
        for (int i = 0; i < viewCount; i++) {
            View v = new View(this);
            v.setLayoutParams(tvLp);
            assert linearLayout != null;
            linearLayout.addView(v);
        }
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_library);
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
                startActivityByAnim(new Intent(this, SearchActivity.class),
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
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rscvContent.startRefresh();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LibraryActivity.super.finish();
                overridePendingTransition(0, 0);
                isExiting = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rscvContent.setBaseRefreshListener(() -> mPresenter.getLibraryData());
    }

    private Boolean isExiting = false;

    @Override
    public void finish() {
        if (!isExiting) {
            isExiting = true;
            llContent.startAnimation(animOut);
        }
    }

    @Override
    public void updateUI(final LibraryBean library) {
        //获取数据后刷新UI
        lavHotauthor.updateData(library.getLibraryNewBooks(), libraryNewBookBean -> {
            SearchBookBean searchBookBean = new SearchBookBean();
            searchBookBean.setName(libraryNewBookBean.getName());
            searchBookBean.setNoteUrl(libraryNewBookBean.getUrl());
            searchBookBean.setTag(libraryNewBookBean.getTag());
            searchBookBean.setOrigin(libraryNewBookBean.getOrgin());
            Intent intent = new Intent(LibraryActivity.this, BookDetailActivity.class);
            intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH);
            intent.putExtra("data", searchBookBean);
            startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
        });
        lkbvKindbooklist.updateData(library.getKindBooks(), new LibraryKindBookListView.OnItemListener() {
            @Override
            public void onClickMore(String title, String url) {
                ChoiceBookActivity.startChoiceBookActivity(LibraryActivity.this, title, url);
            }

            @Override
            public void onClickBook(ImageView animView, SearchBookBean searchBookBean) {
                Intent intent = new Intent(LibraryActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH);
                intent.putExtra("data", searchBookBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void finishRefresh() {
        rscvContent.finishRefresh();
    }

}