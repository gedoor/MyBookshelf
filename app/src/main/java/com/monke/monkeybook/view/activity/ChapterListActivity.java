package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.view.adapter.ChapterListAdapter;
import com.monke.monkeybook.widget.AppCompat;
import com.monke.monkeybook.widget.refreshview.scroller.FastScrollRecyclerView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChapterListActivity extends MBaseActivity {

    @BindView(R.id.rv_list)
    FastScrollRecyclerView rvList;
    @BindView(R.id.toolbar_tab)
    TabLayout toolbarTab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_current_chapter_info)
    TextView tvChapterInfo;
    @BindView(R.id.iv_chapter_sort)
    ImageView ivChapterSort;
    @BindView(R.id.ll_chapter_base_info)
    View llBaseInfo;
    @BindView(R.id.v_shadow)
    View vShadow;

    SearchView searchView;

    private ChapterListAdapter chapterListAdapter;

    private LinearLayoutManager layoutManager;

    private BookShelfBean bookShelf;

    private boolean isChapterReverse;

    public static void startThis(MBaseActivity activity, BookShelfBean bookShelf, int requestCode) {
        Intent intent = new Intent(activity, ChapterListActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        try {
            BitIntentDataManager.getInstance().putData(key, bookShelf.clone());
        } catch (CloneNotSupportedException e) {
            BitIntentDataManager.getInstance().putData(key, bookShelf);
            e.printStackTrace();
        }
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bookShelf != null) {
            String key = String.valueOf(System.currentTimeMillis());
            getIntent().putExtra("data_key", key);
            BitIntentDataManager.getInstance().putData(key, bookShelf);
        }
    }

    @Override
    protected void onDestroy() {
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_chapterlist);
    }

    @Override
    protected void initData() {
        String key = getIntent().getStringExtra("data_key");
        bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
        BitIntentDataManager.getInstance().cleanData(key);

        isChapterReverse = preferences.getBoolean("isChapterReverse", false);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setupActionBar();
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.menu_color_default));
        rvList.setLayoutManager(layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, isChapterReverse));
        rvList.setItemAnimator(null);
        setData(bookShelf);
    }

    @Override
    protected void bindEvent() {
        toolbarTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showChapterInfo(tab.getPosition() == 0);
                updateWhenTabChanged(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                updateWhenTabChanged(tab.getPosition());
            }
        });

        tvChapterInfo.setOnClickListener(view -> layoutManager.scrollToPositionWithOffset(bookShelf.getDurChapter(), 0));

        ivChapterSort.setOnClickListener(v -> {
            if (chapterListAdapter != null) {
                isChapterReverse = !isChapterReverse;
                preferences.edit().putBoolean("isChapterReverse", isChapterReverse).apply();
                layoutManager.setReverseLayout(isChapterReverse);
                layoutManager.scrollToPositionWithOffset(bookShelf.getDurChapter(), 0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_view, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        AppCompat.useCustomIconForSearchView(searchView, getResources().getString(R.string.search));
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.onActionViewCollapsed();
        searchView.setOnCloseListener(() -> {
            toolbarTab.setVisibility(VISIBLE);
            return false;
        });
        searchView.setOnSearchClickListener(view -> toolbarTab.setVisibility(GONE));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chapterListAdapter.search(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (toolbarTab.getVisibility() != VISIBLE) {
            searchViewCollapsed();
            return;
        }
        finish();
    }

    //设置ToolBar
    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setData(BookShelfBean bookShelfBean) {
        this.bookShelf = bookShelfBean;
        chapterListAdapter = new ChapterListAdapter(bookShelfBean, new ChapterListAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int index, int page, int tabPosition) {
                searchViewCollapsed();
                if (index != bookShelf.getDurChapter()) {
                    Intent data = new Intent();
                    data.putExtra("what", 0);
                    data.putExtra("chapter", index);
                    data.putExtra("page", page);
                    setResult(RESULT_OK, data);
                }
                finish();
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean, int tabPosition) {
                searchViewCollapsed();
                Intent data = new Intent();
                data.putExtra("what", tabPosition);
                data.putExtra("bookmark", bookmarkBean);
                setResult(RESULT_OK, data);
                finish();
            }
        });
        rvList.setAdapter(chapterListAdapter);
        updateIndex(bookShelf.getDurChapter());
        updateChapterInfo();
    }

    private void updateIndex(int durChapter) {
        if (toolbarTab.getSelectedTabPosition() == 0) {
            chapterListAdapter.setIndex(durChapter);
        } else {
            chapterListAdapter.notifyDataSetChanged();
        }

        layoutManager.scrollToPositionWithOffset(durChapter, 0);
    }

    private void updateChapterInfo() {
        if (bookShelf != null) {
            if (chapterListAdapter.getItemCount() == 0) {
                tvChapterInfo.setText(bookShelf.getDurChapterName());
            } else {
                tvChapterInfo.setText(String.format(Locale.getDefault(), "%s (%d/%d章)", bookShelf.getDurChapterName(), bookShelf.getDurChapter() + 1, bookShelf.getChapterListSize()));
            }
        }
    }

    private void showChapterInfo(boolean show) {
        llBaseInfo.setVisibility(show ? View.VISIBLE : View.GONE);
        vShadow.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateWhenTabChanged(int tabPos) {
        if (chapterListAdapter != null) {
            chapterListAdapter.tabChange(tabPos);
            if (tabPos == 0) {
                layoutManager.setReverseLayout(isChapterReverse);
                updateIndex(bookShelf.getDurChapter());
            } else {
                layoutManager.setReverseLayout(false);
            }
        }
    }

    private void searchViewCollapsed() {
        searchView.onActionViewCollapsed();
        toolbarTab.setVisibility(VISIBLE);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHAPTER_CHANGE)})
    public void chapterChange(BookContentBean bookContentBean) {
        if (bookShelf != null && bookShelf.getNoteUrl().equals(bookContentBean.getNoteUrl())) {
            chapterListAdapter.upChapter(bookContentBean.getDurChapterIndex());
        }
    }
}
