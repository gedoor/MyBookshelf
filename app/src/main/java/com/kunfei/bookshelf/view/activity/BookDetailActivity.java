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
import android.widget.PopupMenu;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
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
import com.kunfei.bookshelf.databinding.ActivityBookDetailBinding;
import com.kunfei.bookshelf.help.BlurTransformation;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ImageLoader;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.BookDetailContract;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.widget.modialog.ChangeSourceDialog;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.io.File;
import java.io.FileOutputStream;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

import static com.kunfei.bookshelf.presenter.BookDetailPresenter.FROM_BOOKSHELF;

public class BookDetailActivity extends MBaseActivity<BookDetailContract.Presenter> implements BookDetailContract.View {
    private ActivityBookDetailBinding binding;
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
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        //弹窗
        moDialogHUD = new MoDialogHUD(this);
        binding.tvIntro.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            updateView();
        } else {
            if (mPresenter.getSearchBook() == null) return;
            SearchBookBean searchBookBean = mPresenter.getSearchBook();
            upImageView(searchBookBean.getCoverUrl(), searchBookBean.getName(), searchBookBean.getAuthor());
            binding.tvName.setText(searchBookBean.getName());
            author = searchBookBean.getAuthor();
            binding.tvAuthor.setText(TextUtils.isEmpty(author) ? "未知" : author);
            bookUrl = searchBookBean.getNoteUrl();
            String origin = TextUtils.isEmpty(searchBookBean.getOrigin()) ? "未知" : searchBookBean.getOrigin();
            binding.tvOrigin.setText(origin);
            binding.tvChapter.setText(searchBookBean.getLastChapter());  // newest
            binding.tvIntro.setText(StringUtils.formatHtml2Intor(searchBookBean.getIntroduce()));
            binding.tvShelf.setText(R.string.add_to_shelf);
            binding.tvRead.setText(R.string.start_read);
            binding.tvRead.setOnClickListener(v -> {
                //放入书架
            });
            binding.tvIntro.setVisibility(View.INVISIBLE);
            binding.tvLoading.setVisibility(View.VISIBLE);
            binding.tvLoading.setText(R.string.loading);
            binding.tvLoading.setOnClickListener(null);
        }
    }

    @Override
    public void updateView() {
        bookShelfBean = mPresenter.getBookShelf();
        BookInfoBean bookInfoBean;
        if (null != bookShelfBean) {
            if (BookShelfBean.LOCAL_TAG.equals(bookShelfBean.getTag())) {
                binding.ivMenu.setVisibility(View.GONE);
            } else {
                binding.ivMenu.setVisibility(View.VISIBLE);
            }
            bookInfoBean = bookShelfBean.getBookInfoBean();
            binding.tvName.setText(bookInfoBean.getName());
            author = bookInfoBean.getAuthor();
            binding.tvAuthor.setText(TextUtils.isEmpty(author) ? "未知" : author);
            bookUrl = bookInfoBean.getNoteUrl();
            ((RadioButton) binding.rgBookGroup.getChildAt(bookShelfBean.getGroup())).setChecked(true);
            if (mPresenter.getInBookShelf()) {
                binding.tvChapter.setText(bookShelfBean.getDurChapterName()); // last
                binding.tvShelf.setText(R.string.remove_from_bookshelf);
                binding.tvRead.setText(R.string.continue_read);
                binding.tvShelf.setOnClickListener(v -> {
                    //从书架移出
                    mPresenter.removeFromBookShelf();
                });
            } else {
                if (!TextUtils.isEmpty(bookShelfBean.getLastChapterName())) {
                    binding.tvChapter.setText(bookShelfBean.getLastChapterName()); // last
                }
                binding.tvShelf.setText(R.string.add_to_shelf);
                binding.tvRead.setText(R.string.start_read);
                binding.tvShelf.setOnClickListener(v -> {
                    //放入书架
                    mPresenter.addToBookShelf();
                });
            }
            binding.tvIntro.setText(StringUtils.formatHtml2Intor(bookInfoBean.getIntroduce()));
            if (binding.tvIntro.getVisibility() != View.VISIBLE) {
                binding.tvIntro.setVisibility(View.VISIBLE);
            }
            String origin = bookInfoBean.getOrigin();
            if (!TextUtils.isEmpty(origin)) {
                binding.ivWeb.setVisibility(View.VISIBLE);
                binding.tvOrigin.setText(origin);
            } else {
                binding.ivWeb.setVisibility(View.INVISIBLE);
                binding.tvOrigin.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(bookShelfBean.getCustomCoverPath())) {
                upImageView(bookShelfBean.getCustomCoverPath(), bookInfoBean.getName(), bookInfoBean.getAuthor());
            } else {
                upImageView(bookInfoBean.getCoverUrl(), bookInfoBean.getName(), bookInfoBean.getAuthor());
            }
            if (bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                binding.tvChangeOrigin.setVisibility(View.INVISIBLE);
            } else {
                binding.tvChangeOrigin.setVisibility(View.VISIBLE);
            }
            upChapterSizeTv();
        }
        binding.tvLoading.setVisibility(View.GONE);
        binding.tvLoading.setOnClickListener(null);
    }

    @Override
    public void getBookShelfError() {
        binding.tvLoading.setVisibility(View.VISIBLE);
        binding.tvLoading.setText(R.string.load_error_retry);
        binding.tvLoading.setOnClickListener(v -> {
            binding.tvLoading.setText(R.string.loading);
            binding.tvLoading.setOnClickListener(null);
            mPresenter.getBookShelfInfo();
        });
    }

    private void upImageView(String path, String name, String author) {
        binding.ivCover.load(path, name, author);
        ImageLoader.INSTANCE.load(this, path)
                .transition(DrawableTransitionOptions.withCrossFade(1500))
                .thumbnail(defaultCover())
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)))
                .into(binding.ivBlurCover);  //模糊、渐变、缩小效果
    }

    private RequestBuilder<Drawable> defaultCover() {
        return ImageLoader.INSTANCE.load(this, R.drawable.image_cover_default)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)));
    }

    private void refresh() {
        binding.tvLoading.setVisibility(View.VISIBLE);
        binding.tvLoading.setText(R.string.loading);
        binding.tvLoading.setOnClickListener(null);
        mPresenter.getBookShelf().getBookInfoBean().setBookInfoHtml(null);
        mPresenter.getBookShelf().getBookInfoBean().setChapterListHtml(null);
        mPresenter.getBookShelfInfo();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void bindEvent() {
        binding.tvName.setOnClickListener(v -> {
            if (bookShelfBean == null) return;
            if (TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getName())) return;
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, bookShelfBean.getBookInfoBean().getName());
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, bookShelfBean.getBookInfoBean().getName());
            }
            finish();
        });
        binding.ivBlurCover.setOnClickListener(null);
        binding.iflContent.setOnClickListener(v -> finish());
        binding.tvToc.setOnClickListener(v -> {
            ChapterListActivity.startThis(this, mPresenter.getBookShelf(), mPresenter.getChapterList());
        });
        binding.tvChangeOrigin.setOnClickListener(view ->
                ChangeSourceDialog.builder(BookDetailActivity.this, mPresenter.getBookShelf())
                        .setCallback(searchBookBean -> {
                            binding.tvOrigin.setText(searchBookBean.getOrigin());
                            binding.tvLoading.setVisibility(View.VISIBLE);
                            binding.tvLoading.setText(R.string.loading);
                            binding.tvLoading.setOnClickListener(null);
                            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                                mPresenter.changeBookSource(searchBookBean);
                            } else {
                                mPresenter.initBookFormSearch(searchBookBean);
                                mPresenter.getBookShelfInfo();
                            }
                        }).show());

        binding.tvRead.setOnClickListener(v -> {
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

        binding.ivMenu.setOnClickListener(view -> {
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
            popupMenu.getMenu().add(Menu.NONE, R.id.menu_share, Menu.NONE, R.string.share_book);
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
                    case R.id.menu_share:
                        share();
                        break;
                }
                return true;
            });
            popupMenu.show();
        });

        binding.ivCover.setOnClickListener(view -> {
            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                BookInfoEditActivity.startThis(this, mPresenter.getBookShelf().getNoteUrl());
            }
        });

        binding.tvAuthor.setOnClickListener(view -> {
            if (TextUtils.isEmpty(author)) return;
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, author);
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, author);
            }
            finish();
        });

        binding.rgBookGroup.setOnCheckedChangeListener((radioGroup, i) -> {
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

    private void share() {

        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            // 使用url
            String url = mPresenter.getBookShelf().getNoteUrl();
            if (url == null)
                url = "";
            int maxLength = 1273 - 1 - url.length();

            BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(mPresenter.getBookShelf().getTag());

            if (sourceBean != null) {
//                    url=tvBookUrl.getText().toString()+"#"+ gson.toJson(sourceBean).replaceAll("\n\\s*\"[a-zA-Z]+\"(:\"\"|: \"\"| :\"\"| : \"\")\\s*,\\s*\n","\n").trim();
                url = url + "#" + sourceBean.getJson(maxLength);

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

                        try {
                            File file = new File(BookDetailActivity.this.getExternalCacheDir(), binding.tvName.getText().toString() + ".png");
                            FileOutputStream fOut = new FileOutputStream(file);
                            bitmap2.compress(Bitmap.CompressFormat.PNG, 80, fOut);
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
    }
}