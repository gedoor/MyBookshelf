package com.monke.monkeybook.view.impl;

import android.content.Intent;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.presenter.IBookDetailPresenter;
import com.monke.monkeybook.presenter.impl.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.impl.ReadBookPresenterImpl;
import com.monke.monkeybook.utils.BlurTransformation;
import com.monke.monkeybook.view.IBookDetailView;

public class BookDetailActivity extends MBaseActivity<IBookDetailPresenter> implements IBookDetailView {

    private FrameLayout iflContent;
    private ImageView ivBlurCover;
    private ImageView ivCover;
    private TextView tvName;
    private TextView tvAuthor;
    private TextView tvOrigin;
    private TextView tvChapter;
    private TextView tvIntro;
    private TextView tvShelf;
    private TextView tvRead;
    private TextView tvLoading;

    private Animation animHideLoading;
    private Animation animShowInfo;

    @Override
    protected IBookDetailPresenter initInjector() {
        return new BookDetailPresenterImpl(getIntent());
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_detail);
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
        iflContent = (FrameLayout) findViewById(R.id.ifl_content);
        ivBlurCover = (ImageView) findViewById(R.id.iv_blur_cover);
        ivCover = (ImageView) findViewById(R.id.iv_cover);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvAuthor = (TextView) findViewById(R.id.tv_author);
        tvOrigin = (TextView) findViewById(R.id.tv_origin);
        tvChapter = (TextView) findViewById(R.id.tv_chapter);
        tvIntro = (TextView) findViewById(R.id.tv_intro);
        tvShelf = (TextView) findViewById(R.id.tv_shelf);
        tvRead = (TextView) findViewById(R.id.tv_read);
        tvLoading = (TextView) findViewById(R.id.tv_loading);

        tvIntro.setMovementMethod(ScrollingMovementMethod.getInstance());
        initView();

        updateView();
    }

    @Override
    public void updateView() {
        if (null != mPresenter.getBookShelf()) {
            if (mPresenter.getInBookShelf()) {
                if (mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size() > 0)
                    tvChapter.setText(String.format(getString(R.string.tv_read_durprogress), mPresenter.getBookShelf().getBookInfoBean().getChapterlist().get(mPresenter.getBookShelf().getDurChapter()).getDurChapterName()));
                else
                    tvChapter.setText("无章节");
                tvShelf.setText("移出书架");
                tvRead.setText("继续阅读");
                tvShelf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //从书架移出
                        mPresenter.removeFromBookShelf();
                    }
                });
            } else {
                if (mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size() == 0) {
                    tvChapter.setText("无章节");
                } else {
                    tvChapter.setText(String.format(getString(R.string.tv_searchbook_lastest), mPresenter.getBookShelf().getBookInfoBean().getChapterlist().get(mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size() - 1).getDurChapterName()));
                }
                tvShelf.setText("放入书架");
                tvRead.setText("开始阅读");
                tvShelf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //放入书架
                        mPresenter.addToBookShelf();
                    }
                });
            }
            if (tvIntro.getText().toString().trim().length() == 0) {
                tvIntro.setText(mPresenter.getBookShelf().getBookInfoBean().getIntroduce());
            }
            if (tvIntro.getVisibility() != View.VISIBLE) {
                tvIntro.setVisibility(View.VISIBLE);
                tvIntro.startAnimation(animShowInfo);
                tvLoading.startAnimation(animHideLoading);
            }
            if (mPresenter.getBookShelf().getBookInfoBean().getOrigin() != null && mPresenter.getBookShelf().getBookInfoBean().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText("来源:" + mPresenter.getBookShelf().getBookInfoBean().getOrigin());
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
        } else {
            tvChapter.setText(String.format(getString(R.string.tv_searchbook_lastest), mPresenter.getSearchBook().getLastChapter()));
            tvShelf.setText("放入书架");
            tvRead.setText("开始阅读");
            tvRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //放入书架
                }
            });
            tvIntro.setVisibility(View.INVISIBLE);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("加载中...");
        }
        tvLoading.setOnClickListener(null);
    }

    @Override
    public void getBookShelfError() {
        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("加载失败,点击重试");
        tvLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLoading.setText("加载中...");
                tvLoading.setOnClickListener(null);
                mPresenter.getBookShelfInfo();
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        if (mPresenter.getOpenfrom() == BookDetailPresenterImpl.FROM_SEARCH && mPresenter.getBookShelf() == null) {
            //网络请求
            mPresenter.getBookShelfInfo();
        }
    }

    private void initView() {
        String coverUrl;
        String name;
        String author;
        if (mPresenter.getOpenfrom() == BookDetailPresenterImpl.FROM_BOOKSHELF) {
            coverUrl = mPresenter.getBookShelf().getBookInfoBean().getCoverUrl();
            name = mPresenter.getBookShelf().getBookInfoBean().getName();
            author = mPresenter.getBookShelf().getBookInfoBean().getAuthor();
            if (mPresenter.getBookShelf().getBookInfoBean().getOrigin() != null && mPresenter.getBookShelf().getBookInfoBean().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText("来源:" + mPresenter.getBookShelf().getBookInfoBean().getOrigin());
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
        } else {
            coverUrl = mPresenter.getSearchBook().getCoverUrl();
            name = mPresenter.getSearchBook().getName();
            author = mPresenter.getSearchBook().getAuthor();
            if (mPresenter.getSearchBook().getOrigin() != null && mPresenter.getSearchBook().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText("来源:" + mPresenter.getSearchBook().getOrigin());
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
        }

        Glide.with(this).load(coverUrl).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(ivCover);
        Glide.with(this).load(coverUrl).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().bitmapTransform(new BlurTransformation(this, 6)).into(ivBlurCover);
        tvName.setText(name);
        tvAuthor.setText(author);
    }

    @Override
    protected void bindEvent() {
        iflContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        tvRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
    }
}
