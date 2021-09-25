package com.kunfei.bookshelf.view.activity

import android.R
import android.annotation.SuppressLint
import android.view.MenuItem
import com.kunfei.basemvplib.BitIntentDataManager
import com.kunfei.basemvplib.impl.IPresenter
import com.kunfei.bookshelf.base.MBaseActivity
import com.kunfei.bookshelf.databinding.ActivityWebViewBinding
import com.kunfei.bookshelf.utils.theme.ThemeStore

class WebViewActivity : MBaseActivity<IPresenter>() {

    val binding by lazy {
        ActivityWebViewBinding.inflate(layoutInflater)
    }

    override fun initInjector(): IPresenter? {
        return null
    }

    override fun onCreateActivity() {
        window.decorView.setBackgroundColor(ThemeStore.backgroundColor(this))
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupActionBar()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initData() {
        val settings = binding.webView.settings
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.defaultTextEncodingName = "UTF-8"
        settings.javaScriptEnabled = true
        val url = intent.getStringExtra("url")
        val header = BitIntentDataManager.getInstance().getData(url) as? Map<String, String>
        url?.let {
            if (header == null) {
                binding.webView.loadUrl(url)
            } else {
                binding.webView.loadUrl(url, header)
            }
        }
    }

    //设置ToolBar
    private fun setupActionBar() {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = intent.getStringExtra("title")
        }
    }

    //菜单
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}