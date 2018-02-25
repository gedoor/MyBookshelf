package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.model.SearchBook;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;
import com.monke.monkeybook.widget.refreshview.BaseRefreshListener;
import com.monke.monkeybook.widget.refreshview.OnRefreshWithProgressListener;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ChangeSourceView {
    private TextView atvTitle;
    private ImageView ivRefresh;
    private RefreshRecyclerView rvSource;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnClickSource onClickSource;
    private Context context;
    private ChangeSourceAdapter adapter;
    private SearchBook searchBook;
    private List<BookShelfBean> bookShelfS = new ArrayList<>();
    private String thisTag;
    private String bookName;

    public static ChangeSourceView getInstance(MoProgressView moProgressView) {
        return new ChangeSourceView(moProgressView);
    }

    private ChangeSourceView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        adapter = new ChangeSourceAdapter(false);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(context));
        adapter.setOnItemClickListener((view, index) -> {
            moProgressHUD.dismiss();
            onClickSource.changeSource(adapter.getSearchBookBeans().get(index));
        });
        View viewRefreshError = LayoutInflater.from(context).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            reSearchBook();
        });
        rvSource.setNoDataAndrRefreshErrorView(LayoutInflater.from(context).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);
        searchBook = new SearchBook(new SearchBook.OnSearchListener() {
            @Override
            public void refreshSearchBook(List<SearchBookBean> value) {
                if (value.size() > 0) {
                    SearchBookBean searchBookBean = value.get(0);
                    if (Objects.equals(searchBookBean.getName(), bookName)) {
                        if (Objects.equals(searchBookBean.getTag(), thisTag)) {
                            searchBookBean.setIsAdd(true);
                        } else {
                            searchBookBean.setIsAdd(false);
                        }
                        adapter.addSourceAdapter(searchBookBean);
                    }
                }
            }

            @Override
            public void refreshFinish(Boolean value) {
                saveSearchBook();
                rvSource.finishRefresh(true,true);
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                saveSearchBook();
                rvSource.finishRefresh(true);
            }

            @Override
            public Boolean checkIsExist(SearchBookBean value) {
                return false;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                if (value.size() > 0) {
                    SearchBookBean searchBookBean = value.get(0);
                    if (Objects.equals(searchBookBean.getName(), bookName)) {
                        if (Objects.equals(searchBookBean.getTag(), thisTag)) {
                            searchBookBean.setIsAdd(true);
                        } else {
                            searchBookBean.setIsAdd(false);
                        }
                        adapter.addSourceAdapter(searchBookBean);
                    }
                }
            }

            @Override
            public void searchBookError(Boolean value) {
                saveSearchBook();
                rvSource.finishRefresh(true);
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
    }

    void showChangeSource(BookShelfBean bookShelf, final OnClickSource onClickSource, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.onClickSource = onClickSource;
        bookShelfS.add(bookShelf);
        thisTag = bookShelf.getTag();
        bookName = bookShelf.getBookInfoBean().getName();
        String bookAuthor = bookShelf.getBookInfoBean().getAuthor();
        atvTitle.setText(String.format("%s(%s)", bookName, bookAuthor));
        rvSource.startRefresh();
        getSearchBookInDb(bookShelf);
    }

    private void getSearchBookInDb(BookShelfBean bookShelf) {
        Observable.create((ObservableOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookName)).build().list();
            e.onNext(searchBookBeans == null ? new ArrayList<>() : searchBookBeans);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        if (searchBookBeans.size() > 0) {
                            for (SearchBookBean searchBookBean : searchBookBeans) {
                                if (Objects.equals(searchBookBean.getTag(), bookShelf.getTag())) {
                                    searchBookBean.setIsAdd(true);
                                } else {
                                    searchBookBean.setIsAdd(false);
                                }
                            }
                            adapter.addAllSourceAdapter(searchBookBeans);
                            rvSource.finishRefresh(true, true);
                        } else {
                            reSearchBook();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        reSearchBook();
                    }
                });
    }

    private void reSearchBook() {
        rvSource.startRefresh();
        adapter.reSetSourceAdapter();
        long startThisSearchTime = System.currentTimeMillis();
        searchBook.setSearchTime(startThisSearchTime);
        searchBook.searchReNew();
        searchBook.search(bookName, startThisSearchTime, bookShelfS, false);
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_change_source, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        atvTitle = moProgressView.findViewById(R.id.atv_title);
        ivRefresh = moProgressView.findViewById(R.id.iv_refresh);
        rvSource = moProgressView.findViewById(R.id.rf_rv_change_source);

        rvSource.setBaseRefreshListener(this::reSearchBook);

    }

    private void saveSearchBook() {
        Observable.create((ObservableOnSubscribe<Boolean>) e->{
            DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(adapter.getSearchBookBeans());
        }).subscribe();
    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }
}
