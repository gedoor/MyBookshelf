package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.BuildConfig;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.constant.BookType;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.SourceEditPresenter;
import com.kunfei.bookshelf.presenter.contract.SourceEditContract;
import com.kunfei.bookshelf.service.ShareService;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.SourceEditAdapter;
import com.kunfei.bookshelf.view.popupwindow.KeyboardToolPop;
import com.kunfei.bookshelf.widget.views.ATECheckBox;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/26.
 * 编辑书源
 */

public class SourceEditActivity extends MBaseActivity<SourceEditContract.Presenter> implements SourceEditContract.View, KeyboardToolPop.CallBack {
    public final static int EDIT_SOURCE = 1101;
    private final int REQUEST_QR = 202;

    @BindView(R.id.action_bar)
    AppBarLayout actionBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.cb_is_audio)
    ATECheckBox cbIsAudio;
    @BindView(R.id.cb_is_enable)
    ATECheckBox cbIsEnable;
    @BindView(R.id.tv_edit_find)
    TextView tvEditFind;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private SourceEditAdapter adapter;
    private List<SourceEdit> sourceEditList = new ArrayList<>();
    private List<SourceEdit> findEditList = new ArrayList<>();
    private BookSourceBean bookSourceBean;
    private int serialNumber;
    private boolean enable;
    private String title;
    private PopupWindow mSoftKeyboardTool;
    private boolean mIsSoftKeyBoardShowing = false;
    private boolean showFind;
    private String[] keyHelp = {"@", "&", "|", "%", "/", ":", "[", "]", "{", "}", "<", ">", "\\", "$", "#", "!", ".",
            "href", "src", "textNodes", "xpath", "json", "css", "id", "class", "tag"};

    public static void startThis(Object object, BookSourceBean sourceBean) {
        String key = String.valueOf(System.currentTimeMillis());
        BitIntentDataManager.getInstance().putData(key, sourceBean.clone());
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            Intent intent = new Intent(activity, SourceEditActivity.class);
            intent.putExtra("data_key", key);
            activity.startActivityForResult(intent, EDIT_SOURCE);
        } else if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            Intent intent = new Intent(fragment.getContext(), SourceEditActivity.class);
            intent.putExtra("data_key", key);
            fragment.startActivityForResult(intent, EDIT_SOURCE);
        } else if (object instanceof Context) {
            Context context = (Context) object;
            Intent intent = new Intent(context, SourceEditActivity.class);
            intent.putExtra("data_key", key);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
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
                bookSourceBean = new BookSourceBean();
            } else {
                title = getString(R.string.edit_book_source);
                bookSourceBean = (BookSourceBean) BitIntentDataManager.getInstance().getData(key);
                serialNumber = bookSourceBean.getSerialNumber();
                enable = bookSourceBean.getEnable();
            }
        }

    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        mSoftKeyboardTool = new KeyboardToolPop(this, Arrays.asList(keyHelp), this);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new KeyboardOnGlobalChangeListener());
        adapter = new SourceEditAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.reSetData(sourceEditList);
        setText(bookSourceBean);
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        tvEditFind.setOnClickListener(v -> {
            recyclerView.clearFocus();
            if (showFind) {
                adapter.reSetData(sourceEditList);
                tvEditFind.setText(R.string.edit_find);
            } else {
                adapter.reSetData(findEditList);
                tvEditFind.setText(R.string.back);
            }
            showFind = !showFind;
            recyclerView.scrollToPosition(0);
        });
    }

    private boolean canSaveBookSource() {
        SoftInputUtil.hideIMM(recyclerView);
        recyclerView.clearFocus();
        BookSourceBean bookSourceBean = getBookSource(true);
        if (isEmpty(bookSourceBean.getBookSourceName()) || isEmpty(bookSourceBean.getBookSourceUrl())) {
            toast(R.string.non_null_source_name_url, ERROR);
            return false;
        }
        return true;
    }

    @Override
    public String getBookSourceStr(boolean hasFind) {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        return gson.toJson(getBookSource(hasFind));
    }

    @Override
    public void setText(BookSourceBean bookSourceBean) {
        sourceEditList.clear();
        findEditList.clear();
        adapter.notifyDataSetChanged();
        sourceEditList.add(new SourceEdit("bookSourceUrl", bookSourceBean.getBookSourceUrl(), R.string.book_source_url));
        sourceEditList.add(new SourceEdit("bookSourceName", bookSourceBean.getBookSourceName(), R.string.book_source_name));
        sourceEditList.add(new SourceEdit("bookSourceGroup", bookSourceBean.getBookSourceGroup(), R.string.book_source_group));
        sourceEditList.add(new SourceEdit("loginUrl", bookSourceBean.getLoginUrl(), R.string.book_source_login_url));
        //搜索
        sourceEditList.add(new SourceEdit("ruleSearchUrl", bookSourceBean.getRuleSearchUrl(), R.string.rule_search_url));
        sourceEditList.add(new SourceEdit("ruleSearchList", bookSourceBean.getRuleSearchList(), R.string.rule_search_list));
        sourceEditList.add(new SourceEdit("ruleSearchName", bookSourceBean.getRuleSearchName(), R.string.rule_search_name));
        sourceEditList.add(new SourceEdit("ruleSearchAuthor", bookSourceBean.getRuleSearchAuthor(), R.string.rule_search_author));
        sourceEditList.add(new SourceEdit("ruleSearchKind", bookSourceBean.getRuleSearchKind(), R.string.rule_search_kind));
        sourceEditList.add(new SourceEdit("ruleSearchLastChapter", bookSourceBean.getRuleSearchLastChapter(), R.string.rule_search_last_chapter));
        sourceEditList.add(new SourceEdit("ruleSearchIntroduce", bookSourceBean.getRuleSearchIntroduce(), R.string.rule_search_introduce));
        sourceEditList.add(new SourceEdit("ruleSearchCoverUrl", bookSourceBean.getRuleSearchCoverUrl(), R.string.rule_search_cover_url));
        sourceEditList.add(new SourceEdit("ruleSearchNoteUrl", bookSourceBean.getRuleSearchNoteUrl(), R.string.rule_search_note_url));
        //详情页
        sourceEditList.add(new SourceEdit("ruleBookUrlPattern", bookSourceBean.getRuleBookUrlPattern(), R.string.book_url_pattern));
        sourceEditList.add(new SourceEdit("ruleBookInfoInit", bookSourceBean.getRuleBookInfoInit(), R.string.rule_book_info_init));
        sourceEditList.add(new SourceEdit("ruleBookName", bookSourceBean.getRuleBookName(), R.string.rule_book_name));
        sourceEditList.add(new SourceEdit("ruleBookAuthor", bookSourceBean.getRuleBookAuthor(), R.string.rule_book_author));
        sourceEditList.add(new SourceEdit("ruleCoverUrl", bookSourceBean.getRuleCoverUrl(), R.string.rule_cover_url));
        sourceEditList.add(new SourceEdit("ruleIntroduce", bookSourceBean.getRuleIntroduce(), R.string.rule_introduce));
        sourceEditList.add(new SourceEdit("ruleBookKind", bookSourceBean.getRuleBookKind(), R.string.rule_book_kind));
        sourceEditList.add(new SourceEdit("ruleBookLastChapter", bookSourceBean.getRuleBookLastChapter(), R.string.rule_book_last_chapter));
        sourceEditList.add(new SourceEdit("ruleChapterUrl", bookSourceBean.getRuleChapterUrl(), R.string.rule_chapter_list_url));
        //目录页
        sourceEditList.add(new SourceEdit("ruleChapterUrlNext", bookSourceBean.getRuleChapterUrlNext(), R.string.rule_chapter_list_url_next));
        sourceEditList.add(new SourceEdit("ruleChapterList", bookSourceBean.getRuleChapterList(), R.string.rule_chapter_list));
        sourceEditList.add(new SourceEdit("ruleChapterName", bookSourceBean.getRuleChapterName(), R.string.rule_chapter_name));
        sourceEditList.add(new SourceEdit("ruleContentUrl", bookSourceBean.getRuleContentUrl(), R.string.rule_content_url));
        //正文页
        sourceEditList.add(new SourceEdit("ruleContentUrlNext", bookSourceBean.getRuleContentUrlNext(), R.string.rule_content_url_next));
        sourceEditList.add(new SourceEdit("ruleBookContent", bookSourceBean.getRuleBookContent(), R.string.rule_book_content));
        sourceEditList.add(new SourceEdit("httpUserAgent", bookSourceBean.getHttpUserAgent(), R.string.source_user_agent));

        //发现
        findEditList.add(new SourceEdit("ruleFindUrl", bookSourceBean.getRuleFindUrl(), R.string.rule_find_url));
        findEditList.add(new SourceEdit("ruleFindList", bookSourceBean.getRuleFindList(), R.string.rule_find_list));
        findEditList.add(new SourceEdit("ruleFindName", bookSourceBean.getRuleFindName(), R.string.rule_find_name));
        findEditList.add(new SourceEdit("ruleFindAuthor", bookSourceBean.getRuleFindAuthor(), R.string.rule_find_author));
        findEditList.add(new SourceEdit("ruleFindKind", bookSourceBean.getRuleFindKind(), R.string.rule_find_kind));
        findEditList.add(new SourceEdit("ruleFindIntroduce", bookSourceBean.getRuleFindIntroduce(), R.string.rule_find_introduce));
        findEditList.add(new SourceEdit("ruleFindLastChapter", bookSourceBean.getRuleFindLastChapter(), R.string.rule_find_last_chapter));
        findEditList.add(new SourceEdit("ruleFindCoverUrl", bookSourceBean.getRuleFindCoverUrl(), R.string.rule_find_cover_url));
        findEditList.add(new SourceEdit("ruleFindNoteUrl", bookSourceBean.getRuleFindNoteUrl(), R.string.rule_find_note_url));

        if (showFind) {
            adapter.reSetData(findEditList);
        } else {
            adapter.reSetData(sourceEditList);
        }
        cbIsAudio.setChecked(Objects.equals(bookSourceBean.getBookSourceType(), BookType.AUDIO));
        cbIsEnable.setChecked(bookSourceBean.getEnable());
    }

    private void scanBookSource() {
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(intent, REQUEST_QR);
    }

    private BookSourceBean getBookSource(boolean hasFind) {
        BookSourceBean bookSourceBeanN = new BookSourceBean();
        for (SourceEdit sourceEdit : sourceEditList) {
            switch (sourceEdit.getKey()) {
                case "bookSourceUrl":
                    bookSourceBeanN.setBookSourceUrl(sourceEdit.value);
                    break;
                case "bookSourceName":
                    bookSourceBeanN.setBookSourceName(sourceEdit.value);
                    break;
                case "bookSourceGroup":
                    bookSourceBeanN.setBookSourceGroup(sourceEdit.value);
                    break;
                case "loginUrl":
                    bookSourceBeanN.setLoginUrl(sourceEdit.value);
                    break;
                case "ruleSearchUrl":
                    bookSourceBeanN.setRuleSearchUrl(sourceEdit.value);
                    break;
                case "ruleSearchList":
                    bookSourceBeanN.setRuleSearchList(sourceEdit.value);
                    break;
                case "ruleSearchName":
                    bookSourceBeanN.setRuleSearchName(sourceEdit.value);
                    break;
                case "ruleSearchAuthor":
                    bookSourceBeanN.setRuleSearchAuthor(sourceEdit.value);
                    break;
                case "ruleSearchKind":
                    bookSourceBeanN.setRuleSearchKind(sourceEdit.value);
                    break;
                case "ruleSearchIntroduce":
                    bookSourceBeanN.setRuleSearchIntroduce(sourceEdit.value);
                    break;
                case "ruleSearchLastChapter":
                    bookSourceBeanN.setRuleSearchLastChapter(sourceEdit.value);
                    break;
                case "ruleSearchCoverUrl":
                    bookSourceBeanN.setRuleSearchCoverUrl(sourceEdit.value);
                    break;
                case "ruleSearchNoteUrl":
                    bookSourceBeanN.setRuleSearchNoteUrl(sourceEdit.value);
                    break;
                case "ruleBookUrlPattern":
                    bookSourceBeanN.setRuleBookUrlPattern(sourceEdit.value);
                    break;
                case "ruleBookInfoInit":
                    bookSourceBeanN.setRuleBookInfoInit(sourceEdit.value);
                    break;
                case "ruleBookName":
                    bookSourceBeanN.setRuleBookName(sourceEdit.value);
                    break;
                case "ruleBookAuthor":
                    bookSourceBeanN.setRuleBookAuthor(sourceEdit.value);
                    break;
                case "ruleCoverUrl":
                    bookSourceBeanN.setRuleCoverUrl(sourceEdit.value);
                    break;
                case "ruleIntroduce":
                    bookSourceBeanN.setRuleIntroduce(sourceEdit.value);
                    break;
                case "ruleBookKind":
                    bookSourceBeanN.setRuleBookKind(sourceEdit.value);
                    break;
                case "ruleBookLastChapter":
                    bookSourceBeanN.setRuleBookLastChapter(sourceEdit.value);
                    break;
                case "ruleChapterUrl":
                    bookSourceBeanN.setRuleChapterUrl(sourceEdit.value);
                    break;
                case "ruleChapterUrlNext":
                    bookSourceBeanN.setRuleChapterUrlNext(sourceEdit.value);
                    break;
                case "ruleChapterList":
                    bookSourceBeanN.setRuleChapterList(sourceEdit.value);
                    break;
                case "ruleChapterName":
                    bookSourceBeanN.setRuleChapterName(sourceEdit.value);
                    break;
                case "ruleContentUrl":
                    bookSourceBeanN.setRuleContentUrl(sourceEdit.value);
                    break;
                case "ruleContentUrlNext":
                    bookSourceBeanN.setRuleContentUrlNext(sourceEdit.value);
                    break;
                case "ruleBookContent":
                    bookSourceBeanN.setRuleBookContent(sourceEdit.value);
                    break;
                case "httpUserAgent":
                    bookSourceBeanN.setHttpUserAgent(sourceEdit.value);
                    break;
            }
        }
        if (hasFind) {
            for (SourceEdit sourceEdit : findEditList) {
                switch (sourceEdit.getKey()) {
                    case "ruleFindUrl":
                        bookSourceBeanN.setRuleFindUrl(sourceEdit.value);
                        break;
                    case "ruleFindList":
                        bookSourceBeanN.setRuleFindList(sourceEdit.value);
                        break;
                    case "ruleFindName":
                        bookSourceBeanN.setRuleFindName(sourceEdit.value);
                        break;
                    case "ruleFindAuthor":
                        bookSourceBeanN.setRuleFindAuthor(sourceEdit.value);
                        break;
                    case "ruleFindKind":
                        bookSourceBeanN.setRuleFindKind(sourceEdit.value);
                        break;
                    case "ruleFindIntroduce":
                        bookSourceBeanN.setRuleFindIntroduce(sourceEdit.value);
                        break;
                    case "ruleFindLastChapter":
                        bookSourceBeanN.setRuleFindLastChapter(sourceEdit.value);
                        break;
                    case "ruleFindCoverUrl":
                        bookSourceBeanN.setRuleFindCoverUrl(sourceEdit.value);
                        break;
                    case "ruleFindNoteUrl":
                        bookSourceBeanN.setRuleFindNoteUrl(sourceEdit.value);
                        break;
                }
            }
        }
        bookSourceBeanN.setSerialNumber(serialNumber);
        bookSourceBeanN.setEnable(cbIsEnable.isChecked());
        bookSourceBeanN.setBookSourceType(cbIsAudio.isChecked() ? BookType.AUDIO : null);
        return bookSourceBeanN;
    }

    @SuppressLint("SetWorldReadable")
    private void shareBookSource() {
        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(getBookSourceStr(true), 600);
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            emitter.onSuccess(bitmap);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<Bitmap>() {

                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        if (bitmap == null) {
                            toast("书源文字太多,生成二维码失败");
                            return;
                        }
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
                });

    }

    private void openRuleSummary() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.source_rule_url)));
            startActivity(intent);
        } catch (Exception e) {
            toast(R.string.can_not_open, ERROR);
        }
    }

    private void shareText(String title, String text) {
        try {
            Intent textIntent = new Intent(Intent.ACTION_SEND);
            textIntent.setType("text/plain");
            textIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(textIntent, title));
        } catch (Exception e) {
            toast(R.string.can_not_share, ERROR);
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
                    mPresenter.saveSource(getBookSource(true), bookSourceBean)
                            .subscribe(new MyObserver<Boolean>() {
                                @Override
                                public void onNext(Boolean aBoolean) {
                                    bookSourceBean = getBookSource(true);
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
                if (!isEmpty(getBookSource(true).getLoginUrl())) {
                    SourceLoginActivity.startThis(this, getBookSource(true));
                } else {
                    toast(R.string.source_no_login);
                }
                break;
            case R.id.action_copy_source:
                mPresenter.copySource(getBookSourceStr(true));
                break;
            case R.id.action_copy_source_no_find:
                mPresenter.copySource(getBookSourceStr(false));
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
            case R.id.action_share_str:
                shareText("Source Share", getBookSourceStr(true));
                break;
            case R.id.action_share_wifi:
                ShareService.startThis(this, Collections.singletonList(getBookSource(true)));
                break;
            case R.id.action_rule_summary:
                openRuleSummary();
                break;
            case R.id.action_debug_source:
                if (canSaveBookSource()) {
                    mPresenter.saveSource(getBookSource(true), bookSourceBean)
                            .subscribe(new MyObserver<Boolean>() {
                                @Override
                                public void onNext(Boolean aBoolean) {
                                    bookSourceBean = getBookSource(true);
                                    setResult(RESULT_OK);
                                    SourceDebugActivity.startThis(SourceEditActivity.this, getBookSource(true).getBookSourceUrl());
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
                if (back()) {
                    return true;
                }
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
                observable.subscribe(new MyObserver<List<BookSourceBean>>() {
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
            if (back()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSoftKeyboardTool != null) {
            mSoftKeyboardTool.dismiss();
        }
    }

    private boolean back() {
        if (bookSourceBean == null) {
            bookSourceBean = new BookSourceBean();
        }
        if (!getBookSource(true).equals(bookSourceBean)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.exit))
                    .setMessage(getString(R.string.exit_no_save))
                    .setPositiveButton("是", (DialogInterface dialogInterface, int which) -> {
                    })
                    .setNegativeButton("否", (DialogInterface dialogInterface, int which) -> finish())
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void sendText(@NotNull String txt) {
        if (isEmpty(txt)) return;
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

    private void showKeyboardTopPopupWindow() {
        if (isFinishing()) return;
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            return;
        }
        if (mSoftKeyboardTool != null & !this.isFinishing()) {
            mSoftKeyboardTool.showAtLocation(llContent, Gravity.BOTTOM, 0, 0);
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
                recyclerView.setPadding(0, 0, 0, 100);
                showKeyboardTopPopupWindow();
            } else {
                mIsSoftKeyBoardShowing = false;
                recyclerView.setPadding(0, 0, 0, 0);
                if (preShowing) {
                    closePopupWindow();
                }
            }
        }
    }

    public class SourceEdit {
        private String key;
        private String value;
        private int hint;

        SourceEdit(String key, String value, int hint) {
            this.key = key;
            this.value = value;
            this.hint = hint;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getHint() {
            return hint;
        }
    }

}
