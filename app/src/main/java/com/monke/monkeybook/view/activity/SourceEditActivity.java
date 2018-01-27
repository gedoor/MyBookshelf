package com.monke.monkeybook.view.activity;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/26.
 * 编辑书源
 */

public class SourceEditActivity extends MBaseActivity {
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
    private String title;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_source_edit);
    }

    @Override
    protected void initData() {
        String key = this.getIntent().getStringExtra("data_key");
        if (isEmpty(key)) {
            title = getString(R.string.add_book_source);
        } else {
            title = getString(R.string.edit_book_source);
            bookSourceBean = (BookSourceBean) BitIntentDataManager.getInstance().getData(key);
            BitIntentDataManager.getInstance().cleanData(key);
        }
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();

        setHint();
        setText();
    }

    private void setText() {
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
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
