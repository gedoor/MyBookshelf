package com.monke.monkeybook.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.adapter.ChapterListAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChapterListView extends FrameLayout {
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_list_count)
    TextView tvListCount;
    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.rvb_slider)
    RecyclerViewBar rvbSlider;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.fl_bg)
    FrameLayout flBg;
    @BindView(R.id.iv_back)
    ImageView ivBack;

    private ChapterListAdapter chapterListAdapter;
    private OnItemClickListener itemClickListener;
    private BookShelfBean bookShelfBean;
    private Context mContext;

    private Animation animIn;
    private Animation animOut;
    private OnChangeListener changeListener;
    public SharedPreferences preferences;

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
                ivBack.getDrawable().mutate();
                ivBack.getDrawable().setColorFilter(mContext.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
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
        chapterListAdapter.setIndex(durChapter);
        ((LinearLayoutManager) rvList.getLayoutManager()).scrollToPositionWithOffset(durChapter, 0);
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
            animOut.cancel();
            animIn.cancel();
            llContent.setVisibility(VISIBLE);
            llContent.startAnimation(animIn);
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
    }

    public void setData(BookShelfBean bookShelfBean, OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
        this.bookShelfBean = bookShelfBean;
        tvName.setText(bookShelfBean.getBookInfoBean().getName());
        tvListCount.setText(String.format(MApplication.getInstance().getString(R.string.all_chapter_num),
                bookShelfBean.getChapterListSize()));
        chapterListAdapter = new ChapterListAdapter(bookShelfBean, index -> {
            if (itemClickListener != null) {
                itemClickListener.itemClick(index);
                //rvbSlider.scrollToPositionWithOffset(index);
                dismissChapterList();
            }
        });
        rvList.setAdapter(chapterListAdapter);
        rvbSlider.setRecyclerView(rvList);
    }

    public void upChapterList(ChapterListBean chapterListBean) {
        if (chapterListAdapter != null) {
            chapterListAdapter.upChapterList(chapterListBean);
        }
    }

    public Boolean dismissChapterList() {
        if (getVisibility() != VISIBLE) {
            return false;
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
        void itemClick(int index);
    }
}