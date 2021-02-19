//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.databinding.ActivitySearchBookBinding;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.SearchBookPresenter;
import com.kunfei.bookshelf.presenter.contract.SearchBookContract;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.Selector;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.SearchBookAdapter;
import com.kunfei.bookshelf.view.adapter.SearchBookshelfAdapter;
import com.kunfei.bookshelf.widget.explosion_field.ExplosionField;
import com.kunfei.bookshelf.widget.recycler.refresh.OnLoadMoreListener;

import java.util.List;
import java.util.Objects;

public class SearchBookActivity extends MBaseActivity<SearchBookContract.Presenter>
        implements SearchBookContract.View, SearchBookshelfAdapter.CallBack {
    private final int requestSource = 14;

    private ActivitySearchBookBinding binding;
    private View refreshErrorView;
    private ExplosionField mExplosionField;
    private SearchBookAdapter searchBookAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean showHistory;
    private String searchKey;
    private Menu menu;
    private SearchBookshelfAdapter searchBookshelfAdapter;

    public static void startByKey(Context context, String searchKey) {
        Intent intent = new Intent(context, SearchBookActivity.class);
        intent.putExtra("searchKey", searchKey);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected SearchBookContract.Presenter initInjector() {
        return new SearchBookPresenter();
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivitySearchBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        mExplosionField = ExplosionField.attach2Window(this);
        searchBookAdapter = new SearchBookAdapter(this);
        searchBookshelfAdapter = new SearchBookshelfAdapter(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        binding.cardSearch.setCardBackgroundColor(ThemeStore.primaryColorDark(this));
        initSearchView();
        setSupportActionBar(binding.toolbar);
        setupActionBar();
        binding.fabSearchStop.hide();
        binding.fabSearchStop.setBackgroundTintList(Selector.colorBuild()
                .setDefaultColor(ThemeStore.accentColor(this))
                .setPressedColor(ColorUtil.darkenColor(ThemeStore.accentColor(this)))
                .create());
        binding.llSearchHistory.setOnClickListener(null);
        binding.rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));
        refreshErrorView = LayoutInflater.from(this).inflate(R.layout.view_refresh_error, null);
        refreshErrorView.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            toSearch();
        });
        binding.rfRvSearchBooks.setNoDataAndRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_refresh_no_data, null),
                refreshErrorView);

        searchBookAdapter.setItemClickListener((view, position) -> {
            String dataKey = String.valueOf(System.currentTimeMillis());
            Intent intent = new Intent(SearchBookActivity.this, BookDetailActivity.class);
            intent.putExtra("openFrom", BookDetailPresenter.FROM_SEARCH);
            intent.putExtra("data_key", dataKey);
            BitIntentDataManager.getInstance().putData(dataKey, searchBookAdapter.getItemData(position));
            startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
        });

        binding.fabSearchStop.setOnClickListener(view -> {
            binding.fabSearchStop.hide();
            mPresenter.stopSearch();
        });
        binding.rvBookshelf.setLayoutManager(new FlexboxLayoutManager(this));
        binding.rvBookshelf.setAdapter(searchBookshelfAdapter);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_search);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_search_activity, menu);
        this.menu = menu;
        initMenu();
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_book_source_manage:
                BookSourceActivity.startThis(this, requestSource);
                break;
            case android.R.id.home:
                SoftInputUtil.hideIMM(getCurrentFocus());
                finish();
                break;
            default:
                if (item.getGroupId() == R.id.source_group) {
                    item.setChecked(true);
                    if (Objects.equals(getString(R.string.all_source), item.getTitle().toString())) {
                        MApplication.SEARCH_GROUP = null;
                    } else {
                        MApplication.SEARCH_GROUP = item.getTitle().toString();
                    }
                    mPresenter.initSearchEngineS(MApplication.SEARCH_GROUP);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSearchView() {
        mSearchAutoComplete = binding.searchView.findViewById(R.id.search_src_text);
        binding.searchView.setQueryHint(getString(R.string.search_book_key));
        //获取到TextView的控件
        mSearchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mSearchAutoComplete.setPadding(15, 0, 0, 0);
        binding.searchView.onActionViewExpanded();
        LinearLayout editFrame = binding.searchView.findViewById(R.id.search_edit_frame);
        ImageView closeButton = binding.searchView.findViewById(R.id.search_close_btn);
        ImageView goButton = binding.searchView.findViewById(R.id.search_go_btn);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editFrame.getLayoutParams();
        params.setMargins(20, 0, 10, 0);
        editFrame.setLayoutParams(params);
        closeButton.setScaleX(0.9f);
        closeButton.setScaleY(0.9f);
        closeButton.setPadding(0, 0, 0, 0);
        goButton.setPadding(0, 0, 0, 0);
        binding.searchView.setSubmitButtonEnabled(true);
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query))
                    return false;
                searchKey = query.trim();
                if (!searchKey.toLowerCase().startsWith("set:")) {
                    toSearch();
                    binding.searchView.clearFocus();
                } else {
                    parseSecretCode(searchKey);
                    finish();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    List<BookInfoBean> beans = BookshelfHelp.searchBookInfo(newText);
                    searchBookshelfAdapter.setItems(beans);
                    if (beans.size() > 0) {
                        binding.tvBookshelf.setVisibility(View.VISIBLE);
                        binding.rvBookshelf.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvBookshelf.setVisibility(View.GONE);
                        binding.rvBookshelf.setVisibility(View.GONE);
                    }
                } else {
                    binding.tvBookshelf.setVisibility(View.GONE);
                    binding.rvBookshelf.setVisibility(View.GONE);
                }
                if (!newText.toLowerCase().startsWith("set")) {
                    mPresenter.querySearchHistory(newText);
                } else {
                    showHideSetting();
                }
                return false;
            }
        });
        binding.searchView.setOnQueryTextFocusChangeListener((view, b) -> {
            showHistory = b;
            if (!b && binding.searchView.getQuery().toString().trim().equals("")) {
                finish();
            }
            if (showHistory) {
                binding.fabSearchStop.hide();
                mPresenter.stopSearch();
            }
            openOrCloseHistory(showHistory);
        });
    }

    @Override
    protected void bindEvent() {
        binding.tvSearchHistoryClean.setOnClickListener(v -> {
            mExplosionField.explode(binding.tflSearchHistory, true);
            mPresenter.cleanSearchHistory();
        });

        binding.rfRvSearchBooks.setLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadMore() {
                binding.fabSearchStop.show();
                mPresenter.toSearchBooks(null, false);
            }

            @Override
            public void loadMoreErrorTryAgain() {
                binding.fabSearchStop.show();
                mPresenter.toSearchBooks(null, true);
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        Intent intent = this.getIntent();
        searchBook(intent.getStringExtra("searchKey"));
    }

    @Override
    public void onPause() {
        super.onPause();
        showHistory = binding.llSearchHistory.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        openOrCloseHistory(showHistory);
    }

    @Override
    public void searchBook(String searchKey) {
        if (!TextUtils.isEmpty(searchKey)) {
            binding.searchView.setQuery(searchKey, true);
            showHistory = false;
        } else {
            showHistory = true;
            mPresenter.querySearchHistory("");
        }
        openOrCloseHistory(showHistory);
    }

    private void initMenu() {
        if (menu == null) return;
        menu.removeGroup(R.id.source_group);
        menu.add(R.id.source_group, Menu.NONE, Menu.NONE, R.string.all_source);
        List<String> groupList = BookSourceManager.getEnableGroupList();
        for (String groupName : groupList) {
            menu.add(R.id.source_group, Menu.NONE, Menu.NONE, groupName);
        }
        menu.setGroupCheckable(R.id.source_group, true, true);
        if (MApplication.SEARCH_GROUP != null) {
            boolean hasGroup = false;
            for (int i = 0; i < menu.size(); i++) {
                if (menu.getItem(i).getTitle().toString().equals(MApplication.SEARCH_GROUP)) {
                    menu.getItem(i).setChecked(true);
                    hasGroup = true;
                    break;
                }
            }
            if (!hasGroup) {
                menu.getItem(1).setChecked(true);
            }
        } else {
            menu.getItem(1).setChecked(true);
        }
    }

    private void showHideSetting() {
        binding.tflSearchHistory.removeAllViews();
        TextView tagView;
        String[] hideSettings = {"show_nav_shelves", "fade_tts", "use_regex_in_new_rule", "blur_sim_back", "async_draw", "disable_scroll_click_turn"};

        for (String text : hideSettings) {
            tagView = (TextView) getLayoutInflater().inflate(R.layout.item_search_history, binding.tflSearchHistory, false);
            tagView.setTag(text);
            tagView.setText(text);
            tagView.setOnClickListener(view -> {
                String key = "set:" + view.getTag();
                binding.searchView.setQuery(key, false);
            });
            binding.tflSearchHistory.addView(tagView);
        }
    }

    private void parseSecretCode(String code) {
        code = code.toLowerCase().replaceAll("^\\s*set:", "").trim();
        String[] param = code.split("\\s+");
        String msg = null;
        boolean enable = param.length == 1 || !param[1].equals("false");
        switch (param[0]) {
            case "show_nav_shelves":
                MApplication.getConfigPreferences().edit().putBoolean("showNavShelves", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用侧边栏书架！";
                RxBus.get().post(RxBusTag.RECREATE, true);
                break;
            case "fade_tts":
                MApplication.getConfigPreferences().edit().putBoolean("fadeTTS", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用朗读时淡入淡出！";
                break;
            case "use_regex_in_new_rule":
                MApplication.getConfigPreferences().edit().putBoolean("useRegexInNewRule", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用新建替换规则时默认使用正则表达式！";
                break;
            case "blur_sim_back":
                MApplication.getConfigPreferences().edit().putBoolean("blurSimBack", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用仿真翻页背景虚化！";
                break;
            case "async_draw":
                MApplication.getConfigPreferences().edit().putBoolean("asyncDraw", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用异步加载！";
                break;
            case "disable_scroll_click_turn":
                MApplication.getConfigPreferences().edit().putBoolean("disableScrollClickTurn", enable).apply();
                msg = "已" + (enable ? "禁" : "启") + "用滚动模式点击翻页！";
                break;
        }
        if (msg == null) {
            toast("无法识别设置密码: " + code, 0, -1);
        } else {
            toast(msg, 0, 1);
        }
    }

    /**
     * 开始搜索
     */
    private void toSearch() {
        if (!TextUtils.isEmpty(searchKey)) {
            mPresenter.insertSearchHistory();
            //执行搜索请求
            new Handler().postDelayed(() -> {
                mPresenter.initPage();
                binding.rfRvSearchBooks.startRefresh();
                binding.fabSearchStop.show();
                mPresenter.toSearchBooks(searchKey, false);
            }, 300);
        }
    }

    private void openOrCloseHistory(Boolean open) {
        if (open) {
            if (binding.llSearchHistory.getVisibility() != View.VISIBLE) {
                binding.llSearchHistory.setVisibility(View.VISIBLE);
            }
        } else {
            if (binding.llSearchHistory.getVisibility() == View.VISIBLE) {
                binding.llSearchHistory.setVisibility(View.GONE);
            }
        }
    }

    private void addNewHistories(List<SearchHistoryBean> historyBeans) {
        binding.tflSearchHistory.removeAllViews();
        if (historyBeans != null) {
            TextView tagView;
            for (SearchHistoryBean searchHistoryBean : historyBeans) {
                tagView = (TextView) getLayoutInflater().inflate(R.layout.item_search_history, binding.tflSearchHistory, false);
                tagView.setTag(searchHistoryBean);
                tagView.setText(searchHistoryBean.getContent());
                tagView.setOnClickListener(view -> {
                    SearchHistoryBean historyBean = (SearchHistoryBean) view.getTag();
                    List<BookInfoBean> beans = BookshelfHelp.searchBookInfo(historyBean.getContent());
                    binding.searchView.setQuery(historyBean.getContent(), beans.isEmpty());
                });
                tagView.setOnLongClickListener(view -> {
                    SearchHistoryBean historyBean = (SearchHistoryBean) view.getTag();
                    mExplosionField.explode(view);
                    view.setOnLongClickListener(null);
                    mPresenter.cleanSearchHistory(historyBean);
                    return true;
                });
                binding.tflSearchHistory.addView(tagView);
            }
        }
    }

    @Override
    public void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean) {
        //搜索历史插入或者修改成功
        mPresenter.querySearchHistory(searchKey);
    }

    @Override
    public void querySearchHistorySuccess(List<SearchHistoryBean> data) {
        addNewHistories(data);
        if (binding.tflSearchHistory.getChildCount() > 0) {
            binding.tvSearchHistoryClean.setVisibility(View.VISIBLE);
        } else {
            binding.tvSearchHistoryClean.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void refreshSearchBook() {
        searchBookAdapter.upData(SearchBookAdapter.DataAction.CLEAR, null);
    }

    @Override
    public void refreshFinish(Boolean isAll) {
        binding.fabSearchStop.hide();
        binding.rfRvSearchBooks.finishRefresh(isAll, true);
    }

    @Override
    public void loadMoreFinish(Boolean isAll) {
        binding.fabSearchStop.hide();
        binding.rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public void searchBookError(Throwable throwable) {
        if (searchBookAdapter.getICount() == 0) {
            ((TextView) refreshErrorView.findViewById(R.id.tv_error_msg)).setText(throwable.getMessage());
            binding.rfRvSearchBooks.refreshError();
        } else {
            binding.rfRvSearchBooks.loadMoreError();
        }
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        searchBookAdapter.addAll(books, mSearchAutoComplete.getText().toString().trim());
    }

    @Override
    protected void onDestroy() {
        mPresenter.stopSearch();
        mExplosionField.clear();
        super.onDestroy();
    }

    @Override
    public EditText getEdtContent() {
        return mSearchAutoComplete;
    }

    @Override
    public SearchBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == requestSource) {
                initMenu();
                mPresenter.initSearchEngineS(MApplication.SEARCH_GROUP);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public void openBookInfo(BookInfoBean bookInfoBean) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("noteUrl", bookInfoBean.getNoteUrl());
        startActivity(intent);
    }
}
