package com.monke.monkeybook.view.fragment;

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
import android.widget.Toast;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseFragment;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.BookListPresenterImpl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.BookListContract;
import com.monke.monkeybook.view.activity.BookDetailActivity;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.view.adapter.BookShelfGridAdapter;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.OnItemClickListenerTwo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.monke.monkeybook.utils.NetworkUtil.isNetWorkAvailable;

public class BookListFragment extends MBaseFragment<BookListContract.Presenter> implements BookListContract.View {

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.local_book_rv_content)
    RecyclerView rvBookshelf;

    private boolean viewIsList;
    private String bookPx;
    private boolean resumed = false;
    private boolean isRecreate;
    private int group;

    private BookShelfGridAdapter bookShelfGridAdapter;
    private BookShelfListAdapter bookShelfListAdapter;

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
        return new BookListPresenterImpl();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callBackValue = (CallBackValue) getActivity();
    }

    @Override
    protected void bindView() {
        super.bindView();
        ButterKnife.bind(this, view);
        setUpAdapter();
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
    }

    @Override
    protected void initData() {
        viewIsList = preferences.getBoolean("bookshelfIsList", true);
        bookPx = preferences.getString(getString(R.string.pk_bookshelf_px), "0");
        isRecreate = callBackValue != null && callBackValue.isRecreate();
    }

    @Override
    protected void firstRequest() {
        group = preferences.getInt("bookshelfGroup", 0);
        if (preferences.getBoolean(getString(R.string.pk_auto_refresh), false)
                && !isRecreate && isNetWorkAvailable() && group != 2) {
            mPresenter.queryBookShelf(true, group);
        } else {
            mPresenter.queryBookShelf(false, group);
        }
    }

    @Override
    protected void bindEvent() {
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.queryBookShelf(isNetWorkAvailable(), group);
            if (!isNetWorkAvailable()) {
                Toast.makeText(getContext(), "无网络，请打开网络后再试。", Toast.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        });
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        if (bookPx.equals("2")) {
            itemTouchHelpCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        } else {
            itemTouchHelpCallback.setDragEnable(false);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        }
        if (viewIsList) {
            bookShelfListAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfListAdapter.getItemTouchCallbackListener());
        } else {
            bookShelfGridAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfGridAdapter.getItemTouchCallbackListener());
        }
    }


    private void setUpAdapter() {
        if (viewIsList) {
            bookShelfListAdapter = new BookShelfListAdapter(getActivity(), getNeedAnim());
            rvBookshelf.setAdapter(bookShelfListAdapter);
            rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            bookShelfGridAdapter = new BookShelfGridAdapter(getActivity(), getNeedAnim());
            rvBookshelf.setAdapter(bookShelfGridAdapter);
            rvBookshelf.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }
    }

    private boolean getNeedAnim() {
        return preferences.getBoolean(getString(R.string.pk_bookshelf_anim), false);
    }

    private List<BookShelfBean> getBookshelfList() {
        if (viewIsList) {
            return bookShelfListAdapter.getBooks();
        } else {
            return bookShelfGridAdapter.getBooks();
        }
    }

    private OnItemClickListenerTwo getAdapterListener() {
        return new OnItemClickListenerTwo() {
            @Override
            public void onClick(View view, int index) {
                BookShelfBean bookShelfBean = getBookshelfList().get(index);
                bookShelfBean.setHasUpdate(false);
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
                Intent intent = new Intent(getActivity(), ReadBookActivity.class);
                intent.putExtra("openFrom", ReadBookPresenterImpl.OPEN_FROM_APP);
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
                intent.putExtra("openFrom", BookDetailPresenterImpl.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, getBookshelfList().get(index));

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
        List<BookShelfBean> bookShelfBeans = getBookshelfList();
        if (bookShelfBeans != null && bookShelfBeans.size() > 0) {
            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                if (bookShelfBean.isLoading()) {
                    bookShelfBean.setLoading(false);
                    refreshBook(bookShelfBean.getNoteUrl());
                }
            }
        }
    }

    @Override
    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList) {
        if (viewIsList) {
            bookShelfListAdapter.replaceAll(bookShelfBeanList, bookPx);
        } else {
            bookShelfGridAdapter.replaceAll(bookShelfBeanList, bookPx);
        }
    }

    @Override
    public void refreshBook(String noteUrl) {
        if (viewIsList) {
            bookShelfListAdapter.refreshBook(noteUrl);
        } else {
            bookShelfGridAdapter.refreshBook(noteUrl);
        }
    }

    @Override
    public void updateGroup(Integer group) {
        this.group = group;
    }

    @Override
    public void refreshError(String error) {

    }

    @Override
    public void recreate() {

    }

    @Override
    public SharedPreferences getPreferences() {
        return preferences;
    }

    public interface CallBackValue {
        boolean isRecreate();

        int getGroup();
    }

}
