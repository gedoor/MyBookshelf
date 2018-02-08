package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Excluder;
import com.google.gson.stream.JsonWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.BuildConfig;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.listener.OnObservableListener;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.SourceEditPresenterImpl;
import com.monke.monkeybook.presenter.impl.ISourceEditPresenter;
import com.monke.monkeybook.view.impl.ISourceEditView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/26.
 * 编辑书源
 */

public class SourceEditActivity extends MBaseActivity<ISourceEditPresenter> implements ISourceEditView {
    private final int REQUEST_QR_IMAGE = 202;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tie_book_source_url)
    TextInputEditText tieBookSourceUrl;
    @BindView(R.id.til_book_source_url)
    TextInputLayout tilBookSourceUrl;
    @BindView(R.id.tie_book_source_name)
    TextInputEditText tieBookSourceName;
    @BindView(R.id.til_book_source_name)
    TextInputLayout tilBookSourceName;
    @BindView(R.id.tie_ruleSearchUrl)
    TextInputEditText tieRuleSearchUrl;
    @BindView(R.id.til_ruleSearchUrl)
    TextInputLayout tilRuleSearchUrl;
    @BindView(R.id.tie_ruleSearchList)
    TextInputEditText tieRuleSearchList;
    @BindView(R.id.til_ruleSearchList)
    TextInputLayout tilRuleSearchList;
    @BindView(R.id.tie_ruleSearchName)
    TextInputEditText tieRuleSearchName;
    @BindView(R.id.til_ruleSearchName)
    TextInputLayout tilRuleSearchName;
    @BindView(R.id.tie_ruleSearchAuthor)
    TextInputEditText tieRuleSearchAuthor;
    @BindView(R.id.til_ruleSearchAuthor)
    TextInputLayout tilRuleSearchAuthor;
    @BindView(R.id.tie_ruleSearchKind)
    TextInputEditText tieRuleSearchKind;
    @BindView(R.id.til_ruleSearchKind)
    TextInputLayout tilRuleSearchKind;
    @BindView(R.id.tie_ruleSearchLastChapter)
    TextInputEditText tieRuleSearchLastChapter;
    @BindView(R.id.til_ruleSearchLastChapter)
    TextInputLayout tilRuleSearchLastChapter;
    @BindView(R.id.tie_ruleSearchCoverUrl)
    TextInputEditText tieRuleSearchCoverUrl;
    @BindView(R.id.til_ruleSearchCoverUrl)
    TextInputLayout tilRuleSearchCoverUrl;
    @BindView(R.id.tie_ruleSearchNoteUrl)
    TextInputEditText tieRuleSearchNoteUrl;
    @BindView(R.id.til_ruleSearchNoteUrl)
    TextInputLayout tilRuleSearchNoteUrl;
    @BindView(R.id.tie_ruleBookName)
    TextInputEditText tieRuleBookName;
    @BindView(R.id.til_ruleBookName)
    TextInputLayout tilRuleBookName;
    @BindView(R.id.tie_ruleBookAuthor)
    TextInputEditText tieRuleBookAuthor;
    @BindView(R.id.til_ruleBookAuthor)
    TextInputLayout tilRuleBookAuthor;
    @BindView(R.id.tie_ruleCoverUrl)
    TextInputEditText tieRuleCoverUrl;
    @BindView(R.id.til_ruleCoverUrl)
    TextInputLayout tilRuleCoverUrl;
    @BindView(R.id.tie_ruleChapterUrl)
    TextInputEditText tieRuleChapterUrl;
    @BindView(R.id.til_ruleChapterUrl)
    TextInputLayout tilRuleChapterUrl;
    @BindView(R.id.tie_ruleIntroduce)
    TextInputEditText tieRuleIntroduce;
    @BindView(R.id.til_ruleIntroduce)
    TextInputLayout tilRuleIntroduce;
    @BindView(R.id.tie_ruleChapterList)
    TextInputEditText tieRuleChapterList;
    @BindView(R.id.til_ruleChapterList)
    TextInputLayout tilRuleChapterList;
    @BindView(R.id.tie_ruleChapterName)
    TextInputEditText tieRuleChapterName;
    @BindView(R.id.til_ruleChapterName)
    TextInputLayout tilRuleChapterName;
    @BindView(R.id.tie_ruleContentUrl)
    TextInputEditText tieRuleContentUrl;
    @BindView(R.id.til_ruleContentUrl)
    TextInputLayout tilRuleContentUrl;
    @BindView(R.id.tie_ruleBookContent)
    TextInputEditText tieRuleBookContent;
    @BindView(R.id.til_ruleBookContent)
    TextInputLayout tilRuleBookContent;

    private BookSourceBean bookSourceBean;
    private int serialNumber;
    private boolean enable;
    private String title;

    @Override
    protected ISourceEditPresenter initInjector() {
        return new SourceEditPresenterImpl();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putInt("serialNumber", serialNumber);
        outState.putBoolean("enable", enable);
    }

    @Override
    protected void onCreateActivity() {
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
    }

    private void saveBookSource() {
        if (isEmpty(tieBookSourceName.getText().toString().trim()) || isEmpty(tieBookSourceUrl.getText().toString().trim())) {
            Toast.makeText(MApplication.getInstance(), "书源名称和URL不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        BookSourceManage.addBookSource(bookSourceBean, getBookSource(), new OnObservableListener() {
            @Override
            public void success() {
                bookSourceBean = getBookSource();
                Toast.makeText(MApplication.getInstance(), "保存成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void error() {
                Toast.makeText(MApplication.getInstance(), "保存失败", Toast.LENGTH_SHORT).show();
            }
        });
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
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.initiateScan();
    }

    private BookSourceBean getBookSource() {
        BookSourceBean bookSourceBeanN = new BookSourceBean();
        bookSourceBeanN.setBookSourceName(tieBookSourceName.getText().toString().trim());
        bookSourceBeanN.setBookSourceUrl(tieBookSourceUrl.getText().toString().trim());
        bookSourceBeanN.setRuleBookAuthor(tieRuleBookAuthor.getText().toString().trim());
        bookSourceBeanN.setRuleBookContent(tieRuleBookContent.getText().toString().trim());
        bookSourceBeanN.setRuleBookName(tieRuleBookName.getText().toString().trim());
        bookSourceBeanN.setRuleChapterList(tieRuleChapterList.getText().toString().trim());
        bookSourceBeanN.setRuleChapterName(tieRuleChapterName.getText().toString().trim());
        bookSourceBeanN.setRuleChapterUrl(tieRuleChapterUrl.getText().toString().trim());
        bookSourceBeanN.setRuleContentUrl(tieRuleContentUrl.getText().toString().trim());
        bookSourceBeanN.setRuleCoverUrl(tieRuleCoverUrl.getText().toString().trim());
        bookSourceBeanN.setRuleIntroduce(tieRuleIntroduce.getText().toString().trim());
        bookSourceBeanN.setRuleSearchAuthor(tieRuleSearchAuthor.getText().toString().trim());
        bookSourceBeanN.setRuleSearchCoverUrl(tieRuleSearchCoverUrl.getText().toString().trim());
        bookSourceBeanN.setRuleSearchKind(tieRuleSearchKind.getText().toString().trim());
        bookSourceBeanN.setRuleSearchLastChapter(tieRuleSearchLastChapter.getText().toString().trim());
        bookSourceBeanN.setRuleSearchList(tieRuleSearchList.getText().toString().trim());
        bookSourceBeanN.setRuleSearchName(tieRuleSearchName.getText().toString().trim());
        bookSourceBeanN.setRuleSearchNoteUrl(tieRuleSearchNoteUrl.getText().toString().trim());
        bookSourceBeanN.setRuleSearchUrl(tieRuleSearchUrl.getText().toString().trim());
        bookSourceBeanN.setEnable(enable);
        bookSourceBeanN.setSerialNumber(serialNumber);
        return bookSourceBeanN;
    }

    @Override
    public void setText(BookSourceBean bookSourceBean) {
        if (bookSourceBean == null) {
            return;
        }
        tieBookSourceName.setText(bookSourceBean.getBookSourceName());
        tieBookSourceUrl.setText(bookSourceBean.getBookSourceUrl());
        tieRuleBookAuthor.setText(bookSourceBean.getRuleBookAuthor());
        tieRuleBookContent.setText(bookSourceBean.getRuleBookContent());
        tieRuleBookName.setText(bookSourceBean.getRuleBookName());
        tieRuleChapterList.setText(bookSourceBean.getRuleChapterList());
        tieRuleChapterName.setText(bookSourceBean.getRuleChapterName());
        tieRuleChapterUrl.setText(bookSourceBean.getRuleChapterUrl());
        tieRuleContentUrl.setText(bookSourceBean.getRuleContentUrl());
        tieRuleCoverUrl.setText(bookSourceBean.getRuleCoverUrl());
        tieRuleIntroduce.setText(bookSourceBean.getRuleIntroduce());
        tieRuleSearchAuthor.setText(bookSourceBean.getRuleSearchAuthor());
        tieRuleSearchCoverUrl.setText(bookSourceBean.getRuleSearchCoverUrl());
        tieRuleSearchKind.setText(bookSourceBean.getRuleSearchKind());
        tieRuleSearchLastChapter.setText(bookSourceBean.getRuleSearchLastChapter());
        tieRuleSearchList.setText(bookSourceBean.getRuleSearchList());
        tieRuleSearchName.setText(bookSourceBean.getRuleSearchName());
        tieRuleSearchNoteUrl.setText(bookSourceBean.getRuleSearchNoteUrl());
        tieRuleSearchUrl.setText(bookSourceBean.getRuleSearchUrl());
    }

    private void setHint() {
        tilBookSourceName.setHint("BookSourceName");
        tilBookSourceUrl.setHint("BookSourceUrl");
        tilRuleBookAuthor.setHint("RuleBookAuthor");
        tilRuleBookContent.setHint("RuleBookContent");
        tilRuleBookName.setHint("RuleBookName");
        tilRuleChapterList.setHint("RuleChapterList");
        tilRuleChapterName.setHint("RuleChapterName");
        tilRuleChapterUrl.setHint("RuleChapterUrl");
        tilRuleContentUrl.setHint("RuleContentUrl");
        tilRuleCoverUrl.setHint("RuleCoverUrl");
        tilRuleIntroduce.setHint("RuleIntroduce");
        tilRuleSearchAuthor.setHint("RuleSearchAuthor");
        tilRuleSearchCoverUrl.setHint("RuleSearchCoverUrl");
        tilRuleSearchKind.setHint("RuleSearchKind");
        tilRuleSearchLastChapter.setHint("RuleSearchLastChapter");
        tilRuleSearchList.setHint("RuleSearchList");
        tilRuleSearchName.setHint("RuleSearchName");
        tilRuleSearchNoteUrl.setHint("RuleSearchNoteUrl");
        tilRuleSearchUrl.setHint("RuleSearchUrl");
    }

    private void shareBookSource() {
        Bitmap bitmap = mPresenter.encodeAsBitmap(getBookSourceStr());
        try {
            File file = new File(this.getExternalCacheDir(), "bookSource.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            Uri contentUri = FileProvider.getUriForFile(this, getString(R.string.file_provider), file);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "分享书源"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectLocalImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_QR_IMAGE);
    }

    private void openRuleSummary() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://gedoor.github.io/MONKOVEL/sourcerule.html"));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.can_not_open, Toast.LENGTH_SHORT).show();
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
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveBookSource();
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
            case R.id.action_qr_code_image:
                selectLocalImage();
                break;
            case R.id.action_rule_summary:
                openRuleSummary();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_QR_IMAGE && resultCode == RESULT_OK && null != data) {
            mPresenter.analyzeBitmap(data.getData());
            return;
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                mPresenter.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
}
