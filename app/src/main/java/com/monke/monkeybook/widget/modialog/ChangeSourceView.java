package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.monke.basemvplib.BaseActivity;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
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
    private ImageButton ibtStop;
    private RefreshRecyclerView rvSource;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnClickSource onClickSource;
    private Context context;
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private List<BookShelfBean> bookShelfS = new ArrayList<>();
    private String bookTag;
    private String bookName;
    private String bookAuthor;
    private int shelfLastChapter;
    private BaseActivity activity;

    public static ChangeSourceView getInstance(BaseActivity activity, MoProgressView moProgressView) {
        return new ChangeSourceView(activity, moProgressView);
    }

    private ChangeSourceView(BaseActivity activity, MoProgressView moProgressView) {
        this.activity = activity;
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        adapter = new ChangeSourceAdapter(context, false);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(context));
        adapter.setOnItemClickListener((view, index) -> {
            moProgressHUD.dismiss();
            onClickSource.changeSource(adapter.getSearchBookBeans().get(index));
        });
        View viewRefreshError = LayoutInflater.from(context).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.setBackgroundResource(R.color.background_card);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            reSearchBook();
        });
        rvSource.setNoDataAndrRefreshErrorView(LayoutInflater.from(context).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);

        SearchBookModel.OnSearchListener searchListener = new SearchBookModel.OnSearchListener() {
            @Override
            public void refreshSearchBook() {
                ibtStop.setVisibility(View.VISIBLE);
                adapter.reSetSourceAdapter();
            }

            @Override
            public void refreshFinish(Boolean value) {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true,true);
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true);
            }

            @Override
            public Boolean checkIsExist(SearchBookBean searchBookBean) {
                Boolean result = false;
                for (int i = 0; i < adapter.getICount(); i++) {
                    if (adapter.getSearchBookBeans().get(i).getNoteUrl().equals(searchBookBean.getNoteUrl()) && adapter.getSearchBookBeans().get(i).getTag().equals(searchBookBean.getTag())) {
                        result = true;
                        break;
                    }
                }
                return result;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                addSearchBook(value);
            }

            @Override
            public void searchBookError(Boolean value) {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true);
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
        searchBookModel = new SearchBookModel(activity, searchListener, true);
    }

    void showChangeSource(BookShelfBean bookShelf, final OnClickSource onClickSource, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.onClickSource = onClickSource;
        bookShelfS.add(bookShelf);
        bookTag = bookShelf.getTag();
        bookName = bookShelf.getBookInfoBean().getName();
        bookAuthor = bookShelf.getBookInfoBean().getAuthor();
        shelfLastChapter = BookshelfHelp.guessChapterNum(bookShelf.getLastChapterName());
        atvTitle.setText(String.format("%s (%s)", bookName, bookAuthor));
        rvSource.startRefresh();
        getSearchBookInDb(bookShelf);
    }

    private void getSearchBookInDb(BookShelfBean bookShelf) {
        Observable.create((ObservableOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor)).build().list();
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
                            Collections.sort(searchBookBeans, (s1, s2) -> compareSearchBooks(s1, s2));
                            adapter.addAllSourceAdapter(searchBookBeans);
                            ibtStop.setVisibility(View.INVISIBLE);
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
        DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().deleteInTx(adapter.getSearchBookBeans());
        adapter.reSetSourceAdapter();
        long startThisSearchTime = System.currentTimeMillis();
        searchBookModel.setSearchTime(startThisSearchTime);
        searchBookModel.searchReNew();
        searchBookModel.search(bookName, startThisSearchTime, bookShelfS, false);
    }

    private synchronized void addSearchBook(List<SearchBookBean> value) {
        if (value.size() > 0) {
            for (SearchBookBean searchBookBean : value) {
                if (Objects.equals(searchBookBean.getName(), bookName)
                        && (Objects.equals(searchBookBean.getAuthor(), bookAuthor) || Objects.equals(searchBookBean.getAuthor(), "") || Objects.equals(bookAuthor, ""))) {
                    if (Objects.equals(searchBookBean.getTag(), bookTag)) {
                        searchBookBean.setIsAdd(true);
                    } else {
                        searchBookBean.setIsAdd(false);
                    }
                    if (shelfLastChapter > 0) {
                        int lastChapter = BookshelfHelp.guessChapterNum(searchBookBean.getLastChapter());
                        if (lastChapter > shelfLastChapter) {
                            BookSourceBean bookSourceBean = BookshelfHelp.getBookSourceByTag(searchBookBean.getTag());
                            if (bookSourceBean != null) {
                                int increase = (int) Math.ceil((lastChapter - shelfLastChapter) / 100);
                                bookSourceBean.increaseWeight(increase);
                                DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
                            }
                        }
                    }
                    DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);
                    Collections.sort(value, this::compareSearchBooks);
                    activity.runOnUiThread(() ->adapter.addSourceAdapter(searchBookBean));
                    break;
                }
            }
        }
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_change_source, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        atvTitle = moProgressView.findViewById(R.id.atv_title);
        ibtStop = moProgressView.findViewById(R.id.ibt_stop);
        rvSource = moProgressView.findViewById(R.id.rf_rv_change_source);
        ibtStop.setVisibility(View.INVISIBLE);

        rvSource.setBaseRefreshListener(this::reSearchBook);
        ibtStop.setOnClickListener(v -> searchBookModel.stopSearch());

    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }

    private int compareSearchBooks(SearchBookBean s1, SearchBookBean s2) {
        if (s2.getTag().equals(bookTag))
            return 1;
        else if (s1.getTag().equals(bookTag))
            return -1;
        int result = Long.compare(s2.getAddTime(), s1.getAddTime());
        if (result != 0)
            return result;
        result = Integer.compare(s2.getWeight(), s1.getWeight());
        if (result != 0)
            return result;
        return Integer.compare(s2.getLastChapterNum(), s1.getLastChapterNum());
    }
}
