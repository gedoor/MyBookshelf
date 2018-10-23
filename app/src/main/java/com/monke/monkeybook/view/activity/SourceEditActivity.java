package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.BuildConfig;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.presenter.SourceEditPresenterImpl;
import com.monke.monkeybook.presenter.contract.SourceEditContract;
import com.monke.monkeybook.utils.SoftInputUtil;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/26.
 * 编辑书源
 */

public class SourceEditActivity extends MBaseActivity<SourceEditContract.Presenter> implements SourceEditContract.View {
    private final int REQUEST_QR_IMAGE = 202;

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
    @BindView(R.id.tie_ruleChapterUrl)
    AppCompatEditText tieRuleChapterUrl;
    @BindView(R.id.til_ruleChapterUrl)
    TextInputLayout tilRuleChapterUrl;
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
    @BindView(R.id.tie_checkUrl)
    AppCompatEditText tieCheckUrl;
    @BindView(R.id.til_checkUrl)
    TextInputLayout tilCheckUrl;
    @BindView(R.id.tie_ruleChapterUrlNext)
    AppCompatEditText tieRuleChapterUrlNext;
    @BindView(R.id.til_ruleChapterUrlNext)
    TextInputLayout tilRuleChapterUrlNext;
    @BindView(R.id.tie_ruleContentUrlNext)
    AppCompatEditText tieRuleContentUrlNext;
    @BindView(R.id.til_ruleContentUrlNext)
    TextInputLayout tilRuleContentUrlNext;

    private BookSourceBean bookSourceBean;
    private int serialNumber;
    private boolean enable;
    private String title;

    @Override
    protected SourceEditContract.Presenter initInjector() {
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
            toast("书源名称和URL不能为空", ERROR);
            return;
        }
        mPresenter.saveSource(getBookSource(), bookSourceBean);
    }

    @Override
    public void saveSuccess() {
        bookSourceBean = getBookSource();
        toast("保存成功");
        setResult(RESULT_OK);
        finish();
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
        integrator.setCaptureActivity(QRCodeScanActivity.class);
        integrator.initiateScan();
    }

    private String trim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    private BookSourceBean getBookSource() {
        BookSourceBean bookSourceBeanN = new BookSourceBean();
        bookSourceBeanN.setBookSourceName(trim(tieBookSourceName.getText().toString()));
        bookSourceBeanN.setBookSourceUrl(trim(tieBookSourceUrl.getText().toString()));
        bookSourceBeanN.setBookSourceGroup(trim(tieBookSourceGroup.getText().toString()));
        bookSourceBeanN.setCheckUrl(trim(tieCheckUrl.getText().toString()));
        bookSourceBeanN.setRuleBookAuthor(trim(tieRuleBookAuthor.getText().toString()));
        bookSourceBeanN.setRuleBookContent(trim(tieRuleBookContent.getText().toString()));
        bookSourceBeanN.setRuleBookName(trim(tieRuleBookName.getText().toString().trim()));
        bookSourceBeanN.setRuleChapterList(trim(tieRuleChapterList.getText().toString()));
        bookSourceBeanN.setRuleChapterName(trim(tieRuleChapterName.getText().toString()));
        bookSourceBeanN.setRuleChapterUrl(trim(tieRuleChapterUrl.getText().toString()));
        bookSourceBeanN.setRuleChapterUrlNext(trim(tieRuleChapterUrlNext.getText().toString()));
        bookSourceBeanN.setRuleContentUrl(trim(tieRuleContentUrl.getText().toString()));
        bookSourceBeanN.setRuleCoverUrl(trim(tieRuleCoverUrl.getText().toString()));
        bookSourceBeanN.setRuleIntroduce(trim(tieRuleIntroduce.getText().toString()));
        bookSourceBeanN.setRuleSearchAuthor(trim(tieRuleSearchAuthor.getText().toString()));
        bookSourceBeanN.setRuleSearchCoverUrl(trim(tieRuleSearchCoverUrl.getText().toString()));
        bookSourceBeanN.setRuleSearchKind(trim(tieRuleSearchKind.getText().toString()));
        bookSourceBeanN.setRuleSearchLastChapter(trim(tieRuleSearchLastChapter.getText().toString()));
        bookSourceBeanN.setRuleSearchList(trim(tieRuleSearchList.getText().toString()));
        bookSourceBeanN.setRuleSearchName(trim(tieRuleSearchName.getText().toString()));
        bookSourceBeanN.setRuleSearchNoteUrl(trim(tieRuleSearchNoteUrl.getText().toString()));
        bookSourceBeanN.setRuleSearchUrl(trim(tieRuleSearchUrl.getText().toString()));
        bookSourceBeanN.setHttpUserAgent(trim(tieHttpUserAgent.getText().toString()));
        bookSourceBeanN.setRuleFindUrl(trim(tieRuleFindUrl.getText().toString()));
        bookSourceBeanN.setRuleContentUrlNext(trim(tieRuleContentUrlNext.getText().toString()));
        bookSourceBeanN.setEnable(enable);
        bookSourceBeanN.setSerialNumber(serialNumber);
        return bookSourceBeanN;
    }

    @Override
    public void setText(BookSourceBean bookSourceBean) {
        if (bookSourceBean == null) {
            return;
        }
        tieBookSourceName.setText(trim(bookSourceBean.getBookSourceName()));
        tieBookSourceUrl.setText(trim(bookSourceBean.getBookSourceUrl()));
        tieBookSourceGroup.setText(trim(bookSourceBean.getBookSourceGroup()));
        tieCheckUrl.setText(trim(bookSourceBean.getCheckUrl()));
        tieRuleBookAuthor.setText(trim(bookSourceBean.getRuleBookAuthor()));
        tieRuleBookContent.setText(trim(bookSourceBean.getRuleBookContent()));
        tieRuleBookName.setText(trim(bookSourceBean.getRuleBookName()));
        tieRuleChapterList.setText(trim(bookSourceBean.getRuleChapterList()));
        tieRuleChapterName.setText(trim(bookSourceBean.getRuleChapterName()));
        tieRuleChapterUrl.setText(trim(bookSourceBean.getRuleChapterUrl()));
        tieRuleChapterUrlNext.setText(trim(bookSourceBean.getRuleChapterUrlNext()));
        tieRuleContentUrl.setText(trim(bookSourceBean.getRuleContentUrl()));
        tieRuleCoverUrl.setText(trim(bookSourceBean.getRuleCoverUrl()));
        tieRuleIntroduce.setText(trim(bookSourceBean.getRuleIntroduce()));
        tieRuleSearchAuthor.setText(trim(bookSourceBean.getRuleSearchAuthor()));
        tieRuleSearchCoverUrl.setText(trim(bookSourceBean.getRuleSearchCoverUrl()));
        tieRuleSearchKind.setText(trim(bookSourceBean.getRuleSearchKind()));
        tieRuleSearchLastChapter.setText(trim(bookSourceBean.getRuleSearchLastChapter()));
        tieRuleSearchList.setText(trim(bookSourceBean.getRuleSearchList()));
        tieRuleSearchName.setText(trim(bookSourceBean.getRuleSearchName()));
        tieRuleSearchNoteUrl.setText(trim(bookSourceBean.getRuleSearchNoteUrl()));
        tieRuleSearchUrl.setText(trim(bookSourceBean.getRuleSearchUrl()));
        tieHttpUserAgent.setText(trim(bookSourceBean.getHttpUserAgent()));
        tieRuleFindUrl.setText(trim(bookSourceBean.getRuleFindUrl()));
        tieRuleContentUrlNext.setText(trim(bookSourceBean.getRuleContentUrlNext()));
    }

    private void setHint() {
        tilBookSourceName.setHint("BookSourceName");
        tilBookSourceUrl.setHint("BookSourceUrl");
        tilBookSourceGroup.setHint("BookSourceGroup");
        tilCheckUrl.setHint("CheckUrl");
        tilRuleBookAuthor.setHint("RuleBookAuthor");
        tilRuleBookContent.setHint("RuleBookContent");
        tilRuleBookName.setHint("RuleBookName");
        tilRuleChapterList.setHint("RuleChapterList");
        tilRuleChapterName.setHint("RuleChapterName");
        tilRuleChapterUrl.setHint("RuleChapterUrl");
        tilRuleChapterUrlNext.setHint("RuleChapterUrlNext");
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
        tilHttpUserAgent.setHint("HttpUserAgent");
        tilRuleFindUrl.setHint("RuleFindUrl");
        tilRuleContentUrlNext.setHint("RuleContentUrlNext");
    }

    @SuppressLint("SetWorldReadable")
    private void shareBookSource() {
        Bitmap bitmap = mPresenter.encodeAsBitmap(getBookSourceStr());
        try {
            File file = new File(this.getExternalCacheDir(), "bookSource.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            final Intent intent = new Intent(Intent.ACTION_SEND);
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
                SoftInputUtil.hideIMM(this, getCurrentFocus());
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
