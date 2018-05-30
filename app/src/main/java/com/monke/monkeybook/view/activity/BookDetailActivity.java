//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IBookDetailPresenter;
import com.monke.monkeybook.view.impl.IBookDetailView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.monke.monkeybook.presenter.BookDetailPresenterImpl.FROM_BOOKSHELF;

public class BookDetailActivity extends MBaseActivity<IBookDetailPresenter> implements IBookDetailView {
    @BindView(R.id.ifl_content)
    FrameLayout iflContent;
    @BindView(R.id.iv_blur_cover)
    ImageView ivBlurCover;
    @BindView(R.id.iv_cover)
    ImageView ivCover;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_author)
    TextView tvAuthor;
    @BindView(R.id.tv_origin)
    TextView tvOrigin;
    @BindView(R.id.tv_chapter)
    TextView tvChapter;
    @BindView(R.id.tv_intro)
    TextView tvIntro;
    @BindView(R.id.tv_shelf)
    TextView tvShelf;
    @BindView(R.id.tv_read)
    TextView tvRead;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R.id.tv_change_origin)
    TextView tvChangeOrigin;

    private Animation animHideLoading;
    private Animation animShowInfo;
    private MoProgressHUD moProgressHUD;

    @Override
    protected IBookDetailPresenter initInjector() {
        return new BookDetailPresenterImpl(getIntent());
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_detail);
    }

    @Override
    protected void initData() {
        animShowInfo = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animHideLoading = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animHideLoading.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvLoading.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        //弹窗
        moProgressHUD = new MoProgressHUD(this);

        tvIntro.setMovementMethod(ScrollingMovementMethod.getInstance());
        initView();
    }

    @Override
    public void updateView() {
        if (null != mPresenter.getBookShelf()) {
            tvName.setText(mPresenter.getBookShelf().getBookInfoBean().getName());
            tvAuthor.setText(mPresenter.getBookShelf().getBookInfoBean().getAuthor());
            if (mPresenter.getInBookShelf()) {
                if (mPresenter.getBookShelf().getChapterListSize() > 0) {
                    tvChapter.setText(String.format(getString(R.string.read_dur_progress),
                            mPresenter.getBookShelf().getDurChapterListBean().getDurChapterName()));
                } else {
                    tvChapter.setText("无章节");
                }
                tvShelf.setText("移出书架");
                tvRead.setText("继续阅读");
                tvShelf.setOnClickListener(v -> {
                    //从书架移出
                    mPresenter.removeFromBookShelf();
                });
            } else {
                if (mPresenter.getBookShelf().getChapterListSize() == 0) {
                    tvChapter.setText("无章节");
                } else {
                    tvChapter.setText(String.format(getString(R.string.book_search_last),
                            mPresenter.getBookShelf().getLastChapterListBean().getDurChapterName()));
                }
                tvShelf.setText("放入书架");
                tvRead.setText("开始阅读");
                tvShelf.setOnClickListener(v -> {
                    //放入书架
                    mPresenter.addToBookShelf();
                });
            }
            if (mPresenter.getBookShelf().getBookInfoBean().getIntroduce() != null) {
                tvIntro.setText(mPresenter.getBookShelf().getBookInfoBean().getIntroduce());
            }
            if (tvIntro.getVisibility() != View.VISIBLE) {
                tvIntro.setVisibility(View.VISIBLE);
                tvIntro.startAnimation(animShowInfo);
            }
            if (mPresenter.getBookShelf().getBookInfoBean().getOrigin() != null && mPresenter.getBookShelf().getBookInfoBean().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText(String.format("来源:%s", mPresenter.getBookShelf().getBookInfoBean().getOrigin()));
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
        }
        tvLoading.startAnimation(animHideLoading);
        tvLoading.setOnClickListener(null);
    }

    @Override
    public void getBookShelfError() {
        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("加载失败,点击重试");
        tvLoading.setOnClickListener(v -> {
            tvLoading.setText("加载中...");
            tvLoading.setOnClickListener(null);
            mPresenter.getBookShelfInfo();
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        if (mPresenter.getOpenFrom() == BookDetailPresenterImpl.FROM_SEARCH) {
            //网络请求
            mPresenter.getBookShelfInfo();
        }
    }

    private void initView() {
        String coverUrl;
        String name;
        String author;
        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            coverUrl = mPresenter.getBookShelf().getBookInfoBean().getCoverUrl();
            name = mPresenter.getBookShelf().getBookInfoBean().getName();
            author = mPresenter.getBookShelf().getBookInfoBean().getAuthor();
            if (mPresenter.getBookShelf().getBookInfoBean().getOrigin() != null && mPresenter.getBookShelf().getBookInfoBean().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText(String.format("来源:%s", mPresenter.getBookShelf().getBookInfoBean().getOrigin()));
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
            updateView();
        } else {
            coverUrl = mPresenter.getSearchBook().getCoverUrl();
            name = mPresenter.getSearchBook().getName();
            author = mPresenter.getSearchBook().getAuthor();
            if (mPresenter.getSearchBook().getOrigin() != null && mPresenter.getSearchBook().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText(String.format("来源:%s", mPresenter.getSearchBook().getOrigin()));
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
            tvChapter.setText(String.format(getString(R.string.book_search_last), mPresenter.getSearchBook().getLastChapter()));
            tvShelf.setText("放入书架");
            tvRead.setText("开始阅读");
            tvRead.setOnClickListener(v -> {
                //放入书架
            });
            tvIntro.setVisibility(View.INVISIBLE);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("加载中...");
            tvLoading.setOnClickListener(null);
        }
        if (!this.isFinishing()) {
            Glide.with(this).load(coverUrl)
                    .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                            .placeholder(R.drawable.img_cover_default)).into(ivCover);
            Glide.with(this).load(coverUrl)
                    .apply(new RequestOptions()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                            .placeholder(R.drawable.img_cover_gs))
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                    .into(ivBlurCover);
        }
        tvName.setText(name);
        tvAuthor.setText(author);
    }

    @Override
    protected void bindEvent() {
        iflContent.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(getStart_share_ele()){
                    finishAfterTransition();
                }else{
                    finish();
                    overridePendingTransition(0,android.R.anim.fade_out);
                }
            } else {
                finish();
                overridePendingTransition(0,android.R.anim.fade_out);
            }
        });

        tvChangeOrigin.setOnClickListener(view -> moProgressHUD.showChangeSource(this, mPresenter.getBookShelf(),
                searchBookBean -> {
                    tvOrigin.setText(String.format("来源:%s", searchBookBean.getOrigin()));
                    tvLoading.setVisibility(View.VISIBLE);
                    tvLoading.setText("加载中...");
                    tvLoading.setOnClickListener(null);
                    if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                        mPresenter.changeBookSource(searchBookBean);
                    } else {
                        mPresenter.initBookFormSearch(searchBookBean);
                        mPresenter.getBookShelfInfo();
                    }
                }));

        tvRead.setOnClickListener(v -> {
            //进入阅读
            Intent intent = new Intent(BookDetailActivity.this, ReadBookActivity.class);
            intent.putExtra("from", ReadBookPresenterImpl.OPEN_FROM_APP);
            String key = String.valueOf(System.currentTimeMillis());
            intent.putExtra("data_key", key);
            try {
                BitIntentDataManager.getInstance().putData(key, mPresenter.getBookShelf().clone());
            } catch (CloneNotSupportedException e) {
                BitIntentDataManager.getInstance().putData(key, mPresenter.getBookShelf());
                e.printStackTrace();
            }
            startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(getStart_share_ele()){
                    finishAfterTransition();
                }else{
                    finish();
                    overridePendingTransition(0,android.R.anim.fade_out);
                }
            } else {
                finish();
                overridePendingTransition(0,android.R.anim.fade_out);
            }
        });

        ivRefresh.setOnClickListener(view -> {
            AnimationSet animationSet = new AnimationSet(true);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF,0.5f,
                    Animation.RELATIVE_TO_SELF,0.5f);
            rotateAnimation.setDuration(1000);
            animationSet.addAnimation(rotateAnimation);
            ivRefresh.startAnimation(animationSet);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("加载中...");
            tvLoading.setOnClickListener(null);
            mPresenter.getBookShelfInfo();
        });
    }

}