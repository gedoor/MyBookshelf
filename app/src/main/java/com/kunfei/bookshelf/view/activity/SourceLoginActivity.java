package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;

import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.CookieBean;
import com.kunfei.bookshelf.databinding.ActivitySourceLoginBinding;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

public class SourceLoginActivity extends MBaseActivity<IPresenter> {

    private ActivitySourceLoginBinding binding;
    private BookSourceBean bookSourceBean;
    private boolean checking = false;

    public static void startThis(Context context, BookSourceBean bookSourceBean) {
        if (TextUtils.isEmpty(bookSourceBean.getLoginUrl())) {
            return;
        }
        Intent intent = new Intent(context, SourceLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookSourceBean.clone());
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
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivitySourceLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();
    }

    /**
     * 数据初始化
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initData() {
        String key = this.getIntent().getStringExtra("data_key");
        bookSourceBean = (BookSourceBean) BitIntentDataManager.getInstance().getData(key);
        WebSettings settings = binding.webView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        CookieManager cookieManager = CookieManager.getInstance();
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                String cookie = cookieManager.getCookie(url);
                DbHelper.getDaoSession().getCookieBeanDao().insertOrReplace(new CookieBean(bookSourceBean.getBookSourceUrl(), cookie));
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                String cookie = cookieManager.getCookie(url);
                DbHelper.getDaoSession().getCookieBeanDao().insertOrReplace(new CookieBean(bookSourceBean.getBookSourceUrl(), cookie));
                if (checking)
                    finish();
                else
                    showSnackBar(binding.toolbar, getString(R.string.click_check_after_success));
                super.onPageFinished(view, url);
            }
        });
        binding.webView.loadUrl(bookSourceBean.getLoginUrl());
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.login));
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_source_login, menu);
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
                showSnackBar(binding.toolbar, getString(R.string.check_host_cookie));
                binding.webView.loadUrl(bookSourceBean.getBookSourceUrl());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
