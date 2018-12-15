package com.kunfei.bookshelf.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kunfei.bookshelf.BitIntentDataManager;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.MyItemTouchHelpCallback;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.BookListPresenter;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.BookListContract;
import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.view.activity.BookDetailActivity;
import com.kunfei.bookshelf.view.activity.ReadBookActivity;
import com.kunfei.bookshelf.view.adapter.BookShelfAdapter;
import com.kunfei.bookshelf.view.adapter.BookShelfGridAdapter;
import com.kunfei.bookshelf.view.adapter.BookShelfListAdapter;
import com.kunfei.bookshelf.view.adapter.base.OnItemClickListenerTwo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BookListFragment extends MBaseFragment<BookListContract.Presenter> implements BookListContract.View {

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.local_book_rv_content)
    RecyclerView rvBookshelf;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.rl_empty_view)
    RelativeLayout rlEmptyView;

    private Unbinder unbinder;
    private String bookPx;
    private boolean resumed = false;
    private boolean isRecreate;
    private int group;

    private BookShelfAdapter bookShelfAdapter;

    private CallBackValue callBackValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            resumed = savedInstanceState.getBoolean("resumed");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public int createLayoutId() {
        return R.layout.fragment_book_list;
    }

    @Override
    protected BookListContract.Presenter initInjector() {
        return new BookListPresenter();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callBackValue = (CallBackValue) getActivity();
    }

    @Override
    protected void initData() {
        bookPx = preferences.getString(getString(R.string.pk_bookshelf_px), "0");
        isRecreate = callBackValue != null && callBackValue.isRecreate();
    }

    @Override
    protected void bindView() {
        super.bindView();
        unbinder = ButterKnife.bind(this, view);
        if (preferences.getBoolean("bookshelfIsList", true)) {
            rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));
            bookShelfAdapter = new BookShelfListAdapter(getActivity());
        } else {
            rvBookshelf.setLayoutManager(new GridLayoutManager(getContext(), 3));
            bookShelfAdapter = new BookShelfGridAdapter(getActivity());
        }
        rvBookshelf.setAdapter((RecyclerView.Adapter) bookShelfAdapter);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
    }

    @Override
    protected void firstRequest() {
        group = preferences.getInt("bookshelfGroup", 0);
        if (preferences.getBoolean(getString(R.string.pk_auto_refresh), false)
                && !isRecreate && NetworkUtil.isNetWorkAvailable() && group != 2) {
            mPresenter.queryBookShelf(true, group);
        } else {
            mPresenter.queryBookShelf(false, group);
        }
    }

    @Override
    protected void bindEvent() {
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.queryBookShelf(NetworkUtil.isNetWorkAvailable(), group);
            if (!NetworkUtil.isNetWorkAvailable()) {
                Toast.makeText(getContext(), "无网络，请打开网络后再试。", Toast.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        });
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        itemTouchHelpCallback.setSwipeRefreshLayout(refreshLayout);
        if (bookPx.equals("2")) {
            itemTouchHelpCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        } else {
            itemTouchHelpCallback.setDragEnable(false);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        }
        bookShelfAdapter.setItemClickListener(getAdapterListener());
        itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfAdapter.getItemTouchCallbackListener());
    }

    private OnItemClickListenerTwo getAdapterListener() {
        return new OnItemClickListenerTwo() {
            @Override
            public void onClick(View view, int index) {
                BookShelfBean bookShelfBean = bookShelfAdapter.getBooks().get(index);
                Intent intent = new Intent(getActivity(), ReadBookActivity.class);
                intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                try {
                    BitIntentDataManager.getInstance().putData(key, bookShelfBean.clone());
                } catch (CloneNotSupportedException e) {
                    BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                    e.printStackTrace();
                }
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onLongClick(View view, int index) {
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("openFrom", BookDetailPresenter.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, bookShelfAdapter.getBooks().get(index));

                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resumed) {
            resumed = false;
            stopBookShelfRefreshAnim();
        }
    }

    @Override
    public void onPause() {
        resumed = true;
        super.onPause();
    }

    private void stopBookShelfRefreshAnim() {
        if (bookShelfAdapter.getBooks() != null && bookShelfAdapter.getBooks().size() > 0) {
            for (BookShelfBean bookShelfBean : bookShelfAdapter.getBooks()) {
                if (bookShelfBean.isLoading()) {
                    bookShelfBean.setLoading(false);
                    refreshBook(bookShelfBean.getNoteUrl());
                }
            }
        }
    }

    @Override
    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList) {
        bookShelfAdapter.replaceAll(bookShelfBeanList, bookPx);
        if (rlEmptyView == null) return;
        if (bookShelfBeanList.size() > 0) {
            rlEmptyView.setVisibility(View.GONE);
        } else {
            tvEmpty.setText(R.string.bookshelf_empty);
            rlEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void refreshBook(String noteUrl) {
        bookShelfAdapter.refreshBook(noteUrl);
    }

    @Override
    public void updateGroup(Integer group) {
        this.group = group;
    }

    @Override
    public void refreshError(String error) {

    }

    @Override
    public SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface CallBackValue {
        boolean isRecreate();

        int getGroup();
    }

}
