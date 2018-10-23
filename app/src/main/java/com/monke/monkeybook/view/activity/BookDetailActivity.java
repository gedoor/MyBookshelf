//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.BookDetailContract;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.monke.monkeybook.help.Constant.BOOK_GROUPS;
import static com.monke.monkeybook.presenter.BookDetailPresenterImpl.FROM_BOOKSHELF;

public class BookDetailActivity extends MBaseActivity<BookDetailContract.Presenter> implements BookDetailContract.View {
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
    @BindView(R.id.tv_update)
    TextView tvUpdate;
    @BindView(R.id.tv_origin)
    TextView tvOrigin;
    @BindView(R.id.iv_web)
    ImageView ivWeb;
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
    @BindView(R.id.rg_book_group)
    RadioGroup rgBookGroup;
    @BindView(R.id.tv_chapter_size)
    TextView tvChapterSize;
    @BindView(R.id.rb_zdy)
    RadioButton rbZdy;

    private Animation animHideLoading;
    private Animation animShowInfo;
    private MoProgressHUD moProgressHUD;
    private String author;
    private BookShelfBean bookShelfBean;
    private String coverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected BookDetailContract.Presenter initInjector() {
        return new BookDetailPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_detail);
    }

    @Override
    protected void initData() {
        mPresenter.initData(getIntent());
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
        bookShelfBean = mPresenter.getBookShelf();
        BookInfoBean bookInfoBean;
        if (null != bookShelfBean) {
            bookInfoBean = bookShelfBean.getBookInfoBean();
            tvName.setText(bookInfoBean.getName());
            author = bookInfoBean.getAuthor();
            tvAuthor.setText(TextUtils.isEmpty(author) ? "未知" : author);
            ((RadioButton) rgBookGroup.getChildAt(bookShelfBean.getGroup())).setChecked(true);
            if (mPresenter.getInBookShelf()) {
                tvChapter.setText(bookShelfBean.getDurChapterName()); // last
                tvShelf.setText(R.string.remove_from_bookshelf);
                tvRead.setText("继续阅读");
                setTvUpdate(bookShelfBean.getAllowUpdate(), true);
                tvShelf.setOnClickListener(v -> {
                    //从书架移出
                    mPresenter.removeFromBookShelf();
                });
            } else {
                setTvUpdate(false, false);
                if (!TextUtils.isEmpty(bookShelfBean.getLastChapterName())) {
                    tvChapter.setText(bookShelfBean.getLastChapterName()); // last
                }
                tvShelf.setText("放入书架");
                tvRead.setText("开始阅读");
                tvShelf.setOnClickListener(v -> {
                    //放入书架
                    mPresenter.addToBookShelf();
                });
            }
            if (bookInfoBean.getIntroduce() != null) {
                tvIntro.setText(bookInfoBean.getIntroduce());
            }
            if (tvIntro.getVisibility() != View.VISIBLE) {
                tvIntro.setVisibility(View.VISIBLE);
                tvIntro.startAnimation(animShowInfo);
            }
            String origin = bookInfoBean.getOrigin();
            if (!TextUtils.isEmpty(origin)) {
                ivWeb.setVisibility(View.VISIBLE);
                tvOrigin.setText(origin);
            } else {
                ivWeb.setVisibility(View.INVISIBLE);
                tvOrigin.setVisibility(View.INVISIBLE);
            }
            String customCoverPath = bookShelfBean.getCustomCoverPath();
            coverUrl = bookInfoBean.getCoverUrl();
            if (!this.isFinishing()) {
                if (TextUtils.isEmpty(customCoverPath)) {
                    Glide.with(this).load(coverUrl)
                            .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                                    .placeholder(R.drawable.img_cover_default))
                            .into(ivCover);
                    Glide.with(this).load(coverUrl)
                            .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                                    .placeholder(R.drawable.img_cover_gs))
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                            .into(ivBlurCover);
                } else if (customCoverPath.startsWith("http")) {
                    Glide.with(this).load(customCoverPath)
                            .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                                    .placeholder(R.drawable.img_cover_default))
                            .into(ivCover);
                    Glide.with(this).load(customCoverPath)
                            .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                                    .placeholder(R.drawable.img_cover_gs))
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                            .into(ivBlurCover);
                } else {
                    ivCover.setImageBitmap(BitmapFactory.decodeFile(customCoverPath));
                }
            }
            upChapterSizeTv();
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

    private void setTvUpdate(boolean update, boolean show) {
        tvUpdate.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            tvUpdate.setText(update ? R.string.allow_update : R.string.disable_update);
            tvUpdate.setTextColor(getResources().getColor(update ? R.color.white : R.color.darker_gray));
        }
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        if (mPresenter.getOpenFrom() == BookDetailPresenterImpl.FROM_SEARCH) {
            //网络请求
            mPresenter.getBookShelfInfo();
        }
    }

    @SuppressLint("DefaultLocale")
    private void upChapterSizeTv() {
        if (bookShelfBean.getChapterListSize() > 0) {
            tvChapterSize.setText(String.format("(%d)", bookShelfBean.getChapterListSize()));
        } else {
            tvChapterSize.setText("");
        }
    }

    private void initView() {
        for (int i = 0; i < 3; i++) {
            ((RadioButton) rgBookGroup.getChildAt(i)).setText(BOOK_GROUPS[i].substring(0, 2));
        }
        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            updateView();
        } else {
            if (mPresenter.getSearchBook() == null) return;
            SearchBookBean searchBookBean = mPresenter.getSearchBook();
            coverUrl = searchBookBean.getCoverUrl();
            tvName.setText(searchBookBean.getName());
            author = searchBookBean.getAuthor();
            tvAuthor.setText(TextUtils.isEmpty(author) ? "未知" : author);
            String origin = TextUtils.isEmpty(searchBookBean.getOrigin()) ? "未知" : searchBookBean.getOrigin();
            tvOrigin.setText(origin);
            tvChapter.setText(searchBookBean.getLastChapter());  // newest
            tvIntro.setText(searchBookBean.getIntroduce());
            tvShelf.setText("放入书架");
            tvRead.setText("开始阅读");
            tvRead.setOnClickListener(v -> {
                //放入书架
            });
            tvIntro.setVisibility(View.INVISIBLE);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("加载中...");
            tvLoading.setOnClickListener(null);
            setTvUpdate(false, false);
            if (!this.isFinishing()) {
                Glide.with(this).load(coverUrl)
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                                .placeholder(R.drawable.img_cover_default)).into(ivCover);
            }
            Glide.with(this).load(coverUrl)
                    .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                            .placeholder(R.drawable.img_cover_gs))
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                    .into(ivBlurCover);
        }
    }

    @Override
    protected void bindEvent() {
        iflContent.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (getStart_share_ele()) {
                    finishAfterTransition();
                } else {
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
                }
            } else {
                finish();
                overridePendingTransition(0, android.R.anim.fade_out);
            }
        });

        tvUpdate.setOnClickListener(view -> {
            boolean update = !mPresenter.getBookShelf().getAllowUpdate();
            mPresenter.getBookShelf().setAllowUpdate(update);
            setTvUpdate(update, true);
            if (mPresenter.getInBookShelf()) {
                mPresenter.addToBookShelf();
            }
        });

        tvChangeOrigin.setOnClickListener(view -> moProgressHUD.showChangeSource(this, mPresenter.getBookShelf(),
                searchBookBean -> {
                    tvOrigin.setText(searchBookBean.getOrigin());
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
            intent.putExtra("openFrom", ReadBookPresenterImpl.OPEN_FROM_APP);
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
                if (getStart_share_ele()) {
                    finishAfterTransition();
                } else {
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
                }
            } else {
                finish();
                overridePendingTransition(0, android.R.anim.fade_out);
            }
        });

        ivRefresh.setOnClickListener(view -> {
            AnimationSet animationSet = new AnimationSet(true);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            animationSet.addAnimation(rotateAnimation);
            ivRefresh.startAnimation(animationSet);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("加载中...");
            tvLoading.setOnClickListener(null);
            mPresenter.getBookShelfInfo();
        });

        ivCover.setOnClickListener(view -> {
            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                BookInfoActivity.startThis(this, mPresenter.getBookShelf().getNoteUrl());
            }
        });

        tvAuthor.setOnClickListener(view -> {
            if (TextUtils.isEmpty(author)) return;
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, author);
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, author);
            }
            finish();
        });

        rgBookGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            View checkView = radioGroup.findViewById(i);
            if (!checkView.isPressed()) {
                return;
            }
            int idx = radioGroup.indexOfChild(checkView) % BOOK_GROUPS.length;
            mPresenter.getBookShelf().setGroup(idx);
            if (mPresenter.getInBookShelf()) {
                mPresenter.addToBookShelf();
            }
        });
    }

}