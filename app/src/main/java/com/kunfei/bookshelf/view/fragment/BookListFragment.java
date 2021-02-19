package com.kunfei.bookshelf.view.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.databinding.FragmentBookListBinding;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.BookListPresenter;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.BookListContract;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.activity.BookDetailActivity;
import com.kunfei.bookshelf.view.activity.ReadBookActivity;
import com.kunfei.bookshelf.view.adapter.BookShelfAdapter;
import com.kunfei.bookshelf.view.adapter.BookShelfGridAdapter;
import com.kunfei.bookshelf.view.adapter.BookShelfListAdapter;
import com.kunfei.bookshelf.view.adapter.base.OnItemClickListenerTwo;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

public class BookListFragment extends MBaseFragment<BookListContract.Presenter> implements BookListContract.View {

    private CallbackValue callbackValue;
    private FragmentBookListBinding binding;
    private String bookPx;
    private boolean resumed = false;
    private boolean isRecreate;
    private int group;

    private BookShelfAdapter bookShelfAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            resumed = savedInstanceState.getBoolean("resumed");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentBookListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected BookListContract.Presenter initInjector() {
        return new BookListPresenter();
    }

    @Override
    protected void initData() {
        callbackValue = (CallbackValue) getActivity();
        bookPx = preferences.getString(getString(R.string.pk_bookshelf_px), "0");
        isRecreate = callbackValue != null && callbackValue.isRecreate();
    }

    @Override
    protected void bindView() {
        super.bindView();
        int bookshelfLayout = preferences.getInt("bookshelfLayout", 0);
        if (bookshelfLayout == 0) {
            binding.rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));
            bookShelfAdapter = new BookShelfListAdapter(getActivity());
        } else {
            binding.rvBookshelf.setLayoutManager(new GridLayoutManager(getContext(), bookshelfLayout + 2));
            bookShelfAdapter = new BookShelfGridAdapter(getActivity());
        }
        binding.rvBookshelf.setAdapter((RecyclerView.Adapter) bookShelfAdapter);
        binding.refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
    }

    @Override
    protected void firstRequest() {
        group = preferences.getInt("bookshelfGroup", 0);
        boolean needRefresh = preferences.getBoolean(getString(R.string.pk_auto_refresh), false)
                && !isRecreate && NetworkUtils.isNetWorkAvailable() && group != 2;
        mPresenter.queryBookShelf(needRefresh, group);
    }

    @Override
    protected void bindEvent() {
        binding.refreshLayout.setOnRefreshListener(() -> {
            mPresenter.queryBookShelf(NetworkUtils.isNetWorkAvailable(), group);
            if (!NetworkUtils.isNetWorkAvailable()) {
                Toast.makeText(getContext(), R.string.network_connection_unavailable, Toast.LENGTH_SHORT).show();
            }
            binding.refreshLayout.setRefreshing(false);
        });
        ItemTouchCallback itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setSwipeRefreshLayout(binding.refreshLayout);
        itemTouchCallback.setViewPager(callbackValue.getViewPager());
        if (bookPx.equals("2")) {
            itemTouchCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
            itemTouchHelper.attachToRecyclerView(binding.rvBookshelf);
        } else {
            itemTouchCallback.setDragEnable(false);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
            itemTouchHelper.attachToRecyclerView(binding.rvBookshelf);
        }
        bookShelfAdapter.setItemClickListener(getAdapterListener());
        itemTouchCallback.setOnItemTouchCallbackListener(bookShelfAdapter.getItemTouchCallbackListener());
        binding.ivBack.setOnClickListener(v -> setArrange(false));
        binding.ivDel.setOnClickListener(v -> {
            if (bookShelfAdapter.getSelected().size() == bookShelfAdapter.getBooks().size()) {
                AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete)
                        .setMessage(getString(R.string.sure_del_all_book))
                        .setPositiveButton(R.string.yes, (dialog, which) -> delSelect())
                        .setNegativeButton(R.string.no, null)
                        .show();
                ATH.setAlertDialogTint(alertDialog);
            } else {
                delSelect();
            }
        });
        binding.ivSelectAll.setOnClickListener(v -> bookShelfAdapter.selectAll());
    }

    private OnItemClickListenerTwo getAdapterListener() {
        return new OnItemClickListenerTwo() {
            @Override
            public void onClick(View view, int index) {
                if (binding.actionBar.getVisibility() == View.VISIBLE) {
                    upSelectCount();
                    return;
                }
                BookShelfBean bookShelfBean = bookShelfAdapter.getBooks().get(index);
                Intent intent = new Intent(getContext(), ReadBookActivity.class);
                intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
                String key = String.valueOf(System.currentTimeMillis());
                String bookKey = "book" + key;
                intent.putExtra("bookKey", bookKey);
                BitIntentDataManager.getInstance().putData(bookKey, bookShelfBean.clone());
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onLongClick(View view, int index) {
                BookShelfBean bookShelfBean = bookShelfAdapter.getBooks().get(index);
                String key = String.valueOf(System.currentTimeMillis());
                BitIntentDataManager.getInstance().putData(key, bookShelfBean.clone());
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("openFrom", BookDetailPresenter.FROM_BOOKSHELF);
                intent.putExtra("data_key", key);
                intent.putExtra("noteUrl", bookShelfBean.getNoteUrl());
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
        if (binding.viewEmpty.rlEmptyView == null) return;
        if (bookShelfBeanList.size() > 0) {
            binding.viewEmpty.rlEmptyView.setVisibility(View.GONE);
        } else {
            binding.viewEmpty.tvEmpty.setText(R.string.bookshelf_empty);
            binding.viewEmpty.rlEmptyView.setVisibility(View.VISIBLE);
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
        toast(error);
    }

    @Override
    public SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setArrange(boolean isArrange) {
        if (bookShelfAdapter != null) {
            bookShelfAdapter.setArrange(isArrange);
            if (isArrange) {
                binding.actionBar.setVisibility(View.VISIBLE);
                upSelectCount();
            } else {
                binding.actionBar.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void upSelectCount() {
        binding.tvSelectCount.setText(String.format("%d/%d", bookShelfAdapter.getSelected().size(), bookShelfAdapter.getBooks().size()));
    }

    private void delSelect() {
        Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            for (String noteUrl : bookShelfAdapter.getSelected()) {
                BookshelfHelp.removeFromBookShelf(BookshelfHelp.getBook(noteUrl));
            }
            bookShelfAdapter.getSelected().clear();
            emitter.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        mPresenter.queryBookShelf(false, group);
                    }
                });
    }

    public interface CallbackValue {
        boolean isRecreate();

        int getGroup();

        ViewPager getViewPager();
    }

}
