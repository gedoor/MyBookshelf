package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.model.UpLastChapterModel;
import com.monke.monkeybook.model.source.My716;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ChangeSourceView {
    public static SavedSource savedSource = new SavedSource();
    private TextView atvTitle;
    private ImageButton ibtStop;
    private RefreshRecyclerView rvSource;
    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnClickSource onClickSource;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookShelfBean book;
    private List<SearchBookBean> needUpLastChapter;
    private int upLastChapterIndex;
    private String bookTag;
    private String bookName;
    private String bookAuthor;
    private int shelfLastChapter;
    private CompositeDisposable compositeDisposable;
    private Disposable loadDBDisposable;
    private boolean searchIsFinish;


    private ChangeSourceView(MoProgressView moProgressView) {
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
                searchIsFinish = true;
                if (upLastChapterIndex > needUpLastChapter.size() - 1) {
                    rvSource.finishRefresh(true, true);
                }
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
        searchBookModel = new SearchBookModel(context, searchListener, true);
    }

    public static ChangeSourceView getInstance(MoProgressView moProgressView) {
        return new ChangeSourceView(moProgressView);
    }

    void showChangeSource(BookShelfBean bookShelf, final OnClickSource onClickSource, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.onClickSource = onClickSource;
        compositeDisposable = new CompositeDisposable();
        book = bookShelf;
        bookTag = bookShelf.getTag();
        bookName = bookShelf.getBookInfoBean().getName();
        bookAuthor = bookShelf.getBookInfoBean().getAuthor();
        shelfLastChapter = BookshelfHelp.guessChapterNum(bookShelf.getLastChapterName());
        atvTitle.setText(String.format("%s (%s)", bookName, bookAuthor));
        rvSource.startRefresh();
        getSearchBookInDb(bookShelf);
    }

    void stopChangeSource() {
        compositeDisposable.dispose();
        if (searchBookModel != null) {
            searchBookModel.stopSearch();
        }
    }

    private void getSearchBookInDb(BookShelfBean bookShelf) {
        if (loadDBDisposable != null) return;
        Single.create((SingleOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor)).build().list();
            if (searchBookBeans == null) searchBookBeans = new ArrayList<>();
            if (searchBookBeans.size() > 0) {
                for (SearchBookBean searchBookBean : searchBookBeans) {
                    if (searchBookBean.getTag().equals(bookShelf.getTag())) {
                        searchBookBean.setIsAdd(true);
                    } else {
                        searchBookBean.setIsAdd(false);
                    }
                }
                Collections.sort(searchBookBeans, this::compareSearchBooks);
            }
            e.onSuccess(searchBookBeans);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                        loadDBDisposable = d;
                    }

                    @Override
                    public void onSuccess(List<SearchBookBean> searchBookBeans) {
                        if (searchBookBeans.size() > 0) {
                            adapter.addAllSourceAdapter(searchBookBeans);
                            ibtStop.setVisibility(View.INVISIBLE);
                            rvSource.finishRefresh(true, true);
                        } else {
                            reSearchBook();
                        }
                        loadDBDisposable = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        reSearchBook();
                        loadDBDisposable = null;
                    }
                });
    }

    private void reSearchBook() {
        rvSource.startRefresh();
        Observable.create((ObservableOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookList = new ArrayList<>(adapter.getSearchBookBeans());
            List<BookSourceBean> bookSourceList = new ArrayList<>(BookSourceManager.getAllBookSource());
            for (SearchBookBean searchBookBean : new ArrayList<>(adapter.getSearchBookBeans())) {
                boolean hasSource = false;
                for (BookSourceBean bookSourceBean : new ArrayList<>(BookSourceManager.getAllBookSource())) {
                    if (Objects.equals(searchBookBean.getTag(), bookSourceBean.getBookSourceUrl())) {
                        bookSourceList.remove(bookSourceBean);
                        hasSource = true;
                        break;
                    }
                }
                if (!hasSource && !My716.TAG.equals(searchBookBean.getTag())) {
                    searchBookList.remove(searchBookBean);
                }
            }
            searchBookModel.searchReNew();
            searchIsFinish = false;
            searchBookModel.initSearchEngineS(bookSourceList);
            long startThisSearchTime = System.currentTimeMillis();
            searchBookModel.setSearchTime(startThisSearchTime);
            List<BookShelfBean> bookList = new ArrayList<>();
            bookList.add(book);
            searchBookModel.search(bookName, startThisSearchTime, bookList, false);
            e.onNext(searchBookList);
            e.onComplete();
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        adapter.reSetSourceAdapter();
                        adapter.addAllSourceAdapter(searchBookBeans);
                        needUpLastChapter = searchBookBeans;
                        upLastChapterIndex = -1;
                        upLastChapter();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private synchronized void upLastChapter() {
        upLastChapterIndex = upLastChapterIndex + 1;
        if (upLastChapterIndex > needUpLastChapter.size() - 1) {
            if (searchIsFinish) rvSource.finishRefresh(true, false);
            return;
        }
        UpLastChapterModel upLastChapterModel = new UpLastChapterModel();
        upLastChapterModel.toBookshelf(needUpLastChapter.get(upLastChapterIndex))
                .flatMap(upLastChapterModel::getChapterList)
                .flatMap(upLastChapterModel::saveSearchBookBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<SearchBookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                        handler.postDelayed(() -> {
                            if (!d.isDisposed()) {
                                d.dispose();
                                upLastChapter();
                            }
                        }, 20 * 1000);
                    }

                    @Override
                    public void onNext(SearchBookBean searchBookBean) {

                        upLastChapter();
                    }

                    @Override
                    public void onError(Throwable e) {
                        upLastChapter();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private synchronized void addSearchBook(List<SearchBookBean> value) {
        if (value.size() > 0) {
            Collections.sort(value, this::compareSearchBooks);
            for (SearchBookBean searchBookBean : value) {
                if (searchBookBean.getName().equals(bookName)
                        && (searchBookBean.getAuthor().equals(bookAuthor) || TextUtils.isEmpty(searchBookBean.getAuthor()) || TextUtils.isEmpty(bookAuthor))) {
                    if (searchBookBean.getTag().equals(bookTag)) {
                        searchBookBean.setIsAdd(true);
                    } else {
                        searchBookBean.setIsAdd(false);
                    }
                    boolean saveBookSource = false;
                    BookSourceBean bookSourceBean = BookshelfHelp.getBookSourceByTag(searchBookBean.getTag());
                    if (searchBookBean.getSearchTime() < 60 && bookSourceBean != null) {
                        bookSourceBean.increaseWeight(100 / (10 + searchBookBean.getSearchTime()));
                        saveBookSource = true;
                    }
                    if (shelfLastChapter > 0 && bookSourceBean != null) {
                        int lastChapter = BookshelfHelp.guessChapterNum(searchBookBean.getLastChapter());
                        if (lastChapter > shelfLastChapter) {
                            bookSourceBean.increaseWeight(100);
                            saveBookSource = true;
                        }
                    }
                    if (saveBookSource) {
                        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
                    }
                    DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);
                    handler.post(() -> adapter.addSourceAdapter(searchBookBean));
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
        ibtStop.setOnClickListener(v -> stopChangeSource());

    }

    private int compareSearchBooks(SearchBookBean s1, SearchBookBean s2) {
        boolean s1tag = s1.getTag().equals(bookTag);
        boolean s2tag = s2.getTag().equals(bookTag);
        if (s2tag && !s1tag)
            return 1;
        else if (s1tag && !s2tag)
            return -1;
        int result = Long.compare(s2.getAddTime(), s1.getAddTime());
        if (result != 0)
            return result;
        result = Integer.compare(s2.getLastChapterNum(), s1.getLastChapterNum());
        if (result != 0)
            return result;
        return Integer.compare(s2.getWeight(), s1.getWeight());
    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }

    public static class SavedSource {
        String bookName;
        long saveTime;
        BookSourceBean bookSource;

        SavedSource() {
            this.bookName = "";
            saveTime = 0;
        }

        public String getBookName() {
            return this.bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public long getSaveTime() {
            return saveTime;
        }

        public void setSaveTime(long saveTime) {
            this.saveTime = saveTime;
        }

        public BookSourceBean getBookSource() {
            return bookSource;
        }

        public void setBookSource(BookSourceBean bookSource) {
            this.bookSource = bookSource;
        }
    }
}
