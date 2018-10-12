package com.monke.monkeybook.view.activity;

import android.content.Context;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

public class BookInfoActivity extends MBaseActivity {
    private final int ResultSelectCover = 103;

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
    @BindView(R.id.tv_select_cover)
    TextView tvSelectCover;
    @BindView(R.id.tv_change_cover)
    TextView tvChangeCover;
    @BindView(R.id.tv_refresh_cover)
    TextView tvRefreshCover;
    @BindView(R.id.tie_book_jj)
    TextInputEditText tieBookJj;
    @BindView(R.id.til_book_jj)
    TextInputLayout tilBookJj;

    private String noteUrl;
    private BookShelfBean book;
    private MoProgressHUD moProgressHUD;


    public static void startThis(Context context, String noteUrl) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        intent.putExtra("noteUrl", noteUrl);
        context.startActivity(intent);
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
        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString("noteUrl"))) {
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
        tilBookJj.setHint("简介");
        moProgressHUD = new MoProgressHUD(this);
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
                tieBookJj.setText(book.getBookInfoBean().getIntroduce());
                if (TextUtils.isEmpty(book.getCustomCoverPath())) {
                    tieCoverUrl.setText(book.getBookInfoBean().getCoverUrl());
                } else {
                    tieCoverUrl.setText(book.getCustomCoverPath());
                }
            }
            initCover();
        }
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        super.bindEvent();
        tvSelectCover.setOnClickListener(view -> {
            if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, ResultSelectCover);
            } else {
                EasyPermissions.requestPermissions(this, "获取背景图片需存储权限", MApplication.RESULT__PERMS, MApplication.PerList);
            }
        });
        tvChangeCover.setOnClickListener(view ->
                moProgressHUD.showChangeSource(this, book, searchBookBean -> {
                    tieCoverUrl.setText(searchBookBean.getCoverUrl());
                    book.setCustomCoverPath(tieCoverUrl.getText().toString());
                    initCover();
                }));
        tvRefreshCover.setOnClickListener(view -> {
            book.setCustomCoverPath(tieCoverUrl.getText().toString());
            initCover();
        });
    }

    private void initCover() {
        if (!this.isFinishing() && book != null) {
            if (TextUtils.isEmpty(book.getCustomCoverPath())) {
                Glide.with(this).load(book.getBookInfoBean().getCoverUrl())
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
            actionBar.setTitle(R.string.book_info);
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
        book.getBookInfoBean().setName(tieBookName.getText().toString());
        book.getBookInfoBean().setAuthor(tieBookAuthor.getText().toString());
        book.getBookInfoBean().setIntroduce(tieBookJj.getText().toString());
        book.setCustomCoverPath(tieCoverUrl.getText().toString());
        initCover();
        BookshelfHelp.saveBookToShelf(book);
        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, book);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ResultSelectCover:
                if (resultCode == RESULT_OK && null != data) {
                    tieCoverUrl.setText(FileUtil.getPath(this, data.getData()));
                    book.setCustomCoverPath(tieCoverUrl.getText().toString());
                    initCover();
                }
                break;
        }
    }
}
