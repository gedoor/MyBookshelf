package com.kunfei.bookshelf.view.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.databinding.ActivityDonateBinding;
import com.kunfei.bookshelf.help.Donate;
import com.kunfei.bookshelf.utils.theme.ThemeStore;


/**
 * Created by GKF on 2018/1/13.
 * 捐赠页面
 */

public class DonateActivity extends MBaseActivity<IPresenter> {

    private ActivityDonateBinding binding;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, DonateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityDonateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void bindView() {
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void bindEvent() {
        binding.vwZfbTz.setOnClickListener(view -> Donate.aliDonate(this));
        binding.cvWxGzh.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, "开源阅读");
            if (clipboard != null) {
                clipboard.setPrimaryClip(clipData);
                toast(R.string.copy_complete);
            }
        });
        binding.vwZfbHb.setOnClickListener(view -> openActionViewIntent("https://gitee.com/gekunfei/Donate/raw/master/zfbhbrwm.png"));
        binding.vwZfbRwm.setOnClickListener(view -> openActionViewIntent("https://gitee.com/gekunfei/Donate/raw/master/zfbskrwm.jpg"));
        binding.vwWxRwm.setOnClickListener(view -> openActionViewIntent("https://gitee.com/gekunfei/Donate/raw/master/wxskrwm.jpg"));
        binding.vwQqRwm.setOnClickListener(view -> openActionViewIntent("https://gitee.com/gekunfei/Donate/raw/master/qqskrwm.jpg"));
        binding.vwZfbHbSsm.setOnClickListener(view -> getZfbHb(this));
    }

    public static void getZfbHb(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, "537954522");
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            Toast.makeText(context, "高级功能已开启\n红包码已复制\n支付宝首页搜索“537954522” 立即领红包", Toast.LENGTH_LONG).show();
        }
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone");
            assert intent != null;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MApplication.getInstance().upDonateHb();
        }
    }

    private void openActionViewIntent(String address) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(address));
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
            actionBar.setTitle(R.string.donate);
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
