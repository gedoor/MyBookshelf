package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.SearchBookBeanDao;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;
import com.kunfei.bookshelf.view.adapter.ChangeSourceAdapter;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChangeSourceDialog extends BaseDialog implements ChangeSourceAdapter.CallBack {
    private Context context;
    private TextView atvTitle;
    private ImageButton ibtStop;
    private SearchView searchView;
    private RefreshRecyclerView rvSource;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookShelfBean book;
    private String bookTag;
    private String bookName;
    private String bookAuthor;
    private int shelfLastChapter;
    private CompositeDisposable compositeDisposable;
    private Callback callback;

    public static ChangeSourceDialog builder(Context context, BookShelfBean bookShelfBean) {
        return new ChangeSourceDialog(context, bookShelfBean);
    }

    private ChangeSourceDialog(@NonNull Context context, BookShelfBean bookShelfBean) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        init(bookShelfBean);
    }

    private void init(BookShelfBean bookShelf) {
        this.book = bookShelf;
        compositeDisposable = new CompositeDisposable();
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_change_source, null);
        bindView(view);
        setContentView(view);
        initData();
    }

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        searchView = view.findViewById(R.id.searchView);
        atvTitle = view.findViewById(R.id.atv_title);
        ibtStop = view.findViewById(R.id.ibt_stop);
        rvSource = view.findViewById(R.id.rf_rv_change_source);
        ibtStop.setVisibility(View.INVISIBLE);

        rvSource.addItemDecoration(new DividerItemDecoration(context, LinearLayout.VERTICAL));
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

    @Override
    public void changeTo(SearchBookBean searchBookBean) {
        if (!Objects.equals(book.getNoteUrl(), searchBookBean.getNoteUrl())) {
            callback.changeSource(searchBookBean);
        }
        dismiss();
    }

    @Override
    public void showMenu(View view, SearchBookBean searchBookBean) {
        final String url = searchBookBean.getTag();
        final BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(url);
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add(0, R.id.menu_disable, 1, "禁用书源");
        popupMenu.getMenu().add(0, R.id.menu_del, 2, "删除书源");
        popupMenu.getMenu().add(0, R.id.menu_edit, 3, "编辑书源");
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (sourceBean != null) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_disable:
                        sourceBean.setEnable(false);
                        BookSourceManager.addBookSource(sourceBean);
                        adapter.removeData(searchBookBean);
                        Toast.makeText(context, String.format("%s已禁用", sourceBean.getBookSourceName()), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menu_del:
                        BookSourceManager.removeBookSource(sourceBean);
                        adapter.removeData(searchBookBean);
                        Toast.makeText(context, String.format("%s已删除", sourceBean.getBookSourceName()), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menu_edit:
                        SourceEditActivity.startThis(context, sourceBean);
                        break;
                }
            }
            return true;
        });
        popupMenu.show();
    }

    @SuppressLint("InflateParams")
    private void initData() {
        adapter = new ChangeSourceAdapter(false);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(context));
        adapter.setCallBack(this);
        View viewRefreshError = LayoutInflater.from(context).inflate(R.layout.view_refresh_error, null);
        viewRefreshError.setBackgroundResource(R.color.background_card);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            reSearchBook();
        });
        rvSource.setNoDataAndRefreshErrorView(LayoutInflater.from(context).inflate(R.layout.view_refresh_no_data, null),
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
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                addSearchBook(value);
            }

            @Override
            public void searchBookError(Throwable throwable) {
                ibtStop.setVisibility(View.INVISIBLE);
                if (adapter.getICount() == 0) {
                    rvSource.refreshError();
                }
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
        searchBookModel = new SearchBookModel(searchListener);
        bookTag = book.getTag();
        bookName = book.getBookInfoBean().getName();
        bookAuthor = book.getBookInfoBean().getAuthor();
        shelfLastChapter = BookshelfHelp.guessChapterNum(book.getLastChapterName());
        atvTitle.setText(String.format("%s (%s)", bookName, bookAuthor));
        rvSource.startRefresh();
        getSearchBookInDb(book);
        RxBus.get().register(this);
        setOnDismissListener(dialog -> {
            RxBus.get().unregister(ChangeSourceDialog.this);
            compositeDisposable.dispose();
            if (searchBookModel != null) {
                searchBookModel.onDestroy();
            }
        });
    }

    public ChangeSourceDialog setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void show() {
        super.show();
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        params.height = ScreenUtils.getAppSize()[1] - 60;
        params.width = ScreenUtils.getAppSize()[0] - 60;
        getWindow().setAttributes(params);
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
                    BookSourceBean bookSourceBean = BookSourceManager.getBookSourceByUrl(searchBookBean.getTag());
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

    private void stopChangeSource() {
        compositeDisposable.dispose();
        if (searchBookModel != null) {
            searchBookModel.stopSearch();
        }
    }

    public interface Callback {
        void changeSource(SearchBookBean searchBookBean);
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
