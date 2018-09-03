package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookInfoActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_cover)
    ImageView ivCover;
    @BindView(R.id.tie_book_name)
    TextInputEditText tieBookName;
    @BindView(R.id.til_book_name)
    TextInputLayout tilBookName;
    @BindView(R.id.tie_book_author)
    TextInputEditText tieBookAuthor;
    @BindView(R.id.til_book_author)
    TextInputLayout tilBookAuthor;
    @BindView(R.id.tie_cover_url)
    TextInputEditText tieCoverUrl;
    @BindView(R.id.til_cover_url)
    TextInputLayout tilCoverUrl;

    private String noteUrl;
    private BookShelfBean book;


    public static void startThis(String noteUrl) {
        Intent intent = new Intent(MApplication.getInstance(), BookInfoActivity.class);
        intent.putExtra("noteUrl", noteUrl);
        MApplication.getInstance().startActivity(intent);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TextUtils.isEmpty(savedInstanceState.getString("noteUrl"))) {
            noteUrl = savedInstanceState.getString("noteUrl");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("noteUrl", noteUrl);
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_info);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        tilBookName.setHint("书名");
        tilBookAuthor.setHint("作者");
        tilCoverUrl.setHint("封面地址");
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        if (!TextUtils.isEmpty(getIntent().getStringExtra("noteUrl"))) {
            noteUrl = getIntent().getStringExtra("noteUrl");
        }
        if (!TextUtils.isEmpty(noteUrl)) {
            book = BookshelfHelp.getBook(noteUrl);
            if (book != null) {
                tieBookName.setText(book.getBookInfoBean().getName());
                tieBookAuthor.setText(book.getBookInfoBean().getAuthor());
                if (TextUtils.isEmpty(book.getCustomCoverPath())) {
                    tieCoverUrl.setText(book.getBookInfoBean().getCoverUrl());
                } else {
                    tieCoverUrl.setText(book.getCustomCoverPath());
                }
            }
            initCover();
        }
    }

    private void initCover() {
        if (!this.isFinishing() && book != null) {
            if (TextUtils.isEmpty(book.getCustomCoverPath())) {
                Glide.with(this).load(book.getBookInfoBean().getChapterUrl())
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                                .placeholder(R.drawable.img_cover_default)).into(ivCover);
            } else if (book.getCustomCoverPath().startsWith("http")) {
                Glide.with(this).load(book.getCustomCoverPath())
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                                .placeholder(R.drawable.img_cover_default)).into(ivCover);
            } else {
                ivCover.setImageBitmap(BitmapFactory.decodeFile(book.getCustomCoverPath()));
            }
        }
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.book_source_manage);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveInfo();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveInfo() {
        BookshelfHelp.saveBookToShelf(book);
    }
}
