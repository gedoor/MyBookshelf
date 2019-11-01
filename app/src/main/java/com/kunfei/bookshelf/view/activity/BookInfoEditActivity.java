package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.textfield.TextInputLayout;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.permission.Permissions;
import com.kunfei.bookshelf.help.permission.PermissionsCompat;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Unit;

public class BookInfoEditActivity extends MBaseActivity {
    private final int ResultSelectCover = 103;
    private final int ResultEditCover = 104;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_cover)
    ImageView ivCover;
    @BindView(R.id.tie_book_name)
    EditText tieBookName;
    @BindView(R.id.til_book_name)
    TextInputLayout tilBookName;
    @BindView(R.id.tie_book_author)
    EditText tieBookAuthor;
    @BindView(R.id.til_book_author)
    TextInputLayout tilBookAuthor;
    @BindView(R.id.tie_cover_url)
    EditText tieCoverUrl;
    @BindView(R.id.til_cover_url)
    TextInputLayout tilCoverUrl;
    @BindView(R.id.tv_select_cover)
    TextView tvSelectCover;
    @BindView(R.id.tv_change_cover)
    TextView tvChangeCover;
    @BindView(R.id.tv_refresh_cover)
    TextView tvRefreshCover;
    @BindView(R.id.tie_book_jj)
    EditText tieBookJj;
    @BindView(R.id.til_book_jj)
    TextInputLayout tilBookJj;

    private String noteUrl;
    private BookShelfBean book;
    private MoDialogHUD moDialogHUD;


    public static void startThis(Context context, String noteUrl) {
        Intent intent = new Intent(context, BookInfoEditActivity.class);
        intent.putExtra("noteUrl", noteUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("noteUrl", noteUrl);
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_book_info_edit);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        tilBookName.setHint(getString(R.string.book_name));
        tilBookAuthor.setHint(getString(R.string.author));
        tilCoverUrl.setHint(getString(R.string.cover_path));
        tilBookJj.setHint(getString(R.string.book_intro));
        moDialogHUD = new MoDialogHUD(this);
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
        tvSelectCover.setOnClickListener(view -> selectCover());
        tvChangeCover.setOnClickListener(view ->{
            Intent intent = new Intent(BookInfoEditActivity.this, BookCoverEditActivity.class);
            intent.putExtra("name",book.getBookInfoBean().getName());
            intent.putExtra("author",book.getBookInfoBean().getAuthor());
            startActivityForResult(intent, ResultEditCover);
                });
        tvRefreshCover.setOnClickListener(view -> {
            book.setCustomCoverPath(tieCoverUrl.getText().toString());
            initCover();
        });
    }

    private void selectCover() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.bg_image_per)
                .onGranted((requestCode) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, ResultSelectCover);
                    return Unit.INSTANCE;
                })
                .request();
    }

    private void initCover() {
        if (!this.isFinishing() && book != null) {
            if (TextUtils.isEmpty(book.getCustomCoverPath())) {
                Glide.with(this).load(book.getBookInfoBean().getCoverUrl())
                        .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop()
                        .placeholder(R.drawable.img_cover_default)
                        .into(ivCover);
            } else if (book.getCustomCoverPath().startsWith("http")) {
                Glide.with(this).load(book.getCustomCoverPath())
                        .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop()
                        .placeholder(R.drawable.img_cover_default)
                        .into(ivCover);
            } else {
                Glide.with(this).load(new File(book.getCustomCoverPath()))
                        .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop()
                        .placeholder(R.drawable.img_cover_default)
                        .into(ivCover);
            }
        }
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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
                SoftInputUtil.hideIMM(getCurrentFocus());
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveInfo() {
        if (book != null) {
            book.getBookInfoBean().setName(tieBookName.getText().toString());
            book.getBookInfoBean().setAuthor(tieBookAuthor.getText().toString());
            book.getBookInfoBean().setIntroduce(tieBookJj.getText().toString());
            book.setCustomCoverPath(tieCoverUrl.getText().toString());
            initCover();
            BookshelfHelp.saveBookToShelf(book);
            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, book);
            SoftInputUtil.hideIMM(getCurrentFocus());
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        moDialogHUD.dismiss();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ResultSelectCover:
                if (resultCode == RESULT_OK && null != data) {
                    tieCoverUrl.setText(FileUtils.getPath(this, data.getData()));
                    book.setCustomCoverPath(tieCoverUrl.getText().toString());
                    initCover();
                }
                break;
            case ResultEditCover:
                if (resultCode == RESULT_OK && null != data) {
                    String url = data.getStringExtra("url");
                    tieCoverUrl.setText(url);
                    book.setCustomCoverPath(url);
                    initCover();
                }
                break;
        }
    }
}
