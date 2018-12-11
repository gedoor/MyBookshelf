package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.help.ACache;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SourceLoginActivity extends MBaseActivity {
    @BindView(R.id.web_view)
    WebView webView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.action_bar)
    AppBarLayout actionBar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    private BookSourceBean bookSourceBean;
    private boolean checking = false;

    public static void startThis(Context context, BookSourceBean bookSourceBean) {
        if (TextUtils.isEmpty(bookSourceBean.getLoginUrl())) {
            return;
        }
        Intent intent = new Intent(context, SourceLoginActivity.class);
        intent.putExtra("data", bookSourceBean);
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
        bookSourceBean = getIntent().getParcelableExtra("data");
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_source_login);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    /**
     * 数据初始化
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initData() {
        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setUserAgentString(AnalyzeHeaders.getUserAgent(bookSourceBean.getHttpUserAgent()));
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(url);
                ACache.get(SourceLoginActivity.this).put(bookSourceBean.getLoginUrl(), cookies);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(url);
                ACache.get(SourceLoginActivity.this).put(bookSourceBean.getLoginUrl(), cookies);
                if (checking)
                    finish();
                else
                    showSnackBar("登录成功后请点击右上角图标进行首页访问测试");
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl(bookSourceBean.getLoginUrl());
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.login_source, bookSourceBean.getBookSourceName()));
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_check:
                if (checking) break;
                checking = true;
                showSnackBar("正在打开首页，成功自动返回主界面");
                webView.loadUrl(bookSourceBean.getBookSourceUrl());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
