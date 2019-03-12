package com.kunfei.bookshelf.view.fragment;

import android.os.Bundle;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.OpenChapterBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.view.activity.ChapterListActivity;
import com.kunfei.bookshelf.view.adapter.BookmarkAdapter;
import com.kunfei.bookshelf.widget.modialog.EditBookmarkView;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;
import com.kunfei.bookshelf.widget.recycler.scroller.FastScrollRecyclerView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BookmarkFragment extends MBaseFragment {
    @BindView(R.id.rv_list)
    FastScrollRecyclerView rvList;

    private Unbinder unbinder;
    public MoDialogHUD moDialogHUD;
    private BookShelfBean bookShelf;
    private BookmarkAdapter adapter;

    @Override
    public int createLayoutId() {
        return R.layout.fragment_bookmark_list;
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
        moDialogHUD = new MoDialogHUD(getActivity());
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
        unbinder = ButterKnife.bind(this, view);
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
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvList.setAdapter(adapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        RxBus.get().unregister(this);
    }

    public void startSearch(String key) {
        adapter.search(key);
    }

    private void showBookmark(BookmarkBean bookmarkBean) {
        moDialogHUD.showBookmark(bookmarkBean, false, new EditBookmarkView.OnBookmarkClick() {
            @Override
            public void saveBookmark(BookmarkBean bookmarkBean) {
                DbHelper.getDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
                bookShelf.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void delBookmark(BookmarkBean bookmarkBean) {
                DbHelper.getDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
                bookShelf.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void openChapter(int chapterIndex, int pageIndex) {
                RxBus.get().post(RxBusTag.OPEN_BOOK_MARK, bookmarkBean);
                if (getFatherActivity() != null) {
                    getFatherActivity().finish();
                }
            }
        });

    }

    private ChapterListActivity getFatherActivity() {
        return (ChapterListActivity) getActivity();
    }

}
