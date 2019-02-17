package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.dao.SearchBookBeanDao;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.view.adapter.ChangeSourceAdapter;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private SearchView searchView;
    private RefreshRecyclerView rvSource;
    private MoDialogHUD moProgressHUD;
    private MoDialogView moProgressView;
    private OnClickSource onClickSource;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookShelfBean book;
    private String bookTag;
    private String bookName;
    private String bookAuthor;
    private int shelfLastChapter;
    private CompositeDisposable compositeDisposable;

    @SuppressLint("InflateParams")
    private ChangeSourceView(MoDialogView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        adapter = new ChangeSourceAdapter(false);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(context));
        adapter.setOnItemClickListener((view, index) -> {
            moProgressHUD.dismiss();
            onClickSource.changeSource(adapter.getSearchBookBeans().get(index));
        });
        adapter.setOnItemLongClickListener((view, pos) -> {
            final String url = adapter.getSearchBookBeans().get(pos).getTag();
            final BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(url);
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenu().add(0, 0, 1, "禁用书源");
            popupMenu.getMenu().add(0, 0, 2, "删除书源");
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (sourceBean != null) {
                    switch (menuItem.getOrder()) {
                        case 1:
                            sourceBean.setEnable(false);
                            BookSourceManager.addBookSource(sourceBean);
                            BookSourceManager.refreshBookSource();
                            adapter.removeData(pos);
                            break;
                        case 2:
                            BookSourceManager.removeBookSource(sourceBean);
                            adapter.removeData(pos);
                            break;
                    }
                }
                return true;
            });
            popupMenu.show();
            return true;
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
                rvSource.finishRefresh(true, true);
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true);
            }

            @Override
            public Boolean checkIsExist(SearchBookBean searchBookBean) {
                boolean result = false;
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
        searchBookModel = new SearchBookModel(context, searchListener);
    }

    public static ChangeSourceView getInstance(MoDialogView moProgressView) {
        return new ChangeSourceView(moProgressView);
    }

    void showChangeSource(BookShelfBean bookShelf, final OnClickSource onClickSource, MoDialogHUD moProgressHUD) {
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
        RxBus.get().register(this);
    }

    private void stopChangeSource() {
        compositeDisposable.dispose();
        if (searchBookModel != null) {
            searchBookModel.stopSearch();
        }
    }

    void onDestroy() {
        RxBus.get().unregister(this);
        compositeDisposable.dispose();
        if (searchBookModel != null) {
            searchBookModel.onDestroy();
        }
    }

    private void getSearchBookInDb(BookShelfBean bookShelf) {
        Single.create((SingleOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor)).build().list();
            if (searchBookBeans == null) searchBookBeans = new ArrayList<>();
            List<SearchBookBean> searchBookList = new ArrayList<>();
            List<BookSourceBean> bookSourceList = new ArrayList<>(BookSourceManager.getSelectedBookSource());
            if (bookSourceList.size() > 0) {
                for (BookSourceBean bookSourceBean : BookSourceManager.getSelectedBookSource()) {
                    boolean hasSource = false;
                    for (SearchBookBean searchBookBean : new ArrayList<>(searchBookBeans)) {
                        if (Objects.equals(searchBookBean.getTag(), bookSourceBean.getBookSourceUrl())) {
                            bookSourceList.remove(bookSourceBean);
                            searchBookList.add(searchBookBean);
                            hasSource = true;
                            break;
                        }
                    }
                    if (hasSource) {
                        bookSourceList.remove(bookSourceBean);
                    }
                }
                searchBookModel.searchReNew();
                searchBookModel.initSearchEngineS(bookSourceList);
                long startThisSearchTime = System.currentTimeMillis();
                searchBookModel.setSearchTime(startThisSearchTime);
                List<BookShelfBean> bookList = new ArrayList<>();
                bookList.add(book);
                searchBookModel.search(bookName, startThisSearchTime, bookList, false);
                UpLastChapterModel.getInstance().startUpdate(searchBookList);
            }
            if (searchBookList.size() > 0) {
                for (SearchBookBean searchBookBean : searchBookList) {
                    if (searchBookBean.getTag().equals(bookShelf.getTag())) {
                        searchBookBean.setIsCurrentSource(true);
                    } else {
                        searchBookBean.setIsCurrentSource(false);
                    }
                }
                Collections.sort(searchBookList, this::compareSearchBooks);
            }
            e.onSuccess(searchBookList);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
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
                    }

                    @Override
                    public void onError(Throwable e) {
                        reSearchBook();
                    }
                });
    }

    private void reSearchBook() {
        rvSource.startRefresh();
        searchBookModel.initSearchEngineS(BookSourceManager.getSelectedBookSource());
        searchBookModel.searchReNew();
        long startThisSearchTime = System.currentTimeMillis();
        searchBookModel.setSearchTime(startThisSearchTime);
        List<BookShelfBean> bookList = new ArrayList<>();
        bookList.add(book);
        searchBookModel.search(bookName, startThisSearchTime, bookList, false);
    }

    private synchronized void addSearchBook(List<SearchBookBean> value) {
        if (value.size() > 0) {
            Collections.sort(value, this::compareSearchBooks);
            for (SearchBookBean searchBookBean : value) {
                if (searchBookBean.getName().equals(bookName)
                        && (searchBookBean.getAuthor().equals(bookAuthor) || TextUtils.isEmpty(searchBookBean.getAuthor()) || TextUtils.isEmpty(bookAuthor))) {
                    if (searchBookBean.getTag().equals(bookTag)) {
                        searchBookBean.setIsCurrentSource(true);
                    } else {
                        searchBookBean.setIsCurrentSource(false);
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
                        DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
                    }
                    DbHelper.getDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);
                    if (StringUtils.isTrimEmpty(searchView.getQuery().toString()) || searchBookBean.getOrigin().equals(searchView.getQuery().toString())) {
                        handler.post(() -> adapter.addSourceAdapter(searchBookBean));
                    }
                    break;
                }
            }
        }
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.mo_dialog_change_source, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        searchView = moProgressView.findViewById(R.id.searchView);
        atvTitle = moProgressView.findViewById(R.id.atv_title);
        ibtStop = moProgressView.findViewById(R.id.ibt_stop);
        rvSource = moProgressView.findViewById(R.id.rf_rv_change_source);
        ibtStop.setVisibility(View.INVISIBLE);

        rvSource.setBaseRefreshListener(this::reSearchBook);
        ibtStop.setOnClickListener(v -> stopChangeSource());
        searchView.onActionViewExpanded();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isTrimEmpty(newText)) {
                    List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                            .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor))
                            .build().list();
                    adapter.reSetSourceAdapter();
                    adapter.addAllSourceAdapter(searchBookBeans);
                } else {
                    List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                            .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor), SearchBookBeanDao.Properties.Origin.like("%" + searchView.getQuery().toString() + "%"))
                            .build().list();
                    adapter.reSetSourceAdapter();
                    adapter.addAllSourceAdapter(searchBookBeans);
                }
                return false;
            }
        });
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

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UP_SEARCH_BOOK)})
    public void upSearchBook(SearchBookBean searchBookBean) {
        if (!Objects.equals(book.getBookInfoBean().getName(), searchBookBean.getName())
                || !Objects.equals(book.getBookInfoBean().getAuthor(), searchBookBean.getAuthor())) {
            return;
        }
        for (int i = 0; i < adapter.getSearchBookBeans().size(); i++) {
            if (adapter.getSearchBookBeans().get(i).getTag().equals(searchBookBean.getTag())
                    && !adapter.getSearchBookBeans().get(i).getLastChapter().equals(searchBookBean.getLastChapter())) {
                adapter.getSearchBookBeans().get(i).setLastChapter(searchBookBean.getLastChapter());
                adapter.notifyItemChanged(i);
            }
        }
    }
}