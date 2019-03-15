package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.kunfei.bookshelf.BitIntentDataManager;
import com.kunfei.bookshelf.BuildConfig;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.SourceEditPresenter;
import com.kunfei.bookshelf.presenter.contract.SourceEditContract;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.popupwindow.KeyboardToolPop;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/26.
 * 编辑书源
 */

public class SourceEditActivity extends MBaseActivity<SourceEditContract.Presenter> implements SourceEditContract.View {
    public final static int EDIT_SOURCE = 1101;
    private final int REQUEST_QR = 202;

    @BindView(R.id.action_bar)
    AppBarLayout actionBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tie_book_source_url)
    AppCompatEditText tieBookSourceUrl;
    @BindView(R.id.til_book_source_url)
    TextInputLayout tilBookSourceUrl;
    @BindView(R.id.tie_book_source_name)
    AppCompatEditText tieBookSourceName;
    @BindView(R.id.til_book_source_name)
    TextInputLayout tilBookSourceName;
    @BindView(R.id.tie_ruleSearchUrl)
    AppCompatEditText tieRuleSearchUrl;
    @BindView(R.id.til_ruleSearchUrl)
    TextInputLayout tilRuleSearchUrl;
    @BindView(R.id.tie_ruleSearchList)
    AppCompatEditText tieRuleSearchList;
    @BindView(R.id.til_ruleSearchList)
    TextInputLayout tilRuleSearchList;
    @BindView(R.id.tie_ruleSearchName)
    AppCompatEditText tieRuleSearchName;
    @BindView(R.id.til_ruleSearchName)
    TextInputLayout tilRuleSearchName;
    @BindView(R.id.tie_ruleSearchAuthor)
    AppCompatEditText tieRuleSearchAuthor;
    @BindView(R.id.til_ruleSearchAuthor)
    TextInputLayout tilRuleSearchAuthor;
    @BindView(R.id.tie_ruleSearchKind)
    AppCompatEditText tieRuleSearchKind;
    @BindView(R.id.til_ruleSearchKind)
    TextInputLayout tilRuleSearchKind;
    @BindView(R.id.tie_ruleSearchLastChapter)
    AppCompatEditText tieRuleSearchLastChapter;
    @BindView(R.id.til_ruleSearchLastChapter)
    TextInputLayout tilRuleSearchLastChapter;
    @BindView(R.id.tie_ruleSearchCoverUrl)
    AppCompatEditText tieRuleSearchCoverUrl;
    @BindView(R.id.til_ruleSearchCoverUrl)
    TextInputLayout tilRuleSearchCoverUrl;
    @BindView(R.id.tie_ruleSearchNoteUrl)
    AppCompatEditText tieRuleSearchNoteUrl;
    @BindView(R.id.til_ruleSearchNoteUrl)
    TextInputLayout tilRuleSearchNoteUrl;
    @BindView(R.id.tie_ruleBookName)
    AppCompatEditText tieRuleBookName;
    @BindView(R.id.til_ruleBookName)
    TextInputLayout tilRuleBookName;
    @BindView(R.id.tie_ruleBookAuthor)
    AppCompatEditText tieRuleBookAuthor;
    @BindView(R.id.til_ruleBookAuthor)
    TextInputLayout tilRuleBookAuthor;
    @BindView(R.id.tie_ruleCoverUrl)
    AppCompatEditText tieRuleCoverUrl;
    @BindView(R.id.til_ruleCoverUrl)
    TextInputLayout tilRuleCoverUrl;
    @BindView(R.id.tie_ruleChapterListUrl)
    AppCompatEditText tieRuleChapterListUrl;
    @BindView(R.id.til_ruleChapterListUrl)
    TextInputLayout tilRuleChapterListUrl;
    @BindView(R.id.tie_ruleIntroduce)
    AppCompatEditText tieRuleIntroduce;
    @BindView(R.id.til_ruleIntroduce)
    TextInputLayout tilRuleIntroduce;
    @BindView(R.id.tie_ruleChapterList)
    AppCompatEditText tieRuleChapterList;
    @BindView(R.id.til_ruleChapterList)
    TextInputLayout tilRuleChapterList;
    @BindView(R.id.tie_ruleChapterName)
    AppCompatEditText tieRuleChapterName;
    @BindView(R.id.til_ruleChapterName)
    TextInputLayout tilRuleChapterName;
    @BindView(R.id.tie_ruleContentUrl)
    AppCompatEditText tieRuleContentUrl;
    @BindView(R.id.til_ruleContentUrl)
    TextInputLayout tilRuleContentUrl;
    @BindView(R.id.tie_ruleBookContent)
    AppCompatEditText tieRuleBookContent;
    @BindView(R.id.til_ruleBookContent)
    TextInputLayout tilRuleBookContent;
    @BindView(R.id.tie_httpUserAgent)
    AppCompatEditText tieHttpUserAgent;
    @BindView(R.id.til_httpUserAgent)
    TextInputLayout tilHttpUserAgent;
    @BindView(R.id.tie_ruleFindUrl)
    AppCompatEditText tieRuleFindUrl;
    @BindView(R.id.til_ruleFindUrl)
    TextInputLayout tilRuleFindUrl;
    @BindView(R.id.tie_bookSourceGroup)
    AppCompatEditText tieBookSourceGroup;
    @BindView(R.id.til_bookSourceGroup)
    TextInputLayout tilBookSourceGroup;
    @BindView(R.id.tie_ruleChapterListUrlNext)
    AppCompatEditText tieRuleChapterListUrlNext;
    @BindView(R.id.til_ruleChapterListUrlNext)
    TextInputLayout tilRuleChapterListUrlNext;
    @BindView(R.id.tie_ruleContentUrlNext)
    AppCompatEditText tieRuleContentUrlNext;
    @BindView(R.id.til_ruleContentUrlNext)
    TextInputLayout tilRuleContentUrlNext;
    @BindView(R.id.tie_ruleBookUrlPattern)
    AppCompatEditText tieRuleBookUrlPattern;
    @BindView(R.id.til_ruleBookUrlPattern)
    TextInputLayout tilRuleBookUrlPattern;
    @BindView(R.id.tie_ruleBookKind)
    AppCompatEditText tieRuleBookKind;
    @BindView(R.id.til_ruleBookKind)
    TextInputLayout tilRuleBookKind;
    @BindView(R.id.tie_ruleBookLastChapter)
    AppCompatEditText tieRuleBookLastChapter;
    @BindView(R.id.til_ruleBookLastChapter)
    TextInputLayout tilRuleBookLastChapter;
    @BindView(R.id.tie_loginUrl)
    AppCompatEditText tieLoginUrl;
    @BindView(R.id.til_loginUrl)
    TextInputLayout tilLoginUrl;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    private BookSourceBean bookSourceBean;
    private int serialNumber;
    private boolean enable;
    private String title;
    private PopupWindow mSoftKeyboardTool;
    private boolean mIsSoftKeyBoardShowing = false;

    public static void startThis(Activity activity, BookSourceBean sourceBean) {
        Intent intent = new Intent(activity, SourceEditActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        try {
            BitIntentDataManager.getInstance().putData(key, sourceBean.clone());
        } catch (CloneNotSupportedException e) {
            BitIntentDataManager.getInstance().putData(key, sourceBean);
            e.printStackTrace();
        }
        activity.startActivityForResult(intent, EDIT_SOURCE);
    }

    @Override
    protected SourceEditContract.Presenter initInjector() {
        return new SourceEditPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title");
            serialNumber = savedInstanceState.getInt("serialNumber");
            enable = savedInstanceState.getBoolean("enable");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putInt("serialNumber", serialNumber);
        outState.putBoolean("enable", enable);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_source_edit);
    }

    @Override
    protected void initData() {
        String key = this.getIntent().getStringExtra("data_key");
        if (title == null) {
            if (isEmpty(key)) {
                title = getString(R.string.add_book_source);
            } else {
                title = getString(R.string.edit_book_source);
                bookSourceBean = (BookSourceBean) BitIntentDataManager.getInstance().getData(key);
                serialNumber = bookSourceBean.getSerialNumber();
                enable = bookSourceBean.getEnable();
                BitIntentDataManager.getInstance().cleanData(key);
            }
        }
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();

        setHint();
        setText(bookSourceBean);
        mSoftKeyboardTool = new KeyboardToolPop(this, this::insertTextToEditText);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new KeyboardOnGlobalChangeListener());
    }

    private boolean canSaveBookSource() {
        if (isEmpty(trim(tieBookSourceName.getText())) || isEmpty(trim(tieBookSourceUrl.getText()))) {
            toast(R.string.non_null_source_name_url, ERROR);
            return false;
        }
        return true;
    }

    @Override
    public String getBookSourceStr() {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        return gson.toJson(getBookSource());
    }

    private void scanBookSource() {
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(intent, REQUEST_QR);
    }

    private String trim(Editable editable) {
        if (editable == null) {
            return null;
        }
        return StringUtils.trim(editable.toString());
    }

    private BookSourceBean getBookSource() {
        BookSourceBean bookSourceBeanN = new BookSourceBean();
        bookSourceBeanN.setBookSourceName(trim(tieBookSourceName.getText()));
        bookSourceBeanN.setBookSourceUrl(trim(tieBookSourceUrl.getText()));
        if (bookSourceBeanN.getBookSourceUrl().endsWith("/")) {
            tieBookSourceUrl.setText(bookSourceBeanN.getBookSourceUrl().replaceAll("/+$", ""));
            bookSourceBeanN.setBookSourceUrl(trim(tieBookSourceUrl.getText()));
        }
        bookSourceBeanN.setLoginUrl(trim(tieLoginUrl.getText()));
        bookSourceBeanN.setBookSourceGroup(trim(tieBookSourceGroup.getText()));
        bookSourceBeanN.setRuleBookAuthor(trim(tieRuleBookAuthor.getText()));
        bookSourceBeanN.setRuleBookContent(trim(tieRuleBookContent.getText()));
        bookSourceBeanN.setRuleBookName(trim(tieRuleBookName.getText()));
        bookSourceBeanN.setRuleChapterList(trim(tieRuleChapterList.getText()));
        bookSourceBeanN.setRuleChapterName(trim(tieRuleChapterName.getText()));
        bookSourceBeanN.setRuleChapterUrl(trim(tieRuleChapterListUrl.getText()));
        bookSourceBeanN.setRuleChapterUrlNext(trim(tieRuleChapterListUrlNext.getText()));
        bookSourceBeanN.setRuleContentUrl(trim(tieRuleContentUrl.getText()));
        bookSourceBeanN.setRuleCoverUrl(trim(tieRuleCoverUrl.getText()));
        bookSourceBeanN.setRuleIntroduce(trim(tieRuleIntroduce.getText()));
        bookSourceBeanN.setRuleSearchAuthor(trim(tieRuleSearchAuthor.getText()));
        bookSourceBeanN.setRuleSearchCoverUrl(trim(tieRuleSearchCoverUrl.getText()));
        bookSourceBeanN.setRuleSearchKind(trim(tieRuleSearchKind.getText()));
        bookSourceBeanN.setRuleSearchLastChapter(trim(tieRuleSearchLastChapter.getText()));
        bookSourceBeanN.setRuleSearchList(trim(tieRuleSearchList.getText()));
        bookSourceBeanN.setRuleSearchName(trim(tieRuleSearchName.getText()));
        bookSourceBeanN.setRuleSearchNoteUrl(trim(tieRuleSearchNoteUrl.getText()));
        bookSourceBeanN.setRuleSearchUrl(trim(tieRuleSearchUrl.getText()));
        bookSourceBeanN.setHttpUserAgent(trim(tieHttpUserAgent.getText()));
        bookSourceBeanN.setRuleFindUrl(trim(tieRuleFindUrl.getText()));
        bookSourceBeanN.setRuleContentUrlNext(trim(tieRuleContentUrlNext.getText()));
        bookSourceBeanN.setRuleBookUrlPattern(trim(tieRuleBookUrlPattern.getText()));
        bookSourceBeanN.setRuleBookKind(trim(tieRuleBookKind.getText()));
        bookSourceBeanN.setRuleBookLastChapter(trim(tieRuleBookLastChapter.getText()));
        bookSourceBeanN.setEnable(enable);
        bookSourceBeanN.setSerialNumber(serialNumber);
        return bookSourceBeanN;
    }

    @Override
    public void setText(BookSourceBean bookSourceBean) {
        if (bookSourceBean == null) {
            return;
        }
        tieBookSourceName.setText(StringUtils.trim(bookSourceBean.getBookSourceName()));
        tieBookSourceUrl.setText(StringUtils.trim(bookSourceBean.getBookSourceUrl()));
        tieBookSourceGroup.setText(StringUtils.trim(bookSourceBean.getBookSourceGroup()));
        tieLoginUrl.setText(StringUtils.trim(bookSourceBean.getLoginUrl()));
        tieRuleBookAuthor.setText(StringUtils.trim(bookSourceBean.getRuleBookAuthor()));
        tieRuleBookContent.setText(StringUtils.trim(bookSourceBean.getRuleBookContent()));
        tieRuleBookName.setText(StringUtils.trim(bookSourceBean.getRuleBookName()));
        tieRuleChapterList.setText(StringUtils.trim(bookSourceBean.getRuleChapterList()));
        tieRuleChapterName.setText(StringUtils.trim(bookSourceBean.getRuleChapterName()));
        tieRuleChapterListUrl.setText(StringUtils.trim(bookSourceBean.getRuleChapterUrl()));
        tieRuleChapterListUrlNext.setText(StringUtils.trim(bookSourceBean.getRuleChapterUrlNext()));
        tieRuleContentUrl.setText(StringUtils.trim(bookSourceBean.getRuleContentUrl()));
        tieRuleCoverUrl.setText(StringUtils.trim(bookSourceBean.getRuleCoverUrl()));
        tieRuleIntroduce.setText(StringUtils.trim(bookSourceBean.getRuleIntroduce()));
        tieRuleSearchAuthor.setText(StringUtils.trim(bookSourceBean.getRuleSearchAuthor()));
        tieRuleSearchCoverUrl.setText(StringUtils.trim(bookSourceBean.getRuleSearchCoverUrl()));
        tieRuleSearchKind.setText(StringUtils.trim(bookSourceBean.getRuleSearchKind()));
        tieRuleSearchLastChapter.setText(StringUtils.trim(bookSourceBean.getRuleSearchLastChapter()));
        tieRuleSearchList.setText(StringUtils.trim(bookSourceBean.getRuleSearchList()));
        tieRuleSearchName.setText(StringUtils.trim(bookSourceBean.getRuleSearchName()));
        tieRuleSearchNoteUrl.setText(StringUtils.trim(bookSourceBean.getRuleSearchNoteUrl()));
        tieRuleSearchUrl.setText(StringUtils.trim(bookSourceBean.getRuleSearchUrl()));
        tieHttpUserAgent.setText(StringUtils.trim(bookSourceBean.getHttpUserAgent()));
        tieRuleFindUrl.setText(StringUtils.trim(bookSourceBean.getRuleFindUrl()));
        tieRuleContentUrlNext.setText(StringUtils.trim(bookSourceBean.getRuleContentUrlNext()));
        tieRuleBookUrlPattern.setText(StringUtils.trim(bookSourceBean.getRuleBookUrlPattern()));
        tieRuleBookKind.setText(StringUtils.trim(bookSourceBean.getRuleBookKind()));
        tieRuleBookLastChapter.setText(StringUtils.trim(bookSourceBean.getRuleBookLastChapter()));
    }

    private void setHint() {
        tilBookSourceName.setHint(getString(R.string.book_source_name));
        tilBookSourceUrl.setHint(getString(R.string.book_source_url));
        tilBookSourceGroup.setHint(getString(R.string.book_source_group));
        tilLoginUrl.setHint(getString(R.string.book_source_login_url));
        tilRuleBookAuthor.setHint(getString(R.string.rule_book_author));
        tilRuleBookContent.setHint(getString(R.string.rule_book_content));
        tilRuleBookName.setHint(getString(R.string.rule_book_name));
        tilRuleChapterList.setHint(getString(R.string.rule_chapter_list));
        tilRuleChapterName.setHint(getString(R.string.rule_chapter_name));
        tilRuleChapterListUrl.setHint(getString(R.string.rule_chapter_list_url));
        tilRuleChapterListUrlNext.setHint(getString(R.string.rule_chapter_list_url_next));
        tilRuleContentUrl.setHint(getString(R.string.rule_content_url));
        tilRuleCoverUrl.setHint(getString(R.string.rule_cover_url));
        tilRuleIntroduce.setHint(getString(R.string.rule_introduce));
        tilRuleSearchAuthor.setHint(getString(R.string.rule_search_author));
        tilRuleSearchCoverUrl.setHint(getString(R.string.rule_search_cover_url));
        tilRuleSearchKind.setHint(getString(R.string.rule_search_kind));
        tilRuleSearchLastChapter.setHint(getString(R.string.rule_search_last_chapter));
        tilRuleSearchList.setHint(getString(R.string.rule_search_list));
        tilRuleSearchName.setHint(getString(R.string.rule_search_name));
        tilRuleSearchNoteUrl.setHint(getString(R.string.rule_search_note_url));
        tilRuleSearchUrl.setHint(getString(R.string.rule_search_url));
        tilHttpUserAgent.setHint(getString(R.string.source_user_agent));
        tilRuleFindUrl.setHint(getString(R.string.rule_find_url));
        tilRuleContentUrlNext.setHint(getString(R.string.rule_content_url_next));
        tilRuleBookUrlPattern.setHint(getString(R.string.book_url_pattern));
        tilRuleBookKind.setHint(getString(R.string.rule_book_kind));
        tilRuleBookLastChapter.setHint(getString(R.string.rule_book_last_chapter));
    }

    @SuppressLint("SetWorldReadable")
    @SuppressWarnings("unchecked")
    private void shareBookSource() {
        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            BitMatrix result;
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                Hashtable<EncodeHintType, Object> hst = new Hashtable();
                hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                result = multiFormatWriter.encode(getBookSourceStr(), BarcodeFormat.QR_CODE, 600, 600, hst);
                int[] pixels = new int[600 * 600];
                for (int y = 0; y < 600; y++) {
                    for (int x = 0; x < 600; x++) {
                        if (result.get(x, y)) {
                            pixels[y * 600 + x] = Color.BLACK;
                        } else {
                            pixels[y * 600 + x] = Color.WHITE;
                        }
                    }
                }
                Bitmap bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, 600, 0, 0, 600, 600);
                emitter.onSuccess(bitmap);
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        try {
                            File file = new File(SourceEditActivity.this.getExternalCacheDir(), "bookSource.png");
                            FileOutputStream fOut = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                            fOut.flush();
                            fOut.close();
                            //noinspection ResultOfMethodCallIgnored
                            file.setReadable(true, false);
                            Uri contentUri = FileProvider.getUriForFile(SourceEditActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            intent.setType("image/png");
                            startActivity(Intent.createChooser(intent, "分享书源"));
                        } catch (Exception e) {
                            toast(e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        toast(e.getLocalizedMessage());
                    }
                });

    }

    private void openRuleSummary() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.source_rule_url)));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            toast(R.string.can_not_open, ERROR);
        }
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_source_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                if (canSaveBookSource()) {
                    mPresenter.saveSource(getBookSource(), bookSourceBean)
                            .subscribe(new SimpleObserver<Boolean>() {
                                @Override
                                public void onNext(Boolean aBoolean) {
                                    bookSourceBean = getBookSource();
                                    toast("保存成功");
                                    setResult(RESULT_OK);
                                    finish();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    toast(e.getLocalizedMessage());
                                }
                            });
                }
                break;
            case R.id.action_login:
                if (!TextUtils.isEmpty(getBookSource().getLoginUrl())) {
                    SourceLoginActivity.startThis(this, getBookSource());
                } else {
                    toast(R.string.source_no_login);
                }
                break;
            case R.id.action_copy_source:
                mPresenter.copySource(getBookSource());
                break;
            case R.id.action_paste_source:
                mPresenter.pasteSource();
                break;
            case R.id.action_qr_code_camera:
                scanBookSource();
                break;
            case R.id.action_share_it:
                shareBookSource();
                break;
            case R.id.action_rule_summary:
                openRuleSummary();
                break;
            case R.id.action_debug_source:
                if (canSaveBookSource()) {
                    mPresenter.saveSource(getBookSource(), bookSourceBean)
                            .subscribe(new SimpleObserver<Boolean>() {
                                @Override
                                public void onNext(Boolean aBoolean) {
                                    bookSourceBean = getBookSource();
                                    setResult(RESULT_OK);
                                    SourceDebugActivity.startThis(SourceEditActivity.this, getBookSource().getBookSourceUrl());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    toast(e.getLocalizedMessage());
                                }
                            });
                }
                break;
            case android.R.id.home:
                SoftInputUtil.hideIMM(getCurrentFocus());
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_QR && resultCode == RESULT_OK && null != data) {
            String result = data.getStringExtra("result");
            Observable<List<BookSourceBean>> observable = BookSourceManager.importSource(result);
            if (observable != null) {
                observable.subscribe(new SimpleObserver<List<BookSourceBean>>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(List<BookSourceBean> bookSourceBeans) {
                        if (bookSourceBeans.size() > 1) {
                            toast(String.format("导入成功%d个书源, 显示第一个", bookSourceBeans.size()));
                            setText(bookSourceBeans.get(0));
                        } else if (bookSourceBeans.size() == 1) {
                            setText(bookSourceBeans.get(0));
                        } else {
                            toast("未导入");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        toast(e.getLocalizedMessage());
                    }
                });
            } else {
                toast("导入失败");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (bookSourceBean == null) {
                bookSourceBean = new BookSourceBean();
            }
            if (!getBookSource().equals(bookSourceBean)) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.exit))
                        .setMessage(getString(R.string.exit_no_save))
                        .setPositiveButton("是", (DialogInterface dialogInterface, int which) -> {
                        })
                        .setNegativeButton("否", (DialogInterface dialogInterface, int which) -> finish())
                        .show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    private void insertTextToEditText(String txt) {
        if (TextUtils.isEmpty(txt)) return;
        View view = getWindow().getDecorView().findFocus();
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            Editable edit = editText.getEditableText();//获取EditText的文字
            if (start < 0 || start >= edit.length()) {
                edit.append(txt);
            } else {
                edit.replace(start, end, txt);//光标所在位置插入文字
            }
        }
    }

    private void showKeyboardTopPopupWindow(int x, int y) {
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            updateKeyboardTopPopupWindow(x, y); //可能是输入法切换了输入模式，高度会变化（比如切换为语音输入）
            return;
        }
        if (mSoftKeyboardTool != null) {
            mSoftKeyboardTool.showAtLocation(llContent, Gravity.BOTTOM, x, y);
        }
    }

    private void updateKeyboardTopPopupWindow(int x, int y) {
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            mSoftKeyboardTool.update(x, y, mSoftKeyboardTool.getWidth(), mSoftKeyboardTool.getHeight());
        }
    }

    private void closePopupWindow() {
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            mSoftKeyboardTool.dismiss();
        }
    }

    private class KeyboardOnGlobalChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            // 获取当前页面窗口的显示范围
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = SoftInputUtil.getScreenHeight(SourceEditActivity.this);
            int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
            boolean preShowing = mIsSoftKeyBoardShowing;
            if (Math.abs(keyboardHeight) > screenHeight / 5) {
                mIsSoftKeyBoardShowing = true; // 超过屏幕五分之一则表示弹出了输入法
                showKeyboardTopPopupWindow(SoftInputUtil.getScreenWidth(SourceEditActivity.this) / 2, keyboardHeight);
                llContent.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, keyboardHeight + 100);
            } else {
                if (preShowing) {
                    closePopupWindow();
                }
                mIsSoftKeyBoardShowing = false;
                llContent.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
            }
        }
    }

}
