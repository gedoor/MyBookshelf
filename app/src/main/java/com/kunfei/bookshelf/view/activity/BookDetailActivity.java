//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.AppActivityManager;
import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.BuildConfig;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BlurTransformation;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ImageLoader;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.BookDetailContract;
import com.kunfei.bookshelf.utils.BitmapUtil;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.widget.image.CoverImageView;
import com.kunfei.bookshelf.widget.modialog.ChangeSourceDialog;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

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
    @BindView(R.id.tv_share)
    TextView tvShare;
    @BindView(R.id.tv_book_url)
    TextView tvBookUrl;
    @BindView(R.id.book_info_main)
    View bookInfoMain;
    @BindView(R.id.book_info_btns)
    View bookInfoBtns;
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
    private String bookUrl;

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
            upImageView(searchBookBean.getCoverUrl(), searchBookBean.getName(), searchBookBean.getAuthor());
            tvName.setText(searchBookBean.getName());
            author = searchBookBean.getAuthor();
            tvAuthor.setText(TextUtils.isEmpty(author) ? "未知" : author);
            bookUrl = searchBookBean.getNoteUrl();
            tvBookUrl.setText(bookUrl);
//            bookInfoBtns.bringToFront();
            String origin = TextUtils.isEmpty(searchBookBean.getOrigin()) ? "未知" : searchBookBean.getOrigin();
            tvOrigin.setText(origin);
            tvChapter.setText(searchBookBean.getLastChapter());  // newest
            tvIntro.setText(StringUtils.formatHtml2Intor(searchBookBean.getIntroduce()));
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
            bookUrl = bookInfoBean.getNoteUrl();
            tvBookUrl.setText(bookUrl);
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
            tvIntro.setText(StringUtils.formatHtml2Intor(bookInfoBean.getIntroduce()));
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
                upImageView(bookShelfBean.getCustomCoverPath(), bookInfoBean.getName(), bookInfoBean.getAuthor());
            } else {
                upImageView(bookInfoBean.getCoverUrl(), bookInfoBean.getName(), bookInfoBean.getAuthor());
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

    private void upImageView(String path, String name, String author) {
        ivCover.load(path, name, author);
        ImageLoader.INSTANCE.load(this, path)
                .transition(DrawableTransitionOptions.withCrossFade(1500))
                .thumbnail(defaultCover())
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)))
                .into(ivBlurCover);  //模糊、渐变、缩小效果
    }

    private RequestBuilder<Drawable> defaultCover() {
        return ImageLoader.INSTANCE.load(this, R.drawable.image_cover_default)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)));
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


        tvShare.setOnClickListener(v -> {

            Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
                // 使用url
                String url = tvBookUrl.getText().toString();
                if (url == null)
                    url = "";
                int maxLength = 1273 - 1 - url.length();

                BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(mPresenter.getBookShelf().getTag());

                if (sourceBean != null) {
//                    url=tvBookUrl.getText().toString()+"#"+ gson.toJson(sourceBean).replaceAll("\n\\s*\"[a-zA-Z]+\"(:\"\"|: \"\"| :\"\"| : \"\")\\s*,\\s*\n","\n").trim();
                    url=url+"#"+sourceBean.getJson(maxLength);

                    Log.d("QRcode", "Length=" + url.length() + "\n" + url);
                    Bitmap bitmap;
                    QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                    if (url.length() > 300)
                        bitmap = QRCodeEncoder.syncEncodeQRCode(url, 800);
                    else if (url.length() > 100)
                        bitmap = QRCodeEncoder.syncEncodeQRCode(url, 500);
                    else
                        bitmap = QRCodeEncoder.syncEncodeQRCode(url, 300);
                    QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                    emitter.onSuccess(bitmap);
                }
            }).compose(RxUtils::toSimpleSingle)
                    .subscribe(new MySingleObserver<Bitmap>() {

                        @Override
                        public void onSuccess(Bitmap bitmap2) {
                            bookInfoBtns.setVisibility(View.GONE);
                            LinearLayout.LayoutParams layoutParams =
                                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            tvIntro.setLayoutParams(layoutParams);
//                            updateView();

                            bookInfoMain.measure(
                                    View.MeasureSpec.makeMeasureSpec(960, View.MeasureSpec.AT_MOST),
                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                            bookInfoMain.layout(0, 0, bookInfoMain.getMeasuredWidth(), bookInfoMain.getMeasuredHeight());
                            bookInfoMain.buildDrawingCache();
                            Bitmap bitmap = bookInfoMain.getDrawingCache();
                            bookInfoBtns.setVisibility(View.VISIBLE);
                            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 160);
                            tvIntro.setLayoutParams(layoutParams);
                            //假如图片不符合要求，可以使用Bitmap.createBitmap( )方法处理图片

//            BitmapUtil.localshare(this.getApplication(),bitmap,tvName.getText().toString());
                            if (bitmap == null) {
                                toast("生成照片失败");
                                return;
                            }

                            if (bitmap2 != null)
                                bitmap = BitmapUtil.addBitmap(bitmap, bitmap2, 20, 0, 0, 60);

                            try {
                                File file = new File(BookDetailActivity.this.getExternalCacheDir(), tvName.getText().toString() + ".png");
                                FileOutputStream fOut = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 80, fOut);
                                fOut.flush();
                                fOut.close();
                                //noinspection ResultOfMethodCallIgnored
                                file.setReadable(true, false);
                                Uri contentUri = FileProvider.getUriForFile(BookDetailActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
                                final Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                intent.setType("image/png");
                                startActivity(Intent.createChooser(intent, "分享书籍"));
                            } catch (Exception e) {
                                toast(e.getLocalizedMessage());
                            }
                        }
                    });


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
            if (!mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
                popupMenu.getMenu().add(Menu.NONE, R.id.menu_copy_url, Menu.NONE, R.string.copy_url);
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
                    case R.id.menu_copy_url:
                        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(null, mPresenter.getBookShelf().getNoteUrl());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clipData);
                            toast(R.string.copy_complete);
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