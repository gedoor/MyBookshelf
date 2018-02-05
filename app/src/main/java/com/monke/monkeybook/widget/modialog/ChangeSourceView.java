package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
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
    private RecyclerView rvSource;
    private RotateAnimation rotateAnimation;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private MoProgressHUD.OnClickSource onClickSource;
    private Context context;
    private ChangeSourceAdapter adapter;
    private SearchBook searchBook;
    private List<BookShelfBean> bookShelfS = new ArrayList<>();
    private String thisTag;
    private String bookName;
    private String bookAuthor;

    public static ChangeSourceView getInstance(MoProgressView moProgressView) {
        return new ChangeSourceView(moProgressView);
    }

    private ChangeSourceView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        adapter = new ChangeSourceAdapter();
        rvSource.setLayoutManager(new LinearLayoutManager(context));
        rvSource.setAdapter(adapter);
        adapter.setOnItemClickListener((view, index) -> {
            moProgressHUD.dismiss();
            onClickSource.changeSource(adapter.getSearchBookBeans().get(index));
        });
        searchBook = new SearchBook(new SearchBook.OnSearchListener() {
            @Override
            public void refreshSearchBook(List<SearchBookBean> value) {
                if (value.size() > 0) {
                    adapter.reSetSourceAdapter();
                    if (Objects.equals(value.get(0).getName(), bookName)) {
                        if (Objects.equals(value.get(0).getTag(), thisTag)) {
                            value.get(0).setIsAdd(true);
                        } else {
                            value.get(0).setIsAdd(false);
                        }
                        adapter.addSourceAdapter(value.get(0));
                    }
                }
            }

            @Override
            public void refreshFinish(Boolean value) {
                rotateAnimation.cancel();
                saveSearchBook();
            }

            @Override
            public void loadMoreFinish(Boolean value) {

            }

            @Override
            public Boolean checkIsExist(SearchBookBean value) {
                return false;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                if (value.size() > 0) {
                    if (Objects.equals(value.get(0).getName(), bookName)) {
                        if (Objects.equals(value.get(0).getTag(), thisTag)) {
                            value.get(0).setIsAdd(true);
                        } else {
                            value.get(0).setIsAdd(false);
                        }
                        adapter.addSourceAdapter(value.get(0));
                    }
                }
            }

            @Override
            public void searchBookError(Boolean value) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
    }

    void showChangeSource(BookShelfBean bookShelf, final MoProgressHUD.OnClickSource onClickSource, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.onClickSource = onClickSource;
        bookShelfS.add(bookShelf);
        thisTag = bookShelf.getTag();
        bookName = bookShelf.getBookInfoBean().getName();
        bookAuthor = bookShelf.getBookInfoBean().getAuthor();
        atvTitle.setText(String.format("%s(%s)", bookName, bookAuthor));
        ivRefresh.setOnClickListener(view -> {
        });
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
                            rotateAnimation.cancel();
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
        long startThisSearchTime = System.currentTimeMillis();
        searchBook.setSearchTime(startThisSearchTime);
        searchBook.searchReNew();
        searchBook.search(bookName, startThisSearchTime, bookShelfS, false);
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_change_source, moProgressView, true);

        atvTitle = moProgressView.findViewById(R.id.atv_title);
        ivRefresh = moProgressView.findViewById(R.id.iv_refresh);
        rvSource = moProgressView.findViewById(R.id.rv_book_source_list);

        atvTitle.setOnClickListener(view -> {
            rotateAnimation.startNow();
            reSearchBook();
        });
        ivRefresh.setOnClickListener(view -> {
            rotateAnimation.startNow();
            reSearchBook();
        });
        setRefreshAnimation();
        rotateAnimation.startNow();
    }

    private void setRefreshAnimation() {
        rotateAnimation = new RotateAnimation(0, 359,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        ivRefresh.setAnimation(rotateAnimation);
    }

    private void saveSearchBook() {
        Observable.create((ObservableOnSubscribe<Boolean>) e->{
            DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(adapter.getSearchBookBeans());
        }).subscribe();
    }
}
