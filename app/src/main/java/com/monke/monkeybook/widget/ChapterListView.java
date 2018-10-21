package com.monke.monkeybook.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.adapter.ChapterListAdapter;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChapterListView extends FrameLayout {
    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.fl_bg)
    FrameLayout flBg;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.toolbar_tab)
    TabLayout toolbarTab;
    @BindView(R.id.searchView)
    SearchView searchView;

    private ChapterListAdapter chapterListAdapter;
    private OnItemClickListener itemClickListener;
    private BookShelfBean bookShelfBean;
    private Context mContext;

    private Animation animIn;
    private Animation animOut;
    private OnChangeListener changeListener;

    public ChapterListView(@NonNull Context context) {
        this(context, null);
    }

    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    public void setOnChangeListener(OnChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    private void init() {
        setVisibility(INVISIBLE);
        LayoutInflater.from(getContext()).inflate(R.layout.view_chapterlist, this, true);
        initData();
        initView();
    }

    private void initData() {
        animIn = AnimationUtils.loadAnimation(getContext(), R.anim.anim_pop_chapterlist_in);
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flBg.setOnClickListener(null);
                changeListener.animIn();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flBg.setOnClickListener(v -> dismissChapterList());
                llContent.setOnClickListener(null);
                ivBack.setOnClickListener(view -> dismissChapterList());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animOut = AnimationUtils.loadAnimation(getContext(), R.anim.anim_pop_chapterlist_out);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flBg.setOnClickListener(null);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llContent.setVisibility(INVISIBLE);
                setVisibility(INVISIBLE);
                changeListener.animOut();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * 显示章节列表，并定位当前阅读章节
     */
    public void show(int durChapter) {
        upIndex(durChapter);
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
            animOut.cancel();
            animIn.cancel();
            llContent.setVisibility(VISIBLE);
            llContent.startAnimation(animIn);
        }
    }

    private void upIndex(int durChapter) {
        if (toolbarTab.getSelectedTabPosition() == 0) {
            chapterListAdapter.setIndex(durChapter);
            ((LinearLayoutManager) rvList.getLayoutManager()).scrollToPositionWithOffset(durChapter, 0);
        } else {
            chapterListAdapter.notifyDataSetChanged();
        }
    }

    public Boolean hasData() {
        return (changeListener != null && bookShelfBean != null);
    }

    private void initView() {
        ButterKnife.bind(this);
        llContent.setPadding(0, ImmersionBar.getStatusBarHeight((Activity) mContext), 0, 0);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setItemAnimator(null);
        toolbarTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (chapterListAdapter != null) {
                    chapterListAdapter.tabChange(tab.getPosition());
                    if (tab.getPosition() == 0) {
                        upIndex(bookShelfBean.getDurChapter());
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (chapterListAdapter != null) {
                    chapterListAdapter.tabChange(tab.getPosition());
                    if (tab.getPosition() == 0) {
                        upIndex(bookShelfBean.getDurChapter());
                    }
                }
            }
        });
        searchView.onActionViewCollapsed();
        searchView.setOnCloseListener(() -> {
            toolbarTab.setVisibility(VISIBLE);
            return false;
        });
        searchView.setOnSearchClickListener(view -> toolbarTab.setVisibility(INVISIBLE));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chapterListAdapter.search(newText);
                return false;
            }
        });
    }

    public void setData(BookShelfBean bookShelfBean, OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
        this.bookShelfBean = bookShelfBean;
        chapterListAdapter = new ChapterListAdapter(bookShelfBean, new OnItemClickListener() {
            @Override
            public void itemClick(int index, int page, int tabPosition) {
                if (itemClickListener != null) {
                    if (!(tabPosition == 0 && index == bookShelfBean.getDurChapter())) {
                        itemClickListener.itemClick(index, page, tabPosition);
                        searchViewCollapsed();
                        dismissChapterList();
                    }
                }
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean, int tabPosition) {
                if (itemClickListener != null && tabPosition == 1) {
                    itemClickListener.itemLongClick(bookmarkBean, tabPosition);
                }
            }
        });
        rvList.setAdapter(chapterListAdapter);
        Objects.requireNonNull(toolbarTab.getTabAt(0)).setText(mContext.getString(R.string.chapter_list_size, bookShelfBean.getChapterListSize()));
    }

    public void upChapterList(ChapterListBean chapterListBean) {
        if (chapterListAdapter != null) {
            chapterListAdapter.upChapterList(chapterListBean);
        }
    }

    private void searchViewCollapsed() {
        searchView.onActionViewCollapsed();
        toolbarTab.setVisibility(VISIBLE);
    }

    public Boolean dismissChapterList() {
        if (getVisibility() != VISIBLE) {
            return false;
        } else if (toolbarTab.getVisibility() != VISIBLE) {
            searchViewCollapsed();
            return true;
        } else {
            animOut.cancel();
            animIn.cancel();
            llContent.startAnimation(animOut);
            return true;
        }
    }

    public interface OnChangeListener {
        void animIn();

        void animOut();
    }

    public interface OnItemClickListener {
        void itemClick(int index, int page, int tabPosition);

        void itemLongClick(BookmarkBean bookmarkBean, int tabPosition);
    }
}