package com.monke.monkeybook.widget;

import android.annotation.TargetApi;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.view.adapter.ChapterListAdapter;

public class ChapterListView extends FrameLayout{
    private TextView tvName;
    private TextView tvListCount;
    private RecyclerView rvList;
    private RecyclerViewBar rvbSlider;

    private FrameLayout flBg;
    private LinearLayout llContent;

    private ChapterListAdapter chapterListAdapter;
    private OnItemClickListener itemClickListener;
    private BookShelfBean bookShelfBean;

    private Animation animIn;
    private Animation animOut;
    private OnChangeListener changeListener;

    public interface OnChangeListener {
        void animIn();
        void animOut();
    }

    public void setOnChangeListener(OnChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public ChapterListView(@NonNull Context context) {
        this(context,null);
    }

    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setVisibility(INVISIBLE);
        LayoutInflater.from(getContext()).inflate(R.layout.view_chapterlist,this,true);
        initData();
        initView();
    }

    private void initData() {
        animIn = AnimationUtils.loadAnimation(getContext(),R.anim.anim_pop_chapterlist_in);
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flBg.setOnClickListener(null);
                changeListener.animIn();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flBg.setOnClickListener(v -> dismissChapterList());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animOut = AnimationUtils.loadAnimation(getContext(),R.anim.anim_pop_chapterlist_out);
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

    public void show(int durChapter) {
        chapterListAdapter.setIndex(durChapter);
        ((LinearLayoutManager) rvList.getLayoutManager()).scrollToPositionWithOffset(durChapter,0);
        if(getVisibility()!=VISIBLE){
            setVisibility(VISIBLE);
            animOut.cancel();
            animIn.cancel();
            llContent.setVisibility(VISIBLE);
            llContent.startAnimation(animIn);
        }
    }

    public Boolean hasData() {
        return (changeListener != null);
    }

    public interface OnItemClickListener{
        void itemClick(int index);
    }

    private void initView() {
        flBg = findViewById(R.id.fl_bg);
        llContent = findViewById(R.id.ll_content);
        tvName = findViewById(R.id.tv_name);
        tvListCount = findViewById(R.id.tv_list_count);
        rvList = findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setItemAnimator(null);
        rvbSlider = findViewById(R.id.rvb_slider);
    }

    public void setData(BookShelfBean bookShelfBean,OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
        this.bookShelfBean = bookShelfBean;
        tvName.setText(bookShelfBean.getBookInfoBean().getName());
        tvListCount.setText(String.format(MApplication.getInstance().getString(R.string.all_chapter_num),
                bookShelfBean.getBookInfoBean().getChapterlist().size()));
        chapterListAdapter = new ChapterListAdapter(bookShelfBean, index -> {
            if(itemClickListener!=null){
                itemClickListener.itemClick(index);
                rvbSlider.scrollToPositionWithOffset(index);
                dismissChapterList();
            }
        });
        rvList.setAdapter(chapterListAdapter);
        rvbSlider.setRecyclerView(rvList);
    }

    public Boolean dismissChapterList(){
        if(getVisibility()!=VISIBLE){
            return false;
        }else{
            animOut.cancel();
            animIn.cancel();
            llContent.startAnimation(animOut);
            return true;
        }
    }
}