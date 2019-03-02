package com.kunfei.bookshelf.help;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kunfei.bookshelf.BuildConfig;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.UpdateInfoBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.view.activity.UpdateActivity;

import java.io.File;

import androidx.core.content.FileProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

public class UpdateManager {
    private Activity activity;

    public static UpdateManager getInstance(Activity activity) {
        return new UpdateManager(activity);
    }

    private UpdateManager(Activity activity) {
        this.activity = activity;
    }

    public void checkUpdate(boolean showMsg) {
        BaseModelImpl.getInstance().getRetrofitString("https://api.github.com")
                .create(IHttpGetApi.class)
                .getWebContent(MApplication.getInstance().getString(R.string.latest_release_api), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeLastReleaseApi(response.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<UpdateInfoBean>() {
                    @Override
                    public void onNext(UpdateInfoBean updateInfo) {
                        if (updateInfo.getUpDate()) {
                            UpdateActivity.startThis(activity, updateInfo);
                        } else if (showMsg) {
                            Toast.makeText(activity, "已是最新版本", Toast.LENGTH_SHORT).show();
                            UpdateActivity.startThis(activity, updateInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (showMsg) {
                            Toast.makeText(activity, "检测新版本出错", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Observable<UpdateInfoBean> analyzeLastReleaseApi(String jsonStr) {
        return Observable.create(emitter -> {
            try {
                UpdateInfoBean updateInfo = new UpdateInfoBean();
                JsonObject version = new JsonParser().parse(jsonStr).getAsJsonObject();
                boolean prerelease = version.get("prerelease").getAsBoolean();
                if (prerelease)
                    return;
                JsonArray assets = version.get("assets").getAsJsonArray();
                if (assets.size() > 0) {
                    String lastVersion = version.get("tag_name").getAsString();
                    String url = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                    String detail = version.get("body").getAsString();
                    String thisVersion = MApplication.getVersionName().split("\\s")[0];
                    updateInfo.setUrl(url);
                    updateInfo.setLastVersion(lastVersion);
                    updateInfo.setDetail("# "+lastVersion +"\n"+ detail);
                    if (Integer.valueOf(lastVersion.split("\\.")[2]) > Integer.valueOf(thisVersion.split("\\.")[2])) {
                        updateInfo.setUpDate(true);
                    } else {
                        updateInfo.setUpDate(false);
                    }
                }
                emitter.onNext(updateInfo);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
                emitter.onComplete();
            }
        });
    }

    /**
     * 安装apk
     */
    public void installApk(File apkFile) {
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.d("wwd", "Failed to launcher installing activity");
        }
    }

    public static String getSavePath(String fileName) {
        return Environment.getExternalStoragePublicDirectory(DOWNLOAD_SERVICE).getPath() + fileName;
    }
}
