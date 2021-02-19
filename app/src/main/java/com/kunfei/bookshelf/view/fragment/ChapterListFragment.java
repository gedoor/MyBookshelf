package com.kunfei.bookshelf.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.OpenChapterBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.databinding.FragmentChapterListBinding;
import com.kunfei.bookshelf.view.activity.ChapterListActivity;
import com.kunfei.bookshelf.view.adapter.ChapterListAdapter;

import java.util.List;
import java.util.Locale;

public class ChapterListFragment extends MBaseFragment<IPresenter> {

    private FragmentChapterListBinding binding;

    private ChapterListAdapter chapterListAdapter;

    private LinearLayoutManager layoutManager;

    private BookShelfBean bookShelf;
    private List<BookChapterBean> chapterBeanList;
    private boolean isChapterReverse;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentChapterListBinding.inflate(inflater, container, false);
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
            chapterBeanList = getFatherActivity().getChapterBeanList();
        }
        isChapterReverse = preferences.getBoolean("isChapterReverse", false);
    }

    /**
     * 控件绑定
     */
    @Override
    protected void bindView() {
        super.bindView();
        binding.rvList.setLayoutManager(layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, isChapterReverse));
        binding.rvList.setItemAnimator(null);
        chapterListAdapter = new ChapterListAdapter(bookShelf, chapterBeanList, (index, page) -> {
            if (index != bookShelf.getDurChapter()) {
                RxBus.get().post(RxBusTag.SKIP_TO_CHAPTER, new OpenChapterBean(index, page));
            }
            if (getFatherActivity() != null) {
                getFatherActivity().searchViewCollapsed();
                getFatherActivity().finish();
            }
        });
        if (bookShelf != null) {
            binding.rvList.setAdapter(chapterListAdapter);
            updateIndex(bookShelf.getDurChapter());
            updateChapterInfo();
        }
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        super.bindEvent();
        binding.tvCurrentChapterInfo.setOnClickListener(view -> layoutManager.scrollToPositionWithOffset(bookShelf.getDurChapter(), 0));
        binding.ivChapterTop.setOnClickListener(v -> binding.rvList.scrollToPosition(0));
        binding.ivChapterBottom.setOnClickListener(v -> {
            if (chapterListAdapter.getItemCount() > 0) {
                binding.rvList.scrollToPosition(chapterListAdapter.getItemCount() - 1);
            }
        });
    }

    public void startSearch(String key) {
        chapterListAdapter.search(key);
    }

    private void updateIndex(int durChapter) {
        chapterListAdapter.setIndex(durChapter);
        layoutManager.scrollToPositionWithOffset(durChapter, 0);
    }

    private void updateChapterInfo() {
        if (bookShelf != null) {
            if (chapterListAdapter.getItemCount() == 0) {
                binding.tvCurrentChapterInfo.setText(bookShelf.getDurChapterName());
            } else {
                binding.tvCurrentChapterInfo.setText(String.format(Locale.getDefault(), "%s (%d/%d章)", bookShelf.getDurChapterName(), bookShelf.getDurChapter() + 1, bookShelf.getChapterListSize()));
            }
        }
    }

    private ChapterListActivity getFatherActivity() {
        return (ChapterListActivity) getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHAPTER_CHANGE)})
    public void chapterChange(BookContentBean bookContentBean) {
        if (bookShelf != null && bookShelf.getNoteUrl().equals(bookContentBean.getNoteUrl())) {
            chapterListAdapter.upChapter(bookContentBean.getDurChapterIndex());
        }
    }
}
