package com.kunfei.bookshelf.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.OpenChapterBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.databinding.FragmentBookmarkListBinding;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.view.activity.ChapterListActivity;
import com.kunfei.bookshelf.view.adapter.BookmarkAdapter;
import com.kunfei.bookshelf.widget.modialog.BookmarkDialog;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

public class BookmarkFragment extends MBaseFragment<IPresenter> {

    private FragmentBookmarkListBinding binding;
    private BookShelfBean bookShelf;
    private List<BookmarkBean> bookmarkBeanList;
    private BookmarkAdapter adapter;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentBookmarkListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        super.initData();
        if (getFatherActivity() != null) {
            bookShelf = getFatherActivity().getBookShelf();
        }
    }

    /**
     * 控件绑定
     */
    @Override
    protected void bindView() {
        super.bindView();
        adapter = new BookmarkAdapter(bookShelf, new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int index, int page) {
                if (index != bookShelf.getDurChapter()) {
                    RxBus.get().post(RxBusTag.SKIP_TO_CHAPTER, new OpenChapterBean(index, page));
                }
                if (getFatherActivity() != null) {
                    getFatherActivity().searchViewCollapsed();
                    getFatherActivity().finish();
                }
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean) {
                if (getFatherActivity() != null) {
                    getFatherActivity().searchViewCollapsed();
                }
                showBookmark(bookmarkBean);
            }
        });
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (bookShelf != null) {
                bookmarkBeanList = BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName());
                emitter.onSuccess(true);
            } else {
                emitter.onSuccess(false);
            }
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (aBoolean) {
                            adapter.setAllBookmark(bookmarkBeanList);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        RxBus.get().unregister(this);
    }

    public void startSearch(String key) {
        adapter.search(key);
    }

    private void showBookmark(BookmarkBean bookmarkBean) {
        BookmarkDialog.builder(getContext(), bookmarkBean, false)
                .setPositiveButton(new BookmarkDialog.Callback() {
                    @Override
                    public void saveBookmark(BookmarkBean bookmarkBean) {
                        DbHelper.getDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void delBookmark(BookmarkBean bookmarkBean) {
//                        Log.d("delBookmark","before="+bookmarkBeanList.size());
                        DbHelper.getDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
//                        Log.d("delBookmark","after="+bookmarkBeanList.size());
                        bookmarkBeanList = BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName());
//                        Log.d("delBookmark","fine="+bookmarkBeanList.size());
                        adapter.setAllBookmark(bookmarkBeanList);
//                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void openChapter(int chapterIndex, int pageIndex) {
                        RxBus.get().post(RxBusTag.OPEN_BOOK_MARK, bookmarkBean);
                        if (getFatherActivity() != null) {
                            getFatherActivity().finish();
                        }
                    }
                }).show();
    }

    private ChapterListActivity getFatherActivity() {
        return (ChapterListActivity) getActivity();
    }

}
