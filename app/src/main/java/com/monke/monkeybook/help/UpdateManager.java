package com.monke.monkeybook.help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.UpdateInfoBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.view.activity.UpdateActivity;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

public class UpdateManager {
    private Context context;

    public static UpdateManager getInstance(Context context) {
        return new UpdateManager(context);
    }

    private UpdateManager(Context context) {
        this.context = context;
    }

    public void checkUpdate(boolean showMsg) {
        BaseModelImpl.getRetrofitString("https://api.github.com")
                .create(IHttpGetApi.class)
                .getWebContent(MApplication.getInstance().getString(R.string.latest_release_api), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeLastReleaseApi(response.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<UpdateInfoBean>() {
                    @Override
                    public void onNext(UpdateInfoBean updateInfo) {
                        if (!TextUtils.isEmpty(updateInfo.getLastVersion())) {
                            UpdateActivity.startThis(context, updateInfo);
                        } else if (showMsg) {
                            Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "检测新版本出错", Toast.LENGTH_SHORT).show();
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
                    if (Integer.valueOf(lastVersion.split("\\.")[2]) > Integer.valueOf(thisVersion.split("\\.")[2])) {
                        updateInfo.setUrl(url);
                        updateInfo.setLastVersion(lastVersion);
                        updateInfo.setDetail(detail);
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
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Logger.d("UpdateManager", apkFile.toString());
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSavePath(String fileName) {
        return Environment.getExternalStoragePublicDirectory(DOWNLOAD_SERVICE).getPath() + fileName;
    }
}
