package com.kunfei.bookshelf.view.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.help.UpdateManager;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * Created by GKF on 2017/12/15.
 * 关于
 */

public class AboutActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.vw_version)
    CardView vwVersion;
    @BindView(R.id.tv_donate)
    TextView tvDonate;
    @BindView(R.id.vw_donate)
    CardView vwDonate;
    @BindView(R.id.tv_scoring)
    TextView tvScoring;
    @BindView(R.id.vw_scoring)
    CardView vwScoring;
    @BindView(R.id.tv_git)
    TextView tvGit;
    @BindView(R.id.vw_git)
    CardView vwGit;
    @BindView(R.id.tv_disclaimer)
    TextView tvDisclaimer;
    @BindView(R.id.vw_disclaimer)
    CardView vwDisclaimer;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_mail)
    TextView tvMail;
    @BindView(R.id.vw_mail)
    CardView vwMail;
    @BindView(R.id.tv_update)
    TextView tvUpdate;
    @BindView(R.id.vw_update)
    CardView vwUpdate;
    @BindView(R.id.tv_qq)
    TextView tvQq;
    @BindView(R.id.vw_qq)
    CardView vwQq;
    @BindView(R.id.tv_app_summary)
    TextView tvAppSummary;
    @BindView(R.id.tv_update_log)
    TextView tvUpdateLog;
    @BindView(R.id.vw_update_log)
    CardView vwUpdateLog;
    @BindView(R.id.tv_home_page)
    TextView tvHomePage;
    @BindView(R.id.vw_home_page)
    CardView vwHomePage;
    @BindView(R.id.tv_faq)
    TextView tvFaq;
    @BindView(R.id.vw_faq)
    CardView vwFaq;
    @BindView(R.id.tv_share)
    TextView tvShare;
    @BindView(R.id.vw_share)
    CardView vwShare;

    private MoDialogHUD moDialogHUD;
    private String allQQ[] = new String[]{"(公众号)开源阅读软件", "(QQ群)701903217", "(QQ群)805192012", "(QQ群)773736122", "(QQ群)981838750"};

    public static void startThis(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void initData() {
        moDialogHUD = new MoDialogHUD(this);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        tvVersion.setText(getString(R.string.version_name, MApplication.getVersionName()));
    }

    @Override
    protected void bindEvent() {
        vwDonate.setOnClickListener(view -> DonateActivity.startThis(this));
        vwScoring.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "market://details?id=" + getPackageName()));
        vwMail.setOnClickListener(view -> openIntent(Intent.ACTION_SENDTO, "mailto:kunfei.ge@gmail.com"));
        vwGit.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.this_github_url)));
        vwDisclaimer.setOnClickListener(view -> moDialogHUD.showAssetMarkdown("disclaimer.md"));
        vwUpdate.setOnClickListener(view -> UpdateManager.getInstance(this).checkUpdate(true));
        vwHomePage.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.home_page_url)));
        vwQq.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(AboutActivity.this, view);
            for (String qq : allQQ) {
                popupMenu.getMenu().add(qq);
            }
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                joinGroup(menuItem.getTitle().toString());
                return true;
            });
            popupMenu.show();
        });
        vwUpdateLog.setOnClickListener(view -> moDialogHUD.showAssetMarkdown("updateLog.md"));
        vwFaq.setOnClickListener(view -> moDialogHUD.showAssetMarkdown("faq.md"));
        vwShare.setOnClickListener(view -> {
            String url = "https://www.coolapk.com/apk/com.gedoor.monkeybook";
            Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
                Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(url, 600);
                emitter.onSuccess(bitmap);
            }).compose(RxUtils::toSimpleSingle)
                    .subscribe(new SingleObserver<Bitmap>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Bitmap bitmap) {
                            if (bitmap != null) {
                                moDialogHUD.showImageText(bitmap, url);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        });
    }

    private void joinGroup(String name) {
        String key;
        if (name.equals(allQQ[1])) {
            key = "-iolizL4cbJSutKRpeImHlXlpLDZnzeF";
            if (joinQQGroupError(key)) {
                copyName(name.substring(5));
            }
        } else if (name.equals(allQQ[2])) {
            key = "6GlFKjLeIk5RhQnR3PNVDaKB6j10royo";
            if (joinQQGroupError(key)) {
                copyName(name.substring(5));
            }
        } else if (name.equals(allQQ[3])) {
            key = "5Bm5w6OgLupXnICbYvbgzpPUgf0UlsJF";
            if (joinQQGroupError(key)) {
                copyName(name.substring(5));
            }
        } else if (name.equals(allQQ[4])) {
            key = "g_Sgmp2nQPKqcZQ5qPcKLHziwX_mpps9";
            if (joinQQGroupError(key)) {
                copyName(name.substring(5));
            }
        } else {
            copyName(name.substring(5));
        }
    }

    private void copyName(String name) {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, name);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            toast(R.string.copy_complete);
        }
    }

    private boolean joinQQGroupError(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    void openIntent(String intentName, String address) {
        try {
            Intent intent = new Intent(intentName);
            intent.setData(Uri.parse(address));
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
            actionBar.setTitle(R.string.about);
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        return mo || super.onKeyDown(keyCode, event);
    }

}
