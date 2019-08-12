//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.AppActivityManager;
import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BlurTransformation;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.BookDetailContract;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.widget.CoverImageView;
import com.kunfei.bookshelf.widget.modialog.ChangeSourceDialog;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kunfei.bookshelf.presenter.BookDetailPresenter.FROM_BOOKSHELF;

public class BookDetailActivity extends MBaseActivity<BookDetailContract.Presenter> implements BookDetailContract.View {
    @BindView(R.id.ifl_content)
    View vwContent;
    @BindView(R.id.iv_menu)
    ImageView ivMenu;
    @BindView(R.id.iv_blur_cover)
    AppCompatImageView ivBlurCover;
    @BindView(R.id.iv_cover)
    CoverImageView ivCover;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_author)
    TextView tvAuthor;
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
    @BindView(R.id.tv_change_origin)
    TextView tvChangeOrigin;
    @BindView(R.id.rg_book_group)
    RadioGroup rgBookGroup;
    @BindView(R.id.tv_chapter_size)
    TextView tvChapterSize;

    private MoDialogHUD moDialogHUD;
    private String author;
    private BookShelfBean bookShelfBean;
    private String coverPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected BookDetailContract.Presenter initInjector() {
        return new BookDetailPresenter();
    }

    @Override
    protected void onCreateActivity() {
        setTheme(R.style.CAppTransparentTheme);
        setContentView(R.layout.activity_book_detail);
    }

    @Override
    protected void initData() {
        mPresenter.initData(getIntent());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String dataKey = String.valueOf(System.currentTimeMillis());
        getIntent().putExtra("openFrom", FROM_BOOKSHELF);
        getIntent().putExtra("data_key", dataKey);
        BitIntentDataManager.getInstance().putData(dataKey, mPresenter.getBookShelf());
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        //弹窗
        moDialogHUD = new MoDialogHUD(this);
        tvIntro.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            updateView();
        } else {
            if (mPresenter.getSearchBook() == null) return;
            SearchBookBean searchBookBean = mPresenter.getSearchBook();
            upImageView(searchBookBean.getCoverUrl());
            tvName.setText(searchBookBean.getName());
            author = searchBookBean.getAuthor();
            tvAuthor.setText(TextUtils.isEmpty(author) ? "未知" : author);
            String origin = TextUtils.isEmpty(searchBookBean.getOrigin()) ? "未知" : searchBookBean.getOrigin();
            tvOrigin.setText(origin);
            tvChapter.setText(searchBookBean.getLastChapter());  // newest
            tvIntro.setText(StringUtils.formatHtml(searchBookBean.getIntroduce()));
            tvShelf.setText(R.string.add_to_shelf);
            tvRead.setText(R.string.start_read);
            tvRead.setOnClickListener(v -> {
                //放入书架
            });
            tvIntro.setVisibility(View.INVISIBLE);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText(R.string.loading);
            tvLoading.setOnClickListener(null);
        }
    }

    @Override
    public void updateView() {
        bookShelfBean = mPresenter.getBookShelf();
        BookInfoBean bookInfoBean;
        if (null != bookShelfBean) {
            if (BookShelfBean.LOCAL_TAG.equals(bookShelfBean.getTag())) {
                ivMenu.setVisibility(View.GONE);
            } else {
                ivMenu.setVisibility(View.VISIBLE);
            }
            bookInfoBean = bookShelfBean.getBookInfoBean();
            tvName.setText(bookInfoBean.getName());
            author = bookInfoBean.getAuthor();
            tvAuthor.setText(TextUtils.isEmpty(author) ? "未知" : author);
            ((RadioButton) rgBookGroup.getChildAt(bookShelfBean.getGroup())).setChecked(true);
            if (mPresenter.getInBookShelf()) {
                tvChapter.setText(bookShelfBean.getDurChapterName()); // last
                tvShelf.setText(R.string.remove_from_bookshelf);
                tvRead.setText(R.string.continue_read);
                tvShelf.setOnClickListener(v -> {
                    //从书架移出
                    mPresenter.removeFromBookShelf();
                });
            } else {
                if (!TextUtils.isEmpty(bookShelfBean.getLastChapterName())) {
                    tvChapter.setText(bookShelfBean.getLastChapterName()); // last
                }
                tvShelf.setText(R.string.add_to_shelf);
                tvRead.setText(R.string.start_read);
                tvShelf.setOnClickListener(v -> {
                    //放入书架
                    mPresenter.addToBookShelf();
                });
            }
            tvIntro.setText(StringUtils.formatHtml(bookInfoBean.getIntroduce()));
            if (tvIntro.getVisibility() != View.VISIBLE) {
                tvIntro.setVisibility(View.VISIBLE);
            }
            String origin = bookInfoBean.getOrigin();
            if (!TextUtils.isEmpty(origin)) {
                ivWeb.setVisibility(View.VISIBLE);
                tvOrigin.setText(origin);
            } else {
                ivWeb.setVisibility(View.INVISIBLE);
                tvOrigin.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(bookShelfBean.getCustomCoverPath())) {
                upImageView(bookShelfBean.getCustomCoverPath());
            } else {
                upImageView(bookInfoBean.getCoverUrl());
            }
            if (bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                tvChangeOrigin.setVisibility(View.INVISIBLE);
            } else {
                tvChangeOrigin.setVisibility(View.VISIBLE);
            }
            upChapterSizeTv();
        }
        tvLoading.setVisibility(View.GONE);
        tvLoading.setOnClickListener(null);
    }

    @Override
    public void getBookShelfError() {
        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText(R.string.load_error_retry);
        tvLoading.setOnClickListener(v -> {
            tvLoading.setText(R.string.loading);
            tvLoading.setOnClickListener(null);
            mPresenter.getBookShelfInfo();
        });
    }

    private void upImageView(String path) {
        if (TextUtils.isEmpty(path)) return;
        if (Objects.equals(coverPath, path)) return;
        if (this.isFinishing()) return;
        coverPath = path;
        if (coverPath.startsWith("http")) {
            Glide.with(this).load(coverPath)
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .placeholder(R.drawable.img_cover_default)
                    .into(ivCover);
            Glide.with(this).load(coverPath)
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .placeholder(R.drawable.img_cover_gs)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)))
                    .into(ivBlurCover);
        } else {
            File file = new File(coverPath);
            Glide.with(this).load(file)
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .placeholder(R.drawable.img_cover_default)
                    .into(ivCover);
            Glide.with(this).load(file)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .placeholder(R.drawable.img_cover_gs)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)))
                    .into(ivBlurCover);
        }
    }

    private void refresh() {
        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText(R.string.loading);
        tvLoading.setOnClickListener(null);
        mPresenter.getBookShelf().getBookInfoBean().setBookInfoHtml(null);
        mPresenter.getBookShelf().getBookInfoBean().setChapterListHtml(null);
        mPresenter.getBookShelfInfo();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void bindEvent() {
        tvName.setOnClickListener(v -> {
            if (bookShelfBean == null) return;
            if (TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getName())) return;
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, bookShelfBean.getBookInfoBean().getName());
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, bookShelfBean.getBookInfoBean().getName());
            }
            finish();
        });
        ivBlurCover.setOnClickListener(null);
        vwContent.setOnClickListener(v -> finish());

        tvChangeOrigin.setOnClickListener(view ->
                ChangeSourceDialog.builder(BookDetailActivity.this, mPresenter.getBookShelf())
                        .setCallback(searchBookBean -> {
                            tvOrigin.setText(searchBookBean.getOrigin());
                            tvLoading.setVisibility(View.VISIBLE);
                            tvLoading.setText(R.string.loading);
                            tvLoading.setOnClickListener(null);
                            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                                mPresenter.changeBookSource(searchBookBean);
                            } else {
                                mPresenter.initBookFormSearch(searchBookBean);
                                mPresenter.getBookShelfInfo();
                            }
                        }).show());

        tvRead.setOnClickListener(v -> {
            if (!mPresenter.getInBookShelf()) {
                BookshelfHelp.saveBookToShelf(mPresenter.getBookShelf());
                if (mPresenter.getChapterList() != null)
                    DbHelper.getDaoSession().getBookChapterBeanDao().insertOrReplaceInTx(mPresenter.getChapterList());
            }
            Intent intent = new Intent(BookDetailActivity.this, ReadBookActivity.class);
            intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
            intent.putExtra("inBookshelf", mPresenter.getInBookShelf());
            String key = String.valueOf(System.currentTimeMillis());
            String bookKey = "book" + key;
            intent.putExtra("bookKey", bookKey);
            BitIntentDataManager.getInstance().putData(bookKey, mPresenter.getBookShelf().clone());
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

        ivMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
            if (!mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
                popupMenu.getMenu().add(Menu.NONE, R.id.menu_refresh, Menu.NONE, R.string.refresh);
            }
            if (mPresenter.getInBookShelf() && !mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
                if (mPresenter.getBookShelf().getAllowUpdate()) {
                    popupMenu.getMenu().add(Menu.NONE, R.id.menu_disable_update, Menu.NONE, R.string.disable_update);
                } else {
                    popupMenu.getMenu().add(Menu.NONE, R.id.menu_allow_update, Menu.NONE, R.string.allow_update);
                }
            }
            if (!mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
                popupMenu.getMenu().add(Menu.NONE, R.id.menu_edit, Menu.NONE, R.string.edit_book_source);
            }
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.menu_refresh:
                        refresh();
                        break;
                    case R.id.menu_allow_update:
                        mPresenter.getBookShelf().setAllowUpdate(true);
                        mPresenter.addToBookShelf();
                        break;
                    case R.id.menu_disable_update:
                        mPresenter.getBookShelf().setAllowUpdate(false);
                        mPresenter.addToBookShelf();
                        break;
                    case R.id.menu_edit:
                        BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(mPresenter.getBookShelf().getTag());
                        if (sourceBean != null) {
                            SourceEditActivity.startThis(this, sourceBean);
                        }
                        break;
                }
                return true;
            });
            popupMenu.show();
        });

        ivCover.setOnClickListener(view -> {
            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                BookInfoEditActivity.startThis(this, mPresenter.getBookShelf().getNoteUrl());
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
            int idx = radioGroup.indexOfChild(checkView) % (getResources().getStringArray(R.array.book_group_array).length - 1);
            mPresenter.getBookShelf().setGroup(idx);
            if (mPresenter.getInBookShelf()) {
                mPresenter.addToBookShelf();
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        if (mPresenter.getOpenFrom() == BookDetailPresenter.FROM_SEARCH) {
            //网络请求
            mPresenter.getBookShelfInfo();
        }
    }

    @SuppressLint("DefaultLocale")
    private void upChapterSizeTv() {
        String chapterSize = "";
        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF && bookShelfBean.getChapterListSize() > 0) {
            int newChapterNum = bookShelfBean.getChapterListSize() - 1 - bookShelfBean.getDurChapter();
            if (newChapterNum > 0)
                chapterSize = String.format("(+%d)", newChapterNum);
        }
        tvChapterSize.setText(chapterSize);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public void onDestroy() {
        moDialogHUD.dismiss();
        super.onDestroy();
    }
}